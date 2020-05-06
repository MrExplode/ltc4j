package me.mrexplode.ltc4j;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

public class LTCGenerator implements Runnable {
    
    private boolean dropframe = false;
    private boolean colorframe = false;
    private boolean synced = false;
    private boolean reversed = false;
    private Framerate framerate;
    private int sampleRate;
    private int volume;
    
    private int frame = 0;
    private int sec = 0;
    private int min = 0;
    private int hour = 0;
    
    private Mixer mixer;
    private SourceDataLine dataLine;
    private boolean running = true;
    private boolean playing = false;
    
    public LTCGenerator(Mixer output, Framerate framerate, int sampleRate) {
        this.framerate = framerate;
        this.mixer = output;
        this.sampleRate = sampleRate;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("LTC Generator");
        
        dataLine.start();
        while (running) {
            LTCPacket packet = new LTCPacket(hour, min, sec, frame, framerate, dropframe, colorframe, synced, reversed);
            packet.setVolumePercent(volume);
            byte[] content = packet.asAudioSample(sampleRate);
            if (playing)
                dataLine.write(content, 0, content.length);
        }
    }
    
    /**
     * Start playing timecode. The generator will output a continuous stream of timecode, 
     * regardless that the time changed or not.
     * <br>
     * Once {@link #shutdown()} invoked, the timecode can't be started, unless {@link #init()} is called first.
     */
    public void start() {
        this.playing = true;
    }
    
    /**
     * Stop playing timecode. The timecode can be started again by {@link #start()}
     */
    public void stop() {
        this.playing = false;
    }
    
    /**
     * Initializes the generator.
     * Starting the audio stream can be achieved by {@link #start()}
     * @throws LineUnavailableException
     */
    public void init() throws LineUnavailableException {
        AudioFormat format = new AudioFormat(44100, 8, 1, false, true);
        SourceDataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        dataLine = (SourceDataLine) mixer.getLine(info);
        dataLine.open();
        Thread thread = new Thread(this);
        thread.start();
    }
    
    /**
     * Stop outputting timecode and shutdown the thread.
     */
    public void shutdown() {
        running = false;
        dataLine.stop();
        dataLine.close();
        mixer.close();
    }
    
    /**
     * Sets the volume for the output.
     * @param value must be between 0 and 100
     */
    public void setVolume(int value) {
        if (value > 100 || value < 0)
            throw new IllegalArgumentException("Volume should be between 0 and 100");
        this.volume = value;
    }
    
    /**
     * Sets the current time.
     * @param hour
     * @param min
     * @param sec
     * @param frame
     */
    public void setTime(int hour, int min, int sec, int frame) {
        this.hour = hour;
        this.min = min;
        this.sec = sec;
        this.frame = frame;
    }
    
    
    public int getFrame() {
        return frame;
    }

    /**
     * Set frame number
     * @param frame
     */
    public void setFrame(int frame) {
        this.frame = frame;
    }

    
    public int getSec() {
        return sec;
    }

    /**
     * Set second number
     * @param sec
     */
    public void setSec(int sec) {
        this.sec = sec;
    }

    
    public int getMin() {
        return min;
    }

    /**
     * Set minute number
     * @param min
     */
    public void setMin(int min) {
        this.min = min;
    }

    
    public int getHour() {
        return hour;
    }

    /**
     * Set hour number
     * @param hour
     */
    public void setHour(int hour) {
        this.hour = hour;
    }
    

    public boolean isDropframe() {
        return dropframe;
    }

    /**
     * Sets the signal marked as dropframe.
     * @param dropframe
     */
    public void setDropframe(boolean dropframe) {
        this.dropframe = dropframe;
    }

    
    public boolean isColorframe() {
        return colorframe;
    }

    /**
     * Sets the signal marked as color frame.
     * It indicates if the signal is synchronized to a color video signal.
     * @param colorframe
     */
    public void setColorframe(boolean colorframe) {
        this.colorframe = colorframe;
    }

    
    public boolean isSynced() {
        return synced;
    }

    /**
     * Sets the signal marked as synchronized to an external clock.
     * @param synced
     */
    public void setSynced(boolean synced) {
        this.synced = synced;
    }
    
    public boolean isReversed() {
        return reversed;
    }
    
    /**
     * Sets the signal reversed. This means that the sync word that terminates each packet will be reversed, indicating that the time goes backwards.
     * @param value
     */
    public void setReversed(boolean value) {
        reversed = value;
    }

}
