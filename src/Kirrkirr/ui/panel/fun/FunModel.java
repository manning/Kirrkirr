package Kirrkirr.ui.panel.fun;

import Kirrkirr.ui.panel.GraphPanel;
import Kirrkirr.Kirrkirr;
import Kirrkirr.util.Dbg;
import Kirrkirr.util.Helper;

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
// import com.sun.java.accessibility.*;
import javax.swing.event.*;
//import java.io.Serializable;

/** FunModel.java - Model for network visualization panel.
 *
 * @author Conrad Wai 2002
 */
public class FunModel {

    private WordNodeModel selectedNodeModel;

    private Vector nodeModels;
    private Vector edgeModels;
    private Vector sproutedNodes;

    // not static: see accessor methods note
    private Color selectedNodeColor;
    private Color defaultNodeColor;
    private Color notFoundNodeColor;

    // this is really both the parent view and model
    private GraphPanel graph;

    // for FunModelListeners
    // transient means shouldn't be saved during serialization...
    transient private Vector funListeners;

    // create w/ null graph (hopefully to be set soon by a/our view...)
    public FunModel() {
        this(null);
    }

    public FunModel(GraphPanel graph) {

        selectedNodeModel = null;

        this.graph = graph;

        // temp

        // get from graphpanel
        selectedNodeColor = graph.getSelectedNodeColor();
        defaultNodeColor = graph.getDefaultNodeColor();
        notFoundNodeColor = graph.getNotFoundNodeColor();
    }

    // graph is really both the parent view and model
    public GraphPanel getGraph() { return (graph); }
    public void setGraph(GraphPanel graph) {
        this.graph = graph;
    }


    // isFocus => sproutLinks and setAsSelected (change??)
    public WordNodeModel createNode(String uniqueKey, boolean isFocus,
                                    WordNodeModel hub) {
        // we don't have panel size in model, so just make x, y random
        // within a (hopefully) safe range
        // then should move / translate (on another thread?) to the best
        // spot afterward!!
        int startX, startY;

        if (hub != null) {
            // start somewhere near hub
            startX = (int) (Math.random() * 100) - 50;
            startY = (int) (Math.random() * 100) - 50;
            if (startX < 0) {
                startX = 0;
            } else if (startX >= graph.getTopFFFPanel().getWidth()) {
                startX = graph.getTopFFFPanel().getWidth();
            }
            if (startY < 0) {
                startY = 0;
            } else if (startY >= graph.getTopFFFPanel().getHeight()) {
                startY = graph.getTopFFFPanel().getHeight();
            }
        } else {
              // default anywhere start loc
            startX = (int) (Math.random()*graph.getTopFFFPanel().getWidth());
            startY = (int) (Math.random()*graph.getTopFFFPanel().getHeight());
        }

        //try to sprout away from other nodes to avoid clutter
        if (hub != null) {
            WordNodeModel secondHub = hub.getHub();
            if (secondHub != null) {
                Point[] midpoints = LinkEdgeModel.computeEndpoints(hub, secondHub);
                startX = (int) (2 * midpoints[0].x - midpoints[1].x +
                                Math.random() * 40 - 20);

                startY = (int) (2 * midpoints[0].y - midpoints[1].y +
                                Math.random() * 40 - 20);
                if (startX < 0) {
                    startX = 0;
                } else if (startX >= graph.getTopFFFPanel().getWidth()) {
                    startX = graph.getTopFFFPanel().getWidth();
                }
                if (startY < 0) {
                    startY = 0;
                } else if (startY >= graph.getTopFFFPanel().getHeight()) {
                    startY = graph.getTopFFFPanel().getHeight();
                }
            }
        }

        WordNodeModel model = new WordNodeModel(uniqueKey, startX, startY, this, isFocus);
        model.setHub(hub);
        // WordNodeModel's ctor calls nodeModelCreated() below, which does
        // some more stuff...

        return (model);
    }


    public void nodeModelCreated(WordNodeModel created, boolean isFocus) {
        if (nodeModels == null)
            nodeModels = new Vector();
        if (findNodeModel(created) == null)  // should we perform this chk.??
            nodeModels.addElement(created);

        // fire this *before* potential sprout, since want node to be
        // onscreen and (more importantly) to be the selected node before
        // we sprout
        fireNodeModelCreated(created, isFocus);

        if (isFocus)
            created.sproutLinks();
    }


