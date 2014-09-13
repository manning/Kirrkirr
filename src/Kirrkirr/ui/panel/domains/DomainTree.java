package Kirrkirr.ui.panel.domains;

import Kirrkirr.ui.panel.KirrkirrPanel;
import Kirrkirr.ui.panel.NewSemanticPanel;
import Kirrkirr.Kirrkirr;
import Kirrkirr.util.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.*;


public class DomainTree extends JComponent implements Runnable {

    private static final String SC_DOMAIN_FILE_ROOT = "DTREE";
    private static final String SC_DOMAIN_FILE_PREFIX = "DOM";
    private static final String SC_NO_DOMAIN_ENTRY =
        "There_is_no_semantic_domain_information_for";

    private static final int ZOOM_STEPS = 20;

    /** For debug performance measuring */
    private static int numPaints;

    /** The time to paint in milliseconds */
    private long lastPaintTime = 300L;
    private static final long MIN_PAINT_SLEEP = 80L;

    private final Domain root; // Root of domain tree
    private Domain  drawTarget; // May be different to target while zooming
    private Domain target;  // Domain node that we are currently focused on
    private Domain currentWord;
    Domain pinned;  // the Domain the mouse is hovering over (don't need to click it)

    int scrW;  // screen (our panel) width in pixels
    int scrH;  // screen (our panel) height in pixels
    int drawCutoff;  // minimum x RADIUS of node for us to draw children
    int colorCutoff;
    private boolean limitChildrenDrawn = true;

    private boolean starting = true;

    private transient Thread runner;

    // private Vector oldRoots;

    private NewSemanticPanel nsp;

    private Vector trackers; // related to PositionTracker
    final DefaultListModel<Domain> hierarchy, children;  // used in PositionTracker

    private int mouseX, mouseY; // position of mouse in display

    private int zoomStep; // counter when zooming
    private float dx, dy, dRad; // these are used only while zooming

    private float w, h, x, y; // width, height; x, y of center in [0,1] units

    private boolean zooming; // = false;

    private final Vector zoomQueue;
    private final Object zoomQueueLock;

    // todo: Fix this to use uniqueKey internally
    private String printableTailWord;

    private ImageLoader imLoader;
    private boolean loadImages;
    private boolean showPicsOnly;
    public boolean noContent; // = false


    public DomainTree(NewSemanticPanel semPanel, String xmlFile) {
        this((JComponent) semPanel, xmlFile);
        root.setMaxChildren(100);
        nsp = semPanel;
    }


    public DomainTree(JComponent myParent, String xmlFile) {
        this();
        if ( ! parseDomains(xmlFile)) {
            if (Dbg.DOMAINS) {
                Dbg.print("Couldn't parse semantic domains file");
            }
            noContent = true;
        }
    }


    public DomainTree() {
        // oldRoots = new Vector();
        trackers = new Vector();

        // int width = parent.getWidth();
        // if (width > 2000) { width = 2000; }
        float radius = 0.49f; // 0.45f + (width / 2000f) * 0.04f;

        root = new DomainOval(0.5f, 0.5f, radius, 2f, null, this);
        drawTarget = target = root;

        zoomQueue = new Vector();
        zoomQueueLock = new Object();
        zoomStep = 0;

        hierarchy = new DefaultListModel();
        children = new DefaultListModel<Domain>();
        // this.myParent = myParent;

        setDoubleBuffered(false);   // cdm: suspicious??
        DomainMouseInputAdapter dmia = new DomainMouseInputAdapter();
        addMouseListener(dmia);
        addMouseMotionListener(dmia);
        imLoader = null;
        setPreferredSize(new Dimension(1000, 300));
    }


