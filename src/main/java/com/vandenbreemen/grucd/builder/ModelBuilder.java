package com.vandenbreemen.grucd.builder;

import com.vandenbreemen.grucd.model.*;

import java.util.*;

/**
 * Once the raw model types have been generated this object assembles the relations between them into a single unified model
 */
public class ModelBuilder {

    public Model build(List<Type> types) {
        Model model = new Model(types);

        Map<Type, AbstractSet<Type>> encapsulations = new HashMap<>();
        types.forEach(type -> {
            types.forEach(targetType->{
                if(type != targetType) {

                    type.getFields().forEach(field -> {
                        if(field.getTypeName().equals(targetType.getName())) {
                            AbstractSet<Type> targets = encapsulations.computeIfAbsent(type, type1 -> new HashSet<>());
                            targets.add(targetType);
                        }
                        field.getTypeArguments().forEach(arg->{
                            if(arg.equals(targetType.getName())) {
                                AbstractSet<Type> targets = encapsulations.computeIfAbsent(type, type1 -> new HashSet<>());
                                targets.add(targetType);
                            }
                        });
                    });

                }
            });
        });

        types.forEach(type -> {
            if(type.getParentType() != null) {
                model.addRelation(new TypeRelation(type.getParentType(), type, RelationType.nested));
            }
        });

        types.forEach(type -> {
            type.getSuperTypeNames().forEach(superTypeName->{
                types.stream().filter(t->t.getName().equals(superTypeName)).findFirst().ifPresent(superType->{

                    if(superType.getType() == TypeType.Interface) {
                        model.addRelation(new TypeRelation(type, superType, RelationType.implementation));
                    } else {
                        model.addRelation(new TypeRelation(type, superType, RelationType.subclass));
                    }
                });
            });
        });

        types.forEach(type->{
            type.getInterfaceNames().forEach(interfaceName->{
                types.stream().filter(t->t.getName().equals(interfaceName)).findFirst().ifPresent(intrface->{
                    model.addRelation(new TypeRelation(type, intrface, RelationType.implementation));
                });
            });
        });

        encapsulations.entrySet().forEach(relationSet->{
            relationSet.getValue().forEach(target->{
                model.addRelation(new TypeRelation(relationSet.getKey(), target, RelationType.encapsulates));
            });
        });

        return model;

    }

}