    public void sprout(WordNodeModel wModel) {
        if (sproutedNodes == null)
            sproutedNodes = new Vector();
        sproutedNodes.addElement(wModel);
    }


    public boolean isSprouted(WordNodeModel wModel) {
        if(sproutedNodes == null) return false;
        return sproutedNodes.contains(wModel);
    }

    public LinkEdgeModel createEdge(WordNodeModel word1, WordNodeModel word2) {
        LinkEdgeModel model = new LinkEdgeModel(word1, word2, this);

        // edge model ctor calls edgeModelCreated, which makes below redundant
//      if (edgeModels == null)
//          edgeModels = new Vector();
//      edgeModels.addElement(model);

//      fireEdgeModelCreated(model);

        return (model);
    }

    public void edgeModelCreated(LinkEdgeModel created) {
        if (edgeModels == null)
            edgeModels = new Vector();
        edgeModels.addElement(created);

        fireEdgeModelCreated(created);
    }


    // obtainNode and obtainEdge try to find the model, and if it can't
    // creates it, so that we are guaranteed to return the model (and not
    // null).

    public WordNodeModel obtainNode(String uniqueKey, boolean isFocus,
                                    WordNodeModel hub) {
        WordNodeModel nodeModel = findNodeModel(uniqueKey);
        if (nodeModel == null) {
            nodeModel = createNode(uniqueKey, isFocus, hub);
        }
        return (nodeModel);
    }

    public LinkEdgeModel obtainEdge(WordNodeModel word1, WordNodeModel word2) {
        LinkEdgeModel edgeModel = findEdgeModel(word1, word2);
        if (edgeModel == null) {
            edgeModel = createEdge(word1, word2);
        }
        return (edgeModel);
    }


    // return null if not found
    public WordNodeModel findNodeModel(WordNodeModel nodeModel) {
        return (findNodeModel(nodeModel.getUniqueKey()));
    }

    public WordNodeModel findNodeModel(String uniqueKey) {
        if (nodeModels == null)
            return (null);
        int numNodes = nodeModels.size();
        for (int i = 0; i < numNodes; i++) {
            WordNodeModel nodeModel = (WordNodeModel)nodeModels.elementAt(i);
            if (nodeModel.getUniqueKey().equals(uniqueKey))
                return (nodeModel);
        }
        return (null);
    }


    // returns null if not found
    public LinkEdgeModel findEdgeModel(WordNodeModel nodeModel1,
                                       WordNodeModel nodeModel2) {
        return (findEdgeModel(nodeModel1.getUniqueKey(), nodeModel2.getUniqueKey()));
    }


    public LinkEdgeModel findEdgeModel(String uKey1, String uKey2) {
        if (edgeModels == null)
            return (null);
        int numEdges = edgeModels.size();
        for (int i = 0; i < numEdges; i++) {
            LinkEdgeModel edgeModel = (LinkEdgeModel)edgeModels.elementAt(i);

            WordNodeModel nodeModel1 = edgeModel.getNodeModel1();
            WordNodeModel nodeModel2 = edgeModel.getNodeModel2();
            String modelKey1 = nodeModel1.getUniqueKey();
            String modelKey2 = nodeModel2.getUniqueKey();
            if ((modelKey1.equals(uKey1) && modelKey2.equals(uKey2)) ||
                (modelKey1.equals(uKey2) && modelKey2.equals(uKey1)))
                return (edgeModel);
        }
        return (null);
    }

    // removeWord does the "fancy" stuff - determining whether linked
    // words should be deleted, etc.  removeNode does the actual removing
    // and firing...

