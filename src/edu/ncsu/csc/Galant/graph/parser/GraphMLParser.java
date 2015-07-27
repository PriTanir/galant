/**
 * @file GraphMLParser.java
 * @brief code for creating a graph from a GraphML file/string
 *
 * @todo Would probably be best to make the x and y attributes of nodes act
 * as surrogates for positionInLayer and layer, respectively, when a graph
 * is layered. The only part of the code that needs to know about these
 * attributes is the display in GraphPanel. The change would require changes
 * in GraphPanel, Node, NodeState, and possibly Graph and GraphState.
 *
 * @todo A lot of attributes of nodes, edges, and graphs should be set to
 * null if they don't exist (and then not printed when the graph is saved or
 * exported). Default values can be used during display in GraphPanel instead
 * of forced at time of creation. Numerical attributes should be Integer or
 * Double instead of int or double. Changes would be required here, in
 * GraphPanel, in Node, NodeState, Edge, EdgeState, Graph, GraphState, and
 * various places where the numerical attributes are referred to.
 *
 * $Id: GraphMLParser.java 113 2015-05-05 15:31:47Z mfms $
 */

package edu.ncsu.csc.Galant.graph.parser;

import java.awt.Point;
import java.io.File;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.graph.component.Edge;
import edu.ncsu.csc.Galant.graph.component.Graph;
import edu.ncsu.csc.Galant.graph.component.GraphState;
import edu.ncsu.csc.Galant.graph.component.Node;
import edu.ncsu.csc.Galant.graph.component.GraphElement;
import edu.ncsu.csc.Galant.graph.component.AttributeList;
import edu.ncsu.csc.Galant.logging.LogHelper;

/**
 * Parses a text file and creates a <code>Graph</code> from it. Allows for graph-to-editor
 * and editor-to-graph manipulation.
 * @author Ty Devries
 *
 */
public class GraphMLParser {
	
	Graph graph;
	File graphMLFile;
	Document document;
	
	public GraphMLParser(File graphMLFile) {
		this.graph = generateGraph(graphMLFile);
	}
	
	public GraphMLParser(String xml) throws GalantException {
        if ( xml == null || xml.equals( "" ) ) {
            throw new GalantException( "empty graph when invoking GraphMLParser" );
        }
		this.graph = generateGraph(xml);
	}
	
	public DocumentBuilder getDocumentBuilder( DocumentBuilderFactory dbf )
        throws GalantException
    {
		DocumentBuilder db = null;
		
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
            throw new GalantException( e.getMessage()
                                       + "\n - in getDocumentBuilder",
                                       e );
		}
		
