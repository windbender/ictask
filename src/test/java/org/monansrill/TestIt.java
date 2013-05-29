package org.monansrill;

public class TestIt {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String in= "/ictask/jictask/items";
		String out = in.replaceAll("/", "");
		System.out.println("out is "+out);
	}

}
