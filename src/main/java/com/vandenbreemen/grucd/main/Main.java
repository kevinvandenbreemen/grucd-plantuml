package com.vandenbreemen.grucd.main;

import com.vandenbreemen.grucd.model.Type;
import com.vandenbreemen.grucd.parse.ParseJava;
import com.vandenbreemen.grucd.render.plantuml.PlantUMLRenderer;
import com.vandenbreemen.grucd.render.plantuml.PlantUMLScriptGenerator;
import com.vandenbreemen.kevincommon.cmd.CommandLineParameters;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        CommandLineParameters params = new CommandLineParameters(args);
        String outputPath = params.getArgument("o");
        String inputFile = params.getArgument("f");
        String language = params.getArgument("l");

        ParseJava java = new ParseJava();
        List<Type> types = java.parse(inputFile);

        PlantUMLScriptGenerator generator = new PlantUMLScriptGenerator();
        PlantUMLRenderer renderer = new PlantUMLRenderer();

        StringBuilder scriptBuilder = new StringBuilder();

        types.forEach(type -> {
            scriptBuilder.append(generator.renderType(type));
        });

        String svgData = renderer.renderSVG(scriptBuilder.toString());

        try(FileWriter fw = new FileWriter(outputPath)) {
            fw.write(svgData);
            fw.flush();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

}
