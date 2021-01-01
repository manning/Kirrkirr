package Kirrkirr.ui.panel.domains;

import Kirrkirr.dictionary.DictionaryCache;
import Kirrkirr.util.Dbg;
import Kirrkirr.util.FontProvider;
import Kirrkirr.util.URLHelper;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


public class Domain {

    // todo [cdm 2018]: Can we change the cells to JComponent so we can easily tooltip them? What does Jeff do in protovis?

    //----------------STATIC VARIABLES--------------------//
    private static final float GVAL = -0.0002f;
    // static float G=.1f;
    private static final float REL_CNTR_X=.5f;
    private static final float REL_CNTR_Y=.5f;
    static Font font = FontProvider.WORD_LIST_FONT;
    static FontMetrics fm;
    static Font rootFont = FontProvider.PROMINENT_HUGE_WORD_FONT;
    static FontMetrics rootfm;
    static int fh;  //font height (ascent + descent)
    static int fasc; //font ascent
    static int rootfh;

    private static int maxChildren = 100;
    private static final float rotateRange = (float)(Math.PI/8);
    private static final float rotateAngle = (float)(Math.PI/360);

    private static final float MAX_COLOR_COS = .969f;

    // public static Domain testTarget; // A junk debugging variable? Was used in NewSemanticDomain -> public
    private static final Domain repelDummy = new Domain(0.0f,0.0f,0.0f,1.0f,null,null);

    // private static Hashtable lookup=new Hashtable();

    static int drawCount;   // counts draw ops just for performance debugging


    //----------------PRIVATE VARIABLES---------------------//
    private Vector<Domain> children; // needs to be Vector for multithreading currently
    /** Final target color. */
    protected Color myColor;
    /** Current draw color of node. */
    protected Color curColor;

    protected float relDx, relDy;
    /** Relative position and radius of this node to parent node */
    protected float relX, relY, relR;
    /** Ratio between x and y radius used at drawing time */
    float aspect;
    /** cdm 2003: I'm not quite sure what this is needed for... */
    float constantAspect;

    /** X coordinate of center of object */
    protected float scrX;
    /** Y coordinate of center of object */
    protected float scrY;
    /** "Radius" of object in X direction. Scaled by aspect for Y direction. */
    protected float scrR;

    String text;  // holds a printable unique key.  Yucky as used as match!
    protected int textWidth;
    protected int textxpos;  // position of text when scrolling in Oval
    Domain parent;
    DomainTree dtree; // The DomainTree that this domain is in.
    protected boolean selected; // = false;
    private int childCutoffIndex; // = 0;


    /** x= relative x in parent, y= relative y in parent, r= radius (x),
     *  a=ratio of horizontal to vertical aspect, p= parent domain
     */
    public Domain(float x, float y, float r, float a, Domain p, DomainTree dt) {
        relX = x;
        relY = y;
        relR = r;
        aspect = a;
        constantAspect = a;
        text = "";
        parent = p;
        dtree = dt;
        chooseColor();
    }


    private void chooseColor() {
        float r,g,b;
        float mag, parentMag, dot;
        Color parentColor;
        if (parent==null)
            parentColor=Color.lightGray;
        else
            parentColor = parent.getColor();

        float parentr = ((float)parentColor.getRed())/255;
        float parentg = ((float)parentColor.getGreen())/255;
        float parentb = ((float)parentColor.getBlue())/255;

        do {
            r = (float) (1 - Math.random()/2);
            g = (float) (1 - Math.random()/2);
            b = (float) (1 - Math.random()/2);
            mag = r*r + g*g + b*b;

            parentMag = parentr*parentr + parentg*parentg + parentb*parentb;
            dot = r*parentr + g*parentg + b*parentb;
            dot *= dot;

        } while (dot/(mag*parentMag) > MAX_COLOR_COS);

        myColor = new Color(r, g, b);
        curColor=myColor;

        // float HSB[] =Color.RGBtoHSB(myColor.getRed(),myColor.getGreen(),
        // myColor.getBlue(),null);
        // float hue=HSB[0];
        // float sat=HSB[1];
        // float pHSB[] =Color.RGBtoHSB(parentColor.getRed(),
        // parentColor.getGreen(),parentColor.getBlue(),null);
        // fadeTarget=new Color(Color.HSBtoRGB(pHSB[0],pHSB[1],HSB[2]));
    }


