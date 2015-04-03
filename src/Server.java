import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class Server implements Runnable{
	public Manager manage;
 
	public boolean end = true;

	private ServerSocket serverSock;
	private int port;
	
	public Server(Manager mang, int port){
		this.manage = mang;
		this.port = port;
	}
	
	@Override
	public void run() {
		go();
		
	}
	
	public void go()
	{
		String message="Hello from server";
		try
		{
			serverSock = new ServerSocket(port);
			//Server goes into a permanent loop accepting connections from clients			
			while(end)
			{
				//Listens for a connection to be made to this socket and accepts it
				//The method blocks until a connection is made
				Socket sock = serverSock.accept();
				//PrintWriter is a bridge between character data and the socket's low-level output stream
//				PrintWriter writer = new PrintWriter(sock.getOutputStream());
//				writer.println(message);
//				writer.close();
				
				//BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
				//String cmd = br.readLine();
				Message msg = new Message();
				try {
					msg = (Message) ois.readObject();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(msg.messageType.equalsIgnoreCase("REQ")){
					int theirSeqNum = msg.seqNumber;
					//String requestedNode = br.readLine();
					int requestedNode = msg.sourceNode;
					//System.out.println("received request from "+requestedNode);
					manage.processReqMsg(theirSeqNum,requestedNode);
					
				}
				else if(msg.messageType.equalsIgnoreCase("REP")){
					
					//String repliedNode = br.readLine();
					int repliedNode = msg.sourceNode;
                    int[] csVect = msg.csVec;
					int csCount = msg.csCount;
					//System.out.println("received reply from "+repliedNode);
					manage.processReplyMsg(repliedNode,csVect,csCount);
					
				}
			}
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}
	

}
