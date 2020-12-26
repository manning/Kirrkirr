package Kirrkirr.ui.panel;

import Kirrkirr.Kirrkirr;
import Kirrkirr.dictionary.DictFields;
import Kirrkirr.dictionary.DictField;
import Kirrkirr.dictionary.DictionaryInfo;
import Kirrkirr.util.Dbg;
import Kirrkirr.util.Helper;
import Kirrkirr.util.RelFile;
import Kirrkirr.util.FontProvider;

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;


/** FunPanel.java - Implements the graphical network display window.
 *  Originally based on the Graph.java demo from the JDK 1.1.5 examples
 *  (c) kjansz 980913
 *
 *  Modification history
 *  1999/04/02 chris manning: small GUI improvements,
 *                        put more static and final modifiers in code!
 *  (Madhu:'00 put additional methods used by the QuizMaster class)
 *
 *  <p>
 *  vj:2002: remove dependence on DictField (replaced with DictionaryInfo)
 */
public final class OldFunPanel extends JPanel implements Runnable /*, Serializable */ {

    //string constants that need to be translated
    private static final String SC_FIND_IN_LIST="Find_in_word_list";
    private static final String SC_SPROUT="Sprout";
    private static final String SC_COLLAPSE="Collapse";
    private static final String SC_DELETE="Delete";
    private static final String SC_SEE_DEFINITION="Keep_definition";
    private static final String SC_ANCHOR_DOWN="Anchor_down";
    private static final String SC_RELEASE="Release";

    //static constants
    private static final int MAX_NODES = 100;
    private static final int MAX_EDGES = 200;

    private static final boolean SAND = true;  // used to stop small movements?
    private static final double SAND_FACTOR = 0.005;

    private static final int BORDERX = 40;
    private static final int BORDERY = 15;
    // private static final double CENTRIPETALCONST = 2.0;

    public static final Color defaultSelectColor = Color.yellow;
    public static final Color defaultFocusColor = new Color(255,255,145); // light yellow
    public static final Color defaultNodeColor = new Color(149,253,124);//light green
    private static final long serialVersionUID = -2297046782660929624L;

    public static Color selectColor = defaultSelectColor;
    public static Color focusColor = defaultFocusColor;
    public static Color nodeColor = defaultNodeColor;
    private static final Color stressColor = Color.darkGray;
    // private static final Color edgeColor = Color.black;

    private static final int SPROUT = 1, FIND = 2;


    private final Font funfont;
    private final Font superscript;
    private final Font legendfont;

    //static constants for legend
    private static final int w = 200;
    private static final int h = 250;
    private static final int hspace = 4;
    private static final int vspace = 2;
    private static final int boxX = 5;
    private static final int boxW = 50;
    private static final int textX = boxX + boxW + hspace;

    //FunPanel variables
    private final OldGraphPanel graph;

    private int numNodes; // number of nodes on graph in nodes[0, ..., numNodes-1]
    private WordNode[] nodes = new WordNode[MAX_NODES]; //accessed by WordNode, Kirrkirr, GraphPanel

    private int numEdges;
    private FunPanelEdge[] edges = new FunPanelEdge[MAX_EDGES];

    public int Q_SIZE = 5; //accessed by GraphPanel
    private Vector<String> focusNodes = new Vector<String>(Q_SIZE+10);   // list of unique keys
    public Hashtable hideEdge = new Hashtable(5); //accessed by GraphPanel

    private transient volatile Thread relaxer=null;

    // these 5 can't be private: manipulated in GraphPanel
    public boolean stress;       // write spring stresses next to link lines?
    public boolean random;
    public boolean legend;       // show legend?
    public boolean stop;         // stop movement of nodes?
    public boolean gloss;      // show gloss?

    private boolean pausePaint = false;

    /** This is the word that is the current selected (bright yellow) one */
    private WordNode pick;

    //spring algorithm constants -- can't be private. Set in GraphPanel
    public double K2 = 300.0;             //repulsion between any u and v
    public double K3 = 150.0;             //repulsion between u e focusNodes and v
    public double Luv = 80.0;             //zero energy length of a spring
    public double C = 1/5.0;
    public int EW = 3;                    //default edge thickness ( 2*EW) Global, so that dialog can change


    //Swing stuff
    private JPopupMenu popup;
    private JMenuItem find;
    private JMenuItem sprout;
    private JMenuItem delete;
    private JMenuItem collapse;
    private JMenuItem pin;
    private JMenuItem release;
    private JMenuItem openGloss;

    private Image offscreen;
    private Image legendImage;
    private Dimension offscreensize;
    private Graphics offgraphics;

    private FontMetrics funfm; // = null;

    // this size is important to minimum screen size (CDM; Nov 2001)
    private static final Dimension minimumSize = new Dimension(350, 100);


    OldFunPanel(OldGraphPanel graph, boolean small) {
        this.graph = graph;
        FunPanelMouseListener fpml = new FunPanelMouseListener();
        addMouseListener(fpml);
        addMouseMotionListener(fpml);
        // setName("funCanvas"); //cdm: a no-op
        setBackground(Color.white);
        setForeground(Color.black);
        // setMinimumSize(new Dimension(300, 200));
        if (small) {
            funfont = FontProvider.LARGE_WORD_FONT;
            superscript = FontProvider.SMALL_WORD_FONT;
            legendfont = FontProvider.SMALL_TEXT_FONT;
        } else {
            funfont = FontProvider.HUGE_WORD_FONT;
            superscript = FontProvider.WORD_LIST_FONT;
            legendfont = FontProvider.TEXT_FONT;
        }
    }


    @Override
    public Dimension getMinimumSize() {
        return minimumSize;
    }

    public void saveState(ObjectOutputStream oos) throws IOException {
        /*
        stop();
        oos.writeInt(numNodes);
        for (int i=0 ; i < numNodes ; i++) {
            nodes[i].writeExternal(oos);
        }
        oos.writeInt(numEdges);
        for (int i=0 ; i < numEdges ; i++) {
            edges[i].writeExternal(oos);
        }
        oos.writeObject(focusNodes);
        start();
        */
    }

    /** overrides KirrkirrPanel
     *   @exception ClassNotFoundException
     *   @exception IOException
     */
    public void loadState(ObjectInputStream ois) throws
    IOException, ClassNotFoundException {
        /*        boolean temp_stop = stop;
        boolean temp_pause = pausePaint;
        stop = true;
        pausePaint = true;

        stop();
        numNodes = ois.readInt();
        for (int i=0 ; i < numNodes ; i++) {
            nodes[i] = new WordNode();
            nodes[i].readExternal(ois);
        }
        numEdges = ois.readInt();
        for (int i=0 ; i < numEdges ; i++) {
            edges[i] = new FunPanelEdge();
            edges[i].readExternal(ois);
        }
        focusNodes = (Vector) ois.readObject();

        stop = temp_stop;
        pausePaint = temp_pause;
        start();*/
    }

    /*
      Including this has problems, as then network area will be bigger than
      displayed area.  Just get rid of viewport stuff?  {cdm}
      private static final Dimension preferredSize = new Dimension(400, 400);

      public Dimension getPreferredSize() {
      return preferredSize;
      }
    */

    //---------------------------------
    // thread based methods
    // rewritten due to deprecation of thread.stop() in java 1.2

