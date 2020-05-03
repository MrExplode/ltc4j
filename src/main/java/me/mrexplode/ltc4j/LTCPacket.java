package me.mrexplode.ltc4j;

import java.util.Arrays;

/**
 * This class represents one packet of LTC data.
 * 
 * @author <a href="https://github.com/mrexplode">MrExplode</a>
 *
 */
public class LTCPacket {
    
    private final String SYNC_WORD = "0011111111111101";
    private final String USER_BIT_FIELD = "0000";
    
    private int hour;
    private int min;
    private int sec;
    private int frame;
    private Framerate framerate;
    private boolean df;
    private boolean col;
    private boolean sync;
    private boolean reversed;
    
    /**
     * Constructs a packet from the given parameters.<br>
     * All extra ltc parameters are set to false.
     * 
     * @param hour
     * @param min
     * @param sec
     * @param frame
     * @param framerate
     */
    public LTCPacket(int hour, int min, int sec, int frame, Framerate framerate) {
        this(hour, min, sec, frame, framerate, framerate.equals(Framerate.FRAMERATE_DROPFRAME) ? true : false, false, false, false);
    }
    
    /**
     * Constructs a packet from the given parameters.<br>
     * More info about flags can be found <a href="https://en.wikipedia.org/wiki/Linear_timecode#Longitudinal_timecode_data_format">here</a>.
     * 
     * @param hour number
     * @param min number
     * @param sec number
     * @param frame number
     * @param framerate the framerate
     * @param df drop frame flag
     * @param col color frame flag
     * @param sync clock synced frame flag
     * @param reversed should be true when you are playing timecode backwards
     */
    public LTCPacket(int hour, int min, int sec, int frame, Framerate framerate, boolean df, boolean col, boolean sync, boolean reversed) {
        this.hour = hour;
        this.min = min;
        this.sec = sec;
        this.frame = frame;
        this.framerate = framerate;
        this.df = df;
        this.col = col;
        this.sync = sync;
        this.reversed = reversed;
    }

    /**
     * 
     * @return The packet, correctly <a href="https://en.wikipedia.org/wiki/Linear_timecode#Longitudinal_timecode_data_format">formatted</a>,
     *  and encoded with <a href="https://en.wikipedia.org/wiki/Differential_Manchester_encoding">differential manchester encoding</a>.
     */
    public String asStringBits() {
        StringBuilder builder = new StringBuilder();
        String[] frames = bcdSmall(this.frame);
        //frame number units
        for (int i = 0; i < 4; i++) {
            builder.append(frames[i]);
        }
        //user bit field 1
        builder.append(USER_BIT_FIELD);
        //frame number tens
        builder.append(frames[4]);
        builder.append(frames[5]);
        //dropframe
        builder.append(df ? "1" : "0");
        //colorframe
        builder.append(col ? "1" : "0");
        //user bit field 2
        builder.append(USER_BIT_FIELD);
        String[] seconds = bcdBig(sec);
        //seconds units
        for (int i = 0; i < 4; i++) {
            builder.append(seconds[i]);
        }
        //user bit field 3
        builder.append(USER_BIT_FIELD);
        //seconds tens
        builder.append(seconds[4]);
        builder.append(seconds[5]);
        builder.append(seconds[6]);
        //27 flag
        if (framerate.equals(Framerate.FRAMERATE_25)) {
            //BGF 0
            builder.append("0");
        } else {
            //polarity correction bit
            
        }
        //user bit field 4
        builder.append(USER_BIT_FIELD);
        String[] mins = bcdBig(min);
        //minutes units
        for (int i = 0; i < 4; i++) {
            builder.append(mins[i]);
        }
        //user bit field 5
        builder.append(USER_BIT_FIELD);
        //minutes tens
        builder.append(mins[4]);
        builder.append(mins[5]);
        builder.append(mins[6]);
        //43 flag, BGF0 or BGF2
        builder.append("0");
        //user bit field 6
        builder.append(USER_BIT_FIELD);
        String[] hours = bcdSmall(hour);
        //hours units
        for (int i = 0; i < 4; i++) {
            builder.append(hours[i]);
        }
        //user bit field 7
        builder.append(USER_BIT_FIELD);
        //hours tens
        builder.append(hours[4]);
        builder.append(hours[5]);
        //clock sync flag
        builder.append(sync ? "1" : "0");
        //flag
        if (framerate.equals(Framerate.FRAMERATE_25)) {
            //polarity correction bit
            
        } else {
            //BGF 2
            builder.append("0");
        }
        //user bit field 8
        builder.append(USER_BIT_FIELD);
        //sync word
        builder.append(reversed ? new StringBuilder(SYNC_WORD).reverse().toString() : SYNC_WORD);
        
        return manchesterEncode(builder.toString());
    }
    
    /**
     * 
     * @return the packet encoded into bytes
     */
    public byte[] asByteArray() {
        String[] bits = asStringBits().split("(?<=\\G.{8})");
        byte[] result = new byte[bits.length];
        for (int i = 0; i < bits.length; i++) {
            Integer integer = Integer.parseInt(bits[i], 2);
            result[i] = integer.byteValue();
        }
        return result;
    }
    
    private static String manchesterEncode(String value) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '0') {
                result.append("01");
            } else {
                result.append("10");
            }
        }
        return result.toString();
    }
    
    private static String[] bcdSmall(final int value) {
        int var = value;
        String[] result = new String[6];
        //20
        int var6 = (int) (var / 20d);
        var = var - var6 * 20;
        //10
        int var5 = (int) (var / 10d);
        var = var - var5 * 10;
        //8
        int var4 = (int) (var / 8d);
        var = var - var4 * 8;
        //4
        int var3 = (int) (var / 4d);
        var = var - var3 * 4;
        //2
        int var2 = (int) (var / 2d);
        var = var - var2* 2;
        //1
        
        result[0] = Integer.toString(var);
        result[1] = Integer.toString(var2);
        result[2] = Integer.toString(var3);
        result[3] = Integer.toString(var4);
        result[4] = Integer.toString(var5);
        result[5] = Integer.toString(var6);
        return result;
    }
    
    private static String[] bcdBig(int value) {
        int var = value;
        String[] result = new String[7];
        //40
        int var7 = (int) (var / 40d);
        var = var - var7 * 40;
        //20
        int var6 = (int) (var / 20d);
        var = var - var6 * 20;
        //10
        int var5 = (int) (var / 10d);
        var = var - var5 * 10;
        //8
        int var4 = (int) (var / 8d);
        var = var - var4 * 8;
        //4
        int var3 = (int) (var / 4d);
        var = var - var3 * 4;
        //2
        int var2 = (int) (var / 2d);
        var = var - var2* 2;
        //1 = remaining in var
        
        result[0] = Integer.toString(var);
        result[1] = Integer.toString(var2);
        result[2] = Integer.toString(var3);
        result[3] = Integer.toString(var4);
        result[4] = Integer.toString(var5);
        result[5] = Integer.toString(var6);
        result[6] = Integer.toString(var7);
        return result;
    }

}
