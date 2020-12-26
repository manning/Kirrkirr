package Kirrkirr.dictionary;

import Kirrkirr.Kirrkirr;
import Kirrkirr.util.*;

import java.util.*;
import java.io.Serializable;

/** A simple Vector-like class containing an array of DictField and its size.
 *  Madhu:'00, made the class Serializable
 */
public class DictFields implements Serializable {

    private static final long serialVersionUID = -8381779364424299341L;

    private DictField[] field;


    public DictFields(int size) {
        field = new DictField[size];
    }

    /** Constructs a DictFields out of a Vector of DictField elements.
     *
     *  @param v A Vector of DictField
     */
    public DictFields(Vector<DictField> v) {
        int vSize = v.size();
        field = new DictField[vSize];
        for (int i = 0; i < vSize; i++) {
            field[i] = v.elementAt(i);
        }
    }

    /** Converts a DictFields list into a Vector.
     *  This is just temporary until all the code has been converted to
     *  use Vector's rather than DictFields.
     *
     *  @return This DictFields represented as a Vector
     */
    public Vector<DictField> toVector() {
        int size = field.length;
        Vector<DictField> v = new Vector<DictField>(size);
        for (DictField item : field) {
            v.addElement(item);
        }
        return v;
    }

    public int size() {
        return field.length;
    }

    public DictField get(int i) {
        return field[i];
    }

    public void set(int i, DictField df) {
        field[i] = df;
    }

    public boolean member(DictField df) {
        for (DictField aField : field) {
            if (aField.equals(df)) {
                return true;
            }
        }
        return false;
    }

    /** Add to the current DictFields ones in dfs that one doesn't already
     *  have.  I.e., does set union.
     *
     *  @return the union (this)
     */
    public DictFields union(DictFields dfs) {
        Vector<DictField> v = new Vector<DictField>();
        for (int j = 0; j < dfs.field.length; j++) {
            if ( ! member(dfs.field[j])) {
                v.addElement(dfs.field[j]);
            }
        }
        if (v.isEmpty()) {
            return this;
        } else {
            DictFields dfstemp = new DictFields(v);
            return append(dfstemp);
        }
    }

    public DictFields append(DictFields dfs) {
        if (dfs == null)
            return this;
        DictField[] oldfields = field;
        field = new DictField[oldfields.length + dfs.field.length];
        System.arraycopy(oldfields, 0, field, 0, oldfields.length);
        System.arraycopy(dfs.field, 0, field, oldfields.length, dfs.field.length);
        return this;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("DictFields["+field.length+"] = {");
        for (DictField df : field) {
            if (df != null) {
                sb.append("  ");
                sb.append(df.toString());
            }
        }
        sb.append(" }");
        return sb.toString();
    }


