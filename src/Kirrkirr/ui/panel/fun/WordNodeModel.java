package Kirrkirr.ui.panel.fun;

import java.awt.*;
import java.util.*;

import Kirrkirr.dictionary.DictField;
import Kirrkirr.dictionary.DictionaryCache;
import Kirrkirr.ui.panel.GraphPanel;
import Kirrkirr.util.Dbg;
import Kirrkirr.util.Helper;


/** WordNodeModel.java - Represents a node in the FunPanel network
 *  visualization.
 *
 *  @author Conrad Wai 2002
 *  @author Christopher Manning
 */
public class WordNodeModel {

    private WordNodeModel hub;

    private String uniqueKey;  // in the form "walrus" or "home@3" (i.e.,
                               // displayValue@uniquifier - uniquifier if
                               // necessary)
    public double dx, dy;  // manipulated by other classes for model (yuk!)
    // manip. x, y to move around...
    private int x, y;  // top left of node (in parent's coord. sys.)
    private int width, height;  // need for setBounds() - this could
                                // potentially be final
    // width and height represent non-gloss, no-uniquifier value - account for
    // these when painting, if needed

    private boolean drag;

    // this is a minor hack: does not have relevant size (w, h)
    // info. until a WordNode sets it, since a Graphics context is needed
    // in order to retrieve font metrics and get string width...
    private boolean hasSize;

    // sprouting:
    // - each node model has 2 collections storing string uniquekeys (one
    // for all links, one for expanded links) - after all, why have models
    // if not needed yet (and more overhead)
    // - node models can spawn related words' models
    // - when a sprout is requested (often via mouse event), node gets its
    // model and tells it to instantiate more word models
    //   - can just store funModel in ivar (always pass in to ctor) so we
    //   can just call createNode (nodes not instantiated in many places
    //   at all...)
    // - note: if spawned with isFocus false, already connected to someone
    // else (who spawned us)... (so should add to expanded, right? - can
    // we somehow just listen for this stuff? or just have methods can use
    // to control this, since (even selective) collapsing and expanding
    // only happen rarely...) - if have controlling methods, don't need to
    // pass in word to ctor, since creator can just call method and see
    // already added - no, wait, maybe passing in to ctor cleaner (b/c
    // then can just always chk. against own expanded Collection to see if
    // need to add) - but actually arbitrary number of links may already
    // exist, so always will need to chk. against FunModel master list of
    // all node models currently in panel...

    private FunModel funModel;

    // these two Vectors store String uniqueKeys we don't just use
    // DictFields b/c it should be rewritten at some point anyways (see
    // linkWords note in generateLinkWords())
    // cdm 2003: do we really need both??  I think not
    private Vector linkWords;  // = null; // maybe be null if not coomputed
    private Vector expandedWords;

    // for WordNodeModelListeners
    // transient means shouldn't be saved during serialization...
    transient private Vector listeners;

    public static final int NODE_COLOR_CHANGED = 1;
    public static final int NODE_LOCATION_CHANGED = 2;
    public static final int NODE_SIZE_CHANGED = 3;
    public static final int NODE_BOUNDS_CHANGED = 4;
    public static final int NODE_LINK_CREATED = 5;
    public static final int NODE_CHANGED = 6;

    // the width and height don't mean anything...
    //
    // funModel is passed in for a number of reasons.  for example,
    // so can instantiate without calling FunModel.createNode, and still
    // have fun panel/model operate properly - essentially, this tells
    // funModel of its existence (via funModel.nodeModelCreated()), so
    // that it can fire a node created notification, etc.
    // we also need funModel when we sprout links, for a related reason...
    public WordNodeModel(String uniqueKey, int x, int y,
                         FunModel funModel, boolean isFocus) {
        this(uniqueKey, x, y, 200, 100, funModel);

        funModel.nodeModelCreated(this, isFocus);
    }

    public WordNodeModel(int x, int y, int width, int height, boolean isDummy) {
        this.x = x;
        this.y = y;
        if (isDummy) {
            this.width = width;
            this.height = height;
            uniqueKey = "!!!";
        }
    }


