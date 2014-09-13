package Kirrkirr.ui.panel.fun;

// LinkEdgeModelListener.java
/**
 * Interface to listen for edge change notifications.
 * The various notifications include a pointer to the model that changed,
 * and the methods indicate what the change was.
 */

public interface LinkEdgeModelListener {

    public void edgeColorChanged(LinkEdgeModel changed);
    public void edgePointsChanged(LinkEdgeModel changed);
    public void edgeChanged(LinkEdgeModel changed, int changeType);

}

