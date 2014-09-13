package Kirrkirr.ui.panel;

import Kirrkirr.ui.KirrkirrButton;
import Kirrkirr.ui.PicturePanelCallback;
import Kirrkirr.dictionary.DictFields;
import Kirrkirr.util.Helper;
import Kirrkirr.util.Dbg;
import Kirrkirr.util.RelFile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/** An interface for viewing pictures: gives previous/next functionality
 *  and allows deletions
 *  Note [cdm]: This should be generalized with a callback interface so
 *  that the MediaPanel can just use this one.
 *  @author Kevin Lim
 *  @author Christopher Manning
 */
public class SlideShowPanel extends JPanel implements ActionListener {

    private String defaultPic;
    private JScrollPane sp;
    private JViewport vp;
    private JLabel picLabel;
    private JLabel currentLabel;

    private Vector<String> currentImages;
    private int current = 0;
    private JButton prev, next;
    private PicturePanelCallback callback;

    /** The deletePending variable is set to true if the current picture is
     *  to be deleted when the user moves off it.
     */
    private boolean deletePending;

    private static final int SP_MIN_WIDTH = 200;
    private static final int SP_MIN_HEIGHT = 100;


    public SlideShowPanel(PicturePanelCallback callback, boolean
                            horizontal, boolean label, boolean icons,
                            String prev_str, String next_str,
                            Color mediaColor, String defaultPic)
    {
        super(true);

        this.defaultPic = defaultPic;
        this.callback = callback;
        currentImages = new Vector<>();

        initViewer();

        JComponent buttonBox = initButtonBox(horizontal, icons, label, prev_str,
                                             next_str, mediaColor);

        if (horizontal) {
            setLayout(new BorderLayout());
            add(sp, BorderLayout.CENTER);
            add(buttonBox, BorderLayout.SOUTH);
        } else {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setOpaque(true);
            if(mediaColor != null)
                setBackground(mediaColor);
            setForeground(Color.black);
            add(buttonBox);
            add(Box.createHorizontalStrut(2));
            add(sp);
            //TODO move me to media
            //setBorder(BorderFactory.createTitledBorder(Helper.getTranslation(SC_ILLUSTRATIONS)));
        }
    }

    private void initViewer() {
        picLabel = new JLabel();
        picLabel.setHorizontalAlignment(JLabel.CENTER);
        picLabel.setVerticalAlignment(JLabel.CENTER);
        picLabel.setDoubleBuffered(true);
        picLabel.setOpaque(true);
        picLabel.setBackground(Color.white);

        sp = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                             ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setMinimumSize(new Dimension(SP_MIN_WIDTH, SP_MIN_HEIGHT));
        sp.setDoubleBuffered(true);
        sp.setOpaque(true);
        sp.setBackground(Color.white);
        vp = sp.getViewport();
        vp.setView(picLabel);
    }

    private JComponent initButtonBox(boolean horizontal, boolean icons,
                                     boolean label,
                                     String prev_str, String next_str,
                                     Color mediaColor) {
        if(horizontal) {
            JComponent buttonBox = new JPanel();
            buttonBox.setLayout(new BoxLayout(buttonBox, BoxLayout.X_AXIS));


            prev = new KirrkirrButton(Helper.getTranslation(prev_str), this);
            prev.setEnabled(false);
            buttonBox.add(prev);
            buttonBox.add(Box.createGlue());

            next = new KirrkirrButton(Helper.getTranslation(next_str), this);
            buttonBox.add(next);

            return(buttonBox);
        } else {
            // only have pictures if not tiny.
            if (!icons) {
                prev = new KirrkirrButton(prev_str, this);
            } else {
                prev = new KirrkirrButton(prev_str, "Back16.gif", this);
            }
            prev.setVerticalTextPosition(AbstractButton.TOP);
            prev.setHorizontalTextPosition(AbstractButton.CENTER);
            prev.setMnemonic('p');
            prev.setEnabled(false);
            prev.setPreferredSize(new Dimension(80, 73));
            prev.setMaximumSize(new Dimension(80, 73));

            if(label)
                currentLabel = new JLabel(" "); // "10 of 10"

            if (!icons) {
                next = new KirrkirrButton(next_str, this);
            } else {
                next = new KirrkirrButton(next_str, "Forward16.gif", this);
            }
            next.setVerticalTextPosition(AbstractButton.TOP);
            next.setHorizontalTextPosition(AbstractButton.CENTER);
            next.setMnemonic('n');
            next.setPreferredSize(new Dimension(80, 73));
            next.setMaximumSize(new Dimension(80, 73));

            JPanel buttonPanel = new JPanel();

            // buttonPanel.setLayout(new FlowLayout()); [the default]
            buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
            // buttonPanel.setLayout(new GridLayout(3, 1));
            buttonPanel.setOpaque(true);
            if(mediaColor != null)
                buttonPanel.setBackground(mediaColor);
            buttonPanel.setForeground(Color.black);
            buttonPanel.add(Box.createVerticalGlue());
            buttonPanel.add(prev);
            buttonPanel.add(Box.createVerticalStrut(8));
            if(label)
                buttonPanel.add(currentLabel);
            buttonPanel.add(Box.createVerticalStrut(8));
            buttonPanel.add(next);
            buttonPanel.add(Box.createVerticalGlue());
            buttonPanel.setMaximumSize(new Dimension(80, 500));

            return(buttonPanel);
        }
    }

    /**
     * returns the size (num pictures) of the slide show.
     */
    public int getShowSize() {
        return currentImages.size();
    }

    /**
     * returns the current index in the slide show.
     */
    public int getShowIndex() {
        return current;
    }

    /**
     * sets the text of the label(if one exists).
     */
    public void setLabelText(String text) {
        if(currentLabel == null) return;

        currentLabel.setText(text);
    }

