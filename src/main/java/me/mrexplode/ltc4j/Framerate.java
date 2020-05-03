package me.mrexplode.ltc4j;


public class Framerate {
    
    /**
     * 24 frames per second
     */
    public static final Framerate FRAMERATE_24 = new Framerate("24");
    
    /**
     * 25 frames per second
     */
    public static final Framerate FRAMERATE_25 = new Framerate("25");
    
    /**
     * 29.97 frames per second. As the documentation says:<br>
     * Frame numbers 0 and 1 are skipped during the first second of every minute, except multiples of 10 minutes. This converts 30 frame/second time code to the 29.97 frame/second NTSC standard.
     */
    public static final Framerate FRAMERATE_DROPFRAME = new Framerate("29.97");
    
    /**
     * 30 frames per second
     */
    public static final Framerate FRAMERATE_30 = new Framerate("30");
    
    private String frameString;
    
    protected Framerate(String s) {
        this.frameString = s;
    }
    
    protected String getFrameString() {
        return frameString;
    }

}
