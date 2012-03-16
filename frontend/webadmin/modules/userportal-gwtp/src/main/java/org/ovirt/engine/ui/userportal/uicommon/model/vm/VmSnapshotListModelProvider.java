package org.ovirt.engine.ui.userportal.uicommon.model.vm;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SnapshotModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmSnapshotListModel;
import org.ovirt.engine.ui.userportal.gin.ClientGinjector;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmSnapshotCreatePopupPresenterWidget;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalModelResolver;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalSearchableDetailModelProvider;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class VmSnapshotListModelProvider extends UserPortalSearchableDetailModelProvider<SnapshotModel, UserPortalListModel, VmSnapshotListModel> {

    private final Provider<VmSnapshotCreatePopupPresenterWidget> createPopupProvider;

    @Inject
    public VmSnapshotListModelProvider(ClientGinjector ginjector,
            UserPortalListProvider parentModelProvider,
            UserPortalModelResolver resolver,
            Provider<VmSnapshotCreatePopupPresenterWidget> createPopupProvider) {
        super(ginjector, parentModelProvider, VmSnapshotListModel.class, resolver);
        this.createPopupProvider = createPopupProvider;
    }

    @Override
    protected VmSnapshotListModel createModel() {
        return new VmSnapshotListModel();
    }

    @Override
    protected AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UICommand lastExecutedCommand) {
        if (lastExecutedCommand == getModel().getNewCommand()) {
            return createPopupProvider.get();
        } else {
            return super.getModelPopup(lastExecutedCommand);
        }
    }

}
