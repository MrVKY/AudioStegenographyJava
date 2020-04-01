package com.mycompany.stegenography;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import javax.swing.JFileChooser;

public class Stegenography {
    static String path = "";
    static String outputPath = "";
    static byte[] audioBytes;
    static byte[] outputAudioBytes;
    static byte[] textDataBytes;
    static AudioInputStream audioInputStream;
    static boolean pathOk;
    //read Audio file from the path and return the bytes of the audio file recursively
    public static byte[] readAudio(String audioFilePath) {

        System.out.println("Reading the audio file......");

        AudioInputStream audioInputStream = null;
        byte[] audioBytes = null;
        File audioFile = new File(audioFilePath);

        try {
            audioInputStream = AudioSystem.getAudioInputStream(audioFile);
            int bytesPerFrame = audioInputStream.getFormat().getFrameSize();
            // Set an arbitrary buffer size of 1024 frames.
            int numBytes = 154600 * bytesPerFrame;
            audioBytes = new byte[numBytes];
            try {

                audioInputStream.read(audioBytes);
                pathOk = true;

            } catch (Exception ex) {
                System.out.println("Audio file error:" + ex);
                pathOk = false;
            }
        } catch (Exception e) {
            System.out.println("Audio file error:" + e);
            pathOk = false;
        }
        
        return audioBytes;
    }
    //Reads the text data and returns the byte array

    public static byte[] readTextData(String inputText) {

        byte[] buff = new byte[inputText.length()];
        for(int i=0;i<inputText.length();i++){
            buff[i] = (byte)inputText.charAt(i);
        }
        return buff;
    }
    //embedding process
    public static boolean embed() {

        System.out.println("Embedding the text message in the audio file ...");
        System.out.println("The plain text data length: "
                + textDataBytes.length);


        // First encode the length of the  text
        int messageLength = textDataBytes.length;
        byte[] len = bit_conversion(messageLength);

        System.out.println("The audio bytes before embedding the message length: "
                        + audioBytes.length);

        audioBytes = encode_text(audioBytes, len, 0);
        System.out.println("The audio bytes after embedding the message length: "
                        + audioBytes.length);
        audioBytes = encode_text(audioBytes, textDataBytes, 32);
        System.out.println("The audio bytes after embedding the text message: "
                + audioBytes.length);

        System.out.println();

        // now write the byte array to an audio file
        JFileChooser chooser = new JFileChooser();
            chooser.showSaveDialog(null);
            //File f =
        File fileOut =  chooser.getSelectedFile();;// new File("src/outputAudio.wav");
        ByteArrayInputStream byteIS = new ByteArrayInputStream(audioBytes);
        AudioInputStream audioIS = new AudioInputStream(byteIS,
                audioInputStream.getFormat(), audioInputStream.getFrameLength());
        if (AudioSystem.isFileTypeSupported(AudioFileFormat.Type.WAVE, audioIS)) {
            try {
                AudioSystem.write(audioIS, AudioFileFormat.Type.WAVE, fileOut);
                System.out.println("Steganographed AU file was written ...");
            } catch (Exception e) {
                System.err.println("Sound File write error");
                return false;
            }
        }

        return true;
    }

    public static byte[] bit_conversion(int i) {

        return (new byte[] { 0, 0, 0, (byte) (i & 0x000000FF) });

    }

    //Encoding the text data
    public static byte[] encode_text(byte[] data, byte[] addition, int offset)

    {
        // check that the data + offset will fit in the Audio

        if (addition.length + offset > data.length) {
            throw new IllegalArgumentException("File not long enough!");
        }

        // loop through each addition byte
        for (int i = 0; i < addition.length; ++i) {
            // loop through the 8 bits of each byte
            int add = addition[i];
            for (int bit = 7; bit >= 0; --bit, ++offset) // ensure the new
            // offset value
            // carries on
            // through both
            // loops
            {
                // assign an integer to b, shifted by bit spaces AND 1
                // a single bit of the current byte
                int b = (add >>> bit) & 1;

                // assign the bit by taking: [(previous byte value) AND 0xfe] OR
                // bit to add

                // changes the last bit of the byte in the Audio to be the bit
                // of addition

                data[offset] = (byte) ((data[offset] & 0xFE) | b);

            }

        }

        return data;

    }

    //extract the data
    public static String extract() {
        
            System.out
                .println("Extracting the encrypted text from the audio file ...");

        System.out
                .println("The audio bytes before extracting the encrypted message length: "
                        + outputAudioBytes.length);



        byte[] buff = decode_text(outputAudioBytes);

        System.out.println("The extracted message length: " + buff.length);
        StringBuilder b= new StringBuilder("");
        for(int i=0;i<buff.length;i++){
            b.append((char) buff[i]);
        }
        String s = b.toString();
        return s;
        
    }

    //Decode text
    public static byte[] decode_text(byte[] data)

    {

        int length = 0;
        int offset = 32;
        // loop through 32 bytes of data to determine text length
        for (int i = 0; i < 32; ++i) // i=24 will also work, as only the 4th
        // byte contains real data
        {
            length = (length << 1) | (data[i] & 1);
        }
        System.out.println("The extracted length is: " + length);

        byte[] result = new byte[length];

        // loop through each byte of text

        for (int b = 0; b < result.length; ++b)

        {

            // loop through each bit within a byte of text

            for (int i = 0; i < 8; ++i, ++offset)

            {

                // assign bit: [(new byte value) << 1] OR [(text byte) AND 1]

                result[b] = (byte) ((result[b] << 1) | (data[offset] & 1));

            }

        }

        return result;

    }
public static boolean fileBrowse(String path){
    Stegenography.path = path;
    audioBytes = readAudio(Stegenography.path);
     try {
            Stegenography.audioInputStream = AudioSystem.getAudioInputStream(new File(
                    Stegenography.path));
    } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
            pathOk = false;      
    } catch (IOException e) {
            e.printStackTrace();
            pathOk = false;
    }
     
    return pathOk;
}

public static boolean textEmbedd(String text){
    
    Stegenography.textDataBytes = Stegenography.readTextData(text);
     //reading the audio file
    return embed();
    
}
public static void clearAll(){
    path = "";
    outputPath = "";
    audioBytes = null;
    outputAudioBytes = null;
    textDataBytes = null;
    audioInputStream = null;
    pathOk = false;
}

public static boolean extractFileBrowse(String path){
    Stegenography.outputPath = path;
    Stegenography.outputAudioBytes = readAudio(Stegenography.outputPath);
    return pathOk;
          
}
}
