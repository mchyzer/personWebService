

package org.personWebService.server.hibernate;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.personWebService.server.dao.PersonWsDaemonLogDao;
import org.personWebService.server.hibernate.dao.HibernateDaoFactory;
import org.personWebService.server.hibernate.dao.TransactionDAO;



/** 
 * Factory for returning <code>DAO</code> objects.
 * <p/>
 * @version $Id: PersonWsDaoFactory.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
public abstract class PersonWsDaoFactory {

  /**
   * 
   */
  private static PersonWsDaoFactory gdf;


  /**
   * Return singleton {@link PersonWsDaoFactory} implementation.
   * <p/>
   * @return factory
   * @since   1.2.0
   */
  public static PersonWsDaoFactory getFactory() {
    if (gdf == null) {
      gdf = getFactoryHelper( );
    }
    return gdf;
  } 

  /**
   * Return singleton {@link PersonWsDaoFactory} implementation using the specified
   * configuration.
   * <p/>
   * @return factory
   * @throws  IllegalArgumentException if <i>cfg</i> is null.
   */
  private static PersonWsDaoFactory getFactoryHelper() 
    throws  IllegalArgumentException
  {
    return new HibernateDaoFactory();
  }

  /**
   * @return the daemon log
   */
  public abstract PersonWsDaemonLogDao getTwoFactorDaemonLog();

  /**
   * 
   */
  public static void internal_resetFactory() {
    gdf = null;
  }

  /**
   * get a hibernate session (note, this is a framework method
   * that should not be called outside of hibernate framework methods
   * @return the session
   */
  public Session getSession() {
    return null;
  }
  
  /**
   * get a hibernate configuration (this is internal for dev team only)
   * @return the configuration
   */
  public Configuration getConfiguration() {
    return null;
  }

  /**
   * return the transaction implementation
   * @return the transaction implementation
   */
  public TransactionDAO getTransaction() {
    return null;
  }
  
} 