    /** Create a WordNodeModel.
     *  @param uniqueKey  A unique key for the word.  This includes an
     *      indication if this isn't a word in the dictionary (if known).
     */
    private WordNodeModel(String uniqueKey, int x, int y,
                          int width, int height, FunModel funModel) {
        this.uniqueKey = uniqueKey;

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.funModel = funModel;

        this.hasSize = false;

        /* XXXXX
        // check for word in dict: if does not exist, gray out word
        // can get Kirrkirr and cache from FunModel to GraphPanel (?)
        // (alternatively, can use Kirrkirr's "static"-ness ...)
        GraphPanel graph = funModel.getGraph();
        if (graph != null) {
            DictionaryCache cache = graph.parent.cache;
            DictEntry de = cache.getIndexEntry(uniqueKey);

            if (de == null) {
                this.isInDict = false;
            } else {
                this.isInDict = true;
            }
        }
        XXXXX */

        // lookup and get links for this uniqueKey
        // generateLinkWords();
    }


    // where to put synchronization in WordNode / WordNodeModel...??
    public void translate(int dx, int dy, FFFPanel parent) {

        //      Dbg.print("got translate");
        if(parent != null) {
            dx = parent.keepNodeInBounds(this, dx, true);
            dy = parent.keepNodeInBounds(this, dy, false);
        }
        x += dx;
        y += dy;

        fireChanged(NODE_LOCATION_CHANGED);
    }


    // accessors

    public String getUniqueKey() { return (uniqueKey); }
    public boolean isDummy() { return uniqueKey.equals("!!!"); }
    public int getX() { return (x); }
    public int getY() { return (y); }
    public int getWidth() { return (width); }
    public int getHeight() { return (height); }

    public Color getColor() {
        if (this == funModel.getSelectedNodeModel()) {
            return funModel.getSelectedNodeColor();
        } else {
            return funModel.getDefaultNodeColor();
        }
    }

    public void updateColor() {
        fireChanged(NODE_COLOR_CHANGED);
    }

    public Color getNodeTextColor() {
        if (Helper.isResolved(uniqueKey)) {
            return Color.black;
        } else {
            return Color.gray;
        }
    }

    public boolean hasSize() { return (hasSize); }

    public void setSize(int w, int h) {
        hasSize = true;
        if (width != w || height != h) {
            width = w;
            height = h;
            fireChanged(NODE_SIZE_CHANGED);
        }
    }

    public boolean isInDict() { return Helper.isResolved(uniqueKey); }


    /** This should only happen lazily, when links are needed.
     *  It is a fairly time-consuming operation.
     */
    private void generateLinkWords() {
        // can get Kirrkirr and cache from FunModel to GraphPanel (?)
        // (alternatively, can use Kirrkirr's "static"-ness ...)
        GraphPanel graph = funModel.getGraph();
        if (graph != null) {
            // as in old graph panel...
            DictionaryCache cache = graph.parent.cache;
            linkWords = cache.getLinksForWord(uniqueKey);
            if (Dbg.NEWFUN) Dbg.print(uniqueKey + " links: " + linkWords);
            if (linkWords == null) {
                uniqueKey = Helper.makeKeyUnknown(uniqueKey);
                linkWords = new Vector();  // put in empty one
            }
        } else if (Dbg.NEWFUN) {
            Dbg.print("generateLinkWords for " + uniqueKey +
                      ": graph is null!  Bad??");
        }
    }


    public void sproutLinks() {
        if (linkWords == null) {
            generateLinkWords();
        }
        if (Dbg.NEWFUN) {
            Dbg.print("sprouting " + uniqueKey + " using " + linkWords);
        }
        int numLinks = linkWords.size();
        for (int i = 0; i < numLinks; i++) {
            DictField df = (DictField) linkWords.elementAt(i);
            String uniqueKey = df.uniqueKey;
            createLink(uniqueKey);
        }
        if (numLinks > 0)
            funModel.sprout(this);
    }


    private void createLink(String uniqueKey) {
        // conceivably, links may not be reciprocal in dict./lang.
        if (linkWords == null) {
            generateLinkWords();
        }
        if (!linkWords.contains(uniqueKey)) {
            linkWords.addElement(uniqueKey);
        }

        // create models / nodes through FunModel createNode if they don't
        // exist, with sproutLinks == false
        // obtainXXX checks that this node/edge doesn't already exist - if
        // it does, just returns existing one
        WordNodeModel relatedWordModel = funModel.obtainNode(uniqueKey,
                                                             false, this);
        LinkEdgeModel linkEdgeModel = funModel.obtainEdge(this, relatedWordModel);

        if (expandedWords == null)
            expandedWords = new Vector();

        expandedWords.addElement(relatedWordModel.getUniqueKey());

        // a little hack...
        relatedWordModel.addExpandedWordToList(this.getUniqueKey());

        // would anyone find this useful?? - obtainXXX should already take
        // care of most things...
//      fireChanged(NODE_LINK_CREATED);
    }

