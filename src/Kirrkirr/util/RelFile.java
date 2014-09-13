package Kirrkirr.util;

import Kirrkirr.Kirrkirr;

import java.net.*;
import java.io.*;
import java.lang.reflect.*;

import javax.swing.*;
import java.applet.*;

/** This is a small static class that is just meant to clean up
 *  file access in older code.  The idea is that it gets at files
 *  relative to and below the code base, either as URL or File.
 *  The Init method sets up the parameters.
 *  The rest looks in or down up to two levels below the codebase.
 *  The first level corresponds to language and the second to file type
 *
 *  OS specific: if (System.getProperty("os.name").startsWith("Windows"))
 *
 *  RelFile.java -- (c) christopher manning -- may 1999
 *  10/9/99 - kjansz add the write-file interface
 */
public final class RelFile
{
    //static string constants that need to be translated
    //these three public because accessed in DirsOptionPanel
    public static final String SELECT_TEMP_DIR =
        "Select Temp Directory";
    //public static final String TEMP_DIR = "Temp_Directory";
    public static final String SELECT = "Select";

    /** The character encoding used on created files */
    public static final String ENCODING = "UTF-8";

    static private String lineSeparator;
    static private String fileSeparator; // = null;
    static private boolean isApplet; // = false;
    static private String codeBaseStr; // = null;
    static private URL codeBase; // = null;
    static public String WRITE_DIRECTORY; // = null;
    static private boolean applAudio; // = false;
                // this is true if application is capable of playing audio
    static private String[] htmlCreators = {"MOSS","MSIE"};//MOSS is netscape; MSIE is internet explorer.
    static private String htmlCreator=null;

    //read in from properties file, or user-prompted,
    //*required* to access dictionary and other dictionary-specific
    //files (images, sounds, etc.). Perhaps should be in Kirrkirr?
    public static String dictionaryDir;

    private RelFile() {} // static class

    /** Init is used to initialize where the codebase is and whether
     * one has an applet.  It should be called before using other things.
     * This version called from the applet with the codebase URL.
     * Also sets up line separator, file separator, the temporary/writeable
     * directory and whether the system can play audio. For macintoshes,
     * sets the application used for opening html files (if it can
     * be found).
     * @param codeBase the URL representing the codebase/Kirrkirr home.
     */
    public static void Init(URL codeBase)
    {
        isApplet = true;
        fileSeparator = "/";
        lineSeparator = System.getProperty("line.separator");
        RelFile.codeBase = codeBase;
        if (Dbg.FILE) {
            Dbg.print("RelFile.Init: running as applet");
	    dumpSystemInfo();
        }

        // --- Java 1.2 only
        //try {
        //    File tmpFile = File.createTempFile("kirrkirr","tmp");
        //    Dbg.print("tmp:" + tmpFile.getAbsolutePath());
        //} catch (Exception e) {
        //    Dbg.print(e);
        //}
        //WRITE_DIRECTORY = getPath(tmpFile);
        // ---
        WRITE_DIRECTORY = codeBase.getFile(); // was "TEMP";

        if (WRITE_DIRECTORY.charAt(WRITE_DIRECTORY.length()-1)=='/'){
            WRITE_DIRECTORY=WRITE_DIRECTORY.substring(0,WRITE_DIRECTORY.length()-1);
        }

        /*if (RelFile.codeBase.charAt(RelFile.codeBase.length()-1)=='/'){
            RelFile.codeBase=RelFile.codeBase.substring(0,RelFile.codeBase.length()-1);
            }*/
        String javaVersion = System.getProperty("java.version");
        applAudio = (javaVersion.compareTo("1.2") >= 0);
        setHtmlCreator();
    }


