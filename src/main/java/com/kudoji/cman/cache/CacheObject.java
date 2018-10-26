package com.kudoji.cman.cache;

import java.io.Serializable;

/**
 * Wrapper over Object that need to be cached
 * Should be serializable to have ability to save class' instance to file
 */
public class CacheObject implements Serializable {
    /**
     * Stores key
     */
    private String key;
    /**
     * Object that need to be cached
     */
    private Object object;
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
    public CacheObject(String key, Object object){
        this.key = key;
        this.object = object;
        this.frequency = 0;
        this.createTime = System.currentTimeMillis();
    }

    public String getKey(){
        return this.key;
    }

    public Object getObject(){
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
