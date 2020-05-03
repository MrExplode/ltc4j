package ovh.stranck.ltc4j;

/**
 * This class helps trasforming a boolean array of bits into a byte array.
 * 
 * @author <a href="https://www.stranck.ovh">Stranck</a>
 *
 */
public class ByteBuilder {
	private byte[] buffer;
	private int bufferIndex;
	private short currentValue; //Using short cuz java sucks with unsigned numbers
	private byte bitIndex;
	
	/**
	 * 
	 * @param numBits	Number of bits presents in the byte array we're converting
	 */
	public ByteBuilder(int numBits){
		buffer = new byte[(int)(Math.ceil((float) numBits / 8))];
	}
	
	/**
	 * Adds a bit to the final byte array
	 * @param b	boolean rappresentation of the bit
	 */
	public void add(boolean b){
		if(bitIndex++ == 8) flush();
		currentValue = (short) (currentValue << 1 | (b ? 1 : 0));
	}
	
	/**
	 * Saves the current bits into an indipendent value
	 * and restart the input buffer
	 */
	public void flush(){
		buffer[bufferIndex++] = (byte) (currentValue & 0xff);
		currentValue = 0;
		bitIndex = 1;
	}
	/**
	 * Pads to the left any remaining bits and calls {@link #flush()}
	 */
	public void pad(){
		if(bitIndex > 0){
			currentValue = (short) (currentValue << (8 - bitIndex));
			flush();
		}
	}
	
	/**
	 * Calls {@link #pad()} and return the converted byte buffer
	 * @return
	 */
	public byte[] toByteArray(){
		pad();
		return buffer;
	}
}