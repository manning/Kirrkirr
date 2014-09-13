package Kirrkirr.ui.panel;

import Kirrkirr.Kirrkirr;
import Kirrkirr.ui.KirrkirrButton;
import Kirrkirr.dictionary.DictField;
import Kirrkirr.dictionary.DictFields;
import Kirrkirr.ui.data.GameWordGenerator;
import Kirrkirr.util.Helper;
import Kirrkirr.util.Dbg;
import Kirrkirr.util.RelFile;
import Kirrkirr.util.FontProvider;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * This creates a word relationship game. It populates the FunPanel
 * with random words, and words related to those words, until it
 * has enough questions (MIN_QUESTIONS) or too many words in the funpanel
 * (MAX_NODES). Then it asks the player to double click a word that
 * has a given relationship with another word. This game only counts three
 * types of relationships: same, sub-entry/main-entry and opposite. The
 * beginning mode does not include opposite. "Same" means same-as or
 * same-meaning. "Sub" means sub-entry and "main" means main-entry. "Opposite"
 * only means opposite. The number of nodes each time varies, as does the
 * number of questions. The user can give up if s/he can't guess,
 * or if s/he doesn't get it in three tries, the computer tells the user
 * the answer. There may be repeats of questions, even one right after
 * another, since there may be several of the same relationship with
 * the same word. It's a generally easy game, since you can lookup
 * words by single clicking and looking at thier definitions on the
 * other panel, or just blindly guess.
 * <p>There used to be a generate report function which created
 * a kind of report card based on which words the user got right and
 * wrong. But when I changed this from a quiz to a game I removed that
 * function, though it may be useful to re-add later.
 * @author Madhu        kirrkirr version 2.1.5  copyright 2000
 * @author Kristen revised: version 2.1.7          4/2001
 *
 * - cw Aug 2002: got game working again, using the new dictInfo
 * spec. stuff.
 */
public class WordGamePanel extends GamePlayPanel implements ActionListener
{
    /* Static constants
     */

    /* static String constants that need to be translated
     * *txt is for the status box, below the picture.
     * *instr is for the large panel up top
     * *button goes on buttons
     */
    private static final String SC_NAME="Word_Game";
    private static final String SC_GAME_QUESTION="Game_Question";
    private static final String SC_SCORE="Score";
    private static final String SC_DESC="Play_a_game_locating_word_relationships";
    private static final String SC_GIVE_UP="I_give_up";
    private static final String SC_LONG_DESC =
        "This_is_the_word_matching_game.\n" +
        "Double_click_on_the_word_that_you_think_\n" +
        "matches_the_description_on_top_of_the_screen.\n" +
        "The_face_on_the_right_will_tell_you_whether_you're_right_or_wrong.";
    private static final String SC_WELCOME_TXT="Welcome_to_the_word_game!";
    private static final String SC_START_BUTTON="Start_Game";
    private static final String SC_GIVEUP_BUTTON="I_give_up";
    private static final String SC_NEXT_Q_BUTTON="Next_Question";
    private static final String SC_THREE_TRIES_TXT="You_guessed_wrong_3_times.";
    private static final String[] SC_GOOD_TXT={"Good_job!","Excellent!","Very_good!",
                                      "Super!","You_are_right!",
                                      "Fantastic!","Correct!","GREAT!"};
    private static final String[] SC_BAD_TXT={"Wrong,_try_again.","Wrong,_guess_again.",
                                     "Incorrect._Keep_trying.","Sorry,_that_is_incorrect",
                                      "Incorrect,_try_again."};
    private static final String SC_DONE_INSTR="Click_on_restart_for_a_new_game!";
    private static final String SC_DONE_TXT="Press_restart_to_play_again";
    private static final String[] SC_GIVEUP={"Here_is_a_word_that_means_the_same_as",
                                          "Here_is_a_word_that_whose_root_word_is",
                                          "Here_is_a_word_that_is_the_root_word_of",
                                          "Here_is_a_word_that_means_the_opposite_of"};
    private static final String[] SC_WORD_INSTR={"Double_click_on_a_word_which_means_the_same_as",
                                                  "Double_click_on_a_word_which_whose_root_word_is",
                                                  "Double_click_on_a_word_which_is_the_root_word_of",
                                                  "Double_click_on_a_word_which_means_the_opposite_of"};
    private static final String SC_HINTLABEL = "HINT";
    private static final String SC_HINTS_TAKEN = "Hints_taken";
    // private static final String SC_LEVEL[] = {"Beginner","Advanced"};


