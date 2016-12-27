package virtuoso.graphs.view;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.StringTokenizer;

public class ViewPositiveInheritance extends View{
	
	ViewPositiveInheritance(String userName, String password, String viewUser) throws SQLException, ClassNotFoundException {
		
		super(userName, password, viewUser);			
	}
	
	@Override
	protected String getPermission(String node) throws IOException{
		
		String line;
		BufferedReader br=null;
		StringTokenizer st=null;
	
		try {
			br=new BufferedReader(new FileReader(permissionFile));
		}
		catch(IOException e) {
			System.out.println("File not found!");
		}
	
		while((line=br.readLine())!=null) {
	
			st=new StringTokenizer(line, ",");
		
			if(st.nextToken().equals(node))
				break;
		}
		br.close();
		
			if(line!=null)
				return st.nextToken();
				else {
					return new String("default");
				}
	}
}
	


