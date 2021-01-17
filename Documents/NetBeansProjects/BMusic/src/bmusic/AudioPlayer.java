/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bmusic;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import static javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import static javax.sound.sampled.AudioSystem.getAudioInputStream;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author ZARD
 */
public class AudioPlayer{
    
    AudioInputStream in;
     SourceDataLine line;
    private String songName;
    
    private byte[] image;
    public BufferedImage getImage(){
        try{
            ByteArrayInputStream bis = new ByteArrayInputStream(image);
            BufferedImage bImage2 = ImageIO.read(bis);
            return  bImage2;
        }catch(Exception ex){
            return null;
        }
    }
    private String filePath;
    public void setPlaySong(String path){
        filePath = path;
        try{
           Mp3File mp3file = new Mp3File(filePath);
           if (mp3file.hasId3v2Tag()) {
               ID3v2 id3v1Tag = mp3file.getId3v2Tag(); 
               songName = id3v1Tag.getTitle();
               artist = id3v1Tag.getArtist();
               songLength = mp3file.getLengthInSeconds();
               image = id3v1Tag.getAlbumImage();
               
           }
        } catch (UnsupportedTagException | InvalidDataException
               | IOException e) {
            throw new IllegalStateException(e);
        }
    }
    public String getSongName(){
        return songName;
    }
    private String artist;
    public String getArtist(){
        return artist;
    }
    private long songLength;
     public long getSongLength(){
        return songLength;
    }
    public void play() {
        final File file = new File(filePath);
        try{
           in = getAudioInputStream(file);
           Mp3File mp3file = new Mp3File(filePath);
           if (mp3file.hasId3v2Tag()) {
               ID3v2 id3v1Tag = mp3file.getId3v2Tag(); 
               songName = id3v1Tag.getTitle();
               artist = id3v1Tag.getArtist();
               songLength = mp3file.getLengthInSeconds();
                MainFrame.slTIme.setMaximum((int)songLength);
           }
           final AudioFormat outFormat = getOutFormat(in.getFormat());
           final DataLine.Info info = new DataLine.Info(SourceDataLine.class, outFormat);
           line = (SourceDataLine) AudioSystem.getLine(info);
           if (line != null) {
               line.open(outFormat);
               line.start();
              
               stream(getAudioInputStream(outFormat, in), line);
               line.drain();
               line.stop();
           }
 
        } catch (UnsupportedAudioFileException 
               | LineUnavailableException | UnsupportedTagException | InvalidDataException
               | IOException e) {
            throw new IllegalStateException(e);
        }
    }
    public void stop(){
        if(line != null){
            line.stop();
        }
    }
    private AudioFormat getOutFormat(AudioFormat inFormat) {
        final int ch = inFormat.getChannels();

        final float rate = inFormat.getSampleRate();
        return new AudioFormat(PCM_SIGNED, rate, 16, ch, ch * 2, rate, false);
    }
 
    private void stream(AudioInputStream in, SourceDataLine line) 
        throws IOException {
        final byte[] buffer = new byte[4096];
        for (int n = 0; n != -1; n = in.read(buffer, 0, buffer.length)) {
            line.write(buffer, 0, n);
            MainFrame.slTIme.setValue((int)TimeUnit.MICROSECONDS.toSeconds(line.getMicrosecondPosition()));
            MainFrame.lbTime.setText(convertTime( (int)TimeUnit.MICROSECONDS.toSeconds(line.getMicrosecondPosition()))+"/"+ convertTime((int)songLength));
        }
    }

    private String convertTime(int timeInSeconds){
        int secondsLeft = timeInSeconds % 3600 % 60;
        int minutes = (int) Math.floor(timeInSeconds % 3600 / 60);
        String MM = ((minutes     < 10) ? "0" : "") + minutes;
        String SS = ((secondsLeft < 10) ? "0" : "") + secondsLeft;

        return MM + ":" + SS;
    }
}
