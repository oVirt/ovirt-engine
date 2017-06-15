package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.AbstractDiskModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.StorageForceCreatePopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class VmDiskPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<AbstractDiskModel, VmDiskPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<AbstractDiskModel> {
        boolean handleEnterKeyDisabled();
    }

    private Provider<StorageForceCreatePopupPresenterWidget> forceCreateConfirmPopupProvider;

    @Inject
    public VmDiskPopupPresenterWidget(EventBus eventBus, ViewDef view,
            Provider<StorageForceCreatePopupPresenterWidget> forceCreateConfirmPopupProvider) {
        super(eventBus, view);
        this.forceCreateConfirmPopupProvider = forceCreateConfirmPopupProvider;
    }

    @Override
    protected void handleEnterKey() {
        if (!getView().handleEnterKeyDisabled()) {
            super.handleEnterKey();
        }
    }

    @Override
    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(AbstractDiskModel source,
            UICommand lastExecutedCommand) {
        if (lastExecutedCommand.getName().equals("OnSave")) { //$NON-NLS-1$
            return forceCreateConfirmPopupProvider.get();
        } else {
            return super.getConfirmModelPopup(source, lastExecutedCommand);
        }
    }
}
