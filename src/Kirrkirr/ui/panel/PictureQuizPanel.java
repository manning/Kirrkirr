package Kirrkirr.ui.panel;

import Kirrkirr.Kirrkirr;
import Kirrkirr.dictionary.DictFields;
import Kirrkirr.dictionary.DictField;
import Kirrkirr.dictionary.DictEntry;
import Kirrkirr.util.Helper;
import Kirrkirr.util.Dbg;
import Kirrkirr.util.FontProvider;
import Kirrkirr.ui.data.GameWordGenerator;
import Kirrkirr.ui.PicturePanelCallback;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

/** The Picture Quiz game panel.
 * @author Kevin Lim
 * @author Kristen Parton
 * @since kirrkirr version 2.1.7
 */
public class PictureQuizPanel extends GamePlayPanel implements PicturePanelCallback
{
    private static final String SC_DEFAULT_PIC = "smile.jpg";
    private static final String SC_PREV = "Previous";
    private static final String SC_NEXT = "Next";
    private static final String SC_WORDGAME = "Word_Game";
    private static final String SC_YOURSCORE = "Your_score";
    private static final String SC_HITS = "Hits";
    private static final String SC_MISSES = "Misses";
    private static final String SC_HINTS = "Hints";
    private static final String SC_REMAINING = "Matches_remaining";
    private static final String SC_PICGAME_DESC =
        "The Picture Matching Game!\n" +
        "This game will display several pictures\n" +
        "and a group of words that match them.\n" +
        "Use your mouse to click through the pictures.\n" +
        "Double-click a word if you think that\n" +
        "it matches the current picture.";

    private static final String SC_DIRECTIONS_1 = "Double-click the word that matches";
    private static final String SC_DIRECTIONS_2 = "the picture above (once for hint).";

  private static final String SC_MISSLABEL = "Sorry, keep trying!";
  private static final String SC_HITLABEL = "Yes!! This is a picture of";
  private static final String SC_HITLABEL2 = "View another picture to continue playing.";
  private static final String SC_YOUWIN = "Congratulations! YOU WIN!";
  private static final String SC_EXCEPTION = "Please view a new picture before you guess again.";
  private static final String SC_HINTLABEL = "HINT";

  //IVARS
  private JFrame windowFrame;
  private Kirrkirr parentKirrkirr;
  private OldFunPanel wordHalfPanel;
  private SlideShowPanel picHalfPanel;
  private int hits, misses, remaining;
  private static int hintsTaken;
  private JLabel liveScore, remainingLabel, feedbackLabel, extraDirections;
  private static JLabel hintsLabel;
  private Hashtable filenameHash;
  private boolean ready;
  private static JButton getHint;

  private static String currWord;
 // private JComboBox levelMenu;


  //PRESET COLORS (STATIC)
    private static final Color lightBrown = new Color(255, 213, 170);
    private static final Color lighterBrown = new Color(224, 192, 192);


  //private static final int STARTING_LEVEL_INDEX = 4;
  /** Maximum number of times it will try to look for a word with a picture */
  private static final int MAX_TRIES = 150; // probably match if 1 in 100 pics


