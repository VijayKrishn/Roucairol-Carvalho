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
	
	public Server(Manager mang){
		this.manage = mang;
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
			serverSock = new ServerSocket(5000);
			//Server goes into a permanent loop accepting connections from clients			
			while(end)
			{
				//Listens for a connection to be made to this socket and accepts it
				//The method blocks until a connection is made
				Socket sock = serverSock.accept();
				//PrintWriter is a bridge between character data and the socket's low-level output stream
				PrintWriter writer = new PrintWriter(sock.getOutputStream());
				writer.println(message);
				writer.close();
				
				BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				String cmd = br.readLine();
				
				if(cmd.equalsIgnoreCase("REQ")){
					String theirSeqNum = br.readLine();
					String requestedNode = br.readLine();
					manage.processReqMsg(theirSeqNum,requestedNode);
				}
				else if(cmd.equalsIgnoreCase("REP")){
					String repliedNode = br.readLine();
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
