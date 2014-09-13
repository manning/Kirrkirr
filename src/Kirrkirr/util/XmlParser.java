package Kirrkirr.util;

import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.*;
import java.io.*;

/**
 * This overrides the SAXParser in the apache xerces
 * package so that it can be stopped mid-parse. That way, given an
 * input stream that starts in the middle of the xml document,
 * it can stop when it finishes reading the current document
 * fragment. (Otherwise it crashes, complaining that it has found
 * more xml tags after the end root tag). The ContentHandler needs
 * to call start() and stop() when appropriate (in this case, it
 * is XmlMiniDocument).
 * @see org.apache.xerces.parsers.SAXParser
 */
public class XmlParser extends SAXParser{

    private boolean stopped; // = false;

    public XmlParser() {
	super();
    }

    public void stop() {
	stopped=true;
	fParseInProgress=false;
    }

    public void start() {
	stopped=false;
    }

    /** Copied from superclass org.apache.xerces.framework.XMLParser, in order
     *  to make it stoppable. (So if xerces version is increased,
     *  should re-do this - current version is 1.4.3).
     *  @see org.apache.xerces.framework.XMLParser#parse(InputSource)
     */
    public void parse(InputSource source)
	throws SAXException,
	       java.io.IOException{
	if (fParseInProgress) {
            throw new org.xml.sax.SAXException("FWK005 parse may not be called while parsing."); // REVISIT - need to add new error message
        }

        try {
            if (parseSomeSetup(source)) {
		while(!stopped && !fScanner.atEndOfInput())
		    fScanner.parseSome(false);
            }else Dbg.print("didnt setup");
	    if (!fScanner.atEndOfInput())
		super.endDocument();
        } catch (org.xml.sax.SAXException ex) {
	    if (Dbg.ERROR) {
		ex.printStackTrace();
	    }
            throw ex;
        } catch (IOException ex) {
	    if (Dbg.ERROR)
                ex.printStackTrace();
            throw ex;
        } catch (Exception ex) {
	    if (Dbg.ERROR) {
                ex.printStackTrace();
	    }
            throw new org.xml.sax.SAXException(ex);
        }
        finally {
            fParseInProgress = false;
        }
    }


    public void startDTD(org.apache.xerces.utils.QName rootElement, int publicId, int systemId) throws Exception{
	super.startDTD(rootElement,publicId,systemId);
//	Dbg.print("at least we started it");
    }

}

