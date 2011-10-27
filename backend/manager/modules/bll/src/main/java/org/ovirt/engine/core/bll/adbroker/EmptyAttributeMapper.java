package org.ovirt.engine.core.bll.adbroker;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.AttributesMapper;

public class EmptyAttributeMapper implements AttributesMapper {

    public static final Boolean foundValue = Boolean.TRUE;

    public Object mapFromAttributes(Attributes searchResult) throws NamingException {
        return foundValue;
    }
}