    /** Init is used to initialize where the codebase is and whether
     * one has an applet.  It should be called before using other things.
     * This version is called when it is not an applet.
     * Also sets up line separator, file separator, the temporary/writeable
     * directory and whether the system can play audio. For macintoshes,
     * sets the application used for opening html files (if it can
     * be found).
     * @param directory the string of the user's codebase/Kirrkirr directory.
     */
    public static void Init(String directory)
    {
        isApplet = false;
        fileSeparator = System.getProperty("file.separator");
        lineSeparator = System.getProperty("line.separator");
        if (directory.equals("")) {
            codeBaseStr = System.getProperty("user.dir");
        } else {
            codeBaseStr = directory;
        }
        WRITE_DIRECTORY = codeBaseStr;
        if (Dbg.FILE) {
            Dbg.print("RelFile.Init: running as an application");
	    dumpSystemInfo();
        }

        // Only a Java VM 1.2 or higher is capable of playing audio clips
        // (can't convert 1.1.8 to decimal, so string comparison is used
        String javaVersion = System.getProperty("java.version");
        applAudio = (javaVersion.compareTo("1.2") >= 0);
        setHtmlCreator();
    }


    /**
     *  @return Whether the directory exists (if a file -- always true if
     *  it is a URL)
     */
    public static boolean setDictionaryDir(String dir) {
        try {
            dictionaryDir = dir;
            //check whether dir exists
            if (isUrl(dir)) {
                return true;
            } else {
                File f = new File(dir);
		if (Dbg.FILE) {
		    Dbg.print("Does " + f + " exist: " + f.exists());
		}
                return f.exists();
            }
        } catch (Exception e) {
            Dbg.print("setDictionaryDir: not valid name: " + dir);
            e.printStackTrace();
            return false;
        }
    }


    /**
     * @return the line separator for the current machine
     */
    public static String lineSeparator() {
        return lineSeparator;
    }

    /**
     * @return the file separator for the current machine
     */
    public static String fileSeparator() {
        return fileSeparator;
    }

    /**
     * @return the url separator ("/")
     */
    public static String urlSeparator() {
        return "/";
    }


    public static boolean isUrl(String s) {
        return s.trim().toLowerCase().startsWith("http://");
    }


    /** Returns the filename only from the fully qualified
     *  pathname or URL passed in.
     *  @return the filename (without folder, subfolder or base)
     *  from the fully qualified pathname or URL passed in
     */
    public static String getFile(final String u) {
        String file = u;
        // file = u.getPath(); // strip file: or http: stuff - only in Java 1.3

        String sep = RelFile.urlSeparator();
        int s = file.lastIndexOf(sep);
        if (s == -1) {
            // the urls returned can have \ in them as separator in Windows
            // this seems like a bug to me (not converting file path to
            // url properly), but could just mean things should be done
            // subtly differently in RelFile.  At any rate, this fixes it....
            sep = System.getProperty("file.separator");
            s = file.lastIndexOf(sep);
        }
        if (s == -1) {
            // one may have no slashes but still a file: protocol...
            sep = ":";
            s = file.lastIndexOf(sep);
        }
        // Dbg.print("link "+file+" sep "+sep+" s "+s+" u "+u.getFile());

         if (s != -1) {
            file = file.substring(s+sep.length());
        }
        return (file);
    }


    /** Returns the path without the file from the fully qualified
     *  pathname or URL passed in.
     *  @param u A complete file path
     *  @return the path (without the last filename)
     *    from the fully qualified pathname or URL passed in
     */
    public static String getPath(String u) {
        String dir = u;
        int s = Math.max(dir.lastIndexOf(RelFile.urlSeparator()),
                         dir.lastIndexOf(':'));

        if (s != -1) {
            dir = dir.substring(0, s);
        }
        return dir;
    }

    /** Creates a string representing the path to the file, and puts the
     *  string in the StringBuffer passed in. The order is the order
     *  passed in, ie:
     *  base+fileseparator+folder+fileseparator+subfolder+fileseparator+file
     *  If any of the arguments are null, it skips adding them and the next
     *  fileseparator.
     */
    private static void FillStringBuffer(StringBuffer sb, String base,
                                  String folder, String subfolder, String file)
    {
        if (base != null && ! base.equals("")) {
            sb.append(base);
            sb.append(fileSeparator);
        }
        if (folder != null && ! folder.equals("")) {
            sb.append(folder);
            sb.append(fileSeparator);
        }
        if (subfolder != null && ! subfolder.equals("")) {
            sb.append(subfolder);
            sb.append(fileSeparator);
        }
        sb.append(file);
        if (Dbg.FILE) {
            Dbg.print("RelFile: " + sb + " b " + base + " f " + folder +
                      " s " + subfolder + " f " + file);
            if (file == null) {
                Exception e = new Exception("Dummy to get stacktrace!");
                e.printStackTrace();
            }
        }
    }

