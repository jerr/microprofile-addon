package org.jboss.forge.addon.microprofile.core.ui;

import java.io.FileNotFoundException;
import java.util.List;

import javax.inject.Inject;

import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.javaee.cdi.CDIOperations;
import org.jboss.forge.addon.microprofile.core.facet.MicroprofileFacet;
import org.jboss.forge.addon.parser.java.resources.JavaResource;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UISelection;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.hints.InputType;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.roaster.model.source.JavaClassSource;

/**
 * Abstract Command to edit class code in MicroPorfile Addon context
 *
 * @author <a href="mailto:jer@printstacktrace.org">Jeremie Lagarde</a>
 */
@FacetConstraint(MicroprofileFacet.class)
public abstract class AbstractEditClassCommand extends AbstractMicroprofileCommand
{

   @Inject
   private CDIOperations cdiOperations;
   
   @Inject
   @WithAttributes(label = "Target Class", description = "The class to edit", required = true, type = InputType.DROPDOWN)
   private UISelectOne<JavaResource> targetClass;

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
      super.initializeUI(builder);

      // Elements in the command execution context
      UIContext uiContext = builder.getUIContext();
      UISelection<FileResource<?>> selection = uiContext.getInitialSelection();
      Project project = getSelectedProject(uiContext);

      // Candidate classes
      final List<JavaResource> classes = cdiOperations.getProjectInjectionPointBeans(project);
      targetClass.setValueChoices(classes);

      // Class selected to edit
      int selectedClassIndex = classes.size() - 1;
      if (!selection.isEmpty() && classes.contains(selection.get()))
      {
         selectedClassIndex = classes.indexOf(selection.get());
      }
      if (selectedClassIndex >= 0)
      {
         targetClass.setDefaultValue(classes.get(selectedClassIndex));
      }

      // Adding UI
      builder.add(targetClass);
   }

   @Override
   public void validate(UIValidationContext validator)
   {
      super.validate(validator);
      if (getSource() == null)
         validator.addValidationError(targetClass, "Java class source could not be found");
   }

   protected JavaClassSource getSource()
   {
      try
      {
         JavaResource javaResource = targetClass.getValue();
         if (javaResource != null)
         {
            return javaResource.getJavaType();
         }
      }
      catch (FileNotFoundException ffe)
      {
      }
      return null;
   }

   protected void setSource(JavaClassSource source)
   {
      JavaResource javaResource = targetClass.getValue();
      if (javaResource != null)
      {
         javaResource.setContents(source);
      }
   }
}
