package org.personWebService.server.hibernate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.personWebService.server.databasePerf.PwsDatabasePerfLog;
import org.personWebService.server.util.PersonWsServerUtils;


/**
 * 
 * for simple HQL, use this instead of inverse of control.
 * this will do proper error handling and descriptive exception
 * handling.  This will by default use the transaction modes
 * PersonWsTransactionType.READONLY_OR_USE_EXISTING, and 
 * PersonWsTransactionType.READ_WRITE_OR_USE_EXISTING depending on
 * if a transaction is needed.
 * 
 * @author mchyzer
 *
 */
public class BySql extends HibernateDelegate {
  
  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = PersonWsServerUtils.getLog(BySql.class);

  /** assign a transaction type, default use the transaction modes
   * PersonWsTransactionType.READONLY_OR_USE_EXISTING, and 
   * PersonWsTransactionType.READ_WRITE_OR_USE_EXISTING depending on
   * if a transaction is needed */
  private PersonWsTransactionType transactionType = null;
  
  /**
   * assign a different transactionType (e.g. for autonomous transactions)
   * @param theTransactionType
   * @return the same object for chaining
   */
  public BySql setTransactionType(PersonWsTransactionType 
      theTransactionType) {
    this.transactionType = theTransactionType;
    return this;
  }
  
  /**
   * string value for error handling
   * @return the string value
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("BySql, query: '");
    result.append(this.query);
    result.append(", tx type: ").append(this.transactionType);
    //dont use bindVars() method so it doesnt lazy load
    if (this.bindVars != null) {
      int index = 0;
      int size = this.bindVars().size();
      for (Object object : this.bindVars()) {
        result.append("Bind var[").append(index++).append("]: '");
        result.append(PersonWsServerUtils.toStringForLog(object, 50));
        if (index!=size-1) {
          result.append(", ");
        }
      }
    }
    return result.toString();
  }
  /**
   * map of params to attach to the query.
   * access this with the bindVarNameParams method
   */
  private List<Object> bindVars = null;

  /**
   * query to execute
   */
  private String query = null;

  /**
   * set the query to run
   * @param theHqlQuery
   * @return this object for chaining
   */
  public BySql createQuery(String theHqlQuery) {
    this.query = theHqlQuery;
    return this;
  }
  
  /**
   * lazy load params
   * @return the params map
   */
  private List<Object> bindVars() {
    if (this.bindVars == null) {
      this.bindVars = new ArrayList<Object>();
    }
    return this.bindVars;
  }
  
  /** query count exec queries, used for testing */
  public static int queryCountQueries = 0;
  

  /**
   * execute some sql
   * @param sql can be insert, update, delete, or ddl
   * @param params prepared statement params
   * @return the number of rows affected or 0 for ddl
   */
  @SuppressWarnings("deprecation")
  public int executeSql(final String sql, final List<Object> params) {
  
    HibernateSession hibernateSession = this.getHibernateSession();
    hibernateSession.misc().flush();
    long start = System.nanoTime();
    PreparedStatement preparedStatement = null;
    int result = -1;
    try {
      
      //we dont close this connection or anything since could be pooled
      Connection connection = hibernateSession.getSession().connection();
      preparedStatement = connection.prepareStatement(sql);
  
      BySqlStatic.attachParams(preparedStatement, params);
      
      result = preparedStatement.executeUpdate();
      
      return result;

    } catch (Exception e) {
      throw new RuntimeException("Problem with query in bysqlstatic: " + sql, e);
    } finally {
      PersonWsServerUtils.closeQuietly(preparedStatement);
      if (PwsDatabasePerfLog.LOG.isDebugEnabled()) {
        PwsDatabasePerfLog.dbPerfLog(((System.nanoTime()-start)/1000000L) + "ms SQL: " + sql + ", params: " + PersonWsServerUtils.toStringForLog(params) + ", result: " + result);
      }
    }
  
  }


  /**
   * @param theHibernateSession
   */
  public BySql(HibernateSession theHibernateSession) {
    super(theHibernateSession);
  }


  
  /**
   * @param bindVars1 the bindVars to set
   */
  void setBindVars(List<Object> bindVars1) {
    this.bindVars = bindVars1;
  }


  
  /**
   * @param query1 the query to set
   */
  void setQuery(String query1) {
    this.query = query1;
  }
  
  
  
}
