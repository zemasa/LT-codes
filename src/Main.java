import java.io.IOException;
import java.util.List;


public class Main {

	public static void main(String[] args) {

		System.out.print("Message: ");
		
		byte[] msg = new byte[32000];
		
		
		try {
			System.in.read(msg, 0, 32000);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.exit(-2);
		}
		
		System.out.println("\n\n------ YOUR MESSAGE ------\n" + new String(msg));
		
		Encoder enc = new Encoder(1000, 32);
		
		System.out.println("------ PADDED MESSAGE ------\n" + new String(enc.pad(msg)));
		
		EncodingThread eThread = new EncodingThread(enc, msg);
		DecodingThread dThread = new DecodingThread(enc);
		
		eThread.start();
		dThread.start();
		
		try {
			eThread.join();
			dThread.join();
		} catch (InterruptedException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.exit(-3);
		}

	}
}

class EncodingThread extends Thread {
    
	Encoder enc;
	byte[] msg;
	List<Block> encMsg;
	
    EncodingThread(Encoder e, byte[] m) {
        enc = e;
        msg = m;
    }

    public void run() {
    	
        encMsg = enc.encode(new String(msg));
                
        System.out.println("------ ENCODED MESSAGE ------");
        
        int i=1;
        for(Block b : encMsg){
        	String s = new String(b.getData());
        	
        	if(!(s.equals(""))){
        		for(int j=0; j<32; j++)
        			System.out.print(b.getData()[j]);
    			if(i%4==0) System.out.println("");
        	}
        	i++;
        }
        System.out.println("");
    }
}

class DecodingThread extends Thread {
    
	Encoder enc;
	
    DecodingThread(Encoder e) {
        enc = e;
    }

    public void run() {
    	
        System.out.println("------ DECODED MESSAGE ------\n" + new String(enc.decode()));
    }
}