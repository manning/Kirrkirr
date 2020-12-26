package Kirrkirr.ui.panel;

import Kirrkirr.Kirrkirr;
import Kirrkirr.dictionary.DictFields;
import Kirrkirr.util.Helper;
import Kirrkirr.util.Dbg;
import Kirrkirr.util.RelFile;
import Kirrkirr.ui.KirrkirrButton;
import Kirrkirr.ui.PicturePanelCallback;

import javax.swing.*;
import java.applet.*;
import java.awt.*;
import java.awt.event.*;


/** MediaPanel: displays the multimedia files associated
 *  with a given word. Can have pictures and sounds.
 */
public class MediaPanel extends KirrkirrPanel implements ActionListener, PicturePanelCallback {

    private final SoundPanel soundPanel;
    private final SlideShowPanel picturePanel;

    private DictFields currentImages; // = null;
    private static final String defaultPic = "default.gif";

    /** String constants (SC) that need to be translated for MediaPanel.java
     */
    private static final String SC_MEDIA_NAME     = "Multimedia";
    private static final String SC_MEDIA_ROLLOVER = "See_pictures_and_hear_sounds_for_words";
    private static final String SC_PREVIOUS       = "Previous";
    private static final String SC_NEXT           = "Next";
    private static final String SC_OF             = "of";
    private static final String SC_PLAY           = "Play_Word";
    private static final String SC_STOP           = "Stop";
    private static final String SC_LISTEN         = "Listen_to_the_word";

    public MediaPanel(Kirrkirr kparent, JFrame window, int size) {
	super(kparent, window);
        setName(Helper.getTranslation(SC_MEDIA_NAME));

	Color mediaColor = new Color(255, 154, 53);

        soundPanel = new SoundPanel(parent, size);
	picturePanel = new SlideShowPanel(this, false, true,
					    (size > KirrkirrPanel.TINY),
					    SC_PREVIOUS,
					    SC_NEXT, mediaColor,
					    defaultPic);

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setOpaque(true);
        setBackground(mediaColor);
        setForeground(Color.black);
        add(soundPanel);
        add(picturePanel);
    }


    /** Returns the String that is suitable rollover text for a tabbed
     *  pane containing this panel.
     *  @return the string to be used as rollover text
     */
    @Override
    public String getTabRollover() {
	return Helper.getTranslation(SC_MEDIA_ROLLOVER);
    }


  /** Used to avoid multi-loading pictures when in gloss mode, but it's
   *  done by a messy trick which should be redone.
   */
    private boolean batchImages = false;

    /** Called by Kirrkirr when JComponents (History, ScrollPanel,
     *  KirrkirrPanels) update the current word. Clears the pictures and
     *  sounds of the old word,
     *  and if the new word has pictures and/or sound, adds the new ones.
     *  The implementation of this currently relies on a dirty trick when in
     *  Gloss mode to decide when to stop batching pictures from
     *  multiple setCurrentWord calls and when to restart.  Should be
     *  redone when setCurrentWord is rethought.
     */
    @Override
    public void setCurrentWord(/* padded */ String tailWord, boolean gloss,
                                            final JComponent signaller, final int signallerType,
                                            final int arg)
    {
	if (Dbg.K) {
	    Dbg.print("MP: setCurrentWord: " + tailWord + " gloss is " +
		      gloss + " signallerType is " + signallerType +
		      " batchImages is " + batchImages);
	}

	if (gloss) {
	    if (batchImages) {
		batchImages = false;
	    } else {
		picturePanel.enableButtons(false);
	    }
	    soundPanel.setNoCurrentClip();
	    return;
	}
        if (signallerType != Kirrkirr.DICTIONARYCACHE) {
	    batchImages = false;
	}
	if ( ! batchImages) {
	    currentImages = parent.cache.getPictures(tailWord);
	    if (signallerType == Kirrkirr.DICTIONARYCACHE) {
		batchImages = true;
	    }
	} else {
	    if (Dbg.K) {
		Dbg.print("MP: unioning " + currentImages +
			  "\n  and " + parent.cache.getPictures(tailWord));
	    }
	    if (currentImages == null) {
		currentImages = parent.cache.getPictures(tailWord);
	    } else {
		currentImages.union(parent.cache.getPictures(tailWord));
	    }
	}
	if (Dbg.K) {
	    Dbg.print("MP: currentImages is " + currentImages);
	}

	picturePanel.setCollection(currentImages);

        if (currentImages == null || currentImages.size() == 0) {
	    picturePanel.setLabelText("");
        } else {
            picturePanel.setLabelText((picturePanel.getShowIndex()+1)+
				      " "+SC_OF+" "+
				      currentImages.size());
        }

	soundPanel.setCurrentClip(tailWord);
    }


