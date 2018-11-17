package com.kudoji.cman.cache;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * First level is memory
 * Second level is file system
 */
public class TwoLevelCache<K, V> implements Cache<K, V>{
    /**
     *
     */
    private final MemoryCache<K, V> mc;
    private final FileCache<K, V> fc;

    /**
     * Cache strategies as follows:
     * * OLDTOFILE: older objects will be moved to file cache;
     * * OLDTOMEMORY: older objects will be moved to memory cache;
     * * FREQUENTTOFILE: frequent objects will be moved to file cache;
     * * FREQUENTTOMEMORY: frequent objects will be moved to memoty cache;
     */
    public enum CacheStrategy{
        OLDTOFILE,
        OLDTOMEMORY,
        FREQUENTTOFILE,
        FREQUENTTOMEMORY,
    }

    /**
     * Where object is located
     * * MEMORY - element located in memory,
     * * FILE - element is in file,
     * * NONE - neither
     */
    public enum ObjectLocation{
        MEMORY,
        FILE,
        NONE
    }

    /**
     * Current cache strategy
     */
    private CacheStrategy cacheStrategy;

    /**
     * Default constructor
     */
    public TwoLevelCache(){
        this.mc = new MemoryCache<>();
        this.mc.setMaxSize(0);

        this.fc = new FileCache<>();
        this.fc.setMaxSize(0);

        this.cacheStrategy = CacheStrategy.FREQUENTTOMEMORY;
    }

    public TwoLevelCache(CacheStrategy cacheStrategy){
        this.mc = new MemoryCache<>();
        this.mc.setMaxSize(0);

        this.fc = new FileCache<>();
        this.fc.setMaxSize(0);

        this.cacheStrategy = cacheStrategy;
    }

    /**
     * First element always cached in memory. If memory cache overfilled, try file cache
     * @param key
     * @param object
     * @return true if object put either to memory or file cache, false otherwise
     */
    @Override
    public boolean put(K key, V object) {
        boolean result = false;

        result = this.mc.put(key, object);

        if (!result){
            //  memory cache is full, try file cache
            result = this.fc.put(key, object);
        }

        return result;
    }

    @Override
    public boolean put(CacheObject<K, V> cacheObject){
        boolean result = false;

        result = this.mc.put(cacheObject);

        if (!result){
            //  memory cache is full, try file cache
            result = this.fc.put(cacheObject);
        }

        return result;
    }

    /**
     * Retrieve object by key. First checks value in memory cache. If fails, tries file cache
     * @param key
     * @return null if object is not found in both caches, real object otherwise
     */
    @Override
    public V get(K key) {
        V result = null;

        //  check memory cache first
        result = this.mc.get(key);

        if (result == null){
            //  key is not in memory cache, try file one
            result = this.fc.get(key);
        }

        return result;
    }

    /**
     * Deletes object from memory/file cache
     * @param key
     * @return false if value is not found in both caches, true in case value deleted from one of the caches
     */
    @Override
    public boolean delete(K key) {
        boolean result = false;

        result = this.mc.delete(key);
        if (!result){
            result = this.fc.delete(key);
        }

        return result;
    }

    @Override
    public void flush() {
        this.mc.flush();
        this.fc.flush();
    }

    /**
     * Calculates total cache size
     * @return memory + file caches size
     */
    @Override
    public int size() {
        return this.mc.size() + this.fc.size();
    }

    /**
     * Default method returns maximum capacity
     * @return
     */
    @Override
    public int getMaxSize() {
        return this.mc.getMaxSize() + this.fc.getMaxSize();
    }

    /**
     * Memory cache max capacity
     * @return
     */
    public int getMaxSizeMemoryCache(){
        return this.mc.getMaxSize();
    }

    /**
     * File cache max capacity
     * @return
     */
    public int getMaxSizeFileCache(){
        return this.fc.getMaxSize();
    }

    /**
     * Sets max capacity for both caches.
     * Amount for each cache calculated like value / 2
     * @param value
     * @return
     */
    @Override
    public void setMaxSize(int value) {
        if (value <= 0){
            throw new IllegalArgumentException("Two-level cache size must not be negative");
        }

        int maxSizeMemoryCache = value / 2;

        try{
            this.mc.setMaxSize(maxSizeMemoryCache);
        }catch (IllegalArgumentException e){
            return;
        }

        //  max size for memory cache set successfully
        this.fc.setMaxSize(value - maxSizeMemoryCache); // rest goes to file cache
    }

    public void setMaxSizeMemoryCache(int value){
        this.mc.setMaxSize(value);
    }

    public void setMaxSizeFileCache(int value){
        this.fc.setMaxSize(value);
    }

    /**
     * Gets all cached objects
     *
     * @return
     */
    @Override
    public List<CacheObject<K, V>> getAll(){
        List<CacheObject<K, V>> result;

        result = this.mc.getAll();
        result.addAll(this.fc.getAll());

        return result;
    }

    /**
     * Checks whether key present in memory/file cache or not
     * Method doesn't increment frequency
     *
     * @param key
     * @return
     */
    @Override
    public boolean isKeyPresent(K key){
        return (this.mc.isKeyPresent(key) || this.fc.isKeyPresent(key));
    }

