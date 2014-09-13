package Kirrkirr.ui;

import java.io.Serializable;

/**
 * Interface designed for use with PictureSlideShow. The method imageUpdated
 * will be
 * called when the image being displayed in the slide show is changed.
 */

public interface PicturePanelCallback extends Serializable {

    /**
     * Called when the image of the slideshow is updated.
     */
    public void imageUpdated();

}

