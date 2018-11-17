package com.kudoji.cman.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//  first level cache - memory
public class MemoryCache<K, V> implements Cache<K, V>{
    //  max cache size
    //  default is zero - unlimited
    private int maxSize;
    private final Map<K, CacheObject<K, V>> cache;

    public MemoryCache(){
        this.cache = new HashMap<>();
        this.maxSize = 0;
    }

    /**
     * Adds new element to cache
     * @param key
     * @param object
     * @return false than element has not been added due to cache overflow; true - all is fine
     */
    @Override
    public boolean put(K key, V object){
        if ((this.maxSize > 0) && !this.cache.containsKey(key) && (this.cache.size() == this.maxSize)){
            //  if add new element than exceed maximum limit
            return false;
        }

        CacheObject<K, V> cacheObject = new CacheObject<>(key, object);
        this.cache.put(key, cacheObject);

        return true;
    }

    /**
     * The same as the above but with cacheObject
     * @param cacheObject
     * @return
     */
    @Override
    public boolean put(CacheObject<K, V> cacheObject){
        K key = cacheObject.getKey();
        if ((this.maxSize > 0) && !this.cache.containsKey(key) && (this.cache.size() == this.maxSize)){
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
    @Override
    public V get(K key){
        CacheObject<K, V> cacheObject = this.cache.get(key);

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
    @Override
    public boolean delete(K key){
        if (this.cache.containsKey(key)){
            this.cache.remove(key);
            return true;
        }

        return false;
    }

    @Override
    public void flush(){
        this.cache.clear();
    }

    @Override
    public int size(){
        return this.cache.size();
    }

    @Override
    public int getMaxSize(){
        return this.maxSize;
    }

    /**
     * Sets cache maximum size
     *
     * @param maxSize
     * @return false if size is not set, true otherwise
     */
    @Override
    public void setMaxSize(int maxSize){
        if (maxSize < 0){
            throw new IllegalArgumentException("Memory cache size must not be negative");
        }

        this.maxSize = maxSize;

        if (maxSize == 0){
            //  unlimited cache size
            return;
        }

        int cacheSize = this.size();
        if (cacheSize > maxSize){
            //  max cache size is less than current cache size
            //  delete all object that are out of bound
            ArrayList<K> cacheObjects = new ArrayList<>(this.cache.keySet());
            for (int i = maxSize; i < cacheSize; i++){
                this.delete(cacheObjects.get(i));
            }
        }
    }

    /**
     * Gets all object that are in memory cache
     * @return
     */
    @Override
    public List<CacheObject<K, V>> getAll(){
        return new ArrayList<>(this.cache.values());
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
        return (this.cache.get(key) != null);
    }

    /**
     * Returns CacheObject by key or null
     *
     * @param key
     * @return
     */
    private CacheObject<K, V> getCacheObject(K key){
        return this.cache.get(key);
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
}