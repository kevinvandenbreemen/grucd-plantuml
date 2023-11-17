package com.vandenbreemen.grucd.main;

import com.vandenbreemen.grucd.builder.ModelBuilder;
import com.vandenbreemen.grucd.builder.SourceCodeExtractor;
import com.vandenbreemen.grucd.doc.SystemInfo;
import com.vandenbreemen.grucd.model.Model;
import com.vandenbreemen.grucd.model.Type;
import com.vandenbreemen.grucd.parse.ParseJava;
import com.vandenbreemen.grucd.parse.ParseKotlin;
import com.vandenbreemen.grucd.render.plantuml.PlantUMLRenderer;
import com.vandenbreemen.grucd.render.plantuml.PlantUMLScriptGenerator;
import com.vandenbreemen.kevincommon.cmd.CommandLineParameters;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) {

        generateAndProcessUML(args);
    }

    /**
     * Generate and process the UML, writing it to a file if requested.  Use this method if you want to use the resulting
     * svg
     * @param args
     * @return
     */
    public static String generateAndProcessUML(String[] args) {
        System.out.println("ARGS:  "+Arrays.asList(args));

        new SystemInfo().print();

        CommandLineParameters params = new CommandLineParameters(args);
        params.addAtLeast("o", "Where to store the resulting class diagram SVG file or script");
        params.addAtLeast("f", "(or else -d) file you wish to parse and generate UML of");
        params.addAtLeast("d", "(or else -f) directory you wish to parse and generate UML of");
        params.addAtLeast("p", "store plantuml script used to generate diagram to output file but do nothing else");
        params.addAtLeast("u", "Print names of all un-used types or interfaces in the codebase and exit");
        if(!params.validate()) {
            System.out.println(params.document());
            return "Params invalid";
        }

        String outputPath = params.getArgument("o");
        String inputFile = params.getArgument("f");
        String inputDir = params.getArgument("d");

        SourceCodeExtractor sourceCodeExtractor = new SourceCodeExtractor();
        List<String> filesToVisit = sourceCodeExtractor.getFilenamesToVisit(inputFile, inputDir == null ? "" : inputDir);


        PlantUMLRenderer renderer = new PlantUMLRenderer();

        Model model = sourceCodeExtractor.buildModelWithFiles(filesToVisit);

        PlantUMLScriptGenerator generator = new PlantUMLScriptGenerator();
        String script = generator.render(model);

        if(params.flag("p")) {
            System.out.println(script);

            try {
                try (PrintStream stream = new PrintStream(new FileOutputStream(outputPath))) {
                    stream.println(script);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                return "Error occurred";
            }

            return "";
        } else if (params.flag("u")) {
            System.out.println("UNUSED TYPES IN CODEBASE:\n");
            model.getUnusedTypes().forEach((type -> System.out.println(type)));
            return "";
        }

        String svgData = renderer.renderSVG(script);
        if(outputPath == null) {
            return svgData;
        }

        try(FileWriter fw = new FileWriter(outputPath)) {
            fw.write(svgData);
            fw.flush();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return "Written out to " + outputPath;
    }

}
