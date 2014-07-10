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
   * 1 based start index for query, cant be less than 1
   */
  private int startIndex = 1;
  
  
  /**
   * 1 based start index for query, cant be less than 1
   * @return the startIndex
   */
  public int getStartIndex() {
    return this.startIndex;
  }

  
  /**
   * 1 based start index for query, cant be less than 1
   * @param startIndex1 the startIndex to set
   */
  public void setStartIndex(int startIndex1) {
    if (startIndex1 < 1) {
      startIndex1 = 1;
    }
    this.startIndex = startIndex1;
  }

  /**
   * count of results to fetch.  Note, if more than max, then will be set to max
   */
  private Integer count = null;

  /**
   * count of results to fetch.  Note, if more than max, then will be set to max
   * @return the count
   */
  public Integer getCount() {
    return this.count;
  }
  
  /**
   * count of results to fetch.  Note, if more than max, then will be set to max
   * @param count1 the count to set
   */
  public void setCount(int count1) {
    if (count1 < 0) {
      count1 = 0;
    }
    this.count = count1;
  }

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
