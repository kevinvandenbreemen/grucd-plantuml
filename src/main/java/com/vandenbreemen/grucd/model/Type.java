package com.vandenbreemen.grucd.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a class/interface/enum
 */
public class Type {

    private String name;

    private List<Field> fields;

    private String pkg;

    public Type(String name, String pkg) {
        this.name = name;
        this.pkg = pkg;
        this.fields = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void addField(Field field) {
        fields.add(field);
    }

    public String getPkg() {
        return pkg;
    }
}
