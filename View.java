package virtuoso.graphs.view;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.StringTokenizer;
//import virtuoso.jena.driver.VirtModel;
import virtuoso.graphs.view.MyWrapper;
import virtuoso.graphs.view.NoPermissionForClassException;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import virtuoso.graphs.com.Ops;
import virtuoso.jena.driver.VirtModel;


public class View {
			
		protected static Connection con;
		protected String URL = "jdbc:virtuoso://localhost:1111";
		protected String DRIVER_NAME="virtuoso.jdbc3.Driver";
		
		protected String graphName;
		protected String viewGraph;
		protected String viewUser;
		protected String permissionFile;
		
		protected Hashtable<String, Integer> noOfVisits;
		protected Ops m;
		protected static OntModel model;
		
		View(String userName, String password, String viewUser) throws SQLException, ClassNotFoundException {
			
			this.viewUser=viewUser;
			
			Class.forName(DRIVER_NAME);
			con = DriverManager.getConnection (URL, userName, password);
			
			m=new Ops(con);
			noOfVisits=new Hashtable<String, Integer>();
			
			model=ModelFactory.createOntologyModel();			
		}
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
				parentAuth=new String("positive");
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

		
		protected void viewCreate(String graph, String permPath) throws SQLException {
			
			graphName=graph;
			permissionFile=permPath;
			
			
			int index=graphName.lastIndexOf('.');
			String firstPart=graphName.substring(0, index);
			String lastPart=graphName.substring(index, graphName.length());
			viewGraph=firstPart + "View" + viewUser+lastPart;
		
			Statement s=con.createStatement();
			s.executeQuery("sparql DROP SILENT GRAPH <" + viewGraph + ">");
			s.executeQuery("sparql CREATE GRAPH <" + viewGraph + ">");
		
			
			ResultSet rs=m.listHierarchyRootNodes(graphName);
		
			while(rs.next()) {
				
				try {
				String root=rs.getObject(1).toString();
				
				MyWrapper p=createViewRoot(root,root); 
				}
				catch(IOException e) {
					System.out.println("IOException in creatViewRoot!");
					e.printStackTrace();
				}
			}
			
			/*System.out.println("Write.");
			VirtModel virtM=VirtModel.openDatabaseModel(viewGraph, URL, "dba", "virtuoso"); //change
			model.add(virtM);
			
			/*com.hp.hpl.jena.util.iterator.ExtendedIterator<OntClass> roots=model.listHierarchyRootClasses();
			
			System.out.println("beep");
			
			while(roots.hasNext()) {
				
				OntClass rootC=roots.next();
				
				System.out.println("Root class="+ rootC.toString());
				
				com.hp.hpl.jena.util.iterator.ExtendedIterator<OntClass> subC=rootC.listSubClasses();
				
				System.out.println("Subclasses:");
				
				while(subC.hasNext())
					System.out.println(subC.next().toString());
				
			}
			
			System.out.println("XML:");
			model.write(System.out);
			
			FileOutputStream fout=null;
			try {
				fout=new FileOutputStream("/home/roukna/Project/LibraryOutput");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("XML:");
			model.write(fout);*/
		}
		
		
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
			try {
				if(line!=null)
					return st.nextToken();
					else {
						throw new NoPermissionForClassException(node);
					}
				}
			catch(NoPermissionForClassException e) {
				System.out.println(e);
				e.printStackTrace();
			}
			return null;
		}
		protected void addPropertiesInView(String nodeURI) throws SQLException {
			
			String query="sparql "+
					"INSERT INTO <"+ viewGraph +"> { <"+ nodeURI +"> ?p ?o }" +
					"WHERE { GRAPH <"+ graphName +"> "+
					"{ <"+ nodeURI +"> ?p ?o . FILTER (?p != rdfs:subClassOf)}}";
			
			Statement s=con.createStatement();
			s.executeQuery(query);
			
		}
		protected void addDocumentsInView(String nodeURI) throws SQLException {
			
			String query="sparql "+
					"INSERT INTO <"+ viewGraph +"> { ?s rdf:type <"+ nodeURI +"> } " +
					"WHERE { GRAPH <"+ graphName +"> "+
					"{ ?s rdf:type <"+ nodeURI +"> } }";
			System.out.print(query);
			
			Statement s=con.createStatement();
			s.executeQuery(query);
			
			ResultSet documents=m.listDocumentsInClasses(nodeURI, graphName);
			
			while(documents.next())
				addPropertiesInView(documents.getObject(1).toString());
			
			
		}


	}


