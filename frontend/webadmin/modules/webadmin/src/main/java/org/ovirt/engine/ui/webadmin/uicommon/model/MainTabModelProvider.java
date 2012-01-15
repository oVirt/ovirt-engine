package org.ovirt.engine.ui.webadmin.uicommon.model;

import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;

/**
 * Default {@link MainModelProvider} implementation for use with tab controls.
 *
 * @param <T>
 *            List model item type.
 * @param <M>
 *            List model type.
 */
public class MainTabModelProvider<T, M extends SearchableListModel> extends SearchableTabModelProvider<T, M> implements MainModelProvider<T, M> {

    private final Class<M> mainModelClass;

    public MainTabModelProvider(ClientGinjector ginjector, Class<M> mainModelClass) {
        super(ginjector);
        this.mainModelClass = mainModelClass;
    }

    @Override
    public M getModel() {
        return UiCommonModelResolver.getMainListModel(getCommonModel(), mainModelClass);
    }

    @Override
    public void onMainTabSelected() {
        getCommonModel().setSelectedItem(getModel());
    }

}
