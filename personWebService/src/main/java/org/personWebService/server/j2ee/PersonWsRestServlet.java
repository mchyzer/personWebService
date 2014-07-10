/**
 * @author mchyzer
 * $Id: PersonWsRestServlet.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.personWebService.server.j2ee;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.log4j.NDC;
import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;
import org.personWebService.server.beans.PwsNode;
import org.personWebService.server.beans.PwsNode.PwsNodeType;
import org.personWebService.server.config.PersonWebServiceServerConfig;
import org.personWebService.server.daemon.DaemonController;
import org.personWebService.server.util.PersonWsServerUtils;
import org.personWebService.server.ws.corebeans.PwsResponseBean;
import org.personWebService.server.ws.corebeans.PwsResultProblem;
import org.personWebService.server.ws.rest.PwsRestContentType;
import org.personWebService.server.ws.rest.PwsRestHttpMethod;
import org.personWebService.server.ws.rest.PwsRestInvalidRequest;
import org.personWebService.server.ws.rest.PwsWsVersion;
import org.personWebService.server.ws.security.PwsCustomAuthentication;
import org.personWebService.server.ws.security.PwsDefaultAuthentication;




/**
 *
 */
@SuppressWarnings("serial")
public class PersonWsRestServlet extends HttpServlet {

  /**
   * @see javax.servlet.GenericServlet#init()
   */
  @Override
  public void init() throws ServletException {
    super.init();
    DaemonController.scheduleJobsOnce();

  }

  /**
   * retrieve the subject logged in to web service
   * 
   * @return the subject
   */
  @SuppressWarnings({ "unchecked" })
  public static String retrievePrincipalLoggedIn() {
    String authenticationClassName = PersonWebServiceServerConfig.retrieveConfig().propertyValueString(
        "ws.security.authentication.class");

    if (StringUtils.isBlank(authenticationClassName)) {
      authenticationClassName = PwsDefaultAuthentication.class.getName();
    }
    
    String userIdLoggedIn = null;

    //this is for container auth (or custom auth, non-rampart)
    //get an instance
    Class<? extends PwsCustomAuthentication> theClass = PersonWsServerUtils
        .forName(authenticationClassName);

    PwsCustomAuthentication wsAuthentication = PersonWsServerUtils.newInstance(theClass);

    userIdLoggedIn = wsAuthentication
        .retrieveLoggedInSubjectId(PersonWsFilterJ2ee.retrieveHttpServletRequest());

    // cant be blank!
    if (StringUtils.isBlank(userIdLoggedIn)) {
      //server is having trouble if got this far, but also the user's fault
      throw new PwsRestInvalidRequest("No user is logged in");
    }

    
    //puts it in the log4j ndc context so userid is logged
    if (NDC.getDepth() == 0) {
      StringBuilder ndcBuilder = new StringBuilder("< ");
      ndcBuilder.append(userIdLoggedIn).append(" - ");
      HttpServletRequest request = PersonWsFilterJ2ee.retrieveHttpServletRequest();
      if (request != null) {
        ndcBuilder.append(request.getRemoteAddr());
      }
      ndcBuilder.append(" >");
      NDC.push(ndcBuilder.toString());
    }
    
    return userIdLoggedIn;

  }

  /**
   * authorization.  make sure the logged in principal is in the authz group
   * for the tf server.  maybe check IP address too
   */
  public static void assertLoggedOnPrincipalTfServer() {
    String principal = retrievePrincipalLoggedIn();
    if (StringUtils.isBlank(principal)) {
      throw new PwsRestInvalidRequest("No logged in user to WS!");
    }
    
    Map<String, String> userToNetworks = PersonWebServiceServerConfig.retrieveConfig().pwsServerAuthz();
    
    if (!userToNetworks.containsKey(principal)) {
      StringBuilder error = new StringBuilder("User '" + principal + "' is not allowed to use the web service: "
          + userToNetworks.size() + " users are allowed: ");
      
      String errorForClient = error.toString();
      
      for (String userName: userToNetworks.keySet()) {
        error.append(", ").append(userName);
      }
      LOG.error(error);
      throw new PwsRestInvalidRequest(errorForClient.toString());
    }
    
    String networks = userToNetworks.get(principal);
    
    if (!StringUtils.isBlank(networks)) {

      HttpServletRequest httpServletRequest = PersonWsFilterJ2ee.retrieveHttpServletRequest();
      
      String clientIpAddress = httpServletRequest.getRemoteAddr();

      //if its equal, thats ok, might be ipv6
      if (!StringUtils.equals(networks, clientIpAddress)) {

        if (!PersonWsServerUtils.ipOnNetworks(clientIpAddress, networks)) {

          throw new PwsRestInvalidRequest("User '" + principal + "' is not allowed to use the web service from ip address: " + clientIpAddress);
          
        }
      }
    }
    
  }
  
  
  /** when this servlet was started */
  private static long startupTime = System.currentTimeMillis();

