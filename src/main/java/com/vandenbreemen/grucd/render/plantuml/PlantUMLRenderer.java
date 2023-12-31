package com.vandenbreemen.grucd.render.plantuml;

import org.apache.log4j.Logger;

/**
 * Provides for rendering PlantUML scripts down to images
 */
public class PlantUMLRenderer {

    private static final Logger logger = Logger.getLogger(PlantUMLRenderer.class);

    public String renderSVG(String script) {

        logger.debug("Rendering PlantUML script:\n----------------------------------\n" + script + "\n----------------------------------");
        return new PlantUmlDirectRenderer().render(script);
    }

}
