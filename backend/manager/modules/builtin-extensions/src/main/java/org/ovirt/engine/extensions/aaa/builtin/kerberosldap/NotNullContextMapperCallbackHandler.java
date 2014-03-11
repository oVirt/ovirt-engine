package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import javax.naming.NameClassPair;

import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.ContextMapperCallbackHandler;

public class NotNullContextMapperCallbackHandler extends ContextMapperCallbackHandler {

    public NotNullContextMapperCallbackHandler(ContextMapper mapper) {
        super(mapper);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void handleNameClassPair(NameClassPair nameClassPair) {
        Object mappingResult = getObjectFromNameClassPair(nameClassPair);
        if (mappingResult != null) {
            getList().add(mappingResult);
        }
    }

}
