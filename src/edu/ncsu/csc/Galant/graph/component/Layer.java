/**
 * Keeps track of position and display information for nodes on a single
 * layer. The <em>logical</em> order of nodes on a layer is determined by the
 * ArrayList <code>nodes</code>, while the display order is determined by
 * setting the positionInLayer attribute of a node. These two are brought in
 * sync by displayPositions.
 */

package edu.ncsu.csc.Galant.graph.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.ncsu.csc.Galant.algorithm.Terminate;
import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.logging.LogHelper;

class Layer extends GraphElement {
    LayeredGraph graph;
    ArrayList<Node> nodes;

    public Layer(LayeredGraph graph) {
        super(graph);
        this.graph = graph;
        nodes = new ArrayList<Node>();
    }

    /**
     * adds new positions numbered layer.nodes.size(), ... , position to the
     * nodes list of the given layer; the new positions are filled with null
     * nodes; can be called even if not needed - it does nothing, safely, in
     * that case
     */
    void ensurePosition(int position) {
        for ( int i = nodes.size(); i <= position; i++ ) {
            nodes.add(null);
        }
    }

    /**
     * puts node v into the i-th position
     */
    public void addNode( Node v, int i) {
        ensurePosition(i);
        nodes.set(i, v);
    }

    /**
     * @return the node at the given position on this layer
     */
    public Node getNodeAt(int position) {
        return nodes.get(position);
    }

    /**
     * Displays the marked/unmarked state of all nodes on this layer
     */
    public void displayMarks() throws Terminate {
        for ( Node v: nodes ) {
            /** @todo logical marks not yet implemented */
            //            v.setVisited( graph.isMarked( v ) );
        }
    }

    /**
     * Removes displayed marks from all nodes on this layer without affecting
     * their logical status.
     */
    public void removeMarks() throws Terminate {
        for ( Node v: nodes ) {
            v.setVisited( false );
        }
    }

    /**
     * logically unmarks all nodes on this layer
     */
    public void clearMarks() {
        for ( Node v: nodes ) {
            /** @todo logical marks not yet implemented */
            //            graph.unMark( v );
        }
    }

    /**
     * Sets node labels to be blank
     */
    public void clearLabels() throws Terminate {
        for ( Node v: nodes ) {
            v.setLabel("");
        }
    }

    /**
     * Highlights nodes on this layer and, if appropriate, incident edges --
     * see "enum Scope"
     *
     * @todo will need to be fixed; probably via a class Channel
     */
//     public void highlight( LayeredGraph.Scope scope ) throws Terminate {
//         for ( Node v: nodes ) {
//             v.setSelected( true );
//             if ( scope == LayeredGraph.Scope.UP
//                  || scope == LayeredGraph.Scope.BOTH ) {
//                 for ( Edge e: v.getOutgoingEdges() ) {
//                     e.setSelected( true );
//                 }
//             }
//             if ( scope == LayeredGraph.Scope.DOWN
//                  || scope == LayeredGraph.Scope.BOTH ) {
//                 for ( Edge e: v.getIncomingEdges() ) {
//                     e.setSelected( true );
//                 }
//             }
//         }
//     }

    /**
     * Highlights the nodes between the two given positions, inclusive (used
     * to highlight an insertion)
     */
    public void highlightNodes( int positionOne, int positionTwo ) throws Terminate {
        for ( int i = positionOne; i <= positionTwo; i++ ) {
            nodes.get(i).setSelected( true );
        }
    }

    /**
     * undoes highlighting for the nodes and any edges incident on this layer
     */
    public void unHighlight() throws Terminate {
        for ( Node v: nodes ) {
            v.setSelected( false );
            for ( Edge e: v.getIncidentEdges() ) {
                e.setSelected( false );
            }
        }
    }


    /**
     * Displays logical weights assigned to the nodes
     */
    public void displayWeights() throws Terminate {
        for ( Node v: nodes ) {
            /** @todo logical weights not yet implemented */
            //            v.setWeight( graph.getWeight( v ) );
        }
    }

    /**
     * Gives node weights a default value that makes them invisible
     */
    public void clearWeights() throws Terminate {
        for ( Node v: nodes ) {
            v.clearWeight();
        }
    }

    /**
     * @return the list of nodes on this layer
     */
    public List<Node> getNodes() {
        return nodes;
    }

    /**
     * inserts the node currently at position originalPosition so
     * node currently at newPosition, shifting the intervening nodes to the
     * right
     */
    public void insert( int originalPosition, int newPosition ) {
        Node toBeInserted = nodes.remove( originalPosition );
        nodes.add( newPosition, toBeInserted );
        updatePositions();
    }

    /**
     * sorts the nodes by their weight (as assigned by Galant code)
     */
    public void sort() {
        System.out.println("-> sort: " + nodes);
        Collections.sort(nodes);
        updatePositions();
        System.out.println("<- sort: " + nodes);
    }

