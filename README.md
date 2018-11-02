# cman

cman is a configurable two-level cache manager (for caching Objects).

It has two levels:
* level 1: memory cache;
* level 2: filesystem.

cman allows to specify different cache strategies and max sizes of level 1 and 2 caches.


The initial task looks as follows:
> "Create a configurable two-level cache (for caching Objects).
Level 1 is memory, level 2 is filesystem.
Config params should let one specify the cache strategies and max sizes of level 1 and 2."


# known issues

The assumption which leads to the issue described below is **FileCache can be re-used after program re-launch, thus metadata has to be stored along with cached object (inside the each file where the object resides)**.

If we don't care about **frequency** and **age** on the first and second cache's levels then issue can be easily solved.

Cache interface has two methods

* boolean put(CacheObject<K, V> cacheObject);
* List<CacheObject<K, V>> getAll();

which expose inner CacheObject is an object's wrapper over cached one.

The wrapped is used to keep metadata about cached object such as object's **frequency** and **age**.

This metadata is needed in TwoLevelCache class to apply different cache strategies.

To avoid the issue, TwoLevelCache could be using its own wrapper, but it wouldn't work with FileCache because **frequency** and **age** will be lost.

I couldn't find easier implementation that the proposed one.