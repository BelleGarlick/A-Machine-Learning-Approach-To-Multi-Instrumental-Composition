package com.garlick.midi_csv_convertor;

import java.util.ArrayList;

import javax.sound.midi.Track;

public class TrackMatrix {
    
    //Buffer to store known active notes, before notes are turn off
    ArrayList<ActiveNote> activeNotes = new ArrayList<>();
    
    //Stored list of notes to compare against for given input key
    public static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    
    //Stored track data and semi-tone bounds
    private final int[][] trackMatrix;
    private int minimumSemiTone = 0;
    private int maximumSemiTone = 127;
    
    /**
     * Initialise a track matrix which will be appended to the song matrix
     * 
     * @param width Length of the song / width of the track
     * @param height height/semi-tones in the track
     * @param minimumSemiTone Minimum key
     * @param maximumSemiTone Maximum key
     */
    public TrackMatrix(int width, int height, int minimumSemiTone, int maximumSemiTone){
        trackMatrix = new int[height * 2][width];
        this.minimumSemiTone = minimumSemiTone;
        this.maximumSemiTone = maximumSemiTone;
    }
    
    /**
     * Turn a key on at a particular time for a particular key
     * 
     * @param key The key to turn a note on
     * @param eventTick The tick the note turn on 
     */
    public void noteOn(int key, long eventTick){
        ActiveNote an = new ActiveNote();
        an.key = key;
        an.startTime = eventTick;
        activeNotes.add(an);
    }
    
    /**
     * Turn a note off for a key at a tick with a certain quantisation level
     * 
     * @param key Key to turn off
     * @param eventTick Tick to turn note off
     * @param quantisation Quantisation level for track
     * @return return 1 if note is out of semi-tone range
     */
    public int noteOff(int key, long eventTick, int quantisation){
        int keyOutOfBounds = 0;
        
        ActiveNote activatedNote = null;
        for (ActiveNote an : activeNotes){
            if (an.key == key){
                keyOutOfBounds += addNote(key, an.startTime, eventTick, quantisation);
                activatedNote = an;
            }
        }
                
        if (activatedNote != null){
            this.activeNotes.remove(activatedNote);
        }
        
        return keyOutOfBounds;
    }  

    /**
     * If any notes were not correctly disposed in the MIDI file
     * the notes will be turn of here
     * 
     * @param quantisation Quantisation of the song
     */
	public void removeInactiveNotes(int quantisation) {
        for (ActiveNote an : activeNotes){
        	if (an.startTime < this.trackMatrix[0].length * quantisation) {
                addNote(an.key, an.startTime, this.trackMatrix[0].length * quantisation, quantisation);
        	}
        }
	}

    /**
     * Add a semi-tone to the track matrix
     * 
     * @param key Semi-tone of note
     * @param start start index of note
     * @param end End index of note
     * @param quantisation quantisation level
     * @return 1 if the note was outside of semi-tone bounds
     */
    private int addNote(int key, long start, long end, int quantisation){
        int relativeKey = key - this.minimumSemiTone;
        int startPos = Convertor.convertTickToQuantizedBeatDown(start, quantisation);
        int endPos = Convertor.convertTickToQuantizedBeatUp(end, quantisation) - 1;
        
        int activeKeyPos = relativeKey * 2;
        int activeKeyOnPos = activeKeyPos + 1;
        
        if (relativeKey >= 0 && relativeKey < this.maximumSemiTone - this.minimumSemiTone) {
            this.trackMatrix[activeKeyOnPos][startPos] = 1;
            for (int i = startPos; i <= endPos; i++){
                this.trackMatrix[activeKeyPos][i] = 1;
            }
            
            return 0;
        } else { 
            return 1;
        }
    }
    
    /**
     * 
     * @return The generated track matrix
     */
    int[][] getMatrix() {
        return this.trackMatrix;
    }
    
    /**
     * Class to store a known active note
     */
    private static class ActiveNote {        
        public int key = 0;
        public long startTime = 0;        
    }
}
