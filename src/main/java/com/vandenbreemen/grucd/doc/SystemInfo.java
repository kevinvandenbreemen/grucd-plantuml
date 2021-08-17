package com.vandenbreemen.grucd.doc;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class SystemInfo {

    private static final Logger logger = Logger.getLogger(SystemInfo.class);

    public String getVersion() {
        try(BufferedReader r = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getSystemResourceAsStream("version.properties")
        ))) {
            return r.readLine().trim();
        }
        catch (IOException ioe){
            logger.fatal("Could not read current system version!", ioe);
            Runtime.getRuntime().halt(1);
        }

        return null;
    }

    private String getAsciiArt() {
        try(BufferedReader r = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getSystemResourceAsStream("art.dat")
        ))) {
            StringBuilder bld = new StringBuilder();
            Scanner scanner = new Scanner(r);
            while(scanner.hasNextLine()) {
                bld.append(scanner.nextLine()).append("\n");
            }

            return bld.toString();
        }
        catch (IOException ioe){
            logger.fatal("Could not read ASCII art resource!", ioe);
            Runtime.getRuntime().halt(1);
        }

        return "";
    }

    public void print() {
        String ascii = getAsciiArt();
        System.out.println(String.format(ascii, getVersion()));
    }

}
