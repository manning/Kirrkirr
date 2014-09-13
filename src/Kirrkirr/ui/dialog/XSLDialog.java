package Kirrkirr.ui.dialog;

import Kirrkirr.Kirrkirr;
import Kirrkirr.ui.KirrkirrButton;
import Kirrkirr.util.Helper;
import Kirrkirr.util.RelFile;
import Kirrkirr.util.KirrkirrFileFilter;
import Kirrkirr.util.XslDriver;


import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/** The XSL dialog allows the user to convert an xml file based on
 *  an xsl/xslt file.
 */
public class XSLDialog extends JDialog implements ActionListener {

    private final static String SC_XSL_TRANSFORM = "XSL_transformer";
    private final static String SC_CANCEL = "Cancel";
    private final static String SC_TRANSFORM = "Transform";

    private AuxFilePanel xmlFilePanel;
    private AuxFilePanel xslFilePanel;
    private AuxFilePanel outFilePanel;

    private KirrkirrButton run;
    private KirrkirrButton cancel;

    private static final Dimension minimumSize = new Dimension(500, 200);

    public XSLDialog(Kirrkirr parent) {
        super(parent.window, Helper.getTranslation(SC_XSL_TRANSFORM), false);

        // setLocationRelativeTo(parent);  // This isn't in Swing-1.1 !
        int xPos = parent.getWidth()/2-225;
        if (xPos < 0) xPos = 0;
        int yPos = parent.getHeight()/2-100;
        if (yPos < 0) yPos = 0;
        setLocation(xPos, yPos);
        setSize(minimumSize);

        xmlFilePanel = new AuxFilePanel("XML file to convert:",
                                        KirrkirrFileFilter.XML_ENTRY);
        xslFilePanel = new AuxFilePanel("XSL file to use:",
                                        KirrkirrFileFilter.XSL_ENTRY);

        //this file filter should effectively accept all types of files -
        //could be xml, html, anything
        outFilePanel = new AuxFilePanel("Output file: ", KirrkirrFileFilter.XML_ENTRY);

        Container content = getContentPane();
        content.setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        content.add(xmlFilePanel);
        content.add(xslFilePanel);
        content.add(outFilePanel);
        content.add(buttonPanel());

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e){
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
            String xmlFile, xslFile, outFile;

            xmlFile = xmlFilePanel.getText();
            xslFile = RelFile.makeAbsoluteURLString(xslFilePanel.getText());
            outFile = outFilePanel.getText();
            TransformThread transformThread = new TransformThread(xmlFile,
                                                                  xslFile,
                                                                  outFile);
            transformThread.start();
            dispose();
        } else if(src == cancel) {
            dispose();
        }
    }


    static class TransformThread extends Thread {

        private String xmlFile, xslFile, outFile;

        public TransformThread(String xml, String xsl, String out) {
            xmlFile = xml;
            xslFile = xsl;
            outFile = out;
            setPriority(MIN_PRIORITY);
        }

        public void run() {
            XslDriver.makeHtml(xmlFile, xslFile, outFile);
        }

    }

}

