package virtuoso.graphs.view;

import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Context;

@Path("/viewDefault")
public class ViewDemo {
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/credentials")
	public String viewCreate(@Context ServletContext ctx, @QueryParam("userName") String userName, @QueryParam("password") String pwd, @QueryParam("graphName") String graphName, @QueryParam("userID") String userID) throws ClassNotFoundException, SQLException {
		
		long startTime=System.nanoTime();
		
		
		//System.out.println(userName+", "+pwd+", "+graphName);
		
		int firstIndex=graphName.lastIndexOf('/');
		int lastIndex=graphName.lastIndexOf('.');
		String subString=graphName.substring(firstIndex+1, lastIndex);
		System.out.println(subString);
		
		String permissionFile="/WEB-INF/"+subString+"DefaultPermission.txt";
		String path=ctx.getRealPath(permissionFile);
		
		
		View v=new View(userName, pwd, userID);
		v.viewCreate(graphName, path);
		
		long endTime=System.nanoTime();
		long timeRequired=endTime-startTime;
		return new String("View created successfully, Time required="+(timeRequired*(Math.pow(10, -9)))+" seconds.");
	}

}
