package org.personWebService.server.util;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;

/**
 * get the version
 * 
 * @author mchyzer
 */
public class PwsServerVersion {
  
  /**
   * current vesion, e.g. v1_0_17
   * @return the version
   */
  public static String currentVersionString() {
    
    Properties properties = PersonWsServerUtils.propertiesFromResourceName("/org/openTwoFactor/server/version.properties", true, true);
    
    String versionString = properties.getProperty("version");
    
    if (StringUtils.isBlank(versionString)) {
      throw new RuntimeException("No version found");
    }
    
    return versionString;
  }

}