		return db;
	}
	
    /**
     * @todo Not clear the this is ever called
     */
	public void setDocument( DocumentBuilder db, File file )
        throws GalantException {
		try {
			this.document = db.parse(file);
		}
        catch ( Exception e ) {
            throw new GalantException( e.getMessage()
                                       + "\n - in setDocument(DocumentBuilder, File)",
                                       e );
        }
	}
	
	public void setDocument( DocumentBuilder db, String xml )
        throws GalantException
    {
		InputSource is = new InputSource(new StringReader(xml));
		try {
			this.document = db.parse(is);
		}
        catch (Exception e) {
            throw new GalantException( e.getMessage()
                                       + "\n - in setDocument(DocumentBuilder,String)",
                                       e );
		}
	}
	
    /**
     * Parses the value stored in the xml node, trying first to interpret it
     * as an integer, then as a double, then as a boolean, and the leaving it
     * as a string; the attribute name,value pair is then stored as an
     * attribute of the graph node. 
     */
    private void processAttribute(GraphElement graphElement, Node xmlNode) {
        String attributeName = xmlNode.getNodeName();
        String attributeValueString = xmlNode.getTextContent();
        try {
            Integer intVal = Integer.parseInt(attributeValueString);
            graphElement.set(attributeName, intVal);
        }
        catch (NumberFormatException intE) {
            try {
                Double doubleVal = Double.parseDouble(attributeValueString);
                graphElement.set(attributeName, doubleVal);
            }
            catch (NumberFormatException doubleE) {
                if ( attributeValueString.equalsIgnoreCase("true") ) {
                    graphElement.set(attributeName, true);
                }
                else if ( attributeValueString.equalsIgnoreCase("false") ) {
                    graphElement.set(attributeName, false);
                }
                else {
                    graphElement.set(attributeName, attributeValueString);
                }
            }
        }
    }

	public Graph buildGraphFromInput( DocumentBuilder db ) 
        throws GalantException
    {
        LogHelper.enterMethod( getClass(), "buildGraphFromInput" );
		GraphDispatch dispatch = GraphDispatch.getInstance();
		
		//TODO populate Graph g
		Graph graphUnderConstruction = new Graph();
		NodeList nodes;
		NodeList edges;
		NodeList graph;
		GraphState graphState = graphUnderConstruction.getGraphState();
		graphState.setLocked(true);
		nodes = getNodes();
		edges = getEdges();
		graph = getGraphNode();

		NamedNodeMap attributes = graph.item(0).getAttributes(); //only one graph => hardcode 0th index

        // the awkward ?: construction is needed because org.w3c.dom.Node
        // conflicts with Galant Node
		String directed = ((attributes.getNamedItem("edgedefault") != null)
                           ? attributes.getNamedItem("edgedefault").getNodeValue()
                           : "undirected");
		graphUnderConstruction.setDirected(directed.equalsIgnoreCase("directed"));

        String name = (attributes.getNamedItem( "name" ) != null )
            ? attributes.getNamedItem("name").getNodeValue()
            : null;
        graphUnderConstruction.setName( name );
        String comment = ( attributes.getNamedItem( "comment" ) != null )
                       ? attributes.getNamedItem("comment").getNodeValue()
                       : null;
        graphUnderConstruction.setComment( comment );
        String type = ( attributes.getNamedItem( "type" ) != null )
                       ? attributes.getNamedItem("type").getNodeValue()
                       : null;
        String typename = ( attributes.getNamedItem( "type" ) != null )
                       ? attributes.getNamedItem("type").getNodeValue()
                       : null;
        if ( typename != null && typename.equalsIgnoreCase( "layered" ) ) {
            graphUnderConstruction.setLayered( true );
        }
        else {
            graphUnderConstruction.setLayered( false );
        }
		
        LogHelper.logDebug( "Created new graph:\n" + g );
        LogHelper.logDebug( " number of nodes = " + nodes.getLength() );
        LogHelper.logDebug( " number of edges = " + edges.getLength() );

        LogHelper.beginIndent();
		for ( int nodeIndex = 0; nodeIndex < nodes.getLength(); nodeIndex++ ) {
            LogHelper.logDebug( " processing " + nodeIndex + "th node." );
            Node node = nodes.get(nodeIndex);
            Node graphNode = new GraphElement(graphUnderConstruction, graphState);
            NamedNodeMap attributes = node.getAttributes();
            if ( attributes != null ) {
                for ( int i = 0; i < attributes.getLength(); i++ ) {
                    Node attribute = attributes.item(i);
                    processAttribute(graphNode, attribute);
                    logHelper.logDebug("Node attribute " + attribute.getNodeName()
                                       + ", value = " + attribute.getTextContent());
                }
            }
            LogHelper.logDebug( "adding node " + graphNode );
            graphNode.initializeAfterParsing();
            graphUnderConstruction.addNode(graphNode);
		}
        LogHelper.endIndent();

        LogHelper.beginIndent();
		for ( int edgeIndex = 0; edgeIndex < edges.getLength(); edgeIndex++ ) {
            LogHelper.logDebug( " processing " + nodeIndex + "th edge." );
            Node node = edges.get(edgeIndex);
            Node graphEdge = new Edge(graphUnderConstruction, graphState);
            NamedNodeMap attributes = node.getAttributes();
            if ( attributes != null ) {
                for ( int i = 0; i < attributes.getLength(); i++ ) {
                    Node attribute = attributes.item(i);
                    processAttribute(graphEdge, attribute);
                    logHelper.logDebug("Node attribute " + attribute.getNodeName()
                                       + ", value = " + attribute.getTextContent());
                }
            }
            LogHelper.logDebug( "adding node " + graphNode );
            graphNode.initializeAfterParsing();
            graphUnderConstruction.addNode(graphNode);
		}
        LogHelper.endIndent();

        graphUnderConstruction.getGraphState().setLocked(false);
        LogHelper.exitMethod( getClass(), "buildGraphFromInput:\n" + g );
        return graphUnderConstruction;
    } // buildGraphFromInput
	
    /**
     * @todo the exception handling here needs to go at least one level up
     */
	public Graph generateGraph(String xml) {
        LogHelper.enterMethod( getClass(), "generateGraph( String )" );
		DocumentBuilderFactory dbf = null;
		DocumentBuilder db = null;
        Graph newGraph = null;

        try {
            dbf = DocumentBuilderFactory.newInstance();
            db = getDocumentBuilder(dbf);
            setDocument(db, xml);
            newGraph = buildGraphFromInput(db);
        }
//         catch ( GalantException exception ) {
//             exception.report( exception.getMessage()
//                               + "\n called from generateGraph(String xml)" );
//             exception.display();
//         }
        catch ( Exception exception ) {
            if ( exception instanceof GalantException ) {
                GalantException ge = (GalantException) exception;
                ge.report( "" );
                ge.display();
            }
            else exception.printStackTrace( System.out );
        }

        LogHelper.exitMethod( getClass(), "generateGraph( String )" );
		return newGraph;
	}
	
	public Graph generateGraph(File file) {
        LogHelper.enterMethod( getClass(), "generateGraph( File )" );
		DocumentBuilderFactory dbf = null;
		DocumentBuilder db = null;
        Graph newGraph = null;

        try {
            dbf = DocumentBuilderFactory.newInstance();
            db = getDocumentBuilder(dbf);
            setDocument(db, file);
            newGraph = buildGraphFromInput(db);
        }
//         catch ( GalantException exception ) {
//             exception.report( exception.getMessage()
//                               + "\n called from generateGraph(File)" );
//             exception.display();
//         }
        catch ( Exception exception ) {
            GalantException topLevelException
                = new GalantException( exception.getMessage()
                                       + "\n - in generateGraph(File)" );
            topLevelException.report( "" );
            topLevelException.display();
        }

        LogHelper.exitMethod( getClass(), "generateGraph( File )" );
        return newGraph;
	}
	
// 	public void addNode(Node n) {
// 		//TODO add node to front of Nodes, return complete graph string
// 		graph.addNode(n, 0);
// 	}
	
// 	public void addEdge(Edge e) {
// 		//TODO add edge to front of Edges, return complete graph string
// 		graph.addEdge(e, 0);
// 	}
	
	public NodeList getGraphNode() {
		return this.document.getElementsByTagName("graph");
	}
	
	public NodeList getNodes() {
		return this.document.getElementsByTagName("node");
	}
	
	public NodeList getEdges() {
		return this.document.getElementsByTagName("edge");
	}
	
	public Graph getGraph() {
		return this.graph;
	}
	
}

//  [Last modified: 2015 07 26 at 21:05:54 GMT]
