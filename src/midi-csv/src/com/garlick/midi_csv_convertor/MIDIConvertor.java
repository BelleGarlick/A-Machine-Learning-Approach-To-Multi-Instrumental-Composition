package com.garlick.midi_csv_convertor;

import java.io.File;

public class MIDIConvertor {
    
    public static final String MINIMUM_SEMI_TONE = "C0"; //Inclusive
    public static final String MAXIMUM_SEMI_TONE = "G#5"; //Inclusive
    public static final float DEFAULT_QUANTISATION = 0.25f;
    
    /**
     * Entry point
     * 
     * @param args Command line inputs
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
    	//args = new String[]{"../../out.mid", "../../test.csv", "C2", "C3", "0.5"};
        //args = new String[]{"mid/", "out/"};
        
        if (args.length >= 2) {
        
            //Get argument Information
            String inputLocation = args[0];
            String outputLocation = args[1];
            String type = (new File(inputLocation)).isDirectory()? "folder" : "file";

            String minimumSemiTone = MINIMUM_SEMI_TONE;
            String maximumSemiTone = MAXIMUM_SEMI_TONE;
            if (args.length > 2) {minimumSemiTone = args[2];}
            if (args.length > 3) {maximumSemiTone = args[3];}

            float quantisation = DEFAULT_QUANTISATION; 
            if (args.length > 4){quantisation = 1 / Float.parseFloat(args[4]);}


            //Output to user
            System.out.println("-- Converting " + type + ": '"+inputLocation+"'->'"+outputLocation+"'"
                    + " between semi-tones '"+minimumSemiTone+"'->'"+maximumSemiTone+"' --");

            //Convert
            switch (type) {
                case "file":
                    parseFile(inputLocation, outputLocation, minimumSemiTone, maximumSemiTone, quantisation);
                    break;
                case "folder":
                    parseFolder(inputLocation, outputLocation, minimumSemiTone, maximumSemiTone, quantisation);
                    break;
            }
        } else {
            System.out.println("==================\n"
                    + "Please enter the required arguments: \n"
                    + "*Input File/Input Path (string)\n"
                    + "*Output File/Output Path (string)\n"
                    + "Minimum Semi-Tone Bound (int)\n"
                    + "Maximum Semi-Tone Bound (int)\n"
                    + "Quantisation Level (int)\n\n"
                    + "For more information, please see: https://github.com/SamGarlick/A-Machine-Learning-Approach-To-Multi-Instrumental-Composition\n"
                    + "==================");
        }
        
    }
    
    
    /**
     * Convert given midi file
     * 
     * @param inputPath Input path to read the midi file from
     * @param outputPath Output path to save file to
     * @param minimum Minimum key bound
     * @param maximum Maximum key bound
     * @param quantisation Quantisation Level
     * @throws Exception 
     */
    private static void parseFile(String inputFile, String outputFile, 
                                String minimum, String maximum,
                                float quantisation) throws Exception {
        
        if (inputFile.endsWith(".midi") || inputFile.endsWith(".mid")) {
            SongMatrix songMatrix = Convertor.createSongMatrix(inputFile, quantisation, minimum, maximum);
            songMatrix.save(outputFile);
            
            System.out.println("-- Finished exporting "+outputFile+" --");
        } else {
            System.out.println("Unkown file type, please try '*.midi' or '*.mid'.");
        }
    }
    
    /**
     * Loop through and save all files in given input path to given output path
     * 
     * @param inputPath Input path to read midi files from
     * @param outputPath Output path to save files to
     * @param minimum Minimum key bound
     * @param maximum Maximum key bound
     * @param quantisation Quantisation Level
     * @throws Exception 
     */
    private static void parseFolder(String inputPath, String outputPath, 
                                String minimum, String maximum,
                                float quantisation) throws Exception {
        
        File inpDir = new File(inputPath);
        File outDir = new File(outputPath);
        if (!outDir.exists()) {
            System.out.println("Creating folder: " + outDir.getAbsolutePath());
            outDir.mkdir();
        }
        
        SongMatrix songMatrix;
        songMatrix = null;
        int convertedFiles = 0;
        
        System.out.println("-- Parsing files in: " + inpDir.getAbsolutePath()+" --");
        for (File f : inpDir.listFiles()) {
            String filePath = f.getPath();
            
            //Check if valid file
            if (filePath.endsWith(".midi") || filePath.endsWith(".mid")) {
                String output = outDir.getAbsolutePath() + "/" + f.getName().split("\\.")[0] + ".csv";
                //Convert and save
                songMatrix = Convertor.createSongMatrix(filePath, quantisation, minimum, maximum);
                songMatrix.save(output);
                convertedFiles++;
            }
        }
        System.out.println("-- Finished exporting "+convertedFiles+" files --");
        
    }
}