    @Override
    public long getAge(K key){
        long cacheObjectAge = this.mc.getAge(key);

        if (cacheObjectAge == -1){
            return this.fc.getAge(key);
        }

        return cacheObjectAge;
    }

    @Override
    public int getFrequency(K key){
        int cacheObjectFrequency = this.mc.getFrequency(key);

        if (cacheObjectFrequency == -1){
            return this.fc.getFrequency(key);
        }

        return cacheObjectFrequency;
    }

    /**
     * Returns location for the object
     * @param key
     * @return
     */
    public ObjectLocation getLocation(K key){
        if (this.mc.isKeyPresent(key)){
            return ObjectLocation.MEMORY;
        }else if (this.fc.isKeyPresent(key)){
            return ObjectLocation.FILE;
        }

        return ObjectLocation.NONE;
    }

    /**
     * Sets cache strategy and applies it
     *
     * @param cacheStrategy
     */
    public void setCacheStrategy(CacheStrategy cacheStrategy){
        this.cacheStrategy = cacheStrategy;

        applyCacheStrategy();
    }

    /**
     * Applies current cache strategy
     */
    public void applyCacheStrategy(){
        switch (this.cacheStrategy){
            case FREQUENTTOFILE:
                applyFrequentToFileCacheStrategy();
                break;
            case FREQUENTTOMEMORY:
                applyFrequentToMemoryCacheStrategy();
                break;
            case OLDTOFILE:
                applyOldToFileCacheStrategy();
                break;
            case OLDTOMEMORY:
                applyOldToMemoryCacheStrategy();
                break;
            default:
                break;
        }
    }

    /**
     * Moves frequent objects to file cache
     */
    private void applyFrequentToFileCacheStrategy(){
        List<CacheObject<K, V>> cacheObjects = this.getAll();

        //  sort list by frequency BUT most frequent elements MUST at the BOTTOM of the list
        cacheObjects.sort((cacheObject1, cacheObject2) -> {
            int frequencyCacheObject1 = cacheObject1.getFrequency();
            int frequencyCacheObject2 = cacheObject2.getFrequency();

            if (frequencyCacheObject1 < frequencyCacheObject2){
                return -1;
            }else if (frequencyCacheObject1 == frequencyCacheObject2){
                return 0;
            }else{
                return 1;
            }
        });

        fillCache(cacheObjects);
    }

    /**
     * Moves frequent objects to memory cache
     */
    private void applyFrequentToMemoryCacheStrategy(){
        List<CacheObject<K, V>> cacheObjects = this.getAll();

        //  sort list by frequency BUT most frequent elements MUST at the TOP of the list
        cacheObjects.sort((cacheObject1, cacheObject2) -> {
            int frequencyCacheObject1 = cacheObject1.getFrequency();
            int frequencyCacheObject2 = cacheObject2.getFrequency();

            if (frequencyCacheObject1 > frequencyCacheObject2){
                return -1;
            }else if (frequencyCacheObject1 == frequencyCacheObject2){
                return 0;
            }else{
                return 1;
            }
        });

        fillCache(cacheObjects);
    }

    /**
     * Moves old objects to file cache
     */
    private void applyOldToFileCacheStrategy(){
        List<CacheObject<K, V>> cacheObjects = this.getAll();

        //  sort list by age BUT most youngest elements MUST at the TOP of the list
        cacheObjects.sort((cacheObject1, cacheObject2) -> {
            long ageCacheObject1 = cacheObject1.getAge();
            long ageCacheObject2 = cacheObject2.getAge();

            if (ageCacheObject1 < ageCacheObject2){
                return -1;
            }else if (ageCacheObject1 == ageCacheObject2){
                return 0;
            }else{
                return 1;
            }
        });

        fillCache(cacheObjects);
    }

    /**
     * Moves old objects to memory cache
     */
    private void applyOldToMemoryCacheStrategy(){
        List<CacheObject<K, V>> cacheObjects = this.getAll();

        //  sort list by age BUT most youngest elements MUST at the BOTTOM of the list
        cacheObjects.sort((cacheObject1, cacheObject2) -> {
            long ageCacheObject1 = cacheObject1.getAge();
            long ageCacheObject2 = cacheObject2.getAge();

            if (ageCacheObject1 > ageCacheObject2){
                return -1;
            }else if (ageCacheObject1 == ageCacheObject2){
                return 0;
            }else{
                return 1;
            }
        });

        fillCache(cacheObjects);
    }

    /**
     * Fills memory and file caches by cacheObject according their capacity
     * Objects from the top goes to MemoryCache first, remaining to FileCache
     *
     * @param cacheObjects
     */
    private void fillCache(List<CacheObject<K, V>> cacheObjects){
        //  flush caches
        this.mc.flush();
        this.fc.flush();

//        for (CacheObject cacheObject: cacheObjects){
//            System.out.println(cacheObject.getKey() + "\t" + cacheObject.getFrequency());
//        }

        for (CacheObject<K, V> cacheObject: cacheObjects){
            if (!this.mc.put(cacheObject)){
                //  memory cache is full
                //  put the rest of objects to the file cache
                this.fc.put(cacheObject);
            }
        }
    }
}
