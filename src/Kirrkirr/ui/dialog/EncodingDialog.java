package Kirrkirr.ui.dialog;

import Kirrkirr.ui.KirrkirrButton;
import Kirrkirr.Kirrkirr;
import Kirrkirr.util.*;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.io.*;

/** A utility for changing the character set encoding of a file.
 *
 *  @author Christopher Manning
 */
public class EncodingDialog extends JDialog implements ActionListener {

    private final static String SC_CHAR_TRANSFORM = "Character_encoding_transformer";
    private final static String SC_CANCEL = "Cancel";
    private final static String SC_TRANSFORM = "Re-encode";

    private JTextField inEncodingField;
    private JTextField outEncodingField;
    private AuxFilePanel inFilePanel;
    private AuxFilePanel outFilePanel;

    private KirrkirrButton run;
    private KirrkirrButton cancel;

    private static final Dimension minimumSize = new Dimension(500, 200);

    public EncodingDialog(Kirrkirr parent) {
        super(parent.window, Helper.getTranslation(SC_CHAR_TRANSFORM), false);

        // setLocationRelativeTo(parent);  // This isn't in Swing-1.1 !
        int xPos = parent.getWidth()/2-225;
        if (xPos < 0) xPos = 0;
        int yPos = parent.getHeight()/2-100;
        if (yPos < 0) yPos = 0;
        setLocation(xPos, yPos);
        setSize(minimumSize);

        JPanel inEncodingPanel = new JPanel();
        inEncodingPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel inEncodingLabel = new JLabel("Input encoding:");
        inEncodingField = new JTextField(12);
        JLabel inEncodingHint = new JLabel("(US-ASCII, ISO-8859-1, UTF-8, ...)");
        inEncodingPanel.add(inEncodingLabel);
        inEncodingPanel.add(inEncodingField);
        inEncodingPanel.add(inEncodingHint);

        JPanel outEncodingPanel = new JPanel();
        outEncodingPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel outEncodingLabel = new JLabel("Output encoding:");
        outEncodingField = new JTextField(12);
        JLabel outEncodingHint = new JLabel("(US-ASCII, ISO-8859-1, UTF-8, ...)");
        outEncodingPanel.add(outEncodingLabel);
        outEncodingPanel.add(outEncodingField);
        outEncodingPanel.add(outEncodingHint);

        inFilePanel = new AuxFilePanel("Input file:",
                                        KirrkirrFileFilter.ANY_ENTRY);
        outFilePanel = new AuxFilePanel("Output file: ", KirrkirrFileFilter.ANY_ENTRY);

        Container content = getContentPane();
        content.setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        content.add(Box.createVerticalStrut(6));
        content.add(inFilePanel);
        content.add(inEncodingPanel);
        content.add(Box.createVerticalStrut(6));
        content.add(outFilePanel);
        content.add(outEncodingPanel);
        content.add(Box.createVerticalStrut(6));
        content.add(buttonPanel());

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        setVisible(true);
    }

    private JPanel buttonPanel() {
        //button to run a particular tool
        run = new KirrkirrButton(SC_TRANSFORM, null, this);

        //button to close the dialog
        cancel = new KirrkirrButton(SC_CANCEL, null, this);

        //layout and add buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(run);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(cancel);
        buttonPanel.add(Box.createHorizontalGlue());
        return buttonPanel;
    }

    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == run) {
            String inFile = inFilePanel.getText();
            String outFile = outFilePanel.getText();
            String inEncoding = inEncodingField.getText();
            String outEncoding = outEncodingField.getText();
            ConvertEncodingThread encodeThread =
                    new ConvertEncodingThread(inFile, inEncoding,
                            outFile, outEncoding);
            encodeThread.start();
            dispose();
        } else if (src == cancel) {
            dispose();
        }
    }


    /**
     * Converts the encoding of an <code>InputStream</code> and writes the
     * converted encoding to an <code>OutputStream</code>.
     *
     * @author Roger Levy
     * @version January 2003
     */
    static class ConvertEncodingThread extends Thread {

      private BufferedReader r;
      private BufferedWriter w;
      private String from;
      private String to;


        /**
         * Constructor creates the thread.
         *
         * @param in   The filename to be converted.
         * @param out  The filename to write to.
         * @param from The encoding of the InputStream.
         * @param to   The desired encoding for the OutputStream.
         */
      public ConvertEncodingThread(String in, String from, String out, String to) {
        try {
          InputStream inStream = new FileInputStream(in);
          OutputStream outStream = new FileOutputStream(out);
          this.from = from;
          this.to = to;
          r = new BufferedReader(new InputStreamReader(inStream, from));
          w = new BufferedWriter(new OutputStreamWriter(outStream, to));
          setPriority(MIN_PRIORITY);
        } catch (IOException ioe) {
            r = null;
            w = null;
        }
      }

      public void run() {
        if (r != null && w != null) {
          try {
            String line;
            Regex rx = new OroRegex("(\\<\\?xml +version *= *\"[0-9.]+\" +encoding *= *\")" +
                    from + "(\"\\?>)", "\1" + to + "\2");
            // Dbg.print("Regexi is " + rx);
            // Dbg.print("String is " + "(\\<\\?xml +version *= *\"[0-9.]+\" +encoding *= *\")" +
            //        from + "(\"\\?\\>)");
            while ((line = r.readLine()) != null) {
              // put in changing the XML encoding line special case
              // this bit doesn't seem to work!  Why not?
              rx.doReplace(line);
              w.write(line);
              w.newLine();
            }
            r.close();
            w.flush();
            w.close();
          } catch (IOException e) {
            System.err.println("ConvertEncodingThread run: " + e);
          }
        }
      }

      /**
       * The main() method converts a file. Its arguments are (Infile,
       * Outfile, EncodingIn, EncodingOut).
       */
      public static void main(String[] args) {
          String from = args[2];
          String to = args[3];
          // Use default encoding if no encoding is specified.
          if (from == null) {
            from = System.getProperty("file.encoding");
          }
          if (to == null) {
            to = System.getProperty("file.encoding");
          }

          new ConvertEncodingThread(args[0], args[1], from, to).start();
      }

    } // end class ConvertEncodingThread

} // end class EncodingDialog