    /** Get a string representation of a filename specified relative to
     *  to the code base.
     *  @return the string representing the filename (including the folder
     *  and subfolder) specified relative to the code base, or the null if
     *  the file URL can't be formed (if applet)
     */
    public static String MakeFileName(String folder, String subfolder,
                                      String file) {
        StringBuffer sb = new StringBuffer(80);

        FillStringBuffer(sb, codeBaseStr, folder, subfolder, file);
        String str = sb.toString();
        if (isApplet) {
            URL url;
            try {
                url = new URL(codeBase, str);
            } catch (MalformedURLException e) {
                if (Dbg.ERROR) Dbg.print("Couldn't create " + str + ": badly specified URL");
                return null;
            }
            return url.getFile();
        } else {
            return str;
        }
    }


    /**
     * Get a string representation of a filename specified in the subfolder specified,
     * relative to to the code base.
     * @return the string representing the filename (including the subfolder)
     * specified relative to the code base, or the null if the file URL
     * can't be formed (if applet)
     */
    public static String MakeFileName(String subfolder, String file)
    {
        return MakeFileName(null, subfolder, file);
    }

    /** Get a string representation of a filename specified in the subfolder
     *  specified,
     *  relative to to the user's temporary (writeable) directory.
     *  @return the string representing the filename (including the subfolder)
     *          specified relative to the temporary (writeable) directory
     */
    public static String MakeWriteFileName(String subfolder, String file)
    {
        StringBuffer sb = new StringBuffer(80);  // initial size; starts empty

        FillStringBuffer(sb, WRITE_DIRECTORY, null, subfolder, file);
        if (Dbg.FILE) {
            Dbg.print("Made write file is |" + sb.toString() + '|');
            Dbg.print("fileSeparator is " + fileSeparator +
                      " WRITE_DIRECTORY is " + WRITE_DIRECTORY);
        }
        return sb.toString();
    }


    /** Get a URL representation of a filename specified relative to
     *  to the code base.
     *  @return the URL representing the filename (including the folder and
     *      subfolder) specified relative to the code base, or the null if
     *      the file URL can't be formed
     */
    public static URL makeURL(String folder, String subfolder, String file) {
        StringBuffer sb = new StringBuffer(80);  // initial size; starts empty
        URL url;

        FillStringBuffer(sb, codeBaseStr, folder, subfolder, file);
        try {
            if (isApplet)
                {
                    url = new URL(codeBase, sb.toString());
                }
            else
                {
                    url = new URL("file", "", sb.toString());
                    // cdm: if 2nd argument was null in URL constructor,
                    // under Unix run as an application, one got
                    // NullPointerExceptions elsewhere in the program when
                    // url was used.  Making it empty string seems to
                    // fix things!  (Using null worked fine on Windows....)
                }
        } catch (MalformedURLException e) {
            if (Dbg.ERROR) Dbg.print("Couldn't create " +
                                    sb.toString() + ": badly specified URL");
            return null;
        }
        if (Dbg.FILE) Dbg.print("Made URL is |" + url + '|');
        return url;
    }

    /**
     * Get a URL representation of a filename specified relative to
     * to the code base.
     * @return the URL representing the filename (including the subfolder)
     * specified relative to the code base, or the null if the file URL
     * can't be formed
     */
    public static URL makeURL(String subfolder, String file) {
        return makeURL(null, subfolder, file);
    }

   /**
     * Get a URL representation of a filename specified relative to
     * to the code base.
     * @return the URL representing the filename specified relative to the
     * code base, or the null if the file URL can't be formed
     */
    public static URL makeURL(String file) {
        return makeURL(null, null, file);
    }

