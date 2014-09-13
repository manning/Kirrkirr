package Kirrkirr.ui.panel;

import Kirrkirr.Kirrkirr;
import Kirrkirr.ui.data.CrosswordGenerator;
import Kirrkirr.ui.data.CrosswordInfo;
import Kirrkirr.util.Helper;
import Kirrkirr.util.Dbg;
import Kirrkirr.util.FontProvider;

import java.awt.*;
import java.awt.event.*;


public class CrosswordPanel extends GamePlayPanel implements MouseListener, KeyListener {

    private static final String SC_ACROSS = "across";
    private static final String SC_DOWN   = "down";
    private static final String SC_CROSSWORD_DESC =
        "The Crossword Puzzle Game!\n" +
        "This game generates a random Headword crossword for you,\n" +
        "with clues in Gloss.  Use your mouse to choose the square\n" +
        "you want to fill in by clicking on it.  " +
        "The Gloss clue for that square\n" +
        "will be shown at the top in blue.  " +
        "Use your keyboard to fill in the spaces.\n" +
        "Letters that appear red are incorrect.\n" +
        "Letters that appear black match the clue correctly.\n" +
        "Work until all the words are entered in black.";

    private Kirrkirr parent;

    //static constants
    private static final int    kBlockWidth     = 25;
    private static final int    kBlockHeight    = 25;

    private static final int    kBlocksWide     = CrosswordGenerator.MATSIZE_X;
    private static final int    kBlocksHigh     = CrosswordGenerator.MATSIZE_Y;

    private static final int    kAcross = 0;
    private static final int    kDown = 1;

    private static final int kPadding = 1;//20;
    private static final int kQuestionAreaHeight = 40;

    //variables
    private int gDirection = kAcross;
    private int gCurX   = 0;
    private int gCurY   = 0;

    private int         gBlockMinY      = 0;
    private int         gBlockMaxY      = 0;
    private int         gBlockMinX      = 0;
    private int         gBlockMaxX      = 0;

    private int         gOldBlockMinY   = 0;
    private int         gOldBlockMaxY   = 0;
    private int         gOldBlockMinX   = 0;
    private int         gOldBlockMaxX   = 0;

    private int         gOlderBlockMinY = 0;
    private int         gOlderBlockMaxY = 0;
    private int         gOlderBlockMinX = 0;
    private int         gOlderBlockMaxX = 0;

    /*---------------------------------------------------------------*/

    private int[][] layout;
    private char[][] answers;
    private String[] gQuestionsAcross;
    private String[] gQuestionsDown;
    private CrosswordInfo firstWord;
    /*---------------------------------------------------------------*/

    private String[][] gGuesses = new String[kBlocksWide][kBlocksHigh];

    private boolean     gUpdateActiveAreaFlag = false;
    private boolean     gChangedActiveAreaFlag = false;


   /*---------------------------------------------------------------*/

    public CrosswordPanel(Kirrkirr parent)
    {
        super();
        this.parent = parent;
        addMouseListener(this);
        addKeyListener(this);

        setPreferredSize(new Dimension((kBlocksWide * kBlockWidth) + (kPadding * 2), (kBlocksHigh * kBlockHeight) + (kPadding * 3) + kQuestionAreaHeight));
    }

    public boolean horizontalLayout(){
        return false;
    }

    public void startGame()
    {
        GenerateBoard();
        NewGame();
        revalidate();
        requestFocus();
        repaint();
    }


    public void restartGame()
    {
      startGame();
    }

    private void GenerateBoard()
    {
        CrosswordGenerator generator = new CrosswordGenerator(parent,
                                                              getDifficulty());
        generator.generateBoard();
        gQuestionsAcross = generator.getProcessedAcrossQuestions();
        gQuestionsDown = generator.getProcessedDownQuestions();
        layout = generator.getProcessedLayout();
        answers = generator.getProcessedAnswers();
        firstWord = generator.getBeginningWord();
    }


