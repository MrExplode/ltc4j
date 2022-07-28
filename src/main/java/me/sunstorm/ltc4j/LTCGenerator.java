package me.sunstorm.ltc4j;

import lombok.Getter;
import lombok.Setter;

import javax.sound.sampled.*;

public class LTCGenerator implements Runnable {

    /**
     * Sets the signal marked as dropframe.
     * @param dropFrame
     */
    @Getter @Setter
    private boolean dropFrame = false;

    /**
     * Sets the signal marked as color frame.
     * It indicates if the signal is synchronized to a color video signal.
     * @param colorFrame
     */
    @Getter @Setter
    private boolean colorFrame = false;

    /**
     * Sets the signal marked as synchronized to an external clock.
     * @param synced
     */
    @Getter @Setter
    private boolean synced = false;

    /**
     * Sets the signal reversed. This means that the sync word that terminates each packet will be reversed, indicating that the time goes backwards.
     * @param value
     */
    @Getter @Setter
    private boolean reversed = false;

    /**
     * Set frame number
     * @param frame
     */
    @Getter @Setter
    private int frame = 0;

    /**
     * Set second number
     * @param sec
     */
    @Getter @Setter
    private int sec = 0;

    /**
     * Set minute number
     * @param min
     */
    @Getter @Setter
    private int min = 0;

    /**
     * Set hour number
     * @param hour
     */
    @Getter @Setter
    private int hour = 0;

    private final Mixer mixer;
    private final Framerate frameRate;
    private final int sampleRate;
    private boolean running = true;
    private boolean playing = false;
    private SourceDataLine dataLine;
    private int volume;

    public LTCGenerator(Mixer output, Framerate frameRate, int sampleRate) {
        this.frameRate = frameRate;
        this.mixer = output;
        this.sampleRate = sampleRate;
    }

    @Override
    public void run() {
        dataLine.start();
        while (running) {
            if (playing) {
                LTCPacket packet = new LTCPacket(hour, min, sec, frame, frameRate, dropFrame, colorFrame, synced, reversed);
                packet.setVolumePercent(volume);
                byte[] content = packet.asAudioSample(sampleRate);
                dataLine.write(content, 0, content.length);
            }
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
}
