package org.ovirt.engine.ui.uicommonweb.models.resources;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.QuotaUsagePerUser;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.GetUserVmsByUserIdAndGroupsParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.place.UserPortalApplicationPlaces;

@SuppressWarnings("unused")
public class ResourcesModel extends SearchableListModel {

    private static class ResourceComparator implements Comparator<VM>, Serializable {

        @Override
        public int compare(VM o1, VM o2) {
            String name1 = o1.getName() == null ? "" : o1.getName(); //$NON-NLS-1$
            String name2 = o2.getName() == null ? "" : o2.getName(); //$NON-NLS-1$
            return name1.compareTo(name2);
        }
    }

    private static final ResourceComparator COMPARATOR = new ResourceComparator();

    private EntityModel privateDefinedVMs;

    public EntityModel getDefinedVMs() {
        return privateDefinedVMs;
    }

    private void setDefinedVMs(EntityModel value) {
        privateDefinedVMs = value;
    }

    private EntityModel privateRunningVMs;

    public EntityModel getRunningVMs() {
        return privateRunningVMs;
    }

    private void setRunningVMs(EntityModel value) {
        privateRunningVMs = value;
    }

    private EntityModel privateRunningVMsPercentage;

    public EntityModel getRunningVMsPercentage() {
        return privateRunningVMsPercentage;
    }

    private void setRunningVMsPercentage(EntityModel value) {
        privateRunningVMsPercentage = value;
    }

    private EntityModel privateDefinedCPUs;

    public EntityModel getDefinedCPUs() {
        return privateDefinedCPUs;
    }

    private void setDefinedCPUs(EntityModel value) {
        privateDefinedCPUs = value;
    }

    private EntityModel privateUsedCPUs;

    public EntityModel getUsedCPUs() {
        return privateUsedCPUs;
    }

    private void setUsedCPUs(EntityModel value) {
        privateUsedCPUs = value;
    }

    private EntityModel privateUsedCPUsPercentage;

    public EntityModel getUsedCPUsPercentage() {
        return privateUsedCPUsPercentage;
    }

    private void setUsedCPUsPercentage(EntityModel value) {
        privateUsedCPUsPercentage = value;
    }

    private EntityModel privateDefinedMemory;

    public EntityModel getDefinedMemory() {
        return privateDefinedMemory;
    }

    private void setDefinedMemory(EntityModel value) {
        privateDefinedMemory = value;
    }

    private EntityModel privateUsedMemory;

    public EntityModel getUsedMemory() {
        return privateUsedMemory;
    }

    private void setUsedMemory(EntityModel value) {
        privateUsedMemory = value;
    }

    private EntityModel privateUsedMemoryPercentage;

    public EntityModel getUsedMemoryPercentage() {
        return privateUsedMemoryPercentage;
    }

    private void setUsedMemoryPercentage(EntityModel value) {
        privateUsedMemoryPercentage = value;
    }

    private EntityModel privateTotalDisksSize;

    public EntityModel getTotalDisksSize() {
        return privateTotalDisksSize;
    }

    private void setTotalDisksSize(EntityModel value) {
        privateTotalDisksSize = value;
    }

    private EntityModel privateNumOfSnapshots;

    public EntityModel getNumOfSnapshots() {
        return privateNumOfSnapshots;
    }

    private void setNumOfSnapshots(EntityModel value) {
        privateNumOfSnapshots = value;
    }

    private EntityModel privateTotalSnapshotsSize;

    public EntityModel getTotalSnapshotsSize() {
        return privateTotalSnapshotsSize;
    }

    private void setTotalSnapshotsSize(EntityModel value) {
        privateTotalSnapshotsSize = value;
    }

    private EntityModel privateUsedQuotaPercentage;

    public EntityModel getUsedQuotaPercentage() {
        return privateUsedQuotaPercentage;
    }

    private void setUsedQuotaPercentage(EntityModel value) {
        privateUsedQuotaPercentage = value;
    }

    public ResourcesModel() {
        setApplicationPlace(UserPortalApplicationPlaces.extendedResourceSideTabPlace);

        setDefinedVMs(new EntityModel());
        setRunningVMs(new EntityModel());
        setRunningVMsPercentage(new EntityModel());
        setDefinedCPUs(new EntityModel());
        setUsedCPUs(new EntityModel());
        setUsedCPUsPercentage(new EntityModel());
        setDefinedMemory(new EntityModel());
        setUsedMemory(new EntityModel());
        setUsedMemoryPercentage(new EntityModel());
        setTotalDisksSize(new EntityModel());
        setNumOfSnapshots(new EntityModel());
        setTotalSnapshotsSize(new EntityModel());
        setUsedQuotaPercentage(new EntityModel());
    }

