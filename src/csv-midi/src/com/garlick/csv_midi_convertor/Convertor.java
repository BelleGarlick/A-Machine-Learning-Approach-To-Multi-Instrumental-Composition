package com.garlick.csv_midi_convertor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

public class Convertor {
    
    public static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    
    /**
     * Encode CSV file as MIDI
     * 
     * @param in File location 
     * @param trackCount Tracks in matrix
     * @param minimumKey minimum key
     * @param velocity default velocity for a note
     * @param inQuantisation quantisation level
     * @return Return MIDI sequence
     * @throws FileNotFoundException
     * @throws IOException
     * @throws InvalidMidiDataException
     * @throws Exception 
     */
    public static Sequence encode(String in, int trackCount, String minimumKey, int velocity, float inQuantisation) throws FileNotFoundException, IOException, InvalidMidiDataException, Exception {
        Sequence seq = new Sequence(Sequence.PPQ, 120);
        int minKey = parseKeyString(minimumKey);
        int quantisation = (int) (120 * inQuantisation);
        
        ArrayList<ArrayList<Float>> songMatrix = convertFileToMatrix(in);
        
        int trackSize = songMatrix.size() / trackCount;
        String notesList = "{";
        for (int trackNo = 0; trackNo < trackCount; trackNo++) {
            if (trackNo > 0){notesList += ", ";}
            Track t = seq.createTrack();
            ArrayList<ArrayList<Float>> keys = getSongMatrixRange(songMatrix, trackNo * trackSize, (trackNo + 1) * trackSize);
            int noteCount = decodeTrack(t, keys, minKey, velocity, quantisation);
            notesList += noteCount;
        }
        System.out.println("Converted file: " + new File(in).getName() + "; " + trackCount + " tracks with " + notesList+"} notes.");
        return seq;   
    }
    
    /**
     * Seperate out full song matrix into sub track matrix
     * 
     * @param arr Array to extract from
     * @param s Start level
     * @param e End level
     * @return 
     */
    private static ArrayList<ArrayList<Float>> getSongMatrixRange(ArrayList<ArrayList<Float>> arr, int s, int e) {
        ArrayList<ArrayList<Float>> range = new ArrayList<>();
        for (int i = s; i < e; i++) {
            range.add(arr.get(i));
        }
        return range;
    }
    
    /**
     * Load CSV file into matrix of floats
     * 
     * @param location File location to load from
     * @return Matrix of floats
     * @throws FileNotFoundException
     * @throws IOException 
     */
    private static ArrayList<ArrayList<Float>> convertFileToMatrix(String location) throws FileNotFoundException, IOException {
        ArrayList<ArrayList<Float>> songMatrix = new ArrayList<>();
        File f = new File(location);
        if (f.exists()){
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            while ((line = br.readLine()) != null) {
                if (line.length() > 5) {
                    ArrayList<Float> row = new ArrayList<>();
                    String[] tokens = line.split(",");
                    for (String s : tokens) {
                        s = s.replaceAll(" ", "");
                        row.add(Float.parseFloat(s));
                    }
                    songMatrix.add(row);
                }
            }
            br.close();
        } else {
            System.out.println("Cannot find file: " + f.getAbsolutePath());
        }
        return songMatrix;
    }
    
    
    /**
     * Decode a track using ActiveOnEncoding
     * 
     * @param t Track to append notes to
     * @param notes CSV matrix of notes
     * @param minimumKey minimum key
     * @param velocity velocity of notes 
     * @param quantisation quantisation level
     * @return quantity of notes in track
     * @throws InvalidMidiDataException
     * @throws Exception 
     */
    private static int decodeTrack(Track t, ArrayList<ArrayList<Float>> notes, int minimumKey, int velocity, int quantisation) throws InvalidMidiDataException, Exception {
        return ActiveOnEncoding.decode(t, notes, minimumKey, velocity, quantisation);
    }
    
    /**
     * Converts a semi-tone string into an integer index.
     * 0 = C-2,
     * 1 = C#-2,
     * 2 = D-2...
     * 
     * @param input Semi-tone string
     * @return key index
     */
    private static int parseKeyString(String input) {
        int keyValue = 0;
        for (int index = 0; index < NOTE_NAMES.length; index++) {
            if (input.toUpperCase().startsWith(NOTE_NAMES[index].toUpperCase())) {
                //Add index of found note
                keyValue += index;
                String remainingChars = input.replace(NOTE_NAMES[index], "");
                
                //alter if sharp key
                if (remainingChars.startsWith("#")) {
                    keyValue += 1;
                    remainingChars = remainingChars.substring(1);
                }
                
                //Change octave
                keyValue += (Integer.parseInt(remainingChars) + 2) * 12;
                
                //Break Loop
                index = NOTE_NAMES.length;
            }
        }
        return keyValue;
    }
}