    /** Listener for the next and previous buttons. Updates
     *  images and labels appropriately.
     *  Assumption: next and/or previous should only be enabled if there are
     *  pictures, so we can assume that currentImages is non-null.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
    }

    @Override
    public void imageUpdated() {
	picturePanel.setLabelText((picturePanel.getShowIndex()+1)+" "+SC_OF+" "+picturePanel.getShowSize());
    }

    static class SoundPanel extends JPanel implements ActionListener {

        private final JButton playAu;
        private final JButton stopAu;
        private final JButton previous;
        private final JButton next;
        private final JLabel currentLabel;
        private int current = 0;
        private AudioClip currentClip; //=null;
        private final Kirrkirr parent;
        private DictFields currentSounds;

        private boolean audioCapable = true;

        private final JPanel dir_p;
        private final Dimension maximumSize = new Dimension(200, 1000);
        private final Dimension minimumSize = new Dimension(140, 100);
	// you need it to be wide enough for play word and stop to appear
	// next to each other (FlowLayout...).  140 is about the minimum

        public Dimension getMinimumSize() {
            return minimumSize;
        }

        public Dimension getMaximumSize() {
            return maximumSize;
        }

        public Dimension getPreferredSize() {
            return minimumSize;
        }

	SoundPanel(Kirrkirr k) {
	    this(k, KirrkirrPanel.NORMAL);
	}

        SoundPanel(Kirrkirr parent, int size) {
            super();
            this.parent = parent;

            Color soundPanelColor = new Color(204, 102, 0);

            // Check if audio clips can be made in the current environment
            audioCapable = RelFile.canMakeAudioClip();

            JPanel buttonPanel = new JPanel();
            // buttonPanel.setLayout(new FlowLayout()); [the default]
            buttonPanel.setOpaque(true);
            buttonPanel.setBackground(soundPanelColor);
            buttonPanel.setForeground(Color.black);
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            playAu = new KirrkirrButton(SC_PLAY, this);
            playAu.setEnabled(false);
            stopAu = new KirrkirrButton(SC_STOP, this);
            stopAu.setEnabled(false);
            buttonPanel.add(playAu);
            buttonPanel.add(stopAu);

            previous = new KirrkirrButton("<<", this);
            previous.setVerticalTextPosition(AbstractButton.CENTER);
            previous.setHorizontalTextPosition(AbstractButton.RIGHT);
            previous.setMnemonic('p');
            previous.setEnabled(false);

            currentLabel = new JLabel("10 of 10");

            next = new KirrkirrButton(">>", this);
            next.setVerticalTextPosition(AbstractButton.CENTER);
            next.setHorizontalTextPosition(AbstractButton.LEFT);
            next.setMnemonic('n');

            dir_p = new JPanel();
            // dir_p.setLayout(new FlowLayout()); [the default]
            dir_p.setOpaque(true);
            dir_p.setBackground(soundPanelColor);
            dir_p.setForeground(Color.black);
            dir_p.add(previous);
            dir_p.add(currentLabel);
            dir_p.add(next);
            dir_p.setVisible(false);

            JLabel iconLabel = null;
            if (size == KirrkirrPanel.TINY) {
                // No icon
            } else {
                ImageIcon spkrIcon;
                if (size == KirrkirrPanel.SMALL) {
                  spkrIcon = RelFile.makeImageIcon("speaker_small.gif", false);
                } else {
                  spkrIcon = RelFile.makeImageIcon("speaker.gif", false);
                }
              iconLabel = new JLabel(spkrIcon);
              iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            }

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createTitledBorder(Helper.getTranslation(SC_LISTEN)));

            add(Box.createVerticalGlue());
            add(buttonPanel);
            add(dir_p);
            if (iconLabel != null) {
              add(iconLabel);
            }
	    add(Box.createHorizontalStrut(2));
            setOpaque(true);
            setBackground(soundPanelColor);
            setForeground(Color.black);
        }


	/**
	 * Handler for play, stop, next, and previous buttons.
	 */
        public void actionPerformed(ActionEvent e)
        {
            if (currentClip == null)
                return;

	    Object src = e.getSource();
            if (src == playAu) {
                currentClip.play();
            } else if (src == stopAu) {
                currentClip.stop();
            } else if (src == next) {
                current++;
                if (next.isEnabled())
                    previous.setEnabled(true);
                if (current >= currentSounds.size() - 1) {
                    next.setEnabled(false);
                    current = currentSounds.size() - 1;
                }

                currentClip =
		    RelFile.makeAudioClip(currentSounds.get(current).uniqueKey,
                            true);
            } else if (src == previous){
                current--;
                if (previous.isEnabled())
                    next.setEnabled(true);
                if (current <= 0) {
                    previous.setEnabled(false);
                    current = 0;
                }

                currentClip =
		    RelFile.makeAudioClip(currentSounds.get(current).uniqueKey,
                            true);
            }
            currentLabel.setText((current+1)+" "+SC_OF+" "+currentSounds.size());
        }


	/**
	 * Given a headword, if the headword has sounds
	 * and the program is capable of playing audio, it
	 * enables the buttons and makes the audio clip.
	 * Otherwise, disables the buttons.
	 * @param uniqueKey The word to find sounds for
	 */
        public void setCurrentClip(String uniqueKey) {
            currentSounds = parent.cache.getSounds(uniqueKey);
            current = 0;
            previous.setEnabled(false);
            next.setEnabled(true);
            dir_p.setVisible(false);

            if (currentSounds == null || currentSounds.size() == 0
	    	|| !(audioCapable)){
                playAu.setEnabled(false);
                stopAu.setEnabled(false);
                next.setEnabled(false);
            }
            else {
                playAu.setEnabled(true);
                stopAu.setEnabled(true);
                currentClip =
		    RelFile.makeAudioClip(currentSounds.get(current).uniqueKey,
                            true);
                currentLabel.setText((current+1)+" "+SC_OF+" "+currentSounds.size());
                if(currentSounds.size() <= 1) {
                    next.setEnabled(false);
                }
                else {
                    dir_p.setVisible(true);
                }
            }
        }

        public void setNoCurrentClip() {
	    playAu.setEnabled(false);
	    stopAu.setEnabled(false);
	    next.setEnabled(false);
	}

    } // end class SoundPanel

} // end class MediaPanel