    // public void setAspect(float a) {
    //     aspect = a;
    // }

    public float getAspect() {
        return aspect;
    }


    public float getScrXCenter() {
        return scrX;
    }

    public float getScrYCenter() {
        return scrY;
    }

    public float getScrW() {
        return scrR*2;
    }

    public float getScrH() {
        return scrR*2;
    }

    public boolean stable() {
        if (scrR<(dtree.scrW*.43) || scrR>(dtree.scrW*.47)) return false;
        if (Math.abs(getScrXCenter()-dtree.scrW/2)>3) return false;
        if (Math.abs(getScrYCenter()-dtree.scrH/2)>3) return false;
        return true;
    }

    public Color getColor() {
        return curColor;
    }

    public Color getFinalColor() {
        return myColor;
    }

    private boolean haveChildren() {
        return children!=null && ! children.isEmpty();
    }

    public Domain randomChild() {
        if (haveChildren()) {
            return children.get((int) (Math.random()*children.size()));
        } else {
          return null;
        }
    }

    //  gives semantic domains precedent over words
    public Domain randomChildDomainPri() {
        if (haveChildren()) {
            ArrayList<Domain> domains = new ArrayList<Domain>();
            for (Domain child : children) {
                List<Domain> grandkids = child.getChildren();
                if (grandkids != null && !grandkids.isEmpty()) {
                    domains.add(child);
                }
            }
            if ( ! domains.isEmpty()) {
                return domains.get((int) (Math.random() * domains.size()));
            } else {
                return children.get((int) (Math.random() * children.size()));
            }
        } else {
            return null;
        }
    }

    public Domain click(int scrXClick, int scrYClick) {
        if (!contains(scrXClick,scrYClick)) return parent;
        if (children==null) return this;
        int nChildren = children.size();

        //search through visible domains

        for (int i=nChildren-1;i>=childCutoffIndex;i--){
            Domain target = children.get(i);
            if (target.contains(scrXClick,scrYClick))
                return target;
        }
        return this;
    }


    public boolean contains(int scrX1, int scrY1) {
        float dx=(scrX1-scrX);
        float dy=(scrY1-scrY);
        float dsq=(dx*dx + dy*dy);
        return (dsq<(scrR*scrR));
    }

    public boolean contained() {
        float dx=(relX-.5f);
        float dy=(relY-.5f);
        float dsq=(dx*dx+dy*dy);
        float d2=(.5f-relR);
        return (dsq<(d2*d2));
    }


    public void synch() {
        if (relDx == 0.0f && relDy == 0.0f) return; //short circuit eval

        relX+=relDx;
        relY+=relDy;

        if (!contained()) {
            //undo move
            relX-=relDx;
            relY-=relDy;

            //try to move but keep in bounds
            float dx1 = (relX - REL_CNTR_X);
            float dy1 = (relY - REL_CNTR_Y);
            float dSq = dx1*dx1 + dy1*dy1;

            Domain pinned = dtree.pinned;
            if (dSq < .01f) {
                //nudge items stuck in middle that would otherwise repel
                //outside of circle
                relX+=.001f;
                relY+=.001f;
            } else if (pinned != null) {
                //if we're close to pinned domain, rotate out of way
                float dxpx = relX - pinned.relX;
                float dypy = relY - pinned.relY;
                dSq = dxpx*dxpx + dypy*dypy;
                if (dSq < 0.0625f) {     //close to pinned domain

                    float pdx = (pinned.relX - REL_CNTR_X);
                    float pdy = (pinned.relY - REL_CNTR_Y);
                    float pAngle = (float) Math.atan(pdy/pdx); //for pinned domain
                    if(pdx <0.0f)
                        pAngle += (float) Math.PI;

                    float angle = (float) Math.atan(dy1/dx1);  //for this domain
                    if(dx1 < 0.0f)
                        angle += (float) Math.PI;

                    // Anything close (angular distance-wise) and
                    // pinned against the edge gets rotated (rather than
                    // strictly repelled) away...
                    if(angle > pAngle - rotateRange && angle <=pAngle) {
                        //rotate to smaller angle
                        rotate(rotateAngle, false);
                    }
                    else if(angle > pAngle && angle <pAngle + rotateRange) {
                        //rotate to larger angle
                        rotate(rotateAngle, true);
                    }
                }
            }
        }

        relDx=0.0f;
        relDy=0.0f;
    }