    public void removeSelectedWord() {
        removeWord(getSelected());
    }
    public synchronized void removeWord(WordNodeModel nodeModel) {
        if (nodeModel.equals(getSelected()))
            setSelected(null);

        if (edgeModels != null) {
            int numEdges = edgeModels.size();
            for (int i = 0; i < numEdges; i++) {
                LinkEdgeModel edgeModel = (LinkEdgeModel)edgeModels.elementAt(i);

                WordNodeModel nodeModel1 = edgeModel.getNodeModel1();
                WordNodeModel nodeModel2 = edgeModel.getNodeModel2();
                WordNodeModel otherNodeModel = null;

                if (nodeModel1.equals(nodeModel))
                    otherNodeModel = nodeModel2;
                else if (nodeModel2.equals(nodeModel))
                    otherNodeModel = nodeModel1;
                // else otherNodeModel remains null

                if (otherNodeModel != null) {  // found link to nodeModel
                    boolean linkRemoved = removeLink(i, nodeModel, otherNodeModel, true);
                    // b/c of the Java 1.1.8-compatible way we're looping
                    // through this (no Iterator w/ concurrent remove), we
                    // have to do this "manually"
                    if (linkRemoved) {
                        i--;
                        numEdges--;
                    }
                }
            }
        }

        // now nodeModel should have no expanded links left.  remove
        // nodeModel (and fire so node removed)
        removeNode(nodeModel);
    }

    // remove from collection and fire notification
    // cf. removeWord()
    public void removeNode(WordNodeModel nodeModel) {
        nodeModels.removeElement(nodeModel);
        fireNodeModelRemoved(nodeModel);
    }

    public synchronized boolean removeLink(LinkEdgeModel edgeModel,
                                           WordNodeModel nodeModel,
                                           WordNodeModel otherNodeModel,
                                           boolean removeEdgeIfNotLeaf) {
        return (removeLink(edgeModels.indexOf(edgeModel), nodeModel,
                           otherNodeModel, removeEdgeIfNotLeaf));
    }
    // removeEdgeIfNotLeaf - true when Remove, false when Collapse...
    public synchronized boolean removeLink(int edgeNum, WordNodeModel
                                           nodeModel, WordNodeModel
                                           otherNodeModel, boolean
                                           removeEdgeIfNotLeaf) {
        if (edgeNum < 0)
            return (false);
        if ((!removeEdgeIfNotLeaf) && (otherNodeModel.getNumExpandedLinks()>1))
            return (false);
        LinkEdgeModel edgeModel = (LinkEdgeModel)edgeModels.elementAt(edgeNum);

        // remove edgeModel (and fire so edge removed)
        // (don't forget to update otherNodeModel, too) - take
        // care of nodes (expanded list, etc.)...
        edgeModels.removeElementAt(edgeNum);

        nodeModel.removeExpandedWordFromList(otherNodeModel.getUniqueKey());
        otherNodeModel.removeExpandedWordFromList(nodeModel.getUniqueKey());
        fireEdgeModelRemoved(edgeModel);

        // if otherNodeModel has no other links, remove it as well
        // (and fire to remove associated node)
        if (otherNodeModel.getNumExpandedLinks() == 0) {
            removeNode(otherNodeModel);
        }
        return (true);
    }

    // shouldn't need to use these too often, with the other methods
    // available...
    // may still be null...
    public Vector getNodeModels() { return (nodeModels); }
    public Vector getEdgeModels() { return (edgeModels); }

    // when a new word is selected from an external source (i.e., not from
    // within this model/panel - e.g., from word list), determine whether
    // this new word "belongs here."  true if we're empty, or if the new
    // word is, or links to, one of our words; false otherwise.
    //
    // maybe should return node model, or null if doesn't belong??
    public boolean wordBelongsHere(String uniqueKey) {
        if (nodeModels == null || nodeModels.size() == 0)
            return (true);

        // see if "one of ours"
        WordNodeModel nodeModel = findNodeModel(uniqueKey);
        if (nodeModel != null)
            return (true);

        // see if links to one of ours...
        int numNodes = nodeModels.size();
        for (int i = 0; i < numNodes; i++) {
            WordNodeModel currModel = (WordNodeModel)nodeModels.elementAt(i);
            if (currModel.isLinkedTo(uniqueKey))
                return (true);
        }
        return (false);
    }

    public WordNodeModel getSelected() { return (selectedNodeModel); }

