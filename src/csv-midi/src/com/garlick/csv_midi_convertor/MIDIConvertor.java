package com.garlick.csv_midi_convertor;

import java.io.File;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;

public class MIDIConvertor {
    
    //Defaults
    public static final String MINIMUM_SEMI_TONE = "C0"; //Inclusive
    public static final float DEFAULT_QUANTISATION = 0.25f;
    public static final int DEFAULT_VELOCITY = 70;
    
    /**
     * Entry point
     * 
     * @param args Command line inputs
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        args = new String[]{
            "in/", 
            "out/", 
            "4",
            "C1",
            "0.5",
            "70"
        };
        
        //Parse Args
        if (args.length > 3) {
            //Extract Required info
            String inputLocation = args[0];
            String outputLocation = args[1];
            int trackCount = Integer.parseInt(args[2]);
            String type = (new File(inputLocation)).isDirectory()? "folder" : "file";

            //Extract Optional info
            String minimumKey = MINIMUM_SEMI_TONE;
            if (args.length > 3) {minimumKey = args[3];}
            
            float quantisation = DEFAULT_QUANTISATION;
            if (args.length > 4) {quantisation = Float.parseFloat(args[4]);}
            
            int velocity = DEFAULT_VELOCITY;
            if (args.length > 5) {velocity = Integer.parseInt(args[5]);}
            

            //Output to user
            System.out.println("-- Converting " + type + ": '"+inputLocation+"'->'"+inputLocation+"'"
                    + " with "+outputLocation + " tracks;"
                    + " Minimum semi-tone " + minimumKey + ";"
                    + " Quantisation: " + quantisation + ";"
                    + " Note Velocity: " + velocity + " --");
            
            
            //Convert
            switch (type) {
                case "file":
                    parseFile(inputLocation, outputLocation, trackCount, minimumKey, quantisation, velocity);
                    break;
                case "folder":
                    parseFolder(inputLocation, outputLocation, trackCount, minimumKey, quantisation, velocity);
                    break;
            }
            
        } else {
            System.out.println("==================\n"
                    + "Please enter the required arguments: \n"
                    + "*Input File/Input Path (string)\n"
                    + "*Output File/Output Path (string)\n"
                    + "*Tracks Count (int)\n"
                    + "Minimum Semi-Tone Bound (String)\n"
                    + "Quantisation Level (float)\n"
                    + "Note Velocity (int)\n\n"
                    + "For more information, please see: https://github.com/SamGarlick/A-Machine-Learning-Approach-To-Multi-Instrumental-Composition\n"
                    + "==================");
        }
    }
    
    
    /**
     * Convert a given CSV to midi file type
     * 
     * @param inputFile Input csv location
     * @param outputFile Output midi location
     * @param trackCount Tracks per midi to be converted
     * @param minimumKey Minimum key bounds
     * @param quantisation Quantisation level for music
     * @param velocity Note Velocity
     * @throws InvalidMidiDataException
     * @throws Exception 
     */
    private static void parseFile(String inputFile, String outputFile,
            int trackCount, String minimumKey, 
            float quantisation, int velocity) throws InvalidMidiDataException, Exception {
        
        Sequence seq = Convertor.encode(inputFile, trackCount, minimumKey, velocity, quantisation);
        MidiSystem.write(seq, 1, new File(outputFile)); 

        System.out.println("-- Finished exporting "+outputFile+" --");
    }
    
    
    /**
     * Loop through files in folder and save as MIDI
     * 
     * @param inputLocation Input path to read CSVs from
     * @param outputLocation Output path to save MIDI to
     * @param trackCount Amount of tracks in each song
     * @param minimumKey Minimum semi tone bound
     * @param quantisation Quantisation level 
     * @param velocity Note velocity
     */
    private static void parseFolder(String inputLocation, String outputLocation, int trackCount, String minimumKey, float quantisation, int velocity) throws Exception {
        
        File inpDir = new File(inputLocation);
        File outDir = new File(outputLocation);
        if (!outDir.exists()) {
            System.out.println("Creating folder: " + outDir.getAbsolutePath());
            outDir.mkdir();
        }
        
        int convertedFiles = 0;
        
        System.out.println("-- Parsing files in: " + inpDir.getAbsolutePath()+" --");
        for (File f : inpDir.listFiles()) {
            String filePath = f.getPath();
            
            //Check if valid file
            String output = outDir.getAbsolutePath() + "/" +  f.getName().split("\\.")[0] + ".mid";
            
            //Convert and save
            Sequence seq = Convertor.encode(filePath, trackCount, minimumKey, velocity, quantisation);
            MidiSystem.write(seq, 1, new File(output)); 
            convertedFiles++;
        }
        System.out.println("-- Finished exporting " + convertedFiles + " files --");
    }
}