    /**
     * Get a URL representation of a filename specified in the subfolder specified,
     * relative to to the user's temporary (writeable) directory.
     * @return the URL representing the filename (including the subfolder)
     * specified relative to the temporary (writeable) directory, or the null
     * if the file URL can't be formed
     */
    public static URL MakeWriteURL(String subfolder, String file) {
        StringBuffer sb = new StringBuffer(80);  // inital size only; starts empty
        URL url;

        FillStringBuffer(sb, WRITE_DIRECTORY, null, subfolder, file);
        try {
            url = new URL("file", "", sb.toString());
            // cdm: if 2nd argument was null in URL constructor, under Unix run
            // as an application, one got NullPointerExceptions elsewhere in
            // the program when url was used.  Making it empty string seems to
            // fix things!  (Using null worked fine on Windows....)
        } catch (MalformedURLException e) {
            if (Dbg.ERROR) Dbg.print("Couldn't create " + sb.toString() + ": badly specified URL");
            return null;
        }
        return url;
    }

    /**
     * Get a String representation of the URL of the filename specified, in
     * the folder and subfolder specified, relative to to the code base.
     * Known issue: the Sun JDK code (all tested versions)
     * doesn't return RFC 1738/2396 compatible URIs.  This would be okay
     * except for the fact that the JDK1.1 HtmlEditorPane requires such URIs
     * in IMG specifications (later versions of JDK fix this).  So IMGs
     * don't appear right in generated HTML under JDK1.1 under Windows
     * or under Firefox. (Other platforms okay, I think?)
     * We now try to fix this manually with String fiddling.
     * <p>
     * These are the kinds of formats there should be:<p>
     * file:///home/someuser/somedir   file:///C:/Documents and Settings
     * file:////somehost/someshare/afile.txt  /home/someuser/somedir
     * c:\program files\some dir   c:/program files/some dir
     * <p>Reflecting Unix and Windows with windows by drive or UNC.
     *
     * @return the String representation of the URL of the filename
     * (including the folder and subfolder) specified relative to the
     * code base, or the null if the file URL can't be formed
     */
    public static String MakeURLString(String folder, String subfolder,
                                       String file) {
        URL url= makeURL(folder, subfolder, file);
        if (url != null) {
            String urlStr = url.toString();
            // fix ugly Sun/MS Windows brokenness of the URLS produced
            // this is needed to get RFC-compliant URIs that work in Firefox
            if (urlStr.startsWith("file:") && ! urlStr.startsWith("file:/")) {
                urlStr = urlStr.substring(0, 5) + "///" + urlStr.substring(5);
                urlStr = urlStr.replace('\\', '/');
                if (Dbg.HTML_PANEL) Dbg.print("Changed urlStr to |" + urlStr + '|');
            }
            return urlStr;
        }
        if (Dbg.ERROR) Dbg.print("RelFile:MakeURLString(folder,subfolder,file): null url"+folder+ ' ' +subfolder+ ' ' +file);
        return null;
    }

    /**
     * Get a String representation of the URL of the filename specified, in
     * the subfolder specified, relative to to the code base.
     * @return the String representation of the URL of the filename
     * (including the subfolder) specified relative to the
     * code base, or the null if the file URL can't be formed
     */
    public static String MakeURLString(String subfolder, String file)
    {
        return MakeURLString(null, subfolder, file);
    }

    /**
     * Get a String representation of the URL of the filename specified
     * relative to to the code base.
     * @return the String representation of the URL of the filename
     * specified relative to the code base, or the null if the file URL
     * can't be formed
     */
    public static String MakeURLString(String file)
    {
        return MakeURLString(null, null, file);
    }


    public static String makeAbsoluteURLString(String file) {
        URL url = null;
        try {
            url = new URL("file", "", file);
        } catch (MalformedURLException mue) {
        }
        if (url == null) {
            return null;
        } else {
            return url.toString();
        }
    }


   /**
     * Get a String representation of the URL of the filename specified
     * in the subfolder specified, relative to to the user's temporary
     * (writeable) directory.
     * @return the URL representing the filename (including the subfolder)
     * specified relative to the temporary (writeable) directory, or the null
     * if the file URL can't be formed
     */
    public static String MakeWriteURLString(String subfolder, String file)
    {
        URL url=MakeWriteURL(subfolder, file);
        if (url!=null)
            return url.toString();
        if (Dbg.ERROR) Dbg.print("RelFile:MakeWriteURLString(subfolder,file): null url"+subfolder+ ' ' +file);
        return null;
    }


