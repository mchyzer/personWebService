package org.personWebService.server.hibernate;

import java.io.Serializable;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.hibernate.Session;
import org.personWebService.server.exceptions.PwsDaoException;
import org.personWebService.server.exceptions.PwsStaleObjectStateException;
import org.personWebService.server.util.PersonWsServerUtils;



/**
 * @version $Id: ByObject.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 * @author mchyzer
 */
public class ByObject extends HibernateDelegate {

  /** logger */
  private static final Log LOG = PersonWsServerUtils.getLog(ByObject.class);

  /**
   * assign the entity name to refer to this mapping (multiple mappings per object)
   */
  private String entityName = null;

  /**
   * @param theHibernateSession
   */
  ByObject(HibernateSession theHibernateSession) {
    super(theHibernateSession);
  }

  /**
   * <pre>
   * call hibernate method "delete" on a list of objects
   * 
   * HibernateSession.byObjectStatic().delete(collection);
   * 
   * </pre>
   * @param collection is collection of objects to delete in one transaction.  If null or empty just ignore
   * @throws PwsDaoException
   */
  public void delete(final Collection<?> collection) throws PwsDaoException {
    if (collection == null) {
      return;
    }
    try {
      for (Object object : collection) {
        delete(object);
      }
    } catch (PwsStaleObjectStateException e) {
      throw e;
    } catch (RuntimeException e) {
      String errorString = "Exception in delete: " + PersonWsServerUtils.classNameCollection(collection) + ", " + this;
      if (!PersonWsServerUtils.injectInException(e, errorString)) {
        LOG.error(errorString, e);
      }
      throw e;
    }
  }

  /**
   * <pre>
   * call hibernate method "delete" on a list of objects
   * 
   * HibernateSession.byObjectStatic().delete(Rosetta.getDAO(_f));
   * 
   * </pre>
   * @param object is an object (if collection will still work), if null, will probably throw exception
   * @throws PwsDaoException
   */
  public void delete(final Object object) throws PwsDaoException {
    
    //dont fail if collection in there
    if (object instanceof Collection) {
      delete((Collection)object);
      return;
    }
    try {

      HibernateSession.assertNotReadonly();

      HibernateSession hibernateSession = this.getHibernateSession();
      Session session  = hibernateSession.getSession();
      
      if (!this.isIgnoreHooks() && object instanceof HibPwsLifecycle) {
        ((HibPwsLifecycle)object).onPreDelete(hibernateSession);
      }

      if (StringUtils.isBlank(this.entityName)) {
        session.delete(object);
      } else {
        session.delete(this.entityName, object);
      }
      
      if (!this.isIgnoreHooks() && object instanceof HibPwsLifecycle) {
        ((HibPwsLifecycle)object).onPostDelete(hibernateSession);
      }

    } catch (PwsStaleObjectStateException e) {
      throw e;
    } catch (RuntimeException e) {
      String errorString = "Exception in delete: " + PersonWsServerUtils.classNameCollection(object) + ", " + this;
      if (!PersonWsServerUtils.injectInException(e, errorString)) {
        LOG.error(errorString, e);
      }
      throw e;
    }
    
  }

  /**
   * <pre>
   * call hibernate method "save" on a list of objects
   * 
   * HibernateSession.byObjectStatic().save(collection);
   * 
   * </pre>
   * @param collection is collection of objects to save in one transaction.  If null or empty just ignore
   * @throws PwsDaoException
   */
  public void save(final Collection<?> collection) throws PwsDaoException {
    if (collection == null) {
      return;
    }
    try {
      for (Object object : collection) {
        save(object);
      }
    } catch (PwsStaleObjectStateException e) {
      throw e;
    } catch (RuntimeException e) {
      String errorString = "Exception in save: " + PersonWsServerUtils.classNameCollection(collection) + ", " + this;

      if (!PersonWsServerUtils.injectInException(e, errorString)) {
        LOG.error(errorString, e);
      }

      throw e;
    }
  }