    /** Picture to show when user gets question right */
    private static final String RIGHT_PIC="smile.jpg";
    /** Picture to show when user gets question wrong */
    private static final String WRONG_PIC="frown.jpg";
    /** Picture to show when game is over */
    private static final String DONE_PIC="smile.jpg";
    /** Picture to show when game starts/restarts */
    private static final String START_PIC="smile.jpg";
    /** The minimum of questions to ask in one game */
    private static final int MIN_QUESTIONS=6;
    /** The maximum of (total) nodes to have in the graph panel in one game */
    private static final int MAX_NODES=10;
    /* Indices for the relations */
    private static final int SAME=0,SUB=1,MAIN=2,OPP=3;
    /** Maximum number of tries to give user on each question,
        before showing the answer */
    private static final int MAX_TRIES=3;
    /** Used to avoid too many sub/main questions in one game.
        (Since a lot of relations are sub/main).   */
    private static final int MAXSUB=2;

    private static final int NUM_LOOKUPS = 100;
    private static final int MAX_COUNTER = 20;

    private static final Color lightBrown = new Color(255, 213, 170);
    private static final Color lighterBrown = new Color(224, 192, 192);

    /* Game variables
     */
    private static Kirrkirr parent;

    public static String currWord;
    private static int hintsTaken;


    /** Beginner or advanced? */
    private boolean beginnerLevel;
    /** The current word being asked about.  A uniqueKey. */
    private String mainWord;
    /** The current "WordRelation" being asked about (see class below)
     *  (see WordGamePanel.WordRelation inner class)
     */
    private WordRelation currentwordRel;

    /** stores the DictField's of most of the words in the game */
    private Hashtable dictfields;
    /** stores all the words in the game, linked to thier wordrelations */
    private Hashtable wordHash;

    /** Possible answers for the current question */
    private Vector answers;
    /** List of possible mainWords */
    private Vector wordList;

    /** Color array corresponding to the indices for the relations */
    private Color[] relationColors;

    /** Num nodes currently in the graph panel */
    private int numnodes=0;
    /** Total questions in the current game */
    private int totalQuestions=0;
    /** Number right so far(used for score label) */
    private int numright=0;
    /** Count of current question (used for score label) */
    private int curQuest=0;
    /** The index of the current relation we're asking about */
    private int curRelation=0;
    /** Number of tries for current question */
    private int numTries=0;
    /** Number of sub/main questions in this game (used so dont make too many) */
    private int numSub=0;
    /** Number of sub/main questions in this game (used so dont make too many) */
    private int totalSub=0;

    /* Swing components
     */
    private OldFunPanel funPanel;
    private JTextArea statusTxt;
    private JLabel instrLabel, relationLabel, mainWordLabel, statusPicLabel,
        txtCurQuest, txtNumRight;
    private KirrkirrButton giveupButton, restartButton;
    // private JComboBox levelMenu;
    private static JLabel hintsLabel;
    private static JButton getHint;

