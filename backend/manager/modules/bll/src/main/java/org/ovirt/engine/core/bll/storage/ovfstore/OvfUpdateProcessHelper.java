package org.ovirt.engine.core.bll.storage.ovfstore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.VmTemplateHandler;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.BaseDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.vdscommands.RemoveVMVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.UpdateVMVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.utils.ovf.OvfManager;

public class OvfUpdateProcessHelper {
    private OvfManager ovfManager;

    public OvfUpdateProcessHelper() {
        ovfManager = new OvfManager();
    }

    /**
     * Adds the given vm metadata to the given map
     */
    public String buildMetadataDictionaryForVm(VM vm,
                                                  Map<Guid, KeyValuePairCompat<String, List<Guid>>> metaDictionary,
                                                  ArrayList<DiskImage> allVmImages) {
        String vmMeta = generateVmMetadata(vm, allVmImages);
        metaDictionary.put(
                vm.getId(),
                new KeyValuePairCompat<>
                        (vmMeta, vm.getDiskMap().values().stream().map(BaseDisk::getId).collect(Collectors.toList())));
        return vmMeta;
    }

    protected String generateVmTemplateMetadata(VmTemplate template, List<DiskImage> allTemplateImages) {
        return ovfManager.exportTemplate(template, allTemplateImages, ClusterUtils.getCompatibilityVersion(template));
    }

    /**
     * Adds the given template metadata to the given map
     */
    public String buildMetadataDictionaryForTemplate(VmTemplate template,
                                                        Map<Guid, KeyValuePairCompat<String, List<Guid>>> metaDictionary) {
        List<DiskImage> allTemplateImages = template.getDiskList();
        String templateMeta = generateVmTemplateMetadata(template, allTemplateImages);
        metaDictionary.put(template.getId(), new KeyValuePairCompat<>(
                templateMeta, allTemplateImages.stream().map(BaseDisk::getId).collect(Collectors.toList())));
        return templateMeta;
    }

    /**
     * Loads additional need vm data for it's ovf
     */
    public void loadVmData(VM vm) {
        VmDeviceUtils.setVmDevices(vm.getStaticData());
        if (vm.getInterfaces().isEmpty()) {
            vm.setInterfaces(getVmNetworkInterfaceDao().getAllForVm(vm.getId()));
        }
        if (StringUtils.isEmpty(vm.getVmtName())) {
            if (!Guid.Empty.equals(vm.getVmtGuid())) {
                VmTemplate t = getVmTemplateDao().get(vm.getVmtGuid());
                vm.setVmtName(t.getName());
            } else {
                vm.setVmtName(VmTemplateHandler.BLANK_VM_TEMPLATE_NAME);
            }
        }
    }

    protected List<DiskImage> getAllImageSnapshots(DiskImage diskImage) {
        return ImagesHandler.getAllImageSnapshots(diskImage.getImageId());
    }


    public ArrayList<DiskImage> getVmImagesFromDb(VM vm) {
        ArrayList<DiskImage> allVmImages = new ArrayList<>();
        List<DiskImage> filteredDisks = ImagesHandler.filterImageDisks(vm.getDiskList(), false, true, true);

        for (DiskImage diskImage : filteredDisks) {
            allVmImages.addAll(getAllImageSnapshots(diskImage));
        }

        for (DiskImage disk : allVmImages) {
            DiskVmElement dve = DbFacade.getInstance().getDiskVmElementDao().get(new VmDeviceId(disk.getId(), vm.getId()));
            disk.setDiskVmElements(Collections.singletonList(dve));
        }

        return allVmImages;
    }

    /**
     * Loads additional need template data for it's ovf
     */
    public void loadTemplateData(VmTemplate template) {
        VmDeviceUtils.setVmDevices(template);
        if (template.getInterfaces() == null || template.getInterfaces().isEmpty()) {
            template.setInterfaces(getVmNetworkInterfaceDao()
                    .getAllForTemplate(template.getId()));
        }
    }

    protected String generateVmMetadata(VM vm, ArrayList<DiskImage> AllVmImages) {
        return ovfManager.exportVm(vm, AllVmImages, ClusterUtils.getCompatibilityVersion(vm));
    }

    protected VmDao getVmDao() {
        return DbFacade.getInstance().getVmDao();
    }

    protected StorageDomainDao getStorageDomainDao() {
        return DbFacade.getInstance().getStorageDomainDao();
    }


    protected VmTemplateDao getVmTemplateDao() {
        return DbFacade.getInstance().getVmTemplateDao();
    }

    protected VmNetworkInterfaceDao getVmNetworkInterfaceDao() {
        return DbFacade.getInstance().getVmNetworkInterfaceDao();
    }

    protected VmStaticDao getVmStaticDao() {
        return DbFacade.getInstance().getVmStaticDao();
    }

    protected SnapshotDao getSnapshotDao() {
        return DbFacade.getInstance().getSnapshotDao();
    }

    /**
     * Update the information contained in the given meta dictionary table in the given storage pool/storage domain.
     */
    public boolean executeUpdateVmInSpmCommand(Guid storagePoolId,
                                                  Map<Guid, KeyValuePairCompat<String, List<Guid>>> metaDictionary,
                                                  Guid storageDomainId) {
        UpdateVMVDSCommandParameters tempVar = new UpdateVMVDSCommandParameters(storagePoolId, metaDictionary);
        tempVar.setStorageDomainId(storageDomainId);
        return Backend.getInstance().getResourceManager().runVdsCommand(VDSCommandType.UpdateVM, tempVar)
                .getSucceeded();
    }

    /**
     * Removes the ovf of the vm/template with the given id from the given storage pool/storage domain.
     */
    protected boolean executeRemoveVmInSpm(Guid storagePoolId, Guid id, Guid storageDomainId) {
        return Backend.getInstance().getResourceManager().runVdsCommand(VDSCommandType.RemoveVM,
                new RemoveVMVDSCommandParameters(storagePoolId, id, storageDomainId)).getSucceeded();
    }
}
