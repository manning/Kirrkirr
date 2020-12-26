package Kirrkirr.ui.data;

import java.util.*;

import Kirrkirr.Kirrkirr;
import Kirrkirr.util.Helper;


public class CrosswordGenerator {

    private static final int VERTICAL   = 1;
    //accessed from CrosswordPanel
    public static final int HORIZONTAL = 0;
    private static final int MAXWORDS   = 20;
    private static final int MAX_BOARD_WORDS = MAXWORDS-1;
    public static final int MATSIZE_X    = 20;
    public static final int MATSIZE_Y    = 9;//also change other constants

    private static final int LONGEST    = 0;
    private static final int FITABLE    = 1;
    private static final int LONGFIT    = 2;

    private static final int STRAT_EDGES_AND_FIT    = 0;
    private static final int STRAT_EDGES_ONLY       = 1;
    private static final int STRAT_EDGES_AND_LENGTH = 2;
    private static final int STRAT_EDGES_AND_COLLS  = 3;
    private static final int STRAT_MIDS_AND_LENGTH  = 4;
    private static final int STRAT_MIDS             = 5;
    private static final int STRAT_GRAVITY          = 6;
    private static final int NUM_STRATS             = 7;

    private static final char NULL_CHAR     = ' ';

    private CrosswordInfo[]   wordArray;
    private char[][]       charMatrix;
    private int[][]        dirMatrix;
    private int[][]        layoutMatrix;
    private String[]     downQuestions;
    private String[]     acrossQuestions;
    private int[]        letterDistrib;
    private CrosswordInfo[]   usedStack;
    //private String     stratString[];
    //private String     firstString[];

    private int        wordsPlaced = 0;
    private int        wordsCrossed = 0;
    private int        usedStackPointer = 0;
    private float      averageFit = 0;
    private int        averageLen = 0;
    private int        contCeil = 49;
    private int        gravityX;
    private int        gravityY;

    private int[][][]  stratMatrix1; // score/direction first
    private int[][][]  stratMatrix2; // crossings/direction first

    private Kirrkirr parent;
    private int difficulty;

    public CrosswordGenerator(Kirrkirr parent, int difficulty) {
        this.difficulty = difficulty;
        this.parent = parent;
        wordArray = new CrosswordInfo[MAXWORDS];
        charMatrix = new char[MATSIZE_X][MATSIZE_Y];
        dirMatrix = new int[MATSIZE_X][MATSIZE_Y];
        letterDistrib = new int[26];
        usedStack = new CrosswordInfo[MAXWORDS];
        stratMatrix1 = new int[NUM_STRATS][2][3];
        stratMatrix2 = new int[NUM_STRATS][2][3];
        //stratString = new String[NUM_STRATS];
        //firstString = new String[3];

        /*      stratString[0] = "Edges and Fit";
                stratString[1] = "Edges Only";
                stratString[2] = "Edges and Length";
                stratString[3] = "Edges and Collisions";
                stratString[4] = "Middles and Length";
                stratString[5] = "Middles";
                stratString[6] = "Gravity";

                firstString[0] = "Longest First";
                firstString[1] = "Fitable First";
                firstString[2] = "Longest Fitable First"; */
    }

    private void processBestAnswer() {
        int numWords = CleanWordArray();
        SortWordArray(numWords);
        GenerateHeadNums(numWords);
        CreateLayout(numWords);
        CreateQuestionArrays(numWords);
    }

    private int CleanWordArray() {
        Vector<CrosswordInfo> usedWords = new Vector<CrosswordInfo>();
        for (int i = 0; i < MAXWORDS; i++) {
            if(wordArray[i] == null) break;
            if(wordArray[i].used == 1) usedWords.addElement(wordArray[i]);
        }
        CrosswordInfo[] newWordArray = new CrosswordInfo[usedWords.size()];
        usedWords.copyInto(newWordArray);
        wordArray = newWordArray;
        return(usedWords.size());
    }

