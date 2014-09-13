package Kirrkirr.ui.panel.fun;

import Kirrkirr.ui.panel.GraphPanel;
import Kirrkirr.Kirrkirr;
import Kirrkirr.dictionary.DictFields;
import Kirrkirr.dictionary.DictField;
import Kirrkirr.dictionary.DictionaryInfo;
import Kirrkirr.dictionary.DictionaryCache;
import Kirrkirr.util.Dbg;
import Kirrkirr.util.Helper;
import Kirrkirr.util.RelFile;
import Kirrkirr.util.KRandom;
import Kirrkirr.util.FontProvider;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;


/** FFFPanel.java - Network visualization panel.
 *
 * @author Conrad Wai 2002
 */
public class FFFPanel extends JPanel implements Runnable, FunModelListener {
    private FunModel model;

    private LinkEdges edgeView;

    private final Font funFont;
    private final Font superscriptFont;
    private final Font legendFont;
    private FontMetrics funFM = null;

    // could try to make this static, so that all share one image...
    private Image legendImage;

    // string constants
    private static final String SC_FIND_IN_LIST="Find_in_word_list";
    private static final String SC_SPROUT="Sprout";
    private static final String SC_COLLAPSE="Collapse";
    private static final String SC_REMOVE="Remove";
    private static final String SC_STOP_MOVEMENT="Stop Movement";
    private static final String SC_SHOW_LEGEND="Show Legend";
//     private static final String SC_SEE_DEFINITION="Keep_definition";
//     private static final String SC_ANCHOR_DOWN="Anchor_down";
//     private static final String SC_RELEASE="Release";

    //for right clicks on nodes
    private JPopupMenu popup;
    private JMenuItem find;
    private JMenuItem sprout;
    private JMenuItem collapse;
    private JMenuItem remove;

    //for right clicks on canvas
    private JPopupMenu panelPop;
    private JCheckBoxMenuItem stopMovement;
    private JCheckBoxMenuItem showLegend;

    //Following added to re-introduce spring movement
    private volatile Thread relaxer=null;
    public boolean stop;
    public boolean random;

    public double K2 = 300.0;             //repulsion between any u and v
    public double K3 = 150.0;             //repulsion between u e focusNodes and v
    public double Luv = 80.0;             //zero energy length of a spring
    public double C = 1/5.0;
    public int EW = 3;                    //default edge thickness ( 2*EW ) Global, so that dialog can change it
    private static final boolean SAND = true;  // used to stop small movements?
    private static final double SAND_FACTOR = 0.005;
    private static final int BORDERX = 40;
    private static final int BORDERY = 15;

//     private JMenuItem pin;
//     private JMenuItem release;
//     private JMenuItem openGloss;

    public FFFPanel(FunModel model, GraphPanel graph, boolean small) {
        super();

        // setLayout to null
        setLayout(null);

        this.edgeView = new LinkEdges(this);

        this.model = model;
        this.model.addFunModelListener(this);

        // graph is really both model and view, so store our "parent" in
        // our model just b/c we have access to our model, but not vice versa
        if (model.getGraph() == null) {
            model.setGraph(graph);
        }

        // apparently this won't work...
//      if (legendImage == null)
//          legendImage = getLegendImage();

        if (small) {
            funFont = FontProvider.LARGE_WORD_FONT;
            superscriptFont = FontProvider.SMALL_WORD_FONT;
            legendFont = FontProvider.SMALL_TEXT_FONT;
        } else {
            funFont = FontProvider.HUGE_WORD_FONT;
            superscriptFont = FontProvider.WORD_LIST_FONT;
            legendFont = FontProvider.TEXT_FONT;
        }
        showLegend = new
            JCheckBoxMenuItem(Helper.getTranslation(SC_SHOW_LEGEND), true);
    // listen for clicks to the canvas
        addMouseListener(new MouseAdapter() {
                //              public void mouseClicked(MouseEvent e) {
                //                  updateForCanvasClick(e);
                //              }
                public void mousePressed(MouseEvent e) {
                    if(e.isPopupTrigger()) {
                        showPanelOptions(e);
                    }
                    else {
                        updateForCanvasClick(e);
                    }
                }

            });
        WordNodeModel selModel = model.getSelected();
        if(selModel !=null) {
            setToolTipText(Helper.uniqueKeyToPrintableString(selModel.getUniqueKey()));
        }
        else {
            setToolTipText(null);
        }
    }

    public FunModel getModel() { return (model); }

    // implement FunModelListener

