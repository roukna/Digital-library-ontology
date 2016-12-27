package virtuoso.graphs.view;

public class MyWrapper {
	
	String returnedClass;
	boolean obfuscated;
	
	MyWrapper() {
		returnedClass=null;
		obfuscated=false;
	}
	MyWrapper(String returnedClass, boolean obfuscated) {
		this.returnedClass=returnedClass;
		this.obfuscated=obfuscated;
	}
	
}
