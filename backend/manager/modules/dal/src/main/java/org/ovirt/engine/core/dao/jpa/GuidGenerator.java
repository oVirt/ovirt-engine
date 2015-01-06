package org.ovirt.engine.core.dao.jpa;

import java.io.Serializable;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.ovirt.engine.core.compat.Guid;

public class GuidGenerator implements IdentifierGenerator {

    @Override
    public Serializable generate(SessionImplementor session, Object object) throws HibernateException {
        return Guid.newGuid();
    }

}