  /** Constructor based on the ctor of QuizMasterPanel.
   */
  public PictureQuizPanel(Kirrkirr click, int size) {
    //super(click.window);
    filenameHash = new Hashtable();
    windowFrame = click.window;
    parentKirrkirr = click;
    setName(Helper.getTranslation(SC_WORDGAME));
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    //boolean isSmall = (size == KirrkirrPanel.SMALL);
    setBackground(lighterBrown);
    //setAlignmentX(Component.CENTER_ALIGNMENT);
    //setAlignmentY(Component.CENTER_ALIGNMENT);

    //game panel, will contain picslideshow, funpanel
    JPanel gamePanel = new JPanel();
    gamePanel.setLayout(new BoxLayout(gamePanel, BoxLayout.X_AXIS));
    gamePanel.setBackground(lightBrown);
    //call gamePanel.setLayout()

    // picslideshow... the side that rotates through the pictures
    picHalfPanel = new SlideShowPanel(this, true, false, false, SC_PREV,
    SC_NEXT, null, SC_DEFAULT_PIC);

    picHalfPanel.setPreferredSize(new Dimension(300,100));
    gamePanel.add(Box.createHorizontalGlue());
    gamePanel.add(picHalfPanel);
    gamePanel.add(Box.createHorizontalGlue());

    // funpanel... where the words float around
    QuizGraphPanel graph = new QuizGraphPanel(windowFrame, parentKirrkirr,
                                              this);
    wordHalfPanel = graph.funPanel;
    wordHalfPanel.legend = false;
    graph.funScrollPane.setPreferredSize(new Dimension(350, 100));
    gamePanel.add(graph.funScrollPane);
    gamePanel.add(Box.createHorizontalGlue());
    add(gamePanel);
    add(Box.createGlue());
    //end gamepanel////////////////////////////////////////////////////

    //control panel: contains levelpanel, scorePanel, restart button
    JPanel controlPanel = new JPanel();
    controlPanel.setLayout(new BoxLayout(controlPanel,BoxLayout.X_AXIS));
    controlPanel.setBackground(lighterBrown);
    controlPanel.setPreferredSize(new Dimension(400, 45));
    //controlPanel.setMinimumSize(new Dimension(200, 35));
    /* ////// levelPanel: levelLabel and levelMenu////////////
      JPanel levelPanel = new JPanel();
      levelPanel.setLayout(new BoxLayout(levelPanel,BoxLayout.Y_AXIS));
      //levelPanel.setMaximumSize(new Dimension(250,200));
      //levelPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
      levelPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
      levelPanel.setBackground(lightBrown);

      //the level selector label//////////////////////////////////////
      JLabel levelLabel = new JLabel(" "+Helper.getTranslation("Level")+": ");
      levelLabel.setBackground(lightBrown);
      levelLabel.setForeground(Color.black);
      levelLabel.setFont(FontProvider.PROMINENT_INTERFACE_FONT);
      // levelLabel.setMaximumSize(new Dimension(90,90));
      ////////////////////////////////////////////////////////////////
      levelPanel.add(levelLabel);

      //pulldown level menu///////////////////////////////////////////
      //we'll have levels 1 through 9, indicating # of word/pic pairs (2-10)
      levelMenu = new JComboBox(level);
      levelMenu.setMaximumSize(new Dimension(100,20));
      levelMenu.setSelectedIndex(STARTING_LEVEL_INDEX);
      /*         levelMenu.addActionListener(
          new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                    JComboBox box = (JComboBox) e.getSource();
                    //do something with the value of box
                    }});*/ /*
              levelMenu.setFont(FontProvider.PROMINENT_INTERFACE_FONT);
              ////////////////////////////////////////////////////////////////
              levelPanel.add(levelMenu);

              JLabel note = new JLabel(Helper.getTranslation("Level changes on restart"));
              note.setBackground(lightBrown);
              note.setForeground(Color.black);
              note.setFont(FontProvider.PROMINENT_INTERFACE_FONT);
              ///////////////////////////////////////////////////////////
              levelPanel.add(note);

      ////////////////////////////////////////////////////////////////////////
      controlPanel.add(levelPanel);*/


      getHint = new JButton(SC_HINTLABEL);
      getHint.addActionListener(new ActionListener() {
      	@Override
        public void actionPerformed(ActionEvent e) {
      		Kirrkirr.setHintWord(PictureQuizPanel.getCurrWord());
      		PictureQuizPanel.addHint();
      		PictureQuizPanel.disableHintButton();
      	}
      });
      controlPanel.add(getHint);
      getHint.setEnabled(false);
      //controlPanel.add(Box.createGlue());
      controlPanel.add(Box.createHorizontalStrut(15));
      JPanel directionsBox = new JPanel();
      directionsBox.setLayout(new BoxLayout(directionsBox, BoxLayout.Y_AXIS));
      directionsBox.add(feedbackLabel = new JLabel(Helper.getTranslation(SC_DIRECTIONS_1)));
      directionsBox.add(extraDirections = new JLabel(Helper.getTranslation(SC_DIRECTIONS_2)));
      //controlPanel.add(feedbackLabel = new JLabel(Helper.getTranslation(SC_DIRECTIONS)));
      controlPanel.add(directionsBox);
      feedbackLabel.setForeground(Color.black);
      controlPanel.add(Box.createHorizontalStrut(15));
      //levelPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
      controlPanel.add(Box.createGlue());
      ///scorePanel: panel with the number of matches right, and matches LEFT
      JPanel scorePanel = new JPanel();
      scorePanel.setLayout(new BoxLayout(scorePanel, BoxLayout.X_AXIS));
       // JLabel scoreTitle = new JLabel(Helper.getTranslation("Your Score  "));
       // scorePanel.add(scoreTitle);
        //scorePanel.add(Box.createGlue());
        liveScore = new JLabel();
        hintsLabel = new JLabel();
        remainingLabel = new JLabel();
        RefreshScoreBoard();
        scorePanel.add(liveScore);
        scorePanel.add(Box.createHorizontalStrut(10));
        scorePanel.add(hintsLabel);
        scorePanel.add(Box.createHorizontalStrut(10));
        scorePanel.add(remainingLabel);
        scorePanel.setBorder(new TitledBorder(Helper.getTranslation(SC_YOURSCORE)));
      /////////////////////////////////////////////////////////////////////////
      controlPanel.add(scorePanel);
      //controlPanel.add(Box.createGlue());
      ///restart button: clicking this restarts the game//////////////////////
    /*  KirrkirrButton restartButton = new KirrkirrButton(Helper.getTranslation("Restart Game"),
                        new ActionListener() {
                          public void actionPerformed(ActionEvent e)
                           {  restartGame();  }});
      //restartButton.setPreferredSize(new Dimension(90,21));
      restartButton.setFont(FontProvider.PROMINENT_INTERFACE_FONT);*/
      //////////////////////////////////////////////////////////////////////
      //controlPanel.add(restartButton);
      //restartButton.setAlignmentX(Component.CENTER_ALIGNMENT);

    add(controlPanel);

    click.addHintPanel();
    click.hintsOn = true;
  }

