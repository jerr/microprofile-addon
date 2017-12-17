package org.jboss.forge.addon.microprofile.config.ui;

import javax.inject.Inject;

import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.microprofile.core.facet.MicroprofileFacet;
import org.jboss.forge.addon.microprofile.core.ui.AbstractEditClassCommand;
import org.jboss.forge.addon.parser.java.beans.FieldOperations;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UIPrompt;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.roaster.model.Visibility;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;

/**
 * Inject Configuration into a class
 *
 * <pre>
 * <code>
 * 		&#64;Inject
 * 		Config config
 * </code>
 * </pre>
 *
 * @author <a href="mailto:jer@printstacktrace.org">Jeremie Lagarde</a>
 */
@FacetConstraint(MicroprofileFacet.class)
public class AddConfigCommand extends AbstractEditClassCommand
{

   @Inject
   private FieldOperations beanOperations;

   @Inject
   @WithAttributes(label = "Config field name", defaultValue = "config", description = "The config field name to be injected in the configuartion bean", required = false)
   private UIInput<String> named;

   @Override
   public UICommandMetadata getMetadata(UIContext context)
   {
      return Metadata.from(super.getMetadata(context), getClass()).name("MicroProfile: Config: AddConfig")
               .description("Inject MicroProfile Config bean in your class");
   }

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
      super.initializeUI(builder);
      builder.add(named);
   }

   @Override
   public Result execute(UIExecutionContext context) throws Exception
   {
      String fieldName = named.getValue();
      JavaClassSource source = getSource();
      FieldSource<JavaClassSource> field = source.getField(fieldName);
      if (field != null)
      {
         UIPrompt prompt = context.getPrompt();
         if (prompt.promptBoolean("Field '" + field.getName() + "' already exists. Do you want to overwrite it?"))
         {
            beanOperations.removeField(source, field);
         }
         else
         {
            return Results.fail("Field '" + field.getName() + "' already exists.");
         }
      }

      FieldSource<?> injectionPoint = source.addField().setName(fieldName).setVisibility(Visibility.PRIVATE)
               .setType("org.eclipse.microprofile.config.Config");
      injectionPoint.addAnnotation(Inject.class);

      setSource(source);
      return Results.success("Config " + fieldName + " was added.");
   }

   @Override
   public void validate(UIValidationContext validator)
   {
      super.validate(validator);
      JavaClassSource source = getSource();
      if (source.hasField(named.getValue()))
      {
         validator.addValidationWarning(named, "Field '" + named.getValue() + "' already exists");
      }
   }
}
