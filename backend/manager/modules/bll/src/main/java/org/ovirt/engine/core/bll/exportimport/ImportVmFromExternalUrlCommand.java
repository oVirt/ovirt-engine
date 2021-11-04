package org.ovirt.engine.core.bll.exportimport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ImportVmFromExternalProviderParameters;
import org.ovirt.engine.core.common.action.ImportVmFromExternalUrlParameters;
import org.ovirt.engine.core.common.action.ImportVmFromOvaParameters;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.GetVmFromOvaQueryParameters;
import org.ovirt.engine.core.common.queries.GetVmsFromExternalProviderQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.di.Injector;

@NonTransactiveCommandAttribute
public class ImportVmFromExternalUrlCommand<P extends ImportVmFromExternalUrlParameters> extends CommandBase<P> {

    private ExternalVmImporter vmImporter;

    public ImportVmFromExternalUrlCommand(P parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void init() {
        super.init();

        setClusterId(getParameters().getClusterId());
        if (getCluster() != null) {
            setStoragePoolId(getCluster().getStoragePoolId());
        }
        setStorageDomainId(getParameters().getStorageDomainId());
        vmImporter = getVmImporter();
    }

    @Override
    protected boolean validate() {
        return super.validate() && vmImporter.validate();
    }

    @Override
    protected void executeCommand() {
        setReturnValue(vmImporter.performImport());
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        Set<PermissionSubject> permissionSet = new HashSet<>();
        // Destination domain
        permissionSet.add(new PermissionSubject(getStorageDomainId(),
                VdcObjectType.Storage,
                getActionType().getActionGroup()));
        return new ArrayList<>(permissionSet);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__IMPORT);
        addValidationMessage(EngineMessage.VAR__TYPE__VM);
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put(VdcObjectType.VM.name().toLowerCase(),
                    (getVmName() == null) ? "" : getVmName());
            jobProperties.put(VdcObjectType.Cluster.name().toLowerCase(), getClusterName());
        }
        return jobProperties;
    }

    @Override
    public String getVmName() {
        return getParameters().getNewVmName() != null
                ? getParameters().getNewVmName()
                : getParameters().getExternalName();
    }

    private ExternalVmImporter getVmImporter() {
        boolean isOvaImport = getParameters().getUrl().startsWith("ova://");
        return Injector.injectMembers(
                isOvaImport ? new ExternalOvaVmImporter() : new ExternalVmProviderImporter());
    }

    private abstract class ExternalVmImporter {

        public boolean validate() {
            return true;
        }

        public ActionReturnValue performImport() {
            VM vm = loadExternalVm();
            vm.setName(getVmName());

            return runInternalAction(getImportActionType(), buildImportVmParameters(vm));
        }

        private ImportVmFromExternalProviderParameters buildImportVmParameters(VM vm) {
            ImportVmFromExternalProviderParameters prm = createImportVmParameters(vm);

            prm.setProxyHostId(getParameters().getProxyHostId());
            prm.setVirtioIsoName(getParameters().getVirtioIsoName());
            prm.setExternalName(getParameters().getExternalName());

            if (getParameters().getQuotaId() != null) {
                prm.setQuotaId(getParameters().getQuotaId());
            }

            if (getParameters().getCpuProfileId() != null) {
                prm.setCpuProfileId(getParameters().getCpuProfileId());
            }

            prm.setForceOverride(true);
            prm.setCopyCollapse(true);
//            boolean existsInTheSystem = vmStaticDao.get(vm.getId()) != null;
//            prm.setImportAsNewEntity(existsInTheSystem);
            // A workaround to make the import command reallocate mac addresses, yuck!
            prm.setImportAsNewEntity(true);

            for (Map.Entry<Guid, Disk> entry : vm.getDiskMap().entrySet()) {
                DiskImage disk = (DiskImage) entry.getValue();

                if (getParameters().getVolumeType() == null) {
                    disk.setVolumeType(getAutoDetectedVolumeType(disk));
                } else {
                    disk.setVolumeType(getParameters().getVolumeType());
                }

                // in kvm we just copy the image, in other modes such as vmware or xen we use
                // virt-v2v which converts the image format as well
                if (vm.getOrigin() != OriginType.KVM) {
                    disk.setVolumeFormat(getDiskVolumeFormat(
                            disk.getVolumeType(),
                            getStorageDomain().getStorageType()));
                }

                if (getParameters().getQuotaId() != null) {
                    disk.setQuotaId(getParameters().getQuotaId());
                }
            }

            return prm;
        }

        protected abstract VolumeType getAutoDetectedVolumeType(DiskImage disk);

        protected abstract VM loadExternalVm();

        protected abstract ActionType getImportActionType();

        protected abstract ImportVmFromExternalProviderParameters createImportVmParameters(VM vm);
    }

    private class ExternalVmProviderImporter extends ExternalVmImporter {

        @Override
        public boolean validate() {
            if (!super.validate()) {
                return false;
            }

            if (StringUtils.isBlank(getParameters().getUsername())) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_USERNAME_MUST_BE_SPECIFIED);
            }

            if (StringUtils.isBlank(getParameters().getPassword())) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_PASSWORD_MUST_BE_SPECIFIED);
            }

            return true;
        }

        @Override
        protected VolumeType getAutoDetectedVolumeType(DiskImage disk) {
            return VolumeType.Sparse;
        }

        @Override
        protected ActionType getImportActionType() {
            return ActionType.ImportVmFromExternalProvider;
        }

        @Override
        protected ImportVmFromExternalProviderParameters createImportVmParameters(VM vm) {
            ImportVmFromExternalProviderParameters parameters = new ImportVmFromExternalProviderParameters(
                    vm,
                    getParameters().getStorageDomainId(),
                    getStoragePoolId(),
                    getClusterId());
            parameters.setUrl(getParameters().getUrl());
            parameters.setUsername(getParameters().getUsername());
            parameters.setPassword(getParameters().getPassword());
            return parameters;
        }

        @Override
        protected VM loadExternalVm() {
            List<VM> externalVms = runInternalQuery(QueryType.GetVmsFromExternalProvider, buildGetVmsParameters())
                    .getReturnValue();
            return (externalVms != null ? externalVms : Collections.<VM>emptyList())
                    .stream()
                    .filter(vm -> vm.getName().equals(getParameters().getExternalName()))
                    .findFirst()
                    .orElseThrow(() -> new EngineException(EngineError.noVM));
        }

        public GetVmsFromExternalProviderQueryParameters buildGetVmsParameters() {
            return new GetVmsFromExternalProviderQueryParameters(
                    getParameters().getUrl(),
                    getParameters().getUsername(),
                    getParameters().getPassword(),
                    getParameters().getOriginType(),
                    getParameters().getProxyHostId(),
                    getStoragePoolId(),
                    Collections.singletonList(getParameters().getExternalName()));
        }
    }

    private class ExternalOvaVmImporter extends ExternalVmImporter {

        private final String ovaPath;

        public ExternalOvaVmImporter() {
            ovaPath = getParameters().getUrl().replace("ova://", "");
        }

         @Override
         public boolean validate() {
             if (!super.validate()) {
                 return false;
             }

             if (getParameters().getProxyHostId() == null) {
                 return failValidation(EngineMessage.ACTION_TYPE_FAILED_PROXY_HOST_MUST_BE_SPECIFIED);
             }

             return true;
         }

         @Override
         protected VolumeType getAutoDetectedVolumeType(DiskImage disk) {
             return disk.getVolumeType();
         }

        @Override
        protected ActionType getImportActionType() {
            return ActionType.ImportVmFromOva;
        }

        @Override
        protected ImportVmFromExternalProviderParameters createImportVmParameters(VM vm) {
            ImportVmFromOvaParameters parameters = new ImportVmFromOvaParameters(
                    vm,
                    getParameters().getStorageDomainId(),
                    getStoragePoolId(),
                    getClusterId());
            parameters.setOvaPath(ovaPath);
            return parameters;
        }

        @Override
        protected VM loadExternalVm() {
            return runInternalQuery(QueryType.GetVmFromOva,
                    new GetVmFromOvaQueryParameters(getParameters().getProxyHostId(), ovaPath)).getReturnValue();
        }
    }

    // TODO: remove code duplication with frontend AsyncDataProvider
    private static VolumeFormat getDiskVolumeFormat(VolumeType volumeType, StorageType storageType) {
        if (storageType.isFileDomain()) {
            return VolumeFormat.RAW;
        } else if (storageType.isBlockDomain()) {
            switch (volumeType) {
            case Sparse:
                return VolumeFormat.COW;

            case Preallocated:
                return VolumeFormat.RAW;

            default:
                return VolumeFormat.Unassigned;
            }
        } else {
            return VolumeFormat.Unassigned;
        }
    }
}
