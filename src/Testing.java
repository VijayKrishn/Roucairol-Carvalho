
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
                for(int i=0;i<10;i++){
		manage.cs_enter();
		System.out.println("' CS entered");
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("CS exit");
		manage.cs_exit();
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

                }
	}

}
