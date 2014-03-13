package org.ovirt.engine.extensions.aaa.builtin.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.ovirt.engine.core.aaa.Directory;
import org.ovirt.engine.core.aaa.DirectoryGroup;
import org.ovirt.engine.core.aaa.DirectoryUser;
import org.ovirt.engine.core.common.utils.ExternalId;

/**
 * This directory contains only the internal user as specified in the {@code AdminUser} configuration parameter.
 */
public class InternalDirectory extends Directory {
    /**
     *
     */
    private static final long serialVersionUID = 6614140186031169227L;

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
     * {@inheritDoc}
     */
    @Override
    public DirectoryUser findUser(String name) {
        return getAdminName().equals(name) ? admin : null;
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

    @Override
    public void init() {
        admin = new DirectoryUser(getName(), ADMIN_ID, getAdminName());
        admin.setAdmin(true);
        context.put(ExtensionProperties.AUTHOR, "The oVirt Project");
        context.put(ExtensionProperties.EXTENSION_NAME, "Internal Authorization (Built-in)");
        context.put(ExtensionProperties.LICENSE, "ASL 2.0");
        context.put(ExtensionProperties.HOME, "http://www.ovirt.org");
        context.put(ExtensionProperties.VERSION, "N/A");

    }

    private String getAdminName() {
        return ((Properties) context.get(ExtensionProperties.CONFIGURATION)).getProperty("config.authz.user.name");
    }
}
