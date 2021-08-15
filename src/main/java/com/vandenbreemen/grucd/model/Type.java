package com.vandenbreemen.grucd.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a class/interface/enum
 */
public class Type {

    private String name;

    private List<Field> fields;

    private List<Method> methods;

    private String pkg;

    private TypeType type;

    public Type(String name, String pkg, TypeType type) {
        this.name = name;
        this.pkg = pkg;
        this.type = type;
        this.fields = new ArrayList<>();
        this.methods = new ArrayList<>();
    }

    public Type(String name, String pkg) {
        this(name, pkg, TypeType.Class);
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

    public void addMethod(Method method) {
        this.methods.add(method);
    }

    public List<Method> getMethods() {
        return methods;
    }

    public String getPkg() {
        return pkg;
    }

    public TypeType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Type{" +
                "name='" + name + '\'' +
                ", fields=" + fields +
                ", methods=" + methods +
                ", pkg='" + pkg + '\'' +
                '}';
    }
}
