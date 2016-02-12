/**
 * @author mchyzer
 * $Id$
 */
package org.personWebService.server.ws.rest;


/**
 *
 */
public class PwsRestAuthenticationRequired extends PwsRestInvalidRequest {

  /**
   * 
   */
  public PwsRestAuthenticationRequired() {
  }

  /**
   * @param message
   */
  public PwsRestAuthenticationRequired(String message) {
    super(message);

  }

  /**
   * @param cause
   */
  public PwsRestAuthenticationRequired(Throwable cause) {
    super(cause);

  }

  /**
   * @param message
   * @param cause
   */
  public PwsRestAuthenticationRequired(String message, Throwable cause) {
    super(message, cause);

  }

}
