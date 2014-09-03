package org.personWebService.server.ws.rest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.personWebService.server.beans.PwsNode;
import org.personWebService.server.beans.PwsNode.PwsNodeType;
import org.personWebService.server.config.PersonWebServiceServerConfig;
import org.personWebService.server.hibernate.HibernateSession;
import org.personWebService.server.hibernate.PersonWsDaoFactory;
import org.personWebService.server.hibernate.PwsHibUtils;
import org.personWebService.server.j2ee.PersonWsRestServlet;
import org.personWebService.server.util.PersonWsServerUtils;
import org.personWebService.server.ws.corebeans.PwsCheckPasswordResponseCode;
import org.personWebService.server.ws.corebeans.PwsResponseBean;
import org.personWebService.server.ws.corebeans.PwsUserByIdRequest;
import org.personWebService.server.ws.corebeans.PwsUsersSearchRequest;


/**
 * logic for web service
 */
public class PwsRestLogic {

  /**
   * check a password
   * @param twoFactorDaoFactory 
   * @param id 
   * @param params
   * @return the bean
   */
  public static PwsResponseBean userById(PersonWsDaoFactory twoFactorDaoFactory, String id, Map<String, String> params) {

    PersonWsRestServlet.assertLoggedOnPrincipalTfServer();
    
    PwsUserByIdRequest pwsUserByIdRequest = new PwsUserByIdRequest();

    return userByIdLogic(twoFactorDaoFactory, id, pwsUserByIdRequest);
  }

  /**
   * @param twoFactorDaoFactory data access
   * @param id
   * @param pwsUserByIdRequest 
   * @return the response
   */
  public static PwsResponseBean userByIdLogic(
      PersonWsDaoFactory twoFactorDaoFactory, String id, PwsUserByIdRequest pwsUserByIdRequest) {

    Map<String, Object> trafficLogMap = new LinkedHashMap<String, Object>();
    
    long start = System.nanoTime();
    
    PwsResponseBean pwsResponseBean = new PwsResponseBean();
    
    try {
      
      trafficLogMap.put("id", id);

      //# if id's should be validated by regex, leave blank if shouldnt be validated
      //pws.getPersonById_idRegex = [a-zA-Z0-9-_@\.]+
      String idRegex = PersonWebServiceServerConfig.retrieveConfig().propertyValueString("pws.getPersonById_idRegex");
      if (!StringUtils.isBlank(idRegex)) {
        //lets validate the id
        if (!id.matches(idRegex)) {
          throw new RuntimeException("Invalid id: " + id);
        }
      }      
      PwsNode pwsNode = new PwsNode(PwsNodeType.object);
      
      pwsResponseBean.setPwsNode(pwsNode);

      //do a query to get the data
      List<String[]> results = HibernateSession.bySqlStatic().listSelect(String[].class, 
          "select penn_id, kerberos_principal, admin_view_pref_first_name, admin_view_pref_middle_name, "
          + "admin_view_pref_last_name, admin_view_pref_name, admin_view_pref_email_address, birth_date, gender, last_updated  "
          + "from computed_person where penn_id = ? and active_code = 'A' "
          + "and (is_active_faculty = 'Y' or is_active_staff = 'Y' or is_active_student = 'Y' or directory_prim_cent_affil_id is not null)", PwsHibUtils.listObject(id));

      //  "meta": {
      //    "resourceType": "User",
      //    "created": "2010-01-23T04:56:22Z",
      //    "lastModified": "2011-05-13T04:42:34Z",
      //    "location": "https://example.com/personService/v2/Users/2819c223-7f76-453a-919d-413861904646"
      //  },
      PwsNode metaNode = new PwsNode(PwsNodeType.object);
      {
        pwsNode.assignField("meta", metaNode);
        metaNode.assignField("resourceType", new PwsNode("User"));
        metaNode.assignField("location", new PwsNode(
            PersonWebServiceServerConfig.retrieveConfig().propertyValueStringRequired("personWsServer.appUrlBase") + "personWs/v1/Users/" + id));
      }      
      
      if (PersonWsServerUtils.length(results) > 1) {
        throw new RuntimeException("Why is there more than one record returned by id???? " + id);
      }

      if (PersonWsServerUtils.length(results) == 0) {
        
        //"Errors":[
        //          {
        //            "description":"Resource 2819c223-7f76-453a-919d-413861904646 not found",
        //            "code":"404"
        //          }
        //        ]
        
        PwsNode errorsNode = new PwsNode(PwsNodeType.object);
        pwsNode.assignField("Errors", errorsNode);
        errorsNode.setArrayType(true);
        
        PwsNode errorNode = new PwsNode(PwsNodeType.object);
        errorsNode.addArrayItem(errorNode);

        errorNode.assignField("code", new PwsNode("404"));
        String errorMessage = "Resource " + id + " not found";
        errorNode.assignField("description", new PwsNode(errorMessage));

        pwsResponseBean.setErrorMessage(errorMessage);
        pwsResponseBean.setResultCode("successNotFound");
        
        pwsResponseBean.setHttpResponseCode(404);
        pwsResponseBean.setSuccess(true);
        
        return pwsResponseBean;
      }
      
      String[] resultRow = results.get(0);
      
      String lastUpdated = convertSqlRowToUser(pwsNode, resultRow);
      
      if (!StringUtils.isBlank(lastUpdated)) {
        //2013-10-25 14:57:42.13
        try {
          SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss.SS");
          Date dateStr = formatter.parse(lastUpdated);
          
          String lastModifiedIso = PersonWsServerUtils.dateToIso(dateStr.getTime());
          
          metaNode.assignField("lastModified", new PwsNode(lastModifiedIso));
        } catch (ParseException pe) {
          LOG.error("Cant parse date: " + lastUpdated);
        }
      }
      
      pwsResponseBean.setResultCode("successFound");
      pwsResponseBean.setSuccess(true);
      
      return pwsResponseBean;
    } catch (RuntimeException re) {
      
      pwsResponseBean.setResultCode(PwsCheckPasswordResponseCode.ERROR.name());
      pwsResponseBean.setSuccess(false);
      pwsResponseBean.setErrorMessage(ExceptionUtils.getFullStackTrace(re));
      
      trafficLogMap.put("success", false);
      trafficLogMap.put("resultCode", PwsCheckPasswordResponseCode.ERROR.name());

      LOG.error("error", re);
      
      return pwsResponseBean;

    } finally {
      trafficLogMap.put("logicTime", ((System.nanoTime() - start)/1000000) + "ms");
      PwsRestLogicTrafficLog.wsRestTrafficLog(trafficLogMap);
    }
  }

