package com.kudoji.cman.cache;

import java.util.List;

interface Cache<K, V> {
    boolean put(K key, V object);
    boolean put(CacheObject<K, V> cacheObject);
    V get(K key);
    boolean delete(K key);
    void flush();
    int size();
    int getMaxSize();
    boolean setMaxSize(int value);
    List<CacheObject<K, V>> getAll();
    boolean isKeyPresent(K key);

    /**
     * Gets object's age or -1 in case of error
     * @param key
     * @return
     */
    long getAge(K key);

    /**
     * Gets object's usage frequency or -1 in case of error
     * @param key
     * @return
     */
    int getFrequency(K key);
}
