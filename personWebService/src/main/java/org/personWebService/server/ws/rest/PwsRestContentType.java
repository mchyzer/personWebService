/*
 * @author mchyzer $Id: PwsRestContentType.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.personWebService.server.ws.rest;

import java.io.File;

import org.apache.commons.logging.Log;
import org.personWebService.server.config.PersonWebServiceServerConfig;
import org.personWebService.server.json.DefaultJsonConverter;
import org.personWebService.server.json.JsonConverter;
import org.personWebService.server.util.JsonIndenter;
import org.personWebService.server.util.PersonWsServerUtils;
import org.personWebService.server.util.XmlIndenter;



/**
 * possible content types by grouper ws rest
 */
public enum PwsRestContentType {

  /** 
   * json content type, uses the pluggable json converter
   * http request content type should be set to text/x-json
   */
  json("text/x-json") {


    /**
     * parse a string to an object
     * @param input
     * @param warnings is where warnings should be written to
     * @return the object
     */
    @Override
    public Object parseString(Class<?> theClass, String input, StringBuilder warnings) {

      JsonConverter jsonConverter = jsonConverter();
      
      try {
        return jsonConverter.convertFromJson(theClass, input, warnings);
      } catch (RuntimeException re) {
        LOG.error("Error unparsing string with converter: " + PersonWsServerUtils.className(jsonConverter) + ", " + input);
        throw new RuntimeException("Problem unparsing string with converter: " + PersonWsServerUtils.className(jsonConverter)
            + ", " + PersonWsServerUtils.indent(input, false), re);
      }
    }

    /**
     * write a string representation to result string
     * @param object to write to output
     * @return the string representation
     */
    @Override
    public String writeString(Object object) {
      JsonConverter jsonConverter = jsonConverter();
      
      try {
        String jsonString = jsonConverter.convertToJson(object);
        return jsonString;
      } catch (RuntimeException re) {
        LOG.error("Error converting json object with converter: " 
            + PersonWsServerUtils.className(jsonConverter) + ", " + PersonWsServerUtils.className(object));
        throw new RuntimeException("Error converting json object with converter: " + PersonWsServerUtils.className(jsonConverter)
            + ", " + PersonWsServerUtils.className(object), re);
      }
    }

    @Override
    public String indent(String string) {
      return new JsonIndenter(string).result();
    }
  };

  /** xml header */
  public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
  
  /**
   * indent the content
   * @param string 
   * @return the indented content
   */
  public abstract String indent(String string);
  
  /**
   * instantiate the json convert configured in the grouper-ws.properties file
   * @return the json converter
   */
  @SuppressWarnings("unchecked")
  public static JsonConverter jsonConverter() {
    String jsonConverterClassName = PersonWebServiceServerConfig.retrieveConfig().propertyValueString(
        "jsonConverter", DefaultJsonConverter.class.getName());
    Class<? extends JsonConverter> jsonConverterClass = PersonWsServerUtils.forName(jsonConverterClassName);
    JsonConverter jsonConverter = PersonWsServerUtils.newInstance(jsonConverterClass);
    return jsonConverter;
  }
  
  /**
   * test out a parse
   * @param args
   */
  public static void main(String[] args) {
    String jsonString = PersonWsServerUtils.readFileIntoString(new File("c:/temp/problem.json"));
    PwsRestContentType.json.parseString(Object.class, jsonString, new StringBuilder());
  }
  
  /**
   * parse a string to an object
   * @param theClass 
   * @param input
   * @param warnings is where warnings should be written to
   * @return the object
   */
  public abstract Object parseString(Class<?> theClass, String input, StringBuilder warnings);

  /**
   * write a string representation to result string
   * @param object to write to output
   * @return the string representation
   */
  public abstract String writeString(Object object);

  /**
   * constructor with content type
   * @param theContentType
   */
  private PwsRestContentType(String theContentType) {
    this.contentType = theContentType;
  }
  
  /** content type of request */
  private String contentType;
  
  /**
   * content type header for http
   * @return the content type
   */
  public String getContentType() {
    return this.contentType;
  }
  
  /** logger */
  private static final Log LOG = PersonWsServerUtils.getLog(PwsRestContentType.class);

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNotFound true to throw exception on not found
   * @return the enum or null or exception if not found
   * @throws PwsRestInvalidRequest problem
   */
  public static PwsRestContentType valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) throws PwsRestInvalidRequest {
    return PersonWsServerUtils.enumValueOfIgnoreCase(PwsRestContentType.class, string, exceptionOnNotFound);
  }

  /** content type thread local */
  private static ThreadLocal<PwsRestContentType> contentTypeThreadLocal = new ThreadLocal<PwsRestContentType>();

  /**
   * 
   * @param wsRestContentType
   */
  public static void assignContentType(PwsRestContentType wsRestContentType) {
    contentTypeThreadLocal.set(wsRestContentType);
  }
  
  /**
   * 
   */
  public static void clearContentType() {
    contentTypeThreadLocal.remove();
  }
  
  /**
   * 
   * @return wsRestContentType
   */
  public static PwsRestContentType retrieveContentType() {
    return contentTypeThreadLocal.get();
  }
  

}
