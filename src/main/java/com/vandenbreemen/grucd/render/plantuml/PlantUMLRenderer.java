package com.vandenbreemen.grucd.render.plantuml;

import com.credibledoc.plantuml.svggenerator.SvgGeneratorService;

/**
 * Provides for rendering PlantUML scripts down to images
 */
public class PlantUMLRenderer {

    public void render(String script) {
        System.out.println(SvgGeneratorService.getInstance().generateSvgFromPlantUml(script));
    }

}
