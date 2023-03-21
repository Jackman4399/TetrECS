package uk.ac.soton.comp1206.Utility;

import java.io.File;
import javafx.animation.ParallelTransition;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Multimedia {

  /**
   * Logger for outputting info
   */
  private static final Logger logger = LogManager.getLogger(Multimedia.class);

  /**
   * Boolean to enable/disable audio
   */
  private static SimpleBooleanProperty audioEnabled = new SimpleBooleanProperty(true);

  /**
   * Audio player for playing sound effects
   */
  private static MediaPlayer effectPlayer;

  /**
   * Music player for playing background music
   */
  private static MediaPlayer musicLoopPlayer;

  /**
   * Music player for non-looped music
   */
  private static MediaPlayer musicPlayer;

  /**
   * The sfx volume
   */
  private static double sfxVolume = 1;

  /**
   * The music volume
   */
  private static double musicVolume = 1;
  
  


  /**
   * Plays an audiofile without a loop
   * @param file the audio file
   */
  public static void playAudioEffect(String file){
    if(!audioEnabled.get()) return;

    String audioFile = Multimedia.class.getResource("/" + file).toExternalForm();
    logger.info("Playing audio: " + audioFile);

    try {
      Media media = new Media(audioFile);
      effectPlayer = new MediaPlayer(media);
      effectPlayer.setVolume(sfxVolume);
      effectPlayer.play();
    } catch (Exception e){
      audioEnabled.set(false);
      e.printStackTrace();
      logger.error("Unable to play audio file, disabling audio");
    }
  }

  /**
   * Plays an audiofile as looped music
   * @param file the music file
   */
  public static void playLoopedMusic(String file){
    if(!audioEnabled.get()) return;
    String musicFile = Multimedia.class.getResource("/" + file).toExternalForm();
    logger.info("Playing loop: " + musicFile);
    try {
      Media media = new Media(musicFile);
      musicLoopPlayer = new MediaPlayer(media);
      musicLoopPlayer.setVolume(musicVolume);
      musicLoopPlayer.play();
      musicLoopPlayer.setOnEndOfMedia(() -> playLoopedMusic(file));
    } catch (Exception e){
      audioEnabled.set(false);
      e.printStackTrace();
      logger.error("Unable to play music file, disabling audio");
    }
  }

  /**
   * Stops the looped music from playing
   */
  public static void stopLM(){
    if(musicLoopPlayer != null){
      musicLoopPlayer.stop();
    }
  }

  /**
   * Plays non-looped music
   * @param file
   */
  public static void playMusic(String file){
    if(!audioEnabled.get()) return;

    String musicFile = Multimedia.class.getResource("/" + file).toExternalForm();
    logger.info("Playing music: " + musicFile);

    try {
      Media media = new Media(musicFile);
      musicPlayer = new MediaPlayer(media);
      musicPlayer.setVolume(musicVolume);
      musicPlayer.play();
    } catch (Exception e){
      audioEnabled.set(false);
      e.printStackTrace();
      logger.error("Unable to play audio file, disabling audio");
    }
  }

  /**
   * Stops the music
   */
  public static void stopM(){
    if(musicPlayer != null){
      musicPlayer.stop();
    }
  }

  /**
   * Set the sfx volume
   * @param value
   */
  public static void changeSFX(double value){
    sfxVolume = value;
    if(effectPlayer != null) {
      effectPlayer.setVolume(sfxVolume);
    }
  }

  /**
   * Set the music volume
   * @param value
   */
  public static void changeMusic(double value){
    musicVolume = value;
    if(musicPlayer != null){
      musicPlayer.setVolume(musicVolume);
    }
    if(musicLoopPlayer != null){
      musicLoopPlayer.setVolume(musicVolume);
    }
  }
}
