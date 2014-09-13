package Kirrkirr.ui.panel;

import Kirrkirr.Kirrkirr;
import Kirrkirr.ui.panel.fun.FunModel;
import Kirrkirr.ui.panel.fun.FFFPanel;
import Kirrkirr.ui.panel.fun.WordNodeModel;
import Kirrkirr.ui.panel.optionPanel.FunPanelOptionPanel;
import Kirrkirr.ui.panel.optionPanel.KirrkirrOptionPanel;
import Kirrkirr.util.Helper;
import Kirrkirr.util.Dbg;
import Kirrkirr.util.KRandom;

import java.awt.*;

import javax.swing.*;


/** This panel manages the window system for the network display. */
public class GraphPanel extends KirrkirrPanel {

    private static final int VIEW_OFFSET=10;
    // private boolean toppane;
    private static final String SC_GRAPH_ROLLOVER="Graphical_network_layout_of_words";
    private static final String SC_GRAPH_NAME="Network";

    private FunPanelOptionPanel optionPanel;
    private JLayeredPane zplane;
    private KRandom colors; // for producing window colors
    private int kirrkirrSize;
    private int maxNumViews; // max number windows, set by user
    private int red, green, blue; // for window differentiation
    //private static Color defSelectedColor = Color.yellow;
    //private Color defNodeColor;
    //private static Color defNodeNotFoundColor = Color.gray;
    private Color selectedColor, defaultColor, notFoundColor;


