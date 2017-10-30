package org.ovirt.engine.core.bll.storage.ovfstore;

import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_ACTIVE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_SNAPABLE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.VmTemplateHandler;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.businessentities.storage.BaseDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.FullEntityOvfData;
import org.ovirt.engine.core.common.vdscommands.RemoveVMVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.UpdateVMVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.dao.DbUserDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.utils.ovf.OvfManager;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

@Singleton
public class OvfUpdateProcessHelper {
    @Inject
    private VmDeviceUtils vmDeviceUtils;

    @Inject
    private VmTemplateDao vmTemplateDao;

    @Inject
    private VmNetworkInterfaceDao vmNetworkInterfaceDao;

    @Inject
    private DiskVmElementDao diskVmElementDao;

    @Inject
    private DiskImageDao diskImageDao;

    @Inject
    private ResourceManager resourceManager;

    @Inject
    private OvfManager ovfManager;

    @Inject
    private ClusterUtils clusterUtils;

    @Inject
    private DbUserDao dbUserDao;

    @Inject
    private OvfHelper ovfHelper;
    /**
     * Adds the given vm metadata to the given map
     */
    public String buildMetadataDictionaryForVm(VM vm,
            Map<Guid, KeyValuePairCompat<String, List<Guid>>> metaDictionary,
            FullEntityOvfData fullEntityOvfData) {
        String vmMeta = generateVmMetadata(vm, fullEntityOvfData);
        metaDictionary.put(
                vm.getId(),
                new KeyValuePairCompat<>
                        (vmMeta, vm.getDiskMap().values().stream().map(BaseDisk::getId).collect(Collectors.toList())));
        return vmMeta;
    }

    protected String generateVmTemplateMetadata(FullEntityOvfData fullEntityOvfData) {
        return ovfManager.exportTemplate(fullEntityOvfData,
                clusterUtils.getCompatibilityVersion(fullEntityOvfData.getVmBase()));
    }

    /**
     * Adds the given template metadata to the given map
     */
    public String buildMetadataDictionaryForTemplate(VmTemplate template,
                                                        Map<Guid, KeyValuePairCompat<String, List<Guid>>> metaDictionary) {
        List<DiskImage> allTemplateImages = template.getDiskList();
        Set<DbUser> dbUsers = new HashSet<>(dbUserDao.getAllForTemplate(template.getId()));
        FullEntityOvfData fullEntityOvfData = new FullEntityOvfData(template);
        fullEntityOvfData.setDbUsers(dbUsers);
        fullEntityOvfData.setDiskImages(allTemplateImages);
        ovfHelper.populateUserToRoles(fullEntityOvfData, template.getId());
        String templateMeta = generateVmTemplateMetadata(fullEntityOvfData);
        metaDictionary.put(template.getId(), new KeyValuePairCompat<>(
                templateMeta, allTemplateImages.stream().map(BaseDisk::getId).collect(Collectors.toList())));
        return templateMeta;
    }

    /**
     * Loads additional need vm data for it's ovf
     */
    public void loadVmData(VM vm) {
        vmDeviceUtils.setVmDevices(vm.getStaticData());
        if (vm.getInterfaces().isEmpty()) {
            vm.setInterfaces(vmNetworkInterfaceDao.getAllForVm(vm.getId()));
        }
        if (StringUtils.isEmpty(vm.getVmtName())) {
            if (!Guid.Empty.equals(vm.getVmtGuid())) {
                VmTemplate t = vmTemplateDao.get(vm.getVmtGuid());
                vm.setVmtName(t.getName());
            } else {
                vm.setVmtName(VmTemplateHandler.BLANK_VM_TEMPLATE_NAME);
            }
        }
    }

    public ArrayList<DiskImage> getVmImagesFromDb(VM vm) {
        ArrayList<DiskImage> allVmImages = new ArrayList<>();
        List<DiskImage> filteredDisks = DisksFilter.filterImageDisks(vm.getDiskList(), ONLY_SNAPABLE, ONLY_ACTIVE);

        for (DiskImage diskImage : filteredDisks) {
            allVmImages.addAll(diskImageDao.getAllSnapshotsForLeaf(diskImage.getImageId()));
        }

        for (DiskImage disk : allVmImages) {
            DiskVmElement dve = diskVmElementDao.get(new VmDeviceId(disk.getId(), vm.getId()));
            disk.setDiskVmElements(Collections.singletonList(dve));
        }

        return allVmImages;
    }

    /**
     * Loads additional need template data for it's ovf
     */
    public void loadTemplateData(VmTemplate template) {
        vmDeviceUtils.setVmDevices(template);
        if (template.getInterfaces() == null || template.getInterfaces().isEmpty()) {
            template.setInterfaces(vmNetworkInterfaceDao.getAllForTemplate(template.getId()));
        }
    }

    protected String generateVmMetadata(VM vm, FullEntityOvfData fullEntityOvfData) {
        return ovfManager.exportVm(vm, fullEntityOvfData, clusterUtils.getCompatibilityVersion(vm));
    }

    /**
     * Update the information contained in the given meta dictionary table in the given storage pool/storage domain.
     */
    public boolean executeUpdateVmInSpmCommand(Guid storagePoolId,
                                                  Map<Guid, KeyValuePairCompat<String, List<Guid>>> metaDictionary,
                                                  Guid storageDomainId) {
        UpdateVMVDSCommandParameters tempVar = new UpdateVMVDSCommandParameters(storagePoolId, metaDictionary);
        tempVar.setStorageDomainId(storageDomainId);
        return resourceManager.runVdsCommand(VDSCommandType.UpdateVM, tempVar).getSucceeded();
    }

    /**
     * Removes the ovf of the vm/template with the given id from the given storage pool/storage domain.
     */
    protected boolean executeRemoveVmInSpm(Guid storagePoolId, Guid id, Guid storageDomainId) {
        return resourceManager.runVdsCommand(VDSCommandType.RemoveVM,
                new RemoveVMVDSCommandParameters(storagePoolId, id, storageDomainId)).getSucceeded();
    }
}
