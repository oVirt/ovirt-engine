package org.ovirt.engine.ui.userportal.uicommon.model;

import org.ovirt.engine.ui.common.uicommon.model.ModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;

/**
 * Used to resolve UiCommon detail models using parent model providers.
 */
public class UserPortalModelResolver {

    @SuppressWarnings("unchecked")
    public <D extends EntityModel, M extends ListWithDetailsModel> D getDetailModel(
            Class<D> detailModelClass, ModelProvider<M> parentModelProvider) {
        M parentModel = parentModelProvider.getModel();

        // Resolve detail model by its class
        for (EntityModel detailModel : parentModel.getDetailModels()) {
            if (detailModel != null && detailModel.getClass().equals(detailModelClass)) {
                return (D) detailModel;
            }
        }

        throw new IllegalStateException("Cannot resolve detail model [" + detailModelClass + //$NON-NLS-1$
                "] from parent list model [" + parentModel.getClass() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
