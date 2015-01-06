import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.lang.*;


public class sender {
	
	private static int position = 0;
	private static String hostaddress = null;
	private static int SendPort = 0;
	private static int ReceivePort = 0;
	private static String filename = null;
	
	private static int[] sendseq = new int[50];
	private static int[] recAckseq = new int [50];
	
	private static boolean[] firsttime = new boolean[50];
	
	private static int windowsize = 10;
	
	private static int next = 0;
	private static int base = 0;
	
	private static PrintWriter acklog;
	
	private static PrintWriter seqnumlog;
	
	private static DatagramSocket new_clientSocket;
	private static ArrayList<String> waitinglist = new ArrayList<String>();
	private static int max = 495;
	
	
	public static void main(String argv[]) throws Exception{
		
        
        if (argv.length != 4){
			
			System.out.println("ERROR: incorrect number of arguements of Receiver");
			System.exit(0);
		}
        
        System.out.println("Server Address:" + argv[0]);
        
		hostaddress = argv[0];
		SendPort = Integer.parseInt(argv[1]);
		ReceivePort = Integer.parseInt(argv[2]);
		filename = argv[3];
	    for (int i = 0; i < 32; i++){
            
	    	sendseq[i] = 0;
	    	recAckseq[i] = 0;
	    	firsttime[i] = true;
	    }
		new_clientSocket = new DatagramSocket(ReceivePort);
		
		acklog = new PrintWriter("ack.log", "UTF-8");
		seqnumlog = new PrintWriter("seqnum.log", "UTF-8");
        
        Processfile();
        if (waitinglist.size() <= windowsize){
        	
        	sendSRsmall();
        	
        }else if( waitinglist.size() > windowsize && waitinglist.size() <= 32){
        	
        	//System.out.println("sendsrlarge");
        	sendSRlarge();
        	
        }else if (waitinglist.size() > 32){
        	
        	//System.out.println("sendsrhuge");
        	sendSRhuge();
        	
        }
		
        int portals;
		int receiveport;
		
		portals = SendPort;
		receiveport = ReceivePort;
		
		int n_port = portals;
		int receive_port = receiveport;
		
		InetAddress IPAddress = InetAddress.getByName(hostaddress);
		
		//byte[] sendData = new byte[1024];
		byte[] receiveData = new byte[9999];
		
		//System.out.println("r_port : " + portals);
		packet newPacket = null;
		
		newPacket = packet.createEOT(base+1);
		
		DatagramPacket sendPacket =
        new DatagramPacket(newPacket.getUDPdata(),newPacket.getUDPdata().length, IPAddress,n_port);
		
		new_clientSocket.send(sendPacket);
		
		receiving();
		
		new_clientSocket.close();
		acklog.close();
		seqnumlog.close();
        
		
	}
	
	public static void sendSRhuge() throws Exception{
		
		for (int i = 0; i < windowsize; i++){
			if (recAckseq[i + base] == 0){
				//System.out.println("ffffffffffffffffffffff");
                
				sendData(waitinglist.get(i), i);
			}
		}
		
		receiving();
		
		while (!checkSeqNumhuge(base)){
			
			//System.out.println("the base     ===== " + base);
			
			for (int i = 0; i < windowsize; i++){
				if (i + base >= 32 && i + base < waitinglist.size()){
					if (recAckseq[i+base] == 0){
                        
                        //System.out.println("the i + base     ===== "  + base);
                        
						sendData(waitinglist.get(base + i), base + i);
					}
					
				}
				if (i + base < 32 && i + base < waitinglist.size()){
					if (recAckseq[i + base] == 0){
						
						//System.out.println("ffffffffffffffffffffff");
						
						sendData(waitinglist.get(base + i), base + i);
						
					}
                    
				}
				
			}
            
			receiving();
			//System.out.println("fzzzzzzzzzzzzz");
		}
		
        //new_clientSocket.close();
	}
	public static void sendSRlarge() throws Exception{
		
		for (int i = 0; i < windowsize; i++){
			if (recAckseq[i + base] == 0){
				//System.out.println("ffffffffffffffffffffff");
                
				sendData(waitinglist.get(i), i);
			}
			
		}
		
		receiving();
		
		while (!checkSeqNumlarge(base)){
			
			//System.out.println("the base     ===== " + base);
			
			for (int i = 0; i < windowsize; i++){
				if (i + base < 32 && i + base < waitinglist.size()){
					if (recAckseq[i + base] == 0){
						
                        //	System.out.println("ffffffffffffffffffffff");
						
						sendData(waitinglist.get(base + i), base + i);
						
					}
                    
				}
				
			}
            
			receiving();
			//System.out.println("fzzzzzzzzzzzzz");
		}
		
        //new_clientSocket.close();
        
	}
	
	
	public static void sendSRsmall() throws Exception{
		
		while (!checkSeqNum()){
			
			for (int i = 0; i < waitinglist.size(); i++){
				if (recAckseq[i] == 0){
					//System.out.println("ffffffffffffffffffffff");
                    
					sendData(waitinglist.get(i), i);
				}
				
			}
            
			receiving();
			//System.out.println("fzzzzzzzzzzzzz");
		}
		
        //new_clientSocket.close();
        
	}
	
    
	public static void receiveACK() throws Exception{
		
		int portals;
		int receiveport;
		
		portals = SendPort;
		receiveport = ReceivePort;
		
		int n_port = portals;
		int receive_port = receiveport;
		
		InetAddress IPAddress = InetAddress.getByName(hostaddress);
		byte[] receiveData = new byte[9999];
		
		while (true){
			
			DatagramPacket receivePacket =
            
            new DatagramPacket(receiveData, receiveData.length);
            
			new_clientSocket.receive(receivePacket);
            
			packet repacket = null;
            
			repacket = packet.parseUDPdata(receivePacket.getData());
            
			//System.out.println("afasdfadf");
            
            //System.out.println(new String(repacket.getData()));
            
			int index = repacket.getSeqNum();
			//System.out.println("the reciving index ==   " + index);
			
			acklog.println(index);
			
			if (firsttime[index]){
				recAckseq[index] = 1;
				firsttime[index] = false;
			}else{
				recAckseq[index + 32] = 1;
			}
			
			
			//checkSeqNum();
			String modifiedSentence =
            new String(repacket.getData());
			//System.out.println("CLIENT GOT REVERSED MSG FROM SERVER:"+ modifiedSentence);
            //new_clientSocket.close();
            
		}
        
        //System.out.println("fasdfadfasdfasdfasdfasgas############");
	}
	
	
	public static void sendData(String msg, int index) throws Exception
	{
		
		int portals;
		int receiveport;
		
		portals = SendPort;
		receiveport = ReceivePort;
		
		int n_port = portals;
		int receive_port = receiveport;
		
		InetAddress IPAddress = InetAddress.getByName(hostaddress);
		
		//byte[] sendData = new byte[1024];
		byte[] receiveData = new byte[9999];
		
		//System.out.println("r_port : " + portals);
		packet newPacket = null;
		
		newPacket = packet.createPacket(index % 32, msg);
		
		//System.out.println("the sending index ==   " + index);
		
		DatagramPacket sendPacket =
        new DatagramPacket(newPacket.getUDPdata(),newPacket.getUDPdata().length, IPAddress,n_port);
		
		seqnumlog.println(newPacket.getSeqNum());
		
		new_clientSocket.send(sendPacket);
		if (index < 32){
			sendseq[index] = 1;
		}else{
			sendseq[index] = 1;
		}
		
		//seqWriter
		
		//System.out.println("afasdfadf");
        
	}
	