    public void doTimestep(int scrXmouse, int scrYmouse) {
        if (children==null) return;  //could return false as an optimization?

        int n = children.size();


        dtree.pinned = null; //reset pinned
        for (int i = n-1; i >= childCutoffIndex; i--) {
            Domain kid = (Domain) children.get(i);
            if (kid.contains(scrXmouse, scrYmouse)) {
                dtree.pinned = kid;
                break;
            }
        }

        float strongRepel = (float) Math.sqrt(n-childCutoffIndex);

        //go through each child
        for (int j=n-1; j>=childCutoffIndex; j--) { //only repel visible domains
            Domain child = (Domain) children.get(j);

            //apply gravity (repel from outside)
            //likewise, more children -> more gravity
            child.addGravity((n-childCutoffIndex)/20);

            //repel from other nodes
            for (int k=j-1;k>=childCutoffIndex;k--) {
                child.repel((Domain) children.get(k),
                        strongRepel);
            }

            //if mouse over this domain, don't move
            if(child == dtree.pinned)
                child.relDx = child.relDy = 0.0f;

            child.synch(); //apply changes
        }
        relDx = relDy = 0; //root shouldn't move at any time - will have
        //accumulated dx and dy through repulsion

        //if interrupted, clean up so that dx, dy are applied for all children
        //and we remain in a consistent state
    }


    /** Apply gravity to a domain and bring it back toward center */
    public void addGravity(float scale) {
        float gravdx = (relX-REL_CNTR_X);
        float gravdy = (relY-REL_CNTR_Y);
        float radiusRatio = (float) Math.sqrt(.25f/(gravdx*gravdx+gravdy*gravdy));

        repelDummy.relX = REL_CNTR_X+gravdx*radiusRatio;
        repelDummy.relY = REL_CNTR_Y+gravdy*radiusRatio;
        repelDummy.relR = relR;
        repel(repelDummy, scale);
    }



    //Repel two nodes - if either has mouse over it, will be increased by
    //factor of strength.

    private void repel(Domain d2, float strength) {
        float dx1=(relX-d2.relX);
        float dy1=(relY-d2.relY);
        float distsq=(dx1*dx1 + dy1*dy1);
        float sumrad = relR+d2.relR;

        float f=GVAL*1/distsq;

        f*=sumrad; //force prop to domain size

        //if either domain has mouse over it, increase repulsion to spread
        //domains apart, improving visibility
        if(this == dtree.pinned || d2 == dtree.pinned || d2 == repelDummy)
            f*=strength; //increase repulsion from pinned domain

        if(f > -.001f) return; //ignore small forces to effectively localize repulsion

        relDx-=dx1*f; //apply repulsion
        relDy-=dy1*f;
        d2.relDx+=dx1*f;
        d2.relDy+=dy1*f;
    }

