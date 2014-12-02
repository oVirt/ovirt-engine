package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.vdscommands.RemoveVMVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.UpdateVMVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmStaticDAO;
import org.ovirt.engine.core.dao.VmTemplateDAO;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.utils.linq.Function;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.ovf.OvfManager;

public class OvfUpdateProcessHelper {
    private OvfManager ovfManager;

    public OvfUpdateProcessHelper() {
        ovfManager = new OvfManager();
    }

    /**
     * Adds the given vm metadata to the given map
     */
    protected String buildMetadataDictionaryForVm(VM vm,
                                                  Map<Guid, KeyValuePairCompat<String, List<Guid>>> metaDictionary,
                                                  ArrayList<DiskImage> allVmImages) {
        String vmMeta = generateVmMetadata(vm, allVmImages);
        metaDictionary.put(
                vm.getId(),
                new KeyValuePairCompat<String, List<Guid>>(vmMeta, LinqUtils.transformToList(vm.getDiskMap().values(),
                        new Function<Disk, Guid>() {
                            @Override
                            public Guid eval(Disk a) {
                                return a.getId();
                            }
                        })));
        return vmMeta;
    }

    protected String generateVmTemplateMetadata(VmTemplate template, List<DiskImage> allTemplateImages) {
        return ovfManager.ExportTemplate(template, allTemplateImages, ClusterUtils.getCompatibilityVersion(template));
    }

    /**
     * Adds the given template metadata to the given map
     */
    protected String buildMetadataDictionaryForTemplate(VmTemplate template,
                                                        Map<Guid, KeyValuePairCompat<String, List<Guid>>> metaDictionary) {
        List<DiskImage> allTemplateImages = template.getDiskList();
        String templateMeta = generateVmTemplateMetadata(template, allTemplateImages);
        metaDictionary.put(template.getId(), new KeyValuePairCompat<String, List<Guid>>(
                templateMeta, LinqUtils.transformToList(allTemplateImages, new Function<DiskImage, Guid>() {
            @Override
            public Guid eval(DiskImage diskImage) {
                return diskImage.getId();
            }
        })));
        return templateMeta;
    }

    /**
     * Loads additional need vm data for it's ovf
     */
    protected void loadVmData(VM vm) {
        setManagedVideoDevices(vm.getStaticData());
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

    private void setManagedVideoDevices(VmBase vmBase) {
        Map<Guid, VmDevice> managedDeviceMap = vmBase.getManagedDeviceMap();
        if (managedDeviceMap == null) {
            managedDeviceMap = new HashMap<Guid, VmDevice>();
        }
        List<VmDevice> devices =
                DbFacade.getInstance()
                        .getVmDeviceDao()
                        .getVmDeviceByVmIdAndType(vmBase.getId(), VmDeviceGeneralType.VIDEO);
        for (VmDevice device : devices) {
            managedDeviceMap.put(device.getDeviceId(), device);
        }
    }

    protected List<DiskImage> getAllImageSnapshots(DiskImage diskImage) {
        return ImagesHandler.getAllImageSnapshots(diskImage.getImageId());
    }


    protected ArrayList<DiskImage> getVmImagesFromDb(VM vm) {
        ArrayList<DiskImage> allVmImages = new ArrayList<>();
        List<DiskImage> filteredDisks = ImagesHandler.filterImageDisks(vm.getDiskList(), false, true, true);

        for (DiskImage diskImage : filteredDisks) {
            allVmImages.addAll(getAllImageSnapshots(diskImage));
        }
        return allVmImages;
    }

    /**
     * Loads additional need template data for it's ovf
     */
    protected void loadTemplateData(VmTemplate template) {
        setManagedVideoDevices(template);
        if (template.getInterfaces() == null || template.getInterfaces().isEmpty()) {
            template.setInterfaces(getVmNetworkInterfaceDao()
                    .getAllForTemplate(template.getId()));
        }
    }

    protected String generateVmMetadata(VM vm, ArrayList<DiskImage> AllVmImages) {
        return ovfManager.ExportVm(vm, AllVmImages, ClusterUtils.getCompatibilityVersion(vm));
    }

    protected VmDAO getVmDao() {
        return DbFacade.getInstance().getVmDao();
    }

    protected StorageDomainDAO getStorageDomainDao() {
        return DbFacade.getInstance().getStorageDomainDao();
    }


    protected VmTemplateDAO getVmTemplateDao() {
        return DbFacade.getInstance().getVmTemplateDao();
    }

    protected VmNetworkInterfaceDao getVmNetworkInterfaceDao() {
        return DbFacade.getInstance().getVmNetworkInterfaceDao();
    }

    protected VmStaticDAO getVmStaticDao() {
        return DbFacade.getInstance().getVmStaticDao();
    }

    protected SnapshotDao getSnapshotDao() {
        return DbFacade.getInstance().getSnapshotDao();
    }

    /**
     * Update the information contained in the given meta dictionary table in the given storage pool/storage domain.
     */
    protected boolean executeUpdateVmInSpmCommand(Guid storagePoolId,
                                                  Map<Guid, KeyValuePairCompat<String, List<Guid>>> metaDictionary,
                                                  Guid storageDomainId) {
        UpdateVMVDSCommandParameters tempVar = new UpdateVMVDSCommandParameters(storagePoolId, metaDictionary);
        tempVar.setStorageDomainId(storageDomainId);
        return Backend.getInstance().getResourceManager().RunVdsCommand(VDSCommandType.UpdateVM, tempVar)
                .getSucceeded();
    }

    /**
     * Removes the ovf of the vm/template with the given id from the given storage pool/storage domain.
     */
    protected boolean executeRemoveVmInSpm(Guid storagePoolId, Guid id, Guid storageDomainId) {
        return Backend.getInstance().getResourceManager().RunVdsCommand(VDSCommandType.RemoveVM,
                new RemoveVMVDSCommandParameters(storagePoolId, id, storageDomainId)).getSucceeded();
    }
}
