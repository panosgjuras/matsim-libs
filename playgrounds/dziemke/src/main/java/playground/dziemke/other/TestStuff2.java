package playground.dziemke.other;

import org.junit.Assert;


public class TestStuff2 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		double value = 1.62345234523;
		
		value++;
				
		System.out.println(value);
		
		Assert.assertNotNull(value);
		
		
		System.out.println(Integer.MAX_VALUE);
		System.out.println(Integer.MIN_VALUE);
		System.out.println(Double.MAX_VALUE);
		System.out.println(Double.MIN_VALUE);
	}
}
