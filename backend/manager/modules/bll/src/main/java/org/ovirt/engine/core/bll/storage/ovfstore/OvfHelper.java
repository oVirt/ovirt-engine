package org.ovirt.engine.core.bll.storage.ovfstore;

import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_ACTIVE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_SNAPABLE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.VmTemplateHandler;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.FullEntityOvfData;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DbUserDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.LabelDao;
import org.ovirt.engine.core.dao.PermissionDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;
import org.ovirt.engine.core.utils.ovf.OvfManager;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;

/**
 * OvfHelper is a helper class that encapsulates the bll logic that needs
 * to be done on the ovf read data before using it in the bll project.
 */
@Singleton
public class OvfHelper {

    @Inject
    private VmNetworkInterfaceDao vmNetworkInterfaceDao;

    @Inject
    private VmTemplateDao vmTemplateDao;

    @Inject
    private VmStaticDao vmStaticDao;

    @Inject
    private DiskImageDao diskImageDao;

    @Inject
    private VmHandler vmHandler;

    @Inject
    private OvfManager ovfManager;

    @Inject
    private ClusterUtils clusterUtils;

    @Inject
    private AffinityGroupDao affinityGroupDao;

    @Inject
    private DbUserDao dbUserDao;

    @Inject
    private PermissionDao permissionDao;

    @Inject
    private LabelDao labelDao;

    /**
     * parses a given ovf to a vm, initialize all the extra data related to it such as images, interfaces, cluster,
     * LUNS, etc..
     *
     * @return FullEntityOvfData that represents the given ovf data
     */
    public FullEntityOvfData readVmFromOvf(String ovf) throws OvfReaderException {
        VM vm = new VM();
        FullEntityOvfData fullEntityOvfData = new FullEntityOvfData(vm);
        ovfManager.importVm(ovf, vm, fullEntityOvfData);

        // add images
        vm.setImages((ArrayList) fullEntityOvfData.getDiskImages());
        // add interfaces
        vm.setInterfaces(fullEntityOvfData.getInterfaces());

        // add disk map
        Map<Guid, List<DiskImage>> images = ImagesHandler
                .getImagesLeaf(fullEntityOvfData.getDiskImages());
        for (Map.Entry<Guid, List<DiskImage>> entry : images.entrySet()) {
            List<DiskImage> list = entry.getValue();
            vm.getDiskMap().put(entry.getKey(), list.get(list.size() - 1));
        }
        fullEntityOvfData.getLunDisks().forEach(lunDisk -> vm.getDiskMap().put(lunDisk.getId(), lunDisk));
        return fullEntityOvfData;
    }

    /**
     * parses a given ovf to a vm, intialized with images and interfaces.
     * unlike {@link #readVmFromOvf(String)}, the given ovf is taken from ova
     * and thus closer to the OVF specification.
     * @return
     *        Pair of VM that represents the given ovf data and a Map that
     *        Maps disk UUID to a pair of (filename, actual size) as they are within the OVA
     */
    public VM readVmFromOva(String ovf) throws OvfReaderException {
        ovf = format(ovf);
        VM vm = new VM();
        FullEntityOvfData fullEntityOvfData = new FullEntityOvfData(vm);
        ovfManager.importVmFromOva(ovf, vm, fullEntityOvfData);

        // add images
        vm.setImages((ArrayList) fullEntityOvfData.getDiskImages());
        // add interfaces
        vm.setInterfaces(fullEntityOvfData.getInterfaces());

        // add disk map
        fullEntityOvfData.getDiskImages().forEach(image -> vm.getDiskMap().put(image.getId(), image));
        return vm;
    }

    /**
     * parses a given ovf to a VmTemplate, initialized with images and interfaces.
     * @return
     *        VmTemplate that represents the given ovf data
     */
    public VmTemplate readVmTemplateFromOva(String ovf) throws OvfReaderException {
        ovf = format(ovf);
        VmTemplate template = new VmTemplate();
        FullEntityOvfData fullEntityOvfData = new FullEntityOvfData(template);
        ovfManager.importTemplateFromOva(ovf, fullEntityOvfData);
        template.setInterfaces(fullEntityOvfData.getInterfaces());
        // add disk map
        fullEntityOvfData.getDiskImages().forEach(disk -> template.getDiskTemplateMap().put(disk.getId(), disk));
        return template;
    }

    private String format(String ovf) {
        return ovf
                .replaceAll("[\r\n]+", "") // remove new lines
                .replaceAll("xmlns=[^-\\s]*", ""); // remove global namespace
    }

