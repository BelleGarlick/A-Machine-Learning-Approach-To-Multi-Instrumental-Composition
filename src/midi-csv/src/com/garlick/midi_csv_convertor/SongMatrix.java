package com.garlick.midi_csv_convertor;

import java.io.FileWriter;
import java.io.IOException;


public class SongMatrix {
    
    //Song matrix created 
    int[][] songMatrix = null;
    
    /**
     * Append a track matrix to the created song matrix
     * 
     * @param matrix New Track matrix
     */
    public void addTrack(TrackMatrix matrix){
        if (songMatrix == null ){
            songMatrix = matrix.getMatrix();
        } else {
            int[][] newSongMatrix = new int[songMatrix.length + matrix.getMatrix().length][songMatrix[0].length];
            
            for (int i = 0; i < songMatrix.length; i++){
                newSongMatrix[i] = songMatrix[i];
            }
            for (int i = 0; i < matrix.getMatrix().length; i++){
                newSongMatrix[songMatrix.length + i] = matrix.getMatrix()[i];
            }
            songMatrix = newSongMatrix;
        }
    }

    
    /**
     * Save the song matrix to a CSV file
     * 
     * @param filename File location to save to
     * @throws IOException 
     */
    public void save(String filename) throws IOException {        
        try (FileWriter fw = new FileWriter(filename)) {
            fw.write(toCsv());
        }
    }
    
    /**
     * Convert song matrix to CSV string
     * 
     * @return CSV
     */
    private String toCsv(){
        String csv = "";
        for (int[] row : this.songMatrix){
            if (csv.length()>0){
                csv += "\n";
            }
            String currentRow = "";
            for (int col : row){
                if (currentRow.length() > 0){
                    currentRow += ", ";
                }
                currentRow += col;
            }
            csv += currentRow;
        }
        return csv;
    }
}
