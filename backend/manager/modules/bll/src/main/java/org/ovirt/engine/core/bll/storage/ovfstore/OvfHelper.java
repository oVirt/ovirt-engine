package org.ovirt.engine.core.bll.storage.ovfstore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.VmTemplateHandler;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.ovf.OvfManager;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;

/**
 * OvfHelper is a helper class that encapsulates the bll logic that needs
 * to be done on the ovf read data before using it in the bll project.
 */
public class OvfHelper {
    private OvfManager ovfManager;

    public OvfHelper() {
        ovfManager = new OvfManager();
    }

    /**
     * parses a given ovf to a vm, intialized with images and interfaces.
     * @return
     *        VM that represents the given ovf data
     */
    public VM readVmFromOvf(String ovf) throws OvfReaderException {
        VM vm = new VM();
        ArrayList<DiskImage> diskImages = new ArrayList<>();
        ArrayList<VmNetworkInterface> interfaces  = new ArrayList<>();
        ovfManager.importVm(ovf, vm, diskImages, interfaces);

        // add images
        vm.setImages(diskImages);
        // add interfaces
        vm.setInterfaces(interfaces);

        // add disk map
        Map<Guid, List<DiskImage>> images = ImagesHandler
                .getImagesLeaf(diskImages);
        for (Map.Entry<Guid, List<DiskImage>> entry : images.entrySet()) {
            List<DiskImage> list = entry.getValue();
            vm.getDiskMap().put(entry.getKey(), list.get(list.size() - 1));
        }
        return vm;
    }

    /**
     * parses a given ovf to a VmTemplate, initialized with images and interfaces.
     * @return
     *        VmTemplate that represents the given ovf data
     */
    public VmTemplate readVmTemplateFromOvf(String ovf) throws OvfReaderException {
        ArrayList<DiskImage> diskImages = new ArrayList<>();
        ArrayList<VmNetworkInterface> interfaces = new ArrayList<>();
        VmTemplate template = new VmTemplate();
        ovfManager.importTemplate(ovf, template, diskImages, interfaces);
        template.setInterfaces(interfaces);
        // add disk map
        for (DiskImage disk : diskImages) {
            template.getDiskTemplateMap().put(disk.getId(), disk);
        }
        return template;
    }

    public String generateOvfConfigurationForVm(VM vm) {
        if (VMStatus.ImageLocked != vm.getStatus()) {
            VmHandler.updateDisksFromDb(vm);
            DiskImagesValidator validator = new DiskImagesValidator(vm.getDiskList());
            if (validator.diskImagesNotLocked().isValid()) {
                loadVmData(vm);
                Long currentDbGeneration = getDbFacade().getVmStaticDao().getDbGeneration(vm.getId());
                // currentDbGeneration can be null in case that the vm was deleted during the run of OvfDataUpdater.
                if (currentDbGeneration != null && vm.getStaticData().getDbGeneration() == currentDbGeneration) {
                    return buildMetadataDictionaryForVm(vm);
                }
            }
        }

        return null;
    }

    /**
     * Adds the given vm metadata to the given map
     */
    private String buildMetadataDictionaryForVm(VM vm) {
        ArrayList<DiskImage> AllVmImages = new ArrayList<>();
        List<DiskImage> filteredDisks = ImagesHandler.filterImageDisks(vm.getDiskList(), false, true, true);

        for (DiskImage diskImage : filteredDisks) {
            List<DiskImage> images = ImagesHandler.getAllImageSnapshots(diskImage.getImageId());
            for (DiskImage disk : images) {
                disk.setDiskVmElements(Collections.singletonList(diskImage.getDiskVmElementForVm(vm.getId())));
            }
            AllVmImages.addAll(images);
        }

        return ovfManager.exportVm(vm, AllVmImages, ClusterUtils.getCompatibilityVersion(vm));
    }

    private void loadVmData(VM vm) {
        if (vm.getInterfaces().isEmpty()) {
            vm.setInterfaces(getDbFacade().getVmNetworkInterfaceDao().getAllForVm(vm.getId()));
        }
        if (StringUtils.isEmpty(vm.getVmtName())) {
            if (!Guid.Empty.equals(vm.getVmtGuid())) {
                VmTemplate t = getDbFacade().getVmTemplateDao().get(vm.getVmtGuid());
                vm.setVmtName(t.getName());
            } else {
                vm.setVmtName(VmTemplateHandler.BLANK_VM_TEMPLATE_NAME);
            }
        }
    }

    private DbFacade getDbFacade() {
        return DbFacade.getInstance();
    }

    public boolean isOvfTemplate(String ovfstring) throws OvfReaderException {
        return ovfManager.isOvfTemplate(ovfstring);
    }
}
