package Kirrkirr.ui.dialog;

import Kirrkirr.Kirrkirr;
import Kirrkirr.ui.KirrkirrButton;
import Kirrkirr.util.Helper;
import Kirrkirr.util.RelFile;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

public class PictureDialog extends JFrame implements ActionListener
{
    private static final String SC_CLOSE="Close";

    private static int MAX_WIDTH=640, MAX_HEIGHT=480;

    private static Kirrkirr parent;
    private ImageIcon picture;
    private JLabel pictLabel;
    private JViewport pictVp;
    private JButton close;

    // private final static Dimension minimumSize = new Dimension(380, 500);

    // public Dimension getMinimumSize() {
    //    return minimumSize;
    // }

    // public Dimension getPreferredSize() {
    //     return minimumSize;
    // }

    public PictureDialog(Kirrkirr p, String filename, String word) {
        super();
        parent = p;
	setTitle(word);

        // show the frame
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	setLocation(screenSize.width/2 - 300 + (int) (Math.random()*40),
                    screenSize.height/2 - 200 + (int) (Math.random()*40));

        WindowListener winlin = new WindowAdapter() {
            public void windowClosing(WindowEvent e) { dispose(); }
        };
        addWindowListener(winlin);

	setSize(380, 300);

        JPanel butt_p = new JPanel();
        close = new KirrkirrButton(Helper.getTranslation(SC_CLOSE), this);
        butt_p.add(close);

	pictLabel = new JLabel();
        pictLabel.setHorizontalAlignment(JLabel.CENTER);
        pictLabel.setVerticalAlignment(JLabel.CENTER);
        pictLabel.setOpaque(true);
        pictLabel.setBackground(Color.white);

        JScrollPane pictPane = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        pictPane.setMinimumSize(new Dimension(300, 100));
        // pictPane.setPreferredSize(new Dimension(300, 300));
        pictPane.setOpaque(true);
        pictPane.setBackground(Color.white);
        //pictPane.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
	pictVp = pictPane.getViewport();
	pictVp.setView(pictLabel);

	Dimension d=setNewImage(filename);
	if (d!=null) {
            setSize(d);
        }
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(pictPane, BorderLayout.CENTER);
        getContentPane().add(butt_p, BorderLayout.SOUTH);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == close) {
            dispose();
        }
    }

    private Dimension setNewImage(String filename){
	if(filename == null) {
	    return null;
        }

	picture = RelFile.makeImageIcon(filename,true);
        int height = picture.getIconHeight();
        int width = picture.getIconWidth();

        //System.out.println(height+" "+width+" "+pictVp.getWidth()+" "+pictVp.getHeight()+" "+pictVp.getViewPosition());
        int x = (width-pictVp.getWidth())/2;
        int y = (height-pictVp.getHeight())/2;
	int tempx=width+40;
	int tempy=height+90;
	//System.err.println("x "+tempx+" y "+tempy);
	if (tempx>MAX_WIDTH) tempx=MAX_WIDTH;
	if (tempy>MAX_HEIGHT) tempy=MAX_HEIGHT;
	Dimension d=new Dimension(tempx,tempy);
	if (x < 0)
	    x = 0;
	if (y < 0)
	    y = 0;
	// doing it this way avoids nasty movements of a picture.  It'd be
	// even cleaner if one could just disable display updating between
	// the last two operations.
	pictLabel.setIcon(null);
        pictVp.setViewPosition(new Point(x, y));               //coords of upper-left corner
        pictLabel.setIcon(picture);
	return d;
    }

}

