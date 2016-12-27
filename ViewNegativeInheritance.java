package virtuoso.graphs.view;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.StringTokenizer;

public class ViewNegativeInheritance extends View{
	
	ViewNegativeInheritance(String userName, String password, String viewUser) throws SQLException, ClassNotFoundException {
		
		super(userName, password, viewUser);			
	}
	
	@Override
	protected MyWrapper createViewRoot(String parent, String child) throws IOException, SQLException {
		
		String parentAuth=null, childAuth=null;
		String blankNode=null;
		boolean blankNodeDefined=false;
		boolean multiParentClassDefined=false;
		MyWrapper x;
		int visit;
		
		childAuth=getPermission(child);
		parentAuth=getPermission(parent);
		
		ResultSet subClasses=m.listSubClassNodes(child, graphName);
		
		if(parent==child && parentAuth.equals("default")) {
			parentAuth=new String("negative");
		}
		else if(parentAuth.equals("default")) {
			
			if(m.isClassInGraph(parent, viewGraph)==false)
				parentAuth=new String("negative");
			else
				parentAuth=new String("positive");
	
		}
		
		if(childAuth.equals("default")) {
			childAuth=parentAuth;
		}
			
		if(m.countParentClassNodes(child, graphName)>1) {
			
			if(noOfVisits.containsKey(child.toString())) {
				visit=noOfVisits.get(child.toString());
				noOfVisits.put(child.toString(), (++visit));
			}
			else
				noOfVisits.put(child.toString(), 1);
		}
		
		if(parent.equals(child) && childAuth.equals("positive")) {
			m.createClassesInGraph(child, viewGraph);
			addPropertiesInView(child);
			addDocumentsInView(child);
			
			while(subClasses.next()) 
				x=createViewRoot(child, subClasses.getObject(1).toString());
			
			return new MyWrapper(child, false);
		}
		else if(childAuth.equals("positive")) {
			m.createClassesInGraph(child, viewGraph);
			addPropertiesInView(child);
			addDocumentsInView(child);
			
			if(m.countParentClassNodes(child, graphName)>1) {
			
				if(noOfVisits.get(child.toString())<m.countParentClassNodes(child, graphName)) {
					
					if(parentAuth.equals("positive")) {
						m.addSubClasses(parent, child, viewGraph);
					}
					return new MyWrapper(child, false);
				}
				
			}
			
			if(parentAuth.equals("negative")) {
				
				if(m.countParentClassNodes(child, graphName)>1 && m.hasSuperClassInGraph(child, viewGraph))
					multiParentClassDefined=true;
				
				if(!multiParentClassDefined) {
				blankNode=parent+"obfuscated";
				m.createClassesInGraph(blankNode, viewGraph);
				m.addSubClasses(blankNode, child, viewGraph);
				}
				
				while(subClasses.next()) 
					x=createViewRoot(child, subClasses.getObject(1).toString());
				
				if(!multiParentClassDefined)
					return new MyWrapper(blankNode, true);
				else
					return new MyWrapper(child, false);
			}
			else if(parentAuth.equals("positive")) {
				m.addSubClasses(parent, child, viewGraph);
				
				while(subClasses.next()) 
					x=createViewRoot(child, subClasses.getObject(1).toString());
				
				return new MyWrapper(child, false);
			}
		}
		else if(childAuth.equals("negative")) {
			
			if(m.countParentClassNodes(child, graphName)>1) {
				
				if(noOfVisits.get(child.toString())<m.countParentClassNodes(child, graphName)) {
					return new MyWrapper(child, false);
				}
			}
			
			while(subClasses.next()) {
				
				String c=subClasses.getObject(1).toString();
				x=createViewRoot(child, c);
				
				if(x.obfuscated) {
					if(!blankNodeDefined) {
					blankNode=x.returnedClass;
					blankNodeDefined=true;
					}
				}					
			}
			if(blankNodeDefined) {
				
			if(parentAuth.equals("positive")) {
				m.addSubClasses(parent, blankNode, viewGraph);
				return new MyWrapper(blankNode, false);
			}
			else if(parentAuth.equals("negative") && parent!=child) {
				String blankNode2=parent+"obfuscated";
				m.createClassesInGraph(blankNode2, viewGraph);
				m.addSubClasses(blankNode2, blankNode, viewGraph);
				
				return new MyWrapper(blankNode2, true);
			}
			else if(parentAuth.equals("negative") && parent==child)
				return new MyWrapper(blankNode, true);
		}
		return new MyWrapper(child, false);
	}
	return new MyWrapper(null, false);		
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
