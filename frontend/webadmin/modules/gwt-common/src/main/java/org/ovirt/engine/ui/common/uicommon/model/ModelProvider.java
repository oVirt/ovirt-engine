package org.ovirt.engine.ui.common.uicommon.model;

import org.ovirt.engine.ui.uicommonweb.models.HasEntity;

/**
 * Provider of UiCommon model instances.
 *
 * @param <M>
 *            Model type.
 */
public interface ModelProvider<M extends HasEntity> {

    /**
     * Returns the model instance.
     */
    M getModel();

}
