
public class Testing {
	
	/**
	 * This method tests the protocol and takes currentNode number as argument.
	 * @param currentNode
	 */

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int nodeNo = Integer.parseInt(args[0]);
		String file = "Config.txt";
		Manager manage = new Manager(nodeNo, file);
		manage.start();
		manage.cs_enter();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		manage.cs_exit();
	}

}
