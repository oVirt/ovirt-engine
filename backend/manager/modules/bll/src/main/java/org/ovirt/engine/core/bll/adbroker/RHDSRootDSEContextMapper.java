package org.ovirt.engine.core.bll.adbroker;

import static org.ovirt.engine.core.bll.adbroker.RHDSRootDSEAttributes.namingContexts;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;

public class RHDSRootDSEContextMapper implements ContextMapper {

    private static Log log = LogFactory.getLog(RHDSRootDSEContextMapper.class);
    protected final static String RHDS_NAMING_CONTEXT = "o=netscaperoot";
    protected final static String[] ROOTDSE_ATTRIBUTE_FILTER = { namingContexts.name() };

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
