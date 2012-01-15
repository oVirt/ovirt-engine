package org.ovirt.engine.ui.webadmin.uicommon.model;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;

/**
 * Default {@link DetailModelProvider} implementation for use with tab controls.
 *
 * @param <M>
 *            Main model type.
 * @param <D>
 *            Detail model type.
 */
public class DetailTabModelProvider<M extends ListWithDetailsModel, D extends EntityModel> extends TabModelProvider<D> implements DetailModelProvider<M, D> {

    private final Class<M> mainModelClass;
    private final Class<D> detailModelClass;

    public DetailTabModelProvider(ClientGinjector ginjector,
            Class<M> mainModelClass, Class<D> detailModelClass) {
        super(ginjector);
        this.mainModelClass = mainModelClass;
        this.detailModelClass = detailModelClass;
    }

    @Override
    public D getModel() {
        return UiCommonModelResolver.getDetailListModel(getCommonModel(), mainModelClass, detailModelClass);
    }

    protected M getMainModel() {
        return UiCommonModelResolver.getMainListModel(getCommonModel(), mainModelClass);
    }

    @Override
    public void onSubTabSelected() {
        getMainModel().setActiveDetailModel(getModel());
    }

}
