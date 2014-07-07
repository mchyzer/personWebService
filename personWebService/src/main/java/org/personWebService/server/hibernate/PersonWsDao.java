
package org.personWebService.server.hibernate;
import java.io.File;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.personWebService.server.beans.PersonWsDaemonLog;
import org.personWebService.server.config.PersonWebServiceHibernateConfig;
import org.personWebService.server.config.PersonWebServiceServerConfig;
import org.personWebService.server.util.PersonWsServerUtils;

import edu.internet2.middleware.grouperClientExt.edu.internet2.middleware.morphString.Morph;


/**
 * Base Hibernate DAO interface.
 * @version $Id: PersonWsDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
public abstract class PersonWsDao {

  /**
   * 
   */
  private static Configuration  CFG;

  /**
   * 
   */
  private static SessionFactory FACTORY;

  /** logger */
  private static final Log LOG = PersonWsServerUtils.getLog(PersonWsDao.class);

  /**
   * keep track of if hibernate is initted yet, allow resets... (e.g. for testing)
   */
  public static boolean hibernateInitted = false;
  
  /**
   * init hibernate if not initted
   */
  public static void initHibernateIfNotInitted() {
    if (hibernateInitted) {
      return;
    }
    
    synchronized(PersonWsDao.class) {
      if (hibernateInitted) {
        return;
      }

      try {
        // Find the custom configuration file
        Properties  p   = PersonWebServiceHibernateConfig.retrieveConfig().properties();
        
        //unencrypt pass
        if (p.containsKey("hibernate.connection.password")) {
          String newPass = Morph.decryptIfFile(p.getProperty("hibernate.connection.password"));
          p.setProperty("hibernate.connection.password", newPass);
        }
        
        String connectionUrl = StringUtils.defaultString(PersonWsServerUtils.propertiesValue(p,"hibernate.connection.url"));

        {
          String dialect = StringUtils.defaultString(PersonWsServerUtils.propertiesValue(p,"hibernate.dialect"));
          dialect = PersonWsServerUtils.convertUrlToHibernateDialectIfNeeded(connectionUrl, dialect);
          p.setProperty("hibernate.dialect", dialect);
        }
        
        {
          String driver = StringUtils.defaultString(PersonWsServerUtils.propertiesValue(p,"hibernate.connection.driver_class"));
          driver = PersonWsServerUtils.convertUrlToDriverClassIfNeeded(connectionUrl, driver);
          p.setProperty("hibernate.connection.driver_class", driver);
        }      
        
        // And now load all configuration information
        CFG = new Configuration().addProperties(p);
        addClass(CFG, PersonWsDaemonLog.class);
        CFG.setInterceptor(new PwsSessionInterceptor());
     
        // And finally create our session factory
        //trying to avoid warning of using the same dir
        String tmpDir = PersonWsServerUtils.tmpDir();
        try {
          String newTmpdir = StringUtils.trimToEmpty(tmpDir);
          if (!newTmpdir.endsWith("\\") && !newTmpdir.endsWith("/")) {
            newTmpdir += File.separator;
          }
          newTmpdir += "twoFactor_ehcache_auto_" + PersonWsServerUtils.uniqueId();
          System.setProperty(PersonWsServerUtils.JAVA_IO_TMPDIR, newTmpdir);
          
          //now it should be using a unique directory
          FACTORY = CFG.buildSessionFactory();
        } finally {
          
          //put tmpdir back
          if (tmpDir == null) {
            System.clearProperty(PersonWsServerUtils.JAVA_IO_TMPDIR);
          } else {
            System.setProperty(PersonWsServerUtils.JAVA_IO_TMPDIR, tmpDir);
          }
        }

      } catch (Throwable t) {
        String msg = "unable to initialize hibernate: " + t.getMessage();
        LOG.fatal(msg, t);
        throw new RuntimeException(msg, t);
      }
      //this might not be completely accurate
      hibernateInitted = true;

    
    }
    

  }
  
  /**
   * 
   * @param _CFG
   * @param mappedClass
   */
  private static void addClass(Configuration _CFG, Class<?> mappedClass) {
    addClass(_CFG, mappedClass, null);
  }

  /**
   * 
   * @param _CFG
   * @param mappedClass
   * @param entityNameXmlFileNameOverride send in an entity name if the entity name and xml file are different than
   * the class file.
   */
  private static void addClass(Configuration _CFG, Class<?> mappedClass, String entityNameXmlFileNameOverride) {
    String resourceName = resourceNameFromClassName(mappedClass, entityNameXmlFileNameOverride);
    String xml = PersonWsServerUtils.readResourceIntoString(resourceName, false);
    
    if (xml.contains("<version")) {
      
      //if versioned, then make sure the setting in class is there
      String optimisiticLockVersion = "optimistic-lock=\"version\"";
      
      if (!StringUtils.contains(xml, optimisiticLockVersion)) {
        throw new RuntimeException("If there is a versioned class, it must contain " +
        		"the class level attribute: optimistic-lock=\"version\": " + mappedClass.getName() + ", " + resourceName);
      }
      
      //if versioned, then see if we are disabling
      boolean optimisiticLocking = PersonWebServiceServerConfig.retrieveConfig().propertyValueBoolean(
          "dao.optimisticLocking", true);
      
      if (!optimisiticLocking) {
        xml = StringUtils.replace(xml, optimisiticLockVersion, "optimistic-lock=\"none\"");
      }
    }
    _CFG.addXML(xml);

  }

  /**
   * class is e.g. edu.school.dto.Attribute,
   * must return e.g. edu.school.internal.dao.hib3.Hib3AttributeDAO
   * @param theClass
   * @param entityNameXmlFileNameOverride pass in an override if the entity name and xml file are different than
   * the class file
   * @return the string of resource
   */
  public static String resourceNameFromClassName(Class theClass, String entityNameXmlFileNameOverride) {
    String daoClass = theClass.getName();
    if (!StringUtils.isBlank(entityNameXmlFileNameOverride)) {
      daoClass = StringUtils.replace(daoClass, theClass.getSimpleName(), entityNameXmlFileNameOverride);
    }
    //replace with hbm
    String result = StringUtils.replace(daoClass, ".", "/") + ".hbm.xml";
    
    return result;
  }
  
  /**
   * @return the configuration
   * @throws HibernateException
   */
  public static Configuration getConfiguration()
    throws  HibernateException {
    return CFG;
  }

  /**
   * DONT CALL THIS METHOD, IT IS FOR INTERNAL FRAMEWORK USE
   * ONLY.  Use the HibernateSession callback to get a hibernate Session
   * object
   * @return the session
   * @throws HibernateException
   */
	public static Session session()
    throws  HibernateException {
	  //just in case
	  initHibernateIfNotInitted();
		return FACTORY.openSession();
	} 

	/**
	 * evict a persistent class
	 * @param persistentClass
	 */
	public static void evict(Class persistentClass) {
	  FACTORY.getCache().evictEntityRegion(persistentClass);
	}
	
  /**
   * evict a persistent class
   * @param entityName
   */
  public static void evictEntity(String entityName) {
    FACTORY.getCache().evictEntityRegion(entityName);
  }
  
  /**
   * evict a persistent class
   * @param cacheRegion
   */
  public static void evictQueries(String cacheRegion) {
    FACTORY.getCache().evictQueryRegion(cacheRegion);
  }
  
} 