    /**
     * Make an AudioClip from a file, folder and subfolder specified,
     * relative to the codebase, if the system can play audio.
     * The system can play audio if the jdk is 1.2 or higher,
     * or if it's an applet.
     * @return the AudioClip representing the audio file at the
     * filename (including the folder and subfolder) specified, relative to the code base,
     * or null if the current system can't play audio, or null if the URL couldn't
     * be formed
     */
    public static AudioClip MakeAudioClip(String folder, String subfolder,
                                          String filename)
    {
        AudioClip clip = null;
        if (applAudio) {
            // in Java 2 both applications and Applets can make audio clips (in any format) from this static method
            URL url= makeURL(folder, subfolder, filename);
            if (url==null) {
                if (Dbg.ERROR) Dbg.print("RelFile:MakeAudioClip(folder,subfolder,file): null url"+folder+ ' ' +subfolder+ ' ' +filename);
                return null;
            }
            //this won't compile under jdk1.1 - that's okay
            //must leave in for sound on jdk>1.1
            clip = Applet.newAudioClip(url);
        } else if(isApplet) {
            URL url= makeURL(folder, subfolder, filename);
            if (url==null) {
                if (Dbg.ERROR) Dbg.print("RelFile:MakeAudioClip(folder,subfolder,file): null url"+folder+ ' ' +subfolder+ ' ' +filename);
                return null;
            }
            // Less than Java 2, use the applet (if it exists)
            clip = Kirrkirr.demo.getAudioClip(url);
        }
        return(clip);
    }

    /**
     * If the system can play audio, make an AudioClip from the
     * file and subfolder specified, relative to the codebase.
     * The system can play audio if the jdk is 1.2 or higher,
     * or if it's an applet.
     * @return the AudioClip representing the audio file at the
     * filename (including the subfolder) specified, relative to the code base,
     * or the null if the current system can't play audio.
     */
    public static AudioClip makeAudioClip(String subfolder, String filename)
    {
        return MakeAudioClip(null, subfolder, filename);
    }

    public static AudioClip makeAudioClip(String filename, boolean dictSpecific)
    {
        if (dictSpecific){
            if (dictionaryDir==null)
                return null;
            return MakeAudioClip(dictionaryDir,Kirrkirr.soundFolder,filename);
        }else{
            return makeAudioClip(Kirrkirr.soundFolder, filename);
        }
    }

    /**
     * Whether or not the system can play audio. The system can play audio
     * if the jdk is 1.2 or higher, or if it's an applet.
     */
    public static boolean canMakeAudioClip()
    {
        return (applAudio || isApplet);
    }


    /**
     * Make an ImageIcon from the file, folder and subfolder specified,
     * relative to the codebase.
     * @return ImageIcon representing the image file at the
     *    filename (including the folder and subfolder) specified,
     *    relative to the code base,
     *    or the null if the URL can't be formed, or if a null filename
     *    string was passed in in the first place
     */
    public static ImageIcon makeImageIcon(String folder, String subfolder,
                                          String filename)
    {
        if (filename == null) {
            return null;
        } else if (isApplet) {
            URL url= makeURL(folder, subfolder, filename);
            if (url==null) {
                if (Dbg.ERROR) Dbg.print("RelFile:makeImageIcon(folder,subfolder,file): null url"+folder+ ' ' +subfolder+ ' ' +filename);
                return null;
            }
            return new ImageIcon(url);
        } else {
            String fullname = MakeFileName(folder,subfolder,filename);
            if (Dbg.FILE) {
              File test = new File(fullname);
              if (! test.isFile()) {
                  Dbg.print("FNF: "+fullname);
              }
            }
            return new ImageIcon(fullname);
        }
    }

    /**
     * Make an ImageIcon from the file and subfolder specified,
     * relative to the codebase.
     * @return ImageIcon representing the image file at the
     * filename (including the folder and subfolder) specified, relative to the code base,
     * or the null if the URL can't be formed
     */
    public static ImageIcon makeImageIcon(String subfolder, String filename)
    {
        return makeImageIcon(null, subfolder, filename);
    }

