package com.vandenbreemen.grucd.parse;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.vandenbreemen.grucd.model.Field;
import com.vandenbreemen.grucd.model.Method;
import com.vandenbreemen.grucd.model.Parameter;
import com.vandenbreemen.grucd.model.Type;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ParseJava {

    private static final Logger logger = Logger.getLogger(ParseJava.class);

    public List<Type> parse(String filePath) {

        logger.debug("Parsing " + filePath);

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

                            if(currentType != null) {
                                NDC.pop();
                            }

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
                            NDC.push(currentType.getName());
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

                        @Override
                        public void visit(MethodDeclaration n, Void arg) {
                            super.visit(n, arg);

                            if(!n.hasModifier(Modifier.publicModifier().getKeyword())) {
                                return;
                            }

                            Method method = new Method(n.getNameAsString(), n.getTypeAsString());

                            logger.trace("mthd:  "+method.getName()+ ": "+method.getReturnType());

                            n.getParameters().forEach(p->{
                                logger.trace("parm "+p.getNameAsString());
                                method.addParameter(new Parameter(p.getNameAsString(), p.getTypeAsString()));
                            });

                            currentType.addMethod(method);
                        }
                    }, null);
                }
            });

            return result;

        } catch (FileNotFoundException fex) {

        } finally {
            NDC.pop();
        }

        return null;
    }

}
