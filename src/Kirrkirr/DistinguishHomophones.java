package Kirrkirr;

import java.io.*;
import java.util.*;

import org.w3c.dom.*;

import javax.xml.parsers.*;
// import org.xml.sax.SAXException;

// import com.sun.xml.tree.XmlDocument;
// import org.apache.crimson.tree.XmlDocument;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

/**
 * Distinguishes homophones by adding an hnum attribute to headwords where
 * duplicates exist.
 *
 * Sample Usage: java -mx128m Kirrkirr.DistinguishHomophones orig.xml new.xml
 *
 * *** NOTE: Probably should CHANGE encoding back to "ISO-8859-1" from
 * "US-ASCII" manually in the output file.  This was a *hack* to get the
 * special (e.g., accented) characters to write out as escaped character
 * entities, since IndexMaker was having problems with them when they were
 * being written out as accented chars. ("invalid chars. ..."). ***
 *
 * May not be entirely Java 1.1 compatible, but changes required should be
 * minor (e.g., Vector.add -> Vector.addElement ...)
 *
 * Strategy: Build document tree and use the recursive
 * Element.getElementsByTagName() in order to get the headwords for each
 * dialect (homophones are treated on a per dialect basis).  Run through
 * each dialect's headwords and dump them into a Hashtable, with the
 * headword String as key and a Vector of all Elements with this headword
 * as value.  Detect duplicates while inserting into hashtable, and throw
 * headwords into a Vector (really being used as a Set).  When finished
 * running through headwords, iterate through the duplicates Vector and
 * lookup in the hashtable and add hnums to the Elements.  Finally, write
 * the updated xml document tree back out.  *Certainly* not the most
 * efficient way of doing this - just one of the first ways I came up with
 * (since rarely run anyways, efficiency shouldn't matter too much).
 *
 * Author: Conrad Wai, 2002
 */
public class DistinguishHomophones {

    // private static final String SC_EntryTag = "lxaGroup";
    //Headword Tags
    private static final String[] SC_Dialects = {"lxa", "lxo"};
    //public static final String[] SC_Alternates = {"lxaa", "lxoa"};

    private static final int numDialects = 2;


    public static void main(String argv[]) {
        if (argv.length != 2)
            System.out.println("Error: Usage: DistinguishHomophones "
                               + "<infile> <outfile>");
        else {
            makeHeadwordsUnique(argv[0], argv[1]);
        }
    }

    public static void makeHeadwordsUnique(String infile, String outfile)
    {
        // standard incantation to get a Document obj.
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        DocumentBuilder db;
        Document doc = null;
        Element root = null;
        // parse and build whole doc tree
        try {
            db = dbf.newDocumentBuilder();
            doc = db.parse(new File(infile));
            root = doc.getDocumentElement();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }


        Hashtable[] headwords = new Hashtable[numDialects];  // one per dialect
        Vector[] duplicates = new Vector[numDialects];    // really being used as a Set

        for (int i = 0; i < numDialects; i++) {  // for each dialect
            headwords[i] = new Hashtable(15000);
            duplicates[i] = new Vector(50);

            System.out.println("Finding dups for: " + SC_Dialects[i]);

            // recursively search for/get headwords for the dialect
            NodeList hwNL = root.getElementsByTagName(SC_Dialects[i]);
            int numHW = hwNL.getLength();

            for (int j = 0; j < numHW; j++) {  // for each headword
                Element hwElem = (Element)hwNL.item(j);

                // get hwElem text (improve?)
                String hw = hwElem.getFirstChild().getNodeValue();

                // put into Hashtable - key: String hw, value: Vector
                // elems, of all hwElems (Elements) with headword hw
                Vector elems = (Vector) headwords[i].get(hw); // uses equals()

                // Duplicate if not null (=> already in hashtable)
                // Special cases are dictionary-specific
                if (elems != null
                    && !hw.equals("----") && !hw.equals("-----")
                    && !hw.equals("zzz") && !hw.equals("zzzz"))
                {
                    System.out.println("DUP!: " + hw);
                    if (!duplicates[i].contains(hw))  // compares by equals()
                        duplicates[i].addElement(hw);
                }
                else {
                    elems = new Vector();
                }
                elems.addElement(hwElem);  // add to Vector of vals. (Elements)
                headwords[i].put(hw, elems);  // put/re-put in hashtable
            }

            // Don't really know how this would be incorporated in terms
            // of hnums, etc....
            /*
            // Alternate spellings...
            NodeList althwNL = root.getElementsByTagName(SC_Alternates[i]);
            int numAltHW = althwNL.getLength();
            for (int j = 0; j < numAltHW; j++) {
                Element hwElem = (Element)althwNL.item(j);

                // get hwElem text (improve?)
                String hw = hwElem.getFirstChild().getNodeValue();

                // put into Hashtable (put (parent of) hwElem in as val...??)
                String prevVal = (String)headwords[i].put(hw, "Blah");
                if (prevVal != null && !hw.equals("----") && !hw.equals("-----")) {
                    System.out.println("DUP!: " + hw);
                }
            }
            */
            System.out.println("Finished finding dups for: " + SC_Dialects[i]);
            System.out.println("Adding hnum attribute to duplicates...");

            // Run through the duplicates and set hnum attribute
            //      Iterator dupsIt = duplicates[i].iterator();
            int nDuplicates = duplicates[i].size();
            //      while (dupsIt.hasNext()) {
            for(int dupNum = 0; dupNum < nDuplicates; dupNum++) {
                //              String dupHW = (String)dupsIt.next();
                String dupHW = (String) duplicates[i].elementAt(dupNum);
                Vector dupElems = (Vector)headwords[i].get(dupHW);
                int numDups = dupElems.size();
                for (int d = 0; d < numDups; d++) {
                    Element dupElem = (Element)dupElems.elementAt(d);
                    dupElem.setAttribute("hnum", Integer.toString(d+1));
                }
            }

            System.out.println("Finished pass: " + SC_Dialects[i]);
        }

        // Write updated xml document tree back out
        System.out.println("Writing out updated xml...");

        // Couldn't get Nick Parlante's trick, or any of the variations I
        // found on the web, to work out (using a Transformer is
        // apparently the "official" way in any case...)
        // see http://www.deitel.com/books/xmlHTP1/xmlhtp1_faq.html

//      XmlDocument x = (XmlDocument)doc;
//      //x.write(System.out, "UTF-8");
//      try {
//          x.write(new FileOutputStream(outfile));
//      } catch (Exception e) {
//          e.printStackTrace();
//      }

        TransformerFactory transFact = TransformerFactory.newInstance();
        try {
            Transformer serializer = transFact.newTransformer();

            // *Hack*: set the encoding to "US-ASCII" so that the accented
            // characters (and other special characters) are written out
            // in escaped (&#nnn;) form, since IndexMaker was having
            // problems with them.  Probably should change encoding back
            // to "ISO-8859-1" by hand before running IndexMaker.
            // It'd be nice if I could have found an escapeChars()
            // method/property for the parser/transformer/serializer ...
            // cw 2002
            serializer.setOutputProperty("encoding", "US-ASCII");
//          serializer.setOutputProperty("encoding", "ISO-8859-1");  // need this or UTF-8 will be used, and special chars. will be incorrectly escaped (UTF-8 by default)

            serializer.transform(new DOMSource(doc), new StreamResult(new BufferedOutputStream(new FileOutputStream(outfile))));
//          serializer.transform(new DOMSource(doc), new StreamResult(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(outfile)), "ASCII")));  // "ASCII" (US-ASCII) encoding supported since Java 1.2

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Done.");
    }

}

