package com.garlick.midi_csv_convertor;

import java.io.File;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

/**
 *
 * @author Sam Garlick
 */

public class Convertor {
    //MIDI event values
    public static final int NOTE_ON = 0x90;
    public static final int NOTE_OFF = 0x80;
    
    /**
     * Parse the input file to create a song matrix of tracks between the ranges
     * given, quantised to the quantise level given
     * 
     * @param inputFile Path to the input file
     * @param quantisation Quantity of ticks to quantise the data to
     * @param minimumSemiToneString Minimum inclusive semi-tone
     * @param maximumSemiToneString Maximum inclusive semi-tone
     * @return Song Matrix
     * @throws Exception 
     */
    public static SongMatrix createSongMatrix(String inputFile, int quantisation, String minimumSemiToneString, String maximumSemiToneString) throws Exception{
        SongMatrix sm = new SongMatrix();
        
        //Calc the min, max semi tone as integer from input
        int minSemiTone = parseKeyString(minimumSemiToneString);
        int maxSemiTone = parseKeyString(maximumSemiToneString) + 1; //Make maximum inclusive
        int minimumSemiTone = Math.min(minSemiTone, maxSemiTone);
        int maximumSemiTone = Math.max(minSemiTone, maxSemiTone);
        
        //Load midi data
        Sequence sequence = MidiSystem.getSequence(new File(inputFile));
        int trackLength = convertTickToQuantizedBeatUp(getTrackLength(sequence.getTracks()), quantisation);
        
        //Build track data
        int trackNumber = 0;
        int outOfBoundsNotes = 0;
        for (Track track :  sequence.getTracks()) {
            if (!isEmpty(track)){
                TrackMatrix trackMatrix = buildTrackMatrix(trackLength, minimumSemiTone, maximumSemiTone);
                outOfBoundsNotes += parseTrack(track, trackMatrix, quantisation);
                trackNumber++;
                sm.addTrack(trackMatrix);
            }
        }
        System.out.println("Parsed '"+inputFile+ "' with " + trackNumber + " tracks. "+outOfBoundsNotes +" notes were found out of bounds ("+minimumSemiToneString+", "+maximumSemiToneString+")");

        return sm;
    } 
        
    /**
     * Parse a midi track and update the given track matrix
     * 
     * @param track MIDI track
     * @param trackMatrix Track matrix to update
     * @param quantisation Quantisation level for track
     * @return Quantity of notes that fall outside of given track bounds
     */
    static int parseTrack(Track track, TrackMatrix trackMatrix, int quantisation){
        int outOfBoundsNotes = 0;
        for (int i=0; i < track.size(); i++) { 
            MidiEvent event = track.get(i);
            outOfBoundsNotes += parseEvent(event, trackMatrix, quantisation);
        }
        return outOfBoundsNotes;
    }
    
    /**
     * Parse a midi event and update the given track matrix 
     * 
     * @param event The event in the song
     * @param trackMatrix Track matrix to alter
     * @param quantisation Quantisation level
     * @return Quantity of notes outside of the minimum track range
     */
    private static int parseEvent(MidiEvent event, TrackMatrix trackMatrix, int quantisation){    
        int removedNotes = 0;
        MidiMessage message = event.getMessage();        
        if (message instanceof ShortMessage) {
            ShortMessage sm = (ShortMessage) message;
            if (sm.getCommand() == NOTE_ON) {
                int key = sm.getData1();
                trackMatrix.noteOn(key, event.getTick());
            } else if (sm.getCommand() == NOTE_OFF) {
                int key = sm.getData1();
                removedNotes += trackMatrix.noteOff(key, event.getTick(), quantisation);
            }
        } 
        return removedNotes;
    }
    
    /**
     * Create a new track matrix with of a specific length, and semi-tone range
     * 
     * @param length Length of the track matrix
     * @param lowerSemiTone Lowest note
     * @param upperSemiTone Highest Note
     * @return 
     */
    static TrackMatrix buildTrackMatrix(int length, int lowerSemiTone, int upperSemiTone){
        int arrayHeight = upperSemiTone - lowerSemiTone; //Semi Tones
        
        TrackMatrix midiArray = new TrackMatrix(length, arrayHeight, lowerSemiTone, upperSemiTone);        
        return midiArray;
    }
    
    /**
     * Get the length of a track given the notes in the track
     * 
     * @param tracks Tracks to compare
     * @return The longest Track
     */
    static long getTrackLength(Track[] tracks){
        long trackLength = 0;
        for (Track track : tracks){
            trackLength = Math.max(trackLength, track.ticks());
        }
        return trackLength;
    }
    
    /**
     * Quantises a note down
     * 
     * @param length Length of note
     * @param quantisation Quantisation Level
     * @return quantised length
     */
    public static int convertTickToQuantizedBeatDown(long length, int quantisation){
        return (int)Math.floor((float)length/quantisation);
    }
    
    /**
     * Quantises a note up
     * 
     * @param length Length of note
     * @param quantisation Quantisation Level
     * @return quantised length
     */
    public static int convertTickToQuantizedBeatUp(long length, int quantisation){
        return (int)Math.ceil((float)length/quantisation);
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
    public static int parseKeyString(String input) {
        int keyValue = 0;
        for (int index = 0; index < TrackMatrix.NOTE_NAMES.length; index++) {
            if (input.toUpperCase().startsWith(TrackMatrix.NOTE_NAMES[index].toUpperCase())) {
                //Add index of found note
                keyValue += index;
                String remainingChars = input.replace(TrackMatrix.NOTE_NAMES[index], "");
                
                //alter if sharp key
                if (remainingChars.startsWith("#")) {
                    keyValue += 1;
                    remainingChars = remainingChars.substring(1);
                }
                
                //Change octave
                keyValue += (Integer.parseInt(remainingChars) + 2) * 12;
                
                //Break Loop
                index = TrackMatrix.NOTE_NAMES.length;
            }
        }
        return keyValue;
    }
    
    /**
     * Check if a track contains no notes before parsing it
     * 
     * @param track Input track to check
     * @return Boolean stating if the track is empty
     */
    private static boolean isEmpty(Track track){
        boolean empty = true;
        for (int i=0; i < track.size(); i++) { 
            MidiEvent event = track.get(i);
            MidiMessage message = event.getMessage();        
            if (message instanceof ShortMessage) {
                ShortMessage sm = (ShortMessage) message;
                if (sm.getCommand() == NOTE_ON || sm.getCommand() == NOTE_OFF) {
                    empty = false;
                }
            } 
        }
        return empty;
    }
}
