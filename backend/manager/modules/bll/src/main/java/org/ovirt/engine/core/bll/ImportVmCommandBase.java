package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.VmInterfaceManager;
import org.ovirt.engine.core.bll.network.vm.VnicProfileHelper;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ImportVmParameters;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.DiskImageDynamicDAO;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.UnregisteredOVFDataDAO;
import org.ovirt.engine.core.utils.ovf.OvfLogEventHandler;
import org.ovirt.engine.core.utils.ovf.VMStaticOvfLogHandler;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public abstract class ImportVmCommandBase<T extends ImportVmParameters> extends VmCommand<T> {

    protected Map<Guid, Guid> imageToDestinationDomainMap;
    protected final Map<Guid, DiskImage> newDiskIdForDisk = new HashMap<>();
    private Guid sourceDomainId = Guid.Empty;
    private StorageDomain sourceDomain;

    private final List<String> macsAdded = new ArrayList<>();
    private static VmStatic vmStaticForDefaultValues = new VmStatic();

    ImportVmCommandBase(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        init(parameters);
    }

    ImportVmCommandBase(Guid commandId) {
        super(commandId);
    }

    protected void init(T parameters) {
        setStoragePoolId(parameters.getStoragePoolId());
        imageToDestinationDomainMap = parameters.getImageToDestinationDomainMap();
    }

    /**
     * Cloning a new disk and all its volumes with a new generated id.
     * The disk will have the same parameters as disk.
     * Also adding the disk to newDiskGuidForDisk map, so we will be able to link between
     * the new cloned disk and the old disk id.
     *
     * @param diskImagesList
     *            - All the disk volumes
     * @param disk
     *            - The disk which is about to be cloned
     */
    protected void generateNewDiskId(List<DiskImage> diskImagesList, DiskImage disk) {
        Guid generatedGuid = generateNewDiskId(disk);
        for (DiskImage diskImage : diskImagesList) {
            diskImage.setId(generatedGuid);
        }
    }

    /**
     * Cloning a new disk with a new generated id, with the same parameters as <code>disk</code>. Also
     * adding the disk to <code>newDiskGuidForDisk</code> map, so we will be able to link between the new cloned disk
     * and the old disk id.
     *
     * @param disk
     *            - The disk which is about to be cloned
     */
    protected Guid generateNewDiskId(DiskImage disk) {
        Guid newGuidForDisk = Guid.newGuid();

        // Copy the disk so it will preserve the old disk id and image id.
        newDiskIdForDisk.put(newGuidForDisk, DiskImage.copyOf(disk));
        disk.setId(newGuidForDisk);
        disk.setImageId(Guid.newGuid());
        return newGuidForDisk;
    }

    /**
     * Updating managed device map of VM, with the new disk {@link Guid}.
     * The update of managedDeviceMap is based on the newDiskIdForDisk map,
     * so this method should be called only after newDiskIdForDisk is initialized.
     *
     * @param disk
     *            - The disk which is about to be cloned
     * @param managedDeviceMap
     *            - The managed device map contained in the VM.
     */
    protected void updateManagedDeviceMap(DiskImage disk, Map<Guid, VmDevice> managedDeviceMap) {
        Guid oldDiskId = newDiskIdForDisk.get(disk.getId()).getId();
        managedDeviceMap.put(disk.getId(), managedDeviceMap.get(oldDiskId));
        managedDeviceMap.remove(oldDiskId);
    }

    protected void ensureDomainMap(Collection<DiskImage> images, Guid defaultDomainId) {
        if (imageToDestinationDomainMap == null) {
            imageToDestinationDomainMap = new HashMap<Guid, Guid>();
        }
        if (imageToDestinationDomainMap.isEmpty() && images != null && defaultDomainId != null) {
            for (DiskImage image : images) {
                if (isImagesAlreadyOnTarget()) {
                    imageToDestinationDomainMap.put(image.getId(), image.getStorageIds().get(0));
                } else if (!Guid.Empty.equals(defaultDomainId)) {
                    imageToDestinationDomainMap.put(image.getId(), defaultDomainId);
                }
            }
        }
    }

    /**
     * Space Validations are done using data extracted from the disks. The disks in question in this command
     * don't have all the needed data, and in order not to contaminate the command's data structures, an alter
     * one is created specifically fo this validation - hence dummy.
     * @param disksList
     * @return
     */
    protected List<DiskImage> createDiskDummiesForSpaceValidations(List<DiskImage> disksList) {
        List<DiskImage> dummies = new ArrayList<>(disksList.size());
        for (DiskImage image : disksList) {
            Guid targetSdId = imageToDestinationDomainMap.get(image.getId());
            DiskImage dummy = ImagesHandler.createDiskImageWithExcessData(image, targetSdId);
            dummies.add(dummy);
        }
        return dummies;
    }

    protected void setImagesWithStoragePoolId(Guid storagePoolId, List<DiskImage> diskImages) {
        for (DiskImage diskImage : diskImages) {
            diskImage.setStoragePoolId(storagePoolId);
        }
    }

    protected StorageDomain getSourceDomain() {
        if (sourceDomain == null && !Guid.Empty.equals(sourceDomainId)) {
            sourceDomain = getStorageDomainDAO().getForStoragePool(sourceDomainId, getStoragePool().getId());
        }
        return sourceDomain;
    }

    protected void setSourceDomainId(Guid storageId) {
        sourceDomainId = storageId;
    }

    protected boolean isImagesAlreadyOnTarget() {
        return getParameters().isImagesExistOnTargetStorageDomain();
    }

    protected BaseDiskDao getBaseDiskDao() {
        return getDbFacade().getBaseDiskDao();
    }

    protected DiskImageDynamicDAO getDiskImageDynamicDAO() {
        return getDbFacade().getDiskImageDynamicDao();
    }

    protected SnapshotDao getSnapshotDao() {
        return getDbFacade().getSnapshotDao();
    }

    protected StorageDomain getStorageDomain(Guid domainId) {
        return getStorageDomainDAO().getForStoragePool(domainId, getStoragePool().getId());
    }

    protected UnregisteredOVFDataDAO getUnregisteredOVFDataDao() {
        return getDbFacade().getUnregisteredOVFDataDao();
    }

    @Override
    protected void executeCommand() {
        try {
            addVmToDb();
            processImages();
            VmHandler.addVmInitToDB(getVm().getStaticData());
        } catch (RuntimeException e) {
            getMacPool().freeMacs(macsAdded);
            throw e;
        }

        setSucceeded(true);
    }

    protected abstract void processImages();

    protected void addVmToDb() {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                addVmStatic();
                addVmDynamic();
                addVmInterfaces();
                addVmStatistics();
                getCompensationContext().stateChanged();
                return null;
            }
        });
    }

    protected void addVmStatic() {
        logImportEvents();
        getVm().getStaticData().setId(getVmId());
        getVm().getStaticData().setCreationDate(new Date());
        getVm().getStaticData().setVdsGroupId(getParameters().getVdsGroupId());
        getVm().getStaticData().setMinAllocatedMem(computeMinAllocatedMem());
        getVm().getStaticData().setQuotaId(getParameters().getQuotaId());

        if (getVm().getOriginalTemplateGuid() != null && !VmTemplateHandler.BLANK_VM_TEMPLATE_ID.equals(getVm().getOriginalTemplateGuid())) {
            // no need to check this for blank
            VmTemplate originalTemplate = getVmTemplateDAO().get(getVm().getOriginalTemplateGuid());
            if (originalTemplate != null) {
                // in case the original template name has been changed in the meantime
                getVm().getStaticData().setOriginalTemplateName(originalTemplate.getName());
            }
        }

        if (getParameters().getCopyCollapse()) {
            getVm().setVmtGuid(VmTemplateHandler.BLANK_VM_TEMPLATE_ID);
        }
        getVmStaticDAO().save(getVm().getStaticData());
        getCompensationContext().snapshotNewEntity(getVm().getStaticData());
    }

    private void logImportEvents() {
        // Some values at the OVF file are used for creating events at the GUI
        // for the sake of providing information on the content of the VM that
        // was exported,
        // but not setting it in the imported VM
        VmStatic vmStaticFromOvf = getVm().getStaticData();

        OvfLogEventHandler<VmStatic> handler = new VMStaticOvfLogHandler(vmStaticFromOvf);
        Map<String, String> aliasesValuesMap = handler.getAliasesValuesMap();

        for (Map.Entry<String, String> entry : aliasesValuesMap.entrySet()) {
            String fieldName = entry.getKey();
            String fieldValue = entry.getValue();
            logField(vmStaticFromOvf, fieldName, fieldValue);
        }

        handler.resetDefaults(vmStaticForDefaultValues);

    }

    private static void logField(VmStatic vmStaticFromOvf, String fieldName, String fieldValue) {
        String vmName = vmStaticFromOvf.getName();
        AuditLogableBase logable = new AuditLogableBase();
        logable.addCustomValue("FieldName", fieldName);
        logable.addCustomValue("VmName", vmName);
        logable.addCustomValue("FieldValue", fieldValue);
        AuditLogDirector.log(logable, AuditLogType.VM_IMPORT_INFO);
    }

    protected void addVmInterfaces() {
        VmInterfaceManager vmInterfaceManager = new VmInterfaceManager(getMacPool());

        VnicProfileHelper vnicProfileHelper =
                new VnicProfileHelper(getVdsGroupId(),
                        getStoragePoolId(),
                        getVdsGroup().getcompatibility_version(),
                        AuditLogType.IMPORTEXPORT_IMPORT_VM_INVALID_INTERFACES);

        List<VmNetworkInterface> nics = getVm().getInterfaces();

        vmInterfaceManager.sortVmNics(nics, getVm().getStaticData().getManagedDeviceMap());

        // If we import it as a new entity, then we allocate all MAC addresses in advance
        if (getParameters().isImportAsNewEntity()) {
            List<String> macAddresses = getMacPool().allocateMacAddresses(nics.size());
            for (int i = 0; i < nics.size(); ++i) {
                nics.get(i).setMacAddress(macAddresses.get(i));
            }
        }

        for (VmNetworkInterface iface : getVm().getInterfaces()) {
            initInterface(iface);
            vnicProfileHelper.updateNicWithVnicProfileForUser(iface, getCurrentUser());

            vmInterfaceManager.add(iface,
                                   getCompensationContext(),
                                   !getParameters().isImportAsNewEntity(),
                                   getVm().getOs(),
                                   getVdsGroup().getcompatibility_version());
            macsAdded.add(iface.getMacAddress());
        }

        vnicProfileHelper.auditInvalidInterfaces(getVmName());
    }

    private void initInterface(VmNic iface) {
        if (iface.getId() == null) {
            iface.setId(Guid.newGuid());
        }
        fillMacAddressIfMissing(iface);
        iface.setVmTemplateId(null);
        iface.setVmId(getVmId());
    }

    private void fillMacAddressIfMissing(VmNic iface) {
        if (StringUtils.isEmpty(iface.getMacAddress())
                && getMacPool().getAvailableMacsCount() > 0) {
            iface.setMacAddress(getMacPool().allocateNewMac());
        }
    }

    private void addVmDynamic() {
        VmDynamic tempVar = new VmDynamic();
        tempVar.setId(getVmId());
        tempVar.setStatus(VMStatus.ImageLocked);
        tempVar.setVmHost("");
        tempVar.setVmIp("");
        tempVar.setVmFQDN("");
        tempVar.setLastStopTime(new Date());
        tempVar.setAppList(getParameters().getVm().getDynamicData().getAppList());
        getVmDynamicDAO().save(tempVar);
        getCompensationContext().snapshotNewEntity(tempVar);
    }

    private void addVmStatistics() {
        VmStatistics stats = new VmStatistics();
        stats.setId(getVmId());
        getVmStatisticsDAO().save(stats);
        getCompensationContext().snapshotNewEntity(stats);
        getCompensationContext().stateChanged();
    }

    private int computeMinAllocatedMem() {
        if (getVm().getMinAllocatedMem() > 0) {
            return getVm().getMinAllocatedMem();
        }

        VDSGroup vdsGroup = getVdsGroup();
        if (vdsGroup != null && vdsGroup.getmax_vds_memory_over_commit() > 0) {
            return (getVm().getMemSizeMb() * 100) / vdsGroup.getmax_vds_memory_over_commit();
        }

        return getVm().getMemSizeMb();
    }
}
