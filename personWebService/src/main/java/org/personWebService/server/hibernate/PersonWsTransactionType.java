
package org.personWebService.server.hibernate;

import org.personWebService.server.exceptions.PwsDaoException;
import org.personWebService.server.util.PersonWsServerUtils;

/**
 * enum of possible transaction types
 * 
 * @author mchyzer
 */
public enum PersonWsTransactionType {
  
  /** use the current transaction if one exists, if not, create a new readonly tx.
   * Note, the enclosing transaction could be readonly or readwrite, and no error
   * will be thrown.  However, no matter what, this code cannot commit or rollback... */
  READONLY_OR_USE_EXISTING {
    
    /**
     * if there is a transaction (e.g. not NONE)
     * @return the transaction type
     */
    @Override
    public boolean isTransactional() {
      return true;
    }

    /**
     * return if readonly.  note if readonly_if_not_exist it will throw exception since it doesnt 
     * know if readonly or not...
     * @return true if known readonly, false, if known read_write
     */
    @Override
    public boolean isReadonly() {
      throw new RuntimeException("The transaction type is " + this + " so it is not known if" +
      		" readonly or not!");
    }
    
    /**
     * return if new autonomous transaction
     * @return true if known readonly, false, if known read_write
     */
    @Override
    public boolean isNewAutonomous() {
      return false;
    }
    
    /**
     * return if readonly.  note if readonly_if_not_exist it will throw exception since it doesnt 
     * know if readonly or not...
     * @param existingTransactionType if null, no parent, if not, then this is the enclosing
     * type
     * @throws PwsDaoException if there is a compatibility problem
     */
    @Override
    public void checkCompatibility(PersonWsTransactionType existingTransactionType)
      throws PwsDaoException {
      
      //if the underlying is readonly or not, we are fine...
    }
    /**
     * convert the declared tx type to one that is not "if exists"...
     * @return the type to use (e.g. not an if exists one)
     */
    @Override
    public PersonWsTransactionType transactionTypeToUse() {
      return READONLY_NEW;
    }
  },
  
  /** even if in a current tx, do not use transactions */
  NONE {
    
    /**
     * if there is a transaction (e.g. not NONE)
     * @return the transaction type
     */
    @Override
    public boolean isTransactional() {
      return false;
    }

    /**
     * return if readonly.  note if readonly_if_not_exist it will throw exception since it doesnt 
     * know if readonly or not...
     * @return true if known readonly, false, if known read_write
     */
    @Override
    public boolean isReadonly() {
      //none should not have any work in it, but if so, then readonly
      return true;
    }
    
    /**
     * return if new autonomous transaction
     * @return true if known readonly, false, if known read_write
     */
    @Override
    public boolean isNewAutonomous() {
      return true;
    }
    
    /**
     * return if readonly.  note if readonly_if_not_exist it will throw exception since it doesnt 
     * know if readonly or not...
     * @param existingTransactionType if null, no parent, if not, then this is the enclosing
     * type
     * @throws PwsDaoException if there is a compatibility problem
     */
    @Override
    public void checkCompatibility(PersonWsTransactionType existingTransactionType)
      throws PwsDaoException {
      
      //if the underlying is readonly or not, we are fine...
    }
    /**
     * convert the declared tx type to one that is not "if exists"...
     * @return the type to use (e.g. not an if exists one)
     */
    @Override
    public PersonWsTransactionType transactionTypeToUse() {
      return NONE;
    }
  },
  
  /** even if in the middle of a transaction, create a new readonly autonomous nested transaction.  Code
   * in this state cannot commit or rollback.
   */
  READONLY_NEW {
    
    /**
     * if there is a transaction (e.g. not NONE)
     * @return the transaction type
     */
    @Override
    public boolean isTransactional() {
      return true;
    }

    /**
     * return if readonly.  note if readonly_if_not_exist it will throw exception since it doesnt 
     * know if readonly or not...
     * @return true if known readonly, false, if known read_write
     */
    @Override
    public boolean isReadonly() {
      return true;
    }
    
    /**
     * return if new autonomous transaction
     * @return true if known readonly, false, if known read_write
     */
    @Override
    public boolean isNewAutonomous() {
      return true;
    }
    
    /**
     * return if readonly.  note if readonly_if_not_exist it will throw exception since it doesnt 
     * know if readonly or not...
     * @param existingTransactionType if null, no parent, if not, then this is the enclosing
     * type
     * @throws PwsDaoException if there is a compatibility problem
     */
    @Override
    public void checkCompatibility(PersonWsTransactionType existingTransactionType)
      throws PwsDaoException {
      
      //this is a new tx, so if one exists, its fine
    }
    /**
     * convert the declared tx type to one that is not "if exists"...
     * @return the type to use (e.g. not an if exists one)
     */
    @Override
    public PersonWsTransactionType transactionTypeToUse() {
      return this;
    }
  },
  
