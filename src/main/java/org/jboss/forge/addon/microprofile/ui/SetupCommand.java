package org.jboss.forge.addon.microprofile.ui;

import javax.inject.Inject;

import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.microprofile.facet.MicroprofileFacet;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.hints.InputType;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Metadata;

/**
 * Setup Command to integrate MicroProfile BOM declaration into pom.xml file.
 *
 * @author <a href="mailto:jer@printstacktrace.org">Jeremie Lagarde</a>
 */
public class SetupCommand extends AbstractMicroprofileCommand
{

   @Inject
   private FacetFactory facetFactory;

   @Inject
   private MicroprofileFacet facet;

   @Inject
   @WithAttributes(shortName = 'v', label = "MicroProfile version", type = InputType.DROPDOWN)
   private UISelectOne<String> microprofileVersion;

   @Override
   public UICommandMetadata getMetadata(UIContext context)
   {

      return Metadata.from(super.getMetadata(context), getClass()).name("MicroProfile: Setup")
               .description("Setup MicroProfile in your project");
   }

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
      builder.add(microprofileVersion);

      microprofileVersion.setDefaultValue(() -> facet.getDefaultVersion());
      microprofileVersion.setValueChoices(() -> facet.getAvailableVersions());
   }

   @Override
   public Result execute(UIExecutionContext context) throws Exception
   {
      facet.setVersion(microprofileVersion.getValue());
      facetFactory.install(getSelectedProject(context), facet);
      return Results.success("Command 'setup' successfully executed!");
   }
}