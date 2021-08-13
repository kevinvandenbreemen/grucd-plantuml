package com.vandenbreemen.grucd.parse;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
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

            unit.getResult().ifPresent(new Consumer<CompilationUnit>() {
                @Override
                public void accept(CompilationUnit unit) {
                    unit.accept(new VoidVisitorAdapter<Void>() {
                        @Override
                        public void visit(ClassOrInterfaceDeclaration n, Void arg) {
                            result.add(new Type(n.getNameAsString()));
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
