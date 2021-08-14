package com.vandenbreemen.grucd.model;


/**
 * Relation between two classes
 */
public class TypeRelation {

    private Type from;
    private Type to;
    private RelationType type;

    public TypeRelation(Type from, Type to, RelationType type) {
        this.from = from;
        this.to = to;
        this.type = type;
    }

    public Type getFrom() {
        return from;
    }

    public Type getTo() {
        return to;
    }

    public RelationType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "TypeRelation{" +
                "from=" + from +
                ", to=" + to +
                ", type=" + type +
                '}';
    }
}
