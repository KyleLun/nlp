package cn.nsoc.nlp.core.utils;

import org.mapdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *Cache
 * Create by Alan 2017.11.21
 */
public class Cache4MapDB {
    private final static Logger LOGGER = LoggerFactory.getLogger("mapDB");
    public static final String USER_DIR = System.getProperty("user.dir");
    private final static Map<String, Cache4MapDB> dbs = new Hashtable<>();
    private final static int CONCURRENCY = 16;
    private static DB dbManager;
    private String dbPath;
    private String dbName;


    /**
     * Build cache
     * @param dbPath mapDB path
     * @param dbName mapDB name
     */
    private Cache4MapDB(String dbPath, String dbName) {
        this.dbName = dbName;
        this.dbPath = dbPath;
    }

    /**
     * Singleton Cache4MapDB
     * @param dbPath Map db file path
     * @return Cache4MapDB
     */
    public static Cache4MapDB getDB(String dbPath, String dbName) {
        String path = Paths.get(dbPath, dbName).toString();
        if(!dbs.containsKey(path)) {
            synchronized (Cache4MapDB.class) {
                if(!dbs.containsKey(path)) {
                    Cache4MapDB cache4MapDB = new Cache4MapDB(dbPath, dbName);
                    cache4MapDB.init();
                    dbs.put(path, cache4MapDB);
                }
            }
        }
        return dbs.get(path);
    }

    /**
     * Init Cache4MapDB
     */
    private void init() {
        try {
            if(dbPath == null || dbPath.isEmpty()) {
                dbPath = cacheFilePath();
                LOGGER.warn(String.format("Db path is null or \"\", use default path: %s ", dbPath));
            } else {
                dbPath = Paths.get(dbPath, dbName).toString();
                LOGGER.info(String.format("Build map db path: %s ", dbPath));
            }
            dbMaker(dbPath);
        } catch (DBException.FileLocked e1){
            LOGGER.error(e1.getMessage(), e1);
            throw new ExceptionInInitializerError(e1);
        } catch (Exception e2) {
            dbPath = cacheFilePath();
            LOGGER.error(String.format("Load db file fail. %s", e2.getMessage()), e2);
            LOGGER.warn(String.format("Db path error, use default path: %s ", dbPath));
            deleteFile(dbPath);
            dbMaker(dbPath);
        }
    }

    /**
     * Maker map db
     * @param dbPath Map db path
     */
    private void dbMaker(String dbPath) {
        dbManager = DBMaker
                .fileDB(dbPath)
                .fileMmapEnable()
                .fileMmapEnableIfSupported()
                .fileMmapPreclearDisable()
                .cleanerHackEnable()
                .closeOnJvmShutdown()
                .concurrencyScale(CONCURRENCY)
                .make();
    }

    /**
     * Get HTreeMap
     * @param name HTreeMap name
     * @param maxSize HTreeMap max size
     * @param cacheTime HTreeMap Save time
     * @return HTreeMap
     */
    public final HTreeMap getMap(String name, int maxSize, int cacheTime) {
        if(!dbManager.exists(name)) {
            synchronized (Cache4MapDB.class) {
                if(!dbManager.exists(name)) {
                    dbManager.hashMap(name)
                             .expireMaxSize(maxSize)
                             .expireAfterCreate(cacheTime, TimeUnit.MINUTES)
                             .expireAfterGet(cacheTime, TimeUnit.MINUTES)
                             .create();
                }
            }
        }

        return dbManager.get(name);
    }

    /**
     * Get BTreeMap
     * @param name BTreeMap name
     * @param maxSize BTreeMap max size
     * @return BTreeMap
     */
    public final BTreeMap getTreeMap(String name, int maxSize) {
        if(!dbManager.exists(name)) {
            synchronized (Cache4MapDB.class) {
                if(!dbManager.exists(name)) {
                    dbManager.treeMap(name)
                             .maxNodeSize(maxSize)
                            .create();
                }
            }
        }

        return dbManager.get(name);
    }

    /**
     * Cet file path
     * @return path
     */
    private String cacheFilePath() {
        String path;
        try {
            path = System.getProperty("user.dir");
            LOGGER.info("Get createAsync file path." + path);
        } catch (Exception e) {
            path = "/var/nsoc/worker/";
            LOGGER.error("Util.getWorkingPath error. %s", e);
        }

        if(path == null || "".equals(path)) {
            path = "/var/nsoc/worker/";
        }


        return Paths.get(path, "default.cache").toString();
    }

    /**
     * Delete exists fb file
     * @param cacheFile Cache file
     */
    private void deleteFile(String cacheFile) {
        File file = new File(cacheFile);
        if(file.exists()) {
            boolean b = file.delete();
            if (!b) {
                LOGGER.error(String.format(
                        "Cache file %s is exists and can't delete. You must be delete it", cacheFile));
            } else {
                LOGGER.warn(String.format(
                        "Cache file %s is exists and deleted. ", cacheFile));
            }
        }
    }

    public void shutdown() {
        if (dbManager != null) dbManager.close();
    }
}
