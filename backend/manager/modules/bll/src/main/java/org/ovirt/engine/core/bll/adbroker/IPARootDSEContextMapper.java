package org.ovirt.engine.core.bll.adbroker;

import static org.ovirt.engine.core.bll.adbroker.IPARootDSEAttributes.namingContexts;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;

public class IPARootDSEContextMapper implements ContextMapper {

    protected final static String[] ROOTDSE_ATTRIBUTE_FILTER = { namingContexts.name() };

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
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

}
