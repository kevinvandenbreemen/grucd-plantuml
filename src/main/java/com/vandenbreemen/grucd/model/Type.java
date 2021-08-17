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

    private Type parentType;    //  For nested classes

    /**
     * Names of all super-types
     */
    private List<String> superTypeNames;

    /**
     * Names of all implemented interfaces
     */
    private List<String> interfaceNames;

    public Type(String name, String pkg, TypeType type) {
        this.name = name;
        this.pkg = pkg;
        this.type = type;
        this.fields = new ArrayList<>();
        this.methods = new ArrayList<>();
        this.superTypeNames = new ArrayList<>();
        this.interfaceNames = new ArrayList<>();
    }

    public Type(String name, String pkg) {
        this(name, pkg, TypeType.Class);
    }

    public void addSuperType(String name) {
        this.superTypeNames.add(name);
    }

    public List<String> getSuperTypeNames() {
        return superTypeNames;
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

    public void setParentType(Type parentType) {
        this.parentType = parentType;
    }

    public Type getParentType() {
        return parentType;
    }

    public void addInterface(String name) {
        this.interfaceNames.add(name);
    }

    public List<String> getInterfaceNames() {
        return interfaceNames;
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