    /**
     * Does the layout for the whole WordGamePanel.
     * If it's small, it won't display the status text.
     * @param kparent the Kirrkirr parent
     * @param size one of KirrkirrPanel.SMALL or .NORMAL
     */
    public WordGamePanel(Kirrkirr kparent, int size)
    {
        super();
        JFrame window = kparent.window;
        this.parent = kparent;
        setName(Helper.getTranslation(SC_NAME));

        boolean small = (size <= KirrkirrPanel.SMALL);

        this.setLayout(new BorderLayout());
        this.setBackground(lighterBrown);
        //this.setAlignmentX(Component.CENTER_ALIGNMENT);
        //this.setAlignmentY(Component.CENTER_ALIGNMENT);

        relationColors=new Color[4];
/*      relationColors[SAME]=DictField.getColor(DictField.SYNONYM);
        relationColors[SUB]=DictField.getColor(DictField.SUBENT).darker();
        relationColors[MAIN]=DictField.getColor(DictField.MAINENT).darker();
        relationColors[OPP]=DictField.getColor(DictField.ANTONYM);
*/
        /*********
         * questionPanel: helpLabel, relationLabel, wordLabel,
         *                giveupButton
         */

        JPanel questionPanel=new JPanel();
        questionPanel.setLayout(new FlowLayout(FlowLayout.CENTER,0,0));
        questionPanel.setBorder(BorderFactory.createTitledBorder(Helper.getTranslation(SC_GAME_QUESTION)));
        questionPanel.setBackground(lightBrown);
        questionPanel.setPreferredSize(new Dimension(400,65));
        questionPanel.setMinimumSize(new Dimension(200, 65));

        instrLabel=new JLabel("");
        instrLabel.setBackground(lightBrown);
        instrLabel.setForeground(Color.red.darker());
        instrLabel.setFont(FontProvider.PROMINENT_LARGE_INTERFACE_FONT);
        instrLabel.setVerticalAlignment(SwingConstants.TOP);

        relationLabel=new JLabel("");
        relationLabel.setBackground(lightBrown);
        relationLabel.setForeground(Color.red.darker());
        relationLabel.setFont(FontProvider.PROMINENT_LARGE_INTERFACE_FONT);
        relationLabel.setVerticalAlignment(SwingConstants.TOP);

        mainWordLabel=new JLabel("");
        mainWordLabel.setBackground(lightBrown);
        mainWordLabel.setForeground(Color.red.darker());
        mainWordLabel.setFont(FontProvider.PROMINENT_LARGE_INTERFACE_FONT);
        mainWordLabel.setVerticalAlignment(SwingConstants.TOP);

        giveupButton = new KirrkirrButton(Helper.getTranslation(SC_GIVEUP_BUTTON),this);

        questionPanel.add(instrLabel);
        questionPanel.add(relationLabel);
        questionPanel.add(mainWordLabel);
        questionPanel.add(Box.createHorizontalStrut(4));
        questionPanel.add(giveupButton);

        /********
         * basePanel: controlPanel, funPanel, perfPanel
         *
         */

        JPanel basePanel=new JPanel();
        basePanel.setLayout(new BoxLayout(basePanel,BoxLayout.X_AXIS));
        basePanel.setBackground(lightBrown);

        /********
         * controlPanel: levelPanel, restartButton
         *
         */

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(3,1));//new BoxLayout(controlPanel,BoxLayout.Y_AXIS));
        controlPanel.setBackground(lightBrown);
        controlPanel.setAlignmentX(0);//SwingConstants.TOP);
        controlPanel.setMaximumSize(new Dimension(150,90));

        /***** levelPanel: levelLabel and levelMenu -- commented out ---------
        JPanel levelPanel = new JPanel();
        levelPanel.setLayout(new BoxLayout(levelPanel,BoxLayout.X_AXIS));
        //levelPanel.setMaximumSize(new Dimension(250,200));
        //levelPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        levelPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
        levelPanel.setBackground(lightBrown);
        levelPanel.setMaximumSize(new Dimension(150,21));

        JLabel levelLabel = new JLabel(" "+Helper.getTranslation("Level")+": ");
        levelLabel.setBackground(lightBrown);
        levelLabel.setForeground(Color.black);
        levelLabel.setFont(FontProvider.PROMINENT_INTERFACE_FONT);
        levelLabel.setMaximumSize(new Dimension(90,90));

        //pulldown level menu
        levelMenu = new JComboBox(level);
        levelMenu.addActionListener(this);
        levelMenu.setFont(FontProvider.PROMINENT_INTERFACE_FONT);

        levelPanel.add(levelLabel);
        levelPanel.add(levelMenu);

        //restart button
        restartButton = new KirrkirrButton(Helper.getTranslation("Restart Game"),
                                           this);
        restartButton.setPreferredSize(new Dimension(30,21));
        restartButton.setFont(FontProvider.PROMINENT_INTERFACE_FONT);

        controlPanel.add(levelPanel);
        levelPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        controlPanel.add(restartButton);
        restartButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        ---------- *********/

        //for FunPanel
        GameGraphPanel graph = new GameGraphPanel(window, parent, this);
        funPanel = graph.funPanel;
        funPanel.legend = false;

        // performPanel: statusPic,statusTxt,scorePanel
        JPanel performPanel=new JPanel();
        performPanel.setLayout(new BoxLayout(performPanel,BoxLayout.Y_AXIS));
        performPanel.setBackground(lightBrown);

        ImageIcon statusPic=RelFile.makeImageIcon(START_PIC,false);
        //      ImageIcon statusPic=new ImageIcon(START_PIC,
        //                        Helper.getTranslation(SC_WELCOME_TXT));
        statusPicLabel=new JLabel(statusPic,JLabel.CENTER);