    //this must go!
    public void enableButtons(boolean enable) {
        prev.setEnabled(enable);
        next.setEnabled(enable);
    }

    /** Handles the next and previous buttons for picture display
     *  The pictures are in the currentImages Vector, with indexes
     *  0 to size() - 1.  The current picture is pointed to by the
     *  index current.
     *  Assertion: The buttons will only be enabled (and pressable) if
     *  there is a next/previous picture.
     *  Assertion: there must be a next/prev event between deletions
     *  @param e The button event to handle
     */
    @Override
    public void actionPerformed(ActionEvent e)
    {
        Object obj = e.getSource();

        if (obj == next) {
            if (deletePending) {
                deletePending = false;
                currentImages.removeElementAt(current);
                // current is now pointing at what was next
            } else {
                current++;
            }
            if (current > 0) {
                prev.setEnabled(true);
            }
            int size = currentImages.size();  // may just have changed!
            if (current >= size - 1) {
                next.setEnabled(false);
                current = size - 1;
            }
        } else if (obj == prev) {
            if (deletePending) {
                deletePending = false;
                currentImages.removeElementAt(current);
            }
            current--;
            int size = currentImages.size();  // may just have changed!
            if (current < size - 1) {
                next.setEnabled(true);
            }
            if (current <= 0) {
                prev.setEnabled(false);
                current = 0;
            }
        }

        setImage(currentImages.elementAt(current));

        callback.imageUpdated();
    }


  public String getCurrFilename()
  {
    if(currentImages.isEmpty()) {
        return "";
    } else {
        return currentImages.elementAt(current);
    }
  }

  public void refreshImage() {

  }

    public void setCollection(DictFields images) {
        currentImages.removeAllElements();

        prev.setEnabled(false);
        next.setEnabled(false);
        current = 0;

        if (images == null || images.size() == 0) {
            setImage(null);
        } else {
            for (int i=0, iSize = images.size(); i < iSize; i++) {
                currentImages.addElement(images.get(i).uniqueKey);
            }

            setImage(images.get(current).uniqueKey);
            if (images.size() > 1) {
                next.setEnabled(true);
            }
        }
    }

    public void addNewPic(String filename)
    {
        currentImages.addElement(filename);
        if(currentImages.size() == 1)
            setImage(filename);
        else if(currentImages.size() == 2 || next != null)
            next.setEnabled(current == 0);
    }

    /** This is called to delete the current picture from the list.
     *  However, unless there is only one picture remaining, the picture
     *  is still displayed for the user to look
     *  at, so a flag has to be set so that the next and previous work
     *  specially next time they are called.
     */
    public void deleteCurrentPic() {
        int size = currentImages.size();
        if (size == 0) {
            if (Dbg.ERROR) Dbg.print("deleteCurrentPic called with no pic");
        } else if (size == 1) {
            currentImages.removeElementAt(current);
            setImage(null);
        } else {
            deletePending = true;
        }
    }


    public void clearPicNames()
    {
        currentImages.removeAllElements();
        setImage(null);
        current = 0;
        prev.setEnabled(false);
        next.setEnabled(false);
    }

    /** Makes the image in filename the currently displayed image.
     *  If filename is null, displays the default image.
     *  The image is centered in the viewport.
     *  @param filename The image filename to be displayed
     */
    private void setImage(String filename) {
        ImageIcon picture;
        if (filename == null) {
          picture = RelFile.makeImageIcon(defaultPic, false);
      } else {
          picture = RelFile.makeImageIcon(filename, true);
      }

      int picWidth = picture.getIconWidth();
      int picHeight = picture.getIconHeight();
      int spWidth = sp.getWidth();
      int spHeight = sp.getHeight();
      int sbWidth = sp.getVerticalScrollBar().getWidth();
      int sbHeight = sp.getHorizontalScrollBar().getHeight();

      // Notes: vp.getWidth() vp.getHeight() may return 0 at beginning
      //        vp.getViewSize() returns size of previous picture
      //        vp.getViewPosition() is previous upper left
      //        vp.getViewRect() is previous whole rectangle
      // Since you want to get at whether there are scrollbars, using
      // sp.getWidth() and sp.getHeight() actually seems more reliable.
      // they may also be 0 at the beginning

      if (Dbg.VERBOSE) {
          Dbg.print("Pic W,H: " + picWidth+" "+picHeight);
          Dbg.print("SP W,H, SB W,H: " + spWidth + " " + spHeight + " " +
                    sbWidth + " " + sbHeight);
      }

      if (spWidth > 0 && picWidth > spWidth) {
          spHeight -= sbHeight;  // there'll be a horizontal scrollbar
      }
      if (spHeight > 0 && picHeight > spHeight) {
          spWidth -= sbWidth;    // there'll be a vertical scrollbar
      }
      if (spWidth == 0) {
          spWidth = SP_MIN_WIDTH;   // width not yet set; assume "sensible"
      }
      if (spHeight == 0) {
          spHeight = SP_MIN_HEIGHT;   // height not yet set; assume "sensible"
      }
      int px = (picWidth - spWidth) / 2;
      int py = (picHeight - spHeight) / 2;
      if (px < 0)
          px = 0;
      if (py < 0)
          py = 0;
      // doing it this way avoids nasty movements of a picture.  It'd be
      // even cleaner if one could just disable display updating between
      // the last two operations.
      picLabel.setIcon(null);
      vp.setViewPosition(new Point(px, py));       //coords of upper-left corner
      picLabel.setIcon(picture);
      if (Dbg.VERBOSE) {
          Dbg.print("Setting to x,y: " + px + " " + py);
          Dbg.print("image painted: " + filename);
      }
  }

}

