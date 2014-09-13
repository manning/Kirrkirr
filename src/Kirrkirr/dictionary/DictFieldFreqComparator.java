package Kirrkirr.dictionary;

import java.io.Serializable;
import java.util.*;

import Kirrkirr.util.Dbg;


/** @author Christopher Manning */
public class DictFieldFreqComparator implements Comparator<DictField>, Serializable {

    private static final long serialVersionUID = -5898791065683409281L;

    private DictionaryCache cache;

    public DictFieldFreqComparator(DictionaryCache c) {
        cache = c;
    }

    @Override
    public int compare(DictField dfa, DictField dfb) {
        // return +ve if a shoud come later than b
        if (Dbg.ERROR) {
            if (dfa == null) {
                Dbg.print("dfa is NULL!");
            }
            if (dfb == null) {
                Dbg.print("dfb is NULL!");
            }
        }
        if (dfa == null) {
            if (dfb == null) {
                return 0;
            } else {
                return -1;
            }
        } else if (dfb == null) {
            return 1;
        }
        if ( ! dfa.hasExact()) {
            if ( ! dfb.hasExact()) {
                return 0;
            }
            return 1;
        } else if ( ! dfb.hasExact()) {
            return -1;
        } else {
            int fa = cache.getFreq(dfa.uniqueKey);
            int fb = cache.getFreq(dfb.uniqueKey);
            return fb - fa;   //ie sort by freq in descending order
        }
    }

    public boolean equals(Object a, Object b) {
        DictField dfa = (DictField) a;
        DictField dfb = (DictField) b;

        if ( ! (dfa.hasExact() && dfb.hasExact()) ) {
            return false;
        }
        return cache.getFreq(dfa.uniqueKey) ==
                cache.getFreq(dfb.uniqueKey);
    }

} // DictFieldFreqComparator

