package org.ovirt.engine.core.bll.exportimport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ImportVmTemplateFromExternalUrlParameters;
import org.ovirt.engine.core.common.action.ImportVmTemplateFromOvaParameters;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.GetVmFromOvaQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

@NonTransactiveCommandAttribute
public class ImportVmTemplateFromExternalUrlCommand<P extends ImportVmTemplateFromExternalUrlParameters> extends CommandBase<P> {

    private final String ovaPath;

    public ImportVmTemplateFromExternalUrlCommand(P parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        ovaPath = getParameters().getUrl().replace("ova://", "");
    }

    @Override
    protected void init() {
        super.init();

        setClusterId(getParameters().getClusterId());
        if (getCluster() != null) {
            setStoragePoolId(getCluster().getStoragePoolId());
        }
        setStorageDomainId(getParameters().getStorageDomainId());
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        if (getParameters().getProxyHostId() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_PROXY_HOST_MUST_BE_SPECIFIED);
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        setReturnValue(performImport());
    }

    private ActionReturnValue performImport() {
        VmTemplate vmTemplate = runInternalQuery(QueryType.GetVmTemplateFromOva,
                new GetVmFromOvaQueryParameters(getParameters().getProxyHostId(), ovaPath)).getReturnValue();
        vmTemplate.setName(getVmTemplateName());

        return runInternalAction(ActionType.ImportVmTemplateFromOva, buildImportVmParameters(vmTemplate));
    }

    private ImportVmTemplateFromOvaParameters buildImportVmParameters(VmTemplate vmTemplate) {
        ImportVmTemplateFromOvaParameters prm = createImportVmParameters(vmTemplate);

        prm.setProxyHostId(getParameters().getProxyHostId());

        if (getParameters().getQuotaId() != null) {
            prm.setQuotaId(getParameters().getQuotaId());
        }

        if (getParameters().getCpuProfileId() != null) {
            prm.setCpuProfileId(getParameters().getCpuProfileId());
        }

        if (vmTemplate.getDiskList() == null) {
            vmTemplate.setDiskList(new ArrayList<>());
        }

        if (prm.getImageToDestinationDomainMap() == null) {
            prm.setImageToDestinationDomainMap(new HashMap<>());
        }

        prm.setForceOverride(true);
        prm.setCopyCollapse(true);
        prm.setImportAsNewEntity(getParameters().isImportAsNewEntity());

        for (DiskImage disk : vmTemplate.getDiskTemplateMap().values()) {
            if (getParameters().getQuotaId() != null) {
                disk.setQuotaId(getParameters().getQuotaId());
            }
            vmTemplate.getDiskList().add(disk);
            prm.getImageToDestinationDomainMap().put(disk.getId(), getParameters().getStorageDomainId());
        }

        return prm;
    }

    private ImportVmTemplateFromOvaParameters createImportVmParameters(VmTemplate vmTemplate) {
        ImportVmTemplateFromOvaParameters parameters = new ImportVmTemplateFromOvaParameters(
                vmTemplate,
                getParameters().getStorageDomainId(),
                getStoragePoolId(),
                getClusterId());
        parameters.setProxyHostId(getParameters().getProxyHostId());
        parameters.setOvaPath(ovaPath);

        if (getParameters().getQuotaId() != null) {
            parameters.setQuotaId(getParameters().getQuotaId());
        }

        if (getParameters().getCpuProfileId() != null) {
            parameters.setCpuProfileId(getParameters().getCpuProfileId());
        }
        return parameters;
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
        addValidationMessage(EngineMessage.VAR__TYPE__VM_TEMPLATE);
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put(VdcObjectType.VmTemplate.name().toLowerCase(),
                    (getVmTemplateName() == null) ? "" : getVmTemplateName());
            jobProperties.put(VdcObjectType.Cluster.name().toLowerCase(), getClusterName());
        }
        return jobProperties;
    }

    @Override
    public String getVmTemplateName() {
        return getParameters().getNewTemplateName();
    }
}