  /** logger */
  private static final Log LOG = PersonWsServerUtils.getLog(PersonWsRestServlet.class);

  /**
   * @return the startupTime
   */
  public static long getStartupTime() {
    return PersonWsRestServlet.startupTime;
  }

  /**
   * put an error or timeout to test the client
   * @param username
   */
  public static void testingErrorOrTimeout(String username) {

    String netId = username;
    
    {
      String sendTimeoutForUserIdsString = PersonWebServiceServerConfig.retrieveConfig().propertyValueString("personWsServer.ws.sendTimeoutForUserIds");
      //# user ids that should have a timeout
      //personWsServer.ws.sendTimeoutForUserIds =
      if (!StringUtils.isBlank(sendTimeoutForUserIdsString)) {
        Set<String> sendTimeoutForUserIds = PersonWsServerUtils.splitTrimToSet(sendTimeoutForUserIdsString, ",");
        if (sendTimeoutForUserIds.contains(username) || sendTimeoutForUserIds.contains(netId)) {
          
          //# integer percent of time there is an timeout, e.g. 33 (defaults to 100)
          //personWsServer.ws.sendTimeoutForUserIdsPercentOfTime =
          Integer percent = PersonWebServiceServerConfig.retrieveConfig().propertyValueInt("personWsServer.ws.sendTimeoutForUserIdsPercentOfTime");
          
          if (percent != null) {
            int randomInt = new Random().nextInt(100);
            if (percent > randomInt) {
              
              //# number of millis the WS should sleep for certain user ids
              //personWsServer.ws.sendTimeoutForUserIdsMillis = 
              int timeout = PersonWebServiceServerConfig.retrieveConfig().propertyValueInt("personWsServer.ws.sendTimeoutForUserIdsMillis", 10000);
              
              LOG.info("Sleeping " + timeout + "ms for username: " + (sendTimeoutForUserIds.contains(username) ? username : netId));
              
              PersonWsServerUtils.sleep(timeout);
              
            } else {
              LOG.info("Not sleeping since not percent of time for username: " + (sendTimeoutForUserIds.contains(username) ? username : netId));

            }
          }

        } 
      }
    }
    
    {
      //# user ids that should send errors
      //personWsServer.ws.sendErrorForUserIds =
      
      String sendErrorForUserIdsString = PersonWebServiceServerConfig.retrieveConfig().propertyValueString("personWsServer.ws.sendErrorForUserIds");
      
      if (!StringUtils.isBlank(sendErrorForUserIdsString)) {
        Set<String> sendErrorForUserIds = PersonWsServerUtils.splitTrimToSet(sendErrorForUserIdsString, ",");
        if (sendErrorForUserIds.contains(username) || sendErrorForUserIds.contains(netId)) {
          
          //# integer percent of time there is an error, e.g. 33  (defaults to 100)
          //personWsServer.ws.sendErrorForUserIdsPercentOfTime =
          Integer percent = PersonWebServiceServerConfig.retrieveConfig().propertyValueInt("personWsServer.ws.sendErrorForUserIdsPercentOfTime");

          if (percent != null) {
            int randomInt = new Random().nextInt(100);
            if (percent > randomInt) {

              LOG.info("Sending error for username: " + username);
          
              throw new RuntimeException("Configured to send error for username: " + (sendErrorForUserIds.contains(username) ? username : netId));          
            }
              
            LOG.info("Not sending error since not percent of time for username: " + username);
          }
        }
          
      }
    }
  }
  
