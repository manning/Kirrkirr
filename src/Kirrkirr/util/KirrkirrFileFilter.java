package Kirrkirr.util;

import java.io.*;
import javax.swing.filechooser.FileFilter;

/** The <code>KirrkirrFileFilter</code> object
 *  is a convenient place to put file filter objects.
 *  Currently, can check for xml, temporary html,
 *  or user profile (plain, dat, xml, or csh) files.
 *
 *  @version     1.0, 10/03/2000
 *  @author      Kevin Jansz
 *  @see Kirrkirr
 */
public class KirrkirrFileFilter extends FileFilter implements FilenameFilter {

    public static final short XML_ENTRY = 0;
    public static final short HTML_ENTRY = 1;
    public static final short USER_ENTRY = 2;
    public static final short PLAIN_ENTRY = 3;
    public static final short XSL_ENTRY = 4;
    public static final short CLK_ENTRY = 5;
    public static final short ANY_ENTRY = 6;
    public static final short AUDIO_ENTRY = 7;
    public static final short PROP_ENTRY = 8;
    public static final short IMAGE_ENTRY = 9;
    public static final short TYPES = 10;     // make one larger than biggest

    //string constants that need to be translated
    private static final String[] SC_DESC = { "Xml_files", "Html_files",
					    "Profile_files", "Profile_files",
					    "Xsl_files", "Clk_files", "All_files",
                                            "Sound_files",
                                            "Properties_files", "Image_files"};

    private static final String HTML_EXT = "html";
    private static final String HTM_EXT = "htm";
    private static final String XML_EXT = "xml";
    private static final String CACHE_EXT = "csh";
    private static final String DAT_EXT = "dat";
    private static final String XSL_EXT = "xsl";
    private static final String CLK_EXT = "clk";
    private static final String PROP_EXT = "properties";
    private static final String JPEG_EXT = "jpeg";
    private static final String JPG_EXT = "jpg";
    private static final String GIF_EXT = "gif";
    private static final String AU_EXT = "au";
    private static final String WAV_EXT = "wav";

    private short filter_type;

    public String toString() {
        return "KirrkirrFileFilter on " + getDescription() +
                " (" + filter_type + ")";
    }

    /** Pass in one of the ENTRY constants (XML_ENTRY,
     *  HTML_ENTRY or USER_ENTRY) and create a new
     *  filter of that type.
     */
    public KirrkirrFileFilter(short filter_type) {
	this.filter_type = filter_type;
    }

    /** Return the description of this file type (internationalized) */
    @Override
    public String getDescription() {
	if (filter_type >= 0 && filter_type < TYPES) {
	    return Helper.getTranslation(SC_DESC[filter_type]);
	} else {
	    return null;
	}
    }

    /** Returns whether the file
     *  is a valid type of file for this filter.
     */
    @Override
    public boolean accept(File file) {
	// always allow directories, so they can navigate...
	if (file.isDirectory()) {
	    return true;
	}
	String parent = file.getParent();
	if (parent==null) {
	    if (Dbg.ERROR) {
		Dbg.print("KirrkirrFileFilter: Null parent file.");
            }
            return false;
	}
	return accept(new File(parent),file.getName());
    }

    /** Returns whether the file in the directory
     *  is a valid type of file for this filter.
     */
    @Override
    public boolean accept(File dir, String name) {
	//System.out.println("checking: "+name);
        if (filter_type == ANY_ENTRY) {
            return true;
        } else if (filter_type == HTML_ENTRY) {
	    return isHtmlEntry(name);
	} else if (filter_type == XML_ENTRY) {
	    return isXmlEntry(name);
	} else if (filter_type == USER_ENTRY) {
	    return isUserEntry(name);
	} else if (filter_type == PLAIN_ENTRY) {
	    return isPlainEntry(name);
	} else if (filter_type == XSL_ENTRY) {
	    return isXslEntry(name);
	} else if (filter_type == CLK_ENTRY) {
	    return isClkEntry(name);
        } else if (filter_type == AUDIO_ENTRY) {
            return isAudioEntry(name);
        } else if (filter_type == PROP_ENTRY) {
	    return isPropEntry(name);
	} else if (filter_type == IMAGE_ENTRY) {
	    return isImageEntry(name);
	}
        return false;
    }

    /** Checks for temporary html files - those
     *  that start with "_" and end with .htm*.
     */
    private static boolean isHtmlEntry(String name) {
	String extension = getExtension(name);
	if (extension != null) {
            if ((extension.equals(HTML_EXT) || extension.equals(HTM_EXT))
		&& name.startsWith("_")) {
		//		&& name.startsWith("@")) {
		return(true);
	    }
	}
        return(false);
    }

    /** Checks whether the file extension is .xml.
     */
    private static boolean isXmlEntry(String name) {
	String extension = getExtension(name);
	if (extension != null) {
            if (extension.equals(XML_EXT)) {
		return(true);
	    }
	}
        return false;
    }


    /** Checks whether the file extension is .xsl.
     */
    private static boolean isXslEntry(String name) {
	String extension = getExtension(name);
	if (extension != null) {
            if (extension.equals(XSL_EXT)) {
		return(true);
	    }
	}
        return false;
    }

    private static boolean isClkEntry(String name) {
	String extension = getExtension(name);
	if (extension != null) {
            if (extension.equals(CLK_EXT)) {
		return(true);
	    }
	}
        return(false);
    }

    private static boolean isPropEntry(String name) {
    	String extension = getExtension(name);
    	if (extension != null) {
    		if (extension.equals(PROP_EXT)) {
    			return true;
    		}
    	}
    	return false;
    }


    /** Returns true for xml, cache (csh) and dat
     *  file extensions, as well as for files
     *  which have no extensions.
     */
    private static boolean isUserEntry(String name) {
        String extension = getExtension(name);
        if (extension == null) {
            return(true);
        } else if (extension.equals(XML_EXT)) {
            return(true);
        } else if (extension.equals(CACHE_EXT)) {
            return(true);
        } else if (extension.equals(DAT_EXT)) {
            return(true);
        }
        return false;
    }

    /** Returns true for jpg, jpeg, and gif pictures.
     */
    public static boolean isImageEntry(String name) {
        return isImageExtension(getExtension(name));
    }

    public static boolean isImageExtension(String extension) {
        return extension.equals(JPEG_EXT) || extension.equals(JPG_EXT) ||
                extension.equals(GIF_EXT);
    }

    /** Returns true for au, wav audio files.
     */
    public static boolean isAudioEntry(String name) {
        return isAudioExtension(getExtension(name));
    }

    public static boolean isAudioExtension(String extension) {
        return extension.equals(AU_EXT) || extension.equals(WAV_EXT);
    }

    /** Returns true for files
     *  which have no extensions.
     */
    private static boolean isPlainEntry(String name) {
	String extension = getExtension(name);
	//System.err.println("got "+name+" with "+extension+".");
        return "".equals(extension);
    }

    /** Get the extension of a file.  Works case insensitively.
     *  This method will always return a lowercase extension.
     *  It will return the empty String ("") if there is no
     *  extension. It never returns null.
     *
     *  @param name A filename or path name
     *  @return The file extension or an empty String if none.
     */
    public static String getExtension(String name) {
	String ext = "";
	int i = name.lastIndexOf('.');

        if (i > 0 &&  i < name.length() - 1) {
            ext = name.substring(i+1).toLowerCase();
        }
        return ext;
    }

}