  public static void disableHintButton() {
  	getHint.setEnabled(false);
  }

    @Override
    public boolean horizontalLayout(){
        return true;
    }

    public static void addHint() {
    	hintsTaken++;
    	hintsLabel.setText(Helper.getTranslation(SC_HINTS) + ":" + hintsTaken);
    }

static class QuizGraphPanel extends OldGraphPanel {

    private PictureQuizPanel pqp;

    public QuizGraphPanel(JFrame window, Kirrkirr click,
                          PictureQuizPanel pqp)
    {
        super(click, window, false, KirrkirrPanel.TINY);
        this.pqp = pqp;
    }

    //double click
    @Override
    public void funGetLinks(String uniqueKey) {
        super.funGetLinks(uniqueKey);
        pqp.wordClicked(uniqueKey); //look at what qmp does
    }

    //single click
    @Override
    public void funFindWord(String uniqueKey) {
        super.funFindWord(uniqueKey);
        currWord = uniqueKey;
        getHint.setEnabled(true);
        //pqp.wordClicked(clicked);
    }

  } // end class QuizGraphPanel

	public static String getCurrWord() { return currWord; }

  public void wordClicked(String uniqueKey) {
    if (Dbg.KEVIN) Dbg.print(Helper.uniqueKeyToPrintableString(uniqueKey) + " was double clicked");
    if (! ready) {
    	getHint.setEnabled(true);
      showExceptionLabel();
      return;
    }
    DictFields pics = (DictFields) filenameHash.get(uniqueKey);
    String slideFilename = picHalfPanel.getCurrFilename();
    if (includesFilename(pics, slideFilename)) {
      hits++;
      remaining--;
      if (Dbg.KEVIN) Dbg.print("Word matches picture!");
      showHitLabel(uniqueKey);
      wordHalfPanel.removeNodeForGame(uniqueKey);
      picHalfPanel.deleteCurrentPic();
      if(picHalfPanel.getShowSize() == 0)
          showWinLabel();
      ready = false;
      getHint.setEnabled(false);
    } else {
    	getHint.setEnabled(true);
      misses++;
      showMissLabel();
    }
    RefreshScoreBoard();

    //wordHalfPanel.start();
  }

  private static boolean includesFilename(DictFields df, String picname)
  {
      // cw 2002: was throwing null pointer exceptions
//     for(int i = 0; i < df.size(); i++)
//       if(df.get(i).value.equalsIgnoreCase(picname)) return true;
      for (int i = 0; i < df.size(); i++) {
          DictField field = df.get(i);
          if (field != null && field.uniqueKey != null) {
              if (Dbg.KEVIN) Dbg.print("Comparing " + field.uniqueKey + " to " + picname);
              if (field.uniqueKey.equalsIgnoreCase(picname)) return true;
          }
      }

    return false;
  }

