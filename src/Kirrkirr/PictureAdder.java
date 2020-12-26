package Kirrkirr;

import java.io.RandomAccessFile;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

/** Some standalone method for Nahuatl botany pictures. Maybe can delete? */
public class PictureAdder {

    // delimiters for the xml dictionary
    private static final String delim = "\t\r\n";

    public static void main(String[] argv){
        //clearPicList();
        linkPictures();
    }

    public static void clearPicList(){
        RandomAccessFile oldFile, newFile;
        long curr, length;
        try{
            oldFile = new RandomAccessFile("fileindex.txt", "r");
            newFile = new RandomAccessFile("fileindex2.txt","rw");
            length = oldFile.length();
            curr = oldFile.getFilePointer();
        } catch (Throwable exception) {
             exception.printStackTrace();
             return;
        }

        String currLine;
        while(curr < length){
            try{
                currLine = oldFile.readLine();
                currLine = oldFile.readLine();
                newFile.writeBytes(currLine+"\n");
                /*              StringTokenizer tokenizer = new
                    StringTokenizer(currLine, delim2);
                while (tokenizer.hasMoreTokens()){
                    String token = tokenizer.nextToken();
                    if(token.startsWith("0"))
                        newFile.writeBytes(token+"\n");
                        }*/
                curr = oldFile.getFilePointer();
            } catch (Throwable exception) {
                exception.printStackTrace();
                return;
            }
        }
    }


    public static void linkPictures(){

        Hashtable plants = new Hashtable();
        long currLength, hashLength;
        RandomAccessFile botanyDoc;
        try {
            // scanning the picture list
        botanyDoc = new RandomAccessFile("NahuatlBotany.txt", "r");
        currLength = botanyDoc.getFilePointer();
        hashLength = botanyDoc.length();
         } catch (Throwable exception) {
             exception.printStackTrace();
             return;
        }

        PictureKey currPic;
        // storing the picture list entries in the hash table
        while(currLength<hashLength){
            try{
                String line = botanyDoc.readLine();
                StringTokenizer tokenizer = new
                    StringTokenizer(line, delim);
                currPic = new PictureKey();
                currPic.index = tokenizer.nextToken();
                tokenizer.nextToken();   // skipping 2nd field
                tokenizer.nextToken();   // skipping 3rd field
                currPic.ameyal = tokenizer.nextToken();  // 1st name
                currPic.oapan = tokenizer.nextToken();  // 2nd name
                plants.put(currPic, currPic);
                currLength = botanyDoc.getFilePointer();
            } catch (Throwable exception) {
                exception.printStackTrace();
            }
        }
        try{
            botanyDoc.close();
        } catch (Throwable exception) {
             exception.printStackTrace();
             return;
        }





        RandomAccessFile picNameFile;
        Vector picNames = new Vector();
        long curr, length;

        try{
            picNameFile = new RandomAccessFile("fileindex2.txt", "r");
            length = picNameFile.length();
            curr = picNameFile.getFilePointer();
        } catch (Throwable exception) {
            exception.printStackTrace();
            return;
        }

        // filling vector with image filenames
        while (curr < length) {
            try {
                picNames.addElement(picNameFile.readLine());
                curr = picNameFile.getFilePointer();
            } catch (Throwable exception) {
                exception.printStackTrace();
                return;
            }
        }
        //System.out.println(picNames);





        RandomAccessFile dictionary, newDict;
        PictureKey newKey = new PictureKey();
        Vector dictStrings = new Vector();
        long dictLength, currPos;
        String dictLine;
        try{
            dictionary = new
                RandomAccessFile("./Nahuatl/ActiveNahuatl2002hnums.xml", "r");
            dictLength = dictionary.length();
            currPos = dictionary.getFilePointer();
        } catch (Throwable exception) {
             exception.printStackTrace();
             return;
        }

        int j = 0;
        // storing dictionary, with new tags
        while(currPos < dictLength){
            try{
                dictLine = dictionary.readLine();
                //dictStrings.addElement(dictLine);
                if(dictLine.startsWith("<lxagroup>")){
                    newKey = new PictureKey();
                }
                else if(dictLine.startsWith("<lxa>")){
                    newKey.ameyal = dictLine.substring(5,
                    dictLine.length()-6);
                    //System.out.println("<lxa>");
                    //System.out.println(dictLine);
                    //System.out.println(newKey.ameyal);
                }
                else if(dictLine.startsWith("<lxa hnum=")){
                    newKey.ameyal = dictLine.substring(14,
                    dictLine.length()-6);
                }
                else if(dictLine.startsWith("<lxo>")){
                    newKey.oapan = dictLine.substring(5,
                    dictLine.length()-6);
                    //System.out.println("<lxo>");
                    //System.out.println(dictLine);
                    //System.out.println(newKey.oapan);
                }
                else if(dictLine.startsWith("lxo hnum=")){
                    newKey.oapan = dictLine.substring(14,
                    dictLine.length()-6);
                }
                else if(dictLine.startsWith("<dt>")){
                    //System.out.println("<dt>");
                    Enumeration e = plants.keys();
                    boolean keepGoing = true;
                    while( e.hasMoreElements() && keepGoing){
                        PictureKey currKey = (PictureKey) e.nextElement();
                        if(newKey.equals(currKey)){ // if found
                            // filling index value
                            j++;
                             System.out.println("Key found, " + j + ", " + currKey.index);
                            // System.out.println(currKey + ", " + newKey);
                            StringBuffer picEntry = new StringBuffer("<image>");
                            for(int i=0; i<picNames.size(); i++){
                                String entry =
                                (String)picNames.elementAt(i);
                        //System.out.println(entry +", "+currKey.index);
                                if (entry.startsWith(currKey.index)){
                                    //   System.out.println("Hey there.");
                                    picEntry.append("<imgi>" + entry + "</imgi>");
                                }
                            }
                            picEntry.append("</image>");

                            dictStrings.addElement(picEntry.toString());
                            keepGoing = false;
                        }
                    }
                }
                dictStrings.addElement(dictLine);
                //System.out.println(dictLine);
                currPos = dictionary.getFilePointer();
            } catch (Throwable exception) {
             exception.printStackTrace();
             return;
            }
        }

        // writing out the new dictionary
        try{
            newDict = new
                RandomAccessFile("./Nahuatl/ActiveNahuatl2003.xml", "rw");
        } catch (Throwable exception) {
             exception.printStackTrace();
             return;
        }
        try{
            for(int i = 0; i< dictStrings.size(); i++){
                //System.out.println((String)dictStrings.elementAt(i));
                newDict.writeBytes((String) dictStrings.elementAt(i)+"\n");
            }
        }catch (Throwable exception) {
            exception.printStackTrace();
            return;
        }

    }


