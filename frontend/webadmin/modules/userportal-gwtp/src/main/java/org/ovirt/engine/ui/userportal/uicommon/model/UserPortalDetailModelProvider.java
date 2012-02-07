package org.ovirt.engine.ui.userportal.uicommon.model;

import org.ovirt.engine.ui.common.gin.BaseClientGinjector;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.ModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.TabModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;

/**
 * A {@link DetailModelProvider} implementation that resolves a detail model by its class using parent model provider.
 *
 * @param <M>
 *            Parent model type.
 * @param <D>
 *            Detail model type.
 */
public class UserPortalDetailModelProvider<M extends ListWithDetailsModel, D extends EntityModel> extends TabModelProvider<D> implements DetailModelProvider<M, D> {

    private final ModelProvider<M> parentModelProvider;
    private final Class<D> detailModelClass;

    public UserPortalDetailModelProvider(BaseClientGinjector ginjector,
            ModelProvider<M> parentModelProvider, Class<D> detailModelClass) {
        super(ginjector);
        this.parentModelProvider = parentModelProvider;
        this.detailModelClass = detailModelClass;
    }

    @SuppressWarnings("unchecked")
    @Override
    public D getModel() {
        M parentModel = getParentModel();

        // Resolve detail model by its class
        for (EntityModel detailModel : parentModel.getDetailModels()) {
            if (detailModel != null && detailModel.getClass().equals(detailModelClass)) {
                return (D) detailModel;
            }
        }

        throw new IllegalStateException("Cannot resolve detail model [" + detailModelClass
                + "] from parent list model [" + parentModel.getClass() + "]");
    }

    protected M getParentModel() {
        return parentModelProvider.getModel();
    }

    @Override
    public void onSubTabSelected() {
        getParentModel().setActiveDetailModel(getModel());
    }

}
