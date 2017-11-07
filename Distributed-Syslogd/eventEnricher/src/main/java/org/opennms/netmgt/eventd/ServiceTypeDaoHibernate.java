package org.opennms.netmgt.eventd;

import java.util.List;

import org.opennms.core.criteria.Criteria;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.model.OnmsServiceType;
import org.springframework.stereotype.Service;

@Service
public class ServiceTypeDaoHibernate implements ServiceTypeDao {

    /**
     * <p>Constructor for ServiceTypeDaoHibernate.</p>
     */
    public ServiceTypeDaoHibernate() {
		super();
	}
    

    /** {@inheritDoc} */
    protected String getKey(OnmsServiceType serviceType) {
        return serviceType.getName();
    }



    /** {@inheritDoc} */
    @Override
    public OnmsServiceType findByName(final String name) {
        if (name == null) {
            return null;
        } else {
            return findByCacheKey("from OnmsServiceType as svcType where svcType.name = ?", name);
        }
    }


	private OnmsServiceType findByCacheKey(String string, String name) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public int countAll() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public int countMatching(Criteria arg0) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public void delete(OnmsServiceType arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void delete(Integer arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public List<OnmsServiceType> findAll() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public List<OnmsServiceType> findMatching(Criteria arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void flush() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public OnmsServiceType get(Integer arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void initialize(Object arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public OnmsServiceType load(Integer arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void lock() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public Integer save(OnmsServiceType arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void saveOrUpdate(OnmsServiceType arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void update(OnmsServiceType arg0) {
		// TODO Auto-generated method stub
		
	}
    
    
}

