import com.kudoji.cman.cache.CacheObject;
import com.kudoji.cman.cache.MemoryCache;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;


public class MemoryCacheTest{
    private static MemoryCache mc;
    private static final String key1 = "key1";
    private static final String key2 = "key2";
    private static final String key3 = "key3";
    private static final String object1 = "object1";
    private static final String object2 = "object2";
    private static final String object3 = "object3";

    @BeforeClass
    public static void beforeClass(){
        mc = new MemoryCache();
    }

    @Test
    public void testPut(){
        assertTrue(mc.put(key1, object1));
        assertTrue(mc.put(key1, object1));
        assertEquals(object1, mc.get(key1));
        assertEquals(1, mc.size());

        assertTrue(mc.put(key1, object2));
        assertEquals(1, mc.size());
        assertEquals(object2, mc.get(key1));

        assertTrue(mc.put(key2, object2));
        assertEquals(2, mc.size());

        assertTrue(mc.setMaxSize(1));
        assertEquals(1, mc.size());

        CacheObject cacheObject = new CacheObject(key1, object1);

        assertTrue(mc.put(cacheObject));
        assertEquals(1, mc.size());

        cacheObject = new CacheObject(key2, object2);
        assertFalse(mc.put(cacheObject));
        assertEquals(1, mc.size());

        mc.setMaxSize(10);
        assertTrue(mc.put(cacheObject));
        assertEquals(2, mc.size());
    }

    @Test
    public void testGet(){
        mc.put(key1, object1);
        assertNotNull(mc.get(key1));

        mc.delete(key3);
        assertNull(mc.get(key3));
    }

    @Test
    public void testDelete(){
        mc.delete(key3);

        assertFalse(mc.delete(key3));

        mc.put(key1, object1);
        int size = mc.size();
        assertTrue(mc.delete(key1));
        assertEquals(size - 1, mc.size());

        mc.put(key1, object1);
    }

    @Test
    public void testSize(){
    }

    @Test
    public void testSetMaxSize(){
        assertFalse(mc.setMaxSize(-2));

        assertTrue(mc.setMaxSize(0));
        assertTrue(mc.setMaxSize(4));
    }

    @Test
    public void testFlush(){
        mc.put(key1, object1);

        assertTrue(mc.size() != 0);

        mc.flush();
        assertEquals(0, mc.size());
    }
}