    private void CreateQuestionArrays(int numWords) {
        downQuestions = new String[numWords+1];
        acrossQuestions = new String[numWords+1];
        /*  for(int i = 0; i<=numWords; i++)
            {
            downQuestions[i] = SC_NO_QUESTION;//"NO QUESTION HERE";
            acrossQuestions[i] = SC_NO_QUESTION;//"NO QUESTION HERE";
            }*/
        for(int j = 0; j<numWords; j++)
        {
            CrosswordInfo currWordInfo = wordArray[j];
            String currQuestion = currWordInfo.gloss;
            int currHeadnum = currWordInfo.headnum;
            int currDirection = currWordInfo.direction;
            if(currDirection == HORIZONTAL) acrossQuestions[currHeadnum] = currQuestion;
            else downQuestions[currHeadnum] = currQuestion;
        }
    }

    public char[][] getProcessedAnswers()
    {
        return charMatrix;
    }

    public CrosswordInfo getBeginningWord()
    {
        return wordArray[0];
    }

    public int[][] getProcessedLayout()
    {
        return layoutMatrix;
    }

    public String[] getProcessedDownQuestions()
    {
        return downQuestions;
    }

    public String[] getProcessedAcrossQuestions()
    {
        return acrossQuestions;
    }

    private void SortWordArray(int numWords)
    {
        Comparator c = new WordInfoComparator();
        Arrays.sort(wordArray, 0, numWords-1, c);
    }

    private void GenerateHeadNums(int numWords)
    {
        int currNum = 1;
        WordInfoComparator wordComp = new WordInfoComparator();

        for(int i=0; i<numWords; i++)
        {
            if(i == 0) wordArray[i].headnum = 1;
            else if(wordComp.compare(wordArray[i], wordArray[i-1]) != 0) currNum++;
            wordArray[i].headnum = currNum;
        }
    }

    private void CreateLayout(int numWords)
    {
        InitLayout();
        for(int i = 0; i < numWords; i++)
        {
            PlaceWordInLayout(i);
        }
    }

    private void PlaceWordInLayout(int i)
    {
        int myX = wordArray[i].x;
        int myY = wordArray[i].y;
        int myDir = wordArray[i].direction;
        int myNum = wordArray[i].headnum;
        int myLen = wordArray[i].len;

        if(layoutMatrix[myY][myX] <= 0) layoutMatrix[myY][myX] = myNum;
        if(myDir == HORIZONTAL)
        {
            for(int j = 1; j < myLen; j++)
            {
                if(layoutMatrix[myY][myX+j] == -1) layoutMatrix[myY][myX+j] = 0;
            }
        }
        else
            for(int j = 1; j < myLen; j++)
            {
                if(layoutMatrix[myY+j][myX] == -1) layoutMatrix[myY+j][myX] = 0;
            }
    }

    private void InitLayout()
    {
        layoutMatrix = new int[MATSIZE_Y][MATSIZE_X];
        for (int y = 0; y < MATSIZE_Y; y++) {
            for (int x = 0; x < MATSIZE_X; x++) {
                layoutMatrix[y][x] = -1;
            }
        }
    }