    /** Make an ImageIcon from the file specified.  It will be looked for in
     *  the <code>Kirrkirr.iconsFolder</code> if it isn't <code>dictSpec</code>,
     *  and in the <code>Kirrkirr.imagesFolder</code> subfolder of the current
     *  dictionary if it is
     *  @param filename The name of the image file
     *  @param dictSpecific Whether it's a dictionary (language) specific image,
     *     or a general Kirrkirr icon
     *  @return ImageIcon representing the image file at the filename
     *        (including the folder and subfolder) specified
     *        or the null if the URL can't be formed
     */
    public static ImageIcon makeImageIcon(String filename,
                                          boolean dictSpecific) {
        if (dictSpecific) {
            if (dictionaryDir == null) {
                return null;
            }
            return makeImageIcon(dictionaryDir,Kirrkirr.imagesFolder,filename);
        } else {
            return makeImageIcon(Kirrkirr.iconsFolder, filename);
        }
    }


    /** Pops up a file chooser for the user to pick a new
     *  temporary (ie writable) directory for the temp
     *  files (html, xml, users) to go in. If the user
     *  chooses a directory, sets up new temporary directories
     *  in that directory (if they dont already exist), otherwise
     *  keeps the current temp directory.
     */
    public static void editWriteDirectory()
    {
        /* wdd = new WriteDirectoryDialog();
           wdd.setVisible(true);
        */
        // on MRJ as applet you can't just call JFileChooser() -- it
        // tries to look up property.  So, we give it a directory
        try {
            JFileChooser jfc_dir = new JFileChooser(new File(WRITE_DIRECTORY));
            jfc_dir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            jfc_dir.setMultiSelectionEnabled(false);
            jfc_dir.setDialogTitle(SELECT_TEMP_DIR);
            int selected = jfc_dir.showDialog(Kirrkirr.window, SELECT);

            if (selected == JFileChooser.APPROVE_OPTION) {
                 File file = jfc_dir.getSelectedFile();       //selected directory when in DIR_ONLY mode
                 String newDir = file.toString();
                 if (Dbg.FILE) Dbg.print("RelFile write to: "+newDir);
                 setupWriteDirectoryStructure(WRITE_DIRECTORY, newDir);
                 WRITE_DIRECTORY = newDir;
            }
        } catch(Exception e) {
            if (Dbg.ERROR)
                e.printStackTrace();
        }
    }


    /** Deletes files in the temporary xml and html folders,
     *  using file filters to check for proper extensions.
     *  @see KirrkirrFileFilter
     */
    public static void cleanupWriteDirectories() {
        cleanOutDirectory(WRITE_DIRECTORY, Kirrkirr.xmlFolder, new KirrkirrFileFilter(KirrkirrFileFilter.XML_ENTRY));
        cleanOutDirectory(WRITE_DIRECTORY, Kirrkirr.htmlFolder, new KirrkirrFileFilter(KirrkirrFileFilter.HTML_ENTRY));
    }


    /** Deletes files which conform to the KirrkirrFileFilter passed in
     *  which are in the subfolder/base passed in.
     *  Returns silently if directory doesn't exist etc.
     */
    public static void cleanOutDirectory(String baseDir, String subDir,
                                         KirrkirrFileFilter filter) {
        File sub;
        if(baseDir.equals("")) {
            //using abstract path (ie subdirs from dir your running the app)
            sub = new File(subDir);
        } else {
            sub = new File(baseDir, subDir);
        }

        String[] files = sub.list(filter);
        if (files != null) {
            File target;
            for (int i=0 ; i < files.length ; i++) {
                target = new File(sub, files[i]);
                // Dbg.print("deleting: "+ target.toString());

                if(target.canWrite() && target.isFile()) {
                    target.delete();
                }
            }
        }
    }


