package virtuoso.graphs.com;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Ops {
	
Connection con;
	
	public Ops(Connection con) {
		
		this.con=con;
		
	}
	
	public ResultSet listHierarchyRootNodes(String graphName) throws SQLException {
		
		String query="sparql PREFIX scf: <http://www.iwi-iuk.org/material/RDF/Schema/Class/scf#> "+
				"SELECT ?cls1 " +
				"FROM <"+ graphName +"> " +
				"WHERE { "+
				"?cls1 a owl:Class . "+
				"FILTER NOT EXISTS {?cls1 rdfs:subClassOf ?cls2 .} "+
				"}";
		
		Statement s=con.createStatement();
		ResultSet rs=s.executeQuery(query);
		
		return rs;

	}
	public ResultSet listSubClassNodes(String parentURI, String graphName) throws SQLException {
	
	String query="sparql "+
			"SELECT ?cls " +
			"FROM <"+ graphName +"> " +
			"WHERE { ?cls a owl:Class . ?cls rdfs:subClassOf <"+ parentURI +"> }";
	
	Statement s=con.createStatement();
	ResultSet rs=s.executeQuery(query);
	
	return rs;

	}

	private ResultSet listSuperClassNodes(String childURI, String graphName) throws SQLException {
		
		String query="sparql "+
				"SELECT ?cls " +
				"FROM <"+ graphName +"> " +
				"WHERE { ?cls a owl:Class . <"+ childURI +"> rdfs:subClassOf ?cls }";
		
		Statement s=con.createStatement();
		ResultSet rs=s.executeQuery(query);
		
		return rs;

	}
	public int countParentClassNodes(String childURI, String graphName) throws SQLException {
		
		int count=0;
		ResultSet rs=listSuperClassNodes(childURI, graphName);
		
		while(rs.next()) {
			count++;
		}
		return count;

	}
	public boolean isClassInGraph(String nodeURI, String graphName) throws SQLException {
		
		String query="sparql "+
				"SELECT ?cls " +
				"FROM <"+ graphName +"> " +
				"WHERE { ?cls a owl:Class . FILTER ( ?cls = <"+ nodeURI +"> )  }";
		
		Statement s=con.createStatement();
		ResultSet rs=s.executeQuery(query);
		
		return (rs.next());

		
	}
	public boolean hasSuperClassInGraph(String nodeURI, String graphName) throws SQLException {
		
		String query="sparql "+
				"SELECT ?cls " +
				"FROM <"+ graphName +"> " +
				"WHERE { ?cls a owl:Class . <"+ nodeURI +"> rdfs:subClassOf ?cls }";
		
		Statement s=con.createStatement();
		ResultSet rs=s.executeQuery(query);
		
		return (rs.next());

		
	}
	public void addSubClasses(String parentURI,String childURI, String graphName) throws SQLException {
		
		String query= "sparql "+
					"INSERT DATA INTO <"+ graphName +">  { "+
					"<"+ childURI +"> rdfs:subClassOf <"+ parentURI +"> }";
		
		Statement s=con.createStatement();
		s.executeQuery(query);
	}
	public void createClassesInGraph(String nodeURI, String graphName) throws SQLException {
		
		String query= "sparql "+
				"INSERT INTO <"+ graphName +"> { "+
			"<"+ nodeURI +"> a owl:Class }";
	
	Statement s=con.createStatement();
	s.executeQuery(query);
		
	}
	public ResultSet listDocumentsInClasses(String parentURI, String graphName) throws SQLException {
		
		String query="sparql "+
				"SELECT ?s " +
				"FROM <"+ graphName +"> " +
				"WHERE { ?s rdf:type <"+ parentURI +"> }";
		
		Statement s=con.createStatement();
		ResultSet rs=s.executeQuery(query);
		
		return rs;

	}


}
