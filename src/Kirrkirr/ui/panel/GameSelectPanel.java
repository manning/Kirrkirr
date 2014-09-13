package Kirrkirr.ui.panel;

import Kirrkirr.ui.KirrkirrButton;
import Kirrkirr.Kirrkirr;
import Kirrkirr.util.Helper;
import Kirrkirr.util.Dbg;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

public class GameSelectPanel extends KirrkirrPanel implements ActionListener {

  private static final String SC_WORD_GAMES = "Word_Games";
  private static final String SC_WORD_GAMES_SHORT = "Games";
  private static final String SC_GAME_DESC  = "Game_Description";
  private static final String SC_PICK_GAME = "Pick_a_Game:";
  private static final String SC_CROSSGAME = "Crossword_Game";
  private static final String SC_PICGAME = "Picture_Game";
  private static final String SC_WORDGAME = "Word_Game";
  private static final String SC_PLAY_GAME = "Play_This_Game";
  private static final String SC_SETLEVEL = "Set_Difficulty_Level";
  private static final String SC_EASY = "Easy";
  private static final String SC_MED = "Medium";
  private static final String SC_HARD = "Hard";
  private static final String SC_BACK2MENU = "Back_To_Menu";
  private static final String SC_RESTART = "Restart_Game";
  private static final String SC_PLAYGAMES = "Play_games";

  //IVARS
  private GamePlayPanel gp;
  private GamePlayPanel picPanel, wordPanel, crossPanel;
  private JPanel radioPanel;
  private JPanel descPanel;
  private JPanel playPanel;
  private JTextArea descTextArea;
  private JRadioButton crosswordRB;
  private JRadioButton picMatchRB;
  private JRadioButton wordMatchRB;
  private JComboBox levelBox;

  private KirrkirrButton playButton;
  private KirrkirrButton backButton;
  private KirrkirrButton restartButton;

  private int size;

    //PRESET COLORS (STATIC)
    private static final Color lighterBrown = new Color(224, 192, 192);


    //enum
    private static final int CROSSWORD_DESC = 0;
    private static final int PICMATCH_DESC = 1;
    private static final int WORDMATCH_DESC = 2;

    /** Create a new GameSelectPanel of a certain size */
    public GameSelectPanel(Kirrkirr kparent, int size)
    {
        super(kparent);
        this.size = size;
        boolean isSmall = (size <= KirrkirrPanel.SMALL);
        if (isSmall) {
            setName(Helper.getTranslation(SC_WORD_GAMES_SHORT));
        } else {
            setName(Helper.getTranslation(SC_WORD_GAMES));
        }
        // setLayout(new BorderLayout());
        // setBackground(lighterBrown);
        GenerateStartScreen();
        ShowStartScreen();
    }


