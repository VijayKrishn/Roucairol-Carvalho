import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;


public class Manager {
	
	/*
	 * Variables used in the program read from config.txt file
	 * -------------------------------------------------------
	 * totalNumber = Number of processes/nodes
	 * nodeNo = Current process/node
	 * nodeMap = Hashmap to store node information against the node number
	 * E = Critical section execution time
	 * SD = Syncronization delay, delay between two critical section executions
	 * CSRequests = Number of critical section requests
	 * permissions[] = Boolean array for checking the keys of all processes
	 * reqDefered[] = Boolean array for maintaining defered requests when process in Critical Section
	 * USING = Process in critical section
	 * WAITING = Process waiting for requested keys
	 * OUR_SEQ_NUMBER = Logical clock value used for sending request
	 * HIGH_SEQ_NUMBER = Logical clock of the process that gets updated in case of an event
	 */
	
	public volatile boolean[] permissions;
	public volatile boolean[] reqDefered;
	public int totalNumber;
	public int nodeNo;
	public int OUR_SEQ_NUMBER, HIGH_SEQ_NUM;
	public volatile boolean USING, WAITING;
	public int counter;
	public HashMap<Integer,String> nodeMap = new HashMap<Integer,String>();
	public Server server;
	public String fileName;
	public int E, SD, CSRequests;
	public ReentrantLock lock = new ReentrantLock(); 
	public volatile boolean flag = false;
	public volatile int[] csVector;
	public volatile int csCounter;
	
	
	public Manager(int nodeNo, String fileName){
		this.fileName = fileName;
		this.nodeNo = nodeNo;
	}
	