    public void setSelected(WordNodeModel newSelected) {
        if (selectedNodeModel == newSelected)
            return;

        if (selectedNodeModel != null) {

            // want all nodes to appear default color, distinguish words
            // that aren't in dict by text color

            //      if (selectedNodeModel.isInDict())
            selectedNodeModel.updateColor();
                //          else
                //              selectedNodeModel.setColor(notFoundNodeColor);
        }

        if (newSelected != null) {
            if (Dbg.NEWFUN) Dbg.print("Selected:" + newSelected);

            newSelected.updateColor();

            // handle sending to other panels in graph
            // COMMENT OUT! selection notification can't be mix of
            // top-down and bottom up.

            //   graph.selectedNodeChanged(newSelected.getUniqueKey());
        }

        selectedNodeModel = newSelected;


//      fireSelectedNodeChanged();
    }

    public WordNodeModel getSelectedNodeModel() {
        return selectedNodeModel;
    }

    // getSelected() and stuff not static, change color stuff to
    // non-static and have GraphPanel help us out with this color
    // stuff (when color changed, have graph run through and change each
    // fun panel/model)...
    public Color getSelectedNodeColor() { return (selectedNodeColor); }

    public void setSelectedNodeColor(Color newColor) {
        if (!newColor.equals(selectedNodeColor)) {
            selectedNodeColor = newColor;
            getSelected().updateColor();
        }
    }

    public Color getDefaultNodeColor() { return (defaultNodeColor); }

    public void setDefaultNodeColor(Color newColor) {
        if ( ! newColor.equals(defaultNodeColor)) {
            defaultNodeColor = newColor;  // update ivar
            // run through all node (models) of old default color and
            // repaint
            int numNodes = nodeModels.size();
            for (int i = 0; i < numNodes; i++) {
                WordNodeModel currModel = (WordNodeModel)nodeModels.elementAt(i);
                currModel.updateColor();
            }
        }
    }


    // for FunModelListener

    public void addFunModelListener(FunModelListener listener) {
        // null allowed for serialization...
        if (funListeners == null) {  // lazy eval. (make sure to chk. for
                                  // null in other methods)...
            funListeners = new Vector();
        }

        funListeners.addElement(listener);
    }

    public void removeFunModelListener(FunModelListener listener) {
        if (funListeners == null) return;

        int index = funListeners.indexOf(listener);

        if (index != -1)
            funListeners.removeElementAt(index);
    }

    // rewrite (consolidate)...??
    private void fireNodeModelCreated(WordNodeModel nodeModel, boolean isFocus) {
        if (funListeners == null) return;

        int numListeners = funListeners.size();
        for (int i = 0; i < numListeners; i++) {
            FunModelListener listener = (FunModelListener)funListeners.elementAt(i);
            listener.funNodeModelCreated(this, nodeModel, isFocus);
        }
    }

    private void fireEdgeModelCreated(LinkEdgeModel edgeModel) {
        if (funListeners == null) return;

        int numListeners = funListeners.size();
        for (int i = 0; i < numListeners; i++) {
            FunModelListener listener = (FunModelListener)funListeners.elementAt(i);
            listener.funEdgeModelCreated(this, edgeModel);
        }
    }

    private void fireNodeModelRemoved(WordNodeModel nodeModel) {
        if (funListeners == null) return;

        int numListeners = funListeners.size();
        for (int i = 0; i < numListeners; i++) {
            FunModelListener listener = (FunModelListener)funListeners.elementAt(i);
            listener.funNodeModelRemoved(this, nodeModel);
        }
    }

    private void fireEdgeModelRemoved(LinkEdgeModel edgeModel) {
        if (funListeners == null) return;

        int numListeners = funListeners.size();
        for (int i = 0; i < numListeners; i++) {
            FunModelListener listener = (FunModelListener)funListeners.elementAt(i);
            listener.funEdgeModelRemoved(this, edgeModel);
        }
    }

    // (will be) helper...??
    private void fireChanged() {
        if (funListeners == null) return;

        int numListeners = funListeners.size();
        for (int i = 0; i < numListeners; i++) {
            FunModelListener listener = (FunModelListener)funListeners.elementAt(i);
            listener.funModelChanged(this);
        }
    }

    public void shiftNodesOnResize(FFFPanel parent) {
        int i;
        for(i = 0; i < nodeModels.size(); i++) {
            WordNodeModel wModel = (WordNodeModel) nodeModels.elementAt(i);
            wModel.translate(0, 0, parent);
        }

    }

}

