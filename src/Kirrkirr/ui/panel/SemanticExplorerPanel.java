package Kirrkirr.ui.panel;

import Kirrkirr.Kirrkirr;
import Kirrkirr.util.*;
import Kirrkirr.ui.KirrkirrButton;

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/** The <code>SemanticExplorer</code> presents a tree-based explorer
 *  that lets the user navigate the <DOMAIN> hierarchy.
 *  @author      Kevin Jansz
 *  @see Kirrkirr
 */
public class SemanticExplorerPanel extends KirrkirrPanel
            implements TreeSelectionListener, ActionListener {

    private JButton clear;
    private JButton collapse;
    private JButton expand;

    private static final String SC_NAME="Domains";
    private static final String SC_ROOT="Semantic_Domain";
    private static final String SC_OTHER="unknown";
    private static final String SC_CLEAR="Clear";
    private static final String SC_COLLAPSE="Collapse";
    private static final String SC_EXPAND="Expand";
    private static final String SC_DESC="Explore a tree view of semantic domains";
    private static final String SC_WORD="Word";

    private JTree tree; // = null;
    private DefaultTreeModel treeModel;
    private SemanticTreeNode root;
    private SemanticTreeNode noDomain;

    public SemanticExplorerPanel (Kirrkirr kparent, JFrame window, int size) {
        super(kparent, window);
        setName(Helper.getTranslation(SC_NAME));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        root = new SemanticTreeNode(Helper.getTranslation(SC_ROOT));
        noDomain = new SemanticTreeNode(Helper.getTranslation(SC_OTHER) + "@?");

        treeModel = new DefaultTreeModel(root);
        tree = new JTree(treeModel);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.putClientProperty("JTree.lineStyle", "Angled");
        tree.setShowsRootHandles(true);
        tree.setRootVisible(false);
        tree.setCellRenderer(new SemanticTreeRenderer(parent));
        tree.addTreeSelectionListener(this);
        JScrollPane semanticView = new JScrollPane(tree);

        add(semanticView);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));

        clear = new KirrkirrButton(SC_CLEAR, this);
        clear.setBackground(Color.black);
        clear.setForeground(Color.white);
        p.add(clear);

        collapse = new KirrkirrButton(SC_COLLAPSE, this);
        collapse.setBackground(Color.red);
        collapse.setForeground(Color.white);
        p.add(collapse);

        expand = new KirrkirrButton(SC_EXPAND, this);
        expand.setBackground(Color.green);
        expand.setForeground(Color.black);
        p.add(expand);

        p.add(Box.createHorizontalGlue());
        add(p);
    }


    /** Returns the String that is suitable rollover text for a tabbed
     *  pane containing this panel.
     *  @return the string to be used as rollover text
     */
    public String getTabRollover() {
        return SC_DESC;
    }


    public void tabSelected()
    {/*
        String tailWord = (String) parent.scrollPanel.getSelectedWord();
        Vector backwardList = parent.history.getBackwardList();
        // Lim Hong Lee:
        // need to load back all the words from the backwardList for
        // cdm: this was a no-op.  Intended purpose?
        // boolean[] changed = parent.history.getChanged();

        for (int i=0; i<backwardList.size(); i++) {
            // cdm backwardlist is now a hnum padded String
            String entry = (String) backwardList.elementAt(i);
            setCurrentWord(entry, !parent.headwordsShowing(), parent.history, 0, 0);
            }*/
    }


    public void setCurrentWord(String uniqueKey, boolean gloss,
                         final JComponent signaller, final int signallerType,
                               final int arg) {
        if (gloss) return;
        Vector dF = parent.cache.getAllDomains(uniqueKey);
        if (dF == null) { // should only happen if dictionary missing
            Dbg.print("SemanticExplorer: no dictfield found: " + uniqueKey);
            return;
        }
        SemanticTreeNode last = root;
        SemanticTreeNode next = null;
        Vector newWordNodes = new Vector();
        if (Dbg.DOMAINS) Dbg.print("Doing " + Helper.uniqueKeyToPrintableString(uniqueKey) + ", dF is " + dF + " of size " + dF.size());

        // System.err.println("Last is |" + last + "|");
        int dsize = dF.size();
        for (int j = 0; j < dsize; j++) {
            if (j != 0) {
                // you're about to start down a new domain path (for another
                // sense) so first add the word to the domain path you've just
                // finished
                next = addEntryToNode(uniqueKey, last);
                newWordNodes.addElement(next);
            }

            Vector vec = (Vector) dF.elementAt(j);
            for (int i = 0, vsize = vec.size(); i < vsize; i++) {
                String curr = (String) vec.elementAt(i);
                if (i == 0) {
                    last = root;
                }
                last = addEntryToNode(curr, last);
            }
        }

        // System.err.println("Last is |" + last + "|");
        if (dsize == 0) {
            if (noDomain.getChildCount() == 0) {
                // only add the "other" category if it's needed
                treeModel.insertNodeInto(noDomain,root,root.getChildCount());
            }
            last = noDomain;
        }

        // System.err.println("Last is |" + last + "|");
        next = addEntryToNode(uniqueKey, last);
        newWordNodes.addElement(next);

        int newWordCount = newWordNodes.size();
        TreePath paths[] = new TreePath[newWordCount];

        for (int i=0 ; i < newWordCount ; i++) {
            TreeNode[] nodes = treeModel.getPathToRoot((SemanticTreeNode) newWordNodes.elementAt(i));
            paths[i] = new TreePath(nodes);
            tree.makeVisible(paths[i]);
        }

        tree.removeTreeSelectionListener(this);
        tree.scrollPathToVisible(paths[0]);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        tree.setSelectionPaths(paths);
        tree.addTreeSelectionListener(this);
    }


    /** Gets selected node's text name (if any)
     *  and copies it to the system clipboard, returning the
     *  length of the text copied, or 0 if there was none selected.
     *  Overrides KirrkirrPanel
     *  This now seems to be partly working, but one doesn't get anything
     *  from control-C in semantic explorer, as no keyboard stuff built-in.
     *  @param isCut true if this should be a cut operation
     *  @return how many characters were copied (0 if no selection)
     */
    public int copyText() {
        if (Dbg.CUTPASTE) Dbg.print("SemanticExplorer: called copy text");
        TreePath path = tree.getSelectionPath();
        System.err.println("TreePath is " + path);
        if (path == null) {
            return(0);
        } else {
            String entry = (String) ((SemanticTreeNode)path.getLastPathComponent()).getUserObject();
            String selected = Helper.getWord(entry);
            System.err.println("Selected word is " + selected);
            parent.putStringInClipboard(selected);
            System.err.println("Selected length is " + selected.length());
            return selected.length();
        }
    }


    /** This routine adds a new node with value dF as a child of the
     *  node.  In general it tries to sort the entries, except it will
     *  place "unknown" at the end only kind of fortuitiously...
     *  @param word This unique key is used to initialize a new
     *  SemanticTreeNode daughter
     *  @param node The parent node to which the daughter is added
     *  @return the new child node that was created
     */
    private SemanticTreeNode addEntryToNode(String word,
                                           SemanticTreeNode node) {
        if (Dbg.DOMAINS) Dbg.print("Trying to add " + word + " to " + node);
        SemanticTreeNode newNode = node.getChildWithValue(word);
        if (newNode == null) {
            // the tree doesn't contain this node - so add it
            // now add it in alphabetic order
            newNode = new SemanticTreeNode(word);
            boolean inserted = false;
            int numkids = node.getChildCount();
            for (int i = 0; i < numkids; i++) {
                DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode)
                    node.getChildAt(i);
                String sisterWord = (String) dmtn.getUserObject();
                if (word.compareTo(sisterWord) < 0) {
                    treeModel.insertNodeInto(newNode, node, i);
                    inserted = true;
                    break;
                }
            }
            if ( ! inserted) {
                treeModel.insertNodeInto(newNode, node, node.getChildCount());
            }
            //System.out.println("Added: "+newNode.getUserObject());
        }
        return(newNode);
    }


    void removeAllChildren(SemanticTreeNode node) {
        int kids = node.getChildCount();
        SemanticTreeNode target;

        for(int i=(kids-1) ; i >= 0 ; i--) {
            target = (SemanticTreeNode)node.getChildAt(i);
            treeModel.removeNodeFromParent(target);
        }
    }

    void collapseAllChildren(SemanticTreeNode node) {
        int kids = node.getChildCount();
        SemanticTreeNode target;

        for(int i=(kids-1) ; i >= 0 ; i--) {
            TreeNode[] nodes = treeModel.getPathToRoot((SemanticTreeNode)node.getChildAt(i));
            TreePath path = new TreePath(nodes);
            tree.collapsePath(path);
        }
    }

    void expandAllChildren(SemanticTreeNode node) {
        int kids = node.getChildCount();
        SemanticTreeNode target;

        for(int i=(kids-1) ; i >= 0 ; i--) {
            TreeNode[] nodes = treeModel.getPathToRoot((SemanticTreeNode)node.getChildAt(i));
            TreePath path = new TreePath(nodes);
            tree.expandPath(path);
        }
    }


    /** TreeSelectionListener interface.
     */
    public void valueChanged(TreeSelectionEvent event) {
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        TreePath path = tree.getSelectionPath();
        if(path == null)
            return;

        String entry = (String) ((SemanticTreeNode)path.getLastPathComponent()).getUserObject();
        if (Helper.isResolved(entry)) {
            parent.setCurrentWord(entry, false, this, parent.EXPLORER, 0);
            parent.setStatusBar(Helper.getTranslation(SC_WORD)+": "+
                                Helper.uniqueKeyToPrintableString(entry));
        } else {
            parent.setStatusBar(Helper.getTranslation(SC_ROOT)+": " +
                                Helper.getWord(entry));
        }
    }


    /** ActionListener interface (listens to JButton clicks).
     *  @param e the event that has arrived
     */
    public void actionPerformed(ActionEvent e)
    {
        Object obj = e.getSource();
        if (obj.equals(clear) ) {
            clearExplorer();
        } else if (obj.equals(collapse) ) {
            collapseAllChildren(root);
        } else if (obj.equals(expand) ) {
            expandAllChildren(root);
        }
    }


    public boolean isResizable() {
        return(true);
    }

    public void clearExplorer() {
        removeAllChildren(root);
        removeAllChildren(noDomain);
    }

    //
    //---------------- other classes --------------------------------
    //

    /**
     *       SemanticTreeNode
     */
    private static class SemanticTreeNode extends DefaultMutableTreeNode {

        /** Used for for creating the nodes (domains) */
        SemanticTreeNode(final String userObject) {
            super(userObject);
        }

        SemanticTreeNode getChildWithValue(String dF) {
            SemanticTreeNode found;
            int numKids = getChildCount();
            for (int i=0 ; i < numKids; i++) {
                found = (SemanticTreeNode) getChildAt(i);
                if ((found.getUserObject()).equals(dF)) {
                    return(found);
                }
            }
            return null;
        }

    } //SemanticTreeNode


    /** SemanticTreeRenderer.
     *  Should maybe just combine this with other stuff for Cell
     *  Rendering, and have other class implement TreeCellRenderer.
     *  But note performance info in DefaultTreeCellRenderer javadoc.
     */
    private class SemanticTreeRenderer extends DefaultTreeCellRenderer {

        private final Kirrkirr parent;
        // these next 3 can't be static as in inner class
        private final ImageIcon word_in_dict = RelFile.makeImageIcon("blue-ball.gif",false);
        private final ImageIcon domain_in_dict = RelFile.makeImageIcon("magenta-ball.gif",false);
        private final ImageIcon domain_non_dict = RelFile.makeImageIcon("red-ball.gif",false);

        public SemanticTreeRenderer(Kirrkirr parent) {
            this.parent = parent;
            setOpaque(true);
            setForeground(Color.black);
        }

        public Component getTreeCellRendererComponent (final JTree    tree,
                                                       final Object   value,
                                                       final boolean  selected,
                                                       final boolean  expanded,
                                                       final boolean  leaf,
                                                       final int      row,
                                                       final boolean  hasFocus)
        {
            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus); //not sure why you'd call this on super - but they do it in the examples

            SemanticTreeNode node = (SemanticTreeNode) value;
            String entry = (String) node.getUserObject();

            String word = Helper.getWord(entry);
            String poly = Helper.getUniquifier(entry);

            boolean showpoly = parent.showPoly() && ! poly.equals("");
            boolean isDictionaryWord = Helper.isResolved(entry);
            boolean markedsubw = (parent.seeSubwords() && isDictionaryWord
                    && parent.cache.isSubWord(entry));

            setBackground(selected ? Color.yellow : Color.white);
            if (markedsubw) {
                // don't indent for tree (maybe no italic, even?)
                setFont(FontProvider.LARGE_WORD_SUBWORD_FONT);
            } else {
                setFont(FontProvider.LARGE_WORD_FONT);
            }
            if (showpoly) {
                setText(word+" ("+poly+")");
            } else {
                setText(word);
            }

            if (isDictionaryWord) {
                if (leaf) {
                    setIcon(word_in_dict);
                } else {
                    setIcon(domain_in_dict);
                }
            } else {
                setIcon(domain_non_dict);
            }
            return this;
        }

    } //SemanticTreeRenderer

} //SemanticExplorer