  /** This method creates the three panels needed on the chooser screen. */
  private void GenerateStartScreen()
  {
     //make descPanel, playPanel, radioPanel;

    //descPanel: all "live", based on what radiobutton is selected, a certain description is seen
    descPanel = new JPanel();
    // Bad UI idea!
    // Border descBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
    //                                                      Helper.getTranslation(SC_GAME_DESC));
    // descPanel.setBorder(descBorder);
    descPanel.setLayout(new BoxLayout(descPanel, BoxLayout.Y_AXIS));
    JLabel descLabel = new JLabel(Helper.getTranslation(SC_GAME_DESC));
    descLabel.setHorizontalTextPosition(SwingConstants.LEFT);
    descPanel.add(descLabel);

    descTextArea = new JTextArea(CrosswordPanel.getDesc());
    descTextArea.setEditable(false);
    //ImageIcon i = RelFile.makeImageIcon("game-intro.jpg");
    JScrollPane descSP = new JScrollPane(descTextArea);
    descPanel.add(descSP);

    //radioPanel: no live labels, just make buttonGroup, shortdescs for GamePlayPanels
    radioPanel = new JPanel();
    radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.Y_AXIS));

    JLabel dirLabel = new JLabel(Helper.getTranslation(SC_PICK_GAME));
    radioPanel.add(dirLabel);

    ButtonGroup group = new ButtonGroup();

    crosswordRB = new JRadioButton(Helper.getTranslation(SC_CROSSGAME));
    crosswordRB.setSelected(true);
    crosswordRB.addActionListener(this);

    picMatchRB = new JRadioButton(Helper.getTranslation(SC_PICGAME));
    picMatchRB.addActionListener(this);
    // disable if there are no pictures in the dictionary!
    if (parent.dictInfo == null || parent.dictInfo.getImagesXPath() == null) {
        picMatchRB.setEnabled(false);
    }

    wordMatchRB = new JRadioButton(Helper.getTranslation(SC_WORDGAME));
    wordMatchRB.addActionListener(this);

    radioPanel.add(crosswordRB);
    radioPanel.add(picMatchRB);
    radioPanel.add(wordMatchRB);

    group.add(crosswordRB);
    group.add(picMatchRB);
    group.add(wordMatchRB);

    //playPanel
    playPanel = new JPanel();
    playPanel.setLayout(new BoxLayout(playPanel,BoxLayout.X_AXIS));
    playButton = new KirrkirrButton(Helper.getTranslation(SC_PLAY_GAME), this);
    playPanel.add(playButton);
    playPanel.add(Box.createGlue());

    JLabel levelLabel = new JLabel(Helper.getTranslation(SC_SETLEVEL) + ": ");
    playPanel.add(levelLabel);
    String[] levels = new String[3];//{"Easy","Medium","Hard"};
    levels[0] = Helper.getTranslation(SC_EASY);
    levels[1] = Helper.getTranslation(SC_MED);
    levels[2] = Helper.getTranslation(SC_HARD);
    levelBox = new JComboBox(levels);
    playPanel.add(levelBox);
    playPanel.add(Box.createGlue());
  }

  private void showDescription(int game) {
    if (game == WORDMATCH_DESC) {
      descTextArea.setText(WordGamePanel.getDesc());
      //if(Dbg.KEVIN) Dbg.print("label is: "+WordGamePanel.getDesc());
      //descTextArea.repaint();
    } else if (game == PICMATCH_DESC) {
      descTextArea.setText(PictureQuizPanel.getDesc());
      //descTextArea.repaint();
    } else {
      //default
      descTextArea.setText(CrosswordPanel.getDesc());
      //descTextArea.repaint();
    }
  }

  private void RestartSelectedGame()
  {
    gp.restartGame();
  }

  private void PlaySelectedGame()
  {
    removeAll();
    //add the game panel
    gp = GenerateCurrentGamePanel();
    /*JScrollPane scroller = new JScrollPane(gp, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
        JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    */
    gp.setDifficulty(levelBox.getSelectedIndex());

    boolean horizontal=gp.horizontalLayout();
    setLayout(new BoxLayout(this, horizontal?BoxLayout.Y_AXIS:BoxLayout.X_AXIS));
    JScrollPane sp=new JScrollPane(gp);

    if (horizontal)
        add(sp);
    JPanel ctrlPanel = CreatePlayControlPanel(horizontal);
    add(ctrlPanel);
    if (!horizontal)
        add(sp);
//  backButton=new KirrkirrButton(Helper.getTranslation("Back To Menu"), this);
//  backButton.setAlignmentX(Component.LEFT_ALIGNMENT);
//  add(backButton);
    // repaint();
    gp.startGame();
  }

  private JPanel CreatePlayControlPanel(boolean horizontal)
  {
    JPanel cp = new JPanel();
    cp.setLayout(new BoxLayout(cp, horizontal?BoxLayout.X_AXIS:BoxLayout.Y_AXIS));

    backButton = new KirrkirrButton(Helper.getTranslation(SC_BACK2MENU), this);
    if (horizontal)
        cp.add(Box.createHorizontalGlue());
    else
        cp.add(Box.createVerticalGlue());
    if (horizontal)
        backButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
    cp.add(backButton);
    if (horizontal)
        cp.add(Box.createHorizontalGlue());
    else
        cp.add(Box.createVerticalGlue());

    restartButton=new KirrkirrButton(Helper.getTranslation(SC_RESTART), this);
    cp.add(restartButton);
    if (horizontal)
        cp.add(Box.createHorizontalGlue());
    else
        cp.add(Box.createVerticalGlue());
    cp.setBackground(lighterBrown);
    return cp;
  }

  private GamePlayPanel GenerateCurrentGamePanel()
  {
    if (picMatchRB.isSelected()) {
      if (picPanel == null) picPanel = new PictureQuizPanel(parent, size);
      return picPanel;
    }
    if (wordMatchRB.isSelected()) {
      if (wordPanel == null) wordPanel = new WordGamePanel(parent, size);
      return wordPanel;
    }
    if(crossPanel == null) crossPanel = new CrosswordPanel(parent);
    return crossPanel;
  }

  private void ShowStartScreen()
  {
    this.removeAll();
    //add the start screen objects instantiated in GenerateStartScreen()
    setLayout(new BorderLayout());
    if (size > KirrkirrPanel.TINY) {
      add(playPanel, BorderLayout.SOUTH);
    } else {
      descPanel.add(playPanel);
    }
    add(descPanel, BorderLayout.CENTER);
    add(radioPanel, BorderLayout.WEST);
    repaint();
  }

  //////////////////////////Next three methods needed to be KirrkirrPanel

  public String getTabRollover() {
        return Helper.getTranslation(SC_PLAYGAMES);
  }

  public void setCurrentWord(String g, boolean gloss, JComponent x,
                             int a, int b) {
  } //nothing

  public void tabSelected()
   {
       // ShowStartScreen();
   }


    /** The ActionListener that handles all button presses
     *  @param ae The event to process
     */
    public void actionPerformed(ActionEvent ae) {
        Object src = ae.getSource();
        if (src == crosswordRB) {
            showDescription(CROSSWORD_DESC);
        } else if (src == picMatchRB) {
            showDescription(PICMATCH_DESC);
        } else if (src == wordMatchRB) {
            showDescription(WORDMATCH_DESC);
        } else if (src == playButton) {
            PlaySelectedGame();
        } else if (src == backButton) {
        	parent.removeHintPanel();
        	parent.hintsOn = false;
            ShowStartScreen();
        } else if (src == restartButton) {
            RestartSelectedGame();
        } else {
            if (Dbg.ERROR) Dbg.print("ActionEvent with no handler!");
        }
    }

}


