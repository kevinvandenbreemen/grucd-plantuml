package com.vandenbreemen.grucd.render.plantuml;

import com.vandenbreemen.grucd.model.Field;
import com.vandenbreemen.grucd.model.Method;
import com.vandenbreemen.grucd.model.Parameter;
import com.vandenbreemen.grucd.model.Type;
import com.vandenbreemen.grucd.util.TabbedWriter;

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
                        parmsBuilder.append(parameter.getName()).append(": ").append(parameter.getTypeName());
                    }
                });
                writer.println("+ "+method.getName() + "("+parmsBuilder.toString()+"): "+ method.getReturnType());
            }
        });
        writer.unTab().println("}");

        return writer.toString();
    }

}