  private void RefreshScoreBoard()
  {
      liveScore.setText(Helper.getTranslation(SC_HITS) + ":" + hits + " "
                + Helper.getTranslation(SC_MISSES)+":"+misses);
      hintsLabel.setText(Helper.getTranslation(SC_HINTS) + ":" + hintsTaken);
      remainingLabel.setText(Helper.getTranslation(SC_REMAINING)+": "+remaining);
  }


  public void tabSelected() {
      startGame();
  }

    @Override
    public void startGame() {
        wordHalfPanel.clearFunPanel();
        picHalfPanel.clearPicNames();
        filenameHash = new Hashtable();
        hits = misses = 0;
        remaining = getDifficulty() * 3 + 3;
        RefreshScoreBoard();
        for (int i = 0; i < remaining; i++) {
            addRandomPicWord();
        }
        wordHalfPanel.start();
        picHalfPanel.refreshImage();
        if (Dbg.KEVIN) Dbg.print("Started game hits=" + hits + " misses=" +
                misses + " remaining=" + remaining + " filenameHash=" + filenameHash);
        showDefaultLabel();
    }

   @Override
   public void restartGame()
   {
    //if(Dbg.KEVIN) Dbg.print("restart!");
   	getHint.setEnabled(false);
    startGame();
   }


  private void addRandomPicWord() {
    int counter = 0;
    DictEntry de;
    String randomWord;
    do {
        randomWord = GameWordGenerator.GetGameHeadword(getDifficulty(),
                                                       parentKirrkirr);
        //too slow if it's real difficulty: use GameWordGenerator.HARD
        de = parentKirrkirr.cache.getIndexEntry(randomWord);
        counter++;
    } while ( ! de.hasPics && counter < MAX_TRIES);
    // DictFields dfs=parentKirrkirr.cache.getGlossEntry(randomWord);
    //System.out.println("dfs "+dfs);
    //if(randomWord == null && Dbg.ERROR) Dbg.print("Failure in PQP.AddRandomWord");
    if (counter < MAX_TRIES) {
        wordHalfPanel.addNode(randomWord);
        DictFields picsDF = parentKirrkirr.cache.getPictures(randomWord);
        int numOfPics = picsDF.size();
        int randomInt = GameWordGenerator.randomInt(numOfPics);
        String filename = picsDF.get(randomInt).uniqueKey;
        picHalfPanel.addNewPic(filename);
        filenameHash.put(randomWord, picsDF);
    }
  }


    public static String getDesc()
    {
      return (Helper.getTranslation(SC_PICGAME_DESC));//"This is the picture matching game."+'\n'+"");
    }

    public void showMissLabel()
    {
      feedbackLabel.setFont(FontProvider.PROMINENT_LARGE_INTERFACE_FONT);
      feedbackLabel.setText(Helper.getTranslation(SC_MISSLABEL));
      extraDirections.setVisible(false);

    }

    public void showHitLabel(String word)
    {
      feedbackLabel.setFont(FontProvider.PROMINENT_INTERFACE_FONT);
      feedbackLabel.setText(Helper.getTranslation(SC_HITLABEL) + " " + word + ".");
      extraDirections.setText(Helper.getTranslation(SC_HITLABEL2));
      extraDirections.setVisible(false);

    }

    public void showDefaultLabel()
    {
      feedbackLabel.setFont(FontProvider.PROMINENT_INTERFACE_FONT);
      feedbackLabel.setText(Helper.getTranslation(SC_DIRECTIONS_1));
      extraDirections.setText(Helper.getTranslation(SC_DIRECTIONS_2));
      extraDirections.setVisible(false);
      ready = true;
    }

    public void showWinLabel()
    {
      feedbackLabel.setFont(FontProvider.PROMINENT_LARGE_INTERFACE_FONT);
      feedbackLabel.setText(Helper.getTranslation(SC_YOUWIN));
      extraDirections.setVisible(false);

    }

    public void showExceptionLabel()
    {
      feedbackLabel.setFont(FontProvider.PROMINENT_INTERFACE_FONT);
      feedbackLabel.setText(Helper.getTranslation(SC_EXCEPTION));
      extraDirections.setVisible(false);

    }


    @Override
    public void imageUpdated() {
        showDefaultLabel();
    }

} // end class PictureQuizPanel

