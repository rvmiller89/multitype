/**
 * @author Ryan Miller
 */

package multitype;

public class FEUSender {

	private FEUSender() {
		
	}
	
	public static void send(FrontEndUpdate feu)
	{
		BackendClient bc = Activator.getDefault().client;
		bc.sendUpdate(feu);
	}

}
