package org.ovirt.engine.ui.common.uicommon.model;

import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;

/**
 * Provider of general (including non-searchable) detail model instances.
 * <p>
 * Contains main model type information to distinguish detail models of the same type for different main models.
 *
 * @param <M>
 *            Main model type.
 * @param <D>
 *            Detail model type.
 */
public interface DetailModelProvider<M extends ListWithDetailsModel, D extends HasEntity> extends ModelProvider<D> {

    /**
     * Notifies main model that the corresponding sub tab has been selected.
     */
    void onSubTabSelected();

    /**
     * Notifies main model that the corresponding sub tab has been un-selected.
     */
    void onSubTabDeselected();

    /**
     * Activate the detail model provided by this model provider. This is useful for views that are bound to multiple
     * detail models.
     */
    void activateDetailModel();

    /**
     * Returns the main model associated with the detail model.
     */
    M getMainModel();

}
