package com.kudoji.cman.cache;

import java.io.*;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

/**
 * Was thinking how to implement file cache mechanism.
 * Have come to few solutions which I don't like:
 *
 * 1. Implement key-value storage in one file.
 *      This could be implemented as storing entire HashMap to file.
 *      Any time get() calls need to read entire file to memory and use HashMap.get(key) method (or cache values
 *      in memory and check memory cache before reading the file which makes task identical to MemoryCache
 *      which is incorrect due to one issue - this is file cache).
 *      Any time put() calls need to read HashMap from memory, call HashMap.put() and save HashMap to file.
 *      Not good idea because any cache's manipulation forces to read/write entire HashMap to memory which
 *      will grow over time
 *
 * 2. The same as 1 but...
 *      Wrap each object to HashMap and write it to file. The approach is similar to the 1 but will add one more
 *      cycle when need to find value in cache.
 *      Again, wastage of resources.
 *
 * 3. Use a database, sqlite, for example with id|key|value table structure.
 *      Which makes task NOT to be pure file cache since sqlite is another storage engine and uses it own cache
 *      mechanics...
 *
 * 4. Keep each object in a file.
 *      Could be implemented as follows:
 *      - all files stored in one directory
 *      - file name is a key (md5(key)/sha(key) or anything since key can have dangerous symbols)
 *      - method put(): if file exists - replace file, add new in other case
 *      - method get(): if file exists - return object, null otherwise.
 *      The best approach in terms of performance apart of part 3 maybe but don't like too many files and
 *      delegating entire folder for cache.
 *
 * Decided to implement 4th method.
 */
public class FileCache<K, V> implements Cache<K, V>{
    //  max cache size
    //  default is zero - unlimited
    private int sizeMax;
    //  folder to store cache files
    private String cacheDir = ".cache";
    //  keep flag in case of error during cache folder creating
    private boolean isCacheFolderExists;

    public FileCache(){
        this.sizeMax = 0;

        File fDir = new File(this.cacheDir);
        //  check whether folder exists or not
        if (!fDir.exists()){
            this.isCacheFolderExists = fDir.mkdir();
        }else{
            this.isCacheFolderExists = true;
        }
    }

    /**
     * Adds new element to cache
     * @param key
     * @param object
     * @return false than element has not been added due to cache overflow or io error(s); true - all is fine
     */
    @Override
    public boolean put(K key, V object) {
        String fileName = getFileName(key.toString());
        File file = new File(fileName);
        if (!file.exists()){
            if (this.sizeMax > 0 && this.size() == this.sizeMax){
                //  cannot add new value due to cache max size
                return false;
            }
        }else{
            //  clean place for new value
            file.delete();
        }

        CacheObject<K, V> cacheObject = new CacheObject<>(key, object);
        return saveObjectToFile(fileName, cacheObject);
    }

    /**
     * The same as above but with CacheObject
     * @param cacheObject
     * @return
     */
    @Override
    public boolean put(CacheObject<K, V> cacheObject) {
        K key = cacheObject.getKey();

        String fileName = getFileName(key.toString());
        File file = new File(fileName);
        if (!file.exists()){
            if (this.sizeMax > 0 && this.size() == this.sizeMax){
                //  cannot add new value due to cache max size
                return false;
            }
        }else{
            //  clean place for new value
            file.delete();
        }

        return saveObjectToFile(fileName, cacheObject);
    }

    /**
     * Returns value from cache based on key
     * @param key
     * @return cached object or null
     */
    @Override
    public V get(K key) {
        String fileName = getFileName(key.toString());
        File fObject = new File(fileName);

        if (!fObject.exists()){
            //  object for the key is not cached
            return null;
        }

        CacheObject<K, V> cacheObject = readObjectFromFile(fileName);

        if (cacheObject != null){
            V object = cacheObject.getObject();
            //  need to update cache object in file cache to save frequency
            saveObjectToFile(fileName, cacheObject);

            return object;
        }

        return null;
    }

    @Override
    public boolean delete(K key) {
        File fObject = new File(getFileName(key.toString()));

        return fObject.delete();
    }

    @Override
    public void flush() {
        File fDir = new File(this.cacheDir);

        for (File f: fDir.listFiles()){
            f.delete();
        }
    }

