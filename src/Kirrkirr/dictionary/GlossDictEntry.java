package Kirrkirr.dictionary;

import Kirrkirr.util.Dbg;
import Kirrkirr.util.KAlphaComparator;

import java.io.*;
import java.util.*;


/** <P>Information for each english dictionary entry that is stored in the
 *  .clk file by serialization: the number of equivalents it has in Warlpiri,
 *  and the position in the xml dictionary file for each warlpiri equivalent,
 *  whether this word has pictures, whether it is a subword,  and a short
 *  representing the index in the word's String under which it should be
 *  stored, if it is a subword. For
 *  example, "maternal aunt" might have a short field of 9, means to index the
 *  word under "aunt" rather than "maternal."</P>
 *  <P><U>Unsolved issues:</U> The short can replace the boolean isSub, by
 *  checking for -1. But overall this may not be the best way to index
 *  english subwords. Plus, some words should be under many words - ie
 *  "maternal" *and* "aunt". But that would complicate the word list and may
 *  take up a bit of memory. It's not always easy to tell which words to put
 *  it under (by machine). The carats in the dictionary
 *  data would also complicate matters further. Also, should sounds of
 *  Warlpiri equivalents be noticed in the english list? Should notes
 *  be implemented for the english list? </P>
 *  <P>Note: must keep serialVersionUID so it runs off of the serialized
 *  version in the .clk file after editing this source file. ("If not
 *  specified by the class, the value returned is a hash computed from the
 *  class's name, interfaces, methods, and fields using the Secure Hash
 *  Algorithm (SHA) as defined by the National Institute of Standard.")</P>
 *  @see <a href="http://java.sun.com/products/jdk/1.1/docs/guide/serialization/spec/class.doc.html">
 *  http://java.sun.com/products/jdk/1.1/docs/guide/serialization/spec/class.doc.html</a>
 */
public class GlossDictEntry implements Externalizable
{
    /** See class notes. Used for serialization compatibility */
    private static final long serialVersionUID =-359376639500834659L;

    /** Whether this word is a subword */
    public boolean isSubword;
    /** Whether any of the warlpiri words associated with this word have
pictures */
    public boolean hasPics;
    public transient boolean hasNote; // =false;
    public int freq;
    /** The naming here is kind of backwards.  If an entry is a single word,
     *  the subwords are the complete glosses that occur under it.
     */
    public String[] subwords;
   /** The positions of the warlpiri equivalents' entries in the xml
       dictionary file */
    //public long[] fpos;
    public String[] /* padded */ headwords;

    // If this is a subword, a short index in the word's string
    // saying which part of the phrase to index it under.
    // (See class notes). If not a subword, -1.
    // public short mainwordPosition;


    /** Gives you a new GlossDictEntry.
     *  Used to but no longer sets the mainwordPosition to -1.
     *  Indeed, all the contents of this are no-ops, since object variables
     *  are initialized by default to these values.
     */
    public GlossDictEntry() {
	// mainwordPosition=-1;
	// subwords=null;
	// fpos=null;
	//freq=0;
	// hasPics=false;
	// isSubword=false;
    }

    public boolean isSubword(String word) {
	for (int i=0; i<numSubwords(); i++) {
	    if (subwords[i].equals(word)) return true;
	}
	return false;
    }

    public int numSubwords(){
	if (subwords==null)
	    return 0;
	return subwords.length;
    }

    public int numMatches(){
	if (headwords==null)
	    return 0;
	return headwords.length;
    }


    /** Prints out all the fields of the EnglishDictEntry for debugging.
     */
    public String toString() {
	StringBuilder temp=new StringBuilder("EDE[matches: ");
	temp.append(numMatches()).append(" {");
	for (int i=0; i<numMatches(); i++){
	    temp.append(" ").append(headwords[i]);
	}
	temp.append(" } freq: ").append(freq);
	if (hasPics)
	    temp.append(" with pics");
	temp.append(", ");
	if (! isSubword) {
	    if (numSubwords() == 0) {
		temp.append("full and head gloss");
	    } else {
		temp.append("subgloss of");
		for (int i=0; i<numSubwords(); i++) {
		    temp.append(" ").append(subwords[i]);
		    if (i < numSubwords() - 1)
			temp.append(",");
		}
	    }
	} else {
	    temp.append("full gloss");
	}
	temp.append("]");
	return temp.toString();
    }


    /** Used by IndexMaker. Adds the headword at that file position
     *  to the list of warlpiri equivalents to this word. Runs
     *  really slowly, because have to recopy the array each time
     *  (but only run once, when making dictionary).
     *
    public void addHeadword(long curf){
	int num=numMatches();
	if (num==0){
	    fpos=new long[1];
	    fpos[0]=curfpos;
	} else {
	    long[] temp=new long[num];
	    System.arraycopy(fpos,0,temp,0,num);
	    fpos=new long[num+1];
	    System.arraycopy(temp,0,fpos,0,num);
	    fpos[num]=curfpos;
	}
	}*/

    public void addHeadword(String curhw){
	int num=numMatches();
	if (num==0){
	    headwords=new String[1];
	    headwords[0]=curhw;
	} else {
	    String[] temp=new String[num];
	    System.arraycopy(headwords,0,temp,0,num);
	    headwords=new String[num+1];
	    System.arraycopy(temp,0,headwords,0,num);
	    headwords[num]=curhw;
	}
     }

    public void addSubword(String subword){
	if (isSubword(subword)) return;
	int num=numSubwords();
	if (num==0){
	    subwords=new String[1];
	    subwords[0]=subword;
	} else {
	    String[] temp=new String[num];
	    System.arraycopy(subwords,0,temp,0,num);
	    subwords=new String[num+1];
	    System.arraycopy(temp,0,subwords,0,num);
	    subwords[num]=subword;
	}
	Arrays.sort(subwords, new KAlphaComparator());
    }

    /** Writes out the non-transient fields for
     *  serialization (to the clk file).
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeBoolean(hasPics);
	out.writeBoolean(isSubword);
	out.writeObject(headwords);
	out.writeObject(subwords);
	out.writeInt(freq);
    }

    private static int sleepNum=0;
    /** Reads in the non-transient fields from
     *  a serialized version (ie the clk file).
     *  Sleep every now and then so that when loading
     *  we allow other threads to take over.
     */
    public void readExternal(ObjectInput in) throws IOException {
	try{
	    hasPics = in.readBoolean();
	    isSubword=in.readBoolean();
	    headwords=(String[])in.readObject();
	    subwords=(String[])in.readObject();
	    freq=in.readInt();
	    if (sleepNum%500 == 0)
		Thread.sleep(10);
	    sleepNum++;
	} catch (InterruptedException e) {
	} catch(Exception e) {
	    if (Dbg.ERROR)
		e.printStackTrace();
	}
    }

}

