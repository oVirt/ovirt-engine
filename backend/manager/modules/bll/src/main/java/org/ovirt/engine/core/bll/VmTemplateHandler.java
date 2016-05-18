package org.ovirt.engine.core.bll;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.common.backendinterfaces.BaseHandler;
import org.ovirt.engine.core.common.businessentities.EditableField;
import org.ovirt.engine.core.common.businessentities.EditableOnTemplate;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.ObjectIdentityChecker;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VmTemplateHandler {
    private static final Logger log = LoggerFactory.getLogger(VmTemplateHandler.class);

    public static final Guid BLANK_VM_TEMPLATE_ID = Guid.Empty;
    public static final String BLANK_VM_TEMPLATE_NAME = "Blank";
    private static ObjectIdentityChecker updateVmTemplate;

    /**
     * Initialize static list containers, for identity and permission check. The initialization should be executed
     * before calling ObjectIdentityChecker.
     *
     * @see Backend#initHandlers()
     */
    public static void init() {
        final Class<?>[] inspectedClassNames = new Class<?>[] { VmBase.class, VmTemplate.class };
        updateVmTemplate = new ObjectIdentityChecker(VmTemplateHandler.class);

        for (Pair<EditableField, Field> pair : BaseHandler.extractAnnotatedFields(EditableField.class, inspectedClassNames)) {
            updateVmTemplate.addPermittedFields(pair.getSecond().getName());
        }

        for (Pair<EditableOnTemplate, Field> pair : BaseHandler.extractAnnotatedFields(EditableOnTemplate.class, inspectedClassNames)) {
            updateVmTemplate.addPermittedFields(pair.getSecond().getName());
        }
    }

    public static boolean isUpdateValid(VmTemplate source, VmTemplate destination) {
        return updateVmTemplate.isUpdateValid(source, destination);
    }

    public static void updateDisksFromDb(VmTemplate vmt) {
        vmt.getDiskTemplateMap().clear();
        vmt.getDiskImageMap().clear();
        vmt.getDiskList().clear();
        List<Disk> diskList = DbFacade.getInstance().getDiskDao().getAllForVm(vmt.getId());
        for (Disk dit : diskList) {
            DiskImage diskImage = (DiskImage) dit;
            vmt.getDiskTemplateMap().put(dit.getId(), diskImage);
            // Translation from number of sectors to GB.
            vmt.setSizeGB(Double.valueOf(dit.getSize()) / Double.valueOf(1024 * 1024 * 1024));
            vmt.getDiskImageMap().put(dit.getId(), diskImage);

            DiskVmElement dve = DbFacade.getInstance().getDiskVmElementDao().get(new VmDeviceId(dit.getId(), vmt.getId()));
            dit.setDiskVmElements(Collections.singletonList(dve));

            vmt.getDiskList().add(diskImage);
        }
    }

    /**
     * Lock the VM template with the given id in a new transaction, handling the compensation data using the given
     * {@link CompensationContext}.
     *
     * @param vmTemplateGuid
     *            The id of the template to lock.
     * @param compensationContext
     *            The compensation context for saving the old status (can't be <code>null</code>).
     */
    public static void lockVmTemplateInTransaction(final Guid vmTemplateGuid,
            final CompensationContext compensationContext) {
        TransactionSupport.executeInNewTransaction(() -> {
            setVmTemplateStatus(vmTemplateGuid, VmTemplateStatus.Locked, compensationContext);
            compensationContext.stateChanged();
            return null;
        });
    }

    public static void unlockVmTemplate(Guid vmTemplateGuid) {
        setVmTemplateStatus(vmTemplateGuid, VmTemplateStatus.OK, null);
    }

    /**
     * Set the status of the VM template with the given id to the desired status, saving the old status if necessary.
     *
     * @param vmTemplateGuid
     *            The id of the template to set the status for.
     * @param status
     *            The status to set.
     * @param compensationContext
     *            The compensation context for saving the old status (can be <code>null</code> if the old status is not
     *            required to be saved).
     */
    private static void setVmTemplateStatus(
            Guid vmTemplateGuid, VmTemplateStatus status, CompensationContext compensationContext) {
        VmTemplate vmTemplate = DbFacade.getInstance().getVmTemplateDao().get(vmTemplateGuid);
        if (vmTemplate != null) {
            if (compensationContext != null) {
                compensationContext.snapshotEntityStatus(vmTemplate);
            }
            vmTemplate.setStatus(status);
            DbFacade.getInstance().getVmTemplateDao().update(vmTemplate);
        } else {
            log.warn(
                    "setVmTemplateStatus: vmTemplate is null, not setting status '{}' to vmTemplate",
                    status);
        }
    }
}