    /** This overrides the corresponding method in Component.
     *  It is called when the size of the DomainTree window is changed.
     *  <p>
     *  Questions: do w and h always just equal scrW and scrH as floats?  If so,
     *  why do all these complex calculations??
     *  Answer: at the start, yes, but not after we zoom, but it's not yet
     *  clear to me whether this is or isn't the source of subsequent problems....
     */
    public void setBounds(int X, int Y, int W, int H) {
        if (Dbg.DOMAINS) {
            Dbg.print("DomainTree.setBounds(X=" + X + ", Y=" + Y + ", W=" + W + ", H=" + H + ")");
            Dbg.print("  DomainTree.setBounds before x=" + x + ", y=" + y + ", w=" + w + ", h=" + h);
        }
        float oldW = getWidth();
        float oldH = getHeight();
        if (w == 0.0f) w = W;
        if (h == 0.0f) h = H;

        super.setBounds(X, Y, W, H);
        scrW = getWidth();
        scrH = getHeight();
        drawCutoff = (int)(scrW * 0.1);
        colorCutoff = (int)(scrW * 0.8);

        if (oldW != 0.0f) {
            float grW = W/oldW;
            float grH = H/oldH;
            // jan 2005 try deleting
            // if (W != oldW) {    // cdm added 12/2004: not necessary if same width
            //  target.resizeChildren(grW);
            // }

            x += (x*grW)-x;
            y += (y*grH)-y;
            w*=grW;
            h*=grH;
        }
        if (Dbg.DOMAINS) {
            Dbg.print("  DomainTree.setBounds oldW="+oldW+", oldH="+oldH + ", scrW=" + scrW+
                    ", scrH=" + scrH);
            Dbg.print("  DomainTree.setBounds drawCutoff=" + drawCutoff + " colorCutoff=" + colorCutoff);
            Dbg.print("  DomainTree.setBounds  after x=" + x + ", y=" + y + ", w=" + w + ", h=" + h);
        }
    }


    private void setWindowBounds(float growthFactor, float xshift, float yshift) {
        if (Dbg.DOMAINS) {
            Dbg.print("DomainTree.setWindowBounds(growthFactor=" + growthFactor + ", xshift=" + xshift + ", yshift=" + yshift + ")");
            Dbg.print("  DomainTree.setBounds before x=" + x + ", y=" + y + ", w=" + w + ", h=" + h);
        }
        float tw=w;
        float th=h;

        w*=growthFactor;
        h*=growthFactor;

        float dw=w-tw;
        float dh=h-th;

        x += xshift-(dw*((target.getScrXCenter()-x)/tw));
        y += yshift-(dh*((target.getScrYCenter()-y)/th));
        if (Dbg.DOMAINS) {
            Dbg.print("  DomainTree.setBounds  after x=" + x + ", y=" + y + ", w=" + w + ", h=" + h);
        }
    }