    @Override
    public int size() {
        return new File(this.cacheDir).list().length;
    }

    @Override
    public int getMaxSize() {
        return this.sizeMax;
    }

    @Override
    public void setMaxSize(int value) {
        if (value < 0){
            throw new IllegalArgumentException("File cache maximum size must not be negative");
        }

        this.sizeMax = value;

        if (value == 0){
            //  unlimited cache size
            return;
        }

        int cacheSize = this.size();
        if (cacheSize > value){
            //  max cache size is less than current cache size
            //  delete all object that are out of bound
            File fDir = new File(this.cacheDir);
            File[] files = fDir.listFiles();
            for (int i = value; i < cacheSize; i++){
                files[i].delete();
            }
        }
    }

    /**
     * Gets all objects that are in file cache
     * @return
     */
    @Override
    public List<CacheObject<K, V>> getAll() {
        List<CacheObject<K, V>> result = new ArrayList<>();

        File fDir = new File(cacheDir);

        for (File f: fDir.listFiles()){
            CacheObject<K, V> cacheObject = readObjectFromFile(f.getPath());

            if (cacheObject != null){
                result.add(cacheObject);
            }
        }

        return result;
    }

    /**
     * Checks whether key present in memory cache or not
     * Method doesn't increment frequency
     *
     * @param key
     * @return
     */
    @Override
    public boolean isKeyPresent(K key){
        String fileName = getFileName(key.toString());
        File fObject = new File(fileName);

        return fObject.exists();
    }

    @Override
    public long getAge(K key){
        CacheObject<K, V> cacheObject = getCacheObject(key);

        if (cacheObject == null){
            return -1;

        }

        return cacheObject.getAge();
    }

    @Override
    public int getFrequency(K key){
        CacheObject<K, V> cacheObject = getCacheObject(key);

        if (cacheObject == null){
            return -1;

        }

        return cacheObject.getFrequency();
    }

    /**
     * Returns CacheObject by key or null
     *
     * @param key
     * @return
     */
    private CacheObject<K, V> getCacheObject(K key){
        String fileName = getFileName(key.toString());
        File fObject = new File(fileName);

        if (!fObject.exists()){
            return null;
        }

        return readObjectFromFile(fileName);
    }

    /**
     * Gets file name based on cache value or null in case of error
     *
     * @param key
     * @return Full file name relatively to cache directory
     */
    private String getFileName(String key){
        String sha1 = null;

        try{
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.reset();
            md.update(key.getBytes("UTF-8"));
            sha1 = byteToHex(md.digest());
        }catch (Exception e){
            e.printStackTrace();
        }

        return cacheDir + File.separator + sha1;
    }

    /**
     * Taken from here
     * @url https://stackoverflow.com/questions/4895523/java-string-to-sha1
     * @param value
     * @return
     */
    private String byteToHex(final byte[] value){
        java.util.Formatter formatter = new java.util.Formatter();

        for (byte b : value){
            formatter.format("%02x", b);
        }

        String result = formatter.toString();
        formatter.close();

        return result;
    }

    /**
     * Saves object to file
     *
     * @param fileName
     * @param cacheObject
     * @return false if and only if object is not saved
     */
    private boolean saveObjectToFile(String fileName, CacheObject<K, V> cacheObject){
        boolean isError = false;

        try{
            FileOutputStream fos = new FileOutputStream(fileName);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(cacheObject);

            oos.close();
            fos.close();
        }catch (IOException e){
            e.printStackTrace();
            isError = true;
        }

        return !isError;
    }

    /**
     * Reads object from file
     * @param fileName
     * @return
     */
    @SuppressWarnings("unchecked")
    private CacheObject<K, V> readObjectFromFile(String fileName){
        CacheObject<K, V> cacheObject = null;

        try{
            FileInputStream fis = new FileInputStream(fileName);
            ObjectInputStream ois = new ObjectInputStream(fis);

            cacheObject = (CacheObject<K, V>)ois.readObject();

            ois.close();
            fis.close();
        }catch (ClassNotFoundException | IOException e){
            e.printStackTrace();
        }

        return cacheObject;
    }
}
