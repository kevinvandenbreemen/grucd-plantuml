package com.vandenbreemen.grucd.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Software system model
 */
public class Model {

    private List<Type> types;
    private List<TypeRelation> relations;

    public Model(List<Type> types) {
        this.types = types;
        this.relations = new ArrayList<>();
    }

    public void addRelation(TypeRelation relation) {
        this.relations.add(relation);
    }

    public List<Type> getTypes() {
        return types;
    }

    public List<TypeRelation> getRelations() {
        return relations;
    }
}