    /** Create the three subdirectories needed for temp Kirrkirr files, if they
     *  dont already exist. Sets up html directory, xml directory, and users
     *  directory, based on Kirrkirr variables (Kirrkirr.xmlFolder, Kirrkirr.htmlFolder, and Kirrkirr.usersFolder).
     *  Once created, all writeable files conforming to the file filter in the old
     *  directory are moved to the new directory
     */
    public static void setupWriteDirectoryStructure(String oldDir, String newDir) {
        makeDirectoryIfNotExists(newDir, Kirrkirr.xmlFolder);
        moveAllWriteFiles(oldDir, newDir, Kirrkirr.xmlFolder, new KirrkirrFileFilter(KirrkirrFileFilter.XML_ENTRY));

        makeDirectoryIfNotExists(newDir, Kirrkirr.htmlFolder);
        moveAllWriteFiles(oldDir, newDir, Kirrkirr.htmlFolder, new KirrkirrFileFilter(KirrkirrFileFilter.HTML_ENTRY));

        makeDirectoryIfNotExists(newDir, Kirrkirr.usersFolder);
        moveAllWriteFiles(oldDir, newDir, Kirrkirr.usersFolder, new KirrkirrFileFilter(KirrkirrFileFilter.USER_ENTRY));
    }

    /** Checks to see if the subdirectory already exists in the base directory
     *  passed in. If not, it creates it there.
     */
    public static void makeDirectoryIfNotExists(String baseDir, String subDir) {
        File sub;

        sub = new File(baseDir, subDir);
        if((!sub.exists()) && (!sub.isDirectory())) {
            sub.mkdir();
        }
    }

    /** Moves all writeable files conforming to the file filter passed in
     *  from the subfolder of the source directory to the subfolder of
     *  the target directory (up to 10,000 files).
     *  @param sourceDir the base of where to move files from
     *  @param targetDir the base of where to move files to
     *  @param subDir the subdirectory in both directories in
     *  which to look for the files
     *  @param filter the KirrkirrFileFilter to choose the appropriate files
     *  (ie, html, xml, users).
     */
    public static void moveAllWriteFiles(String sourceDir, String targetDir, String subDir,
                                         KirrkirrFileFilter filter) {
        File targetFolder;
        File sourceFolder;

        if (sourceDir.equals("")) {
            //using abstract path (ie subdirs from dir your running the app)
            sourceFolder = new File(subDir);
        } else {
            sourceFolder = new File(sourceDir, subDir);
        }

        if (targetDir.equals("")) {
            //using abstract path (ie subdirs from dir your running the app)
            targetFolder = new File(subDir);
        } else {
            targetFolder = new File(targetDir, subDir);
        }

        String[] files = sourceFolder.list(filter);

        for (int i=0 ; i < files.length ; i++) {
            File source = new File(sourceFolder, files[i]);
            File target = new File(targetFolder, files[i]);
            // Dbg.print("moving from: "+ source.toString()+" to: "+ target.toString());

            if(source.canWrite() && source.isFile()) {
                source.renameTo(target);
            }
        }
    }


