package com.vandenbreemen.grucd.parse;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.vandenbreemen.grucd.model.Field;
import com.vandenbreemen.grucd.model.Type;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ParseJava {

    public List<Type> parse(String filePath) {
        try {
            JavaParser parser = new JavaParser();
            ParseResult<CompilationUnit> unit = parser.parse(new File(filePath));

            List<Type> result = new ArrayList<>();


            unit.getResult().ifPresent(new Consumer<>() {
                @Override
                public void accept(CompilationUnit unit) {
                    unit.accept(new VoidVisitorAdapter<Void>() {

                        private Type currentType;

                        @Override
                        public void visit(ClassOrInterfaceDeclaration n, Void arg) {

                            String name = n.getNameAsString();
                            final String[] packageName = {""};
                            n.getFullyQualifiedName().ifPresent(new Consumer<String>() {
                                @Override
                                public void accept(String fullName) {
                                    if(fullName.length() > name.length()) {
                                        packageName[0] = fullName.substring(0, fullName.length() - (name.length() + 1));
                                    }
                                }
                            });

                            currentType = new Type(n.getNameAsString(), packageName[0]);
                            result.add(currentType);
                            super.visit(n, arg);
                        }

                        @Override
                        public void visit(FieldDeclaration n, Void arg) {
                            if(n.getModifiers().contains(Modifier.publicModifier())) {
                                for (VariableDeclarator dec : n.getVariables()) {
                                    Field field = new Field(dec.getName().asString(), dec.getTypeAsString());
                                    currentType.addField(field);
                                }
                            }
                        }
                    }, null);
                }
            });

            return result;

        } catch (FileNotFoundException fex) {

        }

        return null;
    }

}
