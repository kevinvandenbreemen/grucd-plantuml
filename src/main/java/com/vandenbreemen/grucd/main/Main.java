package com.vandenbreemen.grucd.main;

import com.vandenbreemen.grucd.builder.ModelBuilder;
import com.vandenbreemen.grucd.doc.SystemInfo;
import com.vandenbreemen.grucd.model.Model;
import com.vandenbreemen.grucd.model.Type;
import com.vandenbreemen.grucd.parse.ParseJava;
import com.vandenbreemen.grucd.parse.ParseKotlin;
import com.vandenbreemen.grucd.render.plantuml.PlantUMLRenderer;
import com.vandenbreemen.grucd.render.plantuml.PlantUMLScriptGenerator;
import com.vandenbreemen.kevincommon.cmd.CommandLineParameters;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) {

        System.out.println("ARGS:  "+Arrays.asList(args));

        new SystemInfo().print();

        CommandLineParameters params = new CommandLineParameters(args);
        params.addRequired("o", "Where to store the resulting class diagram SVG file or script");
        params.addAtLeast("f", "(or else -d) file you wish to parse and generate UML of");
        params.addAtLeast("d", "(or else -f) directory you wish to parse and generate UML of");
        params.addAtLeast("p", "store plantuml script used to generate diagram to output file but do nothing else");
        params.addAtLeast("u", "Print names of all un-used types or interfaces in the codebase and exit");
        if(!params.validate()) {
            System.out.println(params.document());
            return;
        }

        String outputPath = params.getArgument("o");
        String inputFile = params.getArgument("f");
        String inputDir = params.getArgument("d");

        List<String> filesToVisit = new ArrayList<>();
        if(inputFile != null) {
            logger.info("Parsing single file '"+inputFile+"'");
            filesToVisit.add(inputFile);
        } else {
            logger.info("Parsing directory "+inputDir);
            try {
                Files.walk(Paths.get(inputDir)).filter((filePath)->{return filePath.getFileName().toString().endsWith(".java");}).forEach(path -> {
                    logger.debug("path (java)="+path.toFile().getAbsolutePath());
                    filesToVisit.add(path.toFile().getAbsolutePath());
                });
                Files.walk(Paths.get(inputDir)).filter((filePath)->{return filePath.getFileName().toString().endsWith(".kt");}).forEach(path -> {
                    logger.debug("path (kotlin)="+path.toFile().getAbsolutePath());
                    filesToVisit.add(path.toFile().getAbsolutePath());
                });
            } catch (IOException ioe) {
                logger.error("Failed to get files to parse", ioe);
            }
        }

        ParseJava java = new ParseJava();
        ParseKotlin kotlin = new ParseKotlin();

        List<Type> allTypes = new ArrayList<>();

        filesToVisit.forEach(file->{
            if(file.endsWith(".java")) {
                allTypes.addAll(java.parse(file));
            } else if(file.endsWith(".kt")) {
                allTypes.addAll(kotlin.parse(file));
            }
        });


        PlantUMLRenderer renderer = new PlantUMLRenderer();
        ModelBuilder modelBuilder = new ModelBuilder();

        Model model = modelBuilder.build(allTypes);

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
                return;
            }

            return;
        } else if (params.flag("u")) {
            System.out.println("UNUSED TYPES IN CODEBASE:\n");
            model.getUnusedTypes().forEach((type -> System.out.println(type)));
            return;
        }

        String svgData = renderer.renderSVG(script);

        try(FileWriter fw = new FileWriter(outputPath)) {
            fw.write(svgData);
            fw.flush();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

}
