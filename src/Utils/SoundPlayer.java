package Utils;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class SoundPlayer {

    /**
     * Plays a sound from the given resource path.
     *
     * @param soundPath The path to the sound file in the resources (e.g., "/sounds/door_open.wav").
     */
    public static void playSound(String soundPath) {
        new Thread(() -> {
            try {
                // Obtain URL of the sound file
                URL soundURL = SoundPlayer.class.getResource(soundPath);
                if (soundURL == null) {
                    System.err.println("Sound file not found: " + soundPath);
                    return;
                }

                // Open an audio input stream
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);

                // Get a sound clip resource
                Clip clip = AudioSystem.getClip();

                // Open audio clip and load samples from the audio input stream
                clip.open(audioIn);

                // Play the sound
                clip.start();

                // Optional: Wait for the clip to finish playing
                // Uncomment the following lines if you want the thread to wait until the sound finishes
                /*
                while (!clip.isRunning())
                    Thread.sleep(10);
                while (clip.isRunning())
                    Thread.sleep(10);
                clip.close();
                */

            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                e.printStackTrace();
            }
            // Catching InterruptedException is only necessary if you uncomment the sleep lines
            // catch (InterruptedException e) {
            //     e.printStackTrace();
            // }
        }).start();
    }

    /**
     * Plays a sound in a continuous loop from the given resource path.
     *
     * @param soundPath The path to the sound file in the resources (e.g., "/sounds/theme.wav").
     */
    public static void playSoundLoop(String soundPath) {
        new Thread(() -> {
            try {
                // Obtain URL of the sound file
                URL soundURL = SoundPlayer.class.getResource(soundPath);
                if (soundURL == null) {
                    System.err.println("Sound file not found: " + soundPath);
                    return;
                }

                // Open an audio input stream
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);

                // Get a sound clip resource
                Clip clip = AudioSystem.getClip();

                // Open audio clip and load samples from the audio input stream
                clip.open(audioIn);

                // Set volume to 50% (half volume)
                FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float range = (volumeControl.getMaximum() - volumeControl.getMinimum());
                float halfVolume = volumeControl.getMinimum() + range*0.75f;
                volumeControl.setValue(halfVolume);

                // Loop the sound continuously
                clip.loop(Clip.LOOP_CONTINUOUSLY);

                // Keep the thread alive while the sound plays
                while (true) {
                    Thread.sleep(1000);
                }

            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}