    public void funNodeModelCreated(FunModel changed, WordNodeModel
                                    newNodeModel, boolean isFocus) {
        addNode(newNodeModel, isFocus);

        if (isFocus)  // if not, wait until edge created to positionLink()
            positionNode(newNodeModel);
    }

    public void funEdgeModelCreated(FunModel changed, LinkEdgeModel
                                    newEdgeModel) {
        addEdge(newEdgeModel);

        positionLink(newEdgeModel);
    }

    public void funNodeModelRemoved(FunModel changed, WordNodeModel
                                    nodeModel) {
        removeNode(nodeModel);
        Vector nodeModels = model.getNodeModels();
        if(nodeModels == null || nodeModels.size() == 0) {
            model.getGraph().deleteEmptyFunPanel(this);
        }

    }

    public void funEdgeModelRemoved(FunModel changed, LinkEdgeModel
                                    edgeModel) {
        removeEdge(edgeModel);
    }

    public void funModelChanged(FunModel changed) {
    }


    // isFocus => sproutLinks and setAsSelected (change??)
    //
    // change to private??
    public void addNode(WordNodeModel nodeModel, boolean isFocus) {
        WordNode node = new WordNode(nodeModel, this);

        add(node, 0);  // add to front

        node.repaint();  // necess.??

        if (isFocus) {
            this.model.setSelected(nodeModel);
            setToolTipText(Helper.uniqueKeyToPrintableString(nodeModel.getUniqueKey()));
        }
    }

    public void addEdge(LinkEdgeModel edgeModel) {
        edgeView.addEdge(edgeModel);

        // note: -1 to add at end, but *edges not JComponents*...
        // would have Container.getComponentCount()-1 (maybe -2?) so
        // legend always at back...??
//      add(linkEdge, getComponentCount()-1);  // add to back (so nodes in
//                                             // front)
    }

    public void moveNodeToFront(WordNodeModel nodeModel) {
        Component[] components = getComponents();
        WordNode wnode = null;
        int i;
        boolean found = false;
        for (i = 0; i < components.length; i++) {
            if (components[i] instanceof WordNode) {
                if (((WordNode)components[i]).getModel().equals(nodeModel)) {
                    found = true;
                    wnode = (WordNode) components[i];
                    break;
                }
            }
        }
        if (found) {
            remove(wnode);
            add(wnode, 0);
            repaint();  // seems to help...
        }
    }

    public void removeNode(WordNodeModel nodeModel) {
        // hack? (no good way to get WordNode given node model...)
        Component[] components = getComponents();
        int i;
        boolean found = false;
        for (i = 0; i < components.length; i++) {
            if (components[i] instanceof WordNode) {
                if (((WordNode)components[i]).getModel().equals(nodeModel)) {
                    found = true;
                    break;
                }
            }
        }
        if (found) {
            remove(i);
            repaint();  // seems to help...
        }
    }

    public void removeEdge(LinkEdgeModel edgeModel) {
        edgeView.removeEdge(edgeModel);
    }

    // positions (focus) node in center of the screen
    public void positionNode(WordNodeModel nodeModel) {
        // right now method assumes we're node's parent (could check...)

        int panelWidth = getWidth();
        int panelHeight = getHeight();

        int totalDX = panelWidth/2 - nodeModel.getX();
        int totalDY = panelHeight/2 - nodeModel.getY();

        // don't animate this one and just call translate directly (so
        // done immediately on same thread), so that done before and
        // position set before sprout / position links
        nodeModel.translate(totalDX, totalDY, this);  // SwingUtilities.invokeLater?
    }

    private static final int ANGLE_VARIATION = 180;
    private static final int MIN_EDGE_DIST = 80;
    private static final int MAX_EDGE_DIST_VARIATION = 30;
    // so that MAX_EDGE_DIST = MIN_EDGE_DIST + MAX_EDGE_DIST_VARIATION;

