package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import static org.ovirt.engine.extensions.aaa.builtin.kerberosldap.RHDSRootDSEAttributes.namingContexts;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;

public class RHDSRootDSEContextMapper implements ContextMapper {

    private static final Logger log = LoggerFactory.getLogger(RHDSRootDSEContextMapper.class);
    protected static final String RHDS_NAMING_CONTEXT = "o=netscaperoot";
    static final String[] ROOTDSE_ATTRIBUTE_FILTER = { namingContexts.name() };

    public static String getDefaultNamingContextFromNameingContexts(Attribute namingContexts) {
        for (int index = 0; index < namingContexts.size(); ++index) {
            String namingContext;
            try {
                namingContext = (String) namingContexts.get(index);
            } catch (NamingException e) {
                log.error("Failed getting naming contexts from root DSE", e);
                return null;
            }
            if (!RHDS_NAMING_CONTEXT.equalsIgnoreCase(namingContext)) {
                return namingContext;
            }
        }
        return null;
    }
    @Override
    public Object mapFromContext(Object ctx) {

        DirContextAdapter searchResult = (DirContextAdapter) ctx;
        Attributes attributes = searchResult.getAttributes();

        if (attributes == null) {
            return null;
        }

        Attribute att = attributes.get(namingContexts.name());

        if (att != null) {
            return getDefaultNamingContextFromNameingContexts(att);
        } else {
            return null;
        }
    }

}