    public void NewGame() {
        for (int j = 0 ; j < kBlocksHigh ; j++) {
            for (int i = 0 ; i < kBlocksWide ; i++) {
                gGuesses[i][j] = "";
            }
        }

        gOldBlockMinY   = 0;
        gOldBlockMaxY   = 0;
        gOldBlockMinX   = 0;
        gOldBlockMaxX   = 0;

        if(firstWord.direction == CrosswordGenerator.HORIZONTAL)
                gDirection = kAcross;
        else
          gDirection = kDown;
        gCurX = firstWord.x;
        gCurY = firstWord.y;
        SetActiveBlock(gCurX, gCurY, gDirection);
    }

    /*----------------------------------------------*/

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Font answerFont = FontProvider.HUGE_WORD_FONT;
        Font questionFont = FontProvider.GIGANTIC_WORD_FONT;
        Font questionFont18 = FontProvider.HUGE_WORD_FONT;

        FontMetrics answerFontMetrics = g.getFontMetrics(answerFont);
        FontMetrics questionFontMetrics = g.getFontMetrics(questionFont);

        g.setColor(Color.white);
        g.draw3DRect(0, 0, getSize().width - 1, getSize().height - 1, true);
        int viewWidth = kBlocksWide * kBlockWidth;
        int viewHeight = kBlocksHigh * kBlockHeight;
        int top = kPadding;
        int left = (getSize().width / 2) - (viewWidth / 2);
        g.fillRect(left, top, viewWidth, kQuestionAreaHeight);//, false);

        setPreferredSize(new Dimension(viewWidth,viewHeight+kQuestionAreaHeight));

        g.setFont(FontProvider.WORD_LIST_FONT);
        g.setColor(Color.blue);
        StringBuffer sb =
            new StringBuffer(String.valueOf(layout[gBlockMinY][gBlockMinX]));
        sb.append(" - ");
        if (gDirection == kAcross)
            sb.append(Helper.getTranslation(SC_ACROSS));
        else
            sb.append(Helper.getTranslation(SC_DOWN));
        g.drawString(sb.toString(), left + 5, top + 12);
        g.setFont(questionFont);
        if (gDirection == kAcross) {
            if (questionFontMetrics.stringWidth(gQuestionsAcross[layout[gBlockMinY][gBlockMinX]]) > viewWidth - 4)
            {
                g.setFont(questionFont18);
            }

            g.drawString(gQuestionsAcross[layout[gBlockMinY][gBlockMinX]],
                         (getSize().width / 2) - (g.getFontMetrics().stringWidth(gQuestionsAcross[layout[gBlockMinY][gBlockMinX]]) / 2),
                         (top + kQuestionAreaHeight) - 8);

        } else {
            if (questionFontMetrics.stringWidth(gQuestionsDown[layout[gBlockMinY][gBlockMinX]]) > viewWidth - 4)
            {
                g.setFont(questionFont18);
            }

            g.drawString(gQuestionsDown[layout[gBlockMinY][gBlockMinX]],
                         (getSize().width / 2) - (g.getFontMetrics().stringWidth(gQuestionsDown[layout[gBlockMinY][gBlockMinX]]) / 2),
                         (top + kQuestionAreaHeight) - 8);
        }

        left = (getSize().width / 2) - (viewWidth / 2);
        top = (kPadding * 2) + kQuestionAreaHeight;