    public void positionLink(LinkEdgeModel edgeModel) {
        if (Dbg.NEWFUN) Dbg.print("positioning link " + edgeModel);

        int dx, dy;

        // use color to determine as well (e.g., "similar" but less
        // intense goes on same side, further away, or something)??

        // need one end of link to be "center" / "anchor" / "focus" -
        // store from when adding (isFocus...), or just use getSelected,
        // or something else?  Right now just checking against getSelected...

        WordNodeModel anchorNode = model.getSelected();
        WordNodeModel movingNode = null;

        // have locals so don't have to keep requesting these...!!
        if (edgeModel.getNodeModel1().equals(model.getSelected())) {
            movingNode = edgeModel.getNodeModel2();
        } else if (edgeModel.getNodeModel2().equals(model.getSelected())) {
            movingNode = edgeModel.getNodeModel1();
        } else {
            anchorNode = null;
        }

        if (anchorNode == null) {
            if (Dbg.ERROR) Dbg.print("Trying to move non-selected nodes.  What to do?");
            // selected may be null
            String selected = (model.getSelected() == null) ? "null" : model.getSelected().getUniqueKey();
            if (Dbg.ERROR) Dbg.print("Selected: " +
              selected + ", " + "Link Nodes: " +
              edgeModel.getNodeModel1().getUniqueKey() + ", " +
              edgeModel.getNodeModel2().getUniqueKey());
            return;
        }

        if (Dbg.NEWFUN) Dbg.print("Anchor: " + anchorNode.getUniqueKey() +
                                  " Moving: " + movingNode.getUniqueKey());

        // random distance w/in range (any clever way to determine
        // range??), edge angle = sectionBaseAngle +
        // random.nextInt(sectionAngleSize), x dist from anchor =
        // dist.*cos(edge angle), y dist from anchor = dist.*sin(edge angle),
        // each linkType gets a section (== "slice")

        int linkTag = edgeModel.getTag();

        KRandom random = new KRandom();  // generate seed based on current time

        // divide circle / ring into "sections" or "slices"     // too squished...

//      int sectionBaseAngle = getLinkAngle(anchorNode.getUniqueKey(),
//      movingNode.getUniqueKey(), linkTag);

        dx = (movingNode.getX() - anchorNode.getX());
        if(dx == 0) dx++;
        int sectionBaseAngle = (int) Math.tan((movingNode.getY() -
                                         anchorNode.getY())/dx);
        if(movingNode.getX() - anchorNode.getX() < 0) sectionBaseAngle += 180;
        int edgeAngleDegrees;
        //achieve a little bit of variation.

        //if anchor node not the first node on panel
        if(anchorNode.getHub() != null) {
            edgeAngleDegrees = random.nextInt(ANGLE_VARIATION) - ANGLE_VARIATION/2;
        }
        else { //spread evenly
            sectionBaseAngle = getLinkAngle(anchorNode.getUniqueKey(),
                                            movingNode.getUniqueKey(),
                                            linkTag);
            edgeAngleDegrees = random.nextInt(20) - 10;;
        }
        edgeAngleDegrees+=sectionBaseAngle;

        //      int edgeDist = MIN_EDGE_DIST + random.nextInt(MAX_EDGE_DIST_VARIATION);
        int edgeDist = anchorNode.getWidth()/2 + movingNode.getWidth()/2 +
            random.nextInt(MAX_EDGE_DIST_VARIATION);


        // Math.toRadians is Java 1.2
        double edgeAngleRadians = ((double)edgeAngleDegrees) / 180.0;
        edgeAngleRadians *= Math.PI;

        // using getX and getY is a further approximation
        dx = anchorNode.getX() + anchorNode.getWidth()/2 -
            movingNode.getX() - movingNode.getWidth()/2;
        dx += (int)(((double)edgeDist) * Math.cos(edgeAngleRadians));
        dy = anchorNode.getY() + anchorNode.getHeight()/2 -
            movingNode.getY() - movingNode.getHeight()/2;
        dy += (int)(((double)edgeDist) * Math.sin(edgeAngleRadians));

        if (Dbg.NEWFUN) Dbg.print(//"angle size " + sectionAngleSize +
          " base angle " + sectionBaseAngle + " edgeAngle " +
          (float)edgeAngleRadians + " edgeDist " + edgeDist + " dx " + dx +
          " dy " + dy);

        animateMove(movingNode, dx, dy);
    }

    // could make this an option ("Animation Smoothness" or something)
    public static final int NUM_WAYPOINTS = 20;

    // hack: recursive (in order to repel nodes): base case is repelNodes
    public void animateMove(WordNodeModel nodeModel, int totalDX,
                                         int totalDY) {
        //      totalDX = keepNodeInBounds(nodeModel, totalDX, true);
        //      totalDY = keepNodeInBounds(nodeModel, totalDY, false);

        final int dx = totalDX / NUM_WAYPOINTS;
        final int dy = totalDY / NUM_WAYPOINTS;

        final WordNodeModel nModel = nodeModel;

        Thread animator = new Thread() {
                public void run() {
                    // "animate" towards somewhere near center of screen
                    nModel.setDrag(true); //disable spring effects
                    for (int i = 0; i < NUM_WAYPOINTS; i++) {
                        SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    nModel.translate(dx, dy, FFFPanel.this);
                                }
                            });

                        // pause for a bit
                        try {
                            //                      Dbg.print("anim sleeping");
                            Thread.sleep(150);
                            //                      Dbg.print("anim awake");
                        } catch (InterruptedException ie) {
                            break;  // interrupted
                        }
                        if (isInterrupted()) break;  // alt. interrupted
                    }
                    nModel.setDrag(false); //re-enable spring effects
                }
            };
        animator.start();

        // tried joining here sometimes, but just seemed to slow things down
