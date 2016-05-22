package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.action.ImportVmFromExternalProviderParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaListModel;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

import com.google.inject.Inject;

public class ImportVmFromExternalSourceModel extends ImportVmFromExternalProviderModel {

    private String url;
    private String username;
    private String password;
    private Guid proxyHostId;

    @Inject
    public ImportVmFromExternalSourceModel(VmImportGeneralModel vmImportGeneralModel,
            VmImportDiskListModel importDiskListModel,
            VmImportInterfaceListModel vmImportInterfaceListModel,
            ClusterListModel<Void> cluster,
            QuotaListModel clusterQuota) {
        super(vmImportGeneralModel, importDiskListModel, vmImportInterfaceListModel, cluster, clusterQuota);
    }

    void setUrl(String url) {
        this.url = url;
    }

    void setUsername(String username) {
        this.username = username;
    }


    void setPassword(String password) {
        this.password = password;
    }

    void setProxyHostId(Guid proxyHostId) {
        this.proxyHostId = proxyHostId;
    }

    @Override
    public void importVms(IFrontendMultipleActionAsyncCallback callback) {
        Frontend.getInstance().runMultipleAction(
                VdcActionType.ImportVmFromExternalProvider,
                buildImportVmFromExternalProviderParameters(),
                callback);
    }

    private List<VdcActionParametersBase> buildImportVmFromExternalProviderParameters() {
        List<VdcActionParametersBase> prms = new ArrayList<>();

        for (Object item : getItems()) {
            ImportVmData importVmData = (ImportVmData) item;
            VM vm = importVmData.getVm();

            ImportVmFromExternalProviderParameters prm = new ImportVmFromExternalProviderParameters(
                    vm,
                    getStorage().getSelectedItem().getId(),
                    getStoragePool().getId(),
                    getCluster().getSelectedItem().getId());
            prm.setUrl(url);
            prm.setUsername(username);
            prm.setPassword(password);
            prm.setProxyHostId(proxyHostId);
            prm.setVirtioIsoName(getIso().getIsChangable() ? getIso().getSelectedItem() : null);
            prm.setExternalName(importVmData.getName());

            if (getClusterQuota().getSelectedItem() != null &&
                    getClusterQuota().getIsAvailable()) {
                prm.setQuotaId(getClusterQuota().getSelectedItem().getId());
            }

            CpuProfile cpuProfile = getCpuProfiles().getSelectedItem();
            if (cpuProfile != null) {
                prm.setCpuProfileId(cpuProfile.getId());
            }

            prm.setForceOverride(true);
            prm.setCopyCollapse(importVmData.getCollapseSnapshots().getEntity());

            for (Map.Entry<Guid, Disk> entry : vm.getDiskMap().entrySet()) {
                DiskImage disk = (DiskImage) entry.getValue();
                ImportDiskData importDiskData = getDiskImportData(disk.getDiskAlias());
                disk.setVolumeType(getAllocation().getSelectedItem());
                // in kvm we just copy the image, in other modes such as vmware or xen we use
                // virt-v2v which converts the image format as well
                if (vm.getOrigin() != OriginType.KVM) {
                    disk.setVolumeFormat(AsyncDataProvider.getInstance().getDiskVolumeFormat(
                            disk.getVolumeType(),
                            getStorage().getSelectedItem().getStorageType()));
                }

                if (getDiskImportData(disk.getDiskAlias()).getSelectedQuota() != null) {
                    disk.setQuotaId(importDiskData.getSelectedQuota().getId());
                }
            }

            updateNetworkInterfacesForVm(vm);

            if (importVmData.isExistsInSystem() ||
                    importVmData.getClone().getEntity()) {
                prm.setImportAsNewEntity(true);
                prm.setCopyCollapse(true);
            }

            prms.add(prm);
        }

        return prms;
    }
}
