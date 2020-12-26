package Kirrkirr.util;

import Kirrkirr.dictionary.DictionaryCache;

import java.io.*;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.JOptionPane;

import org.w3c.dom.*;
import org.xml.sax.*;
import org.apache.xerces.dom.*;
import org.xml.sax.helpers.DefaultHandler;

/** <P>XmlMiniDocument constructs a XmlDocument object for one
 *  dictionary entry. This makes it possible to deal with
 *  the entries one at a time, rather than having to parse the
 *  whole (10MB) at once. Adapted from SAX2Writer.java from
 *  xerces samples.</P>
 *  <P>Chris: The performance of this part is quite bad. It should be
 *  cleaned up.  If we really wanted
 *  to do things well, then what we should do is use the internal more
 *  efficient DOM being used internally to xalan; see:
 *  http://xml.apache.org/xalan-j/xsltc/xsltc_dom.html
 *  But leaving aside figuring out how that works, we should at least make
 *  our SAX2 -> DOM2 conversion more convenient.</P>
 *  <P>Useful functions for use with Kirrkirr:
 *  <DL>
 *  <DD>Document = setup()</DD>
 *  <DD>Document = setup(Writer)</DD>
 *  <DD>Document = parseElement(InputStream, Document)</DD>
 *  <DD>Document = parseElement(InputStream, Document, Writer)</DD>
 *  <DD>Document = finish(Document, Writer)</DD>
 *  <DD>Document = finish(Document)</DD>
 *  </DL>
 *  (See below for descriptions).</P>
 *  <P>The function names are a bit confusing right now. They
 *  were overloaded to allow for outputting the mini XML file
 *  as it is parsed from the large XML file stream -
 *  this way, don't have to parse twice. (Could also
 *  iterate over Document, but this way seems faster?).
 *  (Note: can also pass in System.err to debug).
 *  It is the caller's responsibility to close the writer file at some point.
 *  So, functions which don't take a Writer don't write
 *  the xml file as it is parsed. Also, the document
 *  needs to know if this is the only part of the xml
 *  file being read (then just use start), or if
 *  other parts will be added later (then use startMultiple,
 *  addElement, and endMultiple), in order to
 *  end the document (with the dictionaryXPath tag)
 *  at the appropriate time.</P>
 *
 *  @author Kevin Jansz 2/8/1999 (c)
 *  @author Kristen Parton
 *  @author Christopher Manning
 *  Rewritten to use Xerces, Kristen Parton, Jul 2001.
 *  Made faster by Chris Manning Aug 2000, Jan 2002
 */
public final class XmlMiniDocument extends DefaultHandler implements EntityResolver, DTDHandler {

    /** How deeply nested XML elements in the dictionary can be.
     *  This is usually quite low.
     */
    private static final int MAXDEPTH = 50;

    private XmlParser parser;

    private Document  doc;
    private Writer    out = null;
    private int       entryDepth;
    private Element[] values = new Element[MAXDEPTH];
    private String entryElement;
    private Vector headwordXPathTokens;
    

    //---- Functions used by Kirrkirr ------

    /** Create an XmlMiniDocument around one dictionary entry.
     *  At the moment both dictionaryXPath and entryElement are both an XML element
     *  and we require a dictionary structure where the whole dictionary includes a
     *  list of entries.  We should move towards dictionaryXPath ~ dictionaryElements
     *  allowing a String[] of elements and building that nested structure.
     */
    public XmlMiniDocument(String headwordXPath, String entryElement) {
        // if (parser == null) {
            parser = new XmlParser();
            headwordXPathTokens = new Vector();
            try {
                //parser.setFeature("http://apache.org/xml/features/continue-after-fatal-error", true);
                parser.setErrorHandler(this);
                parser.setContentHandler(this);
                parser.setEntityResolver(this);
                parser.setDTDHandler(this);
            } catch(Exception e) {
                e.printStackTrace();
            }
            this.entryElement = entryElement;
            parseXPath(headwordXPath);
            if (Dbg.PARSE) {
                Dbg.print("XmlMiniDocument: headwordXPath is " + headwordXPath + " entryElement is " + entryElement);
            }
        // }
    }

    private void parseXPath(String headwordXPath) {
        Regex restrict = new OroRegex("\\[[^[]*\\]", "");
        headwordXPath = restrict.doReplace(headwordXPath);        
        StringTokenizer tokenizer = new StringTokenizer(headwordXPath, "\\/");
    	while (tokenizer.hasMoreTokens()) {
    	    String nextToken = tokenizer.nextToken();
    	    int compare = nextToken.compareTo(entryElement);
    		
    	    if (compare == 0) { break; }
    	    if (entryElement != null) {
    	        headwordXPathTokens.addElement(nextToken);
            }
        }
    }
    

    public Document setup() throws Exception {
        return setup(null);
    }

