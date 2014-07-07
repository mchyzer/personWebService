package org.personWebService.server.ws.corebeans;

import org.personWebService.server.beans.PwsNode;
import org.personWebService.server.util.PersonWsServerUtils;



/**
 * base class that beans extends
 * @author mchyzer
 */
public class PwsResponseBean {

  /**
   * http response code defaults to 200
   */
  private int httpResponseCode = 200;
  
  /**
   * http response code defaults to 200
   * @return the httpResponseCode
   */
  public int getHttpResponseCode() {
    return this.httpResponseCode;
  }
  
  /**
   * http response code defaults to 200
   * @param httpResponseCode1 the httpResponseCode to set
   */
  public void setHttpResponseCode(int httpResponseCode1) {
    this.httpResponseCode = httpResponseCode1;
  }

  /**
   * pws node which is the JSON of the response (minus all the standard stuff)
   */
  private PwsNode pwsNode;
  
  
  /**
   * pws node which is the JSON of the response (minus all the standard stuff)
   * @return the pwsNode
   */
  public PwsNode getPwsNode() {
    return this.pwsNode;
  }
  
  /**
   * pws node which is the JSON of the response (minus all the standard stuff)
   * @param pwsNode1 the pwsNode to set
   */
  public void setPwsNode(PwsNode pwsNode1) {
    this.pwsNode = pwsNode1;
  }

  /**
   * "debug" input param: true|false If true or if a param is set on the server, 
   * then a debug message will be returned
   */
  private StringBuilder debugMessage = new StringBuilder();
  
  /**
   * code which doesnt change which can be parsed about the state of the result
   */
  private String resultCode;
  
  
  
  
  /**
   * code which doesnt change which can be parsed about the state of the result
   * @return the resultCode
   */
  public String getResultCode() {
    return this.resultCode;
  }

  
  /**
   * code which doesnt change which can be parsed about the state of the result
   * @param resultCode1 the resultCode to set
   */
  public void setResultCode(String resultCode1) {
    this.resultCode = resultCode1;
  }

  /**
   * friendly message that could change, this is for logging and shouldnt be parsed
   * e.g. User is authenticated"
   */
  private String responseMessage;

  /**
   * friendly message that could change, this is for logging and shouldnt be parsed
   * e.g. User is authenticated"
   * @return the responseMessage
   */
  public String getResponseMessage() {
    return this.responseMessage;
  }
  
  /**
   * friendly message that could change, this is for logging and shouldnt be parsed
   * e.g. User is authenticated"
   * @param responseMessage1 the responseMessage to set
   */
  public void setResponseMessage(String responseMessage1) {
    this.responseMessage = responseMessage1;
  }

  /** 
   * if there are warnings, they will be there
   */
  private StringBuilder resultWarning = new StringBuilder();

  /**
   * append error message to list of error messages
   * 
   * @param warning
   */
  public void appendWarning(String warning) {
    if (this.resultWarning.length() > 0) {
      this.resultWarning.append(", ");
    }
    this.resultWarning.append(warning);
  }

  /**
   * if there are warnings, they will be there
   * @return any warnings
   */
  public String getWarning() {
    return PersonWsServerUtils.trimToNull(this.resultWarning.toString());
  }

  /**
   * the builder for warnings
   * @return the builder for warnings
   */
  public StringBuilder warnings() {
    return this.resultWarning;
  }


  /**
   * @param resultWarnings1 the resultWarnings to set
   */
  public void setWarning(String resultWarnings1) {
    this.resultWarning = PersonWsServerUtils.isBlank(resultWarnings1) ? new StringBuilder() : new StringBuilder(resultWarnings1);
  }

  /**
   * if the request was valid and processed correctly
   */
  private Boolean success;
  /**
   * if there is an error (not success), this is the error message)
   */
  private String errorMessage;

  /**
   * 
   */
  public PwsResponseBean() {
  }

  /**
   * if the request was valid and processed correctly
   * @return if success
   */
  public Boolean getSuccess() {
    return this.success;
  }

  /**
   * if the request was valid and processed correctly
   * @param success1
   */
  public void setSuccess(Boolean success1) {
    this.success = success1;
  }

  /**
   * if there is an error (not success), this is the error message)
   * @return error
   */
  public String getErrorMessage() {
    return this.errorMessage;
  }

  /**
   * if there is an error (not success), this is the error message)
   * @param errorMessage1
   */
  public void setErrorMessage(String errorMessage1) {
    this.errorMessage = errorMessage1;
  }

  /**
   * append error message to list of error messages
   * 
   * @param debug
   */
  public void appendDebug(String debug) {
    if (this.debugMessage.length() > 0) {
      this.debugMessage.append(", ");
    }
    this.debugMessage.append(debug);
  }

  /**
   * if there are debug messages, they will be there
   * @return any debugs
   */
  public String getDebugMessage() {
    return PersonWsServerUtils.trimToNull(this.debugMessage.toString());
  }

  /**
   * @param debugMessage1 the debug to set
   */
  public void setDebugMessage(String debugMessage1) {
    this.debugMessage = PersonWsServerUtils.isBlank(debugMessage1) ? new StringBuilder() : new StringBuilder(debugMessage1);
  }


  
}
