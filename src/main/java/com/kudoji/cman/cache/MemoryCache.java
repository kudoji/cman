package com.kudoji.cman.cache;

import java.util.ArrayList;
import java.util.HashMap;

//  first level cache - memory
public class MemoryCache implements Cache{
    //  max cache size
    //  default is zero - unlimited
    private int sizeMax;
    private HashMap<String, CacheObject> cache;

    public MemoryCache(){
        this.cache = new HashMap<>();
        this.sizeMax = 0;
    }

    /**
     * Adds new element to cache
     * @param key
     * @param object
     * @return false than element has not been added due to cache overflow; true - all is fine
     */
    public boolean put(String key, Object object){
        if ((this.sizeMax > 0) && !this.cache.containsKey(key) && (this.cache.size() == this.sizeMax)){
            //  if add new element than exceed maximum limit
            return false;
        }

        CacheObject cacheObject = new CacheObject(key, object);
        this.cache.put(key, cacheObject);

        return true;
    }

    /**
     * The same as the above but with cacheObject
     * @param cacheObject
     * @return
     */
    public boolean put(CacheObject cacheObject){
        String key = cacheObject.getKey();
        if ((this.sizeMax > 0) && !this.cache.containsKey(key) && (this.cache.size() == this.sizeMax)){
            //  if add new element than exceed maximum limit
            return false;
        }

        this.cache.put(key, cacheObject);

        return true;
    }

    /**
     * Returns value from cache based on key
     * @param key
     * @return cached object or null
     */
    public Object get(String key){
        CacheObject cacheObject = this.cache.get(key);

        if (cacheObject != null){
            return cacheObject.getObject();
        }

        return null;
    }

    /**
     * Deletes value from cache
     * @param key
     * @return true then value deleted, false otherwise
     */
    public boolean delete(String key){
        if (this.cache.containsKey(key)){
            this.cache.remove(key);
            return true;
        }

        return false;
    }

    public void flush(){
        this.cache.clear();
    }

    public int size(){
        return this.cache.size();
    }

    public int getMaxSize(){
        return this.sizeMax;
    }

    /**
     * Sets cache maximum size
     *
     * @param value
     * @return false if size is not set, true otherwise
     */
    public boolean setMaxSize(int value){
        if (value < 0){
            return false;
        }

        this.sizeMax = value;

        if (value == 0){
            //  unlimited cache size
            return true;
        }

        int cacheSize = this.size();
        if (cacheSize > value){
            //  max cache size is less than current cache size
            //  delete all object that are out of bound
            ArrayList<String> cacheObjects = new ArrayList<>(this.cache.keySet());
            for (int i = value; i < cacheSize; i++){
                this.delete(cacheObjects.get(i));
            }
        }

        return true;
    }

    /**
     * Gets all object that are in memory cache
     * @return
     */
    public ArrayList<CacheObject> getAll(){
        return new ArrayList<CacheObject>(this.cache.values());
    }

    /**
     * Checks whether key present in memory cache or not
     * Method doesn't increment frequency
     *
     * @param key
     * @return
     */
    public boolean isKeyPresent(String key){
        return (this.cache.get(key) != null);
    }

    /**
     * Returns CacheObject by key or null
     *
     * @param key
     * @return
     */
    private CacheObject getCacheObject(String key){
        return this.cache.get(key);
    }

    public long getAge(String key){
        CacheObject cacheObject = getCacheObject(key);
        if (cacheObject == null){
            return -1;
        }

        return cacheObject.getAge();
    }

    public int getFrequency(String key){
        CacheObject cacheObject = getCacheObject(key);
        if (cacheObject == null){
            return -1;
        }

        return cacheObject.getFrequency();
    }
}