    /**
     * sorts node by positions in their layers (as assigned by Galant)
     */
    final Comparator<Node> POSITION_COMPARATOR = new Comparator<Node>() {
        public int compare(Node x, Node y) {
            int state = GraphDispatch.getInstance().getDisplayState();
            return x.getPositionInLayer(state) - y.getPositionInLayer(state);
        }
    };

    public void sortByPosition() {
        Collections.sort( nodes, POSITION_COMPARATOR );
        updatePositions();
    }

    /**
     * sorts nodes by their logical weight (for use with 'fast' versions of
     * barycenter-related algorithms)
     */
    final Comparator<Node> WEIGHT_COMPARATOR = new Comparator<Node>() {
        public int compare( Node x, Node y ) {
            /** @todo logical weights not yet implemented */
//             double wx = graph.getWeight( x );
//             double wy = graph.getWeight( y );
//             if ( wx > wy ) return 1;
//             if ( wx < wy ) return -1;
            return 0;
        }
    };

    public void sortByWeight() {
        Collections.sort( nodes, WEIGHT_COMPARATOR );
        updatePositions();
    }

    /**
     * sorts the nodes on this layer by increasing degree
     */
    public void sortByIncreasingDegree() {
        /** @todo sorting not implemented */
        //        LayeredGraph.sortByIncreasingDegree( nodes );
        updatePositions();
    }

    /**
     * Puts nodes with largest (smallest) degree in the middle and puts
     * subsequent nodes farther toward the outside.
     *
     * @param largestInMiddle if true then the node with largest degree goes
     * in the middle.
     */
    public void middleDegreeSort( boolean largestMiddle ) {
        /** @todo sorting not implemented */
//        LayeredGraph.sortByIncreasingDegree( nodes );
       if ( largestMiddle ) Collections.reverse( nodes );
       ArrayList<Node> tempNodeList = new ArrayList<Node>();
       boolean addToFront = true;
       for ( Node node : nodes ) {
           if ( addToFront ) {
               tempNodeList.add( 0, node );
           }
           else {
               tempNodeList.add( tempNodeList.size(), node );
           }
           addToFront = ! addToFront;
       }
       nodes = tempNodeList;
       updatePositions();
    }

    /**
     * Uses the order in the list 'nodes' to update the positions of the
     * nodes in the graph.
     */
    public void updatePositions() {
        int i = 0;
        for ( Node v: nodes ) {
            // sets the position information in the layered graph
            /** @todo logical positions not yet implemented */
//             graph.setPosition( v, i );
            i++;
        }
    }

    /**
     * Updates the display based on the order of the nodes on this layer;
     * assumes the only y-coordinate changes occurred for nodes whose
     * positions changed: see previewPositionChanges()
     */
    public void displayPositions() throws Terminate {
        int i = 0;
        for ( Node v: nodes ) {
            if ( v.getPositionInLayer() != i ) {
                v.setPositionInLayer( i );
            }
            i++;
        }
    }

    public void markPositionChanges() {
        int i = 0;
        for ( Node v: nodes ) {
            if ( v.getPositionInLayer() != i ) {
                /** @todo logical marks not implemented */
                //                graph.mark( v );
            }
            i++;
        }
    }

    /**
     * Saves positions of all the nodes
     */
    public void savePositions() {
        for ( Node v: nodes ) {
            /** @todo logical positions not implemented */
            //            graph.setSavedPosition( v, graph.getPosition( v ) );
        }
    }

    /**
     * Restores saved positions of all the nodes
     */
    public void restoreSavedPositions() {
        for (Node v: nodes ) {
            /** @todo logical positions not implemented */
            // graph.setPosition( v, graph.getSavedPosition( v ) );
        }
    }

    /**
     * Updates the display based on previously saved positions
     */
    public void displaySavedPositions() throws Terminate {
        for ( Node v: nodes ) {
            /** @todo logical positions not implemented */
//             int position = graph.getSavedPosition( v );
//             if ( v.getPositionInLayer() != position ) {
//                 v.setPositionInLayer( position );
//             }
        }
    }

    /**
     * Puts node v at position i on the display (does nothing to its logical
     * position)
     */
    public void displayPosition( Node v, int i ) throws Terminate {
        if ( v.getPositionInLayer() != i ) {
            v.setPositionInLayer( i );
        }
    }

    public String toString() {
        String s = "";
        s += "[";
        for ( Node node : nodes ) {
            s += " " + node.getId() + " " +
                "(" + node.getLayer() + "," + node.getPositionInLayer() + "),";
        }
        s += " ]";
        return s;
    }

} // end, class Layer

//  [Last modified: 2016 06 16 at 15:13:58 GMT]