    /** This static factory method constructs a new DictFields, but takes
     *  only some of the links, looking for a variety of types, and common
     *  words.
     *  The implementation isn't very efficient, and so would perform
     *  badly -- O(n^2) -- if the vector were large
     *
     *  @param v The vector of DictFields to chose from
     *  @param num The maximum number to take
     */
    public static DictFields niceSelection(Vector<DictField> v, int num,
                                        DictionaryCache cache) {
        if (Dbg.PROGRESS) {
            Dbg.print("niceSelection of " + num + " from " + v);
        }
        int vSize = v.size();
        if (vSize <= num) {
            // take them all
            return new DictFields(v);
        }

        // remove any broken (not resolved) links
        // count down as removing moves items down
        for (int i = vSize - 1; i >= 0; i--) {
            DictField df = v.elementAt(i);
            if ( ! df.hasExact()) {
                v.removeElementAt(i);
            }
        }

        vSize = v.size();
        if (vSize <= num) {
            // take the remainder
            return new DictFields(v);
        }

        // sort the links by frequency
        DictField[] items = new DictField[vSize];
        v.copyInto(items);
        Arrays.sort(items, new DictFieldFreqComparator(cache));
        DictFields newDF = new DictFields(num);

        int numEntries = 0;
        // take one of each kind of link
        //      int maxAssigned = 0;
        int max = Kirrkirr.dictInfo.getNumLinks();
        if (max > num)
            max = num;
        boolean[] hasTagOfType = new boolean[max];
        int curindex=0;
        for (int i = 0; i < items.length; i++) {
            DictField curr = items[i];
            if (curr.tag<max && !hasTagOfType[curr.tag])
                {
                    hasTagOfType[curr.tag]=true;
                    newDF.set(curindex,curr);
                    numEntries++;
                    curindex++;
                    if (curindex==num) return newDF;
                    items[i]=null;
                    boolean done=false;
                    for (int j=i+1;j<items.length && !done;j++)
                        {
                            DictField ja = items[j];
                            if (curr.equals(ja)) {
                                items[j]=null;
                                i++;
                            } else {
                                done=true;
                            }
                        }
                }

            /*      DictField curr = (DictField) items[i];
            boolean gotOne = false;
            for (int j = 0; j < maxAssigned; j++) {
                if (newDF.get(j).tag == curr.tag) {
                    gotOne = true;
                }
            }
            if ( ! gotOne) {
                newDF.set(maxAssigned, curr);
                maxAssigned++;
                curr.hnum = DictField.BEAST;
                }*/
        }

        // fill in the rest with frequent items
        DictField prevcur=null;
        for (DictField curr : items) {
            if (curr != null && !curr.equals(prevcur)) {
                newDF.set(curindex, curr);
                numEntries++;
                curindex++;
                if (curindex == num) return newDF;
            }
            prevcur = curr;
        }

        /*      for (int i = curindex; i < num; i++) {
            DictField curr = (DictField) items[i];
            if (curr.hnum != DictField.BEAST) {
                newDF.set(maxAssigned, curr);
                maxAssigned++;
            }
            }*/
        if (numEntries!=newDF.size()) {
            DictFields realDF=new DictFields(numEntries);
            for (int i=0;i<numEntries;i++) {
                realDF.set(i,newDF.get(i));
            }
            return realDF;
        }
        return newDF;
    }


//     public static DictFields niceSelection(Vector v, int num,
//                                      DictionaryCache cache) {
//      if (v.size() <= num) {
//          // take them all
//          return new DictFields(v);
//      }

//      //VJ: this doesn't work w/o dictfields, since helper.getpoly always
//      //returns a value greater than 0.

//      // remove any broken (not resolved) links
//      // count down as removing moves items down
// /*   for (int i = v.size() - 1; i >= 0; i--) {
//             DictField df = (DictField) v.elementAt(i);
//          if (!df.hasExact()) {
//              v.removeElementAt(i);
//          }
//      }
// */

//         if (v.size() <= num) {
//          // take the remainder
//          return new DictFields(v);
//      }

//      // sort the links by frequency
//      Object[] items = new Object[v.size()];
//      v.copyInto(items);
//      Sort.quicksort(items, new DictFieldFreqComparator(cache));
//      DictFields newDF = new DictFields(num);
//      int numentries=0;

//      // take one of each kind of link
//      //      int maxAssigned = 0;
//      int max = Kirrkirr.dictInfo.getNumLinks();
//      if (max > num)
//          max = num;
//      boolean hasTagOfType[]=new boolean[max];
//      int curindex=0;
//      for (int i = 0; i < items.length; i++) {
//          String curr = (String) items[i];
//          if (curr.tag<max && !hasTagOfType[curr.tag])
//              {
//                  hasTagOfType[curr.tag]=true;
//                  newDF.set(curindex,curr);
//                  numentries++;
//                  curindex++;
//                  if (curindex==num) return newDF;
//                  items[i]=null;
//                  boolean done=false;
//                  for (int j=i+1;j<items.length && !done;j++)
//                      {
//                          DictField ja=(DictField)items[j];
//                          if (curr.equals(ja))//curr.value.equals(ja.value) && curr.hnum==ja.hnum)
//                              {
//                                  items[j]=null;
//                                  i++;
//                              }
//                          else
//                              done=true;
//                      }
//              }

//          /*      DictField curr = (DictField) items[i];
//          boolean gotOne = false;
//          for (int j = 0; j < maxAssigned; j++) {
//              if (newDF.get(j).tag == curr.tag) {
//                  gotOne = true;
//              }
//          }
//          if ( ! gotOne) {
//              newDF.set(maxAssigned, curr);
//              maxAssigned++;
//              curr.hnum = DictField.BEAST;
//              }*/
//      }

//      // fill in the rest with frequent items
//      DictField prevcur=null;
//      for (int i = 0; i < items.length; i++)
//          {
//              DictField curr = (DictField) items[i];

//              if (curr!=null && !curr.equals(prevcur))
//                  {
//                      newDF.set(curindex,curr);
//                      numentries++;
//                      curindex++;
//                      if (curindex==num) return newDF;
//                  }
//              prevcur=curr;
//          }

//      /*      for (int i = curindex; i < num; i++) {
//          DictField curr = (DictField) items[i];
//          if (curr.hnum != DictField.BEAST) {
//              newDF.set(maxAssigned, curr);
//              maxAssigned++;
//          }
//          }*/

//      if (numentries!=newDF.size())
//          {
//              DictFields realDF=new DictFields(numentries);
//              for (int i=0;i<numentries;i++)
//                  {
//                      realDF.set(i,newDF.get(i));
//                  }
//              return realDF;
//          }
//      return newDF;
//     }


} // DictFields