  /**
   * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  @SuppressWarnings("unchecked")
  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    // if the WS should run in this env
    if (!PersonWebServiceServerConfig.retrieveConfig().propertyValueBoolean("personWsServer.runWs", true)) {
      throw new RuntimeException("WS doesnt run in this env per: personWsServer.runWs");
    }
    
    if (StringUtils.equals("true",request.getParameter("status"))) {
      return;
    }

    long serviceStarted = System.nanoTime();

    request = new PwsHttpServletRequest(request);
    PersonWsFilterJ2ee.assignHttpServletRequest(request);
    
    PersonWsFilterJ2ee.assignHttpServlet(this);
    List<String> urlStrings = null;
    StringBuilder warnings = new StringBuilder();

    PwsResponseBean pwsResponseBean = null;
    
    //we need something here if errors, so default to xhtml
    PwsRestContentType wsRestContentType = PwsRestContentType.json;
    PwsRestContentType.assignContentType(wsRestContentType);

    boolean logRequestsResponses = PersonWebServiceServerConfig.retrieveConfig().propertyValueBoolean("personWsServer.ws.log.requestsResponses", false);

    boolean indent = false;
    
    String body = null;
    
    try {
      
      if (PersonWsServerUtils.booleanValue(request.getParameter("indent"), false)) {
        indent = true;
      }
      
      //init params (if problem, exception will be thrown)
      request.getParameterMap();
      
      urlStrings = extractUrlStrings(request);
      int urlStringsLength = PersonWsServerUtils.length(urlStrings);

      //get the body and convert to an object
      body = PersonWsServerUtils.toString(request.getReader());

      PwsWsVersion clientVersion = null;

      //get the method and validate (either from object, or HTTP method
      PwsRestHttpMethod tfRestHttpMethod = null;
      {
        String methodString = request.getMethod();
        tfRestHttpMethod = PwsRestHttpMethod.valueOfIgnoreCase(methodString, true);
      }
      
      //if there are other content types, detect them here
      boolean foundContentType = false;
      {
        String contentType = request.getParameter("contentType");
        if (PersonWsServerUtils.equals(contentType, "json")) {
          wsRestContentType = PwsRestContentType.json;
          foundContentType = true;
        }
      }
      
      PwsRestContentType.assignContentType(wsRestContentType);

      if (foundContentType && urlStringsLength > 0) {
        
        String lastUrlString = urlStrings.get(urlStringsLength-1);
        if (lastUrlString.endsWith("." + wsRestContentType.name())) {
          lastUrlString = lastUrlString.substring(0, lastUrlString.length()-(1+wsRestContentType.name().length()));
        }
        urlStrings.set(urlStringsLength-1, lastUrlString);
      }
      
      if (urlStringsLength == 0) {
        
        if (tfRestHttpMethod != PwsRestHttpMethod.GET) {
          throw new PwsRestInvalidRequest("Cant have non-GET method for default resource: " + tfRestHttpMethod);
        }
        
        if (foundContentType) {
          
          //twoFactorResponseBeanBase = new AsasDefaultVersionResourceContainer();
          throw new PwsRestInvalidRequest("Invalid request");
        }
        //twoFactorResponseBeanBase = new AsasDefaultResourceContainer();
        throw new PwsRestInvalidRequest("Invalid request");

      }
        
      if (!foundContentType) {
        throw new PwsRestInvalidRequest("Invalid request " + request.getRequestURI());
      }
      
      //first see if version
      clientVersion = PwsWsVersion.valueOfIgnoreCase(PersonWsServerUtils.popUrlString(urlStrings), true);

      PwsWsVersion.assignCurrentClientVersion(clientVersion, warnings);
      
//      WsRequestBean requestObject = null;
//
//      if (!StringUtils.isBlank(body)) {
//        requestObject = (WsRequestBean) wsRestRequestContentType.parseString(body,
//            warnings);
//      }
//      
//      //might be in params (which might not be in body
//      if (requestObject == null) {
//        //might be in http params...
//        requestObject = (WsRequestBean) GrouperServiceUtils.marshalHttpParamsToObject(
//            request.getParameterMap(), request, warnings);
//
//      }
                  
      pwsResponseBean = tfRestHttpMethod.service(urlStrings, request.getParameterMap(), body);
    } catch (PwsRestInvalidRequest arir) {

      pwsResponseBean = new PwsResultProblem();
      String error = arir.getMessage() + ", " + requestDebugInfo(request);

      //this is a user error, but an error nonetheless
      LOG.error(error, arir);

      pwsResponseBean.setErrorMessage(error + PersonWsServerUtils.getFullStackTrace(arir));
      pwsResponseBean.setSuccess(false);

    } catch (RuntimeException e) {

      //this is not a user error, is a big problem

      pwsResponseBean = new PwsResultProblem();
      LOG.error("Problem with request: " + requestDebugInfo(request), e);
      pwsResponseBean.setErrorMessage("Problem with request: "
          + requestDebugInfo(request) + ",\n" + PersonWsServerUtils.getFullStackTrace(e));
      pwsResponseBean.setSuccess(false);

    }
    
    //set http status code, content type, and write the response
    StringBuilder urlBuilder = null;
    String responseString = null;
    String responseStringForLog = null;
    long responseTimeMillis = -1;
    
    try {
      { 
        urlBuilder = new StringBuilder();
        {
          String url = request.getRequestURL().toString();
          url = PersonWsServerUtils.prefixOrSuffix(url, "?", true);
          urlBuilder.append(url);
        }
        //lets put the params back on (the ones we expect)
        Map<String, String> paramMap = request.getParameterMap();
        boolean firstParam = true;
        for (String paramName : paramMap.keySet()) {
          if (firstParam) {
            urlBuilder.append("?");
          } else {
            urlBuilder.append("&");
          }
          firstParam = false;
          
          urlBuilder.append(PersonWsServerUtils.escapeUrlEncode(paramName))
            .append("=").append(PersonWsServerUtils.escapeUrlEncode(paramMap.get(paramName)));
          
        }
        
      }
      if (warnings.length() > 0) {
        pwsResponseBean.appendWarning(warnings.toString());
      }

      {
        Set<String> unusedParams = ((PwsHttpServletRequest)request).unusedParams();
        //add warnings about unused params
        if (PersonWsServerUtils.length(unusedParams) > 0) {
          for (String unusedParam : unusedParams) {
            pwsResponseBean.appendWarning("Unused HTTP param: " + unusedParam);
          }
        }
      }     
      
      //set schemas
      //  "schemas":  [
      //     "urn:scim:schemas:core:2.0",
      //     "urn:scim:schemas:extension:cifer:user:1.0",
      //     "urn:scim:schemas:extension:penn:user:1.0"
      //  ],
      PwsNode pwsNode = pwsResponseBean.getPwsNode();
      if (pwsNode == null) {
        pwsNode = new PwsNode(PwsNodeType.object);
        pwsResponseBean.setPwsNode(pwsNode);
      }
      PwsNode metaNode = pwsNode.retrieveField("meta");
      if (metaNode == null) {
        metaNode = new PwsNode(PwsNodeType.object);
        
      } else {
        //move meta to the bottom
        pwsNode.removeField("meta");
      }
      pwsNode.assignField("meta", metaNode);
      
      PwsNode ciferMetaNode = pwsNode.retrieveField("urn:scim:schemas:extension:cifer:2.0:Meta");
      if (ciferMetaNode == null) {
        ciferMetaNode = new PwsNode(PwsNodeType.object);
        
      } else {
        //move meta to the bottom
        pwsNode.removeField("urn:scim:schemas:extension:cifer:2.0:Meta");
      }
      pwsNode.assignField("urn:scim:schemas:extension:cifer:2.0:Meta", ciferMetaNode);
      
      {
      //"schemas":[
      //   "urn:scim:schemas:core:2.0",
      //   "urn:scim:schemas:extension:cifer:2.0:User",
      //   "urn:scim:schemas:extension:cifer:2.0:Meta"
      // ]
        
        PwsNode schemasNode = new PwsNode(PwsNodeType.string);
        pwsNode.assignField("schemas", schemasNode);
        schemasNode.setArrayType(true);
        schemasNode.addArrayItem(new PwsNode("urn:scim:schemas:core:2.0"));
        schemasNode.addArrayItem(new PwsNode("urn:scim:schemas:extension:cifer:2.0:User"));
        schemasNode.addArrayItem(new PwsNode("urn:scim:schemas:extension:cifer:2.0:Meta"));
      }

      if (!StringUtils.isBlank(pwsResponseBean.getResultCode()) && ciferMetaNode.retrieveField("xCiferStatusCode") == null) {
        ciferMetaNode.assignField("statusCode", new PwsNode(pwsResponseBean.getResultCode()));
      }
      if (ciferMetaNode.retrieveField("success") == null) {
        boolean success = pwsResponseBean.getSuccess() != null && pwsResponseBean.getSuccess();
        ciferMetaNode.assignField("success", new PwsNode(success));
      }
      if (ciferMetaNode.retrieveField("responseTimestamp") == null) {
        long now = System.currentTimeMillis();
        String responseTimestamp = PersonWsServerUtils.dateToIso(now);
        ciferMetaNode.assignField("responseTimestamp", new PwsNode(responseTimestamp));
      }
      if (ciferMetaNode.retrieveField("httpStatusCode") == null) {
        ciferMetaNode.assignField("httpStatusCode", new PwsNode((long)pwsResponseBean.getHttpResponseCode()));
      }
      if (ciferMetaNode.retrieveField("serverVersion") == null) {
        ciferMetaNode.assignField("serverVersion", new PwsNode("1.0"));
      }

      if (!StringUtils.isBlank(pwsResponseBean.getErrorMessage())) {
        
        //"Errors":[
        //          {
        //            "description":"Resource 2819c223-7f76-453a-919d-413861904646 not found",
        //            "code":"404"
        //          }
        //        ]
        
        PwsNode errorsNode = pwsNode.removeField("Errors");
        
        if (errorsNode == null) {
          errorsNode = new PwsNode(PwsNodeType.object);
          pwsNode.assignField("Errors", errorsNode);
          errorsNode.setArrayType(true);

          PwsNode errorNode = new PwsNode(PwsNodeType.object);
          errorsNode.addArrayItem(errorNode);

          errorNode.assignField("code", new PwsNode("errorMessage"));
          errorNode.assignField("description", new PwsNode(pwsResponseBean.getErrorMessage()));

        }
            
      }
      
      //structure name
      //twoFactorResponseBeanBase.setStructureName(PersonWsServerUtils.structureName(twoFactorResponseBeanBase.getClass()));

      //headers should be there by now
      //set the status code
      //response.setStatus(twoFactorResponseBeanBase.getResponseMeta().getHttpStatusCode());
      response.setStatus(pwsResponseBean.getHttpResponseCode());

      String restCharset = PersonWebServiceServerConfig.retrieveConfig().propertyValueString("personWsServer.restHttpContentTypeCharset", "UTF-8");
      String responseContentType = wsRestContentType.getContentType();

      if (!PersonWsServerUtils.isBlank(restCharset)) {
        responseContentType += "; charset=" + restCharset;
      }

      response.setContentType(responseContentType);

      //temporarily set to uuid, so we can time the content generation
      long millisUuid = -314253647586987L;
      String millisUuidString = Long.toString(millisUuid);
      
      boolean assignResponseTime = false;
      if (ciferMetaNode.retrieveField("responseTime") == null) {
        //  DurationFormatUtils.formatDurationISO()
        //  "responseTime":"P0.011S",
        assignResponseTime = true;
        ciferMetaNode.assignField("responseTime", new PwsNode(millisUuidString));
      }
      
      responseString = pwsNode == null ? null : pwsNode.toJson();
      
      if (indent) {
        responseString = wsRestContentType.indent(responseString);
        if (logRequestsResponses) {
          responseStringForLog = responseString;
        }
      } else {
        if (logRequestsResponses) {
          responseStringForLog = wsRestContentType.indent(responseString);
        }
      }

      responseTimeMillis = (System.nanoTime()-serviceStarted) / 1000000;

      if (assignResponseTime) {
        
        //String responseTimeFormatted = DurationFormatUtils.formatDurationISO(responseTimeMillis);
        
        String responseTimeFormatted = ISOPeriodFormat.standard().print(new Period(responseTimeMillis));
        
        responseString = PersonWsServerUtils.replace(responseString, millisUuidString, responseTimeFormatted);
      }
      
      try {
        response.getWriter().write(responseString);
      } catch (IOException ioe) {
        throw new RuntimeException(ioe);
      }
      
    } catch (RuntimeException re) {
      //problem!
      LOG.error("Problem with request: " + requestDebugInfo(request), re);
    } finally {

      PersonWsServerUtils.closeQuietly(response.getWriter());
      PwsWsVersion.removeCurrentClientVersion();
      PwsRestContentType.clearContentType();

    }
    
    HttpSession httpSession = request.getSession(false);
    if (httpSession != null) {
      httpSession.invalidate();
    }
    
    //lets write the response to file
    if (logRequestsResponses) {
      //make a file:
      StringBuilder fileContents = new StringBuilder();
      Date currentDate = new Date();
      fileContents.append("Timestamp: ").append(currentDate).append("\n");
      fileContents.append("Millis: ").append(Long.toString(responseTimeMillis)).append("\n");
      fileContents.append("URL: ").append(urlBuilder).append("\n");
      fileContents.append("Request body: ").append(body).append("\n\n");
      fileContents.append("Response body: ").append(responseStringForLog).append("\n\n");
      String tempDirLocation = PersonWsServerUtils.tempFileDirLocation();
      
      File logdir = new File(tempDirLocation + "wsLogs");
      PersonWsServerUtils.mkdirs(logdir);
      
      long myRequestIndex = 0;
      synchronized(PersonWsRestServlet.class) {
        myRequestIndex = ++requestIndex;
      }
      
      String usernameForFilePath = "";
      
      //maybe we want userids in the file name
      if (PersonWebServiceServerConfig.retrieveConfig().propertyValueBoolean("personWsServer.ws.log.requestsResponsesLogSubjectId", false)) {
        
        String username = request.getParameter("username");
        
        usernameForFilePath = "_" + PersonWsServerUtils.validFileName(username);
        
      }
      
      String logfileName = tempDirLocation + "wsLogs" + File.separator + PersonWsServerUtils.timestampToFileString(currentDate) + "_" + myRequestIndex + usernameForFilePath + ".txt";
      File file = new File(logfileName);
      file.createNewFile();
      PersonWsServerUtils.saveStringIntoFile(file, fileContents.toString());
      
    }
    
  }

  /** unique id for requests */
  private static long requestIndex = 0;
  
