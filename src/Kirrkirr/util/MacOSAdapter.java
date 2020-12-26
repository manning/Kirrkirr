package Kirrkirr.util;

import java.awt.*;
import java.awt.desktop.*;

import Kirrkirr.Kirrkirr;


public class MacOSAdapter {

    private static Kirrkirr mainApp;

    private MacOSAdapter(Kirrkirr inApp) {
        mainApp = inApp;
    }

    public static void handleAbout(AboutEvent ae) {
        if (mainApp != null) {
            mainApp.displayAbout();
        } else {
            throw new IllegalStateException("handleAbout: TregexGUI instance detached from listener");
        }
    }

    public static void handlePreferences(PreferencesEvent ae) {
        if (mainApp != null) {
            mainApp.displayPreferences();
        } else {
            throw new IllegalStateException("handlePreferences: TregexGUI instance detached from listener");
        }
    }

    public static void handleQuit(QuitEvent ae) {
        if (mainApp != null) {
      /*
      / You MUST setHandled(false) if you want to delay or cancel the quit.
      / This is important for cross-platform development -- have a universal quit
      / routine that chooses whether or not to quit, so the functionality is identical
      / on all platforms.  This example simply cancels the AppleEvent-based quit and
      / defers to that universal method.
      */
            mainApp.quitKirrkirr();
        } else {
            throw new IllegalStateException("handleQuit: TregexGUI instance detached from listener");
        }
    }


    public static void registerMacOSXApplication(Kirrkirr inApp) {
        Desktop desktop = Desktop.getDesktop();
        desktop.setAboutHandler(e -> Kirrkirr.staticDisplayAbout());
        desktop.setPreferencesHandler(e -> Kirrkirr.staticDisplayPreferences());
        desktop.setQuitHandler((e,r) -> Kirrkirr.quitKirrkirr());
    }

}