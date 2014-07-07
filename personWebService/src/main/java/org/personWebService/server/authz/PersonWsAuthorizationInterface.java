/**
 * @author mchyzer
 * $Id: PersonWsAuthorizationInterface.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.personWebService.server.authz;

import java.util.Set;


/**
 * authorization interface for administrative actions.
 */
public interface PersonWsAuthorizationInterface {

  /**
   * you probably do not want to set this in production, and if so, do so temporarily
   * @return the userIds who can backdoor as other users
   */
  public Set<String> adminUserIdsWhoCanBackdoorAsOtherUsers();
}
