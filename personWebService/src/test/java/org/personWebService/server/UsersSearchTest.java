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
public class UsersSearchTest extends TestCase {

  /**
   * @param args
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {

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
    String appUrlBase = PersonWebServiceServerConfig.retrieveConfig().propertyValueStringRequired("personWsServer.appUrlBase");
    
    
    Credentials defaultcreds = new UsernamePasswordCredentials(user, pass);

    //set auth scope to null and negative so it applies to all hosts and ports
    httpClient.getState().setCredentials(new AuthScope(null, -1), defaultcreds);
    
    //xCiferSplitTrim=true&filter=xCiferDescription co "Joh Smi" 
    //HttpMethodBase httpMethodBase = new GetMethod("http://localhost:8089/personWebService/personWs/v1/Users?xCiferSplitTrim=true&filter=urn:scim:schemas:extension:cifer:2.0:User:searchDescription%20co%20%22schm%22&contentType=json&indent=true");
    
    //HttpMethodBase httpMethodBase = new GetMethod("https://personwebservice-test.apps.upenn.edu/personWebService/personWs/v1/Users?xCiferSplitTrim=true&filter=urn:scim:schemas:extension:cifer:2.0:User:searchDescription%20co%20%22schm%22&contentType=json&indent=true");
    HttpMethodBase httpMethodBase = new GetMethod(appUrlBase + "personWs/v1/Users?xCiferSplitTrim=true&filter=urn:scim:schemas:extension:cifer:2.0:User:searchDescription%20co%20%22schm%22&contentType=json&indent=true");
    
    //HttpMethodBase httpMethodBase = new GetMethod("https://personwebservice-test.apps.upenn.edu/personWebService/personWs/v1/Users?xCiferSplitTrim=true&filter=xCiferDescription%20co%20%22Chris%20yzer%22&contentType=json&indent=true");

    //HttpMethodBase httpMethodBase = new GetMethod("http://localhost:8089/personWebService/personWs/v1/Users?xCiferSplitTrim=true&filter=userName%20eq%20%22mchyzer%22&contentType=json&indent=true");
    //HttpMethodBase httpMethodBase = new GetMethod("https://fasttest-small-a-01.apps.upenn.edu/personWebService/personWs/v1/Users?xCiferSplitTrim=true&filter=userName%20eq%20%22mchyzer%22&contentType=json&indent=true");

    //filter=userName eq "jsmith" 
    
    httpMethodBase.setRequestHeader("Connection", "close");
    
    int responseCodeInt = httpClient.executeMethod(httpMethodBase);

    System.out.println("Response code: " + responseCodeInt);
    System.out.println("");
    System.out.println(httpMethodBase.getResponseBodyAsString());
    
  }

}