    private static class PictureKey {
        public String index;
        public String ameyal;
        public String oapan;

        public PictureKey() {}

        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            } else if (obj instanceof PictureKey) {
                PictureKey pk = (PictureKey) obj;
                return equals(pk);
            } else {
                return false;
            }
        }

        public boolean equals(PictureKey obj) {
            //  if(this.ameyal.length()!=obj.ameyal.length() &&
            //this.oapan.length()!=obj.oapan.length())
            //return false;

            int alength, olength;

            if (this.ameyal.length() < obj.ameyal.length())
                alength = this.ameyal.length();
            else alength = obj.ameyal.length();

            if (this.oapan.length() < obj.oapan.length())
                olength = this.oapan.length();
            else olength = obj.oapan.length();

            boolean aequal, oequal;
            String thisChar, objChar;

            aequal = true;
            for(int i=0; i<alength; i++){
                thisChar = this.ameyal.substring(i, i+1);
                objChar = obj.ameyal.substring(i, i+1);

                if(thisChar.equals("???")) thisChar = "a";
                else if(thisChar.equals("???")) thisChar = "e";
                else if(thisChar.equals("???")) thisChar = "i";
                else if(thisChar.equals("???")) thisChar = "o";

                if(objChar.equals("???")) objChar = "a";
                else if(objChar.equals("???")) objChar = "e";
                else if(objChar.equals("???")) objChar = "i";
                else if(objChar.equals("???")) objChar = "o";
                    /*
                    switch(thisChar){
                    case ???: thisChar = a; break;
                    case ???: thisChar = e; break;
                    case ???: thisChar = i; break;
                    case ???: thisChar = o; break;
                    default : break;
                    }
                    switch(objChar){
                    case ???: objChar = a; break;
                    case ???: objChar = e; break;
                    case ???: objChar = i; break;
                    case ???: objChar = o; break;
                    default : break;
                    } */
                if (!thisChar.equals(objChar)) { aequal = false; break; }
            }

            oequal = true;
            for(int i=0; i<olength; i++){
                thisChar = this.oapan.substring(i, i+1);
                objChar = obj.oapan.substring(i, i+1);

                if(thisChar.equals("???")) thisChar = "a";
                else if(thisChar.equals("???")) thisChar = "e";
                else if(thisChar.equals("???")) thisChar = "i";
                else if(thisChar.equals("???")) thisChar = "o";

                if(objChar.equals("???")) objChar = "a";
                else if(objChar.equals("???")) objChar = "e";
                else if(objChar.equals("???")) objChar = "i";
                else if(objChar.equals("???")) objChar = "o";

                    /*  switch(thisChar){
                    case ???: thisChar = a; break;
                    case ???: thisChar = e; break;
                    case ???: thisChar = i; break;
                    case ???: thisChar = o; break;
                    default : break;
                    }
                    switch(objChar){
                    case ???: objChar = a; break;
                    case ???: objChar = e; break;
                    case ???: objChar = i; break;
                    case ???: objChar = o; break;
                    default : break;
                    } */

                if (!thisChar.equals(objChar)){ oequal = false; break; }
            }

            //  if(this.ameyal.equals("komalakawistli"))// && obj.index.equals("0220"))
                //   System.out.println("First: "+this.ameyal+", Second: "+
                //obj.ameyal+"Aequal = "+aequal+", Oequal = "+oequal);
            return (aequal || oequal);

            // if(this.ameyal.equals(obj.ameyal) ||
            //this.oapan.equals(obj.oapan))
            //return true;
            //else return false;
        }

        public int hashCode(){
            return index.hashCode();
        }

        public String toString(){
            return index + ameyal + oapan;
        }

    }




}

