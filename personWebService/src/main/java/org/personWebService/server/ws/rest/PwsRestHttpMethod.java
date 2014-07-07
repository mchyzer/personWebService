/*
 * @author mchyzer $Id: TfRestHttpMethod.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.personWebService.server.ws.rest;

import java.util.List;
import java.util.Map;

import org.personWebService.server.util.PersonWsServerUtils;
import org.personWebService.server.ws.corebeans.PwsResponseBean;


/**
 * types of http methods accepted by rest
 */
public enum PwsRestHttpMethod {

  /** GET */
  GET {

    /**
     * @see PwsRestHttpMethod#service(List, Map, String)
     */
    @Override
    public PwsResponseBean service(List<String> urlStrings,
        Map<String, String> params, String body) {
            
      if (urlStrings.size() == 0) {

        throw new RuntimeException("No expecting this request");
      
      }

      String firstResource = PersonWsServerUtils.popUrlString(urlStrings);
      
      //validate and get the first resource
      PwsRestGet tfRestGet = PwsRestGet.valueOfIgnoreCase(
          firstResource, true);
  
      return tfRestGet.service(urlStrings, params, body);

    }


  },

  /** POST */
  POST {

    /**
     * @see PwsRestHttpMethod#service(List, Map, String)
     */
    @Override
    public PwsResponseBean service(List<String> urlStrings,
        Map<String, String> params, String body) {

      throw new RuntimeException("No expecting this request");

    }


  },

  /** PUT */
  PUT {

    /**
     * @see PwsRestHttpMethod#service(List, Map, String)
     */
    @Override
    public PwsResponseBean service(List<String> urlStrings,
        Map<String, String> params, String body) {
      throw new RuntimeException("No expecting this request");
    }


  },

  /** DELETE */
  DELETE {

    /**
     * @see PwsRestHttpMethod#service(List, Map, String)
     */
    @Override
    public PwsResponseBean service(List<String> urlStrings,
        Map<String, String> params, String body) {
      throw new RuntimeException("No expecting this request");
    }


  };

  /**
   * handle the incoming request based on HTTP method
   * @param body version of client, e.g. v1_3_000
   * @param urlStrings not including the app name or servlet.  for http://localhost/ws/servicesRest/groups/a:b
   * the urlStrings would be size two: {"group", "a:b"}
   * @param params is the request body converted to object
   * @return the resultObject
   */
  public abstract PwsResponseBean service(
      List<String> urlStrings, Map<String, String> params, String body);

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNotFound true to throw exception if method not found
   * @return the enum or null or exception if not found
   * @throws RuntimeException if there is a problem
   */
  public static PwsRestHttpMethod valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) throws RuntimeException {
    return PersonWsServerUtils.enumValueOfIgnoreCase(PwsRestHttpMethod.class, string, exceptionOnNotFound);
  }
}
