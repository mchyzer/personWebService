/**
 * @author mchyzer
 * $Id: TfHtmlFilter.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.personWebService.server.util;


/**
 * filter HTML
 */
public interface PwsHtmlFilter {

  /**
   * filter html from a string
   * @param html
   * @return the html to filter
   */
  public String filterHtml(String html);
  
}
