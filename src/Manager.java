import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;


public class Manager {
	public boolean[] permissions;
	public boolean[] reqDefered;
	// totalNumber = number of processes/nodes
	public int totalNumber;
	public static int nodeNo;
	// nodeNo = current process/node
	public int OUR_SEQ_NUMBER, HIGH_SEQ_NUM;
	public boolean USING, WAITING;
	public int counter;
	public HashMap<Integer,String> nodeMap = new HashMap<Integer,String>();
	public Server server;
	// E = Critical section execution time
	// SD = Syncronization delay, delay between two critical section executions
	// CSRequests = Number of critical section requests
	public static int E, SD, CSRequests;
	
	public Object lock = new Object(); 
	
	public static void main(String args[]){
		nodeNo = Integer.parseInt(args[0]);
		Manager manage = new Manager();
		manage.start("config.txt");
		manage.stop();
	}
	
	public void parseConfigFile(String fileName){
		//parse config file and set values
		BufferedReader br;
		totalNumber = -1;
		E = SD = CSRequests = -1;
		int linecount = 0;
		try {
			br = new BufferedReader(new FileReader(fileName));
			String line;
			while((line = br.readLine()) != null){
				if(line.length() == 0);
				else if(line.charAt(0) == '#');
				else if(totalNumber == -1){
					totalNumber = Integer.parseInt(line.trim());
					linecount = totalNumber;
					//System.out.println(linecount);
				}
				else if(linecount>0){
					line = line.trim().replaceAll("(\t)+", ",");
					System.out.println(line);
					//Initializing the Hashmap with the node configuration
					nodeMap.put(Integer.parseInt(line.split(",")[0]), line.split(",")[1] + ":" + line.split(",")[2]);
					linecount--;
				}else if(CSRequests == -1)
					CSRequests = Integer.parseInt(line.trim());
				else if(SD == -1)
					SD = Integer.parseInt(line.trim());
				else if(E == -1)
					E = Integer.parseInt(line.trim());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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
			}
			
			// only when we receive a reply counter is incremented.
//			else if(i!=nodeNo && permissions[i])
//				counter++;
		}
		
		while(counter != totalNumber-1){ }
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
				counter--;
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
			//*****Should be decremented when permission is made false.
			//counter--;
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
			//counter--;
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
