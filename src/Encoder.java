

import java.security.InvalidAlgorithmParameterException;
import java.lang.IllegalArgumentException;
import java.lang.NullPointerException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Object capable of encoding and decoding data.
 * @author Jose Lopes
 *
 */
public class Encoder {
	
	/**
	 * Number of input blocks.
	 */
	private int k;							// 1000
	
	/**
	 * Size of each input block.
	 */
	private int blockSize;					// 32
	
	/**
	 * Buffer for communication between the encoding and decoding threads.
	 */
	private BlockingQueue<Block> buffer;
	
	/**
	 * Flag for communication between the encoding and decoding threads.
	 */
	private AtomicBoolean ack;
	
	/**
	 * Instance of a parameterized robust soliton probability distribution.
	 */
	private Soliton s;
	 
	/**
	 * Creates a new instance of <code>Encoder</code>.	
	 * @param k Number of input blocks.
	 * @param bS Size of each input block.
	 * @throws IllegalArgumentException In case <code>k</code> or <code>bS</code> are 0 or less.
	 */
	public Encoder(int k, int bS) throws IllegalArgumentException{
		
		if(k<=0 || bS<=0) throw new IllegalArgumentException();
		
		this.k = k;
		this.blockSize = bS;
		
		try {
			s = new Soliton(this.k, 0.12, 0.01);
		} catch (IllegalArgumentException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}
				
		buffer = new ArrayBlockingQueue<Block>(s.getBlocksNeeded()); // k*Beta, for the redundant packets needed
		ack = new AtomicBoolean(false);
		
	}

	/**
	 * Encodes a given string.
	 * @param msg String to be encoded.
	 * @return List of encoded <code>Block</code>s.
	 * @throws NullPointerException In case the input string is null.
	 */
	public List<Block> encode(String msg) throws NullPointerException{
		
		if(msg==null) throw new NullPointerException();
		
		byte[] padded = pad(msg.getBytes());
		
		return(encode(split(padded)));
	}
	
	/**
	 * Encodes a given <code>byte array</code>.
	 * @param inputBlock <code>Byte array</code> to be encoded.
	 * @return List of encoded <code>Block</code>s.
	 * @throws NullPointerException In case the input <code>array</code> is null.
	 */
	public List<Block> encode(byte[][] inputBlock) throws NullPointerException{

		if(inputBlock==null) throw new NullPointerException();
		
		Block encodingBlock = null;
		Random rGen1 = new Random(), rGen2;
		long seed;
		int d=0, j;
		List<Block> encMsg = new ArrayList<Block>();
		
		do{
			seed = rGen1.nextLong();
			try {
				d = s.soliton(seed);
			} catch (InvalidAlgorithmParameterException e1) {
				System.err.println(e1.getMessage());
				e1.printStackTrace();
				System.exit(-8);
			}
			rGen2 = new Random(seed);
			encodingBlock = new Block(seed, d, blockSize);
			
			for(int x=1; x<=d; x++){
				j = rGen2.nextInt(k); // TODO: j <- random(1,k) -- inclusive?

				encodingBlock.addNeighbour(j);
				
				if(x==1)
					encodingBlock.setData(inputBlock[j]);
				else
					encodingBlock.xor(inputBlock[j]);
			}
			
			try {
				buffer.put(encodingBlock.clone());
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
				System.exit(-1);
			}
			encMsg.add(encodingBlock.clone());
		}while(!this.ack.get());
		
		return(encMsg);
	}

	/**
	 * Decodes the blocks in <code>buffer</code>.
	 * @return <code>Byte array</code> representing the decoded message.
	 */
	public byte[] decode(){
				
		List<Block> received = new LinkedList<Block>();
		Block i;
		int j;
		byte[][] inputBlock = new byte[k][blockSize];

		for(int blocks=0; blocks < s.getBlocksNeeded(); blocks++){
			try {
				received.add(buffer.take());
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
				System.exit(-1);
			}
		}
		
		ack.set(true);
		
		while((i = existsExactlyOneNeighbour(received)) != null){ // If does not exist, and ripple is empty, then it's a fail.
			j = ((LinkedList<Integer>) i.getNeighbours()).getFirst();
			
			inputBlock[j] = i.getData();
			
			for(Block l : received)
				if(l.getNeighbours().contains(j)){
					l.xor(inputBlock[j]);
					l.removeNeighbour(j);
				}
		}
				
		return(merge(inputBlock));
	}
	
	/**
	 * Merges a matrix into an <code>array</code>
	 * @param inputBlock Matrix to be merged.
	 * @return Merged <code>array</code>.
	 */
	private byte[] merge(byte[][] inputBlock){
		
		int blocks = inputBlock.length;
		byte[] ret = new byte[blocks * blockSize];
		
		for (int i = 0, ini = 0; i < blocks; i++, ini += blockSize)
			for (int j = 0; j < blockSize; j++)
				ret[ini + j] = inputBlock[i][j];
		
		return ret;
	}
	
	/**
	 * Checks the elements of a given list, if there is one with a single connection to the source packets.
	 * @param list List of <code>Block</code>s.
	 * @return The first <code>Block</code> found, with only one connection to the source packets. 
	 * <code>null</code> if no <code>Block</code> met the condition.
	 */
	private Block existsExactlyOneNeighbour(List<Block> list){
		
		if(list == null || list.isEmpty())
			return null;
		
		for(Block b : list)
			if(b.getNeighbours().size()==1)
				return b;
		
		return null;
	}
	
	/**
	 * Pads a <code>byte array</code> with '0's, if needed.
	 * @param msg <code>Byte array</code> to be padded.
	 * @return Padded <code>byte array</code>.
	 */
	public byte[] pad(byte[] msg){
		
		return(Arrays.copyOf(msg, k * blockSize));
	}
	
	/**
	 * Splits a <code>byte array</code> into a matrix.
	 * @param padded <code>Byte array</code> to be split.
	 * @return Split matrix.
	 */
	private byte[][] split(byte[] padded){
		
		byte[][] ret = new byte[k][blockSize]; 
		int j=0, beginning=0, end=blockSize;
		
		for(int i = 0; i < k; i++, j=0){
			ret[i] = new byte[blockSize];

			for(int k=beginning; k<end; k++, j++){
				ret[i][j] = padded[k];
			}
			
			beginning += blockSize;
			end += blockSize;
		}
		
		return ret;
	}	
}
