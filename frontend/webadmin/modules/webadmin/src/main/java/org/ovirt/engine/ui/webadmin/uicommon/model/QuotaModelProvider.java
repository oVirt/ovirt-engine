package org.ovirt.engine.ui.webadmin.uicommon.model;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.TabModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.qouta.QuotaModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.qouta.EditQuotaClusterPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.qouta.EditQuotaStoragePopupPresenterWidget;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class QuotaModelProvider extends TabModelProvider<QuotaModel> {

    private Provider<EditQuotaClusterPopupPresenterWidget> clusterPopupProvider;
    private Provider<EditQuotaStoragePopupPresenterWidget> storagePopupProvider;

    @Inject
    public QuotaModelProvider(ClientGinjector ginjector,
            Provider<EditQuotaClusterPopupPresenterWidget> clusterPopupProvider,
            Provider<EditQuotaStoragePopupPresenterWidget> storagePopupProvider) {
        super(ginjector);
        this.clusterPopupProvider = clusterPopupProvider;
        this.storagePopupProvider = storagePopupProvider;
    }

    @Override
    protected AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UICommand lastExecutedCommand) {
        String lastExecutedCommandName = lastExecutedCommand.getName();
        if (lastExecutedCommandName == "EditQuotaCluster") {
            return clusterPopupProvider.get();
        }
        else if (lastExecutedCommandName == "EditQuotaStorage") {
            return storagePopupProvider.get();
        }
        return super.getModelPopup(lastExecutedCommand);
    }

    private QuotaModel model;

    @Override
    public QuotaModel getModel() {
        return model != null ? model : new QuotaModel();
    }

    public void setModel(QuotaModel model) {
        this.model = model;
        onCommonModelChange();
    }
}
