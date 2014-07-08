/**
 * @author mchyzer
 * $Id: TfCheckPasswordRequest.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.personWebService.server.ws.corebeans;



/**
 * request for users search
 */
public class PwsUsersSearchRequest {

  /**
   * if we should split trim the query strings by whitespace
   */
  private boolean splitTrim;

  /**
   * filter of the search
   */
  private String filter;

  
  /**
   * if we should split trim the query strings by whitespace
   * @return the xCiferSplitTrim
   */
  public boolean isSplitTrim() {
    return this.splitTrim;
  }

  
  /**
   * if we should split trim the query strings by whitespace
   * @param xCiferSplitTrim1 the xCiferSplitTrim to set
   */
  public void setSplitTrim(boolean xCiferSplitTrim1) {
    this.splitTrim = xCiferSplitTrim1;
  }

  
  /**
   * filter of the search
   * @return the filter
   */
  public String getFilter() {
    return this.filter;
  }

  
  /**
   * filter of the search
   * @param filter1 the filter to set
   */
  public void setFilter(String filter1) {
    this.filter = filter1;
  }
  
  
}
