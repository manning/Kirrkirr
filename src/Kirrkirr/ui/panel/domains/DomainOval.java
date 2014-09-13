package Kirrkirr.ui.panel.domains;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Color;

public class DomainOval extends Domain {

    /** @param x Relative x coordinate of center to parent (0 to 1 scale)
     *  @param y Relative y coordinate of center to parent (0 to 1 scale)
     *  @param r Radius (in x coordinate), 0 to 1 scale
     *  @param a Ratio of horizontal to vertical aspect
     *  @param p Parent domain
     *  @param dt DomainTree that this DomainOval belongs to
     */
    public DomainOval(float x, float y, float r, float a, Domain p, DomainTree dt) {
        super(x, y, r, a, p, dt);
    }

    public float getScrH() {
        return scrR*2/aspect;
    }

    public boolean contains(int x, int y) {
        return super.contains(x,(int) (scrY+((y-scrY)*aspect)));
    }


    public void drawShape(Graphics g)
    {
        g.fillOval((int)(scrX-scrR),(int)(scrY-(scrR/aspect)),(int)(scrR*2),(int)((scrR*2)/aspect));
    }

    public void drawText(Graphics g)
    {
        boolean scrolling = (textWidth > 2*scrR - 6);
        Rectangle oldClip = g.getClipBounds(); //cache old clip bounds

        Rectangle newClip = new Rectangle((int)(scrX-scrR+3), (int) (scrY-scrR/aspect+1), (int)(2*scrR-6), (int) (2*scrR/aspect-2));
        g.setClip(oldClip.intersection(newClip));

        if(!scrolling) {
            textxpos = (int)(getScrXCenter()-textWidth/2);
        }
        else { //scrolling - check bounds
            if(textxpos < (scrX-scrR - textWidth))
                textxpos = (int) scrX;
            else if(textxpos > scrX+scrR)
                textxpos = (int) scrX;
        }
        g.setColor(Color.black);
        g.setFont(font);
        int textypos = (int)(getScrYCenter()+fh/2);
        g.drawString(text, textxpos, textypos);
        if(scrolling) {
            g.drawString(text, (int) (textxpos + textWidth + scrR), textypos);
            textxpos--;
        }
        g.setClip(oldClip);
    }

// jan 2005 -- try deleting
//    public void resize(float factor) {
//        relR /= factor;
//        super.resize(factor);
//    }

}