    public void start() {
        relaxer = new Thread(this);
        // Lower the priority of the network displays
        relaxer.setPriority((Thread.NORM_PRIORITY +
                             Thread.MIN_PRIORITY) / 2);
        try {
            relaxer.setDaemon(true);    // a Daemon thread -- stops on exit
        } catch (SecurityException se) {
        }
        relaxer.start();
    }

    public void stop() {
        if (relaxer!=null)
            relaxer.interrupt();
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                if (!stop) {
                    try {
                        spring();
                    } catch (NullPointerException e) { // nodes fiddled
                        if (Dbg.ERROR) {
                            Dbg.print("FunPanel: run NullPtr");
                            e.printStackTrace();
                        }
                    }
                    if (random && (Math.random() < 0.13)) {
                        WordNode n = nodes[(int)(Math.random() * numNodes)];
                        if (!n.fixed) {
                            n.x += 100*Math.random() - 50;
                            n.y += 100*Math.random() - 50;
                        }
                    }
                }
                repaint();  // even if stopped (in case changed)-- threadsafe
                Thread.sleep(150);
            }
        } catch (InterruptedException e) {
        } catch (Exception ee) {
            if (Dbg.ERROR) {
                Dbg.print("FunPanel:run() exception");
                ee.printStackTrace();
            }
        }
    }
    //----end thread methods

    //----begin FunPanel node/edge functions

    /** Create a new WordNode for this word.
     *  It's added to the array nodes[]
     *  If that array happens to be full, we delete a focus node, no
     *  matter what the number of focus nodes that the user wants....
     *  Chris: I've deleted the register stuff.  It is important that
     *  displaying sprouted nodes is quite quick, and it is slowed down
     *  a lot if each one has to be looked up in the DictionaryCache.
     *  @param word The word to add.  This is _always_ a uniqueKey
     *  @return The number of nodes in the <code>nodes</code> array
     */
    public int addNode(String word) {
        // if there are no free nodes
        while (numNodes >= MAX_NODES) {
            removeOldestFocusNode();
        }
        // DictEntry de=null;
        // if (setExact)
        //     de=Kirrkirr.cache.getIndexEntry(word);
        // boolean register=false;
        // if (de!=null && Kirrkirr.cache.hasRegister(de)){
        //     register=true;
        // }
        WordNode n = new WordNode();
        Dimension d = getSize();
        n.x = 10 + (d.width - 20)*Math.random();
        n.y = 10 + (d.height - 20)*Math.random();
        n.uniqueKey = word;
        // n.register=register;
        n.ptr = 1;
        nodes[numNodes++] = n;
        if (Dbg.NETWORK) {
            Dbg.print("Network addNode: added node " + Helper.uniqueKeyToPrintableString(word));
            Dbg.print("  focusNodes = " + focusNodes);
            StringBuilder sb = new StringBuilder("[");
            for (int k = 0; k < numNodes; k++) {
                sb.append(nodes[k].uniqueKey);
                if (k != numNodes - 1) {
                    sb.append(',');
                } else {
                    sb.append(']');
                }
            }
            Dbg.print("  nodes = " + sb);
        }
        return numNodes - 1;
    }

    private void removeOldestFocusNode() {
        String dele = focusNodes.firstElement();
        removeNode(dele);
    }


    /** This just adds the focus word to focusNodes list if not already
     *  there.  It doesn't do anything with links.   See the 2 argument
     *  version for that.
     *  @param uniqueKey Unique key of word
     */
    private synchronized boolean addFocusNode(String uniqueKey) {
        if (focusNodes.contains(uniqueKey)) {
            // this was already a focus node
            return false;
        } else {
            while (focusNodes.size() > Q_SIZE - 1) {
                removeOldestFocusNode();
            }
            focusNodes.addElement(uniqueKey);
            return true;
        }
    }


    /** Adds a new node.  Called from GraphPanel.
     *  @param uniqueKey The unique word key
     *  @param links The words that this word should sprout links to
     */
    public void addFocusNode(String uniqueKey, DictFields links)
    {
        // before there was a problem here that nodes could be in the graph
        // (as after a collapse, or just when stuff had been deleted)
        // and then you couldn't resprout it, because node was there.
        // so always check for links...
        if (Dbg.NETWORK) Dbg.print("addFocusNode(" + uniqueKey + ", links=" +
                links + ")");
        addFocusNode(uniqueKey);
        int size = links.size();
        if (size == 0) {
            findNode(uniqueKey);
        } else {
            double sepAngle = 2.0 * Math.PI / size;
            // keep nodes away from horizontal (worst for current algorithm)
            double currentAngle = sepAngle / 4;

            for (int i = 0; i < size; i++) {
                DictField df = links.get(i);
                if (Dbg.ERROR) {
                    if (df == null)
                        Dbg.print("null dictfield?? at "+i+" in "+links);
                }
                double dx = Math.cos(currentAngle) * 70.0;
                double dy = Math.sin(currentAngle) * 70.0;

                boolean isNewLink = addEdge(uniqueKey, df, dx, dy);
                if (isNewLink) {
                    //if the link wasn't a new one, then may as well try the same angle again
                    currentAngle += sepAngle;
                    if( currentAngle > 2*Math.PI ) {
                        currentAngle = 2*Math.PI;
                    }
                }
            }
        }
        if (Dbg.NETWORK) Dbg.print("addFocusNode(" + uniqueKey + ") completed.");
    }


    /** addEdge adds an edge and node if required to the "from" String.
     *  CDM: At present this method just 'prays' that there is space left
     *  in the edges array, but I've yet to see it exceeded empirically.
     */
    private boolean addEdge(String uniqueKey, DictField to,
                            double dx, double dy) {
        if (Dbg.NETWORK) {
            Dbg.print("Network: addEdge from " + Helper.uniqueKeyToPrintableString(uniqueKey) +
                      " to " + Helper.uniqueKeyToPrintableString(to.uniqueKey));
        }
        Dimension d = getSize();
        FunPanelEdge e = new FunPanelEdge();
        boolean isNewLink = false;
        int size = numNodes;

        e.from = findNode(uniqueKey);
        e.to = findNode(to.uniqueKey);
        nodes[e.from].addEdge(e);
        nodes[e.to].addEdge(e);

        if( e.to == size ) {
            //keep new node closer to parent
            nodes[e.to].x = nodes[e.from].x + dx;
            nodes[e.to].y = nodes[e.from].y + dy;
            if (nodes[e.to].x < 0) {
                nodes[e.to].x = 4;
            } else if (nodes[e.to].x > d.width) {
                nodes[e.to].x = d.width-4;
            }
            if (nodes[e.to].y < 0) {
                nodes[e.to].y = 4;
            } else if (nodes[e.to].y > d.height) {
                nodes[e.to].y = d.height-4;
            }
            isNewLink = true;
        }
        e.len = 70.0;                           //garbage value for the moment
        e.tag = to.tag;
        edges[numEdges++] = e;
        if (Dbg.NETWORK) Dbg.print("addEdge exiting");
        return isNewLink;
    }

    /** Remove all the nodes in the graphical display
     */
    public synchronized void clearFunPanel() {
        if (relaxer != null) {
            stop();
            for (int i = 0; i < numNodes; i++) {
                nodes[i] = null;
            }
            numNodes = 0;
            for (int i = 0; i < numEdges; i++) {
                edges[i] = null;
            }
            numEdges = 0;
            if (focusNodes.size() > 0) {
                focusNodes.removeAllElements();
            }
            start();
        }
    }

    private void collapseNode(String lbl) {
        collapseNode(lbl, null);
    }

    /** This function is quite the same as the previous one but a new parameter
     *   is required. Used for History.java.
     *          Lim Hong Lee                    21/8/00
     *
     *   @param lbl - the word to collapse
     *   @param backwardList - the backwardlist
     */
    public synchronized void collapseNode(String lbl, Vector backwardList) {
        boolean temp_stop = stop;
        boolean temp_pause = pausePaint;
        stop = true;
        pausePaint = true;

        Vector<WordNode> keep = new Vector<WordNode>(MAX_NODES);          //vector of kept nodes
        int target=numNodes;

        for (int i=0 ; i<numNodes ; i++) {
            if ( nodes[i].uniqueKey.equals(lbl) ) {
                target = i;
            }
            keep.addElement(nodes[i]);
        }

        if (target != numNodes) {
            // something was found

            // cdm: I think this was wrong.  Collapse should never delete node
            // boolean focus = focusNodes.contains(nodes[target].lbl);
            // removeNodeEdges(keep, target, focus);
            if (backwardList == null) {
                removeNodeEdges(keep, target, false);
            } else {
                removeNodeEdges(keep, target, false, backwardList, lbl);
            }
        }

        pausePaint = temp_pause;
        stop = temp_stop;
    } //collapseNode


    /** Returns the index of a node in the nodes array. If it is not there
     *  at present, it is added and the index is returned.
     */
    private int findNode(String uniqueKey) {
        for (int i = 0 ; i < numNodes ; i++) {
            if (nodes[i].uniqueKey.equals(uniqueKey)) {
                nodes[i].ptr++;
                //      Dbg.print("WordNode: "+word+" "+nodes[i].ptr);
                return i;
            }
        }
        return addNode(uniqueKey);
    }


    public int numLinks(String pword) {
        int num=-1;
        //      Dbg.print("*****************");
        for (int i = 0 ; i < numNodes ; i++) {
            //      Dbg.print("nodes["+i+"]= "+nodes[i].lbl);
            if (nodes[i].uniqueKey.equals(pword)) {
                //Dbg.print("WordNode: "+pword+" "+nodes[i].ptr+ " "+nodes[i].colourPtr
                //         +" "+nodes[i].edgestrings);
                num= nodes[i].edgeStrings .size();//nodes[i].ptr-nodes[i].colourPtr;
            }
        }
        return num;
    }

    /** For debugging
     */
    public void printNodes(){
        for (int i=0;i<numNodes;i++){
            Dbg.print(nodes[i].uniqueKey);
        }
        for (int i=0;i<focusNodes.size();i++){
            Dbg.print("* "+focusNodes.elementAt(i));
        }
    }

    /** For debugging.
     */
    public void printAllEdges(){
        for (int i=0;i<numEdges;i++){
            FunPanelEdge cur=edges[i];
            Dbg.print( nodes[cur.from].uniqueKey +"\t\t--> "+nodes[cur.to].uniqueKey);
        }
    }

    /** The problem here (I think) is that a removed node
     *  may be a neighbor of a focus node. In that case,
     *  we don't really want to remove the node (unless
     *  we are in the game). First make the node we want to
     *  remove a non-focus node. What we want is to retain
     *  all the neighbors of focus nodes, so that the full
     *  information for each focus node is shown, but to
     *  remove all non-focus nodes that aren't connected
     *  to focus nodes AND update all the neighbor counts.
     */
    private synchronized void removeNode(String lbl) {
        boolean temp_stop = stop;
        boolean temp_pause = pausePaint;
        stop = true;
        pausePaint = true;

        /*      //here's my attempt at an algorithm (the old one is below)
        //make it a non-focus node
        if (focusNodes.contains(lbl))
            focusNodes.remove(lbl);

        Vector dontkeep=new Vector();
        for (int i=0; i<numNodes; i++) {
            dontkeep.addElement(nodes[i]);
        }

        for (int i=0; i<numNodes; i++) {
            if (focusnodes.contains(nodes[i].lbl)){
                dontkeep.removeElement(nodes[i]);
                break;
            }
            for (Enumeration e=focusnodes.elements();e.hasMoreElements();){
                if (nodes[i].edgestrings.contains(e.nextElement())){
                    dontkeep.removeElement(nodes[i]);
                    break;
                }
            }
        }

        for (Enumeration e=dontkeep.elements();e.hasMoreElements();){

        }
*/

        if (Dbg.NETWORK) Dbg.print("removeNode: " + lbl + " from " + focusNodes);
        Vector<WordNode> keep = new Vector<WordNode>(MAX_NODES);          // vector of kept nodes
        int target = numNodes;

        for (int i = 0 ; i < numNodes; i++) {
            if ( nodes[i].uniqueKey.equals(lbl) ) {
                if (Dbg.NETWORK) {
                    Dbg.print("removeNode: label1 "+nodes[i].uniqueKey +
                        " equals label2 "+lbl);
                }
                target = i;
            } else {
                keep.addElement(nodes[i]);
                if (Dbg.NETWORK) Dbg.print("removeNode: no match for " + lbl + "; keeping:" + nodes[i]);
            }
        }

        if ( target != numNodes ) {
            // we found one....
            boolean focus = focusNodes.removeElement(nodes[target].uniqueKey);
            removeNodeEdges(keep, target, focus);
        } else {
            Dbg.print("ERROR!!!  THINGS WENT WRONG HERE.  Word not in nodes");
        }

        pausePaint = temp_pause;
        stop = temp_stop;
    } // end removeNode


    /** Cleans up (removes) the edges that are connected to just a node
     *  being removed or collapsed
     *   (ie their pointed-to count will now be zero)
     *   Called by "removeNode" and "collapseNode"
     *
     *   @param keep - is the vector of nodes being kept in the graph
     *   @param target - the index of the node
     *   @param focus - states wheter the node is a focus node
     */
    private synchronized void removeNodeEdges(Vector keep, int target,
                                                        boolean focus) {
        FunPanelEdge[] t_edges = new FunPanelEdge[numEdges];  //kept edges

        int j=0;
        //if(Dbg.NETWORK) Dbg.print("gets into removeNodeEdges(v,int,bool)");
        for(int i=0; i<numEdges ; i++) {
            //if(Dbg.NETWORK) Dbg.print("gets into for loop");
            //i is the counter in edges[], j is counter in t_edges[]

            //if current edge doesnt come from or go to target.
            if( (edges[i].to!=target)&&(edges[i].from!=target) ) {
                t_edges[j] = edges[i];
                t_edges[j].sFrom = nodes[edges[i].from].uniqueKey;
                t_edges[j].sTo = nodes[edges[i].to].uniqueKey;
                //Dbg.print("+edge added "+edges[i].from+" -> "+edges[i].to);
            }
            //
            else if (focus && ((edges[i].from == target) && (nodes[edges[i].to].ptr==1))) {
                // if the only node that has an edge to you was the deleted
                // focus node, then you're deleted too
                // pre focusNodes.contains(nodes[edges[i].from].lbl);
                //if(Dbg.KEVIN) Dbg.print("I'm entering the removeElement: " + nodes[edges[i].to]);
                keep.removeElement(nodes[edges[i].to]);
                removeEdge(edges[i], nodes[edges[i].to]);
                removeEdge(edges[i], nodes[edges[i].from]);
                //Dbg.print("-child node removed "+edges[i].from+" -> "+edges[i].to);
            }
            else {
                //if(Dbg.KEVIN) Dbg.print("marker else {");
                removeEdge(edges[i], nodes[edges[i].to]);
                removeEdge(edges[i], nodes[edges[i].from]);
                //Dbg.print("-edge removed "+edges[i].from+" -> "+edges[i].to);
            }
        }

        numNodes = keep.size();
        if (Dbg.KEVIN) Dbg.print("keep size: "+ keep.size());
        numEdges = 0;

        Iterator iterator = keep.iterator();
        for (int i=0 ; i<MAX_NODES ; i++ ) {
            if (iterator.hasNext()) {
                nodes[i] = (WordNode) iterator.next();
                nodes[i].ptr = 0;
            } else {
                nodes[i] = null;
            }
        }

        //Dbg.print("total nodes added: "+numNodes+" edges: "+j);

        //edges = new FunPanelEdges[MAX_EDGES];
        DictField to = new DictField();
        for (int i = 0; i < MAX_EDGES ; i++) {
            if (i < j) {
                to.uniqueKey = t_edges[i].sTo;
                to.tag = t_edges[i].tag;
                addEdge(t_edges[i].sFrom, to, 0.0, 0.0);
                //exact & co-ordinates arguments to addEdge ignored because all nodes exist
            } else {
                edges[i] = null;
            }
        }
        //  if(Dbg.KEVIN) for(int k=0; k< numNodes; k++) Dbg.print(nodes[k].toString());
    } //removeNodeEdges

    public void removeEdge(FunPanelEdge cur, WordNode wn) {
        if (Dbg.NETWORK) {
            Dbg.print("trying to remove "+cur+" from " + wn.uniqueKey);
        }
        if (nodes[cur.from].uniqueKey.equals(wn.uniqueKey)) {
            if (Dbg.NETWORK) {
                Dbg.print("from label lbl = "+nodes[cur.from].uniqueKey +
                          " removing to " +nodes[cur.to].uniqueKey);
            }
            wn.edgeStrings .removeElement(nodes[cur.to].uniqueKey);
        } else if (nodes[cur.to].uniqueKey.equals(wn.uniqueKey)) {
            if (Dbg.NETWORK) {
                Dbg.print("to label lbl = "+nodes[cur.from].uniqueKey +
                          " removing from " +nodes[cur.from].uniqueKey);
            }
            wn.edgeStrings .removeElement(nodes[cur.from].uniqueKey);
        }
    }



    /** This function is quite the same as the previous one but new parameters
     *   is required. Used for History.java
     *   [It should probably be merged together! CDM.]
     *          Lim Hong Lee                    21/8/00
     *
     *   @param backwardList - the backwardList
     *   @param word - the target word
     */
    private synchronized void removeNodeEdges(Vector<WordNode> keep, int target,
                                              boolean focus,
                                           Vector backwardList, String word) {
        FunPanelEdge[] t_edges = new FunPanelEdge[numEdges];  //kept edges

        int j=0;
        DictField entry = new DictField();
        boolean remove = true;

        for (int i=0; i<numEdges ; i++) {

            if ( (edges[i].to!=target)&&(edges[i].from!=target) ) {
                t_edges[j] = edges[i];
                t_edges[j].sFrom = nodes[edges[i].from].uniqueKey;
                t_edges[j].sTo = nodes[edges[i].to].uniqueKey;
            }

            //  When the target is found, check which edges are still required to keep.
            else {
                for (int k = 0; k < backwardList.size(); k++) {
                    String pword = (String) (backwardList.elementAt(k));
                    entry.uniqueKey = Helper.getWord(pword);

                    // If there are any kept edges, do not remove the node.
                    if (pword.equals(nodes[edges[i].from].uniqueKey)) {
                        t_edges[j] = edges[i];
                        t_edges[j].sFrom = nodes[edges[i].from].uniqueKey;
                        t_edges[j].sTo = nodes[edges[i].to].uniqueKey;
                        remove = false;
                        break;
                    }
                }
            }
        }

        numNodes = keep.size();
        numEdges = 0;

        Iterator<WordNode> iterator = keep.iterator();
        for (int i=0 ; i<MAX_NODES ; i++ ) {
            if (iterator.hasNext()) {
                nodes[i] = iterator.next();
                nodes[i].ptr = 0;
            } else {
                nodes[i] = null;
            }
        }

        DictField to = new DictField();
        for (int i=0 ; i<MAX_EDGES ; i++) {
            if (i<j) {
                to.uniqueKey = t_edges[i].sTo;
                to.tag = t_edges[i].tag;
                addEdge(t_edges[i].sFrom, to, 0.0, 0.0);
            } else {
                edges[i] = null;
            }
        }

        // the node should not be the focus when a "Back" is done
        // This next line has sometimes triggered an error, when called
        // with nothing in focusNodes.  This shouldn't happen, but try
        // to avoid crash
        try {
            focusNodes.removeElementAt(focusNodes.size() - 1);
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            Helper.handleException(aioobe);
        }
        if (remove) removeNode(word);
    }

    //---- end node/edge functions
    //---- start other functions

    /** Move all the nodes around a bit
     */
    public synchronized void scrambleShake(final boolean scramble) {
        Dimension d = getSize();
        for (int i=0 ; i < numNodes ; i++) {
            WordNode n = nodes[i];
            if(!n.fixed) {
                if (scramble) {
                    n.x = 10 + (d.width - 20)*Math.random();
                    n.y = 10 + (d.height - 20)*Math.random();
                } else {
                    n.x += 80*Math.random() - 40;
                    n.y += 80*Math.random() - 40;
                }
            }
        }
        // echoNodePositions();
    }

    //used to identify the NaN value - Not a Number
    // appearing in the calculations of the spring algorithm
    //public void checkForNaN(WordNode n, int identify) {
    //    String message = null;
    //    if(Double.isNaN(n.x)) {
    //        message = new String(n.lbl+" has NaN x at:"+identify);
    //    } else if (Double.isNaN(n.y)) {
    //        message = new String(n.lbl+" has NaN y at:"+identify);
    //    } else if (Double.isNaN(n.dx)) {
    //        message = new String(n.lbl+" has NaN dx at:"+identify);
    //    } else if (Double.isNaN(n.dy)) {
    //        message = new String(n.lbl+" has NaN dy at:"+identify);
    //    }
    //
    //    if(message != null) {
    //        Dbg.print(message);
    //        Dbg.print(" x:"+n.x+" y:"+n.y+" dx:"+n.dx+" dy:"+n.dy);
    //        //System.exit(0);
    //    }
    //}

    private static double euclideanDistance(double x1, double y1,
                                     double x2, double y2) {
        return( Math.sqrt( ((x1-x2)*(x1-x2)) + ((y1-y2)*(y1-y2)) ));
    }

    /** Calculates how far node n1 should be moved according to its repulsion
     *  from node n2.  Updates the dx, dy parameters of n1 according to this.
     *  (note: this could be made more efficient by updating both nodes at
     *  the same time since repulson is symmetrical)
     */
    private synchronized void repelfrom(WordNode n1, WordNode n2) {
        double len = euclideanDistance(n1.x, n1.y, n2.x, n2.y);
        if (len < 0.1)     // especially beware the case len == 0.0
            len = 0.1;

        double c2 = C / (len * len);
        // cdm: combine these for the moment
        //if(len < 150.0) {
        // Newtonian gravitational force g_n1_n2
        //temp = K2 * c2;
        //n1.dx += (n1.x - n2.x) * temp;
        //n1.dy += (n1.y - n2.y) * temp;
        //}

        if (len < 200.0) {
            // Newtonian gravitational force h_n1_n2 (greater repulsion from
            // focus nodes).  CDM: Maybe just do this for all nodes??
            // I'm trying that...
            // if (focusNodes.contains(n2.lbl)) {
            double temp = (K3 + K2) * c2;
            n1.dx += (n1.x - n2.x) * temp;
            n1.dy += (n1.y - n2.y) * temp;
            //}
        }
    }

    private void spring() {
        try {

            // Hookes Law - spring forces within each edge
            for (int i = 0 ; i < numEdges ; i++) {
                FunPanelEdge e = edges[i];
                double K1;        //stiffness of spring (depends on if one end is a focus node)
                if ( focusNodes.contains(nodes[e.from].uniqueKey) || focusNodes.contains(nodes[e.to].uniqueKey) ) {
                    K1 = -1/3.0;
                } else {
                    K1 = -1/30.0;
                }
                double len = euclideanDistance(nodes[e.to].x, nodes[e.to].y,
                                               nodes[e.from].x, nodes[e.from].y);
                if((int)len == 0) {
                    continue;
                }

                double temp =  K1 * (len - Luv) *  C / len;
                nodes[e.from].dx += (nodes[e.from].x - nodes[e.to].x) * temp;
                nodes[e.from].dy += (nodes[e.from].y - nodes[e.to].y) * temp;

                nodes[e.to].dx += (nodes[e.to].x - nodes[e.from].x) * temp;
                nodes[e.to].dy += (nodes[e.to].y - nodes[e.from].y) * temp;
            }
            // Newtonian forces between nodes
            for (int i = 0 ; i < numNodes ; i++) {
                for (int j = 0 ; j < numNodes ; j++) {
                    if (i != j)
                        repelfrom(nodes[i], nodes[j]);
                }
            }

            // Newtonian forces from boundary - now done by pseudo-nodes which
            // are placed on the boundaries horizontally and vertically next
            // to each node.  Better than having lots of fenceposts [cdm].

            Dimension d = getSize();
            WordNode ndummy = new WordNode();
            ndummy.uniqueKey = "!!!"; // won't match any word

            for (int i = 0 ; i < numNodes ; i++) {
                WordNode n1 = nodes[i];
                ndummy.x = n1.x;
                ndummy.y = 0.0;
                repelfrom(n1, ndummy);
                ndummy.y = d.height;
                repelfrom(n1, ndummy);
                ndummy.y = n1.y;
                ndummy.x = 0.0;
                repelfrom(n1, ndummy);
                ndummy.x = d.width;
                repelfrom(n1, ndummy);

                // Regardless force nodes to stay roughly on screen
                // cdm June 2000: remove extra clauses, which
                // stopped nodes sticking when resize panel.
                if (n1.x < BORDERX && n1.dx < 2.0)
                    n1.dx = 2.0;
                else if (n1.x > (d.width - BORDERX) && n1.dx > -2.0)
                    n1.dx = -2.0;

                if (n1.y < BORDERY && n1.dy < 2.0)
                    n1.dy = 2.0;
                else if (n1.y > (d.height - BORDERY) && n1.dy > -2.0)
                    n1.dy = -2.0;
            }

            //finalise the new x & y values of each node
            for (int i = 0 ; i < numNodes ; i++) {
                WordNode n = nodes[i];
                if (!n.fixed && !n.pinned) {
                    if(SAND) {
                        if(Math.abs(n.dx)< SAND_FACTOR) {
                            n.dx = 0.0;
                        }
                        if(Math.abs(n.dy)< SAND_FACTOR) {
                            n.dy = 0.0;
                        }
                    }
                    n.x += Math.max(-5, Math.min(5, n.dx));
                    n.y += Math.max(-5, Math.min(5, n.dy));
                }
                n.dx = 0;
                n.dy = 0;
            }
        } catch (NullPointerException e) { // nodes fiddled
            if (Dbg.ERROR) {
                Dbg.print("FunPanel: spring NullPtr");
                e.printStackTrace();
            }
        }
    } //spring


    private void paintNode(Graphics g, WordNode n) {
        int x = (int)n.x;
        int y = (int)n.y;
        g.setFont(funfont);
        if (funfm == null)
            funfm = g.getFontMetrics(funfont);

        if (n == pick) {
            g.setColor(selectColor);
        } else if (focusNodes.contains(n.uniqueKey)) {
            /*            if(n.pinned) {
                g.setColor(pinFocusColor);
                } else {*/
                g.setColor(focusColor);
                //}
        } else {
            g.setColor(nodeColor);
            // if (n.register)
            //     g.setColor(g.getColor().brighter());
        }

        /*        if(n.pinned) {
            g.setColor(g.getColor().brighter());
            }*/

        int w = 10;
        int h = funfm.getHeight() + 4;
        // get display value rather than the unique key stored in lbl
        String trimLbl = Helper.getWord(n.uniqueKey);
        if (gloss) {
            h = (h * 2);
            w += Math.max(funfm.stringWidth(trimLbl),
                          funfm.stringWidth(n.getGlossLbl()));
            h += 2;     //2 above, 2 between and 2 pixel spaces below
        } else {
            w += funfm.stringWidth(trimLbl);
        }
        boolean put_in_poly = graph.parent.showPoly() && ! Helper.unresolvableWord(n.uniqueKey);
        if (put_in_poly) {
            w += 10;
        }

        g.fill3DRect(x - w/2, y - h / 2, w, h, true);
        g.setColor(Color.black);
        // g.drawRect(x - w/2, y - h / 2, w-1, h-1);
        if (!gloss) {
            if (Helper.unknownWord(n.uniqueKey)) {
                g.setColor(Color.gray);
            }
            g.drawString(trimLbl, x - (w-10)/2, (y - (h-4)/2) + funfm.getAscent());       //x,y is middle
        } else {
            if (Helper.unknownWord(n.uniqueKey)) {
                g.setColor(Color.gray);
            }
            g.drawString(trimLbl, x - (w-10)/2, (y - (h-6)/2) + funfm.getAscent());
            g.setColor(Color.gray);
            g.drawString(n.getGlossLbl(), x - (w-10)/2, (y + 2) + funfm.getAscent());
        }
        if (put_in_poly) {
            g.setFont(superscript);
            g.setColor(Color.black);
            g.drawString(String.valueOf(Helper.getUniquifier(n.uniqueKey)), x + (w/2) - 10,
                         (y - (h-4)/2) + 8);
            // g.setFont(funfont);
        }
    }


    public void paintEdge(Graphics g, int x1, int y1, int x2, int y2) {
        int dx = x2-x1;
        int dy = y2-y1;

        if (dx == 0) {
            final int[] x = { x1+EW, x1-EW, x2-EW, x2+EW};
            final int[] y = { y1, y1, y2, y2};
            //Dbg.print("==: "+"x1 "+x1+" y1 "+y1+" x2 "+x2+" y2 "+y2+" dx "+dx+" dy" +dy);
            g.fillPolygon(x, y, 4);
        } else {
            double m = -1.0 * dy/dx;
            double theta = Math.atan(m);
            dx = (int) Math.abs(Math.round(EW * Math.sin(theta)));
            dy = (int) Math.abs(Math.round(EW * Math.cos(theta)));
            if (m < 0) {
                final int[] x = { x1+dx, x1-dx, x2-dx, x2+dx};
                final int[] y = { y1-dy, y1+dy, y2+dy, y2-dy};
                //Dbg.print("-ve: "+"x1 "+x1+" y1 "+y1+" x2 "+x2+" y2 "+y2+" dx "+dx+" dy" +dy);
                g.fillPolygon(x, y, 4);
            } else {
                final int[] x = { x1-dx, x1+dx, x2+dx, x2-dx};
                final int[] y = { y1-dy, y1+dy, y2+dy, y2-dy};
                //Dbg.print("+ve: "+"x1 "+x1+" y1 "+y1+" x2 "+x2+" y2 "+y2+" dx "+dx+" dy" +dy);
                g.fillPolygon(x, y, 4);
            }
        }
    }

    /** Draws a legend in the top right. We assume we are drawing in an empty canvas. */
    private void drawLegend(Graphics g) {
        // g.setColor(getBackground());
        // g.fillRect(0, 0, w, h);

        g.setFont(legendfont);
        FontMetrics fm = g.getFontMetrics(legendfont);

        int rowH = fm.getHeight();
        int boxH = rowH / 2;
        int maxText = 0;
        int rowY = vspace;

        DictionaryInfo dictInfo = Kirrkirr.dictInfo;
        int numLinks = dictInfo.getNumLinks();
        for (int i = 0; i < numLinks; i++) {
            String desc = Helper.getTranslation(dictInfo.getLinkName(i));
            g.setColor(dictInfo.getLinkColor(i));
            g.fillRect(boxX + 1, rowY + (rowH - boxH)/2 + 1, boxW, boxH);
            g.setColor(Color.black);
            g.drawString(desc,textX + 1, rowY + (3 * rowH) / 4 + 1);
            maxText = Math.max(maxText, fm.stringWidth(desc));
            rowY += (rowH + vspace);
        }

        g.setColor(Color.black);
        g.drawRect(1, 1, boxX + boxW + hspace + maxText + vspace + 1, rowY + 1);
    }

