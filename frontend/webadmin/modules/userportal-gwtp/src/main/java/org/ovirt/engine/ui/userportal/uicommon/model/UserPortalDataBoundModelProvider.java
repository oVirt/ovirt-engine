package org.ovirt.engine.ui.userportal.uicommon.model;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.auth.UserLoginChangeEvent;
import org.ovirt.engine.ui.common.auth.UserLoginChangeEvent.UserLoginChangeHandler;
import org.ovirt.engine.ui.common.gin.BaseClientGinjector;
import org.ovirt.engine.ui.common.uicommon.model.DataBoundTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalModelInitEvent.UserPortalModelInitHandler;

/**
 * A {@link DataBoundTabModelProvider} that creates the UiCommon model instance directly, instead of accessing this
 * instance through CommonModel.
 *
 * @param <T>
 *            List model item type.
 * @param <M>
 *            List model type.
 */
public abstract class UserPortalDataBoundModelProvider<T, M extends SearchableListModel> extends DataBoundTabModelProvider<T, M> implements UserPortalModelInitHandler, UserLoginChangeHandler {

    public interface DataChangeListener<T> {

        void onDataChange(List<T> items);

    }

    private M model;
    private DataChangeListener<T> dataChangeListener;

    private List<T> selectedItems;
    private final CurrentUser user;

    public UserPortalDataBoundModelProvider(BaseClientGinjector ginjector, CurrentUser user) {
        super(ginjector);
        this.user = user;
        getEventBus().addHandler(UserPortalModelInitEvent.getType(), this);
        getEventBus().addHandler(UserLoginChangeEvent.getType(), this);
    }

    @Override
    public Object getKey(T item) {
        if (item instanceof UserPortalItemModel) {
            UserPortalItemModel itemModel = (UserPortalItemModel) item;
            return itemModel.isPool() ? ((VmPool) itemModel.getEntity()).getVmPoolId()
                    : ((VM) itemModel.getEntity()).getId();
        }

        return super.getKey(item);
    }

    @Override
    public M getModel() {
        return model;
    }

    @Override
    public void onUserPortalModelInit(UserPortalModelInitEvent event) {
        this.model = createModel();
    }

    /**
     * Creates the model instance.
     */
    protected abstract M createModel();

    public void setDataChangeListener(DataChangeListener<T> changeListener) {
        this.dataChangeListener = changeListener;
    }

    @Override
    protected void updateDataProvider(List<T> items) {
        super.updateDataProvider(items);
        retainSelectedItems();

        if (dataChangeListener != null) {
            dataChangeListener.onDataChange(items);
        }
    }

    /**
     * Retains the item selection of the model.
     */
    protected void retainSelectedItems() {
        if (selectedItems != null) {
            super.setSelectedItems(selectedItems);
        }
    }

    @Override
    public void setSelectedItems(List<T> items) {
        super.setSelectedItems(items);

        // Remember current item selection
        if (rememberModelItemSelection()) {
            this.selectedItems = items;
        }
    }

    /**
     * @return {@code true} to remember item selection of the model and retain it upon data updates, {@code false}
     *         otherwise.
     */
    protected boolean rememberModelItemSelection() {
        return true;
    }

    @Override
    public void onUserLoginChange(UserLoginChangeEvent event) {
        if (!user.isLoggedIn()) {
            getModel().ensureAsyncSearchStopped();
        }
    }

}
