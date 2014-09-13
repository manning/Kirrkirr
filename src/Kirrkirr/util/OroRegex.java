package Kirrkirr.util;

import org.apache.oro.text.regex.*;

/**
 * The <code>OroRegex</code> object implements the Regex interface by using
 * the Jakarta ORO package.
 *
 * @version     1.0, 2001/09/01
 * @author      Christopher Manning
 */
public class OroRegex implements Regex {

    private static PatternCompiler p5c = new Perl5Compiler();
    private static PatternMatcher p5m = new Perl5Matcher();
    private Pattern pattern;
    private Substitution sub;
    private String originalPattern;
    private String originalSub;

    public OroRegex(String patternStr) {
	try {
            originalPattern = patternStr;
            pattern = p5c.compile(patternStr);
	} catch (MalformedPatternException mpe) {
            mpe.printStackTrace();
	}
    }

    public OroRegex (String patternStr, int options) {
	try {
            originalPattern = patternStr;
	    pattern = p5c.compile(patternStr, options);
	} catch (MalformedPatternException mpe) {
            mpe.printStackTrace();
	}
    }

    public OroRegex (String patternStr, String substStr) {
	try {
	    pattern = p5c.compile(patternStr);
	    sub = new Perl5Substitution(substStr);
	    originalPattern = patternStr;
	    originalSub = substStr;
	} catch (MalformedPatternException mpe) {
            mpe.printStackTrace();
	}
    }

    /** For "substitute all" use 
     *  org.apache.oro.text.regex.Util.SUBSTITUTE_ALL 
     */
    public OroRegex (String patternStr, String substStr, int regexOptions) {
	try {
	    pattern = p5c.compile(patternStr, regexOptions);
	    sub = new Perl5Substitution(substStr);
	} catch (MalformedPatternException mpe) {
            mpe.printStackTrace();
	}
    }

    public static Regex newRegex(String pattern) {
	return new OroRegex(pattern);
    }

    public static Regex newICaseRegex(String pattern) {
        return new OroRegex(pattern, Perl5Compiler.CASE_INSENSITIVE_MASK);
    }

    public static Regex newSubRegex(String pattern, String substitute) {
	return new OroRegex(pattern, substitute);
    }

    public boolean hasMatch(String input) {
        return (p5m.contains(input, pattern));
    }

    public String getStringMatch(String input) {
	if (p5m.contains(input, pattern)) {
	    MatchResult mr = p5m.getMatch();
	    return mr.toString();   // returns match 0 = whole thing
	} else {
	    return null;
	}
    }

    public void setSub(String substitute) {
        sub = new Perl5Substitution(substitute);
    }
    
    public String getOriginalPattern() { return originalPattern; }
    public String getOriginalSub() { return originalSub; }

    public String toString() { return originalPattern; }

    public String doReplace(String input) {
        if (sub != null) {
	    return Util.substitute(p5m, pattern, sub, input,
				   Util.SUBSTITUTE_ALL);
        } else {
            System.err.println("Regex: no substitute for regex: "
                + this.toString());
            return(input);
        }
    }

    public String getBackReference(String input, int n) {
	if (p5m.contains(input, pattern)) {
	    MatchResult mr = p5m.getMatch();
	    return mr.group(n);   // returns n'th matched thing
	} else {
	    return null;
	}
    }

    /*
    // just for testing - needs java.io imported
    public static void main(String args[]) {
	Regex a = new OroRegex("p[aeiou]nd[aeiou]");
	if (a.hasMatch("the panda is pending")) {
	    System.out.println("Matched " + 
			       a.getStringMatch("the panda is pending"));
	} else {
	    System.out.println("Bung");
	}
	a.setSub("nose");
	System.out.println("Replaced to: " + 
			   a.doReplace("the panda is pending"));
    }
    */

}

