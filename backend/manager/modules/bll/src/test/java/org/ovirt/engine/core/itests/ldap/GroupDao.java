package org.ovirt.engine.core.itests.ldap;

import java.util.List;

public interface GroupDao {

    public void create(Group group);

    public void create(Group... groups);

    public void update(Group group);

    public void delete(Group group);

    public void delete(Group... groups);

    public List runFilter(String filter);

    public List runFilter(String baseDN, String filter);
}
