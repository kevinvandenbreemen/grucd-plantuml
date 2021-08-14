package com.vandenbreemen.grucd.render.plantuml;

import com.vandenbreemen.grucd.model.*;
import com.vandenbreemen.grucd.util.TabbedWriter;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Renders a diagram model down to PlantUML script for rendering
 */
public class PlantUMLScriptGenerator {

    public String renderType(Type type) {
        TabbedWriter writer = new TabbedWriter();
        writer.println("class " + type.getName() + " {").tab();
        type.getFields().forEach(new Consumer<Field>() {
            @Override
            public void accept(Field field) {
                writer.println("+ " + field.getName() + ": " + field.getTypeName());
            }
        });
        type.getMethods().forEach(new Consumer<Method>() {
            @Override
            public void accept(Method method) {
                StringBuilder parmsBuilder = new StringBuilder();
                method.getParameters().forEach(new Consumer<Parameter>() {
                    @Override
                    public void accept(Parameter parameter) {
                        parmsBuilder.append(parameter.getName()).append(": ").append(parameter.getTypeName()).append(", ");
                    }
                });

                String parms = parmsBuilder.toString();
                if(parms.endsWith(", ")) {
                    parms = parms.substring(0, parms.length()-", ".length());
                }
                writer.println("+ "+method.getName() + "("+parms+"): "+ method.getReturnType());
            }
        });
        writer.unTab().println("}");

        return writer.toString();
    }

    @NotNull
    public String render(@NotNull Model model) {
        StringBuilder script = new StringBuilder();
        model.getTypes().forEach(type->{
            script.append(renderType(type)).append("\n");
        });

        model.getRelations().forEach(relation->{

            String relationshipOperator = null;

            if(relation.getType() == RelationType.encapsulates) {
                relationshipOperator = "o-->";
            }

            if(relationshipOperator != null) {
                script.append(relation.getFrom().getName()).append(" ").append(relationshipOperator).append(" ").append(relation.getTo().getName()).append("\n");
            }
        });

        return script.toString();
    }
}
