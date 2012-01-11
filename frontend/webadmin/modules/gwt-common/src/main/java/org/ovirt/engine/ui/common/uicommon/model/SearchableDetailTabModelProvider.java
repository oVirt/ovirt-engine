package org.ovirt.engine.ui.common.uicommon.model;

import org.ovirt.engine.ui.common.gin.BaseClientGinjector;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

/**
 * Default {@link SearchableDetailModelProvider} implementation for use with tab controls.
 *
 * @param <T>
 *            Detail model item type.
 * @param <M>
 *            Main model type.
 * @param <D>
 *            Detail model type.
 */
public class SearchableDetailTabModelProvider<T, M extends ListWithDetailsModel, D extends SearchableListModel> extends SearchableTabModelProvider<T, D> implements SearchableDetailModelProvider<T, M, D> {

    private final Class<M> mainModelClass;
    private final Class<D> detailModelClass;

    public SearchableDetailTabModelProvider(BaseClientGinjector ginjector,
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
