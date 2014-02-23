package org.ovirt.engine.core.aaa.nop;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.aaa.Directory;
import org.ovirt.engine.core.aaa.DirectoryGroup;
import org.ovirt.engine.core.aaa.DirectoryUser;
import org.ovirt.engine.core.common.utils.ExternalId;

public class NopDirectory extends Directory {
    /**
     *
     */
    private static final long serialVersionUID = 3719648746441818198L;

    /**
     * {@inheritDoc}
     */
    @Override
    public DirectoryUser findUser(String name) {
        ExternalId id = null;
        try {
            id = new ExternalId(name.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            return null;
        }
        return new DirectoryUser(getProfileName(), id, name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DirectoryUser findUser(ExternalId id) {
        String name = null;
        try {
            name = new String(id.getBytes(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
        return new DirectoryUser(getProfileName(), id, name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DirectoryUser> findUsers(List<ExternalId> ids) {
        List<DirectoryUser> users = new ArrayList<>(ids.size());
        for (ExternalId id : ids) {
            DirectoryUser user = findUser(id);
            if (user != null) {
                users.add(user);
            }
        }
        return users;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DirectoryGroup findGroup(String name) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DirectoryGroup findGroup(ExternalId id) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DirectoryUser> queryUsers(String query) {
        return Collections.emptyList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DirectoryGroup> queryGroups(String query) {
        return Collections.emptyList();
    }

    @Override
    public void init() {
    }
}