	/**
	 * This method reads config.txt and initializes all the variables
	 * @param fileName
	 */
	
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
					//System.out.println(line);
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
		csVector = new int[totalNumber];
		csCounter = 0;
		USING = false;
		WAITING = false;
		counter = 0;
	}
	
	/**
	 * This starts the manager and calls parsefile and starts server thread.
	 * @param fileName
	 */
	
	public void start(){
		parseConfigFile(fileName);
		String add = nodeMap.get(nodeNo);
		String[] ips = add.split(":");
		server = new Server(this, Integer.parseInt(ips[1]));
		Thread serverThread = new Thread(server);
		serverThread.start();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * stops the server.
	 */
	public void stop(){
		server.end=false;	
	}
	
	/**
	 * enters critical section when this method is called.
	 * @return boolean
	 */
	
	public boolean cs_enter(){
		int csSum=0;
		synchronized(lock){
                    //System.out.println("lock acquired "+ nodeNo);
		    this.WAITING =true;
		    OUR_SEQ_NUMBER = HIGH_SEQ_NUM+1;
            System.out.println("CS requested from node "+nodeNo+" with " + OUR_SEQ_NUMBER);
                   // System.out.println("lock released "+ nodeNo);
		}
		for(int i=0;i<totalNumber;i++){
			if(i!=nodeNo && !permissions[i]){
				sendRequest(OUR_SEQ_NUMBER,nodeNo,i);
                                //System.out.println("SEQ NUM of request from "+ nodeNo + "is" + OUR_SEQ_NUMBER);
			}
			
			
		}
		
		while(!flag){ 
		//	System.out.println("CS counter"+ counter);
                        
		}
                //System.out.println("nenu true "+ nodeNo);
		synchronized(lock){
                    System.out.println("lock acquired for CS "+ nodeNo);
		    this.WAITING = false;
		    this.USING = true;
		    csVector[nodeNo]++;
		    csCounter++;
		    
		    System.out.println("CS Vector is:");
		    for(int k=0;k<totalNumber;k++){
			    csSum = csSum+csVector[k];
			    System.out.print(csVector[k] + " ");
		    }
		    if(csSum>csCounter){
		         System.out.println("MUTUAL EXCLUSION VIOLATED");
		         csCounter++;
		    } 
		    System.out.println("CS counter is"+ csCounter);
		    System.out.println("In critical section of "+ nodeNo + " for "+ OUR_SEQ_NUMBER);
                   // System.out.println("lock released for CS "+ nodeNo);
		}
		
		return true;
	}//System.out.print(String.valueOf(permissions[k])+" ");
	
	/**
	 * leaves critical section after this method is called.
	 * @return boolean
	 */
	
	public boolean cs_exit(){
		synchronized(lock){
               // System.out.println("lock acquired "+ nodeNo);
		this.USING = false;
		for(int j = 0;j<totalNumber;j++){
			if(reqDefered[j]){
				permissions[j]=false;
				checkFlag();
				reqDefered[j]=false;
				sendReply(nodeNo, j);
			}
		}
             //   System.out.println("lock released "+ nodeNo);
		}
		return true;
	}
	
	/**
	 * 
	 * @param seqNumber
	 * @param nodeNumber
	 * @param destination
	 */
	
	public void sendRequest(int seqNumber,int nodeNumber, int destination){
		try
		{
			String address = nodeMap.get(destination);
			String[] ips = address.split(":");
			//Create a client socket and connect to server at 127.0.0.1 port 5000
			Socket clientSocket = new Socket(ips[0],Integer.parseInt(ips[1]));
			
           // System.out.println("sent request to "+ destination+" from "+ nodeNo);
			ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
			Message reqMsg = new Message("REQ",null,-1,nodeNumber,seqNumber);
			oos.writeObject(reqMsg);
			//writer.close();
			oos.close();
			clientSocket.close();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param nodeNo
	 * @param destination
	 */
	
	public void sendReply(int nodeNo, int destination){
		try
		{
			String address = nodeMap.get(destination);
			String[] ips = address.split(":");
			//Create a client socket and connect to server at 127.0.0.1 port 5000
			Socket clientSocket = new Socket(ips[0],Integer.parseInt(ips[1]));
			//Read messages from server. Input stream are in bytes. They are converted to characters by InputStreamReader
			//Characters from the InputStreamReader are converted to buffered characters by BufferedReader
			//PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
			ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
			Message replyMsg = new Message("REP",csVector,csCounter,nodeNo,-1);
			oos.writeObject(replyMsg);
			
			
			oos.close();
			clientSocket.close();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}		
	}

	/**
	 * 
	 * @param theirSeqNum
	 * @param requestedNode
	 */
	
	public void processReqMsg(int theirSeqNum, int requestedNode){
		int theirNum = theirSeqNum;
		int theirNodeNo = requestedNode;
		boolean ourPriority = false;
		synchronized(lock){		
		//System.out.println("lock acquired "+ nodeNo);
	
		HIGH_SEQ_NUM = Math.max(theirNum, HIGH_SEQ_NUM);
		if(theirNum > OUR_SEQ_NUMBER){
			ourPriority = true;
		}else if((theirNum ==OUR_SEQ_NUMBER) && (theirNodeNo > nodeNo))
			ourPriority = true;
		
		
		if(USING ||(WAITING && ourPriority)){
			reqDefered[theirNodeNo] = true;
		} else if(!(USING || WAITING) ||(WAITING && !permissions[theirNodeNo] && !ourPriority)){
			permissions[theirNodeNo] = false;
                        checkFlag();
			
			sendReply(nodeNo, theirNodeNo);
		} else if(WAITING && permissions[theirNodeNo] && !ourPriority){
			permissions[theirNodeNo] = false;
			checkFlag();
			sendReply(nodeNo, theirNodeNo);
			sendRequest(OUR_SEQ_NUMBER, nodeNo, theirNodeNo);
		}
                //System.out.println("lock released "+ nodeNo);
		}
	}
	
	/**
	 * 
	 * @param repliedNode
	 */
	
	public void processReplyMsg(int repliedNode, int[] csVecs, int csCount){
		int theirNodeNo  = repliedNode;
		synchronized(lock){
                //System.out.println("lock acquired reply"+ nodeNo);
			for(int k=0;k<totalNumber;k++){
				csVector[k]  = Math.max(csVector[k],csVecs[k]);
			}
			csCounter = Math.max(csCounter, csCount);
		permissions[theirNodeNo] = true;
		checkFlag();
		
		//System.out.println("lock released reply"+ nodeNo);
		}
	}
	
	public void checkFlag(){
		
		
			boolean check=true;
			for(int i=0;i<totalNumber;i++){
				if(i!=nodeNo){
					check = check & permissions[i];
				}
			}
			
			if(check){
                               // System.out.println("Node number "+nodeNo+" flag changed because of "+ String.valueOf(check)+ " and permission array is:");
		                for(int k=0;k<totalNumber;k++){
			            // System.out.print(String.valueOf(permissions[k])+" ");
		                }
                                System.out.println("#################");
                        }
			flag = check;
                       // if(flag)
                           // System.out.println("changed flag to "+ String.valueOf(flag));
			
		
	}
}
