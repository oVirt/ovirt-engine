package org.ovirt.engine.ui.common.uicommon.model;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

/**
 * Provider of UiCommon model instances.
 *
 * @param <M>
 *            Model type.
 */
public interface ModelProvider<M extends EntityModel> {

    /**
     * Returns the model instance.
     */
    M getModel();

}