    /** Rotates the domain about the center of its parent domain by rad
     * degrees.  If ccw is true, rotation is in counterclockwise
     * direction; if false, rotation is clockwise.
     */
    private void rotate(float rad, boolean ccw) {
        if(!ccw) rad = -rad;

        //perform the rotation
        float dcx = relX - REL_CNTR_X;
        float dcy = relY - REL_CNTR_Y;

        float cosine = (float) Math.cos(rad);
        float sine = (float) Math.sin(rad);
        float newdx = dcx*cosine - dcy*sine;
        float newdy = dcx*sine + dcy*cosine;

        relX = newdx+REL_CNTR_X;
        relY = newdy+REL_CNTR_Y;

        //check bounds
        if(!contained()) {
            //if would be out of bounds, scale radially inward
            //code assumes rect - will bound ovals too, though not
            //optimally.

            float scale = (float)((.5f - relR)/Math.sqrt(newdx*newdx+newdy*newdy));
            newdx*=scale;
            newdy*=scale;
            relX = newdx+REL_CNTR_X;
            relY = newdy+REL_CNTR_Y;
        }
    }


    protected void fadeColor(float z) {
        float z2=1-z;
        Color parentColor;
        if (parent == null) {
            parentColor = Color.lightGray;
        } else {
            parentColor = parent.getColor();
        }
        curColor= new Color((int)(z2*parentColor.getRed()+z*myColor.getRed()),
                         (int)(z2*parentColor.getGreen()+z*myColor.getGreen()),
                         (int)(z2*parentColor.getBlue()+z*myColor.getBlue()));
    }

    /** This duplicates code in DomainOval!  Make Domain abstract?  (RepelDummy
     *  is one exception.)
     */
    public void drawShape(Graphics g) {
        g.fillOval((int)(scrX-scrR),(int)(scrY-(scrR/aspect)),
                   (int)(scrR*2),(int)((scrR/aspect)*2));
    }


    /** This is what is called to paint things from paintComponent in
     *  DomainTree.  This version draws the top level domain display.
     *  Then draw(Graphics, boolean) is called on children.
     *  XXXX: To do: fold this into an instance of draw().
     *  Have separate RootDomain class!
     *
     *  @param g The graphics object to render in
     *  @param txt False if zooming, true if not zooming
     */
    public void render(Graphics g, boolean txt)
    {
        // if fully off screen return
        if (((scrY-(scrR/aspect)) > dtree.scrH) || (scrY+(scrR/aspect) <0) ||
                ((scrX-scrR) > dtree.scrW) || (scrX+scrR <0)) {
            return;
        }

        if (parent != null) {
            g.setColor(parent.getColor());
        } else {
            g.setColor(Color.gray);
        }
        g.fillRect(0, 0, dtree.scrW, dtree.scrH);

        g.setColor(curColor);
        drawShape(g);

        if (children!=null && scrR>dtree.drawCutoff) {
            int nChildren = children.size();
            for (int i=childCutoffIndex; i<nChildren; i++) {
                children.get(i).draw(g, txt);
            }
        }

        g.setFont(rootFont);
        g.setColor(Color.white);
        g.drawString(text, 33,33);
        g.setColor(Color.black);
        g.drawString(text, 32,32);
        g.setFont(font);
    }


    public void drawText(Graphics g) {}


    /** This is the method that draws Domain objects other than the root node.
     *  It in turn calls drawShape(g) and drawText(g) which are subclassed by
     *  the different classes.
     */
    public void draw(Graphics g, boolean txt) {
        //Test if domain is offscreen.
        if (((scrY-(scrR/aspect)) > dtree.scrH) || ( scrY+(scrR/aspect) <0) ||
                ((scrX-scrR) > dtree.scrW) || (scrX+scrR <0)) {
            return;
        }

        if (selected)
            g.setColor(Color.yellow);
        else
            g.setColor(curColor);

        drawShape(g);
        drawCount++;
        if (children != null && scrR > dtree.drawCutoff) {
            int nChildren = children.size();
            for (int i=childCutoffIndex; i<nChildren; i++)
                children.get(i).draw(g,false);
        }
        if (txt) drawText(g);
    }


    private static int aspectSynchCounter = 0;