    private int readStringArray(String[] words)
    {
        int i;
        for(i=0; i<words.length;i++)//wordsThatFit.size(); i++)
        {
            String uniqueKey = words[i]; //(String) wordsThatFit.elementAt(i);
            String word = Helper.getWord(uniqueKey);
            int len = word.length();
            wordArray[i] = new CrosswordInfo();
            wordArray[i].len = len;
            wordArray[i].word = new char[len];
            wordArray[i].word = word.toCharArray();
            wordArray[i].used = 0;
            wordArray[i].gloss = parent.cache.getFirstGloss(uniqueKey);
        }
        return i;
    }

/*
public int readWordFile(String filename)
{
    int ch = 0, len, y = 0, longest = 0;
    RandomAccessFile file_p;

    File infile = new File(filename);

    if (infile.isFile() && infile.canRead()) {
        try {
            file_p = new RandomAccessFile(infile,"r");
            StringBuffer word = new StringBuffer();

            while (ch != -1) {
                len = 0;
                word.setLength(0);
                while ((ch = file_p.read()) != -1 && ch != '\n') {
                      if (ch != '\r') {
                          word.append((char)ch);
                          len++;
                      }
                }
                if (ch != -1) {
                        String wordString = word.toString();
                        if (len > longest) longest = len;
                        wordArray[y] = new CrosswordInfo();
                        wordArray[y].len  = len;
                        wordArray[y].word = new char[wordString.length()];
                        wordArray[y].word = wordString.toCharArray();
                        wordArray[y].used = 0;
                        y++;
                }
            }
            file_p.close();
        } catch (IOException e) {
                System.out.println("IOException opening" + filename);
                return(-1);
        }
    } else {
        System.out.println("Can't read file " + filename);
    }
    return (y);
}
*/

/*private void printWordArray()
{
    int x = 0;
    while (wordArray[x] != null) {
        System.out.println("Word " + x + " is " + wordArray[x].word +
                           " : Score = " + wordArray[x].score +
                           " : Fitability = " + wordArray[x].fitability);
        x++;
    }
}*/


    private void reInitWordArray()
    {
        int x = 0;
        while (wordArray[x] != null) {
            wordArray[x].used = 0;
            wordArray[x].x = 0;
            wordArray[x].y = 0;
            x++;
        }
    }


    private void initMatrix()
    {
        wordsPlaced = 0;
        wordsCrossed = 0;
        for (int y = 0; y < MATSIZE_Y; y++) {
            for (int x = 0; x < MATSIZE_X; x++) {
                charMatrix[x][y] = NULL_CHAR;
                dirMatrix[x][y] = -1;

            }
        }
    }


    private void initStats()
    {
        int x;
        for (x = 0; x < 26; x++) {
            letterDistrib[x] = 0;
        }
        for (x = 0; x < NUM_STRATS; x++) {
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 3; z++) {
                    stratMatrix1[x][y][z] = 0;
                    stratMatrix2[x][y][z] = 0;
                }
            }
        }
    }


    private void printMatrix() {
        //char temp[] = new char[MATSIZE];
        StringBuffer word = new StringBuffer();


        for (int y = 0; y < MATSIZE_Y; y++) {
            word.setLength(0);
            for (int x = 0; x < MATSIZE_X; x++) {
                word.insert(x, charMatrix[x][y]);
            }
            //System.out.println(new String(word.toString()));
        }

   /* for(int i = 0; i < MATSIZE; i++)
    {
      for(int j = 0; j < MATSIZE; j++)
      {
        if(Dbg.KEVIN) {
          System.out.print(layoutMatrix[i][j]);
          System.out.print(" ");
          }
      }
      if(Dbg.KEVIN) System.out.print("\n");
    }
    //if(Dbg.KEVIN)
    //{
      for(int i = 0; i < wordsPlaced; i++)
      {
        if(wordArray[i].direction == HORIZONTAL) System.out.println(wordArray[i].headnum + " across");
        else System.out.println(wordArray[i].headnum + " down");
        System.out.println(String.valueOf(wordArray[i].word) + " x " + wordArray[i].x + " y " + wordArray[i].y);
      }
    //}*/
    }



    /** CDM: This is horribly written code!  (Bad use of local variables,
     *  and character types hardcoded).
     */
    private void gatherStats()
    {
        int x = 0, y, score, c;

        while (wordArray[x] != null) {
            int len = wordArray[x].len;
            for (y = 0; y < len; y++) {
                c = wordArray[x].word[y];
                if (c > 96 && c < 123) {
                    letterDistrib[c - 97]++;
                }
            }
            x++;
        }
        x = 0;
        while (wordArray[x] != null) {
            int len = wordArray[x].len;
            for (y = 0; y < len; y++) {
                if (wordArray[x].word[y] > 96 && wordArray[x].word[y] < 123) {
                    score = letterDistrib[wordArray[x].word[y] - 97];
                    wordArray[x].score = wordArray[x].score + score;
                    wordArray[x].fitability = (float) wordArray[x].score / len;
                }
            }
            averageFit = averageFit + wordArray[x].fitability;
            averageLen = averageLen + len;
            x++;
        }
        averageFit = averageFit / x;
        averageLen = averageLen / x;

        if (averageLen >= 10) {
            contCeil = 18;
        }
        if (averageLen >= 13) {
            contCeil = 9;
        }
        if (averageLen >= 15) {
            contCeil = 0;
        }
        if (averageLen <= 5) {
            contCeil = 70;
        }
    }


