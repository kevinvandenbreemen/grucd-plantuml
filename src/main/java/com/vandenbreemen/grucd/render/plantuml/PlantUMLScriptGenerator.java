package com.vandenbreemen.grucd.render.plantuml;

import com.vandenbreemen.grucd.model.Field;
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
        writer.unTab().println("}");

        return writer.toString();
    }

}