    private void synchAspect(float z) {
        float zz;
        if (z < 0.3f) {
            zz = 0.0f;
        } else if (z > 1.0f) {
            zz = 1.0f;
        } else {
            zz = z;
        }

        float scrAspect=(float)dtree.scrW/(float)dtree.scrH;

        aspect=(zz*scrAspect)+((1-zz)*constantAspect);
        if (false && Dbg.DOMAINS) {  //todo: restore this
            aspectSynchCounter++;
            if (aspectSynchCounter % 100 == 0) {
                Dbg.print("Synched aspect " + aspectSynchCounter + " times with call from:");
                new Throwable("Stacktrace generator").printStackTrace();
            }
            Dbg.print("Domain: Changed aspect for " + ("".equals(text) ? text: "ROOT") + "  to " + aspect + " based on argument " + z + ", scrAspect " + scrAspect + ", and constantAspect " + constantAspect);
        }
    }


    public void synch(float scrBoundX, float scrBoundY, float scrBoundW, float scrBoundH) {
        scrR=(relR*scrBoundW);
        scrX=(scrBoundX+scrBoundW*relX);
        scrY=(scrBoundY+scrBoundH*relY);

        if ( ! isWord()) {
            synchAspect(scrR/(dtree.scrW/2));
        }

        if (Dbg.DOMAINS2) {
          if (text.equals("spatial") || text.isEmpty()) {
            Dbg.print("Changed " + ("".equals(text) ? text: "ROOT") + " to scrX=" + scrX + " scrY=" + scrY + " scrR=" + scrR + " aspect=" + aspect);
            Dbg.print("  relR="+relR+" scrBoundW="+scrBoundW+ " scrBoundH="+scrBoundH);
          }
        }
        // Test if domain is offscreen.
        if (((scrY-(scrR/aspect)) > dtree.scrH) || ( scrY+(scrR/aspect) <0)) return;
        if (((scrX-scrR) > dtree.scrW) || ( scrX+scrR <0)) return;

        if (scrBoundW<dtree.colorCutoff && scrBoundW>dtree.drawCutoff) {
            fadeColor((scrBoundW-dtree.drawCutoff)/(dtree.colorCutoff-dtree.drawCutoff));
        } else {
            curColor=myColor;
        }

        if (children != null && scrR > dtree.drawCutoff) {
            for (Domain child : children) {
                child.synch(scrX - scrR, scrY - (scrR / aspect), scrR * 2, getScrH());
            }
        }
    }


    public void moveToFront(Domain toMove) {
        //do this to avoid NPE's.  at the worst, one child will draw
        //twice.  faster than synchro on every access to children vector?
        //probably still need synchro to ensure that operations that
        //temporarily cache children vector length don't occur in between
        //these ops if called on thread other than Swing thread
        children.add(toMove);
        children.remove(toMove);
        findChildCutoff(false, dtree.childrenDrawingIsLimited());  //reset our child cutoff (without recursing)
    }


    public void moveToFront() {
        if (parent==null) return;
        parent.moveToFront(this);
    }

    public void recMoveToFront() {
        moveToFront();
        if (parent!=null) parent.recMoveToFront();
    }


    public boolean isWord() {
        return (children == null || children.isEmpty());
    }

    public boolean hasChild(Domain child) {
        if(children == null) return false;
        return children.contains(child);
    }

    public Domain findChild(String childName) {
        if (Dbg.DOMAINS) {
            Dbg.print("Looking for " + childName + " in " + text);
        }
        if (children == null) {
            return null;
        }
        for (Domain child : children) {
            if (Dbg.DOMAINS) {
                Dbg.print("  Comparing against " + child.getText());
            }
            if (child.getText().equals(childName)) {
                if (Dbg.DOMAINS) {
                    Dbg.print("  Found it!");
                }
                return child;
            }
        }
        if (Dbg.DOMAINS) {
            Dbg.print("  Didn't find it");
        }
        return null;
    }

