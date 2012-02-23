package org.ovirt.engine.core.dao;

import java.util.List;

public interface AutoRecoverDAO<T> {
    List<T> listFailedAutorecoverables();
}
