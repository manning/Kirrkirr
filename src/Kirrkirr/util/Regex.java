package Kirrkirr.util;

/**
 * The <code>Regex</code> interface is a simple interface that can make
 * other regular expression classes a bit easier to use.
 * (Historically, it is based on the original patbin interface, and has 
 * since been used with the gnu regexp package and Jakarta-ORO.
 *
 * An interface cannot specify constructors, but the file notes the 
 * constructors that one would normally expect to be available in an 
 * implementing class.
 *
 * @version     1.1, 11/05/2000
 * @author      Kevin Jansz
 * @author      Christopher Manning
 *
 * @see Kirrkirr
 */
public interface Regex {

    /* Sample constructors, and static factory methods

    private Regex() {
        super();
    }

    public Regex(String patternStr) {
    }

    public Regex (String patternStr, int options) {
    }

    public Regex (String patternStr, String substStr) {
    }

    public Regex (String patternStr, String substStr, int regexOptions) {
    }
    
    public static Regex newRegex(String pattern);

    public static Regex newICaseRegex(String pattern);

    public static Regex newSubRegex(String pattern, String substitute);
    */

    /** Returns true iff there is a match for the pattern stored in
     *  the Regex somewhere within the input string. 
     */
    public boolean hasMatch(String input);

    /** Returns as a String the part of the input string that matches
     *  the pattern stored in the Regex. 
     *  Returns null if there is no match.
     */
    public String getStringMatch(String input);

    public void setSub(String substitute);

    /** Replace all instances of the pattern with the substitute
     *  in the string input
     */
    public String doReplace(String input);

    /** This routine matches the input with the pattern in the object,
     *  and then returns the n'th parenthesized back reference.  Since
     *  this routine matches the regex each time, it's expensive to call
     *  for multiple matches.  [cdm: should fix this and introduce a
     *  different matchGetBackReference method]
     */
    public String getBackReference(String input, int n);

}

