package virtuoso.graphs.view;

public class NoPermissionForClassException extends Exception{
	
	String description;
	
	NoPermissionForClassException(String description) {
		this.description=description;
	}
	public String toString() {
		return "No Permission asserted on node"+description+" for user.";
	}

}
