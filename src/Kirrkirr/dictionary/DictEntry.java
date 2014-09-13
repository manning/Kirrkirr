package Kirrkirr.dictionary;

import java.io.*;

/** Information for each dictionary entry that is stored in the .clk
 *  file by serialization: Whether this word has pictures and sounds, whether it is a subword,
 *  the frequency (for sorting, etc), and, most importantly, its position
 *  in the xml dictionary file. Transient fields for note and register (ie not serialized).
 *  The actual word uniqueKey is in a hash pointing to one of those.
 *
 *  <P>Note: must keep serialVersionUID so it runs off of the serialized version
 *  in the .clk file after editing this source file. ("If not specified by the class,
 *  the value returned is a hash computed from the class's name, interfaces, methods,
 *  and fields using the Secure Hash Algorithm (SHA) as defined by the National
 *  Institute of Standard.")
 *
 *  @see <a href="http://java.sun.com/products/jdk/1.1/docs/guide/serialization/spec/class.doc.html">
 *  http://java.sun.com/products/jdk/1.1/docs/guide/serialization/spec/class.doc.html</a>
 */
public class DictEntry implements Externalizable {

    /** See class notes. Used for serialization compatibility */
    private static final long serialVersionUID = -1639338961685868118L;

    /** Whether this word is a subword */
    public boolean isSubword;
    /** Whether this word has pictures */
    public boolean hasPics;
    /** Whether this word has sounds */
    public boolean hasSounds;
    /** The frequency of this word. Used for sorting.
	(Could be good for other things too!). */
    public int freq;
    /** The position of this word's entry in the xml dictionary file */
    public long fpos;

    // public String display;  // not used, it appears

    /** Whether this word has a note */
    public transient boolean hasNote;
    /** Whether this word has a special register - could be non-transient?
        Used for fun panels and games. */
    public transient boolean hasRegister;
    public transient String pos;

    /** Sets the transient fields to false.
     */
    public DictEntry() {
        // cdm 2008: I think this is unnecessary, as get for free, even on deserialization
        // hasNote=false;
	// hasRegister=false;
    }

    /** Prints out all the fields of the DictEntry for debugging.
     */
    public String toString() {
	return "DictEntry[sub:" + isSubword + ", freq:" + freq +
		", fpos:" + fpos + ", hasPics:" + hasPics +
	    ", hasSounds:" + hasSounds + ", hasNote:" + hasNote +", hasRegister:" + hasRegister
	    + ']';
    }

    /** Writes out the non-transient fields for
     *  serialization (to the clk file).
     */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
	// don't write out transient fields
        out.writeBoolean(isSubword);
        out.writeBoolean(hasPics);
        out.writeBoolean(hasSounds);
        out.writeInt(freq);
        out.writeLong(fpos);
    }

    /** Reads in the non-transient fields from
     *  a serialized version (ie the clk file).
     */
    @Override
    public void readExternal(ObjectInput in) throws IOException {
        isSubword = in.readBoolean();
        hasPics = in.readBoolean();
        hasSounds = in.readBoolean();
        freq = in.readInt();
        fpos = in.readLong();
    }

}

