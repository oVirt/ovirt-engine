package org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.ipa;

import static org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.kerberos.InstallerConstants.ERROR_PREFIX;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;

public class OpenLdapUserContextMapper implements ContextMapper {

    private static final Logger log = LoggerFactory.getLogger(OpenLdapUserContextMapper.class);

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

        try {
            return attributes.get("uid").get(0);
        } catch (NamingException e) {
            log.error(ERROR_PREFIX + "Failed getting user GUID");
            return null;
        }
    }

}