  /**
   * @param pwsNode
   * @param resultRow
   * @return lastUpdated
   */
  private static String convertSqlRowToUser(PwsNode pwsNode, String[] resultRow) {
    //we have one result
    int col = 0;
    String pennId = resultRow[col++];
    String netId = resultRow[col++];
    String firstName = resultRow[col++];
    String middleName = resultRow[col++];
    String lastName = resultRow[col++];
    String name = resultRow[col++];
    String emailAddress = resultRow[col++];
    String birthDate = resultRow[col++];
    String gender = resultRow[col++];
    String lastUpdated = resultRow[col++];
    
    pwsNode.assignField("id", new PwsNode(pennId));
    pwsNode.assignField("userName", new PwsNode(netId));
    
    if (!PersonWsServerUtils.isBlank(firstName)
        || !PersonWsServerUtils.isBlank(middleName)
        || !PersonWsServerUtils.isBlank(lastName)
        || !PersonWsServerUtils.isBlank(name)) {
      PwsNode nameNode = new PwsNode(PwsNodeType.object);
      pwsNode.assignField("name", nameNode);
      nameNode.assignField("givenName", new PwsNode(firstName));
      nameNode.assignField("middleName", new PwsNode(middleName));
      nameNode.assignField("familyName", new PwsNode(lastName));
      nameNode.assignField("formatted", new PwsNode(name));
    }
    pwsNode.assignField("active", new PwsNode(true));
    
    if (!PersonWsServerUtils.isBlank(emailAddress)) {
      PwsNode emailsNode = new PwsNode(PwsNodeType.object);
      pwsNode.assignField("emails", emailsNode);
      emailsNode.setArrayType(true);
      
      PwsNode emailNode = new PwsNode(PwsNodeType.object);
      emailsNode.addArrayItem(emailNode);

      emailNode.assignField("value", new PwsNode(emailAddress));
      emailNode.assignField("primary", new PwsNode(true));

    }

    
    PwsNode ciferUserNode = new PwsNode(PwsNodeType.object);
    pwsNode.assignField("urn:scim:schemas:extension:cifer:2.0:User", ciferUserNode);
    
    if (!PersonWsServerUtils.isBlank(birthDate)) {
      if (birthDate.contains(" ")) {
        birthDate = PersonWsServerUtils.prefixOrSuffix(birthDate, " ", true);
      }
      ciferUserNode.assignField("dateOfBirth", new PwsNode(birthDate));
    }
    
    if (!PersonWsServerUtils.isBlank(gender)) {
      
      if (PersonWsServerUtils.equals("M", gender)) {
        ciferUserNode.assignField("gender", new PwsNode("male"));
      }
      if (PersonWsServerUtils.equals("F", gender)) {
        ciferUserNode.assignField("gender", new PwsNode("female"));
      }
      
    }
    
    //  {
    //    "id":"12345678",
    //    "userName":"jsmith",
    //    "name":{
    //       "givenName":"Jeff",
    //       "middleName":"John",
    //       "familyName":"Smith",
    //       "formatted": "Jeff Smith"
    //     },
    //     "userType":"student",
    //     "active":true,
    //     "emails":[
    //       {
    //         "value":"ekohler@example.com",
    //         "primary":true
    //       }
    //     ],
    //     "meta": {
    //       "resourceType": "User",
    //       "created": "2010-01-23T04:56:22Z",
    //       "lastModified": "2011-05-13T04:42:34Z",
    //       "location": "https://example.com/personService/v2/Users/2819c223-7f76-453a-919d-413861904646"
    //     },
    //     "schemas":  [
    //        "urn:scim:schemas:core:2.0",
    //        "urn:scim:schemas:extension:cifer:user:1.0",
    //        "urn:scim:schemas:extension:penn:user:1.0"
    //     ],
    //     "xCiferIdentifiers":[
    //        {
    //          "value":"121234",
    //          "type":"enterprise"
    //        },
    //        {
    //          "value":"xyz123",
    //          "type":"network"
    //        },
    //        {
    //          "value":"xyz123@example.com",
    //          "type":"eppn"
    //        }
    //     ],
    //     "xCiferDescription":"John Smith (jsmith, xyz123) Student in French, Active (also: employee)",
    //     "xCiferSearchDescription":"jeff john smith jsmith xyz123 student in french active employee xyz123@example.com",
    //     "xCiferDateOfBirth":"1990-01-01",
    //     "xCiferGender":"male",
    //     "xCiferNames":[
    //       {
    //         "primary":true,
    //         "givenName":"Jeff",
    //         "middleName":"John",
    //         "familyName":"Smith",
    //         "formatted": "Jeff Smith"
    //       }
    //     ],
    //     "xCiferResourceMetadata": {
    //        "statusCode":"successFound",
    //        "success":true
    //     },
    //     "xCiferResponseMetadata": {
    //        "responseTimestamp:"2012-10-04T03:10:14.123Z",
    //        "responseTime":"P0.011S",
    //        "httpStatusCode":200,
    //        "serverVersion":"1.0"
    //     },
    //     "xPennPrimarySrsDivision": "SEAS"
    //}      
    return lastUpdated;
  }

