package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.guide;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.GuideModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.AddDataCenterClusterPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.ClusterPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.FindMultiStoragePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.FindSingleStoragePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.StorageForceCreatePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.StoragePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmDiskAttachPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmDiskPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmInterfacePopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class GuidePopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<GuideModel<?>, GuidePopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<GuideModel<?>> {
    }

    private final Provider<ClusterPopupPresenterWidget> clusterPopupProvider;
    private final Provider<HostPopupPresenterWidget> hostPopupProvider;
    private final Provider<StoragePopupPresenterWidget> storagePopupProvider;
    private final Provider<VmInterfacePopupPresenterWidget> vmInterfacePopupProvider;
    private final Provider<VmDiskPopupPresenterWidget> vmDiskPopupPopupProvider;
    private final Provider<VmDiskAttachPopupPresenterWidget> vmDiskAttachPopupPopupProvider;
    private final Provider<MoveHostPopupPresenterWidget> moveHostPopupProvider;
    private final Provider<FindSingleStoragePopupPresenterWidget> singleStoragePopupProvider;
    private final Provider<FindMultiStoragePopupPresenterWidget> multiStoragePopupProvider;
    private final Provider<StorageForceCreatePopupPresenterWidget> forceCreateConfirmPopupProvider;
    private final Provider<AddDataCenterClusterPopupPresenterWidget> addDatacenterClusterPopupProvider;

    @Inject
    public GuidePopupPresenterWidget(EventBus eventBus, ViewDef view,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            Provider<StorageForceCreatePopupPresenterWidget> forceCreateConfirmPopupProvider,
            Provider<ClusterPopupPresenterWidget> clusterPopupProvider,
            Provider<HostPopupPresenterWidget> hostPopupProvider,
            Provider<StoragePopupPresenterWidget> storagePopupProvider,
            Provider<VmInterfacePopupPresenterWidget> vmInterfacePopupProvider,
            Provider<VmDiskPopupPresenterWidget> vmDiskPopupPopupProvider,
            Provider<VmDiskAttachPopupPresenterWidget> vmDiskAttachPopupPopupProvider,
            Provider<MoveHostPopupPresenterWidget> moveHostPopupProvider,
            Provider<FindSingleStoragePopupPresenterWidget> singleStoragePopupProvider,
            Provider<FindMultiStoragePopupPresenterWidget> multiStoragePopupProvider,
            Provider<AddDataCenterClusterPopupPresenterWidget> addDatacenterClusterPopupProvider) {
        super(eventBus, view, defaultConfirmPopupProvider);
        this.forceCreateConfirmPopupProvider = forceCreateConfirmPopupProvider;
        this.clusterPopupProvider = clusterPopupProvider;
        this.hostPopupProvider = hostPopupProvider;
        this.storagePopupProvider = storagePopupProvider;
        this.vmInterfacePopupProvider = vmInterfacePopupProvider;
        this.vmDiskPopupPopupProvider = vmDiskPopupPopupProvider;
        this.vmDiskAttachPopupPopupProvider = vmDiskAttachPopupPopupProvider;
        this.moveHostPopupProvider = moveHostPopupProvider;
        this.singleStoragePopupProvider = singleStoragePopupProvider;
        this.multiStoragePopupProvider = multiStoragePopupProvider;
        this.addDatacenterClusterPopupProvider = addDatacenterClusterPopupProvider;
    }

    @Override
    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(GuideModel<?> source,
            UICommand lastExecutedCommand, Model windowModel) {
        String lastExecutedCommandName = lastExecutedCommand.getName();

        if (lastExecutedCommandName.equals("AddCluster")) { //$NON-NLS-1$
            return clusterPopupProvider.get();
        } else if (lastExecutedCommandName.equals("AddHost")) { //$NON-NLS-1$
            return hostPopupProvider.get();
        } else if (lastExecutedCommandName.equals("SelectHost")) { //$NON-NLS-1$
            return moveHostPopupProvider.get();
        } else if (lastExecutedCommandName.equals("AddDataStorage")) { //$NON-NLS-1$
            return storagePopupProvider.get();
        } else if (lastExecutedCommandName.equals("AddIsoStorage")) { //$NON-NLS-1$
            return storagePopupProvider.get();
        } else if (lastExecutedCommandName.equals("AddLocalStorage")) { //$NON-NLS-1$
            return storagePopupProvider.get();
        } else if (lastExecutedCommandName.equals("AttachDataStorage")) { //$NON-NLS-1$
            return multiStoragePopupProvider.get();
        } else if (lastExecutedCommandName.equals("AttachIsoStorage")) { //$NON-NLS-1$
            return singleStoragePopupProvider.get();
        } else if (lastExecutedCommandName.equals("AddNetwork")) { //$NON-NLS-1$
            return vmInterfacePopupProvider.get();
        } else if (lastExecutedCommandName.equals("NewDisk")) { //$NON-NLS-1$
            return vmDiskPopupPopupProvider.get();
        } else if (lastExecutedCommandName.equals("AttachDisk")) { //$NON-NLS-1$
            return vmDiskAttachPopupPopupProvider.get();
        } else if (lastExecutedCommandName.equals("AddDataCenter")) { //$NON-NLS-1$
            return addDatacenterClusterPopupProvider.get();
        } else {
            return super.getModelPopup(source, lastExecutedCommand, windowModel);
        }
    }

    @Override
    public AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(GuideModel<?> source,
            UICommand lastExecutedCommand) {
        if (lastExecutedCommand.getName().equals("OnAddStorage")) { //$NON-NLS-1$
            return forceCreateConfirmPopupProvider.get();
        } else {
            return super.getConfirmModelPopup(source, lastExecutedCommand);
        }
    }

}