        //label with "score", plus textfield (ie scroller)
        JPanel scorePanel = new JPanel();
        scorePanel.setBackground(lightBrown);
        scorePanel.setLayout(new BoxLayout(scorePanel,BoxLayout.X_AXIS));
        scorePanel.setMaximumSize(new Dimension(150,25));
        scorePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel s0 = new JLabel(" "+Helper.getTranslation(SC_SCORE)+": ");
        s0.setOpaque(true);
        s0.setBackground(lightBrown);
        s0.setForeground(Color.black);
        s0.setFont(FontProvider.PROMINENT_LARGE_INTERFACE_FONT);
        scorePanel.add(s0);

        txtNumRight=new JLabel("0");
        txtNumRight.setFont(FontProvider.PROMINENT_LARGE_INTERFACE_FONT);
        txtCurQuest=new JLabel("0");
        txtCurQuest.setFont(FontProvider.PROMINENT_LARGE_INTERFACE_FONT);
        JLabel txtSlash=new JLabel(" / ");
        txtSlash.setFont(FontProvider.PROMINENT_LARGE_INTERFACE_FONT);

        scorePanel.add(txtNumRight);
        scorePanel.add(txtSlash);
        scorePanel.add(txtCurQuest);

        JPanel hintTracker = new JPanel();
        hintTracker.setLayout(new BoxLayout(hintTracker, BoxLayout.Y_AXIS));
        hintTracker.setBackground(lightBrown);
        hintTracker.setAlignmentX(Component.CENTER_ALIGNMENT);

        getHint = new JButton(SC_HINTLABEL);
        getHint.addActionListener(new ActionListener() {
          	@Override
                public void actionPerformed(ActionEvent e) {
          		Kirrkirr.setHintWord(WordGamePanel.getCurrWord());
          		WordGamePanel.addHint();
          		WordGamePanel.disableHintButton();
          	}
          });
        hintTracker.add(getHint);
        hintsLabel = new JLabel();
        hintsLabel.setText(Helper.getTranslation(SC_HINTS_TAKEN) + ": " + hintsTaken);

        hintTracker.add(hintsLabel);

        performPanel.add(statusPicLabel);
        statusPicLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusTxt = new JTextArea(Helper.getTranslation(SC_WELCOME_TXT));
        if (!small) {
            statusTxt.setEditable(false);
            statusTxt.setLineWrap(true);
            statusTxt.setWrapStyleWord(true);
            statusTxt.setBackground(lightBrown);
            statusTxt.setForeground(Color.red.darker());
            statusTxt.setFont(FontProvider.PROMINENT_INTERFACE_FONT);
            statusTxt.setBorder(BorderFactory.createEmptyBorder());
            JScrollPane scroller1 = new JScrollPane(statusTxt,ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scroller1.setMaximumSize(new Dimension(160,60));
            scroller1.setPreferredSize(new Dimension(130,40));
            scroller1.setBorder(BorderFactory.createEmptyBorder());
            scroller1.setAlignmentX(Component.CENTER_ALIGNMENT);
            performPanel.add(scroller1);
            performPanel.add(scorePanel);
        }
        performPanel.add(hintTracker);
        performPanel.add(Box.createHorizontalStrut(2));



        basePanel.add(controlPanel);
        basePanel.add(graph.funScrollPane);
        basePanel.add(performPanel);

        add(questionPanel,BorderLayout.NORTH);
        add(basePanel,BorderLayout.CENTER);

        kparent.addHintPanel();
        kparent.hintsOn = true;
        repaint();
    }


    /** Returns the String that is suitable rollover text for a tabbed
     *  pane containing this panel.
     *  @return the string to be used as rollover text
     */
    public String getTabRollover() {
        return Helper.getTranslation(SC_DESC);
    }

    /** <p>Called when the current pane's tab is clicked on;
     *  Creates a new game.
     */
    public void tabSelected()
    {
        startGame();
    }

    @Override
    public boolean horizontalLayout(){
        return true;
    }

    public static void addHint() {
    	hintsTaken++;
    	hintsLabel.setText(Helper.getTranslation(SC_HINTS_TAKEN) + ": " + hintsTaken);
    }

    public static void disableHintButton() {
      	getHint.setEnabled(false);
      }

