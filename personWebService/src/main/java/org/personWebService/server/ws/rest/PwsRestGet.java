package org.personWebService.server.ws.rest;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.personWebService.server.hibernate.PersonWsDaoFactory;
import org.personWebService.server.j2ee.PersonWsFilterJ2ee;
import org.personWebService.server.util.PersonWsServerUtils;
import org.personWebService.server.ws.corebeans.PwsResponseBean;



/**
 * enum for post requests
 * @author mchyzer
 *
 */
public enum PwsRestGet {

  /** Users get request */
  Users {

    /**
     * handle the incoming request based on GET HTTP method and groups resource
     * @param urlStrings not including the app name or servlet.  
     * for ahttp://localhost:8088/personWebService/personWs/v1/Users/12345678?contentType=json
     * @return the result object
     */
    @Override
    public PwsResponseBean service(List<String> urlStrings,
        Map<String, String> params, String body) {
      
      String id = PersonWsServerUtils.length(urlStrings) > 0 ? urlStrings.get(0) : null;
      
      if (PersonWsServerUtils.length(urlStrings) > 1) {
        throw new PwsRestInvalidRequest("Not expecting more than 1 url string: " + PersonWsServerUtils.toStringForLog(urlStrings));
      }
      
      return PwsRestLogic.userById(PersonWsDaoFactory.getFactory(), id, params);
      
    }

  }
  ;

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNotFound true if exception should be thrown on not found
   * @return the enum or null or exception if not found
   * @throws PwsRestInvalidRequest if there is a problem
   */
  public static PwsRestGet valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) throws PwsRestInvalidRequest {
    return PersonWsServerUtils.enumValueOfIgnoreCase(PwsRestGet.class, 
        string, exceptionOnNotFound);
  }

  /**
   * handle the incoming request based on HTTP method
   * @param urlStrings not including the app name or servlet.  for http://localhost/grouper-ws/servicesRest/groups/a:b
   * the urlStrings would be size two: {"group", "a:b"}
   * @param params 
   * @param body is the request body converted to object
   * @return the result object
   */
  public abstract PwsResponseBean service(
      List<String> urlStrings,
      Map<String, String> params, String body);

}
