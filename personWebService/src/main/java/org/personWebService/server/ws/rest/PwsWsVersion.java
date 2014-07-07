package org.personWebService.server.ws.rest;

import org.personWebService.server.util.PersonWsServerUtils;


/**
 * WS grouper version utils
 * @author mchyzer
 *
 */
public enum PwsWsVersion {
  
  /** the first version available */
  v1;
  
  /** 
   * current version
   * this must be two integers separated by dot for version, and build number.
   * update this before each
   * non-release-candidate release (e.g. in preparation for it)
   * e.g. 1.5
   */
  public static final String TF_VERSION = "1.0";

  /**
   * current grouper version
   * @return current grouper version
   */
  public static PwsWsVersion serverVersion() {
    return v1;
  }

  
  /** current client version */
  public static ThreadLocal<PwsWsVersion> currentClientVersion = new ThreadLocal<PwsWsVersion>();

  /**
   * put the current client version
   * @param clientVersion
   * @param warnings 
   */
  public static void assignCurrentClientVersion(PwsWsVersion clientVersion, StringBuilder warnings) {
    currentClientVersion.set(clientVersion);
  }
  
  /**
   * put the current client version
   */
  public static void removeCurrentClientVersion() {
    currentClientVersion.remove();
  }

  /**
   * return current client version or null
   * @return the current client version or null
   */
  public static PwsWsVersion retrieveCurrentClientVersion() {
    return currentClientVersion.get();
  }

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNotFound true to throw exception on not found
   * @return the enum or null or exception if not found
   * @throws PwsRestInvalidRequest problem
   */
  public static PwsWsVersion valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) throws PwsRestInvalidRequest {
    return PersonWsServerUtils.enumValueOfIgnoreCase(PwsWsVersion.class, string, exceptionOnNotFound);
  }

}
