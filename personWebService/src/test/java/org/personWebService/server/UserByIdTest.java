/**
 * @author mchyzer
 * $Id$
 */
package org.personWebService.server;

import junit.framework.TestCase;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.DefaultHttpParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.personWebService.server.config.PersonWebServiceServerConfig;


/**
 * test user by id
 */
public class UserByIdTest extends TestCase {

  /**
   * @param args
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {

//    System.out.println(new DateTime(System.currentTimeMillis()));
//    
//    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS");
//
//    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
//    
//    System.out.println(
//          simpleDateFormat
//        .format(new Date()) + "Z");
//    
//    if (true) {
//      return;
//    }
    
    HttpClient httpClient = new HttpClient();
    
    DefaultHttpParams.getDefaultParams().setParameter(
        HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));

    httpClient.getParams().setAuthenticationPreemptive(true);
    
    int soTimeoutMillis = 180000;
    
    httpClient.getParams().setSoTimeout(soTimeoutMillis);
    httpClient.getParams().setParameter(HttpMethodParams.HEAD_BODY_CHECK_TIMEOUT, soTimeoutMillis);
    
    int connectionManagerMillis = 180000;
    
    httpClient.getParams().setConnectionManagerTimeout(connectionManagerMillis);

    String user = PersonWebServiceServerConfig.retrieveConfig().propertyValueStringRequired("personWsClient.webService.login");
    String pass = PersonWebServiceServerConfig.retrieveConfig().propertyValueStringRequired("personWsClient.webService.password");

    
    Credentials defaultcreds = new UsernamePasswordCredentials(user, pass);

    //set auth scope to null and negative so it applies to all hosts and ports
    httpClient.getState().setCredentials(new AuthScope(null, -1), defaultcreds);
    
    //    HttpMethodBase httpMethodBase = new GetMethod("http://localhost:8089/personWebService/personWs/v1/Users/10021368?contentType=json&indent=true");
    HttpMethodBase httpMethodBase = new GetMethod("https://fasttest-small-a-01.apps.upenn.edu/personWebService/personWs/v1/Users/10021368?contentType=json&indent=true");
    
    httpMethodBase.setRequestHeader("Connection", "close");
    
    int responseCodeInt = httpClient.executeMethod(httpMethodBase);

    System.out.println("Response code: " + responseCodeInt);
    System.out.println("");
    System.out.println(httpMethodBase.getResponseBodyAsString());
    
  }

}
