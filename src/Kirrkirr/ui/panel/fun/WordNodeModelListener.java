package Kirrkirr.ui.panel.fun;

// WordNodeModelListener.java
/**
 * Interface to listen for node change notifications.
 * The various notifications include a pointer to the model that changed,
 * and the methods indicate what the change was.
 */

public interface WordNodeModelListener {
    // color change
    public void nodeColorChanged(WordNodeModel changed);

    // x, y change
    public void nodeLocationChanged(WordNodeModel changed);

    // width, height change
    public void nodeSizeChanged(WordNodeModel changed);

    // x, y, width, height change
    public void nodeBoundsChanged(WordNodeModel changed);

    // generic change notification.  see WordNodeModel for changeType consts.
    public void nodeChanged(WordNodeModel changed, int changeType);

}

