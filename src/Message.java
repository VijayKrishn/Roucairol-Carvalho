import java.io.Serializable;


public class Message implements Serializable{
 
	public String messageType;
	public int[] csVec=null;
	public int csCount=0;
	public int sourceNode;
	public int seqNumber;
	
	public Message(){
		messageType = "dummy";
	}
	public Message(String msgType, int[] csVector, int csCounter, int srcNode, int seqNum){
		this.messageType = msgType;
		this.csVec = csVector;
		this.csCount = csCounter;
		this.sourceNode = srcNode;
		this.seqNumber= seqNum;
	}
	
	
}
