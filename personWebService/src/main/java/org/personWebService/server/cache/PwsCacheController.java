
package org.personWebService.server.cache;


/**
 * Interface for common cache operations.
 * @version $Id: PwsCacheController.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
public interface PwsCacheController {

 
  /**
   * Flush all caches.
   */
  void flushCache();

  /**
   * @param cache is the name of the cache
   * @return  ehcache statistics for <i>cache</i>.
   */
  PwsCacheStats getStats(String cache);

  /** 
   * Initialize privilege cache.
   */
  void initialize();
  
}

