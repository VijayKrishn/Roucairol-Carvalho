import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;


public class Manager {
	public boolean[] permissions;
	public boolean[] reqDefered;
	public int totalNumber;
	public int nodeNo;
	public int OUR_SEQ_NUMBER, HIGH_SEQ_NUM;
	public boolean USING, WAITING;
	public int counter;
	public HashMap<Integer,String> nodeMap = new HashMap<Integer,String>();
	public Server server;
	
	public Object lock = new Object(); 
	
	public static void main(String args[]){
		Manager manage = new Manager();
		//manage.parseConfigFile();
		manage.start("config.txt");
		manage.stop();
	}
	
	public void parseConfigFile(String fileName){
		//parse config file and set values
		nodeNo = 1;
		totalNumber = 5;
		OUR_SEQ_NUMBER=0;
		HIGH_SEQ_NUM = 0;
		permissions = new boolean[totalNumber];
		reqDefered = new boolean[totalNumber];
		USING = false;
		WAITING = false;
		counter = 0;
	}
	
	public void start(String fileName){
		parseConfigFile(fileName);
		
		server = new Server(this);
		
		Thread serverThread = new Thread(server);
		serverThread.start();
		
		
	}
	
	public void stop(){
		server.end=false;	
	}
	
	public boolean cs_enter(){
		synchronized(lock){
		    this.WAITING =true;
		    OUR_SEQ_NUMBER = HIGH_SEQ_NUM+1;
		}
		for(int i=1;i<=totalNumber;i++){
			if(i!=nodeNo && !permissions[i]){
				sendRequest(OUR_SEQ_NUMBER,nodeNo,i);
			}else if(i!=nodeNo && permissions[i])
				counter++;
		}
		
		while(counter != totalNumber-1){
			
		}
		synchronized(lock){
		this.WAITING = false;
		this.USING = true;
		}
		
		return true;
	}
	
	
	public boolean cs_exit(){
		synchronized(lock){
		this.USING = false;
		for(int j = 1;j<=totalNumber;j++){
			if(reqDefered[j]){
				permissions[j]=false;
				reqDefered[j]=false;
				sendReply(nodeNo, j);
			}
				
		}
		}
		
		return true;
		
	}
	
	public void sendRequest(int seqNumber,int nodeNumber, int destination){
		try
		{
			String address = nodeMap.get(destination);
			String[] ips = address.split(":");
			//Create a client socket and connect to server at 127.0.0.1 port 5000
			Socket clientSocket = new Socket(ips[0],Integer.parseInt(ips[1]));
			//Read messages from server. Input stream are in bytes. They are converted to characters by InputStreamReader
			//Characters from the InputStreamReader are converted to buffered characters by BufferedReader
			PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
			writer.println("REQ");
			writer.println(seqNumber);
			writer.println(nodeNumber);
			//The method readLine is blocked until a message is received 

			writer.close();
			clientSocket.close();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void sendReply(int nodeNo, int destination){
		try
		{
			String address = nodeMap.get(destination);
			String[] ips = address.split(":");
			//Create a client socket and connect to server at 127.0.0.1 port 5000
			Socket clientSocket = new Socket(ips[0],Integer.parseInt(ips[1]));
			//Read messages from server. Input stream are in bytes. They are converted to characters by InputStreamReader
			//Characters from the InputStreamReader are converted to buffered characters by BufferedReader
			PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
			writer.println("REP");
			writer.println(nodeNo);
			//The method readLine is blocked until a message is received 

			counter--;
			writer.close();
			clientSocket.close();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}		
	}

	public void processReqMsg(String theirSeqNum, String requestedNode){
		int theirNum = Integer.parseInt(theirSeqNum);
		int theirNodeNo = Integer.parseInt(requestedNode);
		boolean ourPriority = true;
		synchronized(lock){		
			
		HIGH_SEQ_NUM = Math.max(theirNum, HIGH_SEQ_NUM);
		if(theirNum > OUR_SEQ_NUMBER){
			ourPriority = false;
		}else if(theirNum ==OUR_SEQ_NUMBER && theirNodeNo > nodeNo)
			ourPriority = false;
		
		
		if(USING ||(WAITING && ourPriority)){
			reqDefered[theirNodeNo] = true;
		} else if(!(USING || WAITING) ||(WAITING && !permissions[theirNodeNo] && !ourPriority)){
			permissions[theirNodeNo] = false;
			sendReply(nodeNo, theirNodeNo);
		} else if(WAITING && permissions[theirNodeNo] && !ourPriority){
			permissions[theirNodeNo] = false;
			sendReply(nodeNo, theirNodeNo);
			sendRequest(OUR_SEQ_NUMBER, nodeNo, theirNodeNo);
		}
		}
	}
	
	public void processReplyMsg(String repliedNode){
		int theirNodeNo  = Integer.parseInt(repliedNode);
		synchronized(lock){
		permissions[theirNodeNo] = true;
		counter++;
		}
	}	
}
