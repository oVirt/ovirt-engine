package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import static org.ovirt.engine.extensions.aaa.builtin.kerberosldap.ITDSRootDSEAttributes.namingContexts;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;

public class ITDSRootDSEContextMapper implements ContextMapper {

    private static final Logger log = LoggerFactory.getLogger(ITDSRootDSEContextMapper.class);

    static final String[] ROOTDSE_ATTRIBUTE_FILTER = { namingContexts.name() };

    @Override
    public Object mapFromContext(Object ctx) {

        DirContextAdapter searchResult = (DirContextAdapter) ctx;
        Attributes attributes = searchResult.getAttributes();

        if (attributes == null) {
            return null;
        }

        Attribute att = attributes.get(namingContexts.name());

        if (att != null) {
            try {
                return (att.get(0));
            } catch (NamingException e) {
                log.error("Failed getting naming contexts from root DSE", e);
                return null;
            }
        } else {
            return null;
        }
    }

}