    public static void enableHintButton() {
      	getHint.setEnabled(true);
      }

    public static String getCurrWord() { return currWord; }

    /** Sets the text labels at the top of the pane.
     *  The order of them is instrLabel/relationLabel/mainWordLabel.
     *  This sets instrLabel to nothing, relationLabel to the
     *  relation question passed in as "question" and mainWord
     *  to the word passed in. It also sets the relation label
     *  to the color of the relation and the main word label
     *  to dark red. The relation int passed in corresponds
     *  to the relation index, or -1 if question is null. (Then
     *  the relationLabel is set to blank).
     *  @param relation the index of the relation the question is
     *  about, or -1 if the question is null. (Used to set the color
     *  of the question).
     *  @param question the question to ask, or null.
     *  @param word the word to ask the question about.
     */
    private void setQuestion(String question,int relation,String word)
    {
        instrLabel.setText("");//Helper.getTranslation(instr));
        if (relation==-1)
            relationLabel.setText("");
        else
            {
                relationLabel.setText(Helper.getTranslation(question)+" ");
                relationLabel.setForeground(relationColors[relation]);
            }
        mainWordLabel.setText(word);
        mainWordLabel.setForeground(Color.red.darker());
    }

    /** Called at the end of each guess. Sets the face picture
     *  according to good. If the question is done, gives them
     *  another question or ends the game, otherwise keeps
     *  track of their numTries and tells them the answer if
     *  they've reached MAX_TRIES.
     *  @param good whether the user has gotten the question right.
     *  @param clicked the word they clicked.
     */
    private void setStatus(boolean good, String clicked)
    {
        String text;
        ImageIcon pic;
        if (good) {
            pic=RelFile.makeImageIcon(RIGHT_PIC, false);
            //          pic=new ImageIcon(RIGHT_PIC,Helper.getTranslation("You got it right"));
            int rnd = GameWordGenerator.randomInt(SC_GOOD_TXT.length);
            text = Helper.getTranslation(SC_GOOD_TXT[rnd]);
            numright++;
            txtNumRight.setText((new Integer(numright)).toString());
            numTries=0;
            txtCurQuest.setText((new Integer(curQuest)).toString());

            WordRelation wr=(WordRelation)wordHash.get(clicked);
            wr.remove(mainWord);
            currentwordRel.remove(clicked);
            disableHintButton();
            funPanel.addEdgeForGame(mainWord, wr.getDictField());
            addNewQuestion();
        } else {
            numTries++;
            pic=RelFile.makeImageIcon(WRONG_PIC,false);//new ImageIcon(WRONG_PIC,Helper.getTranslation("You got it wrong"));
            int rnd = GameWordGenerator.randomInt(SC_BAD_TXT.length);
            text = Helper.getTranslation(SC_BAD_TXT[rnd]);
        }
        statusTxt.setText(text);
        statusPicLabel.setIcon(pic);
        if (numTries>=MAX_TRIES){
            statusTxt.setText(Helper.getTranslation(SC_THREE_TRIES_TXT));
            giveup();
            numTries=0;
        }
        if (curQuest>=totalQuestions)
            endOfGame();
    }

    /** Needed to extend GamePanel. Just calls startGame().
     */
    @Override
    public void restartGame()
    {
        startGame();
    }

    /**
     * Loads a new game into the game panel. Occurs when user presses
     * clicks on the game tab, or presses "restart game."
     */
    @Override
    public void startGame()
    {
        wordHash=new Hashtable();
        wordList=new Vector();
        answers=new Vector();
        dictfields=new Hashtable();
        totalQuestions=0;
        numSub=0;
        totalSub=0;
        numnodes=0;
        numright=0;
        numTries=0;
        curQuest=0;
        hintsTaken = 0;

        statusTxt.setText(Helper.getTranslation(SC_WELCOME_TXT));
        statusPicLabel.setIcon(RelFile.makeImageIcon(START_PIC,false));
        //      statusPicLabel.setIcon(new ImageIcon(START_PIC));
        giveupButton.setEnabled(true);
        giveupButton.setVisible(true);
        txtCurQuest.setText("0");
        txtNumRight.setText("0");
        giveupButton.setText(Helper.getTranslation(SC_GIVEUP_BUTTON));
        giveupButton.setBackground(Color.gray.brighter());

        createNewGame();
        if (totalQuestions == 0) {
            giveupButton.setText(Helper.getTranslation(SC_START_BUTTON));
            giveupButton.setBackground(Color.red);
            return;
        }
        addNewQuestion();
        hintsLabel.setText(Helper.getTranslation(SC_HINTS_TAKEN) + ": " + hintsTaken);
        disableHintButton();
    }


