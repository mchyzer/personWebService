/**
 * @author mchyzer
 * $Id: PersonWebServiceHibernateConfig.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.personWebService.server.config;

import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;


/**
 *
 */
public class PersonWebServiceHibernateConfig extends ConfigPropertiesCascadeBase {

  /**
   * use the factory
   */
  private PersonWebServiceHibernateConfig() {
    
  }
  
  /**
   * retrieve a config from the config file or from cache
   * @return the config object
   */
  public static PersonWebServiceHibernateConfig retrieveConfig() {
    return retrieveConfig(PersonWebServiceHibernateConfig.class);
  }

  /**
   * @see ConfigPropertiesCascadeBase#clearCachedCalculatedValues()
   */
  @Override
  public void clearCachedCalculatedValues() {
    
  }

  /**
   * @see ConfigPropertiesCascadeBase#getHierarchyConfigKey
   */
  @Override
  protected String getHierarchyConfigKey() {
    return "personWsHibernate.config.hierarchy";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getMainConfigClasspath
   */
  @Override
  protected String getMainConfigClasspath() {
    return "personWebService.hibernate.properties";
  }
  
  /**
   * @see ConfigPropertiesCascadeBase#getMainExampleConfigClasspath
   */
  @Override
  protected String getMainExampleConfigClasspath() {
    return "personWebService.hibernate.base.properties";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getSecondsToCheckConfigKey
   */
  @Override
  protected String getSecondsToCheckConfigKey() {
    return "personWsHibernate.config.secondsBetweenUpdateChecks";
  }

}
