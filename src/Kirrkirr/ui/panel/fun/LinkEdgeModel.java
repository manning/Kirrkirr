package Kirrkirr.ui.panel.fun;

import java.awt.*;
import java.util.*;

import Kirrkirr.Kirrkirr;
import Kirrkirr.dictionary.DictField;
import Kirrkirr.dictionary.DictFields;
import Kirrkirr.dictionary.DictionaryCache;
import Kirrkirr.ui.panel.GraphPanel;


/** LinkEdgeModel.java - Represents an edge in the FunPanel network
 * visualization.
 *
 * @author Conrad Wai 2002
 */
public class LinkEdgeModel implements WordNodeModelListener {

    // should we acct. for dir. (to/from)?
    private final WordNodeModel word1;
    private final WordNodeModel word2;
    private final int tag;  // link "types" are dictionary-specific now...
    private Color color;  // just store so don't have to look up whenever
                          // we draw
    // it's ok for us to store the link type tag, since these are
    // generically specified now in conv.xml

    // for LinkEdgeModelListeners
    // transient means shouldn't be saved during serialization...
    transient private Vector listeners;

    public static final int EDGE_COLOR_CHANGED = 1;
    public static final int EDGE_POINTS_CHANGED = 2;
    public static final int EDGE_CHANGED = 3;

    // a little unclean, but we need to repaint old + new region.  need to
    // be able to get old rectangle somehow - could put old and new models
    // in node change stuff, but right now just store "old" (after all,
    // why calc. again?) and pass in new, as w/mouse points for
    // WordNodes...
    private Rectangle lastDrawnRect;

    // as with node models, pass funModel in so can instantiate w/o
    // calling FunModel.createEdge and still have fun panel/model operate
    // properly.  essentially, this tells funModel of its existence, so
    // that it can fire an edge created notification...
    // also pass funModel in so we can getGraph in order to access kk
    // (parent) stuff, for link tag and color
    // (alternatively, can use Kirrkirr's "static"-ness ...)
    public LinkEdgeModel(WordNodeModel word1, WordNodeModel word2, FunModel funModel) {
        this.word1 = word1;
        this.word2 = word2;

        // this stuff is probably unnecessarily circuitous
        GraphPanel graph = funModel.getGraph();
        Kirrkirr kk = (graph != null) ? graph.parent : Kirrkirr.kk;

        this.tag = determineTagForLink(kk.cache, word1, word2);
        if (this.tag < 0) {
            // come up with better way to represent??
            this.color = Color.black;
        } else {
            this.color = kk.dictInfo.getLinkColor(this.tag);
        }

        word1.addNodeListener(this);
        word2.addNodeListener(this);

        // just for convenience...
        lastDrawnRect = new Rectangle();

        funModel.edgeModelCreated(this);
    }


    /** Called in ctor to help set tag.
     *  @return -ve if unsuccessful
     */
    private static int determineTagForLink(DictionaryCache cache, WordNodeModel
                                    word1, WordNodeModel word2) {
        // Above is true in only one direction
        // since these entries should be in the cache by now,
        // getDictEntryLinks() should be a fast op. now
        DictFields links = null;
        String linkToWord = null;  // unique key
        if (word1.isLinkedTo(word2)) {
            links = cache.getDictEntryLinks(word1.getUniqueKey());
            linkToWord = word2.getUniqueKey();
        } else if (word2.isLinkedTo(word1)) {  // dict links may not be
                                               // "reciprocal"
            links = cache.getDictEntryLinks(word2.getUniqueKey());
            linkToWord = word1.getUniqueKey();
        } else {
            // found and linkToWord remain null
        }

        if (links == null || linkToWord == null)
            return (-1);

        int numLinks = links.size();
        for (int i = 0; i < numLinks; i++) {
            // df.word() (== df.value) is not a unique key, but rather
            // just the display value - use Helper to make unique key...
            DictField df = links.get(i);

            String linkKey = df.uniqueKey;

            if (linkKey.equals(linkToWord)) {  // found match
                return (df.tag);
            }
        }
        return -1;
    }


    /** These points will be in node model's parent coord system. */
    public Point[] computeEndpoints() {
        return (computeEndpoints(word1, word2));
    }

