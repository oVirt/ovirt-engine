package org.ovirt.engine.ui.common.presenter;

import org.ovirt.engine.ui.uicommonweb.models.Model;

public interface ModelBoundPresenterWidget<T extends Model> {
    void init(T model);
}
