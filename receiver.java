import java.io.*;
import java.net.*;
import java.util.Random;

public class receiver {
    
	private static int[] sendseq = new int[32];
	
	private static String[] reclist = new String[500];
	
	private static String address;
	
	private static String portals;
	
	private static String receiveport;
	
	private static String outputfile;
	
	private static PrintWriter fileWriter;
	
	private static boolean first = true;
	
    private static int roundtimes = 0;
    
    private static PrintWriter arrivallog;
    
	public static void main(String argv[]) throws Exception{
		if (argv.length != 4){
			
			System.out.println("ERROR: incorrect number of arguements of server");
			System.exit(0);
			
		}
		
		address = argv[0];
		portals = argv[1];
		receiveport = argv[2];
		
		outputfile = argv[3];
		//new PrintWriter(new OutputStreamWriter(new FileOutputStream("yourfilepath"), "UTF-8"))
		fileWriter =
        new PrintWriter(new OutputStreamWriter(new FileOutputStream(outputfile)));
		
		//out = new FileWriter("out");
		
		for (int i = 0; i< 32; i++){
			sendseq[i] = -1;
			reclist[i] = null;
		}
		
		arrivallog = new PrintWriter("arrival.log", "UTF-8");
		
		receiveControl();
		
		arrivallog.close();
		fileWriter.close();
		
	}
	public static void receiveControl() throws Exception
	{
		
		Random rand = new Random();
		
		int n_port = 1025;
		
		n_port =1025 + rand.nextInt(1000);
		
		n_port = Integer.parseInt(portals);
		
		//int receive_port = Integer.parseInt(receiveport);
		
		
		
		ServerSocket welcomeSocket = new ServerSocket(n_port);
		
		int r_port = 1025;
		
		r_port = r_port + rand.nextInt(1000);
		
		r_port = Integer.parseInt(receiveport);
		
		System.out.println("the n_port from server: " + n_port);
		System.out.println("the receive_port from server: " + r_port);
		
		String r_port1 = Integer.toString(r_port);
		
		DatagramSocket serverSocket =
        new DatagramSocket(r_port);
		
		byte[] receiveData = new byte[1024];
		
		while(true)
		{
			DatagramPacket receivePacket =
            new DatagramPacket(receiveData, receiveData.length);
			
			serverSocket.receive(receivePacket);
			
			packet repacket = null;
	        
			repacket = packet.parseUDPdata(receivePacket.getData());
			
            int index = repacket.getSeqNum();
            
			InetAddress IPAddress = receivePacket.getAddress();
			
			//System.out.println("afasdfadf");
			
			//System.out.println("the ACK index ==   " + index);
			
			//System.out.println(new String(repacket.getData()));
            
			arrivallog.println(index);
			
			if (index == 31){
                
				first = false;
                roundtimes = roundtimes + 1;
                
			}
			
			if (repacket.getType() == 2){
				packet eotpacket = packet.createEOT(index);
				DatagramPacket sendPacket =
                new DatagramPacket(eotpacket.getUDPdata(),eotpacket.getUDPdata().length, IPAddress,n_port);
                
				serverSocket.send(sendPacket);
				for (int i = 0; i < 50; i++ ){
                    
					if(reclist[i] == null){
                        continue;
                    }
					//out.write('\ufeff');
					fileWriter.print(reclist[i]);
					//out.write(reclist[i]);
				}
				
				serverSocket.close();
				return;
			}
			
			sendseq[index] = 1;
			
			String sentence = new String(receivePacket.getData());
            
			
			
			//int port = receivePacket.getPort();
			
			String new_sentence =
            sentence;
			
			if (first == false && index < 12){
				
				//System.out.println("hereeeeeeeeee!!!!!!!!!!");
				
				reclist[index + (32 * roundtimes)] = new String(repacket.getData());
				
			}else{
				reclist[index] = new String(repacket.getData());
			}
            
			
			//System.out.println("Sentence reversed on :" + new_sentence);
			packet newPacket = null;
			
			//System.out.println(new_sentence.length());
			
			
			newPacket = packet.createACK(index);
			
			
			DatagramPacket sendPacket =
	        new DatagramPacket(newPacket.getUDPdata(),newPacket.getUDPdata().length, IPAddress,n_port);
            
			serverSocket.send(sendPacket);
			
		}
		
		
	}
}
