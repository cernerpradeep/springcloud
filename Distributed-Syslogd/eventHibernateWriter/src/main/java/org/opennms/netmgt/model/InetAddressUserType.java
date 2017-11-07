package org.opennms.netmgt.model;

import java.io.Serializable;
import java.net.InetAddress;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.StringType;
import org.hibernate.usertype.UserType;
import org.opennms.core.utils.InetAddressComparator;
import org.opennms.core.utils.InetAddressUtils;

public class InetAddressUserType implements UserType {

    private static final int[] SQL_TYPES = new int[] { java.sql.Types.VARCHAR };

	/**
     * A public default constructor is required by Hibernate.
     */
    public InetAddressUserType() {}

    @Override
    public Object assemble(final Serializable cached, final Object owner) throws HibernateException {
        return deepCopy(cached);
    }

    /**
     * Since {@link java.net.InetAddress} is immutable, we just return the original
     * value without copying it.
     */
    @Override
    public Object deepCopy(final Object value) throws HibernateException {
        if (value == null) {
            return null;
        } else if (value instanceof InetAddress) {
            // InetAddress is immutable so return the value without copying it
            return value;
        } else {
            throw new IllegalArgumentException("Unexpected type that is mapped with " + this.getClass().getSimpleName() + ": " + value.getClass().getName());
        }
    }

    @Override
    public Serializable disassemble(final Object value) throws HibernateException {
        return (Serializable)deepCopy(value);
    }

    @Override
    public boolean equals(final Object x, final Object y) throws HibernateException {
        if (x == y) return true;
        if (x == null || y == null) return false;
        // It's probably more consistent if we use our own comparator here
        // return ((InetAddress)x).equals((InetAddress)y);
        return new InetAddressComparator().compare((InetAddress)x, (InetAddress)y) == 0;
    }

    @Override
    public int hashCode(final Object x) throws HibernateException {
        return x == null ? 0 : x.hashCode();
    }

    @Override
    public boolean isMutable() {
        return false;
    }

   // @Override
    /*public Object nullSafeGet(final ResultSet rs, final String[] names, final Object owner,SessionImplementor session) throws HibernateException, SQLException {
        return InetAddressUtils.addr((String)StringType.INSTANCE.nullSafeGet(rs, names[0], session));
    }*/

   // @Override
    /*public void nullSafeSet(final PreparedStatement st, final Object value, final int index) throws HibernateException, SQLException {
        if (value == null) {
            StringType.INSTANCE.nullSafeSet(st, null, index);
        } else if (value instanceof InetAddress){
            // Format the IP address into a uniform format
            StringType.INSTANCE.nullSafeSet(st, InetAddressUtils.str((InetAddress)value), index);
        } else if (value instanceof String){
            try {
                // Format the IP address into a uniform format
                StringType.INSTANCE.nullSafeSet(st, InetAddressUtils.normalize((String)value), index);
            } catch (final IllegalArgumentException e) {
                // If the argument is not a valid IP address, then just pass it as-is. This
                // can occur of the query is performing a LIKE query (ie. '192.168.%').
                //
                // TODO: Add more validation of this string
                //
                StringType.INSTANCE.nullSafeSet(st, (String)value, index);
            }
        }
    }*/

    @Override
    public Object replace(final Object original, final Object target, final Object owner) throws HibernateException {
        return original;
    }

    @Override
    public Class<InetAddress> returnedClass() {
        return InetAddress.class;
    }

    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }

	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner)
			throws HibernateException, SQLException {
		// TODO Auto-generated method stub
		 return InetAddressUtils.addr((String)StringType.INSTANCE.nullSafeGet(rs, names[0], session));
	}

	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session)
			throws HibernateException, SQLException {

        if (value == null) {
            StringType.INSTANCE.nullSafeSet(st, null, index, session);
        } else if (value instanceof InetAddress){
            // Format the IP address into a uniform format
            StringType.INSTANCE.nullSafeSet(st, InetAddressUtils.str((InetAddress)value), index, session);
        } else if (value instanceof String){
            try {
                // Format the IP address into a uniform format
                StringType.INSTANCE.nullSafeSet(st, InetAddressUtils.normalize((String)value), index, session);
            } catch (final IllegalArgumentException e) {
                // If the argument is not a valid IP address, then just pass it as-is. This
                // can occur of the query is performing a LIKE query (ie. '192.168.%').
                //
                // TODO: Add more validation of this string
                //
                StringType.INSTANCE.nullSafeSet(st, (String)value, index, session);
            }
        }
    
		
	}

}
