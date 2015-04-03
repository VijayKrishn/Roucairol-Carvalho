import java.util.Random;
public class Testing {
	
	/**
	 * This method tests the protocol and takes currentNode number as argument.
	 * @param currentNode
	 */

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int nodeNo = Integer.parseInt(args[0]);
		String file = "sample.txt";
		Manager manage = new Manager(nodeNo, file);
		
		manage.start();
		
		for(int i=0;i<manage.CSRequests;i++){
			manage.cs_enter();
			System.out.println("' CS entered");
			try {
				Thread.sleep(manage.E);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("CS exit");
			manage.cs_exit();
			
			int wait = getNext(manage.SD);
			try {
				Thread.sleep(wait);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}

	}
	public static int getNext(int lambda) {

		Random r = new Random();

		    return  (int) ((-lambda)*Math.log(1-r.nextDouble()));

	}
	

}

