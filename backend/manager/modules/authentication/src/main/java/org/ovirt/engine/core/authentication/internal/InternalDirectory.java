package org.ovirt.engine.core.authentication.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.authentication.Directory;
import org.ovirt.engine.core.authentication.DirectoryGroup;
import org.ovirt.engine.core.authentication.DirectoryUser;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.ExternalId;

/**
 * This directory contains only the internal user as specified in the {@code AdminUser} configuration parameter.
 */
public class InternalDirectory implements Directory {
    /**
     *
     */
    private static final long serialVersionUID = 6614140186031169227L;

    /**
     * The name of the directory:
     */
    private String name;

    /**
     * The name of the admin user and of the internal domain come from the configuration of the engine.
     */
    private static final String ADMIN_NAME = Config.getValue(ConfigValues.AdminUser);

    /**
     * The identifier of the admin user of the internal directory is inserted in the database when it is created, we
     * need to use exactly the same here.
     */
    private static final ExternalId ADMIN_ID = new ExternalId(
        0xfd, 0xfc, 0x62, 0x7c, 0xd8, 0x75, 0x11, 0xe0, 0x90, 0xf0, 0x83, 0xdf, 0x13, 0x3b, 0x58, 0xcc
    );

    /**
     * The only user supported by this directory.
     */
    private DirectoryUser admin;

    /**
     * Create a new internal directory.
     *
     * @param name the name of the directory
     */
    public InternalDirectory(String name) {
        // Save the name of the domain:
        this.name = name;

        // Create the builtin user:
        admin = new DirectoryUser(this, ADMIN_ID, ADMIN_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DirectoryUser findUser(String name) {
        return ADMIN_NAME.equals(name)? admin: null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DirectoryUser findUser(ExternalId id) {
        return ADMIN_ID.equals(id)? admin: null;
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
        return Collections.singletonList(admin);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DirectoryGroup> queryGroups(String query) {
        return Collections.emptyList();
    }
}
