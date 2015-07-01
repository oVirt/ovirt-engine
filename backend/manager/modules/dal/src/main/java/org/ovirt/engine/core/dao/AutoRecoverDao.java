package org.ovirt.engine.core.dao;

import java.util.List;

public interface AutoRecoverDao<T> {
    List<T> listFailedAutorecoverables();
}