/*

        if (legend) {
            drawLegend(offgraphics);
            offgraphics.drawImage(getLegendImage(), 1, 1, null);
        }

    private Image getLegendImage() {
        if (legendImage == null) {
            legendImage = createImage(w, h);
            Graphics imageGraphic = legendImage.getGraphics();

            imageGraphic.setColor(getBackground());
            imageGraphic.fillRect(0, 0, w, h);

            imageGraphic.setFont(legendfont);
            FontMetrics fm = imageGraphic.getFontMetrics(legendfont);

            int rowH = fm.getHeight();
            int boxH = rowH / 2;
            int maxText = 0;
            int rowY = vspace;

            DictionaryInfo dictInfo = Kirrkirr.dictInfo;
            int numLinks = dictInfo.getNumLinks();
            for(int i=0; i<numLinks; i++) {
                String desc = Helper.getTranslation(dictInfo.getLinkName(i));
                imageGraphic.setColor(dictInfo.getLinkColor(i));
                imageGraphic.fillRect(boxX, rowY + (rowH - boxH)/2, boxW, boxH);
                imageGraphic.setColor(Color.black);
                imageGraphic.drawString(desc,
                                        textX, rowY + (3 * rowH) / 4);
                maxText = Math.max(maxText, fm.stringWidth(desc));

                rowY += (rowH + vspace);
            }

            imageGraphic.setColor(Color.black);
            imageGraphic.drawRect(0, 0, (boxX + boxW + hspace + maxText + vspace), rowY);
            // Graphics consume native resources which should be freed
            imageGraphic.dispose();
        }
        return legendImage;
    }
*/

    @Override
    public void paint(Graphics g) {
        if (pausePaint)
            return;

        Dimension d = getSize();
        if ((offscreen == null) || (d.width != offscreensize.width) || (d.height != offscreensize.height)) {
            offscreen = createImage(d.width, d.height);
            offscreensize = d;
            offgraphics = offscreen.getGraphics();
            offgraphics.setFont(getFont());
        }

        // You desperately need font rendering hints in JDK 7+
        Map<?, ?> desktopHints = (Map<?, ?>)
                Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
        Graphics2D offg2d = (Graphics2D) offgraphics;
        if (desktopHints != null) {
            offg2d.setRenderingHints(desktopHints);
        }

        offgraphics.setColor(getBackground());
        offgraphics.fillRect(0, 0, d.width, d.height);
        if (legend) {
            drawLegend(offgraphics);
            // offgraphics.drawImage(getLegendImage(), 1, 1, null);
        }

        for (int i = 0 ; i < numEdges ; i++) {
            FunPanelEdge e = edges[i];
            if (hideEdge.containsKey(Short.valueOf(e.tag))) {
                nodes[e.from].colourPtr++;
                nodes[e.to].colourPtr++;
                continue;                   //don't draw the edge
            }
            int x1 = (int)nodes[e.from].x;
            int y1 = (int)nodes[e.from].y;
            int x2 = (int)nodes[e.to].x;
            int y2 = (int)nodes[e.to].y;
            int len = (int) euclideanDistance(x1, y1, x2, y2);
            //vj changed
            //offgraphics.setColor(DictField.getColor(e.tag));
            offgraphics.setColor(Kirrkirr.dictInfo.getLinkColor(e.tag));

            paintEdge(offgraphics, x1, y1, x2, y2);

            if (stress) {
                String lbl = String.valueOf(len);
                offgraphics.setColor(stressColor);
                offgraphics.drawString(lbl, x1 + (x2-x1)/2, y1 + (y2-y1)/2);
                // offgraphics.setColor(edgeColor);
            }
        }

        for (int i = 0 ; i < numNodes ; i++) {
            if((focusNodes.contains(nodes[i].uniqueKey)) || (nodes[i].colourPtr < nodes[i].ptr)) {
                paintNode(offgraphics, nodes[i]);
            }
            nodes[i].colourPtr = 0;
        }
        g.drawImage(offscreen, 0, 0, null);
        // cdm: I added a g.dispose() following Core Java2 Vol I p.345
        // but offgraphics continues to be used, so don't
    }

    /** Record that this word is the highlighted selected word.
     *  Called by GraphPanel
     *  @param uniqueKey The unique key for the word to highlight
     */
    public void setSelected(String uniqueKey) {
        for (int i = 0 ; i < numNodes ; i++) {
            WordNode n = nodes[i];
            if (n.uniqueKey.equals(uniqueKey))
                pick = n;
        }
    }


    //------ All the quiz functions. These should either be in a subclass or ??
    //for fyp'00
    public void addEdgeForGame(String uniqueKey, DictField df) {
        // int size = links.size();
        int size = 5;
        double sepAngle = 2*Math.PI / size;
        // keep nodes away from horizontal (worst for current algorithm)
        double currentAngle = sepAngle / 4;

        //for (int i = 0; i < size; i++) {
        //     DictField df = links.get(i);

        double dx = Math.cos(currentAngle) * 70.0;
        double dy = Math.sin(currentAngle) * 70.0;
        addEdge(uniqueKey, df, dx, dy);
        //}
    }

    /*for quizmaster. Displays the individual words with no links
     * @Madhu:'00
     */
    public void addTextForGame(String uniqueKey, DictFields links) {
        if (Dbg.NETWORK) Dbg.print("addText: links is " + links);
        int size = links.size();

        if (size > 0) {
            for (int i = 0; i < size; i++) {
                DictField df = links.get(i);
                findNode(df.uniqueKey);
            }
        }
        findNode(uniqueKey);
    }

    //Madhu:'00 just deletes the node connected to the main word (for WordGame)
    private synchronized void removeNodeEdgesForGame(Vector<WordNode> keep, int target, boolean focus) {
        FunPanelEdge[] t_edges = new FunPanelEdge[numEdges];  //kept edges

        int j=0;
        //if(Dbg.KEVIN) Dbg.print("gets outside quiz for loop");
        for(int i=0; i<numEdges ; i++) {
            //if(Dbg.KEVIN) Dbg.print("gets into quiz for loop");
            //i is the counter in edges[], j is counter in t_edges[]

            if( (edges[i].to!=target)&&(edges[i].from!=target) ) {
                t_edges[j] = edges[i];
                t_edges[j].sFrom = nodes[edges[i].from].uniqueKey;
                t_edges[j].sTo = nodes[edges[i].to].uniqueKey;
                //Dbg.print("+edge added "+edges[i].from+" -> "+edges[i].to);
            } else if (focus && ((edges[i].from == target) && (nodes[edges[i].to].ptr==1))) {
                // if the only node that has an edge to you was the deleted
                // focus node, then you're deleted too
                // pre focusNodes.contains(nodes[edges[i].from].lbl);
                keep.removeElement(nodes[edges[i].to]);
                //Dbg.print("-child node removed "+edges[i].from+" -> "+edges[i].to);
            } else {
                //Dbg.print("-edge removed "+edges[i].from+" -> "+edges[i].to);
            }
        }

        numNodes = keep.size();
        numEdges = 0;

        DictField to = new DictField();
        for (int i=0 ; i<MAX_EDGES ; i++) {
            if (i < j) {
                to.uniqueKey = t_edges[i].sTo;
                to.tag = t_edges[i].tag;
                addEdge(t_edges[i].sFrom, to, 0.0, 0.0);
                //exact & co-ords arguments to addEdge ignored because all nodes exist
            } else {
                edges[i] = null;
            }
        }
    }

    //Madhu:'00
    public synchronized void removeNodeForGame(String lbl) {
        boolean temp_stop = stop;
        boolean temp_pause = pausePaint;
        stop = true;
        pausePaint = true;
        Vector<WordNode> keep = new Vector<>(MAX_NODES);          // vector of kept nodes
        int target=numNodes;

        for(int i=0 ; i<numNodes ; i++) {
            if( nodes[i].uniqueKey.equals(lbl) ) {
                target = i;
                if(Dbg.KEVIN)
                    Dbg.print("label of deletednode:" + lbl);
            }
            else {
                keep.addElement(nodes[i]);
                if(Dbg.KEVIN) Dbg.print("keeping node of label: (" + i + ") "+ nodes[i].uniqueKey);
            }
        }

        if ( target != numNodes ) {
            // we found something
            WordNode temp = nodes[target];
            nodes[target] = nodes[numNodes-1];
            nodes[numNodes-1] = temp;

            boolean focus = focusNodes.removeElement(nodes[target].uniqueKey);
            removeNodeEdgesForGame(keep, target, focus);
        }

        pausePaint = temp_pause;
        stop = temp_stop;
    }


    /* Madhu:'00 collapses the edges from a node, wihle still keeping the rest of the nodes intact
       synchronized void collapseNodeForGame(String lbl){
       boolean temp_stop = stop;
       boolean temp_pause = pausePaint;
       stop = true;
       pausePaint = true;

       Vector keep = new Vector(MAX_NODES);          //vector of kept nodes
       int target=numNodes;

       for(int i=0 ; i<numNodes ; i++) {
       if( nodes[i].lbl.equals(lbl) ) {
       target = i;
       }
       keep.addElement(nodes[i]);
       }
       //Madhu:'00:now keep contains the list of nodes in the funPanel
       //Madhu:'00 target is the number of the node who's edges to be collapsed, in the list

       removeNodeEdgesForGame(keep, target, false);
       pausePaint = temp_pause;
       stop = temp_stop;
       return;
       } */

    public String getSelectedText() {
        if (pick == null) {
            return null;
        } else if (pick.uniqueKey != null) {
            if (graph.parent.showPoly()) {
                return Helper.uniqueKeyToPrintableString(pick.uniqueKey);
            } else {
                return Helper.getWord(pick.uniqueKey);
            }
        } else {
            return Helper.getWord(pick.uniqueKey);
        }
    }


    // End of contents of main class FunPanel!!


    private final class FunPanelMouseListener extends MouseAdapter
               implements Serializable, ActionListener {

        @Override
        public void mousePressed (MouseEvent e) {
            int x = e.getX();
            int y = e.getY();

            double bestdist = Double.MAX_VALUE;
            pick = null;
            for (int i = 0 ; i < numNodes ; i++) {
                WordNode n = nodes[i];
                double dist = (n.x - x) * (n.x - x) + (n.y - y) * (n.y - y);
                if (dist < bestdist) {
                    pick = n;
                    bestdist = dist;
                }
            }

            // make sure the click is somewhere near a word
            // this is currently crude: just uses center of word
            // and note that the distance is kept squared! (i.e., <= 60 pixels)
            if (bestdist > 3600.0) {
                pick = null;
            }
            if (pick!=null &&  !((focusNodes.contains(pick.uniqueKey)) || (pick.colourPtr < pick.ptr))){
                if (Dbg.NETWORK){
                    Dbg.print("Found node "+pick.uniqueKey +" but not gonna use it, ptr="
                              +pick.ptr+" color = "+pick.colourPtr);
                }
                pick=null;
            }
            if (pick == null) {
                if (Dbg.NETWORK && numNodes > 0) {
                    Dbg.print("no node found: x="+x+" y="+y);
                }
                return;
            }
            pick.fixed = true;
            pick.x = x;
            pick.y = y;

            if (focusNodes.removeElement(pick.uniqueKey)) {
                focusNodes.addElement(pick.uniqueKey);
            }
            if (e.isPopupTrigger()) {
                showMyPopup(x, y);
            } else if ((e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0) {
                triggerFunEvent(FIND);
            }
        }


        @Override
        public void mouseDragged(MouseEvent e)
        {
            int x = e.getX();
            int y = e.getY();

            if (pick != null) {
                pick.x = x;
                pick.y = y;
            }
        }

        @Override
        public void mouseMoved(MouseEvent e)
        {
            //Dbg.print("x:"+e.getX()+" y:"+e.getY());
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
            if (pick != null) {
                int x = e.getX();
                int y = e.getY();
                Dimension d = getSize();

                if (x <= 0) {
                    x = 1;
                } else if (x >= d.width) {
                    x = d.width-1;
                }
                if (y <= 0) {
                    y = 1;
                } else if (y >= d.height) {
                    y = d.height - 1;
                }

                pick.x = x;
                pick.y = y;
                pick.fixed = false;

                if (e.isPopupTrigger()) {
                    showMyPopup(x, y);
                } else {
                    //if double clicked, sprout
                    //if clicked with left mouse button, allow moving without
                    // sprouting or showing popup menu
                    if (e.getClickCount() > 1) {
                        triggerFunEvent(SPROUT);
                    }
                }
            }
        }


        private void showMyPopup(int x, int y) {
            if (popup == null) {
                // initialize the popup menu
                popup = new JPopupMenu();
                popup.add(find = new JMenuItem(Helper.getTranslation(SC_FIND_IN_LIST),
                                               RelFile.makeImageIcon("search.gif",false)));
                popup.add(sprout = new JMenuItem(Helper.getTranslation(SC_SPROUT),
                                                 RelFile.makeImageIcon("sprout.gif",false)));
                popup.add(collapse = new JMenuItem(Helper.getTranslation(SC_COLLAPSE),
                                                   RelFile.makeImageIcon("collapse.gif",false)));
                popup.add(delete = new JMenuItem(Helper.getTranslation(SC_DELETE),
                                                 RelFile.makeImageIcon("cut.gif",false)));
                popup.add(openGloss = new JMenuItem(Helper.getTranslation(SC_SEE_DEFINITION),
                                                    RelFile.makeImageIcon("notes.gif",false)));
                popup.add(pin = new JMenuItem(Helper.getTranslation(SC_ANCHOR_DOWN),
                                              RelFile.makeImageIcon("anchor.gif",false)));
                popup.add(release = new JMenuItem(Helper.getTranslation(SC_RELEASE),
                                                  RelFile.makeImageIcon("release.gif",false)));
                find.setHorizontalTextPosition(SwingConstants.RIGHT);
                sprout.setHorizontalTextPosition(SwingConstants.RIGHT);
                collapse.setHorizontalTextPosition(SwingConstants.RIGHT);
                delete.setHorizontalTextPosition(SwingConstants.RIGHT);
                openGloss.setHorizontalTextPosition(SwingConstants.RIGHT);
                pin.setHorizontalTextPosition(SwingConstants.RIGHT);
                release.setHorizontalTextPosition(SwingConstants.RIGHT);
                find.addActionListener(this);
                sprout.addActionListener(this);
                collapse.addActionListener(this);
                delete.addActionListener(this);
                openGloss.addActionListener(this);
                pin.addActionListener(this);
                release.addActionListener(this);
            }
            pin.setEnabled(! pick.pinned);
            release.setEnabled(pick.pinned);
            openGloss.setEnabled(pick.uniqueKey != null);
            find.setEnabled(pick.uniqueKey != null);
            sprout.setEnabled(pick.uniqueKey != null);
            popup.show(graph.funPanel, x, y);
        }


        private void triggerFunEvent(final int arg) {
            if (Dbg.NETWORK) {
                if (arg == FIND) {
                    Dbg.print("Triggered FIND");
                } else {
                    Dbg.print("Triggered SPROUT");
                }
                Dbg.print("pick.exact: " + pick.uniqueKey + '.');
            }
            if (arg == FIND) {
                graph.funFindWord(pick.uniqueKey);
            }
            else if (arg == SPROUT) {
                graph.funGetLinks(pick.uniqueKey);
            }
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (pick == null) {
                if (Dbg.ERROR && numNodes > 0) {
                    Dbg.print("no node found (pick is null) for " + e);
                }
                return;
            }
            Object src = e.getSource();
            if (src == sprout) {
                triggerFunEvent(SPROUT);
            } else if (src == find) {
                triggerFunEvent(FIND);
            } else if (src == delete) {
                removeNode(pick.uniqueKey);
                focusNodes.removeElement(pick.uniqueKey);
            } else if (src == collapse) {
                collapseNode(pick.uniqueKey);
            } else if (src == pin) {
                // toggle between pin and unpin (see MousePressed)
                pick.pinned = ! pick.pinned;
            } else if (src == release) {
                // toggle between pin and unpin (see MousePressed)
                pick.pinned = !(pick.pinned);
            } else if (src == openGloss) {
                graph.openGlossDialog(pick.uniqueKey);
            }
            pick.fixed = false;
            pick = null;
        }

    } // FunPanelMouseListener


    /** A non-static inner class for representing nodes.  It needs to be
     *  non-static because it manipulates the nodes array stored with
     *  a FunPanel object.
     */
    private class WordNode implements Externalizable {

        private static final long serialVersionUID = 8994670180845919396L;

        // cdm Dec2001: these objects have got very big.  Do we really need
        // all these fields??
        double x;
        double y;
        transient double dx;
        transient double dy;

        String uniqueKey;     // a unique key
        boolean fixed;
        boolean pinned; // the node has been pinned down and shouldn't move
        // boolean register;
        int ptr;        // indicates how many nodes have edges to it
        int colourPtr;  //indicates how many edges connected to it are hidden

        String gloss_lbl; // null if not set, empty string if none
        Vector<String> edgeStrings = new Vector<>(); // yuk! (too heavy, remove)

        public WordNode() {
        }

        public String toString() {
            return "|" + uniqueKey +"| eng: "+gloss_lbl + "| x: "+
                x+" y: "+y +" fixed: "+fixed+" ptr: "+ptr+" Colour ptr: "+
                colourPtr;
        }

        public void addEdge(FunPanelEdge cur) {
            if (nodes[cur.from].uniqueKey.equals(uniqueKey)) {
                String pword=nodes[cur.to].uniqueKey;
                if (!edgeStrings .contains(pword))
                    edgeStrings .addElement(pword);
            } else if (nodes[cur.to].uniqueKey.equals(uniqueKey)) {
                String pword=nodes[cur.from].uniqueKey;
                if (!edgeStrings .contains(pword))
                    edgeStrings .addElement(pword);
            }
        }

        public String getGlossLbl() {
            // Lazily initialized
            // exact may be null if no word in dictionary...
            // we only get it if we haven't got it before...
            if (gloss_lbl == null) {
                if (uniqueKey != null) {
                    gloss_lbl = graph.parent.cache.getFirstGloss(uniqueKey);
                    // returns NO_ENG = "" if can't find things
                } else {
                    gloss_lbl = "";
                }
            }
            return gloss_lbl;
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeDouble(x);
            out.writeDouble(y);
            out.writeBoolean(fixed);
            out.writeBoolean(pinned);
            out.writeObject(uniqueKey);
            out.writeInt(ptr);
            out.writeInt(colourPtr);
            out.writeObject(uniqueKey);
            out.writeObject(gloss_lbl);
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            x = in.readDouble();
            y = in.readDouble();
            fixed = in.readBoolean();
            pinned = in.readBoolean();
            uniqueKey = (String) in.readObject();
            ptr = in.readInt();
            colourPtr = in.readInt();
            uniqueKey = (String)in.readObject();
            gloss_lbl = (String)in.readObject();
        }

    } // WordNode


    private static class FunPanelEdge implements Externalizable {

        private static final long serialVersionUID = -294269546099815877L;
        int from;
        int to;
        short tag;
        String sFrom, sTo;           //s_to and s_from are only used in the removeNode method
        double len;

        public FunPanelEdge() {
        }

        @Override
        public void writeExternal(ObjectOutput out)
            throws IOException {
            out.writeInt(from);
            out.writeInt(to);
            out.writeShort(tag);
            out.writeObject(sFrom);
            out.writeObject(sTo);
            out.writeDouble(len);
        }

        @Override
        public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
            from = in.readInt();
            to = in.readInt();
            tag = in.readShort();
            sFrom = (String)in.readObject();
            sTo = (String)in.readObject();
            len = in.readDouble();
        }

    } //FunPanelEdge


} // FunPanel



