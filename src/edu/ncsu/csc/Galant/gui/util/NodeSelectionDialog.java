/**
 * Selects a node in response to a user's input during algorithm
 * execution. The superclass NodeSpecificationDialog is responsible for
 * interacting with the user and ensuring that a valid node is selected.
 */
package edu.ncsu.csc.Galant.gui.util;

import java.awt.Frame;

import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.GalantException;
import edu.ncsu.csc.Galant.algorithm.Terminate;
import edu.ncsu.csc.Galant.gui.util.NodeSpecificationDialog;
import edu.ncsu.csc.Galant.gui.window.GraphWindow;
import edu.ncsu.csc.Galant.graph.component.Node;
import edu.ncsu.csc.Galant.graph.component.Graph;
import edu.ncsu.csc.Galant.graph.datastructure.NodeSet;

public class NodeSelectionDialog extends NodeSpecificationDialog {

    /** set from which the node is to be selected, null if there is no
     * restriction */
    private NodeSet restrictedSet = null;
    /**
     * error message if selected node does not belong to restricted set
     */
    private String errorMessage = "";

    public NodeSelectionDialog(String prompt) {
        super(GraphWindow.getGraphFrame(), prompt);
        initDialog();
    }

    public NodeSelectionDialog(String prompt, NodeSet restrictedSet, String errorMessage) {
        super(GraphWindow.getGraphFrame(), prompt);
        this.restrictedSet = restrictedSet;
        this.errorMessage = errorMessage;
        initDialog();
    }

    private void initDialog() {
        GraphDispatch dispatch = GraphDispatch.getInstance();
        dispatch.getWorkingGraph().setSelectedNode(null);
        dispatch.setActiveQuery(this);
    }

    protected void performAction(Node node)
        throws Terminate, GalantException {
        GraphDispatch dispatch = GraphDispatch.getInstance();
        Graph graph = dispatch.getWorkingGraph();
        if ( restrictedSet != null
             && ! restrictedSet.contains(node) ) {
            throw new GalantException(errorMessage);
        }
        graph.setSelectedNode(node);
        dispatch.setActiveQuery(null);
    }
}

//  [Last modified: 2017 01 14 at 22:37:59 GMT]
