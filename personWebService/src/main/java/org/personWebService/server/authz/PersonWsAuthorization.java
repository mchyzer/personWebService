/**
 * @author mchyzer
 * $Id: PersonWsAuthorization.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.personWebService.server.authz;

import java.util.HashSet;
import java.util.Set;

import org.personWebService.server.config.PersonWebServiceServerConfig;
import org.personWebService.server.util.PersonWsServerUtils;

/**
 * built in implmentation of the administrative actions interface, who 
 * is an admin, etc.  Just check the config file
 */
public class PersonWsAuthorization implements PersonWsAuthorizationInterface {

  /**
   * @see PersonWsAuthorizationInterface#adminUserIdsWhoCanBackdoorAsOtherUsers()
   */
  @Override
  public Set<String> adminUserIdsWhoCanBackdoorAsOtherUsers() {
    
    // if using the default config file based authz, this is the comma separated list of admin ids
    String adminsAllowedToActAsOtherUsers = PersonWebServiceServerConfig.retrieveConfig().propertyValueString(
        "personWsServer.adminsAllowedToActAsOtherUsers");
    
    if (PersonWsServerUtils.isBlank(adminsAllowedToActAsOtherUsers)) {
      return new HashSet<String>();
    }

    return PersonWsServerUtils.splitTrimToSet(adminsAllowedToActAsOtherUsers, ",");

  }

}