    /**
     * parses a given ovf to a VmTemplate, initialized with images and interfaces.
     * @return
     *        VmTemplate that represents the given ovf data
     */
    public FullEntityOvfData readVmTemplateFromOvf(String ovf) throws OvfReaderException {
        VmTemplate template = new VmTemplate();
        FullEntityOvfData fullEntityOvfData = new FullEntityOvfData(template);
        ovfManager.importTemplate(ovf, fullEntityOvfData);
        template.setInterfaces(fullEntityOvfData.getInterfaces());
        // add disk map
        fullEntityOvfData.getDiskImages().forEach(disk -> template.getDiskTemplateMap().put(disk.getId(), disk));
        return fullEntityOvfData;
    }

    public String generateOvfConfigurationForVm(VM vm, boolean asOva) {
        if (VMStatus.ImageLocked != vm.getStatus()) {
            vmHandler.updateDisksFromDb(vm);
            DiskImagesValidator validator = new DiskImagesValidator(vm.getDiskList());
            if (validator.diskImagesNotLocked().isValid()) {
                loadVmData(vm);
                Long currentDbGeneration = vmStaticDao.getDbGeneration(vm.getId());
                // currentDbGeneration can be null in case that the vm was deleted during the run of OvfDataUpdater.
                if (currentDbGeneration != null && vm.getStaticData().getDbGeneration() == currentDbGeneration) {
                    return buildMetadataDictionaryForVm(vm, asOva);
                }
            }
        }

        return null;
    }

    /**
     * Adds the given vm metadata to the given map
     */
    private String buildMetadataDictionaryForVm(VM vm, boolean asOva) {
        List<DiskImage> allVmImages = new ArrayList<>();
        List<DiskImage> filteredDisks = DisksFilter.filterImageDisks(vm.getDiskList(), ONLY_SNAPABLE, ONLY_ACTIVE);
        List<LunDisk> lunDisks = DisksFilter.filterLunDisks(vm.getDiskMap().values());
        List<AffinityGroup> affinityGroups = affinityGroupDao.getAllAffinityGroupsByVmId(vm.getId());
        Set<DbUser> dbUsers = new HashSet<>(dbUserDao.getAllForVm(vm.getId()));
        List<Label> affinityLabels = labelDao.getAllByEntityIds(Collections.singletonList(vm.getId()));

        for (DiskImage diskImage : filteredDisks) {
            List<DiskImage> images = diskImageDao.getAllSnapshotsForLeaf(diskImage.getImageId());
            images.forEach(d -> d.setDiskVmElements(Collections.singletonList(diskImage.getDiskVmElementForVm(vm.getId()))));
            if (asOva) {
                images.forEach(d -> {
                    d.setActualSizeInBytes(-1);
                    d.setVolumeFormat(VolumeFormat.COW);
                    d.setVolumeType(VolumeType.Sparse);
                });
            }
            allVmImages.addAll(images);
        }

        FullEntityOvfData fullEntityOvfData = new FullEntityOvfData(vm);
        fullEntityOvfData.setDiskImages(allVmImages);
        fullEntityOvfData.setLunDisks(lunDisks);
        fullEntityOvfData.setAffinityGroups(affinityGroups);
        fullEntityOvfData.setAffinityLabels(affinityLabels);
        fullEntityOvfData.setDbUsers(dbUsers);
        populateUserToRoles(fullEntityOvfData, vm.getId());
        String ovf;
        if (asOva) {
            ovf = ovfManager.exportOva(vm, fullEntityOvfData, clusterUtils.getCompatibilityVersion(vm));
        } else {
            ovf = ovfManager.exportVm(vm, fullEntityOvfData, clusterUtils.getCompatibilityVersion(vm));
        }
        return ovf;
    }

    public void populateUserToRoles(FullEntityOvfData fullEntityOvfData, Guid entityId) {
        Map<String, Set<String>> userToRoles = new HashMap<>();
        fullEntityOvfData.getDbUsers().forEach(dbUser -> {
            Set<String> roles = new HashSet<>();
            List<Permission> permissions = permissionDao.getAllForAdElementAndObjectId(dbUser.getId(), entityId);
            permissions.forEach(permission -> roles.add(permission.getRoleName()));
            userToRoles.put(dbUser.getLoginName(), roles);
        });

        fullEntityOvfData.setUserToRoles(userToRoles);
    }

    private void loadVmData(VM vm) {
        if (vm.getInterfaces().isEmpty()) {
            vm.setInterfaces(vmNetworkInterfaceDao.getAllForVm(vm.getId()));
        }
        if (StringUtils.isEmpty(vm.getVmtName())) {
            if (!Guid.Empty.equals(vm.getVmtGuid())) {
                VmTemplate template = vmTemplateDao.get(vm.getVmtGuid());
                vm.setVmtName(template.getName());
            } else {
                vm.setVmtName(VmTemplateHandler.BLANK_VM_TEMPLATE_NAME);
            }
        }
    }

    public boolean isOvfTemplate(String ovfstring) throws OvfReaderException {
        return ovfManager.isOvfTemplate(ovfstring);
    }
}
