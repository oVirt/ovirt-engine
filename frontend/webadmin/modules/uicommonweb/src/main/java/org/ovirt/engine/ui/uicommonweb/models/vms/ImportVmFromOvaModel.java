package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ImportVmFromOvaParameters;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaListModel;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

import com.google.inject.Inject;

public class ImportVmFromOvaModel extends ImportVmFromExternalProviderModel {

    protected String ovaPath;
    protected Guid hostId;
    protected Map<String, String> vmNameToOva;

    @Inject
    protected ImportVmFromOvaModel(VmImportGeneralModel vmImportGeneralModel,
            VmImportDiskListModel importDiskListModel,
            VmImportInterfaceListModel vmImportInterfaceListModel,
            ClusterListModel<Void> cluster,
            QuotaListModel clusterQuota) {
        super(vmImportGeneralModel, importDiskListModel, vmImportInterfaceListModel, cluster, clusterQuota);
    }

    @Override
    protected void setTargetArchitecture(List<VM> externalVms) {
        setTargetArchitecture(ArchitectureType.x86_64);
    }

    public void setIsoName(String ovaPath) {
        this.ovaPath = ovaPath;
    }

    public void setHostId(Guid hostId) {
        this.hostId = hostId;
    }

    public void setVmNameToOva(Map<String, String> vmNameToOva) {
        this.vmNameToOva = vmNameToOva;
    }

    @Override
    public void executeImport(IFrontendMultipleActionAsyncCallback callback) {
        Frontend.getInstance().runMultipleAction(
                ActionType.ImportVmFromOva,
                buildImportVmFromOvaParameters(),
                true,
                callback,
                null);
    }

    private List<ActionParametersBase> buildImportVmFromOvaParameters() {
        List<ActionParametersBase> prms = new ArrayList<>();
        for (Object item : getItems()) {
            ImportVmData importVmData = (ImportVmData) item;
            VM vm = importVmData.getVm();
            ImportVmFromOvaParameters prm = new ImportVmFromOvaParameters(
                    vm,
                    getStorage().getSelectedItem().getId(),
                    getStoragePool().getId(),
                    getCluster().getSelectedItem().getId());
            String ovaFilename = vmNameToOva.get(importVmData.getName());
            prm.setOvaPath(ovaFilename == null ? ovaPath : ovaPath + "/" + ovaFilename); //$NON-NLS-1$
            prm.setProxyHostId(hostId);
            prm.setVirtioIsoName(getIso().getIsChangable() && getIso().getSelectedItem() != null ?
                    getIso().getSelectedItem().getRepoImageId() : null);
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
                disk.setVolumeType(getVolumeType(importDiskData));
                disk.setVolumeFormat(AsyncDataProvider.getInstance()
                        .getDiskVolumeFormat(
                                disk.getVolumeType(),
                                getStorage().getSelectedItem().getStorageType()));
                if (getDiskImportData(disk.getDiskAlias()).getSelectedQuota() != null) {
                    disk.setQuotaId(importDiskData.getSelectedQuota().getId());
                }
            }

            updateNetworkInterfacesForVm(vm.getInterfaces());

            if (importVmData.isExistsInSystem() || importVmData.getClone().getEntity()) {
                prm.setImportAsNewEntity(true);
                prm.setCopyCollapse(true);
            }

            prms.add(prm);
        }
        return prms;
    }

    private VolumeType getVolumeType(ImportDiskData importDiskData) {
        return getAllocation().getSelectedItem() == null ? importDiskData.volumeType
                : getAllocation().getSelectedItem();
    }

}
