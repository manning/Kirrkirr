package Kirrkirr.util;

import java.io.Serializable;
import java.util.Comparator;

/** This little class provides a slightly specialized alphabetical order
 *  comparator, which knows to ignore case, and a couple of special morpheme
 *  characters (-, =).  It's been put in its own class because it is used
 *  in several places.
 */
public class KAlphaComparator implements Comparator<String>, Serializable {

    private static final long serialVersionUID = -5137551894597139756L;

    public KAlphaComparator() {
    }

    @Override
    public int compare(String a, String b) {
	return compareTwo(a, b);
    }

    private static int compareTwo(final String a, final String b) {
        // return +ve if a should come later than b
        if (a == null) {
            if (b == null) {
                return 0;
            } else {
                return -1;
            }
        } else if (b == null) {
            return 1;
        }
        if (a.length() == 0) {
            if (b.length() == 0) {
                return 0;
            } else {
                return -1;
            }
        } else if (b.length() == 0) {
            return 1;
        }

        String ac = a;
        char one = a.charAt(0);
        if (one == '-' || one == '=') {
            ac = a.substring(1);
        }

        String bc = b;
        char two=b.charAt(0);
        if (two == '-' || two == '=') {
            bc = b.substring(1);
        }

	//we should strip uniquifiers during comparison
        //jdk1.1 doesn't support compareIgnoreCase
	int retval =  Helper.getWord(ac).toLowerCase().compareTo(Helper.getWord(bc).toLowerCase());
	if (retval != 0) {
            return retval;
        } else {
            ac = Helper.getUniquifier(ac);
            bc = Helper.getUniquifier(bc);
            if (ac == null) {
                if (bc == null) {
                    return 0;
                } else {
                    return -1;
                }
            } else if (bc == null) {
                return 1;
            }
            return ac.compareTo(bc);
        }
    }

}
