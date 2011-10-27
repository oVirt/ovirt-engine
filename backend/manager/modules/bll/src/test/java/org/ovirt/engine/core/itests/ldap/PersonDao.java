package org.ovirt.engine.core.itests.ldap;

import java.util.List;

public interface PersonDao {

    public void create(Person person);

    public void create(Person... persons);

    public void update(Person person);

    public void delete(Person person);

    public void delete(Person... persons);

    public List runFilter(String filter);

    public List runFilter(String baseDN, String filter);
}