/*private void printStats()
{
    System.out.println("LETTER OCCURRENCES:");
    for (int x = 0; x < 26; x++) {
        System.out.println(x + 97 + " - " + letterDistrib[x]);
    }
    System.out.println("Average Fit: " + averageFit);
}*/


    void calcBestAnswer()
    {
        int bestStrat = 0, bestDir = 0, bestfirst = 0;
        int bestStratNum = 0, bestStratNum2 = 0;
        int dir, strat, badStrat;

        // for (strat = 0; strat < 1; strat++) {
        for (strat = 0; strat < NUM_STRATS; strat++) {
            badStrat = 0;
            for (dir = 0; dir < 2 && badStrat == 0; dir++) {
                for (int x = 0; x < 3 && badStrat == 0; x++) {
                    calcAnswer(strat, dir, x);
/*              if (debugFlag)
                    System.out.println(wordsPlaced + " words: " +
                                       stratString[strat] + " " +
                                       firstString[x] + "(" + wordsCrossed +
                                       " crossings)");*/
                    if (wordsPlaced < contCeil) badStrat = 1;
                    if (stratMatrix1[strat][dir][x] > bestStratNum) {
                        bestStratNum = stratMatrix1[strat][dir][x];
                        bestStratNum2 = stratMatrix2[strat][dir][x];
                        bestStrat = strat;
                        bestDir = dir;
                        bestfirst = x;
                    } else if (stratMatrix1[strat][dir][x] == bestStratNum) {
                        if (stratMatrix2[strat][dir][x] > bestStratNum2) {
                            bestStratNum = stratMatrix2[strat][dir][x];
                            bestStrat = strat;
                            bestDir = dir;
                            bestfirst = x;
                        }
                    }
                }
            }
        }
        // Do best one more time!!
        calcAnswer(bestStrat, bestDir, bestfirst);
  /*  if (debugFlag) {
        System.out.println("=================================================");
        System.out.println(wordsPlaced + " words: " + stratString[bestStrat] +
                           " " + firstString[bestfirst] + "(" + wordsCrossed +
                           " crossings)");
    }*/
    }


    void calcAnswer(int strategy, int initDir, int first)
    {
        CrosswordInfo wip;

        initMatrix();
        reInitWordArray();
        wip = getFirstWord(first);
        // Put first good word into the center of the matrix
        // We could do alot more here!!
        wip.direction = initDir;
        if (initDir == HORIZONTAL) {
            wip.y = (MATSIZE_Y-1)/2;
            wip.x = (MATSIZE_X-wip.len)/2;
//        if(Dbg.KEVIN) Dbg.print(wip.x+" "+wip.y);
        } else {
            wip.x = (MATSIZE_X-1)/2;
            wip.y = (MATSIZE_Y-wip.len)/2;
//        if(Dbg.KEVIN) Dbg.print(wip.x+" "+wip.y);
        }
        gravityX = (MATSIZE_X-1)/2;
        gravityY = (MATSIZE_Y-1)/2; // Set gravity
        wip.used = 1;
        insertWord(wip);
        placeNextWord1(wip, strategy);
        stratMatrix1[strategy][initDir][first] = wordsPlaced;
        stratMatrix2[strategy][initDir][first] = wordsCrossed;
    }


    int placeNextWord1(CrosswordInfo lastWord, int strategy)
    {
        push(lastWord);
        CrosswordInfo wip = calculateAndPlace(lastWord, strategy);
        if (wip != null) {
            placeNextWord1(wip, strategy);
        } else {
            pop();            // Flush one
            wip = pop();
            if (wip != null) {
                placeNextWord1(wip, strategy);
            }
        }
        return (0);
    }


    int placeNextWord2(CrosswordInfo lastWord, int strategy)
    {
        CrosswordInfo wip = calculateAndPlace(lastWord, strategy);
        if (wip != null) {
            push(wip);
            placeNextWord2(lastWord, strategy);
        } else {
            wip = pop();
            if (wip != null) {
                placeNextWord2(wip, strategy);
            }
        }
        return (0);
    }


    CrosswordInfo calculateAndPlace(CrosswordInfo lastWord, int strategy) {
        int x = 0, bestIndex = -1;
        float score, bestScore = 0;

        while (wordArray[x] != null) {
            if (wordArray[x].used != 1) {
                score = bestFit1(lastWord, wordArray[x], strategy);
                if (score > bestScore) {
                    bestScore = score;
                    bestIndex = x;
                }
            }
            x++;
        }
        if (bestIndex == -1) {
            return null;
        } else {
            wordArray[bestIndex].used = 1;
            insertWord(wordArray[bestIndex]);
            wordsCrossed = wordsCrossed + wordArray[bestIndex].crossings;
            return wordArray[bestIndex];
        }
    }


    void insertWord(CrosswordInfo word_p) {
        if (word_p.direction == HORIZONTAL) {
            for (int x = 0; x < word_p.len; x++) {
//            if(Dbg.KEVIN) Dbg.print("x "+x+" "+word_p.x);
                dirMatrix[x + word_p.x][word_p.y] = HORIZONTAL;
                charMatrix[x + word_p.x][word_p.y] = word_p.word[x];
            }
        } else {
            for (int y = 0; y < word_p.len; y++) {
//            if(Dbg.KEVIN) Dbg.print("y "+y+" "+word_p.y);
                dirMatrix[word_p.x][y + word_p.y] = VERTICAL;
                charMatrix[word_p.x][y + word_p.y] = word_p.word[y];
            }
        }
        wordsPlaced++;
    }


    CrosswordInfo getFirstWord(int strat) {
        switch (strat) {
            case FITABLE: return (getNextMostFitable());
            case LONGEST: return (getNextLongest());
            case LONGFIT: return (getLongestFitable());
            default:
                return (getNextMostFitable());
        }
    }


    CrosswordInfo getNextMostFitable() {
        float fit = 0;
        int x = 0, address = 0;
        while (wordArray[x] != null) {
            if (wordArray[x].used != 1 && wordArray[x].fitability > fit) {
                fit = wordArray[x].fitability;
                address = x;
            }
            x++;
        }
        return wordArray[address];
    }


    CrosswordInfo getNextLongest() {
        int len = 0, x = 0, address = 0;

        while (wordArray[x] != null) {
            if (wordArray[x].used != 1 && wordArray[x].len > len) {
                len = wordArray[x].len;
                address = x;
            }
            x++;
        }
        return wordArray[address];
    }


    CrosswordInfo getLongestFitable()
    {
        int len = 0, x = 0, address = 0;
        float fit = 0;

        while (wordArray[x] != null) {
            if (wordArray[x].used != 1 && wordArray[x].fitability > fit &&
                    wordArray[x].fitability > averageFit &&
                    wordArray[x].len > len) {
                fit = wordArray[x].fitability;
                len = wordArray[x].len;
                address = x;
            }
            x++;
        }
        return wordArray[address];
    }


    void push(CrosswordInfo word)
    {
        usedStack[usedStackPointer] = word;
        usedStackPointer++;
    }


    CrosswordInfo pop()
    {
        if (usedStackPointer == 0) return null;
        usedStackPointer--;
        return usedStack[usedStackPointer];
    }


    float bestFit1(CrosswordInfo lastWord, CrosswordInfo newWord, int strategy)
    {
        int fit, k;
        float score = -1, bestScore = -1;

        // Find an intersection
        for (int j = 0; j < lastWord.len; j++) {
            for (k = 0; k < newWord.len; k++) {
                if (lastWord.word[j] == newWord.word[k]) {
                    // Check fit
                    // fit will be number of "good collisions"
                    // System.out.println("bestFit with words: " + newWord.word +
                    //                    " " + lastWord.word);
                    fit = checkFit(lastWord, newWord, j, k);
                    if (fit != -1) {
                        // Determine score
                        score = calcScore(lastWord, newWord, strategy, j, k, fit);
                        if (score > bestScore) {
                            bestScore = score;
                            // Determine coords
                            if (lastWord.direction == HORIZONTAL) {
                                newWord.x = lastWord.x + j;
                                //if(Dbg.KEVIN) Dbg.print("newword.x = " + newWord.x);
                                newWord.y = lastWord.y - k;
                                //if(Dbg.KEVIN) Dbg.print("newword.y = " + newWord.y);
                                newWord.direction = VERTICAL;
                            } else {
                                newWord.x = lastWord.x - k;
                                newWord.y = lastWord.y + j;
                                newWord.direction = HORIZONTAL;
                            }
                            newWord.crossings = fit;
                        }
                    }
                }
            }
        }
        return (score);
    }


    float calcScore(CrosswordInfo lastWord, CrosswordInfo newWord, int strat, int j,
                    int k, int fit)
    {
        int mid1, mid2;
        float edgeScore1, edgeScore2, multiplier1, multiplier2;
        float midScore1 = 0, midScore2 = 0;

        // Determine score
        mid1 = (newWord.len - 1) / 2;
        mid2 = (lastWord.len - 1) / 2;
        if (mid1 == 0) mid1 = 1;
        if (mid2 == 0) mid2 = 1;
        multiplier1 = 10 / mid1;
        multiplier2 = 10 / mid2;
        if (k == mid1) {
            edgeScore1 = 1;
        } else if (k < mid1) {
            edgeScore1 = (mid1 - k) * multiplier1;
            midScore1 = k * multiplier1;
        } else {
            edgeScore1 = (k - mid1) * multiplier1;
            midScore1 = (newWord.len - k) * multiplier1;
        }
        if (j == mid2) {
            edgeScore2 = 1;
        } else if (j < mid2) {
            edgeScore2 = (mid2 - j) * multiplier2;
            midScore2 = j * multiplier2;
        } else {
            edgeScore2 = (j - mid2) * multiplier2;
            midScore2 = (lastWord.len - j) * multiplier1;
        }
        switch (strat) {
            case STRAT_EDGES_AND_FIT:
                return (edgeScore1 + edgeScore2 + newWord.fitability);
            case STRAT_EDGES_ONLY:
                return (edgeScore1 + edgeScore2);
            case STRAT_EDGES_AND_LENGTH:
                return (edgeScore1 + edgeScore2 + newWord.len);
            case STRAT_EDGES_AND_COLLS:
                return (edgeScore1 + edgeScore2 + ((fit - 1) * 10));
            case STRAT_MIDS_AND_LENGTH:
                return (midScore1 + midScore2 + newWord.len);
            case STRAT_MIDS:
                return (midScore1 + midScore2);
            case STRAT_GRAVITY:
                return(gravityScore(lastWord, newWord, j, k));
            default:
                return(0);
        }
    }


    float gravityScore(CrosswordInfo lastWord, CrosswordInfo newWord, int j, int k)
    {
        int XCord, YCord;
        float grav;

        // Where j is the intersect on the lastWord, k is the newWord
        // Determine coords
        if (lastWord.direction == HORIZONTAL) {     // VERTICAL
            YCord = lastWord.y - k;
            if (YCord <= gravityY) {
                grav = newWord.len - k;
            } else {
                grav = k;
            }
        } else {                    // HORIZONTAL
            XCord = lastWord.x - k;
            if (XCord <= gravityX) {
                grav = newWord.len - k;
            } else {
                grav = k;
            }
        }
        return (grav);
    }


    int checkFit(CrosswordInfo lastWord, CrosswordInfo newWord, int j, int k)
    {
        // Where j is the intersect on the lastWord, k is the newWord
        // This is a bit oversimplified as we must check for other
        // words that may be in the way!!
        // Boundary Check
        if (boundaryCheck(lastWord, newWord, k) == 0) return (-1);
        // Collision Check
        return (collisionCheck(lastWord, newWord, j, k));
    }


    int boundaryCheck(CrosswordInfo lastWord, CrosswordInfo newWord, int k)
    {
        // System.out.println("boundaryCheck with words: " + newWord.word +
        //                   " " + lastWord.word);
        // Where k is the intersect on the newWord
        if (lastWord.direction == HORIZONTAL) {
            if (lastWord.y - k < 0) return 0;       // Check above
            // Check below
            if (lastWord.y + (newWord.len - k - 1) > MATSIZE_Y-1) return 0;
        } else {
            if (lastWord.x - k < 0) return 0;       // Check left
            // Check right
            if (lastWord.x + (newWord.len - k - 1) > MATSIZE_X-1) return 0;
        }
        return (1);
    }


    int collisionCheck(CrosswordInfo lastWord, CrosswordInfo newWord, int j, int k)
    {
        int XCord, YCord, x, y, ok_flag, goodColls = 1, limit, offset;

        // Where j is the intersect on the lastWord, k is the newWord
        // Determine coords
        if (lastWord.direction == HORIZONTAL) {     // VERTICAL
            XCord = lastWord.x + j;
            YCord = lastWord.y - k;
            // DIAGONALS ARE OK!! DON'T CHECK NEXTA'S ON -1 AND +!
            limit = YCord + newWord.len + 1;
            for (y = YCord - 1; y < limit && y < MATSIZE_Y; y++) {
                if (y < 0) y = 0;
                ok_flag = 0;
                if (charMatrix[XCord][y] != NULL_CHAR && y != lastWord.y) {
                    offset = y - YCord;
                    if (offset > -1 && offset < newWord.len) {
                        if (charMatrix[XCord][y] == newWord.word[offset]) {
                            goodColls++;
                            ok_flag = 1;
                        } else {
                            //System.out.println("Coll: " +newWord.word+XCord+y);
                            return -1;
                        }
                    } else if (offset == -1 || offset == newWord.len) {
                        if (charMatrix[XCord][y] != NULL_CHAR) {
                            return -1;
                        }
                    }
                }
                if (y == YCord - 1 || y == limit - 1) ok_flag = 1;
                if (XCord < MATSIZE_X-1) {
                    if (charMatrix[XCord + 1][y] != NULL_CHAR && y != lastWord.y && ok_flag == 0) {
                        //System.out.println("Nexta: "+newWord.word+XCord+y);
                        return -1;
                    }
                }
                if (XCord > 0) {
                    if (charMatrix[XCord - 1][y] != NULL_CHAR && y != lastWord.y && ok_flag == 0) {
                        //System.out.println("Nexta: "+newWord.word+XCord+y);
                        return -1;
                    }
                }
                if (dirMatrix[XCord][y] == VERTICAL) {
                    //System.out.println("Overlap: "+newWord.word+XCord+y);
                    return -1;
                }
            }
        } else {                    // HORIZONTAL
            XCord = lastWord.x - k;
            YCord = lastWord.y + j;
            limit = XCord + newWord.len + 1;
            for (x = XCord - 1; x < limit && x < MATSIZE_X; x++) {
                if (x < 0) x = 0;
                ok_flag = 0;
                if (charMatrix[x][YCord] != NULL_CHAR && x != lastWord.x) {
                    offset = x - XCord;
                    if (offset > -1 && offset < newWord.len) {
                        if (charMatrix[x][YCord] == newWord.word[offset]) {
                            goodColls++;
                            ok_flag = 1;
                        } else {
                            // System.out.println("Coll: " +newWord.word+x+YCord);
                            return -1;
                        }
                    } else if (offset == -1 || offset == newWord.len) {
                        if (charMatrix[x][YCord] != NULL_CHAR) {
                            return -1;
                        }
                    }
                }
                if (x == XCord - 1 || x == limit - 1) ok_flag = 1;
                if (YCord < MATSIZE_Y-1) {
                    if (charMatrix[x][YCord + 1] != NULL_CHAR && x != lastWord.x && ok_flag == 0) {
                        // System.out.println("Nexta: "+newWord.word+x+YCord);
                        return -1;
                    }
                }
                if (YCord > 0) {
                    if (charMatrix[x][YCord - 1] != NULL_CHAR && x != lastWord.x && ok_flag == 0) {
                        // System.out.println("Nexta: " + newWord.word + x + YCord);
                        return -1;
                    }
                }
                if (dirMatrix[x][YCord] == HORIZONTAL) {
                    //System.out.println("Overlap: " + newWord.word +  x + YCord);
                    return -1;
                }
            }
        }
        return (goodColls);
    }

    private boolean ContainsBadChars(String str)
    {
        for(int i = 0; i < str.length(); i++)
        {
            char key = Character.toLowerCase(str.charAt(i));
            if(key < 'a' || key > 'z') return true;
        }
        return false;
    }

    public void generateBoard()
    {
        String[] words = new String[MAX_BOARD_WORDS];
        for(int i = 0; i < MAX_BOARD_WORDS; i++) {
            do {
                words[i] = GameWordGenerator.GetGameHeadword(difficulty, parent);
            } while ((words[i].trim().length() > MATSIZE_X &&
                    words[i].trim().length() > MATSIZE_Y) || ContainsBadChars(words[i]));
            // if(Dbg.KEVIN) Dbg.print("random word at words["+i+"]: "+words[i]);
        }
        readStringArray(words);
        initStats();
        gatherStats();
        initMatrix();
        calcBestAnswer();
        processBestAnswer();
        printMatrix();
    }

    static class WordInfoComparator implements Comparator<CrosswordInfo> {

        @Override
        public int compare(CrosswordInfo a, CrosswordInfo b) {
            int y1 = a.y;
            int y2 = b.y;
            if(y1>y2) return 1;
            if(y1<y2) return -1;

            int x1 = a.x;
            int x2 = b.x;
            if(x1>x2) return 1;
            if(x1<x2) return -1;

            return 0;
        }

    }



/* public static void main(String args[])
        {
            int wordsRead, filearg = 1;
            String[] words = new String[newCroz.MAX_BOARD_WORDS];
            for(int i = 0; i < newCroz.MAX_BOARD_WORDS; i++)
            {
              words[i] = newCroz.randomHeadWord();
            }
            wordsRead = newCroz.readStringArray(words);
            newCroz.initStats();
            newCroz.gatherStats();
            newCroz.initMatrix();
            //newCroz.printStats();
            //newCroz.printWordArray();
            newCroz.calcBestAnswer();
           // newCroz.populateMatrix();
            newCroz.processBestAnswer();
            newCroz.printMatrix();
            if (newCroz.debugFlag)
                System.out.println("Placed " + newCroz.wordsPlaced+" of " + wordsRead +
                                   " words (" + newCroz.wordsCrossed + " crossings)");
    }*/

}







