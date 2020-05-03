package me.mrexplode.ltc4j;


public class Framerate {
    
    public static final Framerate FRAMERATE_24 = new Framerate("24");
    public static final Framerate FRAMERATE_25 = new Framerate("25");
    public static final Framerate FRAMERATE_DROPFRAME = new Framerate("29.97");
    public static final Framerate FRAMERATE_30 = new Framerate("30");
    
    private String frameString;
    
    protected Framerate(String s) {
        this.frameString = s;
    }
    
    protected String getFrameString() {
        return frameString;
    }

}
