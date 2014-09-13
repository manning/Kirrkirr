package Kirrkirr.util;

import java.io.*;

import java.net.URL;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

/** Adapted from xalan example samples/SimpleTransform/SimpleTransform.java.
 *  Uses the xalan libraries to do XSLT transformations on the XML, in order
 *  to make the HTML files to display.
 *  Note that at present this class is static, and cannot be called
 *  concurrently by several modules.  This is presently okay, as only
 *  DictionaryCache calls it (assuming DictionaryCache synchronises properly).
 */
public class XslDriver {

    private static Transformer transformer = null;
    private static String curStylesheet = null; // caches last used stylesheet

    /**
     *  Use the TraX (Transformation API for XML) interface to perform a
     *  transformation in the simplest manner possible
     *  (3 statements).
     *  @param args Command line arguments: soureFile stylesheet result
     */
    public static void main(String[] args)
    {
        if (args.length != 3) {
            System.err.println("usage: XslDriver xml-source xsl-stylesheet result");
            System.exit(1);
        }
        // now call our own routine
        makeHtml(args[0], args[1], args[2]);
    }


  /** Make an html file with name <code>outFileName</code> from XML file
   *  <code>xmlFileName</code> via the XSL stylesheet <code>stylesheet</code>.
   *  This only re-reads the xsl stylesheet
   *  when the xsl filename has changed, or if it was unsuccessful last time.
   *  CDM Nov 2001: improve over the main() implementation taken from
   *  SimpleTransform.java so that files are closed afterwards.
   *  @return whether this was successful (true) or not
   *  @param xmlFileName fully qualified filename of xml file to read from
   *  @param stylesheetURI fully qualified URI of xsl stylesheet to use
   *  @param outFileName fully qualified filename of (html) file to output
   */
  public static boolean makeHtml(String xmlFileName, String stylesheetURI,
                                                   String outFileName) {
      if (Dbg.FILE) {
          Dbg.print("XslDriver: " + xmlFileName + " -> " + stylesheetURI +
                    " -> " + outFileName);
      }
      try {
          if (transformer == null ||
              (curStylesheet!=null && ! curStylesheet.equals(stylesheetURI))) {
              // Use the static TransformerFactory.newInstance() method to
              // instantiate a TransformerFactory. The
              // javax.xml.transform.TransformerFactory system property
              // setting determines the actual class to instantiate --
              // org.apache.xalan.transformer.TransformerImpl.
              TransformerFactory tFactory = TransformerFactory.newInstance();

              // Use the TransformerFactory to instantiate a Transformer that
              // will work with the stylesheet you specify. This method call
              // also processes the stylesheet into a compiled Templates
              // object.
              // We should close stylesheet, but would need to get inside URI.
              InputStream is = new URL(stylesheetURI).openConnection().getInputStream();
              transformer = tFactory.newTransformer(new StreamSource(is));
              is.close();
              // transformer = tFactory.newTransformer(
              //                           new StreamSource(stylesheetURI));
              curStylesheet = stylesheetURI;
          }

          // Use the Transformer to apply the associated Templates object to
          // an XML document and write the output to a file
          OutputStream os = new BufferedOutputStream(
                                         new FileOutputStream(outFileName));
          InputStream is = new BufferedInputStream(
                                       new FileInputStream(xmlFileName));
          if (true) {
              // let XML code deal with charset encoding issues
              transformer.transform(new StreamSource(is),
                                    new StreamResult(os));
              is.close();
              os.close();
          } else {
              // Deal with charset encoding ourselves based on RelFile.ENCODING
              OutputStreamWriter osw = new OutputStreamWriter(os, RelFile.ENCODING);
              InputStreamReader isr = new InputStreamReader(is, RelFile.ENCODING);
              transformer.transform(new StreamSource(isr),
                                    new StreamResult(osw));
              isr.close();
              osw.close();
          }
      } catch (Exception e) {
          if (Dbg.ERROR) {
              e.printStackTrace();
          }
          return false;
      }
      return true;
  }

}

