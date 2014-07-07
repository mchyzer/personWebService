
package org.personWebService.server.cache;
import  net.sf.ehcache.Statistics;


/**
 * Wrapper around ehcache <i>Statistics</i> class.
 * @version $Id: PwsEhcacheStats.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
public class PwsEhcacheStats implements PwsCacheStats {

  /** stats */
  private Statistics stats;



  /**
   * Instantiate new <i>PwsEhcacheStats</i> object.
   * @param stats1
   * @throws  IllegalArgumentException if <i>stats</i> is null.
   */
  public PwsEhcacheStats(Statistics stats1) 
    throws  IllegalArgumentException
  {
    if (stats1 == null) { 
      throw new IllegalArgumentException("null Statistics");
    }
    this.stats = stats1;
  }



  /**
   * @return  Number of cache hits.
   */
  public long getHits() {
    return this.stats.getCacheHits();
  }

  /**
   * @return  Number of cache misses.
   */
  public long getMisses() {
    return this.stats.getCacheMisses();
  }

  /**
   * @return  Number of objects in cache.
   */
  public long getSize() {
    return this.stats.getObjectCount();
  }

}

