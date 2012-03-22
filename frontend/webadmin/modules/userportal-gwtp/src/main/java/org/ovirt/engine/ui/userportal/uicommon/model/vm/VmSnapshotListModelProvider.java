package org.ovirt.engine.ui.userportal.uicommon.model.vm;

import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmSnapshotListModel;
import org.ovirt.engine.ui.userportal.gin.ClientGinjector;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmClonePopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmSnapshotCreatePopupPresenterWidget;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalModelResolver;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalSearchableDetailModelProvider;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class VmSnapshotListModelProvider extends UserPortalSearchableDetailModelProvider<Snapshot, UserPortalListModel, VmSnapshotListModel> {

    private final Provider<VmSnapshotCreatePopupPresenterWidget> createPopupProvider;
    private final Provider<VmClonePopupPresenterWidget> cloneVmPopupProvider;

    @Inject
    public VmSnapshotListModelProvider(ClientGinjector ginjector,
            UserPortalListProvider parentModelProvider,
            UserPortalModelResolver resolver,
            Provider<VmSnapshotCreatePopupPresenterWidget> createPopupProvider,
            Provider<VmClonePopupPresenterWidget> cloneVmPopupProvider) {
        super(ginjector, parentModelProvider, VmSnapshotListModel.class, resolver);
        this.createPopupProvider = createPopupProvider;
        this.cloneVmPopupProvider = cloneVmPopupProvider;
    }

    @Override
    protected VmSnapshotListModel createModel() {
        return new VmSnapshotListModel();
    }

    @Override
    protected AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UICommand lastExecutedCommand) {
        if (lastExecutedCommand == getModel().getNewCommand()) {
            return createPopupProvider.get();
        } else if (lastExecutedCommand == getModel().getCloneVmCommand()) {
            return cloneVmPopupProvider.get();
        } else {
            return super.getModelPopup(lastExecutedCommand);
        }
    }

}