    @Override
    protected void syncSearch() {
        super.syncSearch();

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object ReturnValue) {
                ResourcesModel resourcesModel = (ResourcesModel) model;
                final ArrayList<VM> list =
                        ((VdcQueryReturnValue) ReturnValue).getReturnValue();

                // Update calculated properties.
                int runningVMs = 0;
                int definedCPUs = 0;
                int usedCPUs = 0;
                int definedMemory = 0;
                int usedMemory = 0;
                long totalDisksSize = 0;
                long totalSnapshotsSize = 0;
                int numOfSnapshots = 0;

                for (VM vm : list) {
                    definedCPUs += vm.getNumOfCpus();
                    definedMemory += vm.getVmMemSizeMb();

                    if (vm.isRunning()) {
                        runningVMs++;
                        usedCPUs += vm.getNumOfCpus();
                        usedMemory += vm.getVmMemSizeMb();
                    }

                    if (vm.getDiskList() != null) {
                        for (DiskImage disk : vm.getDiskList()) {
                            totalDisksSize += disk.getSizeInGigabytes();
                            totalSnapshotsSize += (long) disk.getActualDiskWithSnapshotsSize();
                            numOfSnapshots += disk.getSnapshots().size();
                        }
                    }
                }

                getDefinedVMs().setEntity(list.size());
                getRunningVMs().setEntity(runningVMs);
                getRunningVMsPercentage().setEntity(list.isEmpty() ? 0 : runningVMs * 100 / list.size());
                getDefinedCPUs().setEntity(definedCPUs);
                getUsedCPUs().setEntity(usedCPUs);
                getUsedCPUsPercentage().setEntity(definedCPUs == 0 ? 0 : usedCPUs * 100 / definedCPUs);
                getDefinedMemory().setEntity(sizeParser(definedMemory));
                getUsedMemory().setEntity(sizeParser(usedMemory));
                getUsedMemoryPercentage().setEntity(definedMemory == 0 ? 0 : usedMemory * 100 / definedMemory);
                getTotalDisksSize().setEntity(totalDisksSize >= 1 ? totalDisksSize + "GB" : "<1GB"); //$NON-NLS-1$ //$NON-NLS-2$
                getTotalSnapshotsSize().setEntity(totalSnapshotsSize >= 1 ? totalSnapshotsSize + "GB" : "<1GB"); //$NON-NLS-1$ //$NON-NLS-2$
                getNumOfSnapshots().setEntity(numOfSnapshots);

                Collections.sort(list, COMPARATOR);
                resourcesModel.setItems(list);

                VdcQueryParametersBase parameters =
                        new VdcQueryParametersBase();
                parameters.setRefresh(getIsQueryFirstTime());
                Frontend.getInstance().runQuery(VdcQueryType.GetQuotasConsumptionForCurrentUser, parameters, new AsyncQuery(this,
                        new INewAsyncCallback() {
                            @Override
                            public void onSuccess(Object model, Object ReturnValue) {
                                Map<Guid, QuotaUsagePerUser> quotaPerUserUsageEntityMap =
                                        (HashMap<Guid, QuotaUsagePerUser>) ((VdcQueryReturnValue) ReturnValue).getReturnValue();

                                //calculate personal consumption
                                for (VM vm : list) {
                                    // if vm is running and have a quota
                                    if (vm.getStatus() != VMStatus.Down
                                            && vm.getStatus() != VMStatus.Suspended
                                            && vm.getStatus() != VMStatus.ImageIllegal
                                            && vm.getStatus() != VMStatus.ImageLocked
                                            && vm.getStatus() != VMStatus.PoweringDown
                                            && vm.getQuotaId() != null) {
                                        QuotaUsagePerUser quotaUsagePerUser =
                                                quotaPerUserUsageEntityMap.get(vm.getQuotaId());
                                        // add the vm cpu and mem to the user quota consumption
                                        if (quotaUsagePerUser != null) {
                                            quotaUsagePerUser.setMemoryUsageForUser(quotaUsagePerUser.getMemoryUsageForUser()
                                                    + vm.getMemSizeMb());
                                            quotaUsagePerUser.setVcpuUsageForUser(quotaUsagePerUser.getVcpuUsageForUser()
                                                    + vm.getCpuPerSocket() * vm.getNumOfSockets());
                                        }
                                    }
                                    // for each image of each disk of the vm - if it has a quota
                                    for (DiskImage image : vm.getDiskList()) {
                                        QuotaUsagePerUser quotaUsagePerUser =
                                                quotaPerUserUsageEntityMap.get(image.getQuotaId());
                                        double imageSize =
                                                image.getImage().isActive() ? image.getSizeInGigabytes()
                                                        : image.getActualSize();
                                        // add the disk size to the user storage consumption
                                        if (quotaUsagePerUser != null) {
                                            quotaUsagePerUser.setStorageUsageForUser(quotaUsagePerUser.getStorageUsageForUser()
                                                    + imageSize);
                                        }
                                    }
                                }
                                getUsedQuotaPercentage().setEntity(new ArrayList<>(quotaPerUserUsageEntityMap.values()));
                            }
                        }));
            }
        };

        // Items property will contain list of VMs.
        GetUserVmsByUserIdAndGroupsParameters getUserVmsByUserIdAndGroupsParameters =
                new GetUserVmsByUserIdAndGroupsParameters();
        getUserVmsByUserIdAndGroupsParameters.setIncludeDiskData(true);
        getUserVmsByUserIdAndGroupsParameters.setRefresh(getIsQueryFirstTime());
        Frontend.getInstance().runQuery(VdcQueryType.GetUserVmsByUserIdAndGroups, getUserVmsByUserIdAndGroupsParameters, _asyncQuery);
    }

    // Temporarily converter
    // TODO: Use converters infrastructure in UICommon
    public String sizeParser(long sizeInMb) {
        return sizeInMb >= 1024 && sizeInMb % 1024 == 0 ? sizeInMb / 1024 + "GB" : sizeInMb + "MB"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    protected String getListName() {
        return "ResourcesModel"; //$NON-NLS-1$
    }

}
