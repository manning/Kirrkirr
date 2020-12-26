package Kirrkirr;

import Kirrkirr.util.Helper;
import Kirrkirr.util.RelFile;

import javax.swing.*;

import java.applet.*;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.event.*;


public class KirrkirrApplet extends JApplet implements ActionListener
{
    private static final String SC_STARTING="Kirrkirr is starting";
    private static final String SC_INSTRUCTIONS="Click on the image to start Kirrkirr";

    private String xmlFile,  indexFile, engIndexFile, defaultProfile;
    private String langCode;
    private String dictionaryDirectory;

    private JButton go;
    private String autoStart = null;
    private boolean start=false;

    private static final String INTRO_CLIP = "gong.au";

    @Override
    public void init() {
        RelFile.Init(this.getCodeBase());     // set base for file access
        go = new JButton(RelFile.makeImageIcon("loading.jpg",false));
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(go, BorderLayout.CENTER);
        go.setBackground(Color.white);
        getContentPane().setBackground(Color.white);
        dictionaryDirectory = getParameter("dictionaryDirectory");
        xmlFile =  getParameter("dictionary");
        indexFile = getParameter("index");
	engIndexFile =  getParameter("glossIndex");
	autoStart = getParameter("autoStart");
	langCode = getParameter("langCode");
	defaultProfile = getParameter("defaultProfile");
	go.addActionListener(this);
	showStatus(Helper.getTranslation(SC_INSTRUCTIONS));
        setVisible(true);
    }

    @Override
    public void start()
    {
        xmlFile = RelFile.MakeURLString(xmlFile);
        indexFile = RelFile.MakeURLString(indexFile);
	engIndexFile = RelFile.MakeURLString(engIndexFile);

        Kirrkirr.demo = this;

        if(start) {
            actionPerformed(new ActionEvent(go, 42, "Go")); // pass dummy event
        } else {
            go.setIcon(RelFile.makeImageIcon("press.jpg",false));
            repaint();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == go) {
            AudioClip ply = getAudioClip(RelFile.makeURL(Kirrkirr.soundFolder, INTRO_CLIP));
            ply.play();

            // String args[] = {"-web", xmlFile, indexFile, engIndexFile, htmlFolder, autoStart, defaultProfile, langCode};
            String args[] = {"-web", dictionaryDirectory };
            // as never see it, don't bother with next line ...
            // go.setIcon(RelFile.makeImageIcon("starting.jpg"));
            showStatus(Helper.getTranslation(SC_STARTING)+"... ");
            Kirrkirr.mainInit(args);
        }
    }

}

