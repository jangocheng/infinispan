package org.infinispan.test;

import static java.lang.String.format;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.concurrent.TimeUnit;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.test.fwk.CleanupAfterMethod;
import org.infinispan.test.fwk.CleanupAfterTest;
import org.infinispan.test.fwk.TestCacheManagerFactory;

/**
 * Base class for {@link org.infinispan.test.SingleCacheManagerTest} and {@link org.infinispan.test.MultipleCacheManagersTest}.
 *
 * @author Mircea.Markus@jboss.com
 */
public abstract class AbstractCacheTest extends AbstractInfinispanTest {

   public enum CleanupPhase {
      AFTER_METHOD, AFTER_TEST
   }

   protected CleanupPhase cleanup = CleanupPhase.AFTER_TEST;

   protected boolean cleanupAfterTest() {
      return getClass().getAnnotation(CleanupAfterTest.class) != null || (
              getClass().getAnnotation(CleanupAfterMethod.class) == null &&
                      cleanup == CleanupPhase.AFTER_TEST
      );
   }

   protected boolean cleanupAfterMethod() {
      return getClass().getAnnotation(CleanupAfterMethod.class) != null || (
              getClass().getAnnotation(CleanupAfterTest.class) == null &&
                      cleanup == CleanupPhase.AFTER_METHOD
      );
   }

   public static ConfigurationBuilder getDefaultClusteredCacheConfig(CacheMode mode) {
      return getDefaultClusteredCacheConfig(mode, false, false);
   }

   public static ConfigurationBuilder getDefaultClusteredCacheConfig(CacheMode mode, boolean transactional) {
      return getDefaultClusteredCacheConfig(mode, transactional, false);
   }

   public static ConfigurationBuilder getDefaultClusteredCacheConfig(CacheMode mode, boolean transactional, boolean useCustomTxLookup) {
      ConfigurationBuilder builder = TestCacheManagerFactory.getDefaultCacheConfiguration(transactional, useCustomTxLookup);
      builder.
         clustering()
            .cacheMode(mode)
         .transaction().cacheStopTimeout(0L);

      return builder;
   }

   protected boolean xor(boolean b1, boolean b2) {
      return (b1 || b2) && !(b1 && b2);
   }

   protected void assertEventuallyNotLocked(final Cache<?, ?> cache, final Object key) {
      //lock release happens async, hence the eventually...
      eventually(() -> format("Expected key '%s' to be unlocked on cache '%s'", key, cache),
            () -> !checkLocked(cache, key), 20000, TimeUnit.MILLISECONDS);
   }

   protected void assertEventuallyLocked(final Cache<?, ?> cache, final Object key) {
      eventually(() -> format("Expected key '%s' to be locked on cache '%s'", key, cache),
            () -> checkLocked(cache, key), 20000, TimeUnit.MILLISECONDS);
   }

   protected void assertLocked(Cache<?, ?> cache, Object key) {
      assertTrue(format("Expected key '%s' to be locked on cache '%s'", key, cache), checkLocked(cache, key));
   }

   protected void assertNotLocked(Cache<?, ?> cache, Object key) {
      assertFalse(format("Expected key '%s' to not be locked on cache '%s'", key, cache), checkLocked(cache, key));
   }

   protected boolean checkLocked(Cache<?, ?> cache, Object key) {
      return TestingUtil.extractLockManager(cache).isLocked(key);
   }

   public EmbeddedCacheManager manager(Cache<?, ?> c) {
      return c.getCacheManager();
   }

   public String getDefaultCacheName() {
      return TestCacheManagerFactory.DEFAULT_CACHE_NAME;
   }
}
