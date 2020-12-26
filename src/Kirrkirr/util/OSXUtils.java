package Kirrkirr.util;

import java.lang.reflect.Method;

import javax.swing.JPanel;
import javax.swing.UIManager;


public class OSXUtils {

    private OSXUtils() { } // static methods


    /** Generic registration with the Mac OS X application menu.  Attempts
     *  to register with the Apple EAWT.  This code assumes you're running this
     *  inside a test for being on a Mac OS X machine.
     *  This method calls OSXAdapter.registerMacOSXApplication() and OSXAdapter.enablePrefs().
     *  See OSXAdapter.java for the signatures of these methods.
     * @param kirrkirr Application (JPanel to register with OSX)
     */
    public static void macOSXRegistration(Kirrkirr.Kirrkirr kirrkirr) {
        if (true) {
            // try {
                String classString = "Kirrkirr.util.MacOSAdapter";
                // if (Helper.onOlderJdk()) {
                //     classString = "Kirrkirr.util.OSXAdapter";
                // }
                // Class osxAdapter = ClassLoader.getSystemClassLoader().loadClass(classString);

                // Class[] defArgs = {JPanel.class};
                // Method registerMethod = osxAdapter.getDeclaredMethod("registerMacOSXApplication", defArgs);
                // if (registerMethod != null) {
                //     registerMethod.invoke(osxAdapter, kirrkirr);
                // }
                MacOSAdapter.registerMacOSXApplication(kirrkirr);

                // THIS IS NO LONGER NEEDED WITH NEWER OS X Java
                // This is slightly gross.  To reflectively access methods with boolean args,
                // use "boolean.class", then pass a Boolean object in as the arg, which apparently
                // gets converted for you by the reflection system.
                // defArgs[0] = boolean.class;
                // Method prefsEnableMethod = osxAdapter.getDeclaredMethod("enablePrefs", defArgs);
                // if (prefsEnableMethod != null) {
                //     Object[] args = { Boolean.TRUE };
                //     prefsEnableMethod.invoke(osxAdapter, args);
                // }
//            } catch (NoClassDefFoundError | ClassNotFoundException e) {
//                // This will be thrown first if the OSXAdapter is loaded on a system without the EAWT
//                // because OSXAdapter extends ApplicationAdapter in its def
//                System.err.println("This version of Mac OS X does not support the Apple EAWT.  Application Menu handling has been disabled (" + e + ")");
//            } catch (Exception e) {
//                System.err.println("Exception while loading the OSXAdapter:");
//                e.printStackTrace();
//            }
        }
    }


    public static void setUpMacSystemProperties() {
        try {
            if (Dbg.PROGRESS) Dbg.print("On MacOSX doing menu etc. properties setup");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Kirrkirr");
            // So menu appears on top of screen for Mac
            // for Mac Java 1.4
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            // for Mac Java 1.3
            System.setProperty("com.apple.macos.useScreenMenuBar", "true");
            System.setProperty("com.apple.macos.use-file-dialog-packages", "true");

            // System.setProperty("com.apple.macos.use-file-dialog-packages", "true");
            // use java.awt.FileChooser rather than javax.swing.JFileChooser.
            // See http://developer.apple.com/samplecode/OSXAdapter/OSXAdapter.html for handling Apple application menu
            // apple.awt.showGrowBox
            // apple.awt.brushMetalLook
            UIManager.put("TitledBorder.border", UIManager.getBorder("TitledBorder.aquaVariant"));
        } catch (Exception e) {
            Dbg.print("Error in MacOSX setup");
            e.printStackTrace();
        }
    }

}