        for (int j = 0 ; j < kBlocksHigh ; j++) {
            for (int i = 0 ; i < kBlocksWide ; i++) {
                int tempLeft = left + (i * kBlockWidth);
                int tempTop = top + (j * kBlockHeight);

                if (WithinActiveBlock(i, j)) {
                    if (i == gCurX && j == gCurY)
                        g.setColor(Color.cyan);
                    else
                        g.setColor(Color.yellow);
                    g.fillRect(tempLeft, tempTop, kBlockWidth, kBlockHeight);
                }
                else {
                    g.setColor(Color.white);
                    g.fillRect(tempLeft, tempTop, kBlockWidth, kBlockHeight);
                }

                g.setColor(Color.black);
                g.drawRect(tempLeft, tempTop, kBlockWidth , kBlockHeight );

                if (layout[j][i] == -1){
                    g.setColor(Color.black);
                    g.fillRect(tempLeft, tempTop, kBlockWidth, kBlockHeight);
                }
                else if (layout[j][i] != 0) {
                    String numStr = String.valueOf(layout[j][i]);

                    g.setFont(FontProvider.SMALL_WORD_FONT);
                    g.drawString(numStr, tempLeft + 4 , tempTop + 10);

                }

                // -- put in text if needed

                if (layout[j][i] != -1) {
                    if (gGuesses[i][j].length() != 0) {
                        if ( ! gGuesses[i][j].equalsIgnoreCase(String.valueOf(answers[i][j]))) {
                            g.setColor(Color.red);
                        } else {
                            g.setColor(Color.black);
                        }
                        int sWidth = answerFontMetrics.stringWidth(gGuesses[i][j]);
                        g.setFont(answerFont);
                        g.drawString( gGuesses[i][j], tempLeft + ((kBlockWidth / 2) - (sWidth / 2)), (tempTop + kBlockHeight) - 6);
                    }
                }
            }
        }
    }


    /*----------------------------------------------*/

    void PaintWord(Graphics g, int minX, int maxX, int minY, int maxY) {
        int viewWidth = kBlocksWide * kBlockWidth;
        // int viewHeight = kBlocksHigh * kBlockHeight;
        int left = (getSize().width / 2) - (viewWidth / 2);
        int top = (kPadding * 2) + kQuestionAreaHeight;

        left += (minX * kBlockWidth);
        top += (minY * kBlockHeight);
        /*
        g.clipRect(     left, top,
                   (kBlockWidth * (maxX - minX)) + kBlockWidth,
                   (kBlockHeight * (maxY - minY)) + kBlockHeight);
                   */


        g.setFont(FontProvider.WORD_LIST_FONT);

        Font answerFont = FontProvider.HUGE_WORD_FONT;
        FontMetrics answerFontMetrics = g.getFontMetrics(answerFont);

        viewWidth = kBlocksWide * kBlockWidth;
        // viewHeight = kBlocksHigh * kBlockHeight;

        left = (getSize().width / 2) - (viewWidth / 2);
        top = (kPadding * 2) + kQuestionAreaHeight;

        for (int j = minY ; j <= maxY ; j++) {
            for (int i = minX ; i <= maxX ; i++) {
                int tempLeft = left + (i * kBlockWidth);
                int tempTop = top + (j * kBlockHeight);

                if (WithinActiveBlock(i, j)) {
                    if (i == gCurX && j == gCurY)
                        g.setColor(Color.cyan);
                    else
                        g.setColor(Color.yellow);
                    g.fillRect(tempLeft, tempTop, kBlockWidth, kBlockHeight);
                }
                else {
                    g.setColor(Color.white);
                    g.fillRect(tempLeft, tempTop, kBlockWidth, kBlockHeight);
                }

                g.setColor(Color.black);
                g.drawRect(tempLeft, tempTop, kBlockWidth , kBlockHeight );

                if (layout[j][i] == -1) {
                    g.setColor(Color.black);
                    g.fillRect(tempLeft, tempTop, kBlockWidth, kBlockHeight);
                }
                else if (layout[j][i] != 0) {
                    String numStr = String.valueOf(layout[j][i]);

                    g.setFont(FontProvider.SMALL_WORD_FONT);
                    g.drawString(numStr, tempLeft + 4 , tempTop + 10);

                }

                // -- put in text if needed

                if (layout[j][i] != -1) {
                    if (gGuesses[i][j].length() != 0) {
                        if ( ! gGuesses[i][j].equalsIgnoreCase(String.valueOf(answers[i][j]))) {
                            g.setColor(Color.red);
                        } else {
                            g.setColor(Color.black);
                        }
                        int sWidth = answerFontMetrics.stringWidth(gGuesses[i][j]);
                        g.setFont(answerFont);
                        g.drawString( gGuesses[i][j], tempLeft + ((kBlockWidth / 2) - (sWidth / 2)), (tempTop + kBlockHeight) - 6);
                    }

                }

            }
        }
    }

    /*----------------------------------------------*/

    void PaintQuestionArea(Graphics g) {
        Font questionFont = FontProvider.GIGANTIC_WORD_FONT;
        Font questionFont18 = FontProvider.HUGE_WORD_FONT;
        FontMetrics questionFontMetrics = g.getFontMetrics(questionFont);

        int viewWidth = kBlocksWide * kBlockWidth;
        // int viewHeight = kBlocksHigh * kBlockHeight;

        int top = kPadding;
        int left = (getSize().width / 2) - (viewWidth / 2);
        //g.clipRect(left, top, viewWidth, kQuestionAreaHeight);

        g.setColor(Color.white);
        g.fillRect(left, top, viewWidth, kQuestionAreaHeight);//, false);

        g.setFont(FontProvider.WORD_LIST_FONT);
        g.setColor(Color.blue);
        String s = String.valueOf(layout[gBlockMinY][gBlockMinX]);
        s = s.concat(" - ");
        if (gDirection == kAcross)
            s = s.concat(SC_ACROSS);
        else
            s = s.concat(SC_DOWN);
        g.drawString(s, left + 5, top + 12);
        g.setFont(questionFont);
        if (gDirection == kAcross) {
            if (questionFontMetrics.stringWidth(gQuestionsAcross[layout[gBlockMinY][gBlockMinX]]) > viewWidth - 4) {
                g.setFont(questionFont18);
            }

            g.drawString(gQuestionsAcross[layout[gBlockMinY][gBlockMinX]],
                         (getSize().width / 2) - (g.getFontMetrics().stringWidth(gQuestionsAcross[layout[gBlockMinY][gBlockMinX]]) / 2),
                         (top + kQuestionAreaHeight) - 8);

        } else {
            if (questionFontMetrics.stringWidth(gQuestionsDown[layout[gBlockMinY][gBlockMinX]]) > viewWidth - 4) {
                g.setFont(questionFont18);
            }

            g.drawString(gQuestionsDown[layout[gBlockMinY][gBlockMinX]],
                         (getSize().width / 2) - (g.getFontMetrics().stringWidth(gQuestionsDown[layout[gBlockMinY][gBlockMinX]]) / 2),
                         (top + kQuestionAreaHeight) - 8);
        }

    }

    /*----------------------------------------------*/

    private boolean WithinActiveBlock(int x, int y) {
        if (x < gBlockMinX)
            return(false);
        if (x > gBlockMaxX)
            return(false);
        if (y < gBlockMinY)
            return(false);
        if (y > gBlockMaxY)
            return(false);

        return(true);
    }

    /*----------------------------------------------*/

    private void SetActiveBlock(int x, int y, int direction) {

        gOlderBlockMinY = gOldBlockMinY;
        gOlderBlockMaxY = gOldBlockMaxY;
        gOlderBlockMinX = gOldBlockMinX;
        gOlderBlockMaxX = gOldBlockMaxX;

        gOldBlockMinY   = gBlockMinY;
        gOldBlockMaxY   = gBlockMaxY;
        gOldBlockMinX = gBlockMinX;
        gOldBlockMaxX   = gBlockMaxX;


        if (direction == kAcross) {
            gBlockMinY = y;
            gBlockMaxY = y;
            int tempx = x;
            while (tempx > 0 && layout[y][tempx] != -1) {
                tempx--;
            }
            if (tempx > 0)
                gBlockMinX = tempx + 1;
            else {
                if (layout[y][0] == -1)
                    gBlockMinX = 1;
                else
                    gBlockMinX = 0;
            }

            tempx = x;
            while (tempx < kBlocksWide && layout[y][tempx] != -1)
            {
                tempx++;
            }
            gBlockMaxX = tempx -1;
        } else {
            gBlockMinX = x;
            gBlockMaxX = x;
            int tempy = y;
            while (tempy > 0 && layout[tempy][x] != -1) {
                tempy--;
            }
            if (tempy > 0)
                gBlockMinY = tempy + 1;
            else {
                if (layout[0][x] == -1)
                    gBlockMinY = 1;
                else
                    gBlockMinY = 0;
            }

            tempy = y;
            while (tempy < kBlocksHigh && layout[tempy][x] != -1) {
                tempy++;
            }
            gBlockMaxY = tempy -1;
        }
    }

    /*----------------------------------------------*/

    public void update(Graphics g)  {
        if (gChangedActiveAreaFlag == false && gUpdateActiveAreaFlag == false) {
            paint(g);
            return;
        }

        if (gChangedActiveAreaFlag) {
            PaintQuestionArea(g);
            PaintWord(g, gOlderBlockMinX, gOlderBlockMaxX, gOlderBlockMinY, gOlderBlockMaxY);
            PaintWord(g, gOldBlockMinX, gOldBlockMaxX, gOldBlockMinY, gOldBlockMaxY);
            PaintWord(g, gBlockMinX, gBlockMaxX, gBlockMinY, gBlockMaxY);
            gChangedActiveAreaFlag = false;
            return;
        }

        //-----------------------------------------------

        if (gUpdateActiveAreaFlag == true) {
            gUpdateActiveAreaFlag = false;
            PaintWord(g, gBlockMinX, gBlockMaxX, gBlockMinY, gBlockMaxY);
            return;
        }

    }

    /*----------------------------------------------*/

    private void beep() {
        //play(getCodeBase(), "nope.au");
        //System.out.println("beep");
    }

    public void mouseClicked(MouseEvent e) {}

    /*----------------------------------------------*/

    //public boolean mouseDown(java.awt.Event evt, int x, int y) {
    public void mousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        int viewWidth = kBlocksWide * kBlockWidth;
        int left = (getSize().width / 2) - (viewWidth / 2);
        int top = (kPadding * 2) + kQuestionAreaHeight;

        requestFocus();

        //if (x < left)
            //return false;
        //if (y < top)
            //return false;

        int j = y - top;
        j /= kBlockHeight;

        int i = x - left;
        i /= kBlockWidth;

        if (i >= 0 && i < kBlocksWide && j >= 0 && j < kBlocksHigh) {
            if (layout[j][i] != -1) {
                if(gCurX == i && gCurY == j)
                {
                 ChangeDirection();
                 if(NoCurrQuestion()) ChangeDirection();
                 else {
                  gChangedActiveAreaFlag = true;
                  repaint();
                 }
                 return;
                }
                gCurX = i;
                gCurY = j;
                if (WithinActiveBlock(i, j)) {
                    gUpdateActiveAreaFlag = true;
                    repaint();
                }
                else {
                    SetActiveBlock(i, j, gDirection);
                    if(NoCurrQuestion()) ChangeDirection();
                    gChangedActiveAreaFlag = true;
                    repaint();
                }
                //return true;
            }
        }
        //return true;
    }

    private boolean NoCurrQuestion()
    {
        return
            ((gDirection == kAcross && gQuestionsAcross[layout[gBlockMinY][gBlockMinX]] == null) ||
             (gDirection == kDown   && gQuestionsDown[layout[gBlockMinY][gBlockMinX]] == null));
    }

    /*----------------------------------------------*/

    public void mouseReleased(MouseEvent e) {
        requestFocus();
        //return true;
    }

    /*----------------------------------------------*/

    public void mouseExited(MouseEvent e) {
        //return true;
    }

    /*----------------------------------------------*/

    public void mouseEntered(MouseEvent e) {
        requestFocus();
        //return true;
    }

    /*----------------------------------------------*/

    public void keyTyped(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {}


    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if ((key >= 'A' && key <= 'Z') || (key >= 'a' && key <= 'z')) {   // || key == '-') {
            char[] charArray = new char[1];

            charArray[0] = (char)key;

            gGuesses[gCurX][gCurY] = new String(charArray);
            gGuesses[gCurX][gCurY] = gGuesses[gCurX][gCurY].toUpperCase();


            if (gDirection == kAcross) {
                if (gCurX < kBlocksWide - 1 && layout[gCurY][gCurX+1] != -1)
                    gCurX++;
            }
            else {
                if (gCurY < kBlocksHigh - 1 && layout[gCurY + 1][gCurX] != -1)
                    gCurY++;
            }
            gUpdateActiveAreaFlag = true;
            repaint();
        } else {
            switch ((char)key) {
                /*  case ' ':
                    ChangeDirection();
                    gChangedActiveAreaFlag = true;
                    repaint();
                    break;*/
            case KeyEvent.VK_DOWN:
                if (gDirection == kDown && gCurY < kBlocksHigh - 1 &&
                    layout[gCurY+1][gCurX] != -1) {
                    gCurY++;
                }
                gUpdateActiveAreaFlag = true;
                repaint();
                break;
            case KeyEvent.VK_UP:
                if (gDirection == kDown && gCurY >= 1 &&
                    layout[gCurY - 1][gCurX] != -1) {
                    gCurY--;
                }
                gUpdateActiveAreaFlag = true;
                repaint();
                break;
            case KeyEvent.VK_LEFT:
                if (gDirection == kAcross && gCurX >= 1 &&
                    layout[gCurY][gCurX - 1] != -1) {
                    gCurX--;
                }
                gUpdateActiveAreaFlag = true;
                repaint();
                break;
            case KeyEvent.VK_RIGHT:
                if (gDirection == kAcross && gCurX < kBlocksWide - 1 &&
                    layout[gCurY][gCurX+1] != -1) {
                    gCurX++;
                }
                gUpdateActiveAreaFlag = true;
                repaint();
                break;
            case KeyEvent.VK_BACK_SPACE:
                if ( ! gGuesses[gCurX][gCurY].equals("")) {
                    gGuesses[gCurX][gCurY] = "";
                    gUpdateActiveAreaFlag = true;
                    repaint();
                } else {
                    if (gDirection == kAcross) {
                        if (gCurX != 0 && layout[gCurY][gCurX-1] != -1) {
                            gCurX--;
                            gGuesses[gCurX][gCurY] = "";
                            gUpdateActiveAreaFlag = true;
                            repaint();
                        }
                    } else {
                        if (gCurY != 0 && layout[gCurY - 1][gCurX] != -1) {
                            gCurY--;
                            gGuesses[gCurX][gCurY] = "";
                            gUpdateActiveAreaFlag = true;
                            repaint();
                        }
                    }
                }
                break;
            default:
                if (Dbg.KEVIN)
                    Dbg.print("Crossword key pressed: " + key);
                beep();
                break;
            }
        }
    }


    /*----------------------------------------------*/

    void ChangeDirection() {
        if (gDirection == kDown) {
            gDirection = kAcross;
        }
        else
            gDirection = kDown;
        SetActiveBlock(gCurX, gCurY, gDirection);
    }

    public static String getDesc()
    {
        return (SC_CROSSWORD_DESC);
    }

    /*  public static void main (String[] args)
        {
        JFrame window = new JFrame("crossword window");
        Container container = window.getContentPane();
        container.add(new CrosswordPanel());
        window.pack();
        window.setVisible(true);
        window.addWindowListener(
        new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
        System.exit(0);
        }
        }
        );
        }
    */

}


