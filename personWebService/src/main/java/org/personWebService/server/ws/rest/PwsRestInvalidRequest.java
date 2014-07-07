/*
 * @author mchyzer $Id: TfRestInvalidRequest.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.personWebService.server.ws.rest;

/**
 * exception when there is not a valid request from client
 * this must be called before any response is written
 */
public class PwsRestInvalidRequest extends RuntimeException {

  /**
   * default id
   */
  private static final long serialVersionUID = 1L;

  /**
   * 
   */
  public PwsRestInvalidRequest() {
    //empty constructor
  }

  /**
   * @param message
   */
  public PwsRestInvalidRequest(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public PwsRestInvalidRequest(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public PwsRestInvalidRequest(String message, Throwable cause) {
    super(message, cause);
  }

}
