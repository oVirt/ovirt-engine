package org.ovirt.engine.ui.userportal.uicommon.model.vm;

import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.UserPortalVmSnapshotListModel;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmClonePopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmMakeTemplatePopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmSnapshotCreatePopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmSnapshotCustomPreviewPopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmSnapshotPreviewPopupPresenterWidget;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalSearchableDetailModelProvider;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class VmSnapshotListModelProvider extends UserPortalSearchableDetailModelProvider<Snapshot, UserPortalListModel, UserPortalVmSnapshotListModel> {

    private final Provider<VmSnapshotCreatePopupPresenterWidget> createPopupProvider;
    private final Provider<VmClonePopupPresenterWidget> cloneVmPopupProvider;
    private final Provider<VmSnapshotPreviewPopupPresenterWidget> previewPopupProvider;
    private final Provider<VmSnapshotCustomPreviewPopupPresenterWidget> customPreviewPopupProvider;
    private final Provider<VmMakeTemplatePopupPresenterWidget> cloneTemplatePopupProvider;

    @Inject
    public VmSnapshotListModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            CurrentUser user,
            Provider<VmSnapshotCreatePopupPresenterWidget> createPopupProvider,
            Provider<VmSnapshotPreviewPopupPresenterWidget> previewPopupProvider,
            Provider<VmSnapshotCustomPreviewPopupPresenterWidget> customPreviewPopupProvider,
            Provider<VmMakeTemplatePopupPresenterWidget> cloneTemplatePopupProvider,
            Provider<VmClonePopupPresenterWidget> cloneVmPopupProvider) {
        super(eventBus, defaultConfirmPopupProvider, user);
        this.createPopupProvider = createPopupProvider;
        this.cloneVmPopupProvider = cloneVmPopupProvider;
        this.previewPopupProvider = previewPopupProvider;
        this.customPreviewPopupProvider = customPreviewPopupProvider;
        this.cloneTemplatePopupProvider = cloneTemplatePopupProvider;
    }

    @Override
    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UserPortalVmSnapshotListModel source,
            UICommand lastExecutedCommand, Model windowModel) {
        if (lastExecutedCommand == getModel().getNewCommand()) {
            return createPopupProvider.get();
        } else if (lastExecutedCommand == getModel().getCloneVmCommand()) {
            return cloneVmPopupProvider.get();
        } else if (lastExecutedCommand == getModel().getPreviewCommand()) {
            return previewPopupProvider.get();
        } else if (lastExecutedCommand == getModel().getCustomPreviewCommand()) {
            return customPreviewPopupProvider.get();
        } else if (lastExecutedCommand == getModel().getCloneTemplateCommand()) {
            return cloneTemplatePopupProvider.get();
        } else {
            return super.getModelPopup(source, lastExecutedCommand, windowModel);
        }
    }

}
