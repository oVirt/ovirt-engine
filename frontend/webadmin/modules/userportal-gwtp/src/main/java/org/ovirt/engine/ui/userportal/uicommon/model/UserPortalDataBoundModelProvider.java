package org.ovirt.engine.ui.userportal.uicommon.model;

import java.util.List;

import org.ovirt.engine.ui.common.gin.BaseClientGinjector;
import org.ovirt.engine.ui.common.uicommon.model.DataBoundTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalModelInitEvent.UserPortalModelInitHandler;

public abstract class UserPortalDataBoundModelProvider<T, M extends SearchableListModel> extends DataBoundTabModelProvider<T, M>
        implements UserPortalModelInitHandler {

    public interface DataChangeListener<T> {

        void onDataChange(List<T> items);

    }

    private M model;
    private DataChangeListener<T> dataChangeListener;

    public UserPortalDataBoundModelProvider(BaseClientGinjector ginjector) {
        super(ginjector);
        ginjector.getEventBus().addHandler(UserPortalModelInitEvent.getType(), this);
    }

    @Override
    public M getModel() {
        return model;
    }

    @Override
    public void setSelectedItems(List<T> items) {
        getModel().setSelectedItem(items.size() > 0 ? items.get(0) : null);
        getModel().setSelectedItems(items);
    }

    @Override
    public void onUserPortalModelInit(UserPortalModelInitEvent event) {
        this.model = createModel();
    }

    protected abstract M createModel();

    public void setDataChangeListener(DataChangeListener<T> changeListener) {
        this.dataChangeListener = changeListener;
    }

    @Override
    protected void updateDataProvider(List<T> items) {
        super.updateDataProvider(items);

        if (dataChangeListener != null) {
            dataChangeListener.onDataChange(items);
        }
    }

}
