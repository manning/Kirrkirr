package Kirrkirr.ui.panel.fun;

// FunModelListener.java
/**
 * Interface to listen for canvas model change notifications.
 * The various notifications include a pointer to the model that changed,
 * and the methods indicate what the change was.
 */

public interface FunModelListener {

    public void funNodeModelCreated(FunModel changed, WordNodeModel
				    newNodeModel, boolean isFocus);

    public void funEdgeModelCreated(FunModel changed, LinkEdgeModel
				    newEdgeModel);

    public void funNodeModelRemoved(FunModel changed, WordNodeModel
				    nodeModel);

    public void funEdgeModelRemoved(FunModel changed, LinkEdgeModel
				    edgeModel);

    // generic change notification
    public void funModelChanged(FunModel changed);

}

