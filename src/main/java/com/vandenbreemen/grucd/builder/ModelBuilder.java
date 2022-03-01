package com.vandenbreemen.grucd.builder;

import com.vandenbreemen.grucd.model.*;

import java.util.*;

/**
 * Once the raw model types have been generated this object assembles the relations between them into a single unified model
 */
public class ModelBuilder {

    /**
     * Attempts to find the type to which a given name refers by looking through the imports into the from type
     * @param from      Type
     * @param allTypes  All types in the software system being documented
     * @param toName    Name of type being referenced by the from type
     * @return          Optional containing a type if it exists
     */
    private Optional<Type> findTypeBasedOnImports(Type from, List<Type> allTypes, String toName) {
        return allTypes.stream().filter(type -> {
            if(type.getName().equals(toName)) {

                if(type.getPkg().equals(from.getPkg())) {
                    return false;
                }

                String expectedImportName = type.getPkg()+"."+type.getName();
                if(from.getImports().stream().filter(i->i.equals(expectedImportName)).findAny().isPresent()) {
                    return true;
                }

            }

            return false;

        }).findFirst();
    }

    /**
     * Attempts to find the type with the given name in the software system
     * @param from
     * @param allTypes
     * @param toName
     * @return
     */
    private Optional<Type> findTypeBasedOnName(Type from, List<Type> allTypes, String toName) {
        return allTypes.stream().filter(type -> {
            return type.getName().equals(toName) && type != from;
        }).findFirst();
    }

    /**
     * Search for the type with the given name
     * @param from
     * @param allTypes
     * @param name
     * @return
     */
    private Optional<Type> findAppropriateType(Type from, List<Type> allTypes, String name) {
        Optional<Type> found = findTypeBasedOnImports(from, allTypes, name);
        if(found.isPresent()) {
            return found;
        }

        return findTypeBasedOnName(from, allTypes, name);
    }

    public Model build(List<Type> types) {
        Model model = new Model(types);
        List<Type> unusedTypes = new ArrayList<>(types);    //  Remove items as we find associative targets

        Map<Type, AbstractSet<Type>> encapsulations = new HashMap<>();
        types.forEach(type -> {
            type.getFields().forEach(field -> {

                findAppropriateType(type, types, field.getTypeName()).ifPresent(targetType->{
                    AbstractSet<Type> targets = encapsulations.computeIfAbsent(type, type1 -> new HashSet<>());
                    targets.add(targetType);
                });

                field.getTypeArguments().forEach(arg->{
                    findAppropriateType(type, types, arg).ifPresent(targetType->{
                        AbstractSet<Type> targets = encapsulations.computeIfAbsent(type, type1 -> new HashSet<>());
                        targets.add(targetType);
                    });
                });
            });
        });

        types.forEach(type -> {
            if(type.getParentType() != null) {
                model.addRelation(new TypeRelation(type.getParentType(), type, RelationType.nested));
            }
        });

        types.forEach(type -> {
            type.getSuperTypeNames().forEach(superTypeName->{
                findAppropriateType(type, types, superTypeName).ifPresent(superType->{
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
                findAppropriateType(type, types, interfaceName).ifPresent(intrface->{
                    model.addRelation(new TypeRelation(type, intrface, RelationType.implementation));
                });
            });
        });

        encapsulations.entrySet().forEach(relationSet->{
            relationSet.getValue().forEach(target->{
                unusedTypes.remove(target);
                model.addRelation(new TypeRelation(relationSet.getKey(), target, RelationType.encapsulates));
            });
        });

        model.setUnusedTypes(unusedTypes);
        return model;

    }

}
