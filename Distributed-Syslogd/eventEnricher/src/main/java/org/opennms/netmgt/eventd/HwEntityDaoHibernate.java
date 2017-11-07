package org.opennms.netmgt.eventd;

import org.opennms.netmgt.dao.api.HwEntityDao;
import org.opennms.netmgt.model.OnmsHwEntity;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.stereotype.Service;

/**
 * The Class HwEntityDaoHibernate.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@Service
public class HwEntityDaoHibernate extends AbstractDaoHibernate<OnmsHwEntity, Integer> implements HwEntityDao {

    /**
     * The Constructor.
     */
    public HwEntityDaoHibernate() {
        super(OnmsHwEntity.class);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.api.HwEntityDao#findRootByNodeId(java.lang.Integer)
     */
    @Override
    public OnmsHwEntity findRootByNodeId(Integer nodeId) {
        return (OnmsHwEntity) findUnique("from OnmsHwEntity e where e.parent is null and e.node.id = ?", nodeId);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.api.HwEntityDao#findEntityByIndex(java.lang.Integer, java.lang.Integer)
     */
    @Override
    public OnmsHwEntity findEntityByIndex(Integer nodeId, Integer entPhysicalIndex) {
        return (OnmsHwEntity) findUnique("from OnmsHwEntity e where e.node.id = ? and e.entPhysicalIndex = ?", nodeId, entPhysicalIndex);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.api.HwEntityDao#findEntityByName(java.lang.Integer, java.lang.String)
     */
    @Override
    public OnmsHwEntity findEntityByName(Integer nodeId, String entPhysicalName) {
        return (OnmsHwEntity) findUnique("from OnmsHwEntity e where e.node.id = ? and e.entPhysicalName = ?", nodeId, entPhysicalName);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.api.HwEntityDao#getAttributeValue(java.lang.Integer, java.lang.Integer, java.lang.String)
     */
    @Override
    public String getAttributeValue(Integer nodeId, Integer entPhysicalIndex, String attributeName) {
        OnmsHwEntity e = findEntityByIndex(nodeId, entPhysicalIndex);
        return e == null ? null : getAttributeValue(e, attributeName);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.api.HwEntityDao#getAttributeValue(java.lang.Integer, java.lang.String, java.lang.String)
     */
    @Override
    public String getAttributeValue(Integer nodeId, String nameSource, String attributeName) {
        boolean isRegex = nameSource.startsWith("~");
        if (isRegex) {
            OnmsHwEntity r = findRootByNodeId(nodeId);
            return r == null ? null : findAttribute(r, nameSource.substring(1), attributeName);
        }
        OnmsHwEntity e = findEntityByName(nodeId, nameSource);
        return e == null ? null : getAttributeValue(e, attributeName);
    }

    /**
     * Find attribute.
     *
     * @param entity the entity
     * @param nameRegex the regular expression for entPhysicalName
     * @param attributeName the attribute name
     * @return the string
     */
    private String findAttribute(OnmsHwEntity entity, String nameRegex, String attributeName) {
        if (entity.getEntPhysicalName().matches(nameRegex)) {
            return getAttributeValue(entity, attributeName);
        }
        for (OnmsHwEntity c : entity.getChildren()) {
            String v = findAttribute(c, nameRegex, attributeName);
            if (v != null) {
                return v;
            }
        }
        return null;
    }

    /**
     * Gets the attribute value.
     *
     * @param entity the entity
     * @param attributeName the attribute name
     * @return the attribute value
     */
    private String getAttributeValue(OnmsHwEntity entity, String attributeName) {
        if (attributeName.startsWith("entPhysical")) {
            BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(entity);
            if (wrapper.isWritableProperty(attributeName)) {
                return (String) wrapper.getPropertyValue(attributeName);
            }
        } else {
            return entity.getAttributeValue(attributeName);
        }
        return null;
    }
}

