
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.lang.NullPointerException;

/**
 * Object that represents an encoded packet.
 * @author José Lopes
 *
 */

public class Block {

	/**
	 * Connections to source packets.
	 */
	private List<Integer> neighbours;
	
	/**
	 * Seed used in the soliton distribution, when calculating the degree of this packet.
	 */
	private long seed;
	
	/**
	 * Degree of this packet.
	 */
	private int degree;
	
	/**
	 * The encoded content of this packet.
	 */
	private byte[] data = null;
	
	/**
	 * Size of the encoded content.
	 */
	private int blockSize;
	
	/**
	 * Creates a new instance of <code>Block</code>.
	 * @param seed Value to be used as a seed for the soliton distribution.
	 * @param degree Degree of this packet.
	 * @param blockSize Size of the encoded content on the packet.
	 */
	public Block(long seed, int degree, int blockSize){
		this.neighbours = new LinkedList<Integer>();
		this.seed = seed;
		this.degree = degree;
		this.blockSize = blockSize;
		this.data = new byte[blockSize];
	}
	
	/**
	 * Creates a new instance of <code>Block</code>, equal to another instance.
	 * @param b Instance of <code>Block</code>, from which the values of the new instance will be taken.
	 */
	public Block(Block b){
		this.neighbours = b.getNeighbours();
		this.seed = b.getSeed();
		this.degree = b.getDegree();
		this.blockSize = b.getBlockSize();
		this.data = b.getData();
	}

	/**
	 * Returns the size of the encoded content of this packet.
	 * @return The size of the encoded content of this packet.
	 */
	public int getBlockSize() {
		return blockSize;
	}

	/**
	 * Sets the size of the encoded content of this packet.
	 * @param blockSize New size of the encoded content of this packet.
	 */
	public void setBlockSize(int blockSize) {
		this.blockSize = blockSize;
	}
	
	/**
	 * Returns the degree of this packet.
	 * @return Degree of this packet.
	 */
	public int getDegree() {
		return degree;
	}

	/**
	 * Sets the degree of this packet.
	 * @param degree New degree of this packet.
	 */
	public void setDegree(int degree) {
		this.degree = degree;
	}

	/**
	 * Returns (a copy of) the list of connections this packet has with the source packets.
	 * @return A copy of the list of connections this packet has with the source packets.
	 */
	public List<Integer> getNeighbours() {
		List<Integer> ret = new LinkedList<Integer>();
		
		for(Integer i : neighbours)
			ret.add(i);
		
		return ret;
	}
	
	/**
	 * Sets a new list of connections with the source packets.
	 * @param neighbours New list of connections with the source packets - will be deep copied.
	 */
	public void setNeighbours(List<Integer> neighbours) {
		this.neighbours.clear();
		for(Integer i : neighbours)
			this.neighbours.add(i);
	}
	
	/**
	 * Returns the seed used by the soliton distribution, when calculating the degree of this packet.
	 * @return The seed used by the soliton distribution, when calculating the degree of this packet.
	 */
	public long getSeed() {
		return seed;
	}
	
	/**
	 * Sets the seed used by the soliton distribution, when calculating the degree of this packet.
	 * @param seed The new seed to be used by the soliton distribution, when calculating the degree of this packet.
	 */
	public void setSeed(long seed) {
		this.seed = seed;
	}
	
	/**
	 * Sets the encoded content of this packet.
	 * @param d Encoded packet of this packet.
	 */
	public void setData(byte[] d){
		data = new byte[blockSize];
		
		for(int i=0; i<d.length; i++)
			data[i] = d[i];
	}
	
	/**
	 * Returns the encoded content of this packet.
	 * @return The encoded content of this packet.
	 */
	public byte[] getData(){
		byte[] d = new byte[this.data.length];
		
		for(int i=0; i<this.data.length; i++)
			d[i] = this.data[i];
		
		return d;
	}
	
	/**
	 * Adds a new element to the list of connections this packet has with the source packets.
	 * @param i New element for the list of connections this packet has with the source packets.
	 */
	public void addNeighbour(int i){
		neighbours.add(i);
	}
	
	/**
	 * Removes a connection from the list of connections this packet has with the source packets.
	 * @param i Element (Object, not index) to be removed from the list of connections this packet has with the source packets.
	 */
	public void removeNeighbour(int i){
		neighbours.remove(new Integer(i));
	}
	
	public void removeNeighbourXOR(int j, byte[] block){
		
			for (int k = 0; k < blockSize; k++)
				data[k] = (byte) (data[k] ^  block[k]);
			
			neighbours.remove(new Integer(j));
	}
	
	/**
	 * XORs the <code>input</code> given with the encoded content of this packet.
	 * @param input Input to be XORed with the encoded content of this packet. Will be truncated if bigger than 
	 * <code>blockSize</code>, and padded with '0's if shorter.
	 * @throws NullPointerException In case <code>data</code> or <code>input</code> are <code>null</code>.
	 */
	public void xor(byte[] input) throws NullPointerException{ // TODO: throw NullPointer
		
		if(data==null || input==null) throw new NullPointerException();
		
		if(input.length >= data.length)
			for (int i=0; i<data.length; i++)
				data[i] = (byte)(data[i] ^ input[i]);
		else{
			byte aux[] = new byte[data.length];
			aux = Arrays.copyOf(input, data.length);
			
			for (int i=0; i<data.length; i++)
				data[i] = (byte)(data[i] ^ aux[i]);
		}
	}
	
	/**
	 * Returns a deep copy of this packet.
	 * @return A deep copy of this packet.
	 */
	public Block clone(){
		return(new Block(this));
	}
	
}
