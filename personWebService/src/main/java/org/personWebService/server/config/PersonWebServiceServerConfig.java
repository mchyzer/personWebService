/**
 * @author mchyzer
 * $Id: PersonWebServiceServerConfig.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.personWebService.server.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.personWebService.server.authz.PersonWsAuthorizationInterface;
import org.personWebService.server.util.PersonWsServerUtils;

import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;


/**
 *
 */
public class PersonWebServiceServerConfig extends ConfigPropertiesCascadeBase {

  
  /**
   * cache the tf server authz
   */
  private Map<String, String> tfServerAuthz = null;


  /**
   * use the factory
   */
  private PersonWebServiceServerConfig() {
  }

  
  /**
   * retrieve a config from the config file or from cache
   * @return the config object
   */
  public static PersonWebServiceServerConfig retrieveConfig() {
    return retrieveConfig(PersonWebServiceServerConfig.class);
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
    return "personWsServer.config.hierarchy";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getMainConfigClasspath
   */
  @Override
  protected String getMainConfigClasspath() {
    return "personWebService.server.properties";
  }
  
  /**
   * @see ConfigPropertiesCascadeBase#getMainExampleConfigClasspath
   */
  @Override
  protected String getMainExampleConfigClasspath() {
    return "personWebService.server.base.properties";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getSecondsToCheckConfigKey
   */
  @Override
  protected String getSecondsToCheckConfigKey() {
    return "personWsServer.config.secondsBetweenUpdateChecks";
  }


  /**
   * get the authorization implementation
   * @return the authz
   */
  public PersonWsAuthorizationInterface personWsAuthorization() {
    
    String className = PersonWebServiceServerConfig.retrieveConfig().propertyValueString("personWsServer.personWsAuthzImplementation");
    @SuppressWarnings("unchecked")
    Class<PersonWsAuthorizationInterface> personWsAuthorizationInterfaceClass = PersonWsServerUtils.forName(className);
    PersonWsAuthorizationInterface personWsAuthorizationInterface = PersonWsServerUtils.newInstance(personWsAuthorizationInterfaceClass);
    return personWsAuthorizationInterface;
  
  }


  /**
   * tf server authz
   * @return the map
   */
  public Map<String, String> pwsServerAuthz() {
    
    //ws.authz.tfServer.local.principal
    if (this.tfServerAuthz == null) {
      
      synchronized (this) {
        
        if (this.tfServerAuthz == null) {
          
          Map<String, String> map = new HashMap<String, String>();
          
          Pattern pattern = Pattern.compile("^ws\\.authz\\.tfServer\\.(.*)\\.principal$");
          
          for (Object keyObject : this.properties().keySet()) {
            String key = (String)keyObject;
            Matcher matcher = pattern.matcher(key);
            if (matcher.matches()) {
              
              String tfServerName = matcher.group(1);
              
              String principal = this.propertyValueString(key);
              String networks = this.propertyValueString("ws.authz.tfServer." + tfServerName + ".networks");
              
              map.put(principal, networks);
              
            }
          }
          this.tfServerAuthz = Collections.unmodifiableMap(map);
        }
      }
    }
    
    return this.tfServerAuthz;
    
  }

}
