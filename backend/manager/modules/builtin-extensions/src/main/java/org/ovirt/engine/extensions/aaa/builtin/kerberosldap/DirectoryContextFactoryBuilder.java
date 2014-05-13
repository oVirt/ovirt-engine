package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.util.Hashtable;

import javax.naming.NamingException;
import javax.naming.spi.ObjectFactory;
import javax.naming.spi.ObjectFactoryBuilder;

import org.springframework.ldap.core.support.DefaultDirObjectFactory;

public class DirectoryContextFactoryBuilder implements ObjectFactoryBuilder {

    @Override
    public ObjectFactory createObjectFactory(Object obj, Hashtable<?, ?> environment) throws NamingException {
        return new DefaultDirObjectFactory();
    }

}
