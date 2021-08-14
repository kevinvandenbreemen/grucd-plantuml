package com.vandenbreemen.grucd.model;

public class Parameter {

    private String name;
    private String typeName;

    public Parameter(String name, String typeName) {
        this.name = name;
        this.typeName = typeName;
    }

    public String getName() {
        return name;
    }

    public String getTypeName() {
        return typeName;
    }
}