    /*------------------------------
    public void setText(String newText) {
         text = newText;
         lookup.put(text, this);
    }


    public static Domain getDomain(String t) {
         return (Domain)lookup.get(t);
    }
    --------------------------------*/

    public String getText() {return text;}


    /** This is called recursively to construct the Domain element hierarchy
     *  from the XML file.  This appears to always be done exhaustively,
     *  regardless of what will be displayed in the window!  (I.e., we don't
     *  only find the parts needed and load those.)
     */
    public int getChildren(Element topElem) {
        int totalDescendants = 1;  // 1 for ourself
        int maxDesc = 0;
        // setText(URLHelper.decode(topElem.getAttribute("NAME")));
        text = URLHelper.decode(topElem.getAttribute("NAME"));
        NodeList nl = topElem.getChildNodes();
        int nlLeng = nl.getLength();

        if (nlLeng == 0) {
            return totalDescendants;
        }

        int[] descendantArray = new int[nlLeng];
        children = new Vector<>();
        int cSize = 0;  // mirrors the size of children Vector

        for (int i = 0; i < nlLeng; i++) {
            Node n = nl.item(i);
            if (n instanceof Element) {
                Domain newD;
                if (n.getChildNodes().getLength() == 0) {
                    newD = new DomainRect(0.1f, 0.1f, 0.1f, 2f, this, dtree);
                } else {
                    newD = new DomainOval(0.1f, 0.1f, 0.1f, 2f, this, dtree);
                }

                // recurse.  add 1 to result for child node itself
                int nDescendants = newD.getChildren((Element) n);

                children.add(newD);
                // testTarget=newD;

                if (nDescendants > maxDesc) maxDesc = nDescendants;
                descendantArray[cSize] = nDescendants;
                totalDescendants+=nDescendants;
                cSize++;
            }
        }

        //now that we've processed children, size them according to the
        //number of descendants they contain
        if (children != null) {
            if (Dbg.DOMAINS2) {
                if (getText().isEmpty() || getText().equals("spatial")) {
                    Dbg.print("Domain " + ("".equals(getText())? "ROOT": getText()) + " has " + children.size() + " children.");
                }
            }

            for (int j = 0; j < cSize; j++) {
                Domain child = children.get(j);

                // chris: this was a formula I came up with, but seems to work less well for modern big screens.
                // 3 factors: a base size for all circles, a component based on number of children, and a component based on number of desscendants.
                // Traditional equation was: child.relR = 0.07f + (0.2f / cSize) + (0.1f * descendantArray[j])/maxDesc;
                // Now altered a bit.  Problem: constant part should really be (partly) in pixels not proportion of display size
                child.relR = 0.04f + 0.08f/cSize + 0.18f * descendantArray[j]/maxDesc;
                if (Dbg.DOMAINS) {
                  String kidName = child.getText();
                  if (kidName.equals("spatial"))
                    Dbg.print("Kid " + kidName + " of " + ("".equals(getText())? "ROOT": getText()) + '(' + totalDescendants + " descendants in " + cSize + " children) has " + descendantArray[j] + " descendants and relR " + child.relR);
                }

                // Use random polar coordinate to distribute children better

                float randAngle = (float)(Math.random()*2*Math.PI);
                float randR =(float)(Math.random()*.5-child.relR-.1);


                child.relX=(float)(randR*Math.cos(randAngle))+.5f;
                child.relY=(float)(randR*Math.sin(randAngle))+.5f;

            }
        }
        //return how many children we have in total plus ourself
        return totalDescendants;
    }

    /* -- appears unused
    public Thread zoom(Domain requester, int zoomType, DomainTree tree, boolean
                       waitForFinish, boolean signalDone) {
        return null;
    }
    */