  /**
   * for error messages, get a detailed report of the request
   * @param request
   * @return the string of descriptive result
   */
  public static String requestDebugInfo(HttpServletRequest request) {
    StringBuilder result = new StringBuilder();
    result.append(" uri: ").append(request.getRequestURI());
    result.append(", HTTP method: ").append(((PwsHttpServletRequest)request).getOriginalMethod());
    if (!PersonWsServerUtils.isBlank(request.getParameter("method"))) {
      result.append(", HTTP param method: ").append(request.getParameter("method"));
    }
    result.append(", decoded url strings: ");
    List<String> urlStrings = extractUrlStrings(request);
    int urlStringsLength = PersonWsServerUtils.length(urlStrings);
    if (urlStringsLength == 0) {
      result.append("[none]");
    } else {
      for (int i = 0; i < urlStringsLength; i++) {
        result.append(i).append(": '").append(urlStrings.get(i)).append("'");
        if (i != urlStringsLength - 1) {
          result.append(", ");
        }
      }
    }
    return result.toString();
  }

  /**
   * take a request and get the list of url strings for the rest web service
   * @see #extractUrlStrings(String)
   * @param request is the request to get the url strings out of
   * @return the list of url strings
   */
  private static List<String> extractUrlStrings(HttpServletRequest request) {
    String requestResourceFull = request.getRequestURI();
    return extractUrlStrings(requestResourceFull);
  }

  /**
   * <pre>
   * take a request uri and break up the url strings not including the app name or servlet
   * this does not include the url params (if applicable)
   * if the input is: grouper-ws/servicesRest/xhtml/v1_3_000/groups/members
   * then the result is a list of size 2: {"group", "members"}
   * 
   * </pre>
   * @param requestResourceFull
   * @return the url strings
   */
  private static List<String> extractUrlStrings(String requestResourceFull) {
    String[] requestResources = PersonWsServerUtils.split(requestResourceFull, '/');
    List<String> urlStrings = new ArrayList<String>();

    //loop through and decode
    int index = 0;
    for (String requestResource : requestResources) {
      //skip the app name and lite servlet
      if (index++ < 2) {
        continue;
      }
      //unescape the url encoding
      urlStrings.add(PersonWsServerUtils.escapeUrlDecode(requestResource));
    }
    return urlStrings;
  }

}