	public static String readFile(int begin, int max, File f)
    throws IOException{
		
        Reader r =
        new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        
        try {
            String message = "";
            StringBuilder new_message = new StringBuilder();
            int count = 0;
            int intch;
            
            r.skip(begin);
            
            while ((intch = r.read()) != -1){
                
                byte[] b = message.getBytes("UTF-8");
                
                if (b.length < max){
                    message = message + (char)intch;
                    new_message.append((char) intch);
                    count ++;
                }else{
                    break;
                }
                
            }
            
            position  = position + count;
            
            return message;
            
        } finally {
            
            r.close();
            
        }
    }
	
	public static void Processfile() throws Exception{
		
		File f = new File(filename);
		long size = f.length();
		int counter = 0;
		int base = 0;
		
		while (position < size){
			
			packet packet = null;
			
			String field = readFile(position, max, f);
			
			packet = packet.createPacket(counter, field);
			
			counter ++;
			
			waitinglist.add(field);
		}
		
		//return 0;
        
	}
    
	public static void receiving() throws Exception{
		
		ExecutorService executor = Executors.newFixedThreadPool(2);
		
		Callable<String> callable = new Mythread();
		
		Future<String> future = executor.submit(callable);
		
		try{
			future.get(3, TimeUnit.SECONDS);
		}catch (TimeoutException e) {
			
            int nextSeqNum = 0;
        } catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
        
        executor.shutdown();
	}
	
    public static boolean checkSeqNumhuge(int begin){
		
        boolean test = true;
        if (begin > waitinglist.size()){
            
            return test;
        }
        for (int i = 0; i < waitinglist.size() ; i++){
            
            if (i + begin >= waitinglist.size()){
                break;
            }
            if (recAckseq[i + begin] == 0){
                test = false;
                break;
            }
            
            base = begin + i + 1;
        }
        
        //System.out.println("checke base     == " + base);
        return test;
        
	}
    
    
	public static boolean checkSeqNumlarge(int begin){
		
		boolean test = true;
		if (begin > waitinglist.size()){
			
			return test;
		}
		for (int i = 0; i < waitinglist.size() ; i++){
			
			if (i + begin >= waitinglist.size()){
				break;
			}
			if (recAckseq[i + begin] == 0){
				test = false;
				break;
			}
			
			base = begin + i + 1;
		}
		
		//System.out.println("checke base     == " + base);
		return test;
	}
	
	public static boolean checkSeqNum(){
		
		boolean test = true;
		for (int i = 0; i < waitinglist.size() ; i++){
			
			if (recAckseq[i] == 0){
				test = false;
			}
		}
		return test;
	}
}

class Mythread implements Callable<String>{
	@Override
	public String call() throws Exception{
		sender.receiveACK();
		return "lock!!!!!!!!!!";
	}
	
}
