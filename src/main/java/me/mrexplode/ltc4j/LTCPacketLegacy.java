package me.mrexplode.ltc4j;

/**
 * This class represents one packet of LTC data.
 * 
 * @author <a href="https://github.com/mrexplode">MrExplode</a> and <a href="https://www.stranck.ovh">Stranck</a>
 *
 */
@SuppressWarnings("all")
public class LTCPacketLegacy {

	private static final String SYNC_WORD_STR = "0011111111111101";
	private static final boolean[] SYNC_WORD = convertBitString(SYNC_WORD_STR);
	private static final boolean[] SYNC_WORD_REVERSED = convertBitString(new StringBuilder(SYNC_WORD_STR).reverse().toString());
	private static final boolean[] USER_BIT_FIELD = convertBitString("0000");

	private int hour;
	private int min;
	private int sec;
	private int frame;
	private Framerate framerate;
	private boolean df;
	private boolean col;
	private boolean sync;
	private boolean reversed;
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
	public LTCPacketLegacy(int hour, int min, int sec, int frame, Framerate framerate) {
		this(hour, min, sec, frame, framerate, framerate.equals(Framerate.FRAMERATE_DROPFRAME) ? true : false, false, false, false);
	}

	/**
	 * Constructs a packet from the given parameters.<br>
	 * More info about flags can be found <a href=
	 * "https://en.wikipedia.org/wiki/Linear_timecode#Longitudinal_timecode_data_format">
	 * here</a>.
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
	public LTCPacketLegacy(int hour, int min, int sec, int frame, Framerate framerate, boolean df, boolean col, boolean sync, boolean reversed) {
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
	
	public void setVolumePercent(int volume) {
	    this.volume = (byte) (Byte.MAX_VALUE  * volume / 100);
	}
	
	public byte[] asAudioSample(int sampleRate) {
        return manchesterEncode(asBooleanArray(), getBitExpansion(sampleRate));
    }
	
	public boolean[] asBooleanArray() {
	    boolean data[] = new boolean[80];
        int index = 0;
        index = buildBlock(data, index, frame, false, df, col);
        index = buildBlock(data, index, sec, true, false, false);
        index = buildBlock(data, index, min, true, false, false);
        index = buildBlock(data, index, hour, false, sync, false);
        addAll(data, index, reversed ? SYNC_WORD_REVERSED : SYNC_WORD);
        data[framerate == Framerate.FRAMERATE_25 ? 59 : 27] = getPolarity(data);
        return data;
	}

	/**
	 * 
	 * @return The packet, correctly <a href=
	 *         "https://en.wikipedia.org/wiki/Linear_timecode#Longitudinal_timecode_data_format">
	 *         formatted</a>.
	 */
	public byte[] asByteArray() {
	    boolean[] data = asBooleanArray();
		ByteBuilder builder = new ByteBuilder(data.length);
		builder.add(data);
		return builder.toByteArray();
	}
	/**
	 * @return The packet, correctly <a href=
	 *         "https://en.wikipedia.org/wiki/Linear_timecode#Longitudinal_timecode_data_format">
	 *         formatted</a>.
	 */
	public String asBitString() {
		boolean[] data = asBooleanArray();
		StringBuilder sb = new StringBuilder(data.length);
		for(boolean b : data){
			sb.append(b ? "1" : "0");
		}
		return sb.toString();
	}
	
	private int getBitExpansion(int sampleRate) {
	    return (int) (sampleRate / (160 * framerate.getFps()));
	}

	private int buildBlock(boolean[] data, int index, int value, boolean longValue, boolean flag1, boolean flag2) {
		int optionalBit = longValue ? 1 : 0;
		boolean[] bcd = bcd(value, 6 + optionalBit);
		//First 4 bits of value
		for(int i = 0; i < 4; i++)
			data[index++] = bcd[i];
		//user bits
		index = addAll(data, index, USER_BIT_FIELD);
		//Second 2/3 bits of value
		for(int i = 0; i < 2 + optionalBit; i++)
			data[index++] = bcd[4 + i];
		//First flag
		data[index++] = flag1;
		//If we're working with just 6 bits, add the second flag
		if(!longValue)
			data[index++] = flag2;
		//User bits again
		index = addAll(data, index, USER_BIT_FIELD);
		return index;
	}
	
	
	private static boolean getPolarity(boolean data[]){
		int i = 0;
		for(boolean v : data)
			if(!v)
				i++;
		return i % 2 == 1;
	}
	

	private byte[] manchesterEncode(boolean value[], int repeatBytes) {
		byte[] result = new byte[value.length * 2 * repeatBytes];
		int index = 0;
		for (boolean b : value) {
			if (b) {
				index = repeatBytes(result, index, volume, repeatBytes);
				index = repeatBytes(result, index, (byte) -volume, repeatBytes);
			} else {
			    index = repeatBytes(result, index, (byte) -volume, repeatBytes);
				index = repeatBytes(result, index, volume, repeatBytes);
			}
		}
		return result;
	}
	
	
	private int repeatBytes(byte[] data, int index, byte value, int repeat) {
	    for(int i = 0; i < repeat; i++)
            data[index++] = value;
        return index;
	}

	
	private static int bcdSingle(boolean[] b, int index, int value) {
		while (value > 0 && index < b.length) {
			b[index++] = value % 2 == 1;
			value /= 2;
		}
		return index;
	}

	private static boolean[] bcd(int value, int numBits) {
		boolean[] result = new boolean[numBits];
		bcdSingle(result, 0, value % 10);
		bcdSingle(result, 4, value / 10);
		return result;
	}
	
	@SuppressWarnings("unused")
	private static void printData(boolean[] data){
		for(int i = 0; i < data.length; i++)
			System.out.print((data[i] ? '1' : '0') + (i % 4 == 3 ? " " : ""));
		System.out.println();
	}

	private static int addAll(boolean[] dst, int index, boolean[] src) {
		for (int i = 0; i < src.length; i++)
			dst[index++] = src[i];
		return index;
	}

	private static boolean[] convertBitString(String s) {
		boolean[] ret = new boolean[s.length()];
		for (int i = 0; i < s.length(); i++)
			ret[i] = s.charAt(i) == '1';
		return ret;
	}

}