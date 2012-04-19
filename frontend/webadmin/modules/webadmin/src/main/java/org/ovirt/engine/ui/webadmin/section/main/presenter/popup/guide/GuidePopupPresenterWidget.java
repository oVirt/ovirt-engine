package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.guide;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.GuideModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.ClusterPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.FindMultiStoragePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.FindSingleStoragePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.StoragePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmDiskPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmInterfacePopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class GuidePopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<GuideModel, GuidePopupPresenterWidget.ViewDef> {
    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<GuideModel> {
    }

    private final Provider<ClusterPopupPresenterWidget> clusterPopupProvider;
    private final Provider<HostPopupPresenterWidget> hostPopupProvider;
    private final Provider<StoragePopupPresenterWidget> storagePopupProvider;
    private final Provider<VmInterfacePopupPresenterWidget> vmInterfacePopupProvider;
    private final Provider<VmDiskPopupPresenterWidget> vmDiskPopupPopupProvider;
    private final Provider<MoveHostPopupPresenterWidget> moveHostPopupProvider;
    private final Provider<FindSingleStoragePopupPresenterWidget> singleStoragePopupProvider;
    private final Provider<FindMultiStoragePopupPresenterWidget> multiStoragePopupProvider;

    @Inject
    public GuidePopupPresenterWidget(EventBus eventBus, ViewDef view,
            Provider<ClusterPopupPresenterWidget> clusterPopupProvider,
            Provider<HostPopupPresenterWidget> hostPopupProvider,
            Provider<StoragePopupPresenterWidget> storagePopupProvider,
            Provider<VmInterfacePopupPresenterWidget> vmInterfacePopupProvider,
            Provider<VmDiskPopupPresenterWidget> vmDiskPopupPopupProvider,
            Provider<MoveHostPopupPresenterWidget> moveHostPopupProvider,
            Provider<FindSingleStoragePopupPresenterWidget> singleStoragePopupProvider,
            Provider<FindMultiStoragePopupPresenterWidget> multiStoragePopupProvider) {
        super(eventBus, view);

        this.clusterPopupProvider = clusterPopupProvider;
        this.hostPopupProvider = hostPopupProvider;
        this.storagePopupProvider = storagePopupProvider;
        this.vmInterfacePopupProvider = vmInterfacePopupProvider;
        this.vmDiskPopupPopupProvider = vmDiskPopupPopupProvider;
        this.moveHostPopupProvider = moveHostPopupProvider;
        this.singleStoragePopupProvider = singleStoragePopupProvider;
        this.multiStoragePopupProvider = multiStoragePopupProvider;
    }

    @Override
    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(GuideModel source,
            UICommand lastExecutedCommand, Model windowModel) {
        String lastExecutedCommandName = lastExecutedCommand.getName();

        if (lastExecutedCommandName.equals("AddCluster")) {
            return clusterPopupProvider.get();
        } else if (lastExecutedCommandName.equals("AddHost")) {
            return hostPopupProvider.get();
        } else if (lastExecutedCommandName.equals("SelectHost")) {
            return moveHostPopupProvider.get();
        } else if (lastExecutedCommandName.equals("AddDataStorage")) {
            return storagePopupProvider.get();
        } else if (lastExecutedCommandName.equals("AddIsoStorage")) {
            return storagePopupProvider.get();
        } else if (lastExecutedCommandName.equals("AddLocalStorage")) {
            return storagePopupProvider.get();
        } else if (lastExecutedCommandName.equals("AttachDataStorage")) {
            return multiStoragePopupProvider.get();
        } else if (lastExecutedCommandName.equals("AttachIsoStorage")) {
            return singleStoragePopupProvider.get();
        } else if (lastExecutedCommandName.equals("AddNetwork")) {
            return vmInterfacePopupProvider.get();
        } else if (lastExecutedCommandName.equals("AddDisk")) {
            return vmDiskPopupPopupProvider.get();
        } else {
            return super.getModelPopup(source, lastExecutedCommand, windowModel);
        }
    }
}
