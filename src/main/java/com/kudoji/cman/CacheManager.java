package com.kudoji.cman;

import com.kudoji.cman.cache.FileCache;
import com.kudoji.cman.cache.MemoryCache;
import com.kudoji.cman.cache.TwoLevelCache;

public class CacheManager {
    private final static int MAX_ELEMENTS = 150;

    private static void printCache(TwoLevelCache tlc, String title){
        System.out.println();
        System.out.println(title);
        for (int i = 0; i < MAX_ELEMENTS; i++){
            String key = String.valueOf(i);
            System.out.println("\tobject: '" + tlc.get(key) + "'\tlocation: '" + tlc.getLocation(key) +
                    "'\tage: " + tlc.getAge(key) + "\tfreq: " + tlc.getFrequency(key));
        }

    }
    public static void main(String[] args){
        FileCache fc = new FileCache();

        System.out.println(fc.size());
//        fc.flush();

        fc.put("12", "some new value");
        fc.put("2", "some new value(2)");

        System.out.println(fc.get("12"));
        System.out.println(fc.get("2"));

        System.out.println(fc.getAll());

        MemoryCache mc = new MemoryCache();
        mc.setMaxSize(0);

        mc.put("12", new Integer(123));
        mc.put("12", new Integer(1231));
        mc.put("1", new Integer(1));
        mc.put("2", new Integer(2));
        mc.put("4", new String("sfdsfs"));
        System.out.println(mc.get("4"));
        mc.setMaxSize(2);

        System.out.println(mc.get("12"));
        System.out.println(mc.get("1"));
        System.out.println(mc.get("2"));
        System.out.println(mc.size());

        System.out.println(mc.getAll());

        //  working with two level cache
        TwoLevelCache tlc = new TwoLevelCache();

        tlc.flush();
        tlc.setMaxSizeMemoryCache(50);
        tlc.setMaxSizeFileCache(100);
        for (int i = 0; i < MAX_ELEMENTS; i++){
            String object = "string with value " + i;

            tlc.put(String.valueOf(i), object);
        }

        printCache(tlc, "list of objects:");

        //  update frequency randomly
        for (int i = 0; i < MAX_ELEMENTS; i++){
            String key = String.valueOf((int)(i * Math.random()));
            tlc.get(key);
        }

        printCache(tlc, "list of objects after frequent use:");

        TwoLevelCache.CacheStrategy cacheStrategy = TwoLevelCache.CacheStrategy.FREQUENTTOMEMORY;
        tlc.setCacheStrategy(cacheStrategy);
        printCache(tlc, "list of objects after applying cache strategy (" + cacheStrategy + ")");

        cacheStrategy = TwoLevelCache.CacheStrategy.FREQUENTTOFILE;
        tlc.setCacheStrategy(cacheStrategy);
        printCache(tlc, "list of objects after applying cache strategy (" + cacheStrategy + ")");

        cacheStrategy = TwoLevelCache.CacheStrategy.OLDTOFILE;
        tlc.setCacheStrategy(cacheStrategy);
        printCache(tlc, "list of objects after applying cache strategy (" + cacheStrategy + ")");

        cacheStrategy = TwoLevelCache.CacheStrategy.OLDTOMEMORY;
        tlc.setCacheStrategy(cacheStrategy);
        printCache(tlc, "list of objects after applying cache strategy (" + cacheStrategy + ")");

        tlc.setMaxSizeFileCache(5);
    }
}
