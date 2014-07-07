/*
 * @author mchyzer $Id: PwsHttpServletRequest.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.personWebService.server.j2ee;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.collections.IteratorUtils;
import org.personWebService.server.config.PersonWebServiceServerConfig;
import org.personWebService.server.util.PersonWsServerUtils;
import org.personWebService.server.ws.rest.PwsRestHttpMethod;
import org.personWebService.server.ws.rest.PwsRestInvalidRequest;


/**
 * wrap request so that no nulls are given to axis (since it handles badly)
 */
public class PwsHttpServletRequest extends HttpServletRequestWrapper {
  
  /**
   * retrieve from threadlocal
   * @return the request
   */
  public static PwsHttpServletRequest retrieve() {
    return (PwsHttpServletRequest)PersonWsFilterJ2ee.retrieveHttpServletRequest();
  }
  
  /**
   * method for this request
   */
  private String method = null;
  
  /**
   * @see HttpServletRequest#getMethod()
   */
  @Override
  public String getMethod() {
    if (this.method == null) {
      //get it from the URL if it is there, lets not mess with method
      String methodString = null; //this.getParameter("method");
      if (PersonWsServerUtils.isBlank(methodString)) {
        methodString = super.getMethod();
      }
      //lets see if it is a valid method
      PwsRestHttpMethod.valueOfIgnoreCase(methodString, true);
      this.method = methodString;
    }
    return this.method;
  }

  /**
   * @return original method from underlying servlet
   * @see HttpServletRequest#getMethod()
   */
  public String getOriginalMethod() {
    return super.getMethod();
  }

  /**
   * construct with underlying request
   * @param theHttpServletRequest
   */
  public PwsHttpServletRequest(HttpServletRequest theHttpServletRequest) {
    super(theHttpServletRequest);
  }

  /**
   * @see javax.servlet.ServletRequestWrapper#getParameter(java.lang.String)
   */
  @Override
  public String getParameter(String name) {
    return this.getParameterMap().get(name);
  }

  /**
   * @param name 
   * @return the boolean
   * @see javax.servlet.ServletRequestWrapper#getParameter(java.lang.String)
   */
  public Boolean getParameterBoolean(String name) {
    return PersonWsServerUtils.booleanObjectValue(this.getParameterMap().get(name));
  }

  /**
   * @param name 
   * @return the parameter long
   * @see javax.servlet.ServletRequestWrapper#getParameter(java.lang.String)
   */
  public Long getParameterLong(String name) {
    return PersonWsServerUtils.longObjectValue(this.getParameterMap().get(name), true);
  }

  /** param map which doesnt return null */
  private Map<String, String> parameterMap = null;

  /** unused http params */
  private Set<String> unusedParams = null;

  /**
   * return unused params that arent in the list to ignore
   * @return the unused params
   */
  public Set<String> unusedParams() {
    //init stuff
    this.getParameterMap();
    return this.unusedParams;
  }
  
  /**
   * @see javax.servlet.ServletRequestWrapper#getParameterMap()
   */
  @Override
  public Map<String, String> getParameterMap() {

    if (this.parameterMap == null) {
      boolean valuesProblem = false;
      Set<String> valuesProblemName = new LinkedHashSet<String>();
      Map<String, String> newMap = new LinkedHashMap<String, String>();
      Set<String> newUnusedParams = new LinkedHashSet<String>();

      @SuppressWarnings("unchecked")
      Enumeration<String> enumeration = super.getParameterNames();
      Set<String> paramsToIgnore = new HashSet<String>();
      {
        String paramsToIgnoreString = PersonWebServiceServerConfig.retrieveConfig().propertyValueString("personWsServer.httpParamsToIgnore");
        if (!PersonWsServerUtils.isBlank(paramsToIgnoreString)) {
          paramsToIgnore.addAll(PersonWsServerUtils.splitTrimToList(paramsToIgnoreString, ","));
        }
      }

      String httpParamsOkMultipleString = PersonWebServiceServerConfig.retrieveConfig().propertyValueString("personWsServer.httpParamsOkMultiple");
      Set<String> httpParamsOkMultipleSet = PersonWsServerUtils.nonNull(PersonWsServerUtils.splitTrimToSet(httpParamsOkMultipleString, ","));
      
      if (enumeration != null) {
        while(enumeration.hasMoreElements()) {
          @SuppressWarnings("cast")
          String paramName = (String)enumeration.nextElement();
          
          String[] values = super.getParameterValues(paramName);
          String value = null;
          if (values != null && values.length > 0) {
            
            //there is probably something wrong if multiple values detected
            if (values.length > 1 && !httpParamsOkMultipleSet.contains(paramName)) {
              valuesProblem = true;
              valuesProblemName.add(paramName);
            }
            value = values[0];
          }
          newMap.put(paramName, value);
        }
      }
      this.parameterMap = newMap;
      this.unusedParams = newUnusedParams;
      if (valuesProblem) {
        throw new PwsRestInvalidRequest(
            "Multiple request parameter values where detected for key: " + PersonWsServerUtils.toStringForLog(valuesProblemName)
                + ", when only one is expected");
      }
    }
    return this.parameterMap;
  }

  /**
   * @see javax.servlet.ServletRequestWrapper#getParameterNames()
   */
  @SuppressWarnings("unchecked")
  @Override
  public Enumeration<String> getParameterNames() {
    return IteratorUtils.asEnumeration(this.getParameterMap().keySet().iterator());
  }

  /**
   * @see javax.servlet.ServletRequestWrapper#getParameterValues(java.lang.String)
   */
  @Override
  public String[] getParameterValues(String name) {
    if (this.getParameterMap().containsKey(name)) {
      return new String[]{this.getParameterMap().get(name)};
    }
    return null;
  }

}