//      if (join) {
//          try {
//              animator.join();
//          } catch (InterruptedException ignored) {
//          }
//      }
    }

    // Gravitational repulsion: adapted and simplified from old FunPanel

//     private synchronized void repelNodes() {
//      Vector nodeModels = model.getNodeModels();
//      int numNodes = nodeModels.size();
//      for (int m = 0; m < numNodes; m++) {
//          for (int n = 0; n < numNodes; n++) {
//              if (m == n)
//                  continue;
//              WordNodeModel node1 = (WordNodeModel)nodeModels.elementAt(m);
//              WordNodeModel node2 = (WordNodeModel)nodeModels.elementAt(n);
//              repelfrom(node1, node2);
//          }
//      }
//     }

    private static final double GRAV_C = 1.0/5.0;
    private static final double NODE_M = 22.0;

    private static final double GMM = GRAV_C * NODE_M * NODE_M;

    // can definitely clean this up and improve it a lot
//     private synchronized void repelFrom(WordNodeModel node1, WordNodeModel
//                                      node2) {
//      double repelForce = 0.0;  // (G*M*M)/(r*r)

//      // crude
//      Point[] midPoints = LinkEdgeModel.computeEndpoints(node1, node2);
//      double len = euclideanDistance(midPoints[0].x, midPoints[0].y,
//                                     midPoints[1].x, midPoints[1].y);
//      // w/ the numbers we're using, any smaller and movement would be
//      // very jerky

//      if (len < 10.0)
//          len = 10.0;

//      boolean translate = false;
//      int dx = 0;
//      int dy = 0;

//      // grav. repulsion betw. close nodes
//      // SHOULDN'T this repulsion be stronger for longer words? just
//      // using midpoints insufficient - longer words n pixels apart will
//      // overlap when shorter words n pixels apart won't overlap.

//      if (len < 100.0) {
//          repelForce = GMM / (len * len);

//          double xfactor = 20 * ((node1.getWidth()/2.0 + node2.getWidth()/2.0)
//              - Math.abs(midPoints[0].x - midPoints[1].x)) / node1.getWidth();
//          double yfactor = 15 * ((node1.getHeight()/2.0 + node2.getHeight()/2.0)
//              - Math.abs(midPoints[0].y - midPoints[1].y)) / node1.getHeight();

//          dx = (int)((midPoints[0].x - midPoints[1].x)*repelForce);
//          dy = (int)((midPoints[0].y - midPoints[1].y)*repelForce);

//          if(xfactor > 0 && yfactor > -10) {
//              dx *= 1 + xfactor;
//          }
//          if(yfactor > 0 && xfactor > -10) {
//              dy *= 1 + yfactor;
//          }

//          translate = true;
//      }
//      if (translate) {
//          // too intensive, and ugly (recursive...)
// //       animateMove(node1, dx, dy);

