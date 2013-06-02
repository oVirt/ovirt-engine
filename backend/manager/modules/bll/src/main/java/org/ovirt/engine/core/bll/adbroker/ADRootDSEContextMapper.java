package org.ovirt.engine.core.bll.adbroker;

import static org.ovirt.engine.core.bll.adbroker.ADRootDSEAttributes.defaultNamingContext;
import static org.ovirt.engine.core.bll.adbroker.ADRootDSEAttributes.domainControllerFunctionality;
import static org.ovirt.engine.core.bll.adbroker.ADRootDSEAttributes.domainFunctionality;

import javax.naming.directory.Attributes;

import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;

public class ADRootDSEContextMapper implements ContextMapper {

    protected final static String[] ROOTDSE_ATTRIBUTE_FILTER = { defaultNamingContext.name(), domainControllerFunctionality.name(), domainFunctionality.name() };

    @Override
    public Object mapFromContext(Object ctx) {

        DirContextAdapter searchResult = (DirContextAdapter) ctx;
        Attributes attributes = searchResult.getAttributes();

        return attributes;
    }
}
