package com.vandenbreemen.grucd.main;

import com.vandenbreemen.grucd.builder.ModelBuilder;
import com.vandenbreemen.grucd.model.Model;
import com.vandenbreemen.grucd.model.Type;
import com.vandenbreemen.grucd.parse.ParseJava;
import com.vandenbreemen.grucd.render.plantuml.PlantUMLRenderer;
import com.vandenbreemen.grucd.render.plantuml.PlantUMLScriptGenerator;
import com.vandenbreemen.kevincommon.cmd.CommandLineParameters;
import org.apache.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) {

        CommandLineParameters params = new CommandLineParameters(args);
        String outputPath = params.getArgument("o");
        String inputFile = params.getArgument("f");
        String inputDir = params.getArgument("d");
        String language = params.getArgument("l");

        List<String> filesToVisit = new ArrayList<>();
        if(inputFile != null) {
            logger.info("Parsing single file '"+inputFile+"'");
            filesToVisit.add(inputFile);
        } else {
            logger.info("Parsing directory "+inputDir);
            try {
                Files.walk(Paths.get(inputDir)).filter((filePath)->{return filePath.getFileName().toString().endsWith(".java");}).forEach(path -> {
                    logger.debug("path="+path.toFile().getAbsolutePath());
                    filesToVisit.add(path.toFile().getAbsolutePath());
                });
            } catch (IOException ioe) {
                logger.error("Failed to get files to parse", ioe);
            }
        }

        ParseJava java = new ParseJava();
        PlantUMLRenderer renderer = new PlantUMLRenderer();
        ModelBuilder modelBuilder = new ModelBuilder();

        List<Type> allTypes = new ArrayList<>();

        filesToVisit.forEach(file->{
            allTypes.addAll(java.parse(file));
        });

        Model model = modelBuilder.build(allTypes);

        PlantUMLScriptGenerator generator = new PlantUMLScriptGenerator();
        String script = generator.render(model);

        String svgData = renderer.renderSVG(script);

        try(FileWriter fw = new FileWriter(outputPath)) {
            fw.write(svgData);
            fw.flush();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

}
