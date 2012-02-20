package org.ovirt.engine.ui.userportal.uicommon.model;

import org.ovirt.engine.ui.common.uicommon.model.ModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;

/**
 * Used to resolve the detail models in context of user portal
 *
 * @param <M>
 *            Main model type.
 * @param <D>
 *            Detail model type.
 */
public class UserPortalModelResolver {

    @SuppressWarnings("unchecked")
    public <D, M extends ListWithDetailsModel> D getDetailListModel(Class<D> detailModelClass,
            ModelProvider<M> parentModelProvider) {
        M parentModel = parentModelProvider.getModel();

        // Resolve detail model by its class
        for (EntityModel detailModel : parentModel.getDetailModels()) {
            if (detailModel != null && detailModel.getClass().equals(detailModelClass)) {
                return (D) detailModel;
            }
        }

        throw new IllegalStateException("Cannot resolve detail model [" + detailModelClass +
                "] from parent list model [" + parentModel.getClass() + "]");
    }
}
