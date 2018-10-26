import com.kudoji.cman.cache.CacheObject;
import com.kudoji.cman.cache.FileCache;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class FileCacheTest {
    private FileCache fc;
    private static final String key1 = "key1";
    private static final String key2 = "key2";
    private static final String key3 = "key3";
    private static final String object1 = "object1";
    private static final String object2 = "object2";
    private static final String object3 = "object3";


    @Test
    public void testSize(){
        fc = new FileCache();
        fc.flush();

        assertEquals(0, fc.size());

        fc.put(key1, object1);
        assertEquals(fc.size(), 1);

        fc.put(key1, object2);
        assertEquals(fc.size(), 1);
    }

    @Test
    public void testPut(){
        fc = new FileCache();
        fc.flush();

        assertTrue(fc.put(key1, object1));
        assertTrue(fc.put(key1, object1));
        assertEquals(1, fc.size());

        assertTrue(fc.put(key2, object2));
        assertEquals(2, fc.size());

        fc.setMaxSize(0);
        assertEquals(2, fc.size());

        fc.setMaxSize(1);
        assertEquals(1, fc.size());

        assertFalse(fc.put(key3, object2));
        assertFalse(fc.put(new CacheObject(key3, object2)));

        fc.setMaxSize(0);
        assertTrue(fc.put(key3, object2));
        assertTrue(fc.put(new CacheObject(key3, object2)));
    }

    @Test
    public void testGet(){
        fc = new FileCache();
        fc.flush();

        assertEquals(null, fc.get(key1));

        fc.put(key1, object3);
        assertEquals(object3, fc.get(key1));

        fc.put(key1, object1);
        assertEquals(object1, fc.get(key1));

        CacheObject cacheObject = new CacheObject(key1, object2);
        fc.put(cacheObject);
        assertEquals(object2, fc.get(key1));
    }
}
