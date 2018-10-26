package com.kudoji.cman.cache;

import java.io.Serializable;

/**
 * Wrapper over Object V that need to be cached
 * Should be serializable to have ability to save class' instance to file
 */
public class CacheObject<K, V> implements Serializable {
    /**
     * Stores key
     */
    private K key;
    /**
     * Object that need to be cached
     */
    private V object;
    /**
     * Object's frequency
     */
    private int frequency;
    /**
     * When Object was cached
     */
    private long createTime;

    /**
     * Object
     * @param object
     */
    public CacheObject(K key, V object){
        this.key = key;
        this.object = object;
        this.frequency = 0;
        this.createTime = System.currentTimeMillis();
    }

    public K getKey(){
        return this.key;
    }

    public V getObject(){
        //  increase frequency any time object getter called
        this.incFrequency();

        return this.object;
    }

    public int getFrequency(){
        return this.frequency;
    }

    public long getCreateTime(){
        return this.createTime;
    }

    /**
     * Returns object's age in milliseconds
     * @return
     */
    public long getAge(){
        return System.currentTimeMillis() - this.createTime;
    }

    public void incFrequency(){
        this.frequency++;
    }
}