    private void addNewQuestion()
    {
        if (Dbg.KRISTEN) Dbg.print("add new question");
        curQuest++;
        //check if out of q's
        if (curQuest>totalQuestions)
        {
            if (Dbg.KRISTEN) Dbg.print("done!");
            return;
        }

        boolean found=false;
        while (!found)
        {
            int randint = GameWordGenerator.randomInt(wordList.size());
            mainWord = (String) wordList.elementAt(randint);
            currentwordRel=(WordRelation)wordHash.get(mainWord);
            if (Dbg.KRISTEN) Dbg.print(mainWord+" "+currentwordRel);
            if (currentwordRel!=null){
                answers=null;
                for (int i=0; i<4 && !found; i++) {
                    int curint=i;
                    if (beginnerLevel && i==OPP) break;
                    if (currentwordRel.hasRelation(curint)) {
                        if (((i==SUB || i==MAIN) && numSub<=MAXSUB) ||
                            i==SAME || i==OPP)
                        {
                            if (i==SUB || i==MAIN) numSub++;
                            answers=currentwordRel.getVector(curint);
                            curRelation=curint;
                            found=true;
                            setQuestion(Helper.getTranslation(SC_WORD_INSTR[curRelation]), curRelation, Helper.getWord(mainWord));
                            break;
                        }
                    }
                }
            }
        }
    }


   /**
     * Gets called by FunPanel when a node in the funpanel is
     * double clicked.
     * Checks whether that word is right or wrong for the current
     * question, and updates the status/buttons/questions as
     * needed.
     * @param word the word clicked
     */
    public void wordClicked(String word)
    {
        if (Dbg.KRISTEN) Dbg.print("word clicked");
        if (word.equals(mainWord))
            return;
        if (!answers.contains(word))
            {
                setStatus(false,null);
                return;
            }
        setStatus(true,word);
    }

    private void giveup()
    {
        if (giveupButton.getText().equalsIgnoreCase(Helper.getTranslation(SC_NEXT_Q_BUTTON)))
            {
                giveupButton.setText(Helper.getTranslation(SC_GIVEUP_BUTTON));
                giveupButton.setBackground(Color.gray.brighter());
                txtCurQuest.setText((new Integer(curQuest)).toString());
                if (curQuest>=totalQuestions)
                    endOfGame();
                else
                    addNewQuestion();
                return;
            }
        String answer = (String) answers.elementAt(0);   // a uniqueKey
        WordRelation wr=(WordRelation)wordHash.get(answer);
        wr.remove(mainWord);
        currentwordRel.remove(answer);

        txtCurQuest.setText((new Integer(curQuest)).toString());
        instrLabel.setText(Helper.getTranslation(SC_GIVEUP[curRelation])+" ");
        relationLabel.setText(Helper.getWord(mainWord)+": ");
        relationLabel.setForeground(Color.red.darker());
        mainWordLabel.setText(Helper.getWord(answer));
        mainWordLabel.setForeground(Color.blue);

        funPanel.addEdgeForGame(mainWord, wr.getDictField());

        giveupButton.setText(Helper.getTranslation(SC_NEXT_Q_BUTTON));
        giveupButton.setBackground(Color.yellow);
        //choose a word from the list which is of the
        //relation type, and add the edge to the
        //fun panel. update score, curQuest
    }

