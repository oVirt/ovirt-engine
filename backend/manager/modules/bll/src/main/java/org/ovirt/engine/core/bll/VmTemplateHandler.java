package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.ObjectIdentityChecker;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public class VmTemplateHandler {
    public static Guid BlankVmTemplateId = new Guid();
    public static ObjectIdentityChecker mUpdateVmTemplate;
    /**
     * Initialize static list containers, for identity and permission check. The initialization should be executed
     * before calling ObjectIdentityChecker.
     *
     * @see Backend#InitHandlers
     */
    public static void Init()
    {
        mUpdateVmTemplate = new ObjectIdentityChecker(VmTemplateHandler.class);
        BlankVmTemplateId = new Guid("00000000-0000-0000-0000-000000000000");
        mUpdateVmTemplate.AddPermittedFields(new String[] { "name", "description", "domain", "os", "osType",
                "is_auto_suspend", "interfaces", "mem_size_mb", "num_of_sockets", "cpu_per_socket", "num_of_cpus",
                "vds_group_id", "num_of_monitors", "usb_policy", "time_zone", "diskMap", "default_boot_sequence",
                "iso_path", "diskImageMap", "default_display_type", "priority", "auto_startup", "is_stateless",
                "initrd_url", "kernel_url", "kernel_params", "images", "interfaces", "quotaId", "quotaName" });
    }

    public static List<DiskImage> UpdateDisksFromDb(VmTemplate vmt) {
        vmt.getDiskMap().clear();
        vmt.getDiskImageMap().clear();
        vmt.getDiskList().clear();
        List<DiskImage> diskList =
                DbFacade.getInstance().getDiskImageDAO().getAllForVm(vmt.getId());
        for (DiskImage dit : diskList) {
            vmt.getDiskMap().put(dit.getinternal_drive_mapping(), dit);
            // Translation from number of sectors to GB.
            vmt.setSizeGB(Double.valueOf(dit.getsize()) / Double.valueOf((1024 * 1024 * 1024)));
            vmt.getDiskImageMap().put(dit.getinternal_drive_mapping(), dit);
            vmt.getDiskList().add(dit);
        }
        return diskList;
    }

    /**
     * Check if template state did not changed from last canDoAction check (still not locked and exist in the DB).
     *
     * @return True if template is at valid state, false otherwise.
     */
    public static boolean isTemplateStatusIsNotLocked(Guid id) {
        boolean returnValue = false;
        VmTemplate template = DbFacade.getInstance().getVmTemplateDAO().get(id);
        if ((template != null) && (template.getstatus() != VmTemplateStatus.Locked)) {
            returnValue = true;
        }
        return returnValue;
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
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                SetVmTemplateStatus(vmTemplateGuid, VmTemplateStatus.Locked, compensationContext);
                compensationContext.stateChanged();
                return null;
            }
        });
    }

    public static void UnLockVmTemplate(Guid vmTemplateGuid) {
        SetVmTemplateStatus(vmTemplateGuid, VmTemplateStatus.OK, null);
    }

    public static void MarkVmTemplateAsIllegal(Guid vmTemplateGuid) {
        SetVmTemplateStatus(vmTemplateGuid, VmTemplateStatus.Illegal, null);
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
    private static void SetVmTemplateStatus(
            Guid vmTemplateGuid, VmTemplateStatus status, CompensationContext compensationContext) {
        VmTemplate vmTemplate = DbFacade.getInstance().getVmTemplateDAO().get(vmTemplateGuid);
        if (vmTemplate != null) {
            if (compensationContext != null) {
                compensationContext.snapshotEntityStatus(vmTemplate, vmTemplate.getstatus());
            }
            vmTemplate.setstatus(status);
            DbFacade.getInstance().getVmTemplateDAO().update(vmTemplate);
        } else {
            log.warnFormat(
                    "VmTemplateHandler::SetVmTemplateStatus: vmTemplate is null, not setting status '{0}' to vmTemplate",
                    status);
        }
    }

    private static Log log = LogFactory.getLog(VmTemplateHandler.class);
}
