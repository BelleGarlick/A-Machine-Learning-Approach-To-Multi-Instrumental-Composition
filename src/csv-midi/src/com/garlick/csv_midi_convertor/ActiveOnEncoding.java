package com.garlick.csv_midi_convertor;

import java.util.ArrayList;
import java.util.HashSet;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

/**
 *
 * @author Sam
 */
public class ActiveOnEncoding {
    
    /**
     * Refer to paper for how this encoding / decoding works
     * 
     * @param t Track to append notes to
     * @param notes CSV Array of notes to extract from
     * @param minimumKey Minimum key to alter notes by
     * @param velocity default of notes
     * @param quantisation Quantisation level
     * @return Integer of notes in track
     * @throws InvalidMidiDataException 
     */
    public static int decode(Track t, ArrayList<ArrayList<Float>> notes, int minimumKey, int velocity, int quantisation) throws InvalidMidiDataException {
        int notesCount = 0;
        //Refresh HashMap
        HashSet<Integer> activeKeys = new HashSet<>();
        
        for (int i = 0; i < notes.size()/2; i++) {activeKeys.remove(i);}
        
        for (int seqNo = 0; seqNo < notes.get(0).size(); seqNo++) {
//            System.out.println("Track: " + trackNo + " Sequence#:" + seqNo);
            for (int keyPair = 0; keyPair < notes.size(); keyPair = keyPair + 2) {
                //int seqNo = seqNo;
                int keyNo = (keyPair / 2);     
                int key = keyNo + minimumKey;
                boolean keyActive = notes.get(keyPair).get(seqNo) > 0.5;
                boolean keyStart = notes.get(keyPair + 1).get(seqNo) > 0.5;
                
                //Decide whether to turn on or off
                boolean turnKeyOff = false;
                boolean turnKeyOn = false;
                
                if (!keyActive && !keyStart) {
                    //Both bit off (00)
                    if (activeKeys.contains(keyNo)) {
                        turnKeyOff = true;
                    }
                   
                } else if (keyActive && !keyStart) {
                    //Key Active But No Start (10)
                    if (!activeKeys.contains(keyNo)) {
                        turnKeyOn = true;
                    }
                    
                } else if (keyStart) {
                    //Key not active, but is start (01) ===> (Techically an error but is same as new key starting)
                    //Or key is started (11)
                    if (activeKeys.contains(keyNo)) {
                        turnKeyOff = true;
                    }
                    turnKeyOn = true;
                }
                
                
                //Turn Nots on And Off
                if (turnKeyOff) {
                    activeKeys.remove(keyNo);
                    ShortMessage off = new ShortMessage();
                    off.setMessage(ShortMessage.NOTE_OFF, 0, key, velocity);
                    t.add(new MidiEvent(off,(long) seqNo * quantisation));
                }
                if (turnKeyOn) {
                    notesCount++;
                    activeKeys.add(keyNo);
                    ShortMessage on = new ShortMessage();
                    on.setMessage(ShortMessage.NOTE_ON, 0, key, velocity);
                    t.add(new MidiEvent(on,(long) seqNo * quantisation));
                }
            }
        }
        
        //Remove any left over notes 
        for (Integer i : new HashSet<>(activeKeys)) {
            activeKeys.remove(i);
            ShortMessage off = new ShortMessage();
            off.setMessage(ShortMessage.NOTE_OFF, 0, i + minimumKey, velocity);
            t.add(new MidiEvent(off,(long) notes.get(0).size()));
        }
        
        return notesCount;
    }
}