    private int addWordNet() {
        if (Dbg.KRISTEN) Dbg.print("addwordnet");
        int counter = 0;
        do {
            mainWord = GameWordGenerator.GetGameHeadword(getDifficulty(),
                                                         parent);
            counter++;
        } while(wordList.contains(mainWord) && counter < NUM_LOOKUPS);
        if (counter >= NUM_LOOKUPS) {
            return 0;
        }

        DictFields dfs=parent.cache.getDictEntryLinks(mainWord);
        if (Dbg.KRISTEN) Dbg.print("dfs "+dfs);

        WordRelation wr=(WordRelation) wordHash.get(mainWord);
        if (wr==null) wr = new WordRelation(mainWord);
        int currealones=0;
        for (int i=0;i<dfs.size() && numnodes<MAX_NODES;i++) {
            DictField di=dfs.get(i);
            int relation=-1;
            String curword=di.uniqueKey;
            // watch out: di.value is not a unique key - cw 2002

//          String curtag="";//DictField.getTag(di.tag);

//          if (parent.cache.hasRegister(Helper.makeUniqueKey(curword,di.hnum))) continue;

//          if (curtag.equals("SYN")|| curtag.equals("XME")) {
//              relation=SAME;
//          }
//          else if (curtag.equals("SE") &&
//                    !di.value.equals(mainWord))  {
//              relation=SUB;
//          }
//          else if (curtag.equals("CME") &&
//                   !di.value.equals(mainWord))
//              {
//                  relation=MAIN;
//              }
//          else if (!beginnerLevel &&
//                   curtag.equals("ANT")) {
//              relation=OPP;
//          }
            // cw 2002: hook up to use new dictInfo stuff.  this algorithm
            // is still somewhat crude, though...
            String linkName = Kirrkirr.dictInfo.getLinkName(di.tag);
            if (linkName.startsWith("Same")) {
                relation = SAME;
            } else if (linkName.startsWith("Sub") && !curword.equals(mainWord)) {
                relation = SUB;
            } else if (linkName.startsWith("Main") && !curword.equals(mainWord)) {
                relation = MAIN;
            } else if (!beginnerLevel && linkName.equals("Opposite")) {
                relation = OPP;
            }

            if (relation!=-1) {
                if (((relation==SUB || relation==MAIN) && totalSub<MAXSUB)
                    || relation==OPP || relation==SAME){
                    if (wr.addRelation(relation,curword)==-1) break;
                    if (relation==SUB || relation==MAIN) totalSub++;
                    totalQuestions++;
                    currealones++;
                    numnodes++;
                    dictfields.put(curword,di);
                    WordRelation next=(WordRelation)wordHash.get(curword);
                    if (next!=null)
                        next.addRelation(relation,mainWord);
                    else {
                        // XXXX a WordRelation main is later used as a
                        // hnum-padded string, but curword _isn't_ padded??
                        // or is it just words with no exact>??
                        next=new WordRelation(curword,di);
                        next.addRelation(relation,mainWord);
                        wordHash.put(curword,next);
                    }

                    currentwordRel=(WordRelation)wordHash.get(mainWord);
                    if (currentwordRel==null){
                        wordHash.put(mainWord,wr);
                        currentwordRel=wr;
                    }
                    if (!wordList.contains(mainWord)){
                        wordList.addElement(mainWord);
                        numnodes++;
                    }
                }
            }
            if (Dbg.KRISTEN) Dbg.print("looping: numnodes"+numnodes);
        }
        if (Dbg.KRISTEN) Dbg.print("done currealones"+currealones+"numnodes "+numnodes+" "+wordHash);
        return currealones;
    }


    private void createNewGame()
    {
        if (parent.headwordsListSize() == 0) return;

        //get currentlevel
        // int index=0;
        // if (levelMenu!=null)
        //     index=levelMenu.getSelectedIndex();
        // String currentlevel= SC_LEVEL[index];
        beginnerLevel = (getDifficulty() == GameWordGenerator.EASY);

        //randomly find word until
        //check that it has >=min <=max
        //numfields of types
        //same as, is a, main, sub
        //if adv, count oppposite too
        int counter = 0;
        while (numnodes < MAX_NODES &&
                totalQuestions < MIN_QUESTIONS &&
                counter < MAX_COUNTER) {
            addWordNet();
            counter++;
        }
        if (Dbg.KRISTEN) Dbg.print("found "+mainWord+"+"+numnodes+" "+totalQuestions);

        redrawFunPanel();
        if (Dbg.KRISTEN) Dbg.print("done");
        //mainWord=that word
        //add main's same is, is a words to answerList as same
        //add subentries, mainword(if !=main) to answerList as sub
        //if adv, add opposites.
        //make vectors for same,sub,opp. [remove one everytime they get it]
        //populate funpanel
        //update totalQuest,curQuest
        //update question
    }


