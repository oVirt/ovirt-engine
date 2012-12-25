package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VmTemplateParametersBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NotImplementedException;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

@SuppressWarnings("serial")
public abstract class VmTemplateCommand<T extends VmTemplateParametersBase> extends CommandBase<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected VmTemplateCommand(Guid commandId) {
        super(commandId);
    }

    public VmTemplateCommand(T parameters) {
        super(parameters);
        setVmTemplateId(parameters.getVmTemplateId());
    }

    public VmTemplateCommand() {
    }

    protected String mVmTemplateDescription = "";

    @Override
    protected void executeCommand() {
        throw new NotImplementedException();
    }

    public static boolean isVmTemlateWithSameNameExist(String name) {
        SearchParameters p = new SearchParameters("template : name=" + name, SearchType.VmTemplate);
        p.setMaxCount(Integer.MAX_VALUE);
        List<IVdcQueryable> list = (List<IVdcQueryable>) Backend.getInstance().runInternalQuery(VdcQueryType.Search, p)
                .getReturnValue();

        return list.size() > 0;
    }

    public static boolean isVmTemplateImagesReady(VmTemplate vmTemplate,
            Guid storageDomainId,
            List<String> reasons,
            boolean checkImagesExists,
            boolean checkLocked,
            boolean checkIllegal,
            boolean checkStorageDomain, List<DiskImage> providedVmtImages) {
        boolean returnValue = true;
        List<DiskImage> vmtImages = providedVmtImages;
        if (checkStorageDomain) {
            StorageDomainValidator storageDomainValidator =
                    new StorageDomainValidator(DbFacade.getInstance().getStorageDomainDao().getForStoragePool(
                            storageDomainId, vmTemplate.getstorage_pool_id()));
            returnValue = storageDomainValidator.isDomainExistAndActive(reasons);
        }
        if (returnValue && checkImagesExists) {
            if (vmtImages == null) {
                vmtImages =
                        ImagesHandler.filterImageDisks(DbFacade.getInstance()
                                .getDiskDao()
                                .getAllForVm(vmTemplate.getId()), false, false);
            }
            if (vmtImages.size() > 0
                    && !ImagesHandler.isImagesExists(vmtImages, vmtImages.get(0).getstorage_pool_id().getValue(),
                            storageDomainId)) {
                reasons.add(VdcBllMessages.TEMPLATE_IMAGE_NOT_EXIST.toString());
                returnValue = false;
            }
        }
        if (returnValue && checkLocked) {
            if (vmTemplate.getstatus() == VmTemplateStatus.Locked) {
                returnValue = false;
            } else {
                if (vmtImages != null) {
                    for (DiskImage image : vmtImages) {
                        if (image.getimageStatus() == ImageStatus.LOCKED) {
                            returnValue = false;
                            break;
                        }
                    }
                }
            }
            if (!returnValue) {
                reasons.add(VdcBllMessages.VM_TEMPLATE_IMAGE_IS_LOCKED.toString());
            }
        }
        if (returnValue && checkIllegal && (vmTemplate.getstatus() == VmTemplateStatus.Illegal)) {
            returnValue = false;
            reasons.add(VdcBllMessages.VM_TEMPLATE_IMAGE_IS_ILLEGAL.toString());
        }
        return returnValue;
    }

    /**
     * Determines whether [is domain legal] [the specified domain name].
     *
     * @param domainName
     *            Name of the domain.
     * @param reasons
     *            The reasons.
     * @return <c>true</c> if [is domain legal] [the specified domain name];
     *         otherwise, <c>false</c>.
     */
    public static boolean IsDomainLegal(String domainName, java.util.ArrayList<String> reasons) {
        boolean result = true;
        char[] illegalChars = new char[] { '&' };
        if (!StringHelper.isNullOrEmpty(domainName)) {
            for (char c : illegalChars) {
                if (domainName.contains((Character.toString(c)))) {
                    result = false;
                    reasons.add(VdcBllMessages.ACTION_TYPE_FAILED_ILLEGAL_DOMAIN_NAME.toString());
                    reasons.add(String.format("$Domain %1$s", domainName));
                    reasons.add(String.format("$Char %1$s", c));
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Determines whether [is high availability value legal] [the specified
     * value].
     *
     * @param value
     *            The value.
     * @param reasons
     *            The reasons.
     * @return <c>true</c> if [is vm priority value legal] [the specified
     *         value]; otherwise, <c>false</c>.
     */
    public static boolean IsVmPriorityValueLegal(int value, List<String> reasons) {
        boolean res = false;
        if (value >= 0 && value <= Config.<Integer> GetValue(ConfigValues.VmPriorityMaxValue)) {
            res = true;
        } else {
            reasons.add(VdcBllMessages.VM_OR_TEMPLATE_ILLEGAL_PRIORITY_VALUE.toString());
            reasons.add(String.format("$MaxValue %1$s", Config.<Integer> GetValue(ConfigValues.VmPriorityMaxValue)));
        }
        return res;
    }

    protected ValidationResult templateExists() {
        return getVmTemplate() == null ? new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST)
                : ValidationResult.VALID;
    }

    @Override
    protected String getDescription() {
        return getVmTemplateName();
    }

    protected void RemoveNetwork() {
        List<VmNetworkInterface> list = DbFacade.getInstance().getVmNetworkInterfaceDao()
                .getAllForTemplate(getVmTemplateId());
        for (VmNetworkInterface iface : list) {
            DbFacade.getInstance().getVmDeviceDao().remove(new VmDeviceId(iface.getId(), getVmTemplateId()));
            DbFacade.getInstance().getVmNetworkInterfaceDao().remove(iface.getId());
        }
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<PermissionSubject>();
        permissionList.add(new PermissionSubject(getVmTemplateId(),
                VdcObjectType.VmTemplate,
                getActionType().getActionGroup()));
        return permissionList;
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = new HashMap<String, String>();
            jobProperties.put(VdcObjectType.StoragePool.name().toLowerCase(), getStoragePoolName());
            jobProperties.put(VdcObjectType.VmTemplate.name().toLowerCase(), getVmTemplateName());
        }
        return jobProperties;
    }
}