    public GraphPanel(Kirrkirr kparent, JFrame window, boolean istoppane,
                      int kirrkirrSize) {
        super(kparent, window);
        this.parent = kparent;
        this.window = window;
        // this.toppane = istoppane;
        this.kirrkirrSize = kirrkirrSize;
        setName(Helper.getTranslation(SC_GRAPH_NAME));

        // setting default colors
        selectedColor = Color.yellow;
        defaultColor = new Color(149, 253, 124);   // light green
        notFoundColor = Color.gray;

        red=green=blue=150;
        Rectangle bounds = window.getBounds();
        setLayout(new BorderLayout());
        this.setBounds(bounds);
        maxNumViews = 4;  // hard-coded max # of windows, should be user set
        zplane = new JLayeredPane(); // the window space
        colors = new KRandom();
        add(zplane, BorderLayout.CENTER);
    }

    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);

        // below is only in jdk1.4 -- could test for version if useful
        // if(!parent.window.isActive()) return;

        if(zplane != null)
            updateBounds();
    }

    // Returns the top panel for purposes of word selection or creation.
    // First it obtains the highest layer in the layeredPane, then returns
    // its FFFPanel component.

    public FFFPanel getTopFFFPanel() {
        int topLayer = zplane.highestLayer();
        Component[] comps = zplane.getComponentsInLayer(topLayer);
        if (comps.length>1) {
            if(Dbg.NEWFUN) Dbg.print("Warning: multiple panes in layer.");
        }
        // getting top FFFPanel for word adding or selection
        JPanel topPanel = (JPanel)comps[0];
        FFFPanel fpanel  = (FFFPanel)topPanel.getComponent(0);
        return fpanel;
    }

    // Predicate for determining if the parameter FFFPanel is the top
    // panel in the layered pane.
    public boolean isTopFFFPanel(FFFPanel fpanel) {
        return (getTopFFFPanel() == fpanel);
    }



    // Creates new random colors for window generation. Checks to see
    // that the new color is relatively different from that of the
    // previous window, for visibility. This should be extended to check
    // against the colors of all extant windows.
    private void makeNewColors(FFFPanel fpanel){
        int nred, ngreen, nblue;
        // getting random 'pastel' colors, checking against
        // previous colors.
        do {
            nred = 150+colors.nextInt(105);
            ngreen = 150+colors.nextInt(105);
            nblue = 150+colors.nextInt(105);
        } while ((Math.abs(nred-red)<10) &&
                 (Math.abs(ngreen-green)<10) &&
                 (Math.abs(nblue-blue)<10));

        // setting acceptable new colors
        red = nred;
        green = ngreen;
        blue = nblue;
        fpanel.setBackground(new Color(red, green, blue));
    }


    // The main window-adding function creates the new outer and inner
    // panels, calls the color-setting function and the window-inserting
    // functions.
    private void addView(){
        JPanel currPanel = new JPanel();
        currPanel.setLayout(new BorderLayout());
        currPanel.setBorder(BorderFactory.createLineBorder(Color.black));

        // getting the inner window and its model
        FunModel fmodel = new FunModel(this);
        FFFPanel fpanel = new FFFPanel(fmodel, this, kirrkirrSize <=
        KirrkirrPanel.TINY);

        // choosing new random colors
        makeNewColors(fpanel);
        if(Dbg.NEWFUN) Dbg.print("The color of this panel: "+fpanel.getBackground());
        currPanel.add(fpanel, BorderLayout.CENTER);

        // checking window ordering, layout, etc.
        insertNewPanel(currPanel);
        fpanel.start();
        repaint();
    }


    // Adds the newest panel to the top layer of the layeredPane,
    // then deletes the bottom panel if necessary (i.e. there are too
    // many), and updates the panel locations/dimensions.
    private void insertNewPanel(JPanel newPanel){
        int highLayer = zplane.highestLayer();
        Component[] toStop = zplane.getComponentsInLayer(highLayer);
        if(toStop != null && toStop.length >0) {
            ((FFFPanel)((JPanel)toStop[0]).getComponent(0)).stop();
        }
        zplane.add(newPanel, new Integer(highLayer+1));

        if(Dbg.NEWFUN) Dbg.print("Adding layer at " + (highLayer+1));
        deleteIfNecessary();
        updateBounds();
        repaint();
    }


    // If there are more panels open that the limit, this deletes
    // the bottommost panel, and reduces the layer of all remaining
    // panels by one.
    private void deleteIfNecessary(){
        // if more panels than allowed
        if(zplane.getComponentCount() > maxNumViews){
            int lowest = zplane.lowestLayer();
            Component[] deletable = zplane.getComponentsInLayer(lowest);
            // deleting lowest panel
            if (deletable[0] instanceof JPanel) {
                zplane.remove(deletable[0]);
                ((FFFPanel) ((JPanel)deletable[0]).getComponent(0)).stop();
            }
            Component[]comps = zplane.getComponents();
            // reducing remaining panel depths by one
            for(int i=0; i<comps.length; i++){
                JPanel currPanel = (JPanel) comps[i];
                zplane.setLayer(currPanel, zplane.getLayer(currPanel)-1);
            }
        }
    }

    //Given an empty FFFPanel, removes it from the GraphPanel.
    public void deleteEmptyFunPanel(FFFPanel toDelete) {
        zplane.remove(toDelete.getParent());
        Component[] comps = zplane.getComponents();
        if(comps.length !=0) {
            FFFPanel fpanel = getTopFFFPanel();
            WordNodeModel wmodel = fpanel.getModel().getSelected();
            if(wmodel!=null)
                selectedNodeChanged(wmodel.getUniqueKey());
            fpanel.start(); //restart anim on new top panel
        }
        this.repaint();
    }


    // looks through each of the existing panels to see if
    // the passed-in word exists in any of their underlying
    // funmodels. If it finds a match, it makes that panel
    // appear on top, otherwise it calls addView to make a
    // new panel appear on top.
    private void checkWordPosition(String uniqueKey){
        Component[] comps = zplane.getComponents();
        // checking panels for word
        for(int i=0; i<comps.length; i++){
            JPanel currPanel = (JPanel)comps[i];
            FFFPanel fpanel = (FFFPanel)currPanel.getComponent(0);
            FunModel fmodel = fpanel.getModel();
            // if there is a match
            if(fmodel.wordBelongsHere(uniqueKey)){
                if(Dbg.NEWFUN) Dbg.print("Word belongs in #" + zplane.getLayer(currPanel) + "screen.");
                reshuffle(fpanel); // put this panel on top
                return;
            }
        }
        if(Dbg.NEWFUN) Dbg.print("Making new screen.");
        addView(); // if no match, make new panel
    }



    // checks the panel to be reshuffled against the current top panel.
    // if they don't match, makes the chosen panel the new top panel.
    public void reshuffle(FFFPanel fpanel){
        if(Dbg.NEWFUN) Dbg.print("This panel to be reshuffled: " + fpanel.getBackground());
        if(fpanel.equals(getTopFFFPanel())) {// no reshuffling necessary
        }
        else{   // updates the layers and dimensions/locations
            JPanel topPanel = (JPanel) fpanel.getParent();
            updateLayers(topPanel);
            updateBounds();
            repaint();
        }
    }


    // sets the chosen panel to have the highest layer, and updates
    // the layers of the other panels to preserve their relative order
    // below the selected panel.
    private void updateLayers(JPanel topPanel){
        Component[] comps = zplane.getComponents();
        int numViews = comps.length;
        int highLayer = zplane.highestLayer();
        int oldLayer = zplane.getLayer(topPanel);
        for(int i = 0; i<numViews; i++){
            JPanel currView = (JPanel) comps[i];
            int currLayer = zplane.getLayer(currView);
            if(currLayer == highLayer) { //pause anim on old top layer
                ((FFFPanel)currView.getComponent(0)).stop();
            }
            if(currView.equals(topPanel)) {
                zplane.setLayer(currView, highLayer);
                ((FFFPanel)currView.getComponent(0)).start(); //restart anim
                        //on back panels
            }
            else if(currLayer>oldLayer) {
                zplane.setLayer(currView, currLayer-1);
            }
        }
        repaint();
    }


    // gets the current selected node color
    public Color getSelectedNodeColor(){
        if(zplane==null) return Color.yellow; // default
        /*      FFFPanel fpanel = getTopFFFPanel();
        FunModel fmodel = fpanel.getModel();
        return fmodel.getSelectedNodeColor();*/
        return selectedColor;
    }


    // sets the current selected node color (in all screens)
    public void setSelectedNodeColor(Color newColor){
        selectedColor = newColor;
        Component[] comps = zplane.getComponents();
        int numViews = comps.length;
        for(int i = 0; i<numViews; i++){
            JPanel currView = (JPanel) comps[i];
            FFFPanel fpanel = (FFFPanel)currView.getComponent(0);
            FunModel fmodel = fpanel.getModel();
            fmodel.setSelectedNodeColor(newColor);
        }
    }


    // gets the default node color
    public Color getDefaultNodeColor(){
        if(zplane==null) return (new Color(149, 253, 124));
        /*      FFFPanel fpanel = getTopFFFPanel();
        FunModel fmodel = fpanel.getModel();
        return fmodel.getDefaultNodeColor();*/
        return defaultColor;
    }


     // sets the default node color (in all screens)
    public void setDefaultNodeColor(Color newColor){
        defaultColor = newColor;
        Component[] comps = zplane.getComponents();
        int numViews = comps.length;
        for(int i = 0; i<numViews; i++){
            JPanel currView = (JPanel) comps[i];
            FFFPanel fpanel = (FFFPanel)currView.getComponent(0);
            FunModel fmodel = fpanel.getModel();
            fmodel.setDefaultNodeColor(newColor);
        }
    }


    // gets the "not found" node color
    public Color getNotFoundNodeColor(){
        if(zplane==null) return Color.gray;
        /*      FFFPanel fpanel = getTopFFFPanel();
                FunModel fmodel = fpanel.getModel();
                return fmodel.getNotFoundNodeColor();*/
        return notFoundColor;
    }


