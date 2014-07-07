package org.personWebService.server.ws.rest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.personWebService.server.beans.PwsNode;
import org.personWebService.server.beans.PwsNode.PwsNodeType;
import org.personWebService.server.hibernate.HibernateSession;
import org.personWebService.server.hibernate.PersonWsDaoFactory;
import org.personWebService.server.hibernate.PwsHibUtils;
import org.personWebService.server.j2ee.PersonWsRestServlet;
import org.personWebService.server.util.PersonWsServerUtils;
import org.personWebService.server.ws.corebeans.PwsCheckPasswordResponseCode;
import org.personWebService.server.ws.corebeans.PwsResponseBean;
import org.personWebService.server.ws.corebeans.PwsUserByIdRequest;


/**
 * logic for web service
 */
public class PwsRestLogic {

  /**
   * populate the check password request from request params
   * @param tfCheckPasswordRequest
   * @param params
   */
  private static void populateUserByIdRequest(PwsUserByIdRequest tfCheckPasswordRequest, Map<String, String> params) {
    {
    }

  }
  
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

    populateUserByIdRequest(pwsUserByIdRequest, params);
    
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
        metaNode.assignField("location", new PwsNode("User"));
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
        pwsResponseBean.setResultCode("SUCCESS_USER_NOT_FOUND");
        
        pwsResponseBean.setHttpResponseCode(404);
        
        return pwsResponseBean;
      }
      
      String[] resultRow = results.get(0);
      
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

      if (!PersonWsServerUtils.isBlank(birthDate)) {
        if (birthDate.contains(" ")) {
          birthDate = PersonWsServerUtils.prefixOrSuffix(birthDate, " ", true);
        }
        pwsNode.assignField("xCiferDateOfBirth", new PwsNode(birthDate));
      }
      
      if (!PersonWsServerUtils.isBlank(gender)) {
        
        if (PersonWsServerUtils.equals("M", gender)) {
          pwsNode.assignField("xCiferGender", new PwsNode("male"));
        }
        if (PersonWsServerUtils.equals("F", gender)) {
          pwsNode.assignField("xCiferGender", new PwsNode("female"));
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
      
      return pwsResponseBean;
    } catch (RuntimeException re) {
      
      pwsResponseBean.setResultCode(PwsCheckPasswordResponseCode.ERROR.name());
      pwsResponseBean.setSuccess(false);
      pwsResponseBean.setErrorMessage(ExceptionUtils.getFullStackTrace(re));
      
      trafficLogMap.put("userAllowed", "null");
      trafficLogMap.put("success", false);
      trafficLogMap.put("resultCode", PwsCheckPasswordResponseCode.ERROR.name());

      LOG.error("error", re);
      
      return pwsResponseBean;

    } finally {
      trafficLogMap.put("logicTime", ((System.nanoTime() - start)/1000000) + "ms");
      PwsRestLogicTrafficLog.wsRestTrafficLog(trafficLogMap);
    }
  }

  /** logger */
  private static final Log LOG = PersonWsServerUtils.getLog(PwsRestLogic.class);

}