    static void populateZoomQueue(Vector<Domain> zoomQueue, Domain currentDom, Domain goalDom) {
        //      System.out.println("popzq: cur: " + currentDom.getText() + " goal: " + goalDom.getText());
        if (!(goalDom.isWord() && currentDom == goalDom.parent)) {
            zoomQueue.removeAllElements();
        }
        Vector<Domain> currentDomainInfo = new Vector<>(); //list of domains (down to
        Vector<Domain> goalDomainInfo = new Vector<>();
        Domain t1 = currentDom;
        Domain t2 = goalDom;

        while (t1 != null) {
            currentDomainInfo.insertElementAt(t1, 0);
            t1 = t1.parent;
        }

        while (t2 != null) {
            goalDomainInfo.insertElementAt(t2, 0);
            t2 = t2.parent;
        }

        int i, j;
        int stop = Math.min(currentDomainInfo.size(), goalDomainInfo.size());

        for (i=1;i<stop;i++) {
            if (currentDomainInfo.elementAt(i) != goalDomainInfo.elementAt(i))
                break; //this is point of difference
        }

        for(j = currentDomainInfo.size() - 2; j >= i-1; j--)
            zoomQueue.addElement(currentDomainInfo.elementAt(j)); //for zoom out

        for(j = i; j < goalDomainInfo.size(); j++)
            zoomQueue.addElement(goalDomainInfo.elementAt(j)); //for zoom in
    }

    public List<Domain> getChildren() { return children; }

    public void setSelected(boolean selected) { this.selected = selected; }
    public boolean isSelected() { return selected; }


    /* Accessors for actual numerical limit on number of children drawn
     *
     */

    public static int getMaxChildren() { return maxChildren; }
    public void setMaxChildren(int newMax) {
        maxChildren = newMax;
        findChildCutoff(true, dtree.childrenDrawingIsLimited());
    }


    private void findChildCutoff(boolean recurse, boolean applyLimits) {
        if(children == null) return;
        int nChildren = children.size();
        int wordCount = 0;
        childCutoffIndex = 0;
        for(int i = nChildren-1; i >= 0; i--) {
            Domain child = children.get(i);
            if(recurse)
                child.findChildCutoff(true, applyLimits);
            if(applyLimits) {
                if(child.isWord()) wordCount++;
                if(wordCount == maxChildren)
                    childCutoffIndex = i; //this is where we start drawing
            }
        }

    }

//    public void resizeChildren(float factor) {
//        if (children == null) return;
//        for (int i = 0, nChildren = children.size(); i < nChildren; i++)
//            ((Domain)children.elementAt(i)).resize(factor);
//    }

    /** These days all this method does is set the textWidth!! */
    public void resize(float factor) {
        if (Dbg.DOMAINS) {
            String name = getText();
            if (name.equals("spatial") || name.isEmpty()) {
                Dbg.print("Node " + name + " under parent " +
                          (parent == null ? "NULL" : parent.getText()) +
                          " resized with factor " + factor +
                          " to relR " + relR);
            }
        }
        if (textWidth == 0 && fm != null) {
            textWidth = fm.stringWidth(text);
        }
        // cdm jan 2005 fixes
        //         resizeChildren(factor);
        if (children == null) return;
        for (Domain child : children) {
            child.resize(factor);
        }
    }


    public void getImages(DictionaryCache cache,
                          DomainTree.ImageLoader loaderThread,
                          boolean showPicsOnly) {
        if (children == null) return;
        int nChildren = children.size();

        for (int i = childCutoffIndex; i < nChildren && !loaderThread.halted();
            i++) {
            Domain child = children.get(i);
            if (child.isWord()) {
                child.getImages(cache, loaderThread, showPicsOnly);
            }
        }
    }


    public void removeImages() {
        removeImages(childCutoffIndex);
    }

    /** Removes images for memory conservation.
     */
    public void removeImages(int startIndex) {
        if (children == null) return;
        int nChildren = children.size();
        for (int i = startIndex; i < nChildren; i++) {
            Domain child = children.get(i);
            if (child.isWord()) {
                child.removeImages();
            }
        }
    }

} // end class Domain

