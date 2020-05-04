package me.mrexplode.ltc4j;

/**
 * This enum represents all possible framerates allowed by SMPTE.
 * 
 * @author <a href="https://github.com/mrexplode">MrExplode</a> and <a href="https://www.stranck.ovh">Stranck</a>
 *
 */
public enum Framerate {
    
    /**
     * 24 frames per second
     */
    FRAMERATE_24(24f),
    
    /**
     * 25 frames per second
     */
    FRAMERATE_25(25f),
    
    /**
     * 29.97 frames per second. As the documentation says:<br>
     * Frame numbers 0 and 1 are skipped during the first second of every minute, except multiples of 10 minutes. This converts 30 frame/second time code to the 29.97 frame/second NTSC standard.
     */
    FRAMERATE_DROPFRAME(29.97f),
    
    /**
     * 30 frames per second
     */
    FRAMERATE_30(30f);
    
    private float fps;
    Framerate(float fps) {
        this.fps = fps;
    }
    public float getFps(){
    	return fps;
    }
}
