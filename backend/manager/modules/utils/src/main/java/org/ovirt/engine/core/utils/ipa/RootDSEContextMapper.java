package org.ovirt.engine.core.utils.ipa;

import javax.naming.directory.Attributes;

import org.ovirt.engine.core.ldap.RootDSEQueryInfo;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;

public class RootDSEContextMapper implements ContextMapper {

    @Override
    public Object mapFromContext(Object ctx) {
        if (ctx == null) {
            return null;
        }

        DirContextAdapter searchResult = (DirContextAdapter) ctx;
        Attributes attributes = searchResult.getAttributes();

        if (attributes == null) {
            return null;
        }

        // We return true if IPA, false otherwise
        if (attributes.get(RootDSEQueryInfo.DEFAULT_NAMING_CONTEXT_RESULT_ATTRIBUTE) != null) {
            return false;
        } else {
            return true;
        }
    }

}