    public Document setup(Writer writer) throws Exception {
        doc = new CoreDocumentImpl();
        //parser.reset();  // reset it so it can safely be reused
        int len = headwordXPathTokens.size();
        for (int i = 0; i < len; i++) {
            values[i] = doc.createElement((String)headwordXPathTokens.elementAt(i));
            if (i == 0) {
                doc.appendChild(values[i]);
            } else {
                values[i-1].appendChild(values[i]);
            }
        }        	
        entryDepth = len - 1;
        out = writer;

        if (out != null) {
            try {
            	out.write(DictionaryCache.XML_HEADER);
                for (int i = 0; i < len; i++) 
                	out.write("<" + headwordXPathTokens.elementAt(i) + ">\n");
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        return doc;
    }


    public Document parseElement(InputStream is,
                            Document myDoc) throws Exception {
        return parseElement(is, myDoc, null);
    }

    // private boolean firstTime = true;

    public Document parseElement(InputStream is,
                            Document myDoc, Writer writer) throws Exception {
        doc = myDoc;
        out = writer;
        parser.start();

        if (Dbg.PARSE) {
            Dbg.print("XmlMini: Parsing using " + is + " writing " + writer);
        }
        entryDepth = headwordXPathTokens.size() - 1;
        // String head="<!DOCTYPE ENTRY [\n "+
        //    "  <!ENTITY aacute  \"&#225;\">\n"+
        //    "  <!ENTITY agrave \"&#224;\">\n" +
        //    "  <!ENTITY copy   \"&#169;\">\n"+
        //    "]>\n";

        parser.start();

        entryDepth = headwordXPathTokens.size() - 1;

        if (Dbg.PARSE) {
            Dbg.print("XmlMini: Parsing dtd using " + is + " writing " + writer);
        }

        //This is a hack, but I couldnt get it to work any other way.
        //The first time, read in the header along with the first element,
        //and in the future ignore the header. This makes character entities
        //work happily.
        // if (firstTime) {
        //    SequenceInputStream ss=new SequenceInputStream(new StringBufferInputStream(head),is);
        //    parser.parse(new InputSource(ss));
        //    firstTime=false;
        //} else {
        parser.parse(new InputSource(is));
        //}

        if (out != null) {
            out.flush();
            out = null;
        }

        return doc;
    }


    public Document finish(Document myDoc) throws Exception {
        return finish(myDoc, null);
    }

    public Document finish(Document myDoc, Writer writer) throws Exception {
        out = writer;
        if (out != null) {
            try {
            	int len = headwordXPathTokens.size();
                for (int i = len - 1; i >= 0; i--) 
                	out.write("</" + headwordXPathTokens.elementAt(i) + ">\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
            out.flush();
            out = null;
        }
        return myDoc;
    }

    //--- Functions to implement the ContentHandler interface ---

    @Override
    public void startDocument ()
    {
        if (Dbg.PARSE) Dbg.linePrint(entryDepth, "Start document");
    }

    @Override
    public void endDocument ()
    {
        if (Dbg.PARSE) Dbg.linePrint(entryDepth, "End document");
    }

    @Override
    public void startElement(String uri, String name, String raw,
                             Attributes attrs) {
        entryDepth++;
        if (Dbg.ERROR && entryDepth == MAXDEPTH) {
            Dbg.print("XmlMini: Help!  XML nesting depth exceeded!");
        }
        if (Dbg.PARSE) Dbg.linePrint(entryDepth, "Start element"+entryDepth+
                                     ": name=" + name);
        values[entryDepth] = doc.createElement(name);
        if (attrs != null && attrs.getLength() > 0) {
            for (int i=0; i<attrs.getLength(); i++) {
                String s_key = attrs.getLocalName(i);
                String s_value = attrs.getValue(i);
                values[entryDepth].setAttribute(s_key, s_value);
                if (Dbg.PARSE) {
                    Dbg.linePrint(entryDepth, "Attribute "+entryDepth+
                                  ":  name=" + s_key +
                                  ", value=" + s_value);
                }
            }
        }
        if (out!=null)
        try {
            out.write('<');
            out.write(raw);
            if (attrs != null) {
                //attrs = sortAttributes(attrs);
                int len = attrs.getLength();
                for (int i = 0; i < len; i++) {
                    out.write(' ');
                    out.write(attrs.getQName(i));
                    out.write("=\"");
                    out.write(normalize(attrs.getValue(i)));
                    out.write('"');
                }
            }
            out.write('>');
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /** Normalizes the given string. */
    private static String normalize(String s) {
        StringBuilder str = new StringBuilder();

        int len = (s != null) ? s.length() : 0;
        for (int i = 0; i < len; i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '<': {
                    str.append("&lt;");
                    break;
                }
                case '>': {
                    str.append("&gt;");
                    break;
                }
                case '&': {
                    str.append("&amp;");
                    break;
                }
                case '"': {
                    str.append("&quot;");
                    break;
                }
                case '\r':
                    /* case '\n': {
                    str.append("&#");
                    str.append(Integer.toString(ch));
                    str.append(';');
                    break;
                    // else, default append char
                    }*/
                default: {
                    str.append(ch);
                }
            }
        }
        return str.toString();
    } // normalize(String):String


    @Override
    public void endElement(String uri, String name, String raw) {
        if (Dbg.PARSE) Dbg.linePrint(entryDepth, "End element:  " + name);

        values[entryDepth-1].appendChild(values[entryDepth]);
        values[entryDepth] = null; // no wasted memory
        entryDepth--;

        if (name.equals(entryElement)) {
            parser.stop();
            //once the parser stops, this program will now start at the point
            //just after the "parser.parse(InputSource)" call above
        }
        if (out != null) {
            try {
                out.write("</");
                out.write(raw);
                out.write('>');
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void characters(char[] ch, int start, int length)
    {
        if (Dbg.PARSE) Dbg.linePrint(entryDepth, "Character data:  \"" + escape(ch, start, length) + '"');
        String theGoods = String.valueOf(ch, start, length);
        values[entryDepth].appendChild(doc.createTextNode(theGoods));
        if (out != null) {
            try {
                out.write(normalize(theGoods));
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void ignorableWhitespace (char[] ch, int start, int length)
    {
        if (Dbg.PARSE) Dbg.linePrint(entryDepth, "Ignorable whitespace:  \"" + escape(ch, start, length) + '"');
        characters(ch, start, length);
    }


    /** Returns a string of the location. */
    private String getLocationString(SAXParseException ex) {
        StringBuffer str = new StringBuffer();

        String systemId = ex.getSystemId();
        if (systemId != null) {
            int index = systemId.lastIndexOf('/');
            if (index != -1)
                systemId = systemId.substring(index + 1);
            str.append(systemId);
        }
        str.append(':');
        str.append(ex.getLineNumber());
        str.append(':');
        str.append(ex.getColumnNumber());

        return str.toString();

    } // getLocationString(SAXParseException):String


    @Override
    public void processingInstruction (String target, String data)
    {
        if (Dbg.PARSE) Dbg.linePrint(entryDepth, "Processing Instruction: "
                + target + ' ' + escape(data.toCharArray(),0,data.length()));

        if (out!=null) {
            try {
                out.write("<?");
                out.write(target);
                if (data != null && data.length() > 0) {
                    out.write(' ');
                    out.write(data);
                }
                out.write("?>");
            } catch(Exception e) { e.printStackTrace(); }
        }
    }

    //---- Functions to implement the ErrorHandler interface ---

    private boolean seenError = false;

    /** Warning. */
    @Override
    public void warning(SAXParseException ex) {
    	if ( ! seenError) {
                seenError = true;
                JOptionPane.showMessageDialog(null,
                                "[Warning] "+
                    getLocationString(ex)+": "+
                    ex.getMessage(),
                    "XML Validation Warning",
                    JOptionPane.WARNING_MESSAGE);
            }

        if (Dbg.ERROR)
            System.err.println("[Warning] "+
                               getLocationString(ex)+": "+
                               ex.getMessage());
    }

    /** Error. */
    @Override
    public void error(SAXParseException ex) {
        if ( ! seenError) {
            seenError = true;
            JOptionPane.showMessageDialog(null,
                            "[Error] "+
                               getLocationString(ex)+": "+
                               ex.getMessage(),
                "XML Validation Error",
                JOptionPane.ERROR_MESSAGE);
        }
        if (Dbg.ERROR)
            System.err.println("[Error] "+
                               getLocationString(ex)+": "+
                               ex.getMessage());
        //if (Dbg.PARSE) Dbg.linePrint(entryDepth, "FATAL ERROR: " + message);
        //if (Dbg.PARSE) Dbg.linePrint(entryDepth, "  at " + url + ": line " + line + " column " + column);
        //throw ex;
    }

    /** Fatal error. */
    @Override
    public void fatalError(SAXParseException ex) {
        if (Dbg.ERROR) {
            System.err.println("[Fatal Error] "+
                    getLocationString(ex)+": "+
                    ex.getMessage());
            if ( ! seenError) {
                ex.printStackTrace();
            }
        //throw ex;
        }
        if ( ! seenError) {
            seenError = true;
            JOptionPane.showMessageDialog(null,
                            "[Fatal Error] "+
                               getLocationString(ex)+": "+
                               ex.getMessage(),
                "XML Validation Fatal Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    //////////////////////////////////////////////////////////////////////
    // General utility methods.
    //////////////////////////////////////////////////////////////////////

    /**
     * Escape a string for printing.
     */
    private static String escape (char[] ch, int start, int length) {
        StringBuffer out = new StringBuffer();
        for (int i = start; i < start + length; i++) {
            switch (ch[i]) {
            case '\\':
                out.append("\\\\");
                break;
            case '\n':
                out.append("\\n");
                break;
            case '\t':
                out.append("\\t");
                break;
            case '\r':
                out.append("\\r");
                break;
            case '\f':
                out.append("\\f");
                break;
            default:
                out.append(ch[i]);
                break;
            }
        }
        return out.toString();
    }

} // end of XmlMiniDocument.java