    // possibly useful to have static method where node models passed in...
    public static Point[] computeEndpoints(WordNodeModel nodeModel1,
                                           WordNodeModel nodeModel2) {
        int x1 = nodeModel1.getX();
        int x2 = nodeModel2.getX();
        int y1 = nodeModel1.getY();
        int y2 = nodeModel2.getY();

        int w1 = nodeModel1.getWidth();
        int w2 = nodeModel2.getWidth();
        int h1 = nodeModel1.getHeight();
        int h2 = nodeModel2.getHeight();

        // get "best" points...
        // improve!!
        // there doesn't seem to be any Rectangle method that computes
        // this, so do manually using min/max??
        // temp
        int endX1 = x1 + w1/2;
        int endX2 = x2 + w2/2;
        int endY1 = y1 + h1/2;
        int endY2 = y2 + h2/2;

        Point[] endpoints = new Point[2];
        endpoints[0] = new Point(endX1, endY1);
        endpoints[1] = new Point(endX2, endY2);

        return (endpoints);
    }

//     // throws an IllegalArgumentException (change to some other
//     // RuntimeException??) if pointNum is not 1 or 2
//     public Point getEndpoint(int pointNum) {
//      Point endpoint = null;

//      if (pointNum == 1) {
//          endpoint = calculateLinkPoint(word1);
//      } else if (pointNum == 2) {
//          endpoint = calculateLinkPoint(word2);
//      } else {
//          throw new IllegalArgumentException("Invalid endpoint number");
//      }
//      return (endpoint);
//     }

//     // bundles the two endpoints in an array to return
//     public Point[] getEndpoints() {
//      Point[] endpoints = new Point[2];
//      endpoints[0] = getEndpoint(1);
//      endpoints[1] = getEndpoint(2);

//      return (endpoints);
//     }

    public Rectangle getLastDrawnRect() { return (lastDrawnRect); }

    public void setLastDrawnRect(Rectangle r) {
        lastDrawnRect = r;  // more??
    }

    public WordNodeModel getNodeModel1() { return (word1); }
    public WordNodeModel getNodeModel2() { return (word2); }

    public int getTag() { return (tag); }

    public Color getColor() { return (color); }

    public void setColor(Color newColor) {
        if (!color.equals(newColor)) {
            color = newColor;
            fireChanged(EDGE_COLOR_CHANGED);
        }
    }

    // implements WordNodeModelListener

    // note that we don't even care which (of the two) models is the one
    // that changed...

    // maybe should create an adapter for this listener!!
    public void nodeColorChanged(WordNodeModel changed) { }
    public void nodeLocationChanged(WordNodeModel changed) { }
    public void nodeSizeChanged(WordNodeModel changed) { }
    public void nodeChanged(WordNodeModel changed, int changeType) { }
    // this is the only one we really need to implement...
    public void nodeBoundsChanged(WordNodeModel changed) {
        fireChanged(EDGE_POINTS_CHANGED);
    }

    // for LinkEdgeModelListener

    public void addEdgeListener(LinkEdgeModelListener listener) {
        // null allowed for serialization...
        if (listeners == null) {  // lazy eval. (make sure to chk. for
                                  // null in other methods)...
            listeners = new Vector();
        }

        listeners.addElement(listener);
    }

    public void removeEdgeListener(LinkEdgeModelListener listener) {
        if (listeners == null) return;

        int index = listeners.indexOf(listener);

        if (index != -1)
            listeners.removeElementAt(index);
    }

    // helper for firing changes to listeners...

    // note how we do this (when we fire...) - fire multiple times for
    // single event.  if listener wants to "handle" this and receive just
    // one notification per event, should only really implement
    // edgeChanged and determine for itself what ought to be done;
    // conversely, there is no need at this point to really implement
    // edgeChanged if the specific notifications have been implemented...
    private void fireChanged(int changeType) {
        if (listeners == null) return;

        int numListeners = listeners.size();
        for (int i = 0; i < numListeners; i++) {
            LinkEdgeModelListener listener = (LinkEdgeModelListener)listeners.elementAt(i);
            listener.edgeChanged(this, changeType);  // always fire generic...

            switch (changeType) {
              case EDGE_COLOR_CHANGED:
                  listener.edgeColorChanged(this);
                  break;
              case EDGE_POINTS_CHANGED:
                  listener.edgePointsChanged(this);
                  break;
              default:
                  break;
            }
        }
    }

}