  /**
   * <pre>
   * call hibernate method "save" on an object
   * 
   * HibernateSession.byObjectStatic().save(dao);
   * 
   * </pre>
   * @param object to save
   * @return the id
   * @throws PwsDaoException
   */
  public Serializable save(final Object object) throws PwsDaoException {

    //dont fail if collection in there
    if (object instanceof Collection) {
      save((Collection)object);
      return null;
    }
    try {

      HibernateSession.assertNotReadonly();
      
      HibernateSession hibernateSession = this.getHibernateSession();
      Session session  = hibernateSession.getSession();

      if (!this.isIgnoreHooks() && object instanceof HibPwsLifecycle) {
        ((HibPwsLifecycle)object).onPreSave(hibernateSession);
      }

      Serializable id = null;
      if (StringUtils.isBlank(this.entityName)) {

        id = session.save(object);
      } else {
        id = session.save(this.entityName, object);
      }

      session.flush();
      
      if (!this.isIgnoreHooks() && object instanceof HibPwsLifecycle) {
        ((HibPwsLifecycle)object).onPostSave(hibernateSession);
      }
      return id;
    } catch (PwsStaleObjectStateException e) {
      throw e;
    } catch (RuntimeException e) {
      
      String errorString = "Exception in save: " + PersonWsServerUtils.className(object) + ", " + this;
      
      if (!PersonWsServerUtils.injectInException(e, errorString)) {
        LOG.error(errorString, e);
      }

      throw e;
    }
    
  }
  
  /**
   * <pre>
   * call hibernate method "save" on a collection of objects in batch
   * 
   * </pre>
   * @param collection of objects to save
   * @throws PwsDaoException
   */
  public void saveBatch(final Collection<?> collection) throws PwsDaoException {
    try {
      HibernateSession hibernateSession = this.getHibernateSession();
      Session session = hibernateSession.getSession();

      for (Object object : collection) {
        if (!this.isIgnoreHooks() && object instanceof HibPwsLifecycle) {
          ((HibPwsLifecycle)object).onPreSave(hibernateSession);
        }
      }
      
      for (Object object : collection) {
        
        if (StringUtils.isBlank(this.entityName)) {
          session.save(object);
        } else {
          session.save(this.entityName, object);
        }
      }

      session.flush();
      session.clear();
      
      for (Object object : collection) {
        if (!this.isIgnoreHooks() && object instanceof HibPwsLifecycle) {
          ((HibPwsLifecycle)object).onPostSave(hibernateSession);
        }
      }

    } catch (PwsStaleObjectStateException e) {
      throw e;
    } catch (RuntimeException e) {

      String errorString = "Exception in save: " + PersonWsServerUtils.classNameCollection(collection) + ", " + this;
      
      if (!PersonWsServerUtils.injectInException(e, errorString)) {
        LOG.error(errorString, e);
      }

      throw e;
    }
    
  }

  /**
   * <pre>
   * call hibernate method "saveOrUpdate" on a list of objects
   * 
   * HibernateSession.byObjectStatic().saveOrUpdate(collection);
   * 
   * </pre>
   * @param collection is collection of objects to saveOrUpdate in one transaction.  If null or empty just ignore
   * @throws PwsDaoException
   */
  public void saveOrUpdate(final Collection<?> collection) throws PwsDaoException {
    if (collection == null) {
      return;
    }
    try {
      for (Object object : collection) {
        saveOrUpdate(object);
      }
    } catch (PwsStaleObjectStateException e) {
      throw e;
    } catch (RuntimeException e) {
      
      String errorString = "Exception in saveOrUpdate: " + PersonWsServerUtils.classNameCollection(collection) + ", " + this;
      
      if (!PersonWsServerUtils.injectInException(e, errorString)) {
        LOG.error(errorString, e);
      }

      throw e;
    }
  }


  /**
   * <pre>
   * call hibernate method "save" on an object
   * 
   * HibernateSession.byObjectStatic().save(dao);
   * 
   * </pre>
   * @param object to save
   * @throws PwsDaoException
   */
  public void saveOrUpdate(final Object object) throws PwsDaoException {

    //dont fail if collection in there
    if (object instanceof Collection) {
      saveOrUpdate((Collection)object);
      return;
    }
    try {

      HibernateSession.assertNotReadonly();
      
      HibernateSession hibernateSession = this.getHibernateSession();
      Session session  = hibernateSession.getSession();

      Boolean isInsert = null;
      if (!this.isIgnoreHooks() && object instanceof HibPwsLifecycle) {
        isInsert = PwsHibUtilsMapping.isInsert(object);
        if (isInsert) {
          ((HibPwsLifecycle)object).onPreSave(hibernateSession);
        } else {
          ((HibPwsLifecycle)object).onPreUpdate(hibernateSession);
        }
      }

      if (StringUtils.isBlank(this.entityName)) {
        session.saveOrUpdate(object);
      } else {
        session.saveOrUpdate(this.entityName, object);
      }

      try {
        session.flush();
      } catch (RuntimeException re) {
        throw re;
      }
      if (!this.isIgnoreHooks() && object instanceof HibPwsLifecycle) {
        if (isInsert) {
          ((HibPwsLifecycle)object).onPostSave(hibernateSession);
        } else {
          ((HibPwsLifecycle)object).onPostUpdate(hibernateSession);
        }
      }
    } catch (PwsStaleObjectStateException e) {
      throw e;
    } catch (RuntimeException e) {
      
      String errorString = "Exception in save: " + PersonWsServerUtils.className(object) + ", " + this;

      if (!PersonWsServerUtils.injectInException(e, errorString)) {
        LOG.error(errorString, e);
      }

      throw e;
    }
    
  }

