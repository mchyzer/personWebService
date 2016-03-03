package org.personWebService.server.ws.rest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

      String query = null;
      
      query = "select penn_id, kerberos_principal, admin_view_pref_first_name, admin_view_pref_middle_name, "
          + "admin_view_pref_last_name, admin_view_pref_name, admin_view_pref_email_address, birth_date, "
          + "gender, last_updated, directory_prim_cent_affil_code, school_or_center, org_or_div  "
          + "from PCD_WS_FORMATX_V where penn_id = ?";
      
      if (format1()) {
        query = PersonWsServerUtils.replace(query, "PCD_WS_FORMATX_V", "PCD_WS_FORMAT1_V");
      } else if (format2()) {
        query = PersonWsServerUtils.replace(query, "PCD_WS_FORMATX_V", "PCD_WS_FORMAT2_V");
      } else {
        throw new RuntimeException("Cant match user to format: " + PersonWsRestServlet.retrievePrincipalLoggedIn());
      }
      
      //do a query to get the data
      List<String[]> results = HibernateSession.bySqlStatic().listSelect(String[].class, 
          query, PwsHibUtils.listObject(id));

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

      if (format2()) {

        //do we do address?
        //select CHAR_PENN_ID, ADDRESS_SEQ, STREET1, STREET2, 
        //CITY, STATE, POSTAL_CODE, COUNTRY, SOURCE_ADDRESS, VIEW_TYPE, ADDRESS_TYPE
        //from PCD_WS_ADDRESS_V;
        List<String[]> addressResults = HibernateSession.bySqlStatic().listSelect(String[].class, 
            "select char_penn_id, address_seq, street1, street2, "
            + "city, state, postal_code, country, source_address, view_type, address_type "
            + "from pcd_ws_address_v where char_penn_id = ?", PwsHibUtils.listObject(id));

        //just take the first row
        if (PersonWsServerUtils.length(addressResults) > 0) {
          String[] addressResultRow = addressResults.get(0);

          convertSqlAddressRowToUser(pwsNode, addressResultRow);

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
   * 
   * @return if format2
   */
  public static boolean format2() {
    String userLoggedIn = PersonWsRestServlet.retrievePrincipalLoggedIn();
    String format2forLogins = PersonWebServiceServerConfig.retrieveConfig().propertyValueString("personWsServer.format2forLogins");
    
    Set<String> format2forLoginsSet = PersonWsServerUtils.nonNull(PersonWsServerUtils.splitTrimToSet(format2forLogins, ","));
    
    return format2forLoginsSet.contains(userLoggedIn);
  }

  /**
   * 
   * @return if format1
   */
  public static boolean format1() {
    String userLoggedIn = PersonWsRestServlet.retrievePrincipalLoggedIn();
    String format1forLogins = PersonWebServiceServerConfig.retrieveConfig().propertyValueString("personWsServer.format1forLogins");
    
    Set<String> format1forLoginsSet = PersonWsServerUtils.nonNull(PersonWsServerUtils.splitTrimToSet(format1forLogins, ","));
    
    return format1forLoginsSet.contains(userLoggedIn);
  }

    
    
  /**
   * @param pwsNode
   * @param resultRow
   */
  private static void convertSqlAddressRowToUser(PwsNode pwsNode, String[] resultRow) {

    //  char_penn_id, address_seq, street1, street2, "
    //      + "city, state, postal_code, country, source_address, view_type, address_type "
    //      + "from pcd_ws_address_v where char_penn_id = ?", PwsHibUtils.listObject(id));

    //we have one result
    int col = 0;
    String pennId = resultRow[col++];
    @SuppressWarnings("unused")
    String addressSeq = resultRow[col++];
    String street1 = resultRow[col++];
    String street2 = resultRow[col++];
    String city = resultRow[col++];
    String state = resultRow[col++];
    String postalCode = resultRow[col++];
    String country = resultRow[col++];
    @SuppressWarnings("unused")
    String sourceAddress = resultRow[col++];
    @SuppressWarnings("unused")
    String viewType = resultRow[col++];
    @SuppressWarnings("unused")
    String addressType = resultRow[col++];

    if (!StringUtils.equals(pwsNode.getFields().get("id").getString(), pennId)) {
      throw new RuntimeException("Why do pennids not match? " + pennId 
          + ", " + pwsNode.getFields().get("id").getString());
    }
    
//    "addresses": [
//                  {
//                    "type": "work",
//                    "streetAddress": "100 Universal City Plaza",
//                    "locality": "Hollywood",
//                    "region": "CA",
//                    "postalCode": "91608",
//                    "country": "USA",
//                    "formatted": "100 Universal City Plaza\nHollywood, CA 91608 USA",

    PwsNode addressNode = new PwsNode(PwsNodeType.object);
    PwsNode addressArrayNode = new PwsNode(PwsNodeType.object);
    addressArrayNode.setArrayType(true);
    addressArrayNode.addArrayItem(addressNode);
    
    pwsNode.assignField("addresses", addressArrayNode);

    addressNode.assignField("type", new PwsNode("other"));
    String streetAddress = street1;
    if (!StringUtils.isBlank(street2)) {
      streetAddress += "\n" + street2;
    }
    addressNode.assignField("streetAddress", new PwsNode(streetAddress));
    addressNode.assignField("locality", new PwsNode(city));
    addressNode.assignField("region", new PwsNode(state));
    addressNode.assignField("postalCode", new PwsNode(postalCode));
    addressNode.assignField("country", new PwsNode(country));

    StringBuilder formatted = new StringBuilder(street1);
    PersonWsServerUtils.appendIfNotBlank(formatted, "\n", street2);
    formatted.append("\n").append(city).append(", ").append(state).append(" ").append(postalCode);
    PersonWsServerUtils.appendIfNotBlank(formatted, " ", country);
    
    addressNode.assignField("formatted", new PwsNode(formatted.toString()));
    
    
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
    String affiliation = resultRow[col++];
    String center = resultRow[col++];
    String org = resultRow[col++];
    
    boolean format1 = format1();
    boolean format2 = format2();
    
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
    
    if (!format2) {
      pwsNode.assignField("active", new PwsNode(true));
    }
    
    if (!PersonWsServerUtils.isBlank(emailAddress)) {
      PwsNode emailsNode = new PwsNode(PwsNodeType.object);
      pwsNode.assignField("emails", emailsNode);
      emailsNode.setArrayType(true);
      
      PwsNode emailNode = new PwsNode(PwsNodeType.object);
      emailsNode.addArrayItem(emailNode);

      emailNode.assignField("value", new PwsNode(emailAddress));
      emailNode.assignField("primary", new PwsNode(true));

    }

    if ((!format2 && (!StringUtils.isBlank(birthDate) || !StringUtils.isBlank(gender)))
        || format2 && !StringUtils.isBlank(affiliation)) {
      PwsNode ciferUserNode = new PwsNode(PwsNodeType.object);
      pwsNode.assignField("urn:scim:schemas:extension:cifer:2.0:User", ciferUserNode);

      if (format1) {
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
      } else if (format2) {
        if (!PersonWsServerUtils.isBlank(org)) {
          ciferUserNode.assignField("org", new PwsNode(org));
        }
        if (!PersonWsServerUtils.isBlank(center)) {
          ciferUserNode.assignField("center", new PwsNode(center));
        }
      }
      
      
      if (format2) {
        ciferUserNode.assignField("affiliation", new PwsNode(affiliation));
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
                + " admin_view_pref_last_name, admin_view_pref_name, admin_view_pref_email_address, "
                + "birth_date, gender, last_updated, directory_prim_cent_affil_code, school_or_center, org_or_div ";
      String viewName = null;
      
      if (format1()) {
        viewName = "PCD_WS_FORMAT1_V";
      } else if (format2()) {
        viewName = "PCD_WS_FORMAT2_V";
      } else {
        throw new RuntimeException("Cant match user to format: " + PersonWsRestServlet.retrievePrincipalLoggedIn());
      }

      StringBuilder sqlWithoutSelect = new StringBuilder(" from " + viewName + " where "); 
      boolean sqlWithoutSelectNeedsAnd = false;
      
      //see if we are filtering
      String filter = pwsUsersSearchRequest.getFilter();
      if (!PersonWsServerUtils.isBlank(filter)) {
        
        Pattern filterPattern = Pattern.compile("^urn:scim:schemas:extension:cifer:2.0:User:searchDescription co \"(.*)\"$");
        Matcher matcher = filterPattern.matcher(filter);
        
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
          sqlWithoutSelect.append(" ) ");
          sqlWithoutSelectNeedsAnd = true;
        
        } else {
          
          filterPattern = Pattern.compile("^userName eq \"(.*)\"$");
          matcher = filterPattern.matcher(filter);
          
          if (matcher.matches()) {
           
            String pennKey = matcher.group(1);
            
            //       kerberos_principal = 'mchyzer' and
            
    
            if (sqlWithoutSelectNeedsAnd) {
              sqlWithoutSelect.append(" and ");
            }
            sqlWithoutSelect.append(" kerberos_principal = ?");
            sqlWithoutSelectNeedsAnd = true;

            params.add(pennKey);

          } else {
            throw new RuntimeException("Filter invalid or not implemented yet.  Must be: urn:scim:schemas:extension:cifer:2.0:User:searchDescription co \"some string\"  -   or  -   userName eq \"jsmith\"");
          }
        }
      }
      
      String orderByClause = " order by admin_view_pref_name";
      
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
        pwsNode.assignField("itemsPerPage", new PwsNode(pageSize + startIndex - 1));
        
        String sql = "select * from (select the_row.*, rownum the_row_num from (" + selectClause + sqlWithoutSelect.toString() 
          + orderByClause + " ) the_row) where the_row_num between ? and ?";
        
        results = HibernateSession.bySqlStatic().listSelect(String[].class, 
            sql, params);
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
        List<String> pennIds = new ArrayList<String>();
        Map<String, PwsNode> pennIdToNodeMap = new LinkedHashMap<String, PwsNode>();
        for (String[] resultRow : PersonWsServerUtils.nonNull(results)) {
          PwsNode rowPwsNode = new PwsNode(PwsNodeType.object);
          convertSqlRowToUser(rowPwsNode, resultRow);
          resourcesNode.addArrayItem(rowPwsNode);
          pennIds.add(resultRow[0]);
          pennIdToNodeMap.put(resultRow[0], rowPwsNode);
        }

        //now lets lookup addresses
        if (format2()) {
          int batchSize = 200;

          //get these in batches
          int batchNumberOfBatches = PersonWsServerUtils.batchNumberOfBatches(pennIds, batchSize);
          for (int batchIndex = 0; batchIndex < batchNumberOfBatches; batchIndex++) {
            List<String> batch = PersonWsServerUtils.batchList(pennIds, batchSize, batchIndex);

            //do we do address?
            //select CHAR_PENN_ID, ADDRESS_SEQ, STREET1, STREET2, 
            //CITY, STATE, POSTAL_CODE, COUNTRY, SOURCE_ADDRESS, VIEW_TYPE, ADDRESS_TYPE
            //from PCD_WS_ADDRESS_V;
            List<String[]> addressResults = HibernateSession.bySqlStatic().listSelect(String[].class, 
                "select char_penn_id, address_seq, street1, street2, "
                + "city, state, postal_code, country, source_address, view_type, address_type "
                + "from pcd_ws_address_v where char_penn_id in ("
                + PwsHibUtils.convertToInClauseForSqlStatic(batch)
                + ")", (List<Object>)(Object)batch);
            
            for (String[] addressResult : addressResults) {
              String pennId = addressResult[0];
              PwsNode personNode = pennIdToNodeMap.get(pennId);
              //it wouldnt be there if we have processed this pennid already, and they have multiple addresses
              if (personNode != null) {
                convertSqlAddressRowToUser(personNode, addressResult);
              }
            }
          }
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