  /**
   * use the current transaction if one exists.  If there is a current transaction, it 
   * MUST be read/write or there will be an exception.  If there isnt a transaction in 
   * scope, then create a new read/write one.  If you do not commit at the end, and there
   * is a normal return (no exception), then the transaction will be committed if new, 
   * and not if reusing an existing one.  If there is an exception, and the tx is new, it will
   * be rolledback.  If there is an exception and the tx is reused, the tx will not be touched,
   * and the exception will propagate.
   */
  READ_WRITE_OR_USE_EXISTING {
    
    /**
     * if there is a transaction (e.g. not NONE)
     * @return the transaction type
     */
    @Override
    public boolean isTransactional() {
      return true;
    }

    /**
     * return if readonly.  note if readonly_if_not_exist it will throw exception since it doesnt 
     * know if readonly or not...
     * @return true if known readonly, false, if known read_write
     */
    @Override
    public boolean isReadonly() {
      return false;
    }
    
    /**
     * return if new autonomous transaction
     * @return true if known readonly, false, if known read_write
     */
    @Override
    public boolean isNewAutonomous() {
      return false;
    }
    
    /**
     * return if readonly.  note if readonly_if_not_exist it will throw exception since it doesnt 
     * know if readonly or not...
     * @param existingTransactionType if null, no parent, if not, then this is the enclosing
     * type
     * @throws PwsDaoException if there is a compatibility problem
     */
    @Override
    public void checkCompatibility(PersonWsTransactionType existingTransactionType)
      throws PwsDaoException {
      
      if (existingTransactionType != null && existingTransactionType.isReadonly()) {
        throw new PwsDaoException("Problem since this transaction type '" 
            + this + "' requires read/write, but existing" +
        		" is read/only: '" + existingTransactionType + "'");
      }
    }
    /**
     * convert the declared tx type to one that is not "if exists"...
     * @return the type to use (e.g. not an if exists one)
     */
    @Override
    public PersonWsTransactionType transactionTypeToUse() {
      return READ_WRITE_NEW;
    }
  },

  /**
   * even if in the middle of a transaction, create a new read/write autonomous nested transaction.
   * If this block is exited normally it will always commit.  If exception is thrown, it will 
   * always rollback.
   */
  READ_WRITE_NEW {
    
    /**
     * if there is a transaction (e.g. not NONE)
     * @return the transaction type
     */
    @Override
    public boolean isTransactional() {
      return true;
    }

    /**
     * return if readonly.  note if readonly_if_not_exist it will throw exception since it doesnt 
     * know if readonly or not...
     * @return true if known readonly, false, if known read_write
     */
    @Override
    public boolean isReadonly() {
      return false;
    }
    
    /**
     * return if new autonomous transaction
     * @return true if known readonly, false, if known read_write
     */
    @Override
    public boolean isNewAutonomous() {
      return true;
    }
    
    /**
     * return if readonly.  note if readonly_if_not_exist it will throw exception since it doesnt 
     * know if readonly or not...
     * @param existingTransactionType if null, no parent, if not, then this is the enclosing
     * type
     * @throws PwsDaoException if there is a compatibility problem
     */
    @Override
    public void checkCompatibility(PersonWsTransactionType existingTransactionType)
      throws PwsDaoException {
      
      //this is a new tx, so if one exists, its fine
    }
    /**
     * convert the declared tx type to one that is not "if exists"...
     * @return the type to use (e.g. not an if exists one)
     */
    @Override
    public PersonWsTransactionType transactionTypeToUse() {
      return this;
    }
  };
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @return the enum or null or exception if not found
   */
  public static PersonWsTransactionType valueOfIgnoreCase(String string) {
    return PersonWsServerUtils.enumValueOfIgnoreCase(PersonWsTransactionType.class,string, false );
  }

  /**
   * return if readonly.  note if readonly_if_not_exist it will throw exception since it doesnt 
   * know if readonly or not...
   * @return true if known readonly, false, if known read_write
   */
  public abstract boolean isReadonly();

  /**
   * return if new autonomous transaction
   * @return true if new, false if not
   */
  public abstract boolean isNewAutonomous();

  /**
   * if there is a transaction (e.g. not NONE)
   * @return the transaction type
   */
  public abstract boolean isTransactional();
  
  /**
   * return if readonly.  note if readonly_if_not_exist it will throw exception since it doesnt 
   * know if readonly or not...
   * @param existingTransactionType if null, no parent, if not, then this is the enclosing
   * type
   * @throws PwsDaoException if there is a compatibility problem
   */
  public abstract void checkCompatibility(PersonWsTransactionType existingTransactionType)
    throws PwsDaoException;

  /**
   * convert the declared tx type to one that is not "if exists"...
   * @return the type to use (e.g. not an if exists one)
   */
  public abstract PersonWsTransactionType transactionTypeToUse();

}
