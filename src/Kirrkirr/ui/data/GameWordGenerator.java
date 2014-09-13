package Kirrkirr.ui.data;

import Kirrkirr.Kirrkirr;
import Kirrkirr.util.Dbg;

public class GameWordGenerator {

  public static final int EASY = 0;
  public static final int MEDIUM = 1;
  public static final int HARD = 2;

  private static final int MED_FREQ_THRESHOLD = 2;
  private static final int EASY_FREQ_THRESHOLD = 5;

  private static final int MAX_TRIES = 20;

    // Only static methods
    private GameWordGenerator() {
    }

    /** Harder levels will be more inclusive of harder words.
   *  I.e., easy words will leave
   *  out rare words, but hard level will include all words (determined by
   *  frequency).
   */
  public static String GetGameHeadword(int difficulty, Kirrkirr parent)
  {
    // Blocking of registers doesn't happen at this level, the HARD level
    // should still include register words.
    // we will filter out register words when we check the difficulty level.
    if (difficulty == HARD) return randomHeadWord(parent);
    else { //filtering needs to be done
      int numTries = 0;
      int threshold;
      if (difficulty == MEDIUM) threshold = MED_FREQ_THRESHOLD;
      else threshold = EASY_FREQ_THRESHOLD;

      String randWord;
      do {
        randWord = randomHeadWord(parent);
        numTries++;
        if (Dbg.KEVIN) {
            Dbg.print(randWord + " R F " + parent.cache.hasRegister(randWord) +
                      " " + parent.cache.getFreq(randWord));
        }
      } while (numTries < MAX_TRIES && (parent.cache.hasRegister(randWord) ||
               parent.cache.getFreq(randWord) < threshold));
      return randWord;
    }
  }


  /* private static String randomHeadWord(Kirrkirr parent)
  {
      String pword=null;
      while (pword==null || parent.cache.hasRegister(pword)){
          if (parent.headWordsSize()>0){
              pword=parent.scrollPanel.headWordAt(randomInt(parent.headWordsSize()-1));
          }else{
              if(Dbg.ERROR) Dbg.print("RandomHeadWord failed: headwords list too small");
              return null;
          }
      }
      return pword;

  }*/

    private static String randomHeadWord(Kirrkirr parent) {
        int listSize = parent.headwordsListSize();
        if (listSize > 0) {
            return(parent.scrollPanel.headwordAt(randomInt(listSize)));
        }
        if(Dbg.ERROR) Dbg.print("RandomHeadWord: list size is zero");
        return null;
    }


    /** Returns an int from 0 to (max - 1) inclusive.  Note in particular
     *  that in the standard idioms of usage (where the passed in
     *  parameter is a list length or whatever, then you do <i>not</i>
     *  subtract one from this length before passing it to this routine.
     */
    public static int randomInt(int max) {
        double myrand = Math.random() * max;
        return (int) myrand;
    }

}