//          final int fdx = dx;
//          final int fdy = dy;
//          final WordNodeModel nModel = node1;
//          SwingUtilities.invokeLater(new Runnable() {
//                  public void run() {
//                      nModel.translate(fdx, fdy, FFFPanel.this);
//                  }
//              });
//      }
//     }

    private double euclideanDistance(int x1, int y1, int x2,
                                                  int y2) {
        // sqrt treats arg as double
        return (Math.sqrt( ((x1-x2)*(x1-x2)) + ((y1-y2)*(y1-y2)) ));
    }

    //Adjusts positions of word nodes on a window resize event
    public void shiftNodesOnResize() {
        if(model.getNodeModels() != null) {
            model.shiftNodesOnResize(this);

        }
    }

    public int keepNodeInBounds(WordNodeModel node1, int change,
                                                 boolean isX) {
        //      Dbg.print("got keepnodeinbounds");
        if(isX) {
            if (node1.getX() + change < 0) {
                change = 0 - node1.getX();
            } else if (node1.getX() + change> getWidth() - node1.getWidth()) {
                change = (getWidth() - node1.getWidth()) - node1.getX();
            }
        }
        else {
            if (node1.getY() + change < 0) {
                change = 0 - node1.getY();
            } else if (node1.getY() + change > getHeight() - node1.getHeight()) {
                change = (getHeight() - node1.getHeight()) - node1.getY();
            }
        }
        return change;
    }



    private int getLinkAngle(String uniqueKey, String otherKey, int tag) {
        // int numTotalTypes = model.getGraph().parent.dictInfo.getNumLinks();
        // int linkNum = 1;
        // should hopefully already be in cache
        DictionaryCache cache = model.getGraph().parent.cache;
        DictFields found = cache.getDictEntryLinks(uniqueKey);
        if (found == null || found.size() == 0)
            return (0);  // should only happen if dict is defective

        for (int i = 0, sz = found.size(); i < sz; i++) {
            DictField df = found.get(i);
            if (df.uniqueKey.equals(otherKey)) {
                return (int) (360.0/found.size() * i);
            }
        }
        //      Dbg.print("GetLinkAngle returning 0!!!\n\n\n");
        return 0;
    }



    public Font getFunFont() { return (funFont); }
    public Font getSuperscriptFont() { return (superscriptFont); }
    public Font getLegendFont() { return (legendFont); }

    public FontMetrics getFunFontMetrics(Graphics g, Font f) {
        if (funFM == null)
            funFM = g.getFontMetrics(f);

        return (funFM);
    }

    // largely from old FunPanel

    //static constants for legend (rename some more of these!!)
    private static final int legendWidth = 200;
    private static final int legendHeight = 250;
    private static final int hspace = 4;
    private static final int vspace = 2;
    private static final int boxX = 5;
    private static final int boxW = 50;
    private static final int textX = boxX + boxW + hspace;

    private Image getLegendImage() {
        if (legendImage == null) {
            legendImage = createImage(legendWidth, legendHeight);
            Graphics imageGraphic = legendImage.getGraphics();

            imageGraphic.setColor(getBackground());
            imageGraphic.fillRect(0, 0, legendWidth, legendHeight);

            imageGraphic.setFont(legendFont);
            FontMetrics fm = imageGraphic.getFontMetrics(legendFont);

            int rowH = fm.getHeight();
            int boxH = rowH / 2;
            int maxText = 0;
            int rowY = vspace;

            DictionaryInfo dictInfo = Kirrkirr.dictInfo;
            int numLinks = dictInfo.getNumLinks();
            for (int i = 0; i < numLinks; i++) {
                String desc = Helper.getTranslation(dictInfo.getLinkName(i));
                imageGraphic.setColor(dictInfo.getLinkColor(i));
                imageGraphic.fillRect(boxX, rowY + (rowH - boxH)/2, boxW, boxH);
                imageGraphic.setColor(Color.black);
                imageGraphic.drawString(desc, textX, rowY + (3 * rowH) / 4);
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

    // override so edgeView can draw edges...
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if(showLegend.isSelected())
            g.drawImage(getLegendImage(), 3, 3, null);

        Rectangle clip = g.getClipBounds();  // may be null

        edgeView.redrawRegion(g, clip);
    }

    private void showPanelOptions(MouseEvent e) {
        if(panelPop == null) {
            panelPop = new JPopupMenu();
            panelPop.add(stopMovement = new
                         JCheckBoxMenuItem(Helper.getTranslation(SC_STOP_MOVEMENT), false));
            panelPop.add(showLegend);



            stopMovement.setHorizontalTextPosition(SwingConstants.RIGHT);
            showLegend.setHorizontalTextPosition(SwingConstants.RIGHT);


            PopupActionListener panelOpAL = new PopupActionListener();
            stopMovement.addActionListener(panelOpAL);
            showLegend.addActionListener(panelOpAL);

        }
        panelPop.show(this, e.getX(), e.getY());
    }

    public void showPopup(int x, int y) {
        if (Dbg.NEWFUN) Dbg.print("Pop goes the weasel!");

        if (popup == null) {
            // initialize the popup menu
            popup = new JPopupMenu();
            popup.add(find = new JMenuItem(Helper.getTranslation(SC_FIND_IN_LIST), RelFile.makeImageIcon("search.gif",false)));
            popup.add(sprout = new JMenuItem(Helper.getTranslation(SC_SPROUT), RelFile.makeImageIcon("sprout.gif",false)));
            popup.add(collapse = new JMenuItem(Helper.getTranslation(SC_COLLAPSE), RelFile.makeImageIcon("collapse.gif",false)));
            popup.add(remove = new JMenuItem(Helper.getTranslation(SC_REMOVE), RelFile.makeImageIcon("cut.gif",false)));
//          popup.add(openGloss = new JMenuItem(Helper.getTranslation(SC_SEE_DEFINITION),
//                                              RelFile.makeImageIcon("notes.gif",false)));
//          popup.add(pin = new JMenuItem(Helper.getTranslation(SC_ANCHOR_DOWN),
//                                        RelFile.makeImageIcon("anchor.gif",false)));
//          popup.add(release = new JMenuItem(Helper.getTranslation(SC_RELEASE),
//                                            RelFile.makeImageIcon("release.gif",false)));
            find.setHorizontalTextPosition(SwingConstants.RIGHT);
            sprout.setHorizontalTextPosition(SwingConstants.RIGHT);
            collapse.setHorizontalTextPosition(SwingConstants.RIGHT);
            remove.setHorizontalTextPosition(SwingConstants.RIGHT);
//          openGloss.setHorizontalTextPosition(SwingConstants.RIGHT);
//          pin.setHorizontalTextPosition(SwingConstants.RIGHT);
//          release.setHorizontalTextPosition(SwingConstants.RIGHT);

            PopupActionListener popupAL = new PopupActionListener();
            find.addActionListener(popupAL);
            sprout.addActionListener(popupAL);
            collapse.addActionListener(popupAL);
            remove.addActionListener(popupAL);
//          openGloss.addActionListener(this);
//          pin.addActionListener(this);
//          release.addActionListener(this);
        }
//      pin.setEnabled(! pick.pinned);
//      release.setEnabled(pick.pinned);
//      openGloss.setEnabled(pick.exact != null);
//      find.setEnabled(pick.exact != null);
//      sprout.setEnabled(pick.exact != null);
//      Dbg.print("showing  popup");
        popup.show(this, x, y);
    }

    private void updateForCanvasClick(MouseEvent e) {
        // actually, this only gets called if it is indeed the canvas (and
        // not a node, for example, which has a listener that handles its
        // own mouse events) that was clicked.  but it doesn't do any harm
        // to check for this condition anyways...
        if (e.getComponent() == this) {
            String ukey = null;
            //to retain selection when tabbing between different word
            //sprouts, set selected to null only if top panel
            if(model.getGraph().isTopFFFPanel(this)) {
                setToolTipText(null);
                model.setSelected(null);  // no node selected
            }
            else {
                WordNodeModel wmodel = model.getSelected();
                if(wmodel != null) ukey = wmodel.getUniqueKey();
            }
            //fire this so that when different layer is selected, the
            //selected word will be highlighted in word list
            model.getGraph().selectedNodeChanged(ukey);

            model.getGraph().reshuffle(this);  // bring us to the front...
        }
    }

    // inner class PopupActionListener

    public class PopupActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            // how do we determine which node to apply action to - is
            // getModel().getSelected() ok??
            Object src = e.getSource();
            if (src == find) {
                if (Dbg.NEWFUN) Dbg.print("Find clicked");

                // handle sending to other panels in graph
                getModel().getGraph().selectedNodeChanged(getModel().getSelected().getUniqueKey());
            } else if (src == sprout) {
                if (Dbg.NEWFUN) Dbg.print("Sprout clicked");

                getModel().getSelected().sproutLinks();
            } else if (src == collapse) {
                if (Dbg.NEWFUN) Dbg.print("Collapse clicked");

                // collapse should be implemented somewhat differently
                // than remove: seems like collapse might be better done
                // in node model than fun model...
                getModel().getSelected().collapseLinks();
            } else if (src == remove) {
                if (Dbg.NEWFUN) Dbg.print("Remove clicked");

                getModel().removeSelectedWord();
            } else if (src == stopMovement) {
                if (Dbg.NEWFUN) Dbg.print("Stop Movement clicked");
                if(((JCheckBoxMenuItem)src).isSelected())
                    FFFPanel.this.stop();
                else
                    FFFPanel.this.start();
            } else if (src == showLegend) {
                if (Dbg.NEWFUN) Dbg.print("Show Legend toggled");
                FFFPanel.this.repaint();
            }

        }

    }

    // main - just for testing
    static public void main(String[] args) {
        JFrame frame = new JFrame("Test Fun");
        FunModel funModel = new FunModel();
        FFFPanel panel = new FFFPanel(funModel, null, false);
        frame.setContentPane(panel);

        frame.pack();
        frame.setVisible(true);

        frame.setSize(new Dimension(500, 500));  //??

        frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });

        //temp

