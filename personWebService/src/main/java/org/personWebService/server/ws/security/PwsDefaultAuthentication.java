
/*
 * @author mchyzer
 * $Id: WsTfDefaultAuthentication.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.personWebService.server.ws.security;

import javax.servlet.http.HttpServletRequest;

import org.personWebService.server.j2ee.PersonWsFilterJ2ee;



/**
 * default authentication for grouper if a custom one isnt specified in
 * grouper-ws.properties for non-rampart requests
 */
public class PwsDefaultAuthentication implements PwsCustomAuthentication {

  /**
   * 
   * @see PwsCustomAuthentication#retrieveLoggedInSubjectId(javax.servlet.http.HttpServletRequest)
   */
  public String retrieveLoggedInSubjectId(HttpServletRequest httpServletRequest)
      throws RuntimeException {
    
    // use this to be the user connected, or the user act-as
    String userIdLoggedIn = PersonWsFilterJ2ee.retrieveUserIdFromRequest(false);

    return userIdLoggedIn;
  }

}