    private void redrawFunPanel()
    {
        if (Dbg.KRISTEN) Dbg.print("redraw fun panel "+mainWord);

        funPanel.clearFunPanel();
        Vector v=new Vector();
        Enumeration e=wordHash.elements();
        while (e.hasMoreElements())
            {
                WordRelation wr=(WordRelation)e.nextElement();
                DictField df=wr.getDictField();
                if (df!=null) {
                    v.addElement(df);
                } else {
                    funPanel.addNode(wr.getWord());
                }
            }
        //is mainword added here? if not can add sep.
        funPanel.addTextForGame(mainWord,
                                new DictFields(v));
        funPanel.start();
        funPanel.scrambleShake(false);
        funPanel.K2 = 400.0;
        funPanel.K3 = 500.0;
        repaint();
    }

    private void endOfGame()
    {
        if (Dbg.KRISTEN) Dbg.print("endOfGame");
        statusTxt.setText(Helper.getTranslation(SC_DONE_TXT));
        statusPicLabel.setIcon(RelFile.makeImageIcon(DONE_PIC,false));//new ImageIcon(DONE_PIC));
        giveupButton.setText(Helper.getTranslation(SC_GIVE_UP));
        giveupButton.setEnabled(false);
        giveupButton.setVisible(false);
        //createReportButton.setEnabled(true);
        //setQuestion(Helper.getTranslation(SC_DONE_INSTR1),-1,Helper.getTranslation(SC_DONE_INSTR2));
        instrLabel.setText("");
        relationLabel.setText("");//SC_DONE_INSTR1+" ");
        relationLabel.setBackground(Color.black);
        mainWordLabel.setText(Helper.getTranslation(SC_DONE_INSTR));
        relationLabel.setBackground(Color.black);
        disableHintButton();
    }

    /**
     * Handles all the button presses and the levelMenu switch,
     * namely reset button and giveup button (or next question button)
     * @param e the event that occured
     */
    @Override
    public void actionPerformed(ActionEvent e)
    {
        Object src = e.getSource();

        if(src == giveupButton) {
            if (giveupButton.getText().equalsIgnoreCase(Helper.getTranslation(SC_START_BUTTON)))
                startGame();
            else
                giveup();
        } else if (src == restartButton) {
            startGame();
        }
    }

    public static String getDesc(){
        return (Helper.getTranslation(SC_LONG_DESC));
    }

}

class WordRelation {

    private String main;
    private Vector[] lists;
    private DictField maindf; // =null;

    public WordRelation(String word) {
        main=word;
        lists=new Vector[4];
        lists[0]=new Vector();
        lists[1]=new Vector();
        lists[2]=new Vector();
        lists[3]=new Vector();
    }

    public WordRelation(String word, DictField df)
    {
        this(word);
        maindf=df;
    }

    public DictField getDictField()
    {
        return maindf;
    }

    public String getWord(){ return main; }

    public int addRelation(int rel, String word)
    {
        if (lists[rel].contains(word)) return -1;
        lists[rel].addElement(word);
        return 1;
    }

    public int getRelation(String word)
    {
        for (int i=0;i<4;i++)
            if (lists[i].contains(word))
                return i;
        return -1;
    }

    public Vector getVector(int rel)
    {
        //      if (lists[rel].size()==0) return null;
        return lists[rel];
    }

    public void remove(String word)
    {
        for (int i=0;i<4;i++)
            if (lists[i].contains(word))
                lists[i].removeElement(word);
    }

    public boolean hasRelation(int rel)
    {
        return ! lists[rel].isEmpty();
    }

    public String toString()
    {
        return(main +": lists0"+lists[0]+"\nlists1 "+lists[1]+"\nlists2 "
        +lists[2]+"\nlist3 "+lists[3]);
    }

}


/** Used so that the quiz's graph panel also notifies
 *  the other panels so they can be updated. Also,
 *  so it can call quizmasterpanel directly (since its not
 *  a panel).
 */
class GameGraphPanel extends OldGraphPanel {
    private WordGamePanel qmp;

    public GameGraphPanel(JFrame window, Kirrkirr click,
                          WordGamePanel qmp)
    {
        super(click, window, false, KirrkirrPanel.TINY);
        this.qmp = qmp;
    }

    //double click
    @Override
    public void funGetLinks(String clicked)
    {
        super.funGetLinks(clicked);
        qmp.wordClicked(clicked);
    }

    //single click
    @Override
    public void funFindWord(String clicked)
    {
        super.funFindWord(clicked);
        WordGamePanel.currWord = clicked;
        WordGamePanel.enableHintButton();
        //qmp.wordClicked(clicked);
    }

}