    public void halt() {
        if (runner == null) return;
        runner.interrupt();
        try {
            runner.join(); //make sure it's halted before we proceed
            runner = null;
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }


    public void start() {
        if (runner != null) return;
        runner = new Thread(this);
        runner.start();
    }


    public void paintComponent(Graphics g) {
        long startTime = System.currentTimeMillis();
        if (Dbg.DOMAINS2) {
            Dbg.print("DomainTree.paintComponent invocation " + (++numPaints) +
               " at " + startTime);
           Domain.drawCount = 0;
        }
        if (starting) setupFontMetrics();

        if (drawTarget != null) {
            if (zooming) {
                drawTarget.render(g, false);
            } else {
                target.render(g, true);
            }

            // g.setColor(Color.black);
            //      g.drawString("["+Domain.drawCount+"]",10,10);

            if (currentWord == null && printableTailWord != null) {
                g.setColor(Color.black);
                String msg = Helper.getTranslation(SC_NO_DOMAIN_ENTRY);
                g.drawString(msg, 3, getHeight() - (3 + Domain.fm.getDescent() + Domain.fm.getHeight()));
                g.drawString(printableTailWord, 3, getHeight() - (3 + Domain.fm.getDescent()));
            }
        }
        lastPaintTime = System.currentTimeMillis() - startTime;
        if (Dbg.DOMAINS2) {
           Dbg.print("  DomainTree.paintComponent completed drawing " +
               Domain.drawCount + " domains in " + lastPaintTime + " ms.");
        }
        if (lastPaintTime < MIN_PAINT_SLEEP) {
           lastPaintTime = MIN_PAINT_SLEEP;
        }
    }


    public void zoomOut() {
            zoomTo(target.parent, true);
    }


    public void zoomTo(Domain t, boolean signal)
    {
        if (t==null || t==target) return;

        //clear out the queue and put our info on
        synchronized(zoomQueueLock) {
            Domain.populateZoomQueue(zoomQueue, target, t);

            //this if line was causing the internalSetCurrentWord to never go
            //if(nsp!=null && t.isWord())
            if (nsp != null)
                internalSetCurrentWord(t, signal);
            t.recMoveToFront();
            pinned = null;

            if (!zoomQueue.isEmpty()) {
                beginZoomStage();
            }
        }
    }

    private void internalSetCurrentWord(Domain word, boolean signal) {
        if (currentWord != null) {
            currentWord.setSelected(false);
        }
        word.setSelected(true);
        currentWord = word;

        if (signal) {
            nsp.selectedWordChanged(Helper.printableStringToUniqueKey(word.getText()));
            nsp.setSense(word);
        }
        for (int i = 0, tsize = trackers.size(); i < tsize; i++) {
            ((PositionTracker)trackers.elementAt(i)).repaint();
        }
    }

    private void beginZoomStage() {
        Domain tempTarget = (Domain) zoomQueue.elementAt(0);
        if (tempTarget.isWord()) {
            //don't zoom
            zooming = false;
            zoomQueue.removeElementAt(0); //yank that from the queue
                                          //(should now be empty)
            return;
        }

        zooming = true;

        if (tempTarget.parent == target) {
            //zooming in
            drawTarget = target;
        } else {
            //zooming out
            drawTarget = tempTarget;
        }
        Domain oldTarget = target;
        target = tempTarget;

        //begin loading images for child words
        if (loadImages && ! target.isWord()) {
            if (imLoader != null) {
                try {
                    imLoader.halt();
                    imLoader.join();
                    oldTarget.removeImages();
                } catch (InterruptedException ie) {
                }
            }
            imLoader = new ImageLoader(target);
            imLoader.start();
        }

        root.synch(x,y,w,h); //make sure all coords are updated
        updateHierarchy();
        //amount we need to grow each step of the way
        float targetW = target.getScrW();
        float zoomFactor = getWidth()*.9f/targetW; //scale factor
                                                    //of zoom
        dRad =targetW*(zoomFactor - 1)/ZOOM_STEPS; //find zoom step size

        //amount we need to shift to get to center from starting position
        dx = (getWidth()/2-target.getScrXCenter())/ZOOM_STEPS;
        dy = (getHeight()/2-target.getScrYCenter())/ZOOM_STEPS;

        zoomStep = 1;
    }

    public float zoom() {
        synchronized (zoomQueueLock) {
            float targetW = target.getScrW();
            float growthFactor = (targetW+dRad)/targetW; //growth needs to be multiplier

            zoomStep++; //increment zoom step

            setWindowBounds(growthFactor, dx, dy);

            if (zoomStep>ZOOM_STEPS) {
                zoomQueue.removeElementAt(0); //pop off to expose next
                                              //target
                if (zoomQueue.isEmpty()) {
                    zooming = false;  //we're done
                } else {
                    beginZoomStage();
                }
            }
            return growthFactor;
        }
    }


    /** This is for the thread that animates the main domains display. */
    public void run() {
        try {
            while ( ! runner.isInterrupted()) {
                if (zooming) {
                    zoom();
                } else if (target != null) {
                    target.doTimestep(mouseX, mouseY);
                }
                root.synch(x,y,w,h);
                repaint();
                Thread.sleep(lastPaintTime);
            }
        } catch (InterruptedException except)  {
        }
    }


    public void setupFontMetrics() {
        Graphics g = getGraphics();
        if (g==null) {
            Dbg.print("VOID GRAPHICS");
        } else {
            Domain.fm = g.getFontMetrics(Domain.font);
            Domain.rootfm=g.getFontMetrics(Domain.rootFont);
            if (Domain.fm != null) {
                Domain.fh = Domain.fm.getAscent()+Domain.fm.getDescent();
                Domain.fasc = Domain.fm.getAscent();
            }
            if (Domain.rootfm != null) {
                Domain.rootfh = Domain.rootfm.getAscent() +
                                Domain.rootfm.getDescent();
            }
        }
        root.resize(1.0f);  // cdm 12/2004: this may not be necessary as setBounds will always be called on component??
        starting=false;
    }


    public void registerTracker(PositionTracker tracker) {
        trackers.addElement(tracker);
    }

    //notify of new root
    private void notifyTrackers() {
        for(int i = 0; i < trackers.size(); i++) {
            ((PositionTracker) trackers.elementAt(i)).updateTracker();
        }
    }

    public void updateHierarchy() {
        int hSize = hierarchy.size();
        if (hSize > 0 && target.parent == hierarchy.elementAt(hSize - 1)) {
            hierarchy.addElement(target);
        } else if (hSize > 1 && target == hierarchy.elementAt(hSize - 2)) {
            hierarchy.removeElementAt(hSize - 1);
        }

        populateChildren(target);

        for (int i = 0; i < trackers.size(); i++) {
            ((PositionTracker)trackers.elementAt(i)).updateTracker();
        }
    }

    private void populateChildren(Domain parent) {

        //clear out old ones
        children.removeAllElements();

        //we copy the children so that we may sort them however we wish
        //without affecting the display order

        Vector curChildren = parent.getChildrenVector();
        if (curChildren == null) return;
        Domain[] childArray = new Domain[curChildren.size()];
        curChildren.copyInto(childArray);

        Arrays.sort(childArray, new DomainSorter());

        //populate list model
        for (int i = 0; i < childArray.length; i++) {
            Domain child = childArray[i];
            children.addElement(child);
        }
    }



    /** Handles the parsing of the xml file containing the tree structure
     *  for the domain information.
     *
     *  @param domainFilename Filename/URL path to domains file.
     *  @return true iff the domain display is successfully initialized
     */
    public final boolean parseDomains(String domainFilename) {
        if (domainFilename == null) {
            if (Dbg.DOMAINS) {
                Dbg.print("No domain index specified.");
            }
            return false;
        }
        try {

            InputStream bis = new BufferedInputStream(RelFile.makeURL(domainFilename).openConnection().getInputStream());

            if (bis == null) {
                Dbg.print("Couldn't create buffered input stream");
                return false;
            }
            Document doc;

            //skip to root's position in file
            skipToFirstDomain(bis, domainFilename);
            XmlMiniDocument minidoc = new
            XmlMiniDocument(SC_DOMAIN_FILE_ROOT, SC_DOMAIN_FILE_PREFIX + 0);

            doc = minidoc.setup();
            doc = minidoc.parseElement(bis, doc);
            doc = minidoc.finish(doc);

            //create a dummy root (top-level domain to act as root)
            //  Domain  temproot = new Domain(.5f, .5f, .4f, null);

            Node top = doc.getDocumentElement().getFirstChild();  //get top
                                                                  //level domain
            Element topElem = null;
            if (top instanceof Element)
                topElem = (Element) top;

            //recursively obtain children from file
            root.getChildren(topElem);

            //  root=temproot;
            hierarchy.addElement(root);
            populateChildren(root);
            //return success or failure
            return root != null; // cdm jun 2005: always true, I believe ...
        } catch (FileNotFoundException fnfe) {
            Dbg.print("Domain hierarchy xml file not found! " + domainFilename);
            // root = null; // Just leave as displaying empty root
            return false;
        } catch (Exception e) {
            Dbg.print("Parse failure.");
            e.printStackTrace();
            // root = null; //  Just leave as displaying empty root
            return false;
        }
    }

    //helper method to skip the xml header information since we are using
    //the XmlMiniDocument, which fakes that information.

    private static void skipToFirstDomain(InputStream bis, String domainFilename) {
        String targetTag = '<' + SC_DOMAIN_FILE_ROOT + '>';
        try {
            RandomAccessFile raf=new RandomAccessFile(domainFilename,"r");

            String line=raf.readLine();
            while (line!=null){
                if(line.equals(targetTag)) {
                    bis.skip(raf.getFilePointer());
                    raf.close();
                    return;
                }
                line = raf.readLine();
            }
        } catch(Exception e) {
            Dbg.print("Malformed Domain File!");
        }
        Dbg.print("couldn't find " + targetTag + " line!");
    }


    public String getCurrentWord() {
        if(currentWord == null) return null;
        else return currentWord.getText();
    }

    //  necessary for interaction with the NewSemanticDomain
    public Domain getCurrentDomain() {
    	if (currentWord == null) return null;
    	else return currentWord;
    }

    public void setCurrentWord(String uniqueKey, Vector domFields ) {
        printableTailWord = Helper.uniqueKeyToPrintableString(uniqueKey);
        if (Dbg.DOMAINS) {
            Dbg.print("DomainTree: selecting: " + uniqueKey + " as " + printableTailWord);
            Dbg.print("  in " + domFields);
        }
        if (currentWord != null)
            currentWord.setSelected(false); //deselect old current word

        Domain t = getDomain(uniqueKey, domFields, true); //find the new one

        if (t != root) {  //if it has domain info, set it
            currentWord = t;
            t.setSelected(true);
            nsp.setSense(t);
        }
        else  //otherwise currentWord is null
            currentWord = null;

        zoomTo(t, false);       //zoom - word if has domain info, root otherwise
    }


    public Domain getDomain(String ukey, Vector domFields, boolean findClosest) {
        Domain child;
        Domain parentDomain = root;
        String printableUKey = Helper.uniqueKeyToPrintableString(ukey);
        if (Dbg.DOMAINS) Dbg.print("getDomain finding " + printableUKey);

        if (findClosest && (currentWord == null ||
               ! printableTailWord.equals(currentWord.getText()))) {
            //not triggered by sense toggle - should just go to closest
            //sense, followed by primary sense
            //first check immediate children (non-primary sense could be closest)
            child = target.findChild(printableUKey);
            if (child != null) {
                return child;
            }
        }

        if (domFields != null) {
            Node conversionNode = null;
            for (int i = 0, dfsize = domFields.size(); i < dfsize; i++) {
            	String newName = (String) domFields.elementAt(i);
            	if (Kirrkirr.dc != null) {
            	    newName = Kirrkirr.dc.getConversion(newName, conversionNode);
            	    conversionNode = Kirrkirr.dc.getConversionNode(newName, conversionNode);
            	}
                child = parentDomain.findChild(Helper.getWord(newName));
                // if (child == null) {
                    //++ chris frigging around June 2006
                    // child = parent.findChild(printableUKey);
                    // if (child == null) {
                    //    return root;
                    // }
                // }
                // parent = child;
                // cdm trying this one now June 2006
                if (child != null) {
                    parentDomain = child;
                }
            }
        }
        if (Dbg.DOMAINS) {
            Dbg.print("Searching for " + printableUKey + " in " + parentDomain);
        }
        child = parentDomain.findChild(printableUKey);
        if (child == null) return root;
        return child;
    }


    //external method that may be called by controls that don't have
    //access to our ivars.

    public static void main(String[] argv) {

        if (argv.length < 1) {
            System.out.println("Please pass in the name of a valid Domain xml file.");
            return;
        }

        JFrame mainFrame = new JFrame();
        JComponent content= (JComponent)mainFrame.getContentPane();
        JPanel mainPane = new JPanel();

        mainPane.setLayout(new BorderLayout(3,3));
        mainPane.setPreferredSize(new Dimension(800, 400));

        //add everyone
        content.setLayout(new BorderLayout(4,4));
        content.add(mainPane,BorderLayout.CENTER);

        mainFrame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }});

        final DomainTree d = new DomainTree(mainPane, argv[0]);
        d.setPreferredSize(new Dimension(800,400));

        PositionTracker pt = new PositionTracker(d, null, KirrkirrPanel.NORMAL);

        mainPane.add(d, BorderLayout.CENTER);
        mainPane.add(pt, BorderLayout.WEST);

        mainFrame.pack();
        mainFrame.setVisible(true);
        d.start();
    }

    private class DomainMouseInputAdapter implements MouseInputListener {

        private Domain graspedDomain;

        public void mouseClicked(MouseEvent e) {
            Domain t = target.click(e.getX(),e.getY());
            if (t != null) t.moveToFront();
            zoomTo(t, true);
        }

        public void mouseMoved(MouseEvent e) {
            mouseX = e.getX();
            mouseY = e.getY();
        }

        public void mousePressed(MouseEvent e) {}

        public void mouseDragged(MouseEvent e) {
            graspedDomain = target.click(e.getX(), e.getY());
            if (graspedDomain != null) {
                graspedDomain.moveToFront();
            }
        }

        public void mouseReleased(MouseEvent e) {
            // We should move it, but we don't yet.
            // Some tricky calculations to do!
            /*
            if (graspedDomain  != null) {
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
            */
        }

        public void mouseEntered(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}

    } // end class DomainMouseInputAdapter



    static class DomainSorter implements Comparator<Domain> {

        private KAlphaComparator alphaComp;

        public DomainSorter() {
            alphaComp = new KAlphaComparator();
        }
        public int compare(Domain a, Domain b) {
            return compareTwo(a, b);
        }

        public boolean equals(Object a, Object b) {
            return (compareTwo((Domain) a, (Domain) b) == 0);
        }

        private int compareTwo(final Domain a, final Domain b) {
            return alphaComp.compare(a.getText(), b.getText());
        }
    }


    public void setMaxChildren(int newMax) {
        root.setMaxChildren(newMax);
    }
    public int getMaxChildren() {
        return root.getMaxChildren();
    }

    /* Setter for whether or not children rendering is limited
     */
    public void setChildrenDrawingIsLimited(boolean limit) {
        limitChildrenDrawn = limit;
    }
    public boolean childrenDrawingIsLimited() {
        return limitChildrenDrawn;
    }

    public Domain getRoot() { return root; }

    public class ImageLoader extends Thread {
        private Domain parentDomain;
        private boolean halted;

        public ImageLoader(Domain parentDom) {
            parentDomain = parentDom;
        }

        public void run() {
            parentDomain.getImages(nsp.parent.cache, this, showPicsOnly);
        }

        public boolean halted() { return halted; }
        public void halt() { halted = true; }

    }

    public void showPics(boolean show, boolean showOnly) {
        loadImages = show;
        showPicsOnly = showOnly;
        if (imLoader != null) {
            try {
                imLoader.halt();
                imLoader.join();
            }
            catch(InterruptedException ie) {}
        }
        if (show) {
            imLoader = new ImageLoader(target);
            imLoader.start();
        } else if (imLoader != null) {
            try {
                imLoader.interrupt();
                imLoader.join();
            } catch(InterruptedException ie) {
                ie.printStackTrace();
            } catch (Exception e) {
                Dbg.print("Loader thread for " + root.getText() + " is " + imLoader);
                e.printStackTrace();
            }
            root.removeImages(0);
        }
    }

}