  /**
   * <pre>
   * call hibernate method "load" on an object
   * 
   * </pre>
   * @param <T> 
   * @param theClass to load
   * @param id to find in db
   * @return the result
   * @throws PwsDaoException
   */
  @SuppressWarnings("unchecked")
  public <T> T load(final Class<T> theClass, Serializable id) throws PwsDaoException {
    try {
      HibernateSession hibernateSession = this.getHibernateSession();
      Session session  = hibernateSession.getSession();
      T result = null;
      if (StringUtils.isBlank(this.entityName)) {
        result = (T)session.load(theClass, id);
      } else {
        result = (T)session.load(this.entityName, id);
      }
      
      return result;

    } catch (PwsStaleObjectStateException e) {
      throw e;
    } catch (RuntimeException e) {
      
      String errorString = "Exception in load: " + theClass + ", " 
          + id + ", " + this;

      if (!PersonWsServerUtils.injectInException(e, errorString)) {
        LOG.error(errorString, e);
      }

      throw e;
    }
  }

  /**
   * <pre>
   * call hibernate method "update" on a list of objects
   * 
   * HibernateSession.byObjectStatic().update(collection);
   * 
   * </pre>
   * @param collection is collection of objects to update in one transaction.  If null or empty just ignore
   * @throws PwsDaoException
   */
  public void update(final Collection<?> collection) throws PwsDaoException {
    if (collection == null) {
      return;
    }
    try {
      for (Object object : collection) {
        update(object);
      }
    } catch (PwsStaleObjectStateException e) {
      throw e;
    } catch (RuntimeException e) {
      
      String errorString = "Exception in update: " + PersonWsServerUtils.classNameCollection(collection) + ", " + this;
      
      if (!PersonWsServerUtils.injectInException(e, errorString)) {
        LOG.error(errorString, e);
      }

      throw e;
    }
  }


  /**
   * call hibernate "update" method on an object
   * @param object to update
   * @throws PwsDaoException
   */
  public void update(final Object object) throws PwsDaoException {

    try {

      HibernateSession.assertNotReadonly();

      HibernateSession hibernateSession = this.getHibernateSession();
      Session session  = hibernateSession.getSession();
      
      if (!this.isIgnoreHooks() && object instanceof HibPwsLifecycle) {
        ((HibPwsLifecycle)object).onPreUpdate(hibernateSession);
      }

      if (StringUtils.isBlank(this.entityName)) {

        session.update(object);
      } else {
        session.update(this.entityName, object);
      }
      
      if (!this.isIgnoreHooks() && object instanceof HibPwsLifecycle) {
        ((HibPwsLifecycle)object).onPostUpdate(hibernateSession);
      }
      
    } catch (PwsStaleObjectStateException e) {
      throw e;
    } catch (RuntimeException e) {
      
      String errorString = "Exception in update: " + PersonWsServerUtils.className(object) + ", " + this;
      
      if (!PersonWsServerUtils.injectInException(e, errorString)) {
        LOG.error(errorString, e);
      }

      throw e;
    }
    
  }

  /**
   * @see ByQueryBase#setIgnoreHooks(boolean)
   */
  @Override
  public ByObject setIgnoreHooks(boolean theIgnoreHooks) {
    return (ByObject)super.setIgnoreHooks(theIgnoreHooks);
  }

  /**
   * entity name if the object is mapped to more than one table
   * @param theEntityName the entity name of the object
   * @return this object for chaining
   */
  public ByObject setEntityName(String theEntityName) {
    this.entityName = theEntityName;
    return this;
  }
}