  /** logger */
  private static final Log LOG = PersonWsServerUtils.getLog(PwsRestLogic.class);

  /**
   * check a password
   * @param twoFactorDaoFactory 
   * @param params
   * @return the bean
   */
  public static PwsResponseBean usersSearch(PersonWsDaoFactory twoFactorDaoFactory, Map<String, String> params) {
  
    PersonWsRestServlet.assertLoggedOnPrincipalTfServer();
    
    PwsUsersSearchRequest pwsUsersSearchRequest = new PwsUsersSearchRequest();

    {
      boolean splitTrim = PersonWsServerUtils.booleanValue(params.get("xCiferSplitTrim"), false);
  
      pwsUsersSearchRequest.setSplitTrim(splitTrim);
    }

    {
      String filter = params.get("filter");
      
      pwsUsersSearchRequest.setFilter(filter);
    }

    {
      Integer startIndex = PersonWsServerUtils.intObjectValue(params.get("startIndex"), true);
      if (startIndex != null) {
        pwsUsersSearchRequest.setStartIndex(startIndex);
      }
    }

    {
      Integer count = PersonWsServerUtils.intObjectValue(params.get("count"), true);
      if (count != null) {
        pwsUsersSearchRequest.setCount(count);
      }
    }

    return usersSearchLogic(twoFactorDaoFactory, pwsUsersSearchRequest);
  }

