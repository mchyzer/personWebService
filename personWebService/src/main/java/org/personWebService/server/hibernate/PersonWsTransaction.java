
package org.personWebService.server.hibernate;

/**
 * 
 * Use this class to make a transaction around operations
 * (can also use HibernateSession, thoguh if hib it will throw exceptions)
 * 
 * @author mchyzer
 *
 */
public class PersonWsTransaction {
  
  /**
   * provide ability to turn off all caching for this session
   * @return the enabledCaching
   */
  public boolean isCachingEnabled() {
    //not sure why this would ever be null or not a hibernate session... hmmm
    return ((HibernateSession)this.payload).isCachingEnabled();
  }
  
  /**
   * provide ability to turn off all caching for this session
   * @param enabledCaching1 the enabledCaching to set
   */
  public void setCachingEnabled(boolean enabledCaching1) {
    //not sure why this would ever be null or not a hibernate session... hmmm
    ((HibernateSession)this.payload).setCachingEnabled(enabledCaching1);
  }

  /**
   * the dao can store some state here
   */
  private Object payload;
  
  /**
   * the dao can store some state here
   * @return the payload
   */
  public Object _internal_getPayload() {
    return this.payload;
  }

  /**
   * the dao can store some state here
   * @param payload1 the payload to set
   */
  public void _internal_setPayload(Object payload1) {
    this.payload = payload1;
  }

  /**
   * call this to establish a transaction demarcation for the PersonWsTransactionHandler business logic
   * 
   * @param transactionType
   *          is enum of how the transaction should work.
   * @param transactionHandler
   *          will get the callback
   * @return the object returned from the callback
   * @throws RuntimeException if something wrong inside, not sure which exceptions... its
   * whatever your methods throw
   */
  public static Object callbackTransaction(
      PersonWsTransactionType transactionType,
      PersonWsTransactionHandler transactionHandler) {
    
    return PersonWsDaoFactory.getFactory().getTransaction().transactionCallback(transactionType,
        transactionHandler, new PersonWsTransaction());

  }

  /**
   * call this to establish a transaction demarcation for the PersonWsTransactionHandler business logic.
   * The transaction type will be READ_WRITE_OR_USE_EXISTING (this is the default since probably
   * is what is wanted).
   * 
   * @param transactionHandler
   *          will get the callback
   * @return the object returned from the callback
   * @throws RuntimeException if something wrong inside, not sure which exceptions... its
   * whatever your methods throw
   */
  public static Object callbackTransaction(
      PersonWsTransactionHandler transactionHandler) {
    
    return callbackTransaction(PersonWsTransactionType.READ_WRITE_OR_USE_EXISTING,
        transactionHandler);
  }

  /**
   * commit a transaction (perhaps, based on type)
   * @param commitType
   * @return if the tx committed
   */
  public boolean commit(PwsCommitType commitType) {
    return PersonWsDaoFactory.getFactory().getTransaction().transactionCommit(this, commitType);
  }
  
  /**
   * rollback a transaction (perhaps, based on type)
   * @param rollbackType
   * @return if the tx rolled back
   */
  public boolean rollback(PwsRollbackType rollbackType) {
    return PersonWsDaoFactory.getFactory().getTransaction().transactionRollback(this, rollbackType);
  }

  /**
   * see if a transaction has an open transaction (that hasnt been committed or rolled back yet)
   * @return true if transaction exists and active (not committed or rolled back)
   */
  public boolean isTransactionActive() {
    return PersonWsDaoFactory.getFactory().getTransaction().transactionActive(this);
  }

}
