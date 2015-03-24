import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
			//System.out.println(port);
			serverSock = new ServerSocket(port);
			//Server goes into a permanent loop accepting connections from clients			
			while(end)
			{
				//Listens for a connection to be made to this socket and accepts it
				//The method blocks until a connection is made
				Socket sock = serverSock.accept();
				//PrintWriter is a bridge between character data and the socket's low-level output stream
				
				BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				String cmd = br.readLine();
				System.out.println(cmd);
				
				if(cmd.equalsIgnoreCase("REQ")){
					String theirSeqNum = br.readLine();
					System.out.println("TheirSeqNum : " + theirSeqNum);
					String requestedNode = br.readLine();
					System.out.println("received request from : " + requestedNode);
					manage.processReqMsg(theirSeqNum,requestedNode);
				}
				else if(cmd.equalsIgnoreCase("REP")){
					String repliedNode = br.readLine();
					System.out.println("received reply from : " + repliedNode);
					manage.processReplyMsg(repliedNode);
				}
			}
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}
	

}
