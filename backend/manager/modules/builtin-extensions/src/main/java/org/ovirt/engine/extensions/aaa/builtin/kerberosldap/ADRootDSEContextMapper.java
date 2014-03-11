package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import static org.ovirt.engine.extensions.aaa.builtin.kerberosldap.ADRootDSEAttributes.defaultNamingContext;
import static org.ovirt.engine.extensions.aaa.builtin.kerberosldap.ADRootDSEAttributes.domainControllerFunctionality;
import static org.ovirt.engine.extensions.aaa.builtin.kerberosldap.ADRootDSEAttributes.domainFunctionality;

import javax.naming.directory.Attributes;

import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;

public class ADRootDSEContextMapper implements ContextMapper {

    static final String[] ROOTDSE_ATTRIBUTE_FILTER = { defaultNamingContext.name(), domainControllerFunctionality.name(), domainFunctionality.name() };

    @Override
    public Object mapFromContext(Object ctx) {

        DirContextAdapter searchResult = (DirContextAdapter) ctx;
        Attributes attributes = searchResult.getAttributes();

        return attributes;
    }
}
