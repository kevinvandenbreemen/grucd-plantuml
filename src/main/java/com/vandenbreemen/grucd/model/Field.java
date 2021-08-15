package com.vandenbreemen.grucd.model;

public class Field {

    private String name;
    private String typeName;

    private boolean show = true;

    private Visibility visibility;

    public Field(String name, String typeName, Visibility visibility) {
        this.name = name;
        this.typeName = typeName;
        this.visibility = visibility;
    }

    public String getName() {
        return name;
    }

    public String getTypeName() {
        return typeName;
    }

    /**
     * Hide this field
     */
    public void hide() {
        this.show = false;
    }

    public boolean shouldShow() {
        return show;
    }

    public Visibility getVisibility() {
        return visibility;
    }
}