  /**
   * @param twoFactorDaoFactory data access
   * @param pwsUsersSearchRequest
   * @return the response
   */
  public static PwsResponseBean usersSearchLogic(
      PersonWsDaoFactory twoFactorDaoFactory, PwsUsersSearchRequest pwsUsersSearchRequest) {
  
    Map<String, Object> trafficLogMap = new LinkedHashMap<String, Object>();
    
    long start = System.nanoTime();
    
    PwsResponseBean pwsResponseBean = new PwsResponseBean();
    
    try {
      
      PwsNode pwsNode = new PwsNode(PwsNodeType.object);
      
      pwsResponseBean.setPwsNode(pwsNode);
  
      List<Object> params = new ArrayList<Object>(); 

      String selectClause = "select penn_id, kerberos_principal, admin_view_pref_first_name, "
                + " admin_view_pref_middle_name, "
                + " admin_view_pref_last_name, admin_view_pref_name, admin_view_pref_email_address, birth_date, gender, last_updated ";
      StringBuilder sqlWithoutSelect = new StringBuilder(" from computed_person where "); 
      
      //see if we are filtering
      if (!PersonWsServerUtils.isBlank(pwsUsersSearchRequest.getFilter())) {
        
        Pattern filterPattern = Pattern.compile("^urn:scim:schemas:extension:cifer:2.0:User:searchDescription co \"(.*)\"$");
        Matcher matcher = filterPattern.matcher(pwsUsersSearchRequest.getFilter());
        
        if (matcher.matches()) {
         
          String scope = matcher.group(1);
          
          //       (search_description like '%chris%' and search_description like '%ch%' ) and
          //see if there is a scope
          scope = scope.toLowerCase();
          
          String[] scopes = pwsUsersSearchRequest.isSplitTrim() ? PersonWsServerUtils.splitTrim(scope, " ") : new String[]{scope};
          
          sqlWithoutSelect.append(" ( ");
      
          int index = 0;
          for (String theScope : scopes) {
            if (index != 0) {
              sqlWithoutSelect.append(" and ");
            }
  
            sqlWithoutSelect.append(" search_description like ? ");
            
            params.add("%" + theScope + "%");
              
            index++;
          }
          sqlWithoutSelect.append(" ) and ");
        
        } else {
          
          filterPattern = Pattern.compile("^userName eq \"(.*)\"$");
          matcher = filterPattern.matcher(pwsUsersSearchRequest.getFilter());
          
          if (matcher.matches()) {
           
            String pennKey = matcher.group(1);
            
            //       kerberos_principal = 'mchyzer' and
            
    
            sqlWithoutSelect.append(" kerberos_principal = ? and ");

            params.add(pennKey);

          } else {
            throw new RuntimeException("Filter invalid or not implemented yet.  Must be: urn:scim:schemas:extension:cifer:2.0:User:searchDescription co \"some string\"  -   or  -   userName eq \"jsmith\"");
          }
        }
      }
      
      String orderByClause = " order by admin_view_pref_name";
      
      sqlWithoutSelect.append(" active_code = 'A' "
          + " and (is_active_faculty = 'Y' or is_active_staff = 'Y' or is_active_student = 'Y' "
          + " or directory_prim_cent_affil_id is not null)");
      
      int startIndex = pwsUsersSearchRequest.getStartIndex();

      Integer pageSize = pwsUsersSearchRequest.getCount();

      int maxResults = PersonWebServiceServerConfig.retrieveConfig().propertyValueInt("pws.users.search.maxResults", 1000);

      if (pageSize == null || pageSize > maxResults) {
        pageSize = maxResults;
      }
      
      {
        //get the count
        int totalResults = HibernateSession.bySqlStatic().select(int.class, "select count(1) " + sqlWithoutSelect.toString(), params);
  
        pwsNode.assignField("totalResults", new PwsNode(totalResults));
      }      

      //do a query to get the data if we are getting any data
      List<String[]> results = null;
      
      if (pageSize > 0) {
        params.add(startIndex);
        params.add(startIndex + pageSize - 1);

        //  "totalResults":100,
        //  "itemsPerPage":10,
        //  "startIndex":1,
        
        pwsNode.assignField("startIndex", new PwsNode(startIndex));
        pwsNode.assignField("itemsPerPage", new PwsNode(pageSize));
        
        results = HibernateSession.bySqlStatic().listSelect(String[].class, 
            "select * from (select the_row.*, rownum the_row_num from (" + selectClause + sqlWithoutSelect.toString() 
            + orderByClause + " ) the_row) where the_row_num between ? and ?", params);
      }
      pwsNode.assignField("totalResults", new PwsNode(PersonWsServerUtils.length(results)));

      //  "meta": {
      //    "resourceType": "User",
      //    "created": "2010-01-23T04:56:22Z",
      //    "lastModified": "2011-05-13T04:42:34Z",
      //    "location": "https://example.com/personService/v2/Users/2819c223-7f76-453a-919d-413861904646"
      //  },
      PwsNode metaNode = new PwsNode(PwsNodeType.object);
      {
        pwsNode.assignField("meta", metaNode);
        metaNode.assignField("resourceType", new PwsNode("UserList"));
        metaNode.assignField("location", new PwsNode(
            PersonWebServiceServerConfig.retrieveConfig().propertyValueStringRequired("personWsServer.appUrlBase") + "personWs/v1/Users"));

      }

      //  {
      //    "schemas":["urn:scim:schemas:core:1.0"],
      //    "totalResults":2,
      //    "Resources":[
      //      {
      //        "id":"c3a26dd3-27a0-4dec-a2ac-ce211e105f97",
      //        "title":"Assistant VP",
      //        "userName":"bjensen"
      //      },
      //      {
      //        "id":"a4a25dd3-17a0-4dac-a2ac-ce211e125f57",
      //        "title":"VP",
      //        "userName":"jsmith"
      //      }
      //    ]
      //  }

      if (PersonWsServerUtils.length(results) > 0) {
        
        PwsNode resourcesNode = new PwsNode(PwsNodeType.object);
        pwsNode.assignField("Resources", resourcesNode);
        resourcesNode.setArrayType(true);
        for (String[] resultRow : PersonWsServerUtils.nonNull(results)) {
          PwsNode rowPwsNode = new PwsNode(PwsNodeType.object);
          convertSqlRowToUser(rowPwsNode, resultRow);
          resourcesNode.addArrayItem(rowPwsNode);
        }
        
      }
      
      pwsResponseBean.setResultCode("success");
      pwsResponseBean.setSuccess(true);
      
      return pwsResponseBean;
    } catch (RuntimeException re) {
      
      pwsResponseBean.setResultCode(PwsCheckPasswordResponseCode.ERROR.name());
      pwsResponseBean.setSuccess(false);
      pwsResponseBean.setErrorMessage(ExceptionUtils.getFullStackTrace(re));
      
      trafficLogMap.put("success", false);
      trafficLogMap.put("resultCode", PwsCheckPasswordResponseCode.ERROR.name());
  
      LOG.error("error", re);
      
      return pwsResponseBean;
  
    } finally {
      trafficLogMap.put("logicTime", ((System.nanoTime() - start)/1000000) + "ms");
      PwsRestLogicTrafficLog.wsRestTrafficLog(trafficLogMap);
    }
  }

}
