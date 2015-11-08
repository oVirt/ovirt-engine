package org.ovirt.engine.ui.userportal.uicommon.model.vm;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmDiskListModel;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmDiskAttachPopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmDiskPopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmDiskRemovePopupPresenterWidget;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalSearchableDetailModelProvider;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class VmDiskListModelProvider extends UserPortalSearchableDetailModelProvider<Disk, UserPortalListModel, VmDiskListModel> {

    private final Provider<VmDiskPopupPresenterWidget> diskPopupProvider;
    private final Provider<VmDiskRemovePopupPresenterWidget> removeConfirmPopupProvider;
    private Provider<VmDiskAttachPopupPresenterWidget> attachPopupProvider;

    @Inject
    public VmDiskListModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            CurrentUser user,
            Provider<VmDiskPopupPresenterWidget> diskPopupProvider,
            Provider<VmDiskRemovePopupPresenterWidget> removeConfirmPopupProvider,
            Provider<VmDiskAttachPopupPresenterWidget> attachPopupProvider) {
        super(eventBus, defaultConfirmPopupProvider, user);
        this.diskPopupProvider = diskPopupProvider;
        this.removeConfirmPopupProvider = removeConfirmPopupProvider;
        this.attachPopupProvider = attachPopupProvider;
    }

    @Override
    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(VmDiskListModel source,
            UICommand lastExecutedCommand, Model windowModel) {
        if (lastExecutedCommand == getModel().getNewCommand() || lastExecutedCommand == getModel().getEditCommand()) {
            return diskPopupProvider.get();
        } else if(lastExecutedCommand == getModel().getAttachCommand()) {
            return attachPopupProvider.get();
        }
        else {
            return super.getModelPopup(source, lastExecutedCommand, windowModel);
        }
    }

    @Override
    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(VmDiskListModel source,
            UICommand lastExecutedCommand) {
        if (lastExecutedCommand == getModel().getRemoveCommand()) {
            return removeConfirmPopupProvider.get();
        } else {
            return super.getConfirmModelPopup(source, lastExecutedCommand);
        }
    }

}
