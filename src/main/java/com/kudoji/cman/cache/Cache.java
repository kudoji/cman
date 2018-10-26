package com.kudoji.cman.cache;

import java.util.ArrayList;

interface Cache {
    boolean put(String key, Object object);
    Object get(String key);
    boolean delete(String key);
    void flush();
    int size();
    int getMaxSize();
    boolean setMaxSize(int value);
    ArrayList<CacheObject> getAll();
    boolean isKeyPresent(String key);

    /**
     * Gets object's age or -1 in case of error
     * @param key
     * @return
     */
    long getAge(String key);

    /**
     * Gets object's usage frequency or -1 in case of error
     * @param key
     * @return
     */
    int getFrequency(String key);
}