/*
    // sets the "not found" node color (in all screens)
    public void setNotFoundNodeColor(Color newColor){
        notFoundColor = newColor;
        Component[] comps = zplane.getComponents();
        int numViews = comps.length;
        for(int i = 0; i<numViews; i++){
            JPanel currView = (JPanel) comps[i];
            FFFPanel fpanel = (FFFPanel)currView.getComponent(0);
            FunModel fmodel = fpanel.getModel();
            fmodel.setNotFoundNodeColor(newColor);
        }
    }
*/


    // this goes through the array of visible panels, sets
    // their position based on their depth, and their size
    // based on total number of panels.
    private void updateBounds(){
        Component[] comps = zplane.getComponents();
        int numViews = comps.length;
        for(int i=0; i<numViews; i++){
            // getting current panel
            JPanel currView = (JPanel) comps[i];
            FFFPanel currPanel = (FFFPanel)currView.getComponent(0);
            int currLayer = zplane.getLayer(currView);
            Rectangle bounds = this.getBounds();
            // setting x and y so that older panels are higher in y
            // and lower in x than newer panels.
            bounds.x = (numViews-currLayer) * VIEW_OFFSET;
            bounds.y = (currLayer-1) * VIEW_OFFSET;
            // width and height adjusted to # of panels
            bounds.width = bounds.width - (numViews-1) * VIEW_OFFSET;
            bounds.height = bounds.height - (numViews-1) * VIEW_OFFSET;
            currView.setBounds(bounds);
            // needed for mouse listeners, animation to work
            currPanel.setSize(new Dimension(bounds.width-2,
                                            bounds.height-2));
            currPanel.shiftNodesOnResize();
        }
        repaint();
    }


    // needed to extend KirrkirrPanel
    public String getTabRollover() {
        return Helper.getTranslation(SC_GRAPH_ROLLOVER);
    }


    // Overrides KirrkirrPanel. The current argument list is outdated, and
    // kept mainly to agree with Kirrkirr.java. Should be cleaned up
    // in the future.

    // setCurrentWord adds a panel if none are present, then checks to see
    // if the selected word can be placed in a panel, creating that panel
    // if necessary. Then, the funmodel is obtained and the node is
    // obtained, triggering the drawing of the word.
    public void setCurrentWord(/* padded */ String uniqueKey, boolean gloss,
                        final JComponent signaller, final int signallerType,
                        final int arg)
    {
        if(gloss) return;
        if(zplane.getComponentCount()==0) addView();
        checkWordPosition(uniqueKey);
        FFFPanel fpanel = getTopFFFPanel();
        FunModel fmodel = fpanel.getModel();
        WordNodeModel wmodel = fmodel.obtainNode(uniqueKey, true, null);
        fmodel.setSelected(wmodel);
        fpanel.moveNodeToFront(wmodel);
        fpanel.setToolTipText(Helper.uniqueKeyToPrintableString(uniqueKey));
    }


    /** Called by Kirrkirr when the user clicks "switch to headword"
     *  or "switch to gloss." Most panels will probably implement this
     *  to disable or enable certain features.
     *  @param toGloss true when the scroll list was switched to gloss
     */
    public void scrollPanelChanged(boolean toGloss) {
    }


    // this function called from within the fun directory to
    // set selected words from the panel rather than the wordlist. -always
    // pass word itself since we display only words?
    public void selectedNodeChanged(String uniqueKey){
        parent.setCurrentWord(uniqueKey, false, this, parent.GRAPH, 1);
    }

    // this function instantiates and returns the network option panel
    public KirrkirrOptionPanel getOptionPanel() {
         optionPanel = new FunPanelOptionPanel(parent, this);
         return optionPanel;
    }


    // eventually remove this and take it out of Kirrkirr.java
    public void start() {
        if(zplane == null) return;
        Component[] comps = zplane.getComponents();
        for(int i = 0; i < comps.length; i++) {
            ((FFFPanel)((JPanel) comps[i]).getComponent(0)).start();
        }
    }
    public void stop() {
        if(zplane == null) return;
        Component[] comps = zplane.getComponents();
        for(int i = 0; i < comps.length; i++) {
            ((FFFPanel)((JPanel) comps[i]).getComponent(0)).stop();
        }
    }
    public void run(){}
}