    /** Checks whether we are running on Mac. If so, tries all the
     *  file creators listed above to see if they are registered
     *  on the current machine. The first one it finds that is
     *  registered, it saves as the html creator. That way,
     *  when kirrkirr creates html files, the user can double click
     *  on the file created, and have it open in the application.
     *  (Ideally could check the default application for html
     *  on the user's computer? or at least have them specify
     *  it on the option panel, and save it in the profile).
     *  The messy reflection code is because only Macintoshes
     *  have the MRJ classes.
     *  See MRJToolkitTest.java in the MRJ demos, and also the MRJ API
     *  (that come with the MRJ). Also:
     *  <a href="http://developer.apple.com/techpubs/java/MacOSandJava/MRJToolkit/ProgMRJToolkit/MRJToolkit.a.html">
     *  http://developer.apple.com/techpubs/java/MacOSandJava/MRJToolkit/ProgMRJToolkit/MRJToolkit.a.html</a>
     *  2004 note: If you have to do something like this for MacOSX, I don't
     *  yet know what it is....
     */
    public static void setHtmlCreator() {
        if (Helper.onClassicMac()) {
            if (Dbg.FILE) Dbg.print("RelFile:setHtmlCreator: system is macintosh");
            for (int i=0; i<htmlCreators.length && htmlCreator==null; i++)
                {
                    File targetApp=null;
                    try{
                        String mrjostype="com.apple.mrj.MRJOSType";
                        Class ostypeclass = Class.forName(mrjostype);
                        Class[] types = {mrjostype.getClass()};
                        Constructor cons=ostypeclass.getConstructor(types);
                        Object[] params = {htmlCreators[i]};
                        Object ostype=cons.newInstance(params);

                        String mrjfileutils="com.apple.mrj.MRJFileUtils";
                        Class fileutilsclass = Class.forName(mrjfileutils);
                        types=new Class[1];
                        types[0]=ostypeclass;
                        Method method=fileutilsclass.getMethod("findApplication",types);
                        params=new Object[1];
                        params[0]=ostype;
                        targetApp=(File)method.invoke(null,params);

                        // The above code calls:
                        // targetApp = com.apple.mrj.MRJFileUtils.findApplication(new com.apple.mrj.MRJOSType(htmlCreators[i]));
                    }catch(Exception e){e.printStackTrace();}
                    if (targetApp!=null)
                        {
                            htmlCreator=htmlCreators[i];
                            if (Dbg.FILE) Dbg.print("RelFile:setHtmlCreator: success! found htmlCreator "+htmlCreator);
                        }
                }
        } else if (Dbg.FILE) Dbg.print("RelFile:setHtmlCreator: current OS is "+System.getProperty("os.name"));
    }


    /** Should be called whenever an html file is created.
     *  On macintoshes, sets the filecreator for the file,
     *  so that the user can double click on the file created,
     *  and have it open in the application.
     *  If the machine is not a macintosh, or is a mac but
     *  we couldn't find an html application, this doesn't
     *  do anything.
     *  (Ideally could check the default application for html
     *  on the user's computer? or at least have them specify
     *  it on the option panel, and save it in the profile).
     *  The messy reflection code is because only Macintoshes
     *  have the MRJ classes.
     *  See MRJToolkitTest.java in the MRJ demos, and also the MRJ API
     *  (that come with the MRJ). Also:
     *  <a href="http://developer.apple.com/techpubs/java/MacOSandJava/MRJToolkit/ProgMRJToolkit/MRJToolkit.a.html">
     *  http://developer.apple.com/techpubs/java/MacOSandJava/MRJToolkit/ProgMRJToolkit/MRJToolkit.a.html</a>
     */
    public static File setHtmlFileType(File html) {
        if (htmlCreator!=null) {
            try {
                String mrjostype="com.apple.mrj.MRJOSType";
                Class ostypeclass = Class.forName(mrjostype);
                Class[] types={mrjostype.getClass()};
                Constructor cons=ostypeclass.getConstructor(types);
                Object[] params={htmlCreator};
                Object ostype=cons.newInstance(params);
                // The above code calls:
                // com.apple.mrj.MRJOSType newCreator = new com.apple.mrj.MRJOSType(htmlCreator);

                String mrjfileutils="com.apple.mrj.MRJFileUtils";
                Class fileutilsclass = Class.forName(mrjfileutils);
                types=new Class[2];
                types[0]=html.getClass();
                types[1]=ostypeclass;
                Method method=fileutilsclass.getMethod("setFileCreator",types);
                params=new Object[2];
                params[0]=html;
                params[1]=ostype;
                method.invoke(null,params);
                // The above code calls:
                // com.apple.mrj.MRJFileUtils.setFileCreator(html,newCreator);
            } catch (Exception e) { e.printStackTrace(); }
        }
        return html;
    }


    public static void dumpSystemInfo() {
        Dbg.print("Running on system name: " +
                System.getProperty("os.name") + ", arch: " +
                System.getProperty("os.arch") + ", version: " +
                System.getProperty("os.version"));
	Dbg.print("  fileSeparator is " + fileSeparator);
	Dbg.print("  codeBase is " + codeBase);
        Dbg.print("  Helper.onClassicMac() " + Helper.onClassicMac() +
		  "; Helper.onMacOSX() " + Helper.onMacOSX());
    }
}


