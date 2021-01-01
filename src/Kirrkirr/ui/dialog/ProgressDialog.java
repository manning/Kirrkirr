package Kirrkirr.ui.dialog;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

import Kirrkirr.util.Dbg;
import Kirrkirr.util.Helper;
import Kirrkirr.util.RelFile;

/** ProgressDialog is a simple general class for putting up a splash screen
 *  picture, loading progress messages, and a JProgressBar.
 *  We use it while loading Kirrkirr.
 */
public class ProgressDialog extends JFrame {

    private static final String SC_LOADING = "Loading,_please_wait";
    private static final String SC_CONFIRM_EXIT = "Confirm_exit";
    private static final String SC_CONFIRM_EXIT_TEXT =
       "Do_you_want_to_exit_Kirrkirr?_(Cancel_will_only_remove_the_progress_bar.)";

    private static final String SPLASH_SCREEN_PICTURE = "splash.jpg";

    private final JLabel progressLabel;
    private final JProgressBar progressBar;
    private final int max;


    public ProgressDialog(String title, int max) {
        super(title);
        this.max = max;

        JPanel progressPanel = new JPanel();
        progressPanel.setLayout(new BoxLayout(progressPanel,
                                              BoxLayout.Y_AXIS));
        progressPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JLabel splashLabel = new JLabel(RelFile.makeImageIcon(
                                            SPLASH_SCREEN_PICTURE, false));
        splashLabel.setAlignmentX(CENTER_ALIGNMENT);

        progressLabel = new JLabel(Helper.getTranslation(SC_LOADING)+"...");
        progressLabel.setAlignmentX(CENTER_ALIGNMENT);

        progressBar = new JProgressBar(0, max);  // initialized to min value
        progressLabel.setLabelFor(progressBar);
        progressBar.setAlignmentX(CENTER_ALIGNMENT);

        progressPanel.add(splashLabel);
        progressPanel.add(Box.createVerticalStrut(12));
        progressPanel.add(progressLabel);
        progressPanel.add(Box.createVerticalStrut(12));
        progressPanel.add(progressBar);
        progressPanel.add(Box.createVerticalStrut(8));
        getContentPane().add(progressPanel);

        addWindowListener(new MyWindowListener(this));
        // Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        pack();

        // show the frame in the center
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = getPreferredSize();
        setBounds((screenSize.width - frameSize.width)/2,
                  (screenSize.height - frameSize.height)/2,
                  frameSize.width, frameSize.height);
        // Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        setVisible(true);
        /*
        final ProgressDialog copy = this;
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                copy.pack();
                copy.show();
            }
        });
        */
    }


    public void setMessage(final String message) {
      try {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              progressLabel.setText(message);
            }
          });
      } catch (Exception ie) {
        if (Dbg.ERROR) ie.printStackTrace();
      }
    }


    public int getValue() {
        return progressBar.getValue();
    }

    public void incrementValue(int n) {
        setValue(progressBar.getValue() + n);
    }

    public void incrementValue() {
        incrementValue(1);
    }

    public void incrementValue(String s) {
        setMessage(s);
        incrementValue(1);
    }

    public void setValue(final int v) {
      try {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              progressBar.setValue(v);
              if (v >= max) {
                dispose();
              }
            }
          });
      } catch (Exception ie) {
        if (Dbg.ERROR) ie.printStackTrace();
      }
    }


    private class MyWindowListener extends WindowAdapter {

        Container frame;

        MyWindowListener(final Container c) {
            frame = c;
        }

        public void windowClosing(WindowEvent e) {
            int option = JOptionPane.showConfirmDialog(frame,
                                 Helper.getTranslation(SC_CONFIRM_EXIT_TEXT),
                                 Helper.getTranslation(SC_CONFIRM_EXIT),
                                 JOptionPane.YES_NO_CANCEL_OPTION,
                                 JOptionPane.QUESTION_MESSAGE);
            if (option == JOptionPane.YES_OPTION) {
                System.exit(0);
            } else if (option == JOptionPane.CANCEL_OPTION) {
                e.getWindow().setVisible(false);
            }
        }
    }

}