//      WordNodeModel nodeModel = new WordNodeModel("Hello!", 13, 18, funModel, true);
//      panel.addNode(nodeModel, true);

        funModel.createNode("Goodbye", false, null);


    }

    //COMBINING NEW/OLD?

    public void start() {
      if(relaxer != null) return; //already running
      if(userStop()) return; //user stopped
        relaxer = new Thread(this);
        // Lower the priority of the network displays
        relaxer.setPriority((Thread.NORM_PRIORITY +
                             Thread.MIN_PRIORITY) / 2);
        try {
            relaxer.setDaemon(true);    // a Daemon thread -- stops on exit
        } catch (SecurityException se) {
        }
        if (Dbg.NEWFUN) Dbg.print("FFFPanel starting");
        relaxer.start();
    }

    public void stop() {

        if (relaxer!=null) {
            if (Dbg.NEWFUN) Dbg.print("FFFPanel stopping");
            relaxer.interrupt();
            relaxer = null;
        }
    }

    public void run() {
        try {
            while (!Thread.interrupted()) {
                if(!stop) {
                    try {
                        spring();
                    } catch (NullPointerException e) { // nodes fiddled
                        if (Dbg.ERROR) {
                            Dbg.print("FunPanel: run NullPtr");
                            e.printStackTrace();
                        }
                    }
                    if (random && (Math.random() < 0.13)) {
                        Vector nModels = model.getNodeModels();
                        WordNodeModel n = (WordNodeModel) nModels.elementAt((int)(Math.random() * nModels.size()));
                        //                      if (!n.fixed) {
                        n.dx += 100*Math.random() - 50;
                        n.dy += 100*Math.random() - 50;
                            //                  }
                    }
                }
                repaint();  // even if stopped (in case changed)-- threadsafe
                //              Dbg.print("relaxer sleeping");
                Thread.sleep(150);
                //              Dbg.print("relaxer awake");
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

    private static final int OVERLAP_PUSH = 3;

    private void repelfrom(WordNodeModel n1, WordNodeModel n2) {
        int x1, x2, y1, y2; //for speed, put in locals
        int w1, w2, h1, h2;
        //      Dbg.print("got repel from");
        x1 = n1.getX();
        x2 = n2.getX();
        y1 = n1.getY();
        y2 = n2.getY();
        w1 = n1.getWidth();
        w2 = n2.getWidth();
        h1 = n1.getHeight();
        h2 = n2.getHeight();

        Point[] midPoints = LinkEdgeModel.computeEndpoints(n1, n2);

        repelPoints(n1, n2, midPoints);

        int maxW, maxH;
        maxW = Math.max(w1, w2);
        maxH = Math.max(h1, h2);
        if(n2.isDummy()) return; //no concept of overlap with dummy

        //overlap exists - special force
        if(Math.abs(midPoints[0].x - midPoints[1].x) < maxW  &&
           Math.abs(midPoints[0].y - midPoints[1].y) < maxH ) {
            if(midPoints[0].x < midPoints[1].x) {
                n1.dx -= (1+ OVERLAP_PUSH);
                n2.dx += (1+ OVERLAP_PUSH);
            }
            else {
                n1.dx += (1 + OVERLAP_PUSH);
                n2.dx -= (1 + OVERLAP_PUSH);
            }
            if(midPoints[0].y < midPoints[1].y) {
                n1.dy -= OVERLAP_PUSH;
                n2.dy += OVERLAP_PUSH;
            }
            else {
                n1.dy += OVERLAP_PUSH;
                n2.dy -= OVERLAP_PUSH;
            }
        }
    }

    private void repelPoints(WordNodeModel n1, WordNodeModel n2, Point[] points) {
        double len = euclideanDistance(points[0].x, points[0].y,
                                       points[1].x, points[1].y);

        if (len < 20)     // especially beware the case len == 0.0
            len = 20;     // larger threshhold helps smooth repulsion

        double c2 = C / (len * len);

        //try to separate the sprouts from different hub nodes

        // cdm: combine these for the moment
        //if(len < 150.0) {
        // Newtonian gravitational force g_n1_n2
        //temp = K2 * c2;
        //n1.dx += (n1.x - n2.x) * temp;
        //n1.dy += (n1.y - n2.y) * temp;
        //}

        if (len < 150.0) { //optimization
            double temp = (K3 + K2) * c2;
            //      Dbg.print("sum of lengths: " + (n1.getWidth() + n2.getWidth()));

            double dx, dy;
            if(n1.getHub() != n2.getHub()) //keep local sprouts separate
                temp *= 2;

            dx = ((points[0].x - points[1].x)*temp);
            dy = ((points[0].y - points[1].y)*temp);
            //only apply if not dummy
            if(!n1.isDummy()) {
                n1.dx +=  dx;
                n1.dy +=  dy;
            }
            if(!n2.isDummy()) {
                n2.dx -=  dx;
                n2.dy -=  dy;
            }
        }
    }

    private void spring() {
        WordNodeModel wm1, wm2;
        try {
            double K1 = 0.0;        //stiffness of spring (depends on if one end is a focus node)
            Vector edges = model.getEdgeModels();
            boolean wm1sprout, wm2sprout;
            double templuv = Luv;
            if(edges != null) {
                // Hookes Law - spring forces within each edge
                for (int i = 0 ; i < edges.size(); i++) {
                    LinkEdgeModel e = (LinkEdgeModel) edges.elementAt(i);
                    wm1sprout = model.isSprouted(e.getNodeModel1());
                    wm2sprout = model.isSprouted(e.getNodeModel2());

                    if(wm1sprout || wm2sprout)
                        K1 = -1/5.0;
                    else
                        K1 = -1/30.0;

                    if(wm1sprout && wm2sprout) {
                        //longer spring to keep sprouted nodes separated
                        templuv = Luv * 2;
                    }
                    else {
                        templuv = Luv;
                    }
                    Point[] ends = e.computeEndpoints();
                    double len = euclideanDistance(ends[0].x, ends[0].y,
                                                   ends[1].x, ends[1].y);
                    if((int)len == 0) {
                        continue;
                    }

                    double temp =  K1 * (len - templuv) *  C / len;
                    wm1 = e.getNodeModel1();
                    wm2 = e.getNodeModel2();

                    double dx = (ends[0].x - ends[1].x) * temp;
                    double dy = (ends[0].y - ends[1].y) * temp;
                    wm1.dx += dx;
                    wm1.dy += dy;
                    wm2.dx -= dx;
                    wm2.dy -= dy;

                }
            }

            Vector nodeModels = model.getNodeModels();
            // Newtonian forces between nodes
            if(nodeModels == null) return;

            for (int i = 0 ; i < nodeModels.size() ; i++) {
                wm1 = (WordNodeModel) nodeModels.elementAt(i);
                for (int j = i+1 ; j < nodeModels.size() ; j++) {

                    //repels both (since should be symmetric)
                    repelfrom(wm1, (WordNodeModel)nodeModels.elementAt(j));
                }
            }

            // Newtonian forces from boundary - now done by pseudo-nodes which
            // are placed on the boundaries horizontally and vertically next
            // to each node.  Better than having lots of fenceposts [cdm].

            Dimension d = getSize();
            WordNodeModel ndummy;


            for (int i = 0 ; i < nodeModels.size(); i++) {
                wm1 = (WordNodeModel) nodeModels.elementAt(i);
                int x = wm1.getX();
                int y = wm1.getY();

                ndummy = new WordNodeModel(x, 0, wm1.getWidth(),
                                           wm1.getHeight(), true);
                repelfrom(wm1, ndummy);
                ndummy.translate(0, d.height, null);
                repelfrom(wm1, ndummy);
                ndummy.translate(-x, y - d.height, null);
                repelfrom(wm1, ndummy);
                ndummy.translate(d.width, 0, null);
                repelfrom(wm1, ndummy);

                // Regardless force nodes to stay roughly on screen
                // cdm June 2000: remove extra clauses, which
                // stopped nodes sticking when resize panel.

                if (x < BORDERX && wm1.dx < 2.0)
                    wm1.dx = 2.0;
                else if (x > (d.width - BORDERX) && wm1.dx > -2.0)
                    wm1.dx = -2.0;

                if (y < BORDERY && wm1.dy < 2.0)
                    wm1.dy = 2.0;
                else if (y > (d.height - BORDERY) && wm1.dy > -2.0)
                    wm1.dy = -2.0;
            }

            //finalise the new x & y values of each node
            for (int i = 0 ; i < nodeModels.size() ; i++) {
                wm1 = (WordNodeModel) nodeModels.elementAt(i);

                //              if (!n.fixed && !n.pinned) {
                if(SAND) {
                    if(Math.abs(wm1.dx)< SAND_FACTOR) {
                        wm1.dx = 0.0;
                    }
                    if(Math.abs(wm1.dy)< SAND_FACTOR) {
                        wm1.dy = 0.0;
                    }
                }
                if(!wm1.beingDragged())
                    wm1.translate((int) Math.max(-5, Math.min(5, wm1.dx)),
                                  (int) Math.max(-5, Math.min(5, wm1.dy)), this);
                //              }

                wm1.dx = 0;
                wm1.dy = 0;
            }
        } catch (NullPointerException e) { // nodes fiddled
            if (Dbg.ERROR) {
                Dbg.print("FunPanel: spring NullPtr");
                e.printStackTrace();
            }
        }
    } //spring

    private boolean userStop() {
        if(stopMovement==null) return false;
        return stopMovement.isSelected();
    }

}

