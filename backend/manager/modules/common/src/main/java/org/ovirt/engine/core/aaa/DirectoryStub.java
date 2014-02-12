package org.ovirt.engine.core.aaa;

import java.util.List;

import org.ovirt.engine.core.common.utils.ExternalId;

/**
 * This is a dummy directory used only in the UI. It only contains the name of the directory, as this is all what is
 * needed in the UI. All the methods, except {@link #getName()}, throw an exception when invoked.
 */
public class DirectoryStub implements Directory {
    /**
     * The name of the directory.
     */
    private String name;

    public DirectoryStub(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
    @Override
    public DirectoryUser findUser(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DirectoryUser findUser(ExternalId id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<DirectoryUser> findUsers(List<ExternalId> ids) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DirectoryGroup findGroup(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DirectoryGroup findGroup(ExternalId id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<DirectoryUser> queryUsers(String query) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<DirectoryGroup> queryGroups(String query) {
        throw new UnsupportedOperationException();
    }

}
