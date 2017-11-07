package org.opennms.netmgt.eventd;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import javax.persistence.Table;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.opennms.netmgt.dao.api.OnmsDao;
import org.opennms.netmgt.dao.hibernate.AccessLock;
import org.opennms.netmgt.dao.hibernate.HibernateCriteriaConverter;
import org.opennms.netmgt.model.OnmsCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.HibernateTemplate;

/**
 * <p>Abstract AbstractDaoHibernate class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class AbstractDaoHibernate<T, K extends Serializable>  implements OnmsDao<T, K> {
    
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDaoHibernate.class);
    Class<T> m_entityClass;
    private String m_lockName;
    protected final HibernateCriteriaConverter m_criteriaConverter = new HibernateCriteriaConverter();
    
    @Autowired
    private HibernateTemplate hibernateTemplate;
    
    public void setHibernateTemplate() {
    	
    }
    public AbstractDaoHibernate(final Class<T> entityClass) {
        super();
        m_entityClass = entityClass;
        Table table = m_entityClass.getAnnotation(Table.class);
        m_lockName = (table == null || "".equals(table.name()) ? m_entityClass.getSimpleName() : table.name()).toUpperCase() + "_ACCESS";
    }


    /** {@inheritDoc} */
    @Override
    public void lock() {
    	hibernateTemplate.get(AccessLock.class, m_lockName, LockMode.PESSIMISTIC_WRITE);
    }

    /** {@inheritDoc} */
    @Override
    public void initialize(final Object obj) {
    	hibernateTemplate.initialize(obj);
    }

    /** {@inheritDoc} */
    @Override
    public void flush() {
        hibernateTemplate.flush();
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        hibernateTemplate.flush(); // always flush before clearing, otherwise pending updates/saves are not executed
        hibernateTemplate.clear();
    }

    public void merge(final T entity) {
        hibernateTemplate.merge(entity);
    }

    /**
     * <p>find</p>
     *
     * @param query a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    @SuppressWarnings("unchecked")
    public List<T> find(final String query) {
        return (List<T>)hibernateTemplate.find(query);
    }

    /**
     * <p>find</p>
     *
     * @param query a {@link java.lang.String} object.
     * @param values a {@link java.lang.Object} object.
     * @return a {@link java.util.List} object.
     */
    @SuppressWarnings("unchecked")
    public List<T> find(final String query, final Object... values) {
        return (List<T>)hibernateTemplate.find(query, values);
    }
    
    /**
     * <p>findObjects</p>
     *
     * @param clazz a {@link java.lang.Class} object.
     * @param query a {@link java.lang.String} object.
     * @param values a {@link java.lang.Object} object.
     * @param <S> a S object.
     * @return a {@link java.util.List} object.
     */
    public <S> List<S> findObjects(final Class<S> clazz, final String query, final Object... values) {
        @SuppressWarnings("unchecked")
        final List<S> notifs = (List<S>)hibernateTemplate.find(query, values);
        return notifs;
    }

    /**
     * <p>queryInt</p>
     *
     * @param query a {@link java.lang.String} object.
     * @return a int.
     */
    protected int queryInt(final String query) {
    	final HibernateCallback<Number> callback = new HibernateCallback<Number>() {
            @Override
            public Number doInHibernate(final Session session) throws HibernateException {
                return (Number)session.createQuery(query).uniqueResult();
            }
        };

        return hibernateTemplate.execute(callback).intValue();
    }

    /**
     * <p>queryInt</p>
     *
     * @param queryString a {@link java.lang.String} object.
     * @param args a {@link java.lang.Object} object.
     * @return a int.
     */
    protected int queryInt(final String queryString, final Object... args) {
    	final HibernateCallback<Number> callback = new HibernateCallback<Number>() {
            @Override
            public Number doInHibernate(final Session session) throws HibernateException {
            	final Query query = session.createQuery(queryString);
                for (int i = 0; i < args.length; i++) {
                    query.setParameter(i, args[i]);
                }
                return (Number)query.uniqueResult();
            }

        };

        return hibernateTemplate.execute(callback).intValue();
    }

    /**
     * Return a single instance that matches the query string, 
     * or null if the query returns no results.
     */
    protected T findUnique(final String queryString, final Object... args) {
        final Class <? extends T> type = m_entityClass;
    	final HibernateCallback<T> callback = new HibernateCallback<T>() {
            @Override
            public T doInHibernate(final Session session) throws HibernateException {
            	final Query query = session.createQuery(queryString);
                for (int i = 0; i < args.length; i++) {
                    query.setParameter(i, args[i]);
                }
                final Object result = query.uniqueResult();
                return result == null ? null : type.cast(result);
            }

        };
        return hibernateTemplate.execute(callback);
    }

    /**
     * <p>countAll</p>
     *
     * @return a int.
     */
    @Override
    public int countAll() {
        return queryInt("select count(*) from " + m_entityClass.getName());
    }

    /**
     * <p>delete</p>
     *
     * @param entity a T object.
     * @throws org.springframework.dao.DataAccessException if any.
     */
    @Override
    public void delete(final T entity) throws DataAccessException {
        hibernateTemplate.delete(entity);
    }
    
    /**
     * <p>delete</p>
     *
     * @param key a K object.
     * @throws org.springframework.dao.DataAccessException if any.
     */
    @Override
    public void delete(final K key) throws DataAccessException {
        delete(get(key));
    }
    
    /**
     * <p>deleteAll</p>
     *
     * @param entities a {@link java.util.Collection} object.
     * @throws org.springframework.dao.DataAccessException if any.
     */
    public void deleteAll(final Collection<T> entities) throws DataAccessException {
        hibernateTemplate.deleteAll(entities);
    }

    /**
     * <p>findAll</p>
     *
     * @return a {@link java.util.List} object.
     * @throws org.springframework.dao.DataAccessException if any.
     */
    @Override
    public List<T> findAll() throws DataAccessException {
        return hibernateTemplate.loadAll(m_entityClass);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> findMatching(final org.opennms.core.criteria.Criteria criteria) {
        final HibernateCallback<List<T>> callback = new HibernateCallback<List<T>>() {
            @Override
            public List<T> doInHibernate(final Session session) throws HibernateException {
                LOG.debug("criteria = {}", criteria);
                final Criteria hibernateCriteria = m_criteriaConverter.convert(criteria, session);
                return (List<T>)(hibernateCriteria.list());
            }
        };
        return hibernateTemplate.execute(callback);
    }

    /** {@inheritDoc} */
    @Override
    public int countMatching(final org.opennms.core.criteria.Criteria criteria) throws DataAccessException {
        final HibernateCallback<Integer> callback = new HibernateCallback<Integer>() {
            @Override
            public Integer doInHibernate(final Session session) throws HibernateException {
                
                final Criteria hibernateCriteria = m_criteriaConverter.convertForCount(criteria, session);
                hibernateCriteria.setProjection(Projections.rowCount());
                Long retval = (Long)hibernateCriteria.uniqueResult();
                hibernateCriteria.setProjection(null);
                hibernateCriteria.setResultTransformer(Criteria.ROOT_ENTITY);
                return retval.intValue();
            }
        };
        Integer retval = hibernateTemplate.execute(callback);
        return retval == null ? 0 : retval.intValue();
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public List<T> findMatching(final OnmsCriteria onmsCrit) throws DataAccessException {
        onmsCrit.resultsOfType(m_entityClass); //FIXME: why is this here?
        
        final HibernateCallback<List<T>> callback = new HibernateCallback<List<T>>() {
            @Override
            public List<T> doInHibernate(final Session session) throws HibernateException {
            	final Criteria attachedCrit = onmsCrit.getDetachedCriteria().getExecutableCriteria(session);
                if (onmsCrit.getFirstResult() != null) attachedCrit.setFirstResult(onmsCrit.getFirstResult());
                if (onmsCrit.getMaxResults() != null) attachedCrit.setMaxResults(onmsCrit.getMaxResults());
                return (List<T>)attachedCrit.list();
            }
        };
        return hibernateTemplate.execute(callback);
    }
    
    /** {@inheritDoc} */
    public int countMatching(final OnmsCriteria onmsCrit) throws DataAccessException {
        final HibernateCallback<Integer> callback = new HibernateCallback<Integer>() {
            @Override
            public Integer doInHibernate(final Session session) throws HibernateException {
                final Criteria attachedCrit = onmsCrit.getDetachedCriteria().getExecutableCriteria(session).setProjection(Projections.rowCount());
                Long retval = (Long)attachedCrit.uniqueResult();
                attachedCrit.setProjection(null);
                attachedCrit.setResultTransformer(Criteria.ROOT_ENTITY);
                return retval.intValue();
            }
        };
        Integer retval = hibernateTemplate.execute(callback);
        return retval == null ? 0 : retval.intValue();
    }
    
    /**
     * <p>bulkDelete</p>
     *
     * @param hql a {@link java.lang.String} object.
     * @param values an array of {@link java.lang.Object} objects.
     * @return a int.
     * @throws org.springframework.dao.DataAccessException if any.
     */
    public int bulkDelete(final String hql, final Object[] values ) throws DataAccessException {
        return hibernateTemplate.bulkUpdate(hql, values);
    }
    
    /**
     * <p>get</p>
     *
     * @param id a K object.
     * @return a T object.
     * @throws org.springframework.dao.DataAccessException if any.
     */
    @Override
    public T get(final K id) throws DataAccessException {
        return m_entityClass.cast(hibernateTemplate.get(m_entityClass, id));
    }

    /**
     * <p>load</p>
     *
     * @param id a K object.
     * @return a T object.
     * @throws org.springframework.dao.DataAccessException if any.
     */
    @Override
    public T load(final K id) throws DataAccessException {
        return m_entityClass.cast(hibernateTemplate.load(m_entityClass, id));
    }

    /**
     * <p>save</p>
     *
     * @param entity a T object.
     * @throws org.springframework.dao.DataAccessException if any.
     */
    @Override
    public K save(final T entity) throws DataAccessException {
        try {
        		//hibernateTemplate.getSessionFactory().getCurrentSession().setFlushMode(FlushMode.AUTO);
            return (K)hibernateTemplate.save(entity);
        } catch (final DataAccessException e) {
           // logExtraSaveOrUpdateExceptionInformation(entity, e);
            throw e;
        }
    }

    /**
     * <p>saveOrUpdate</p>
     *
     * @param entity a T object.
     * @throws org.springframework.dao.DataAccessException if any.
     */
    @Override
    public void saveOrUpdate(final T entity) throws DataAccessException {
        try {
            hibernateTemplate.saveOrUpdate(entity);
        } catch (final DataAccessException e) {
          //  logExtraSaveOrUpdateExceptionInformation(entity, e);
            throw e;
        }
    }

    /**
     * <p>update</p>
     *
     * @param entity a T object.
     * @throws org.springframework.dao.DataAccessException if any.
     */
    @Override
    public void update(final T entity) throws DataAccessException {
        try {
            hibernateTemplate.update(entity);
        } catch (final DataAccessException e) {
           // logExtraSaveOrUpdateExceptionInformation(entity, e);
            // Rethrow the exception
            throw e;
        }
    }

    /**
     * <p>Parse the {@link DataAccessException} to see if special problems were
     * encountered while performing the query. See issue NMS-5029 for examples of
     * stack traces that can be thrown from these calls.</p>
     * {@see http://issues.opennms.org/browse/NMS-5029}
     */
    /*private void logExtraSaveOrUpdateExceptionInformation(final T entity, final DataAccessException e) {
        Throwable cause = e;
        while (cause.getCause() != null) {
            if (cause.getMessage() != null) {
                if (cause.getMessage().contains("duplicate key value violates unique constraint")) {
                    final ClassMetadata meta = getSessionFactory().getClassMetadata(m_entityClass);
                    LOG.warn("Duplicate key constraint violation, class: {}, key value: {}", m_entityClass.getName(), meta.getPropertyValue(entity, meta.getIdentifierPropertyName()));
                    break;
                } else if (cause.getMessage().contains("given object has a null identifier")) {
                    LOG.warn("Null identifier on object, class: {}: {}", m_entityClass.getName(), entity.toString());
                    break;
                }
            }
            cause = cause.getCause();
        }
    }*/
}

