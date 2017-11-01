package org.jboss.forge.addon.microprofile.facet;

import static java.util.stream.Collectors.toList;

import java.util.List;

import javax.inject.Inject;

import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.dependencies.Dependency;
import org.jboss.forge.addon.dependencies.DependencyResolver;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.dependencies.builder.DependencyQueryBuilder;
import org.jboss.forge.addon.facets.AbstractFacet;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFacet;
import org.jboss.forge.addon.projects.facets.DependencyFacet;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.addon.projects.facets.ResourcesFacet;

/**
 * The Microprofile Facet
 *
 * @author <a href="mailto:jer@printstacktrace.org">Jeremie Lagarde</a>
 */
@FacetConstraint(DependencyFacet.class)
@FacetConstraint(MetadataFacet.class)
@FacetConstraint(ProjectFacet.class)
@FacetConstraint(ResourcesFacet.class)
@FacetConstraint(MavenFacet.class)
public class MicroprofileFacet extends AbstractFacet<Project> implements ProjectFacet
{

   private static final String MICROPROFILE_VERSION_PROPERTY = "version.microprofile";
   private static final String MICROPROFILE_VERSION_DEFAULT = "1.2";

   public static final Dependency MICROPROFILE_DEPENDENCY = DependencyBuilder
            .create().setGroupId("org.eclipse.microprofile")
            .setArtifactId("microprofile");

   public static final Dependency MICROPROFILE_BOM = DependencyBuilder
            .create().setGroupId("org.eclipse.microprofile")
            .setArtifactId("microprofile")
            .setVersion("${" + MICROPROFILE_VERSION_PROPERTY + "}")
            .setPackaging("pom")
            .setScopeType("import");

   @Inject
   private DependencyResolver resolver;

   private String version;

   public String getDefaultVersion()
   {
      List<String> availableBOMs = getAvailableVersions();
      if (availableBOMs == null || availableBOMs.isEmpty())
      {
         return MICROPROFILE_VERSION_DEFAULT;
      }
      for (int i = availableBOMs.size() - 1; i >= 0; i--)
      {
         String version = availableBOMs.get(i);
         if (!version.endsWith("SNAPSHOT"))
         {
            return version;
         }
      }
      return availableBOMs.get(availableBOMs.size() - 1);
   }

   public List<String> getAvailableVersions()
   {
      List<Coordinate> coordinates = resolver
               .resolveVersions(DependencyQueryBuilder.create(MICROPROFILE_DEPENDENCY.getCoordinate()));
      if (coordinates == null)
         return null;

      return coordinates.stream().map(c -> c.getVersion()).collect(toList());
   }

   @Override
   public boolean install()
   {
      addMicroprofileVersionProperty();
      addMicroprofileBOMDependency();
      return isInstalled();
   }

   @Override
   public boolean isInstalled()
   {
      DependencyFacet dependencyFacet = getFaceted().getFacet(DependencyFacet.class);
      boolean hasBOM = dependencyFacet.hasEffectiveManagedDependency(MICROPROFILE_BOM);

      MetadataFacet metadataFacet = getFaceted().getFacet(MetadataFacet.class);
      boolean hasVersion = metadataFacet.getEffectiveProperty(MICROPROFILE_VERSION_PROPERTY) == null;

      return hasBOM && hasVersion;
   }

   private void addMicroprofileVersionProperty()
   {

      MetadataFacet metadataFacet = getFaceted().getFacet(MetadataFacet.class);

      String installedVersion = metadataFacet.getDirectProperty(MICROPROFILE_VERSION_PROPERTY);
      if (installedVersion == null || !installedVersion.equals(getVersion()))
      {
         metadataFacet.setDirectProperty(MICROPROFILE_VERSION_PROPERTY, getVersion());
      }
   }

   private void addMicroprofileBOMDependency()
   {
      DependencyFacet dependencyFacet = getFaceted().getFacet(DependencyFacet.class);
      dependencyFacet.addDirectManagedDependency(MICROPROFILE_BOM);
   }

   public void setVersion(String version)
   {
      this.version = version;
   }

   private String getVersion()
   {
      return version != null ? version : getDefaultVersion();
   }
}
