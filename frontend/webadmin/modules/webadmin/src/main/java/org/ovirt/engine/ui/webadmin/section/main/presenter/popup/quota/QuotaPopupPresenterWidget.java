package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.quota;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class QuotaPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<QuotaModel, QuotaPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<QuotaModel> {
    }

    private Provider<EditQuotaClusterPopupPresenterWidget> clusterPopupProvider;
    private Provider<EditQuotaStoragePopupPresenterWidget> storagePopupProvider;

    @Inject
    public QuotaPopupPresenterWidget(EventBus eventBus,
            ViewDef view,
            Provider<EditQuotaClusterPopupPresenterWidget> clusterPopupProvider,
            Provider<EditQuotaStoragePopupPresenterWidget> storagePopupProvider) {
        super(eventBus, view);
        this.clusterPopupProvider = clusterPopupProvider;
        this.storagePopupProvider = storagePopupProvider;
    }

    @Override
    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(QuotaModel source,
            UICommand lastExecutedCommand, Model windowModel) {
        String lastExecutedCommandName = lastExecutedCommand.getName();

        if ("EditQuotaCluster".equals(lastExecutedCommandName)) { //$NON-NLS-1$
            return clusterPopupProvider.get();
        } else if ("EditQuotaStorage".equals(lastExecutedCommandName)) { //$NON-NLS-1$
            return storagePopupProvider.get();
        } else {
            return super.getModelPopup(source, lastExecutedCommand, windowModel);
        }
    }
}
