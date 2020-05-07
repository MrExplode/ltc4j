package me.mrexplode.ltc4j;

/**
 * This class represents one packet of LTC data.
 * 
 * @author <a href="https://www.stranck.ovh">Stranck</a> and <a href="https://github.com/mrexplode">MrExplode</a>
 *
 */
public class LTCPacket {
    
    private final boolean[] SYNC_WORD = convertBitString("0011111111111101");
    
    private int hour;
    private int min;
    private int sec;
    private int frame;
    private Framerate framerate;
    private final boolean[][] userbits = new boolean[8][];
    private boolean bgf0;
    private boolean bgf2;
    private boolean reversed;
    private boolean df;
    private boolean sync;
    private boolean col;
    private byte volume;
    
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
     * @param hour
     * @param min
     * @param sec
     * @param frame
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
        for (int i = 0; i < 8; i++)
            userbits[i] = new boolean[4];
    }
    
    private static int buildBlock(boolean[] data, final int index, int value, boolean longValue, boolean flag1, boolean flag2, boolean[] userBits1, boolean[] userBits2) {
        int index2 = index;
        int optionalBit = longValue ? 1 : 0;
        boolean[] bcd = bcd(value, 6 + optionalBit);
        //First 4 bits of value
        for(int i = 0; i < 4; i++)
            data[index2++] = bcd[i];
        //user bits
        index2 = addAllBits(data, index2, userBits1);
        //Second 2/3 bits of value
        for(int i = 0; i < 2 + optionalBit; i++)
            data[index2++] = bcd[4 + i];
        //First flag
        data[index2++] = flag1;
        //If we're working with just 6 bits, add the second flag
        if(!longValue)
            data[index2++] = flag2;
        //User bits again
        index2 = addAllBits(data, index2, userBits2);
        return index2;
    }
    
    /**
     * 
     * @return The packet data as a boolean array, without encoding.
     */
    public boolean[] asBooleanArray() {
        boolean data[] = new boolean[80];
        int index = 0;
        index = buildBlock(data, index, frame, false, df, col, userbits[0], userbits[1]);
        index = buildBlock(data, index, sec, true, false, false, userbits[2], userbits[3]);
        index = buildBlock(data, index, min, true, false, false, userbits[4], userbits[5]);
        index = buildBlock(data, index, hour, false, sync, false, userbits[6], userbits[7]);
        addAllBits(data, index, SYNC_WORD);
        data[framerate == Framerate.FRAMERATE_25 ? 59 : 27] = getBitParity(data, false, true);
        data[framerate == Framerate.FRAMERATE_25 ? 27 : 43] = bgf0;
        data[framerate == Framerate.FRAMERATE_25 ? 43 : 59] = bgf2;
        //if(framerate == Framerate.FRAMERATE_DROPFRAME) data[10] = 
        return data;
    }
    
    /**
     * 
     * @param sampleRate
     * @return The packet data as an audio sample, encoded and expanded to fit the sample rate
     */
    public byte[] asAudioSample(int sampleRate) {
        return manchesterEncode(asBooleanArray(), getBitExpansion(sampleRate));
    }
    
    /**
     * Returns the data encoded with <a href="https://en.wikipedia.org/wiki/Differential_Manchester_encoding">differential manchester encoding</a>, without bit expansion
     * @return data in byte array
     */
    public byte[] asByteArray() {
        return manchesterEncode(asBooleanArray(), 1);
    }
    
    /**
     * Returns the data converted into a string that consits of "1"s and "0"s representing bits
     * @return data in bitstring
     */
    public String asBitsString(){
        boolean[] data = asBooleanArray();
        StringBuilder sb = new StringBuilder(data.length * 8);
        for(boolean b : data)
            sb.append(b ? '1' : '0');

        return sb.toString();
    }
    
    
    public int getPacketSize(Object o){
        return 160 * getBitExpansion((Integer) o);
    }
    
    /**
     * Returns the amount of bit expansion needed for the specified sample rate, with the current framerate.
     * @param sampleRate
     * @return bit expansion value
     */
    private int getBitExpansion(int sampleRate){
        return (int) (sampleRate / (160 * framerate.getFps()));
    }
    
    private byte[] manchesterEncode(boolean value[], int repeatBytes) {
        byte[] result = new byte[value.length * 2 * repeatBytes];
        boolean v = false;
        int index = 0;
        int add = reversed ? -1 : 1;
        for (int i = reversed ? value.length - 1 : 0; (!reversed && i < value.length) || (reversed && i >= 0); i += add) {
            if (value[i]) {
                v = !v;
                index = repeatBytes(result, index, (byte) (v ? volume : -volume), repeatBytes);
                v = !v;
                index = repeatBytes(result, index, (byte) (v ? volume : -volume), repeatBytes);
            } else {
                v = !v;
                index = repeatBytes(result, index, (byte) (v ? volume : -volume), repeatBytes * 2);
            }
        }
        return result;
    }
    
    /**
     * 
     * @param block the index of the userbits
     * @return the userbits at the index, in boolean array
     */
    public boolean[] getUserBits(int block){
        return userbits[block];
    }
    
    /**
     * Sets the specified userbits field to the given value.
     * @param bits the data, in bitString, that will be converted to boolean array
     * @param block index of the userbits field
     */
    public void setUserbits(String bits, int block){
        userbits[block] = convertBitString(bits);
    }
    
    /**
     * Sets the specified userbits field to the given value.
     * @param bits the data, in boolean array
     * @param block index of the userbits field
     */
    public void setUserbits(boolean[] bits, int block){
        userbits[block] = bits;
    }
    
    public boolean getBgf0() {
        return bgf0;
    }
    
    /**
     * Sets the binary group flag 0 to the given value.<br>
     * The combination of BGF0 and BGF2 indicates the data format of the user bits.
     * The combinations are reserved.
     * 
     * @param bgf0 binary group flag 0 value
     */
    public void setBgf0(boolean bgf0) {
        this.bgf0 = bgf0;
    }
    
    public boolean getBgf2() {
        return bgf2;
    }
    
    /**
     * Sets the binary group flag 2 to the given value.<br>
     * The combination of BGF0 and BGF2 indicates the data format of the user bits.
     * The combinations are reserved.
     * 
     * @param bgf2 binary group flag 2 value
     */
    public void setBgf2(boolean bgf2) {
        this.bgf2 = bgf2;
    }
    
    /**
     * Sets the amplitude of the signal.
     * If you wish to have more control beyond this, you should use the {@link javax.sound.sampled.DataLine#getControl(javax.sound.sampled.Control.Type)} 
     * with {@link javax.sound.sampled.FloatControl.Type#MASTER_GAIN}
     * 
     * @param volume The value, range from 0 to 100.
     */
    public void setVolumePercent(int volume){
        this.volume = (byte) (Byte.MAX_VALUE * volume / 100);
    }
    
    
    ////////////////////////////
    //     Helper methods     //
    ////////////////////////////
    
    
    private static int repeatBytes(byte data[], final int index, byte value, int repeatBytes){
        int index2 = index;
        for(int i = 0; i < repeatBytes; i++)
            data[index2++] = value;
        return index2;
    }
    
    private static int bcdSingle(boolean[] b, final int index, final int value) {
        int index2 = index;
        int value2 = value;
        while (value > 0 && index2 < b.length) {
            b[index2++] = value2 % 2 == 1;
            value2 /= 2;
        }
        return index2;
    }
    
    private static boolean[] bcd(int value, int numBits) {
        boolean[] result = new boolean[numBits];
        bcdSingle(result, 0, value % 10);
        bcdSingle(result, 4, value / 10);
        return result;
    }
    
    private static int addAllBits(boolean[] dst, final int index, boolean[] src) {
        int index2 = index;
        for (int i = 0; i < src.length; i++)
            dst[index2++] = src[i];
        return index;
    }
    
    private static boolean[] convertBitString(String s) {
        boolean[] ret = new boolean[s.length()];
        for (int i = 0; i < s.length(); i++)
            ret[i] = s.charAt(i) == '1';
        return ret;
    }
    
    private static boolean getBitParity(boolean data[], boolean searchForBit, boolean odd){
        int i = 0;
        for(boolean v : data)
            if(v == searchForBit)
                i++;
        return i % 2 == (odd ? 1 : 0);
    }

}
