
import java.security.InvalidAlgorithmParameterException;
import java.lang.IllegalArgumentException;
import java.util.Random;

/**
 * Object of a parameterized robust soliton probability distribution, ready for sampling. 
 * @author Jose Lopes
 *
 */
public class Soliton {

	/**
	 * Number of blocks to be encoded.
	 */
	private int k;
	
	/**
	 * Admissible failure probability.
	 */
	private double delta;
	
	/**
	 * Parameter used for the <code>tau</code> function.
	 */
	private double R;
	
	/**
	 * Normalization factor for the robust soliton distribution.
	 */
	private double beta; /* normalization factor */
	
	/**
	 * Creates new <code>Soliton</code> distribution object, according to the received parameters.
	 * @param k Number of blocks to be encoded.
	 * @param c Constant.
	 * @param delta Admissible failure probability.
	 * @throws IllegalArgumentException In case <code>k<=0, c<=0 or delta not in [0,1]</code>
	 */
	public Soliton(int k, double c, double delta) throws IllegalArgumentException{
		int i;
		
		if(k>0 && c>0 && delta>=0 && delta<=1){
			this.k = k;
			this.delta = delta;
		}
		else throw new IllegalArgumentException();
		
		R = c * Math.log(k/delta) * Math.sqrt(k);
		
		for(beta=0,i=1; i<=k; i++)
			try {
				beta += rho(i) + tau(i);
			} catch (InvalidAlgorithmParameterException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
				System.exit(-99);
			}
	}
	
	/**
	 * Ideal soliton distribution.
	 * @param i Argument for the probability mass function.
	 * @return Value (respective to input) of the probability mass function.
	 * @throws InvalidAlgorithmParameterException In case <code>i</code> is not in [1..k].
	 */
	private double rho(int i) throws InvalidAlgorithmParameterException{ /* ideal */
		
		if(i<1 || i> k) throw new InvalidAlgorithmParameterException();
		
		if(i==1){
			return(1.0/k);
		}
		else{
			return(1.0/(i*(i-1.0)));
		}
	}
	
	/**
	 * Extra set of values to the elements of mass function of the ideal soliton distribution.
	 * @param i Argument for the probability mass function.
	 * @return Value (respective to input) of the probability mass function.
	 * @throws InvalidAlgorithmParameterException In case <code>i</code> is not in [1..k].
	 */
	private double tau(int i) throws InvalidAlgorithmParameterException{ /* tau */
	
		if(i<1 || i> k) throw new InvalidAlgorithmParameterException();
		
		int kR = (int) Math.round(k/R);
	
		if(i < kR){
			return(R/(i*k));
		}
		else{
			if(i > kR){
				return(0);
			}
			else{
				return(R*Math.log(R/delta)/k);
			}
		}
	}
	
	/**
	 * Robust soliton distribution.
	 * @param i Argument for the probability mass function.
	 * @return Value (respective to input) of the probability mass function.
	 * @throws InvalidAlgorithmParameterException In case <code>i</code> is not in [1..k].
	 */
	private double mu(int i) throws InvalidAlgorithmParameterException{ /* mu */
	
		return((rho(i)+tau(i))/beta);
	}
	
	/**
	 * Samples the robust soliton distribution.
	 * @return Random degree number, according to the robust soliton probability distribution.
	 * @throws InvalidAlgorithmParameterException In case <code>i</code> is not in [1..k].
	 */
	public int soliton(long seed) throws InvalidAlgorithmParameterException{
		Random rGen = new Random(seed);		
		double r = rGen.nextDouble(), sum=0;
		int d=0;
		
		while(sum<r)
			sum+=mu(++d);

		return(d);
	}
	
	/**
	 * Calculates the number of encoded packets required at the receiving end to ensure 
	 * that the decoding can run to completion, with probability at least 1-delta.
	 * @return The number of encoded packets required at the receiving end to ensure 
	 * that the decoding can run to completion, with probability at least 1-delta.
	 */
	public int getBlocksNeeded(){

		return (int) (k*beta);
	}
}