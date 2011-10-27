package org.ovirt.engine.core.bll.adbroker;

import java.util.Hashtable;

import javax.naming.directory.DirContext;
import org.springframework.ldap.core.support.AbstractContextSource;

public class GSSAPContextSource extends AbstractContextSource {

    @Override
    protected DirContext getDirContextInstance(Hashtable environment) throws javax.naming.NamingException {
        // TODO Auto-generated method stub
        return null;
    }

}