    // can make private, since we are allowed to call private methods of
    // another instance of same class (in createLink)...
    private void addExpandedWordToList(String uniqueKey) {
        if (expandedWords == null)
            expandedWords = new Vector();
        if (!expandedWords.contains(uniqueKey))
            expandedWords.addElement(uniqueKey);
    }

    // kind of hack: called in funModel when removing a link.  should have
    // a listener for this sort of stuff instead
    public void removeExpandedWordFromList(String uniqueKey) {
        if (expandedWords == null)
            return;
        expandedWords.removeElement(uniqueKey);  // ok if not in there...
    }

    public int getNumExpandedLinks() {
        return ((expandedWords != null) ? expandedWords.size() : 0);
    }

    public boolean isLinkedTo(WordNodeModel potentialLinkModel) {
        return (isLinkedTo(potentialLinkModel.getUniqueKey()));
    }
    // potentialLinkWord is treated as a unique key
    public boolean isLinkedTo(String potentialLinkWord) {
        if (linkWords == null) {
            generateLinkWords();
        }
        return (linkWords.contains(potentialLinkWord));

//      int numLinks = linkWords.size();
//      for (int i = 0; i < numLinks; i++) {
//          String linkWord = (String)linkWords.elementAt(i);
//          if (linkWord.equals(potentialLinkWord))
//              return (true);
//      }
//      return (false);
    }

    public void collapseLinks() {
        int numExpanded = getNumExpandedLinks();
        for (int i = 0; i < numExpanded; i++) {
            WordNodeModel otherNodeModel = funModel.findNodeModel((String)expandedWords.elementAt(i));
            if (otherNodeModel == null)
                continue;
            LinkEdgeModel edgeModel = funModel.findEdgeModel(this, otherNodeModel);
            if (edgeModel == null)
                continue;
            boolean linkRemoved = funModel.removeLink(edgeModel, this, otherNodeModel, false);
            // must do this manually, unfortunately (or rely on
            // getNumExpandedLinks every iteration, and on not getting a
            // stale value from compiler optimization)
            if (linkRemoved) {
                i--;
                numExpanded--;
            }
        }
    }

    // for WordNodeModelListener

    public void addNodeListener(WordNodeModelListener listener) {
        // null allowed for serialization...
        if (listeners == null) {  // lazy eval. (make sure to chk. for
                                  // null in other methods)...
            listeners = new Vector();
        }

        listeners.addElement(listener);
    }

    public void removeNodeListener(WordNodeModelListener listener) {
        if (listeners == null) return;

        int index = listeners.indexOf(listener);

        if (index != -1)
            listeners.removeElementAt(index);
    }

    // helper for firing changes to listeners...

    // note how we do this (when we fire...) - fire multiple times for
    // single event.  if listener wants to "handle" this and receive just
    // one notification per event, should only really implement
    // nodeChanged and determine for itself what ought to be done;
    // conversely, there is no need at this point to really implement
    // nodeChanged if the specific notifications have been implemented...
    private void fireChanged(int changeType) {
        if (listeners == null) return;

        int numListeners = listeners.size();
        for (int i = 0; i < numListeners; i++) {
            WordNodeModelListener listener = (WordNodeModelListener)listeners.elementAt(i);
            listener.nodeChanged(this, changeType);  // always fire generic...

            switch (changeType) {
              case NODE_COLOR_CHANGED:
                  listener.nodeColorChanged(this);
                  break;
              case NODE_LOCATION_CHANGED:
                  listener.nodeLocationChanged(this);
                  listener.nodeBoundsChanged(this);
                  break;
              case NODE_SIZE_CHANGED:
                  listener.nodeSizeChanged(this);
                  listener.nodeBoundsChanged(this);
                  break;
              case NODE_BOUNDS_CHANGED:
                  listener.nodeBoundsChanged(this);
                  break;
//            case NODE_LINK_CREATED:
//                listener.nodeLinkCreatedChanged(this);
//                break;
              default:
                  break;
            }
        }
    }

    public boolean beingDragged() {
        //      Dbg.print("got being dragged");
        return drag;
    }

    public  void setDrag(boolean beingDragged) {
        //      Dbg.print("got setdrag");
        drag = beingDragged;
    }

    public WordNodeModel getHub() { return hub; }
    public void setHub(WordNodeModel newHub) { hub = newHub; }

}

