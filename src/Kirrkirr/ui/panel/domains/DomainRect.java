package Kirrkirr.ui.panel.domains;

import Kirrkirr.dictionary.DictFields;
import Kirrkirr.dictionary.DictionaryCache;
import Kirrkirr.util.Dbg;
import Kirrkirr.util.Helper;
import Kirrkirr.util.RelFile;

import javax.swing.*;
import java.awt.*;


public class DomainRect extends Domain {

    private static final int TEXT_ONLY = 0;
    private static final int THUMB_ONLY = 1;
    private static final int TEXT_THUMB = 2;

    private static final int NODE_PADDING = 12;
    protected static final int COMBO_THUMB_SIZE = 24;
    protected static final int SOLO_THUMB_SIZE = 48;

    private int imageMode = TEXT_ONLY;
    protected ImageIcon imageIcon; //for image thumbnails


    public DomainRect(float x, float y, float r, float a, Domain p, DomainTree dt) {
        super(x, y, r, a, p, dt);
    }


    @Override
    public boolean contains(int x, int y) {
        return x >= (scrX-scrR) && x <= (scrX+scrR) &&
            y >= (scrY-scrR/aspect) && y <= (scrY+scrR/aspect);
    }


    @Override
    public boolean contained() {
        return parent.contains((int)(scrX-scrR),(int)(scrY-(scrR/aspect))) &&
            parent.contains((int)(scrX+scrR),(int)(scrY-(scrR/aspect))) &&
            parent.contains((int)(scrX-scrR),(int)(scrY+(scrR/aspect))) &&
            parent.contains((int)(scrX+scrR),(int)(scrY+(scrR/aspect)));
    }


    @Override
    public float getScrH() {
        if (imageMode == TEXT_ONLY) {
            return Domain.fh + (NODE_PADDING/2);
        } else if (imageMode == TEXT_THUMB) {
            return Domain.fh + COMBO_THUMB_SIZE + NODE_PADDING;
        } else {
            return SOLO_THUMB_SIZE + NODE_PADDING;
        }
    }


    @Override
    public void drawText(Graphics g) {
        if (imageMode != THUMB_ONLY) {
            g.setColor(Color.black);
            g.setFont(font);
            int textypos = (int) (getScrYCenter() - getScrH()/2 + fasc + 2);
            g.drawString(text, (int)(getScrXCenter()-textWidth/2), textypos);
        }
    }


    @Override
    public void drawShape(Graphics g) {
        // don't draw it if too small
        if ((int) ((scrR*2)/aspect) < 6) {
            return;
        }
        if (false && Dbg.DOMAINS) {
            Dbg.print("Drawing " + text + " rectangle from (" +
                      ((int)(scrX-scrR)) + "," + ((int)(scrY-(scrR/aspect))) +
                      ") w " +  ((int)(scrR*2)) + " h " + ((int)((scrR*2)/aspect)));
            Dbg.print("  scrX=" + scrX + " scrY=" + scrY + " scrR=" + scrR + " aspect=" + aspect);
        }
        g.fillRect((int)(scrX-scrR),(int)(scrY-(scrR/aspect)),(int)(scrR*2),(int)((scrR*2)/aspect));
        if (selected) {
            g.setColor(Color.black);
            g.drawRect((int)(scrX-scrR),(int)(scrY-(scrR/aspect)),(int)(scrR*2),(int)((scrR*2)/aspect));
        }
        if (imageMode == TEXT_THUMB) {
            imageIcon.paintIcon(null, g, (int)(scrX-COMBO_THUMB_SIZE/2.0f), (int)(scrY+getScrH()/2.0f-COMBO_THUMB_SIZE-6));
        } else if (imageMode == THUMB_ONLY) {
            imageIcon.paintIcon(null, g, (int)(scrX-SOLO_THUMB_SIZE/2.0f), (int)(scrY-getScrH()/2.0f+6));
        }
    }


    /** The DomainRect nodes are sized based simply on their content, and so
     *  the resize factor is ignored
     *  @param factor An ignored parameter for this subclass
     */
    @Override
    public void resize(float factor) {
        if (imageMode != THUMB_ONLY && textWidth == 0 && Domain.fm != null) {
            textWidth = Domain.fm.stringWidth(text);
            constantAspect = aspect =
                ((float)(textWidth+NODE_PADDING))/(Domain.fh + (NODE_PADDING/2));
        }

        float width;
        if (imageMode == THUMB_ONLY) {
            width = SOLO_THUMB_SIZE + NODE_PADDING;
        } else if (imageMode == TEXT_THUMB) {
            width = textWidth > COMBO_THUMB_SIZE ? textWidth: COMBO_THUMB_SIZE;
            width += NODE_PADDING;
        } else {
            width = textWidth+NODE_PADDING;
        }
        relR = width/(2*dtree.scrW);
    }


    private void sizeForImage(int imageMode) {
        this.imageMode = imageMode;
        if (imageMode == TEXT_THUMB) {
            constantAspect = aspect =
                ((float) (textWidth+NODE_PADDING))/(Domain.fh+COMBO_THUMB_SIZE+8);
            // the 8 is for padding
        } else if (imageMode == THUMB_ONLY) {
            constantAspect = aspect = 1.0f;
        } else {
            constantAspect = aspect =
                ((float)(textWidth+NODE_PADDING))/(Domain.fh + (NODE_PADDING/2));
        }
        resize(0.0f);
    }


    /** Removes images for memory conservation.  Here, there are images, but
     *  no children.
     */
    @Override
    public void removeImages() {
        imageIcon = null;
        sizeForImage(TEXT_ONLY); //shrink back to text size
    }


    @Override
    public void getImages(DictionaryCache cache,
                               DomainTree.ImageLoader loaderThread,
                               boolean showPicsOnly) {
        // a Rect node doesn't have children
        DictFields pictures =
            cache.getPictures(Helper.printableStringToUniqueKey(getText()));
        if (pictures == null || pictures.size() == 0) return;
        // if have image, set it
        ImageIcon icon = RelFile.makeImageIcon(pictures.get(0).uniqueKey, true);
        Image im = icon.getImage();
        int size = showPicsOnly ? SOLO_THUMB_SIZE: COMBO_THUMB_SIZE;
        icon.setImage(im.getScaledInstance(size, size, Image.SCALE_FAST));
        imageIcon = icon;
        sizeForImage(showPicsOnly ? THUMB_ONLY: TEXT_THUMB);
    }

}

