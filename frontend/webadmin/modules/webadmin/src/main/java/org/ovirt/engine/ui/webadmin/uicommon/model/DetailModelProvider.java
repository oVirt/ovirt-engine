package org.ovirt.engine.ui.webadmin.uicommon.model;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;

/**
 * Provider of general (non-searchable) detail model instances.
 * <p>
 * Contains main model type information to distinguish detail models of the same type for different main models.
 * 
 * @param <M>
 *            Main model type.
 * @param <D>
 *            Detail model type.
 */
public interface DetailModelProvider<M extends ListWithDetailsModel, D extends EntityModel> extends ModelProvider<D> {

}
