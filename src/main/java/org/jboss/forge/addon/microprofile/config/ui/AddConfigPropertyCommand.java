package org.jboss.forge.addon.microprofile.config.ui;

import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.microprofile.core.facet.MicroprofileFacet;
import org.jboss.forge.addon.microprofile.core.ui.AbstractEditClassCommand;
import org.jboss.forge.addon.parser.java.beans.FieldOperations;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.hints.InputType;
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
 * Add an injected property <code>
 * 		&#64;Inject
 * 		&#64;String proerty;
 * </code>
 *
 * @author <a href="mailto:jer@printstacktrace.org">Jeremie Lagarde</a>
 */
@FacetConstraint(MicroprofileFacet.class)
public class AddConfigPropertyCommand extends AbstractEditClassCommand
{
   @Inject
   private FieldOperations beanOperations;

   @Inject
   @WithAttributes(label = "Property field name", description = "The property field name to be injected in the configuartion value", required = true)
   private UIInput<String> named;

   @Inject
   @WithAttributes(label = "Property key", description = "The key of the config property used to look up the configuration value", required = true)
   private UIInput<String> key;

   @Inject
   @WithAttributes(label = "Property type", description = "The type intended to be used for this field", type = InputType.JAVA_CLASS_PICKER, required = true)
   private UIInput<String> type;

   @Inject
   @WithAttributes(label = "Default value", description = "The default value if the configured property value does not exist", required = false)
   private UIInput<String> defaultValue;

   @Inject
   @WithAttributes(label = "Is dynamic", description = "If a property is dynamic, its value will be retrieved just in time", defaultValue = "false", required = false)
   private UIInput<Boolean> isDynamic;

   @Override
   public UICommandMetadata getMetadata(UIContext context)
   {
      return Metadata.from(super.getMetadata(context), getClass()).name("MicroProfile: Config: AddProperty")
               .description("Inject MicroProfile Property in your class");
   }

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
      super.initializeUI(builder);
      builder.add(named);
      builder.add(key);
      builder.add(type);
      builder.add(defaultValue);
      builder.add(isDynamic);
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

      FieldSource<?> injectionPoint = source.addField().setName(fieldName).setVisibility(Visibility.PRIVATE);
      injectionPoint.addAnnotation(Inject.class);
      injectionPoint.addAnnotation(ConfigProperty.class);
      injectionPoint.getAnnotation(ConfigProperty.class).setStringValue("name", key.getValue());
      if(defaultValue.hasValue())
         injectionPoint.getAnnotation(ConfigProperty.class).setStringValue("defaultValue", defaultValue.getValue());

      if(isDynamic.hasValue() && isDynamic.getValue())
         injectionPoint.setType("javax.inject.Provider<" + type.getValue() + ">");
      else
         injectionPoint.setType(type.getValue());

      setSource(source);
      return Results.success("The field " + fieldName + " was added.");
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
