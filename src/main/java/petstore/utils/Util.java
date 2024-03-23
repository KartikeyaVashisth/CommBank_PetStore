package petstore.utils;

public class Util {

	public int generateRandomId() {
		
		int random = (int) (Math.random()*100);
		return random;
	}
}
