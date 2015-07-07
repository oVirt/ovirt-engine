package org.ovirt.engine.ui.userportal.uicommon.model;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.DataBoundTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * A {@link DataBoundTabModelProvider} that creates the UiCommon model instance directly, instead of accessing this
 * instance through CommonModel.
 *
 * @param <T>
 *            List model item type.
 * @param <M>
 *            List model type.
 */
public class UserPortalDataBoundModelProvider<T, M extends SearchableListModel> extends DataBoundTabModelProvider<T, M> {

    public interface DataChangeListener<T> {

        void onDataChange(List<T> items);

    }

    private DataChangeListener<T> dataChangeListener;

    private List<T> selectedItems;
    private final CurrentUser user;

    @Inject
    public UserPortalDataBoundModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            CurrentUser user) {
        super(eventBus, defaultConfirmPopupProvider);
        this.user = user;
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
}
