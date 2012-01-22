package org.ovirt.engine.core.bll;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AddImagesFromImportParameters;
import org.ovirt.engine.core.common.action.AddVmTemplateFromImportParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.TemplateCandidateInfo;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.vdscommands.GetImageInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class AddVmTemplateFromImportCommand<T extends AddVmTemplateFromImportParameters> extends
        AddVmTemplateCommand<T> {
    protected TemplateCandidateInfo _candidateInfo;

    public AddVmTemplateFromImportCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        boolean canDoAction = true;

        if (!GetAndCheckCandidateInfo() || !CheckCandidateImagesGUIDsLegal()) {
            canDoAction = false;
        }

        else if (isVmTemlateWithSameNameExist(_candidateInfo.getVmTemplateData().getname())) {
            log.errorFormat(
                    "VmTemplateHandler::AddVmTemplateFromImportCommand::CanDoAction: Cannot import candidate {0}, a template with the same name already exists in VDC",
                    _candidateInfo.getVmTemplateData().getname());
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_NAME_ALREADY_EXISTS);
            canDoAction = false;
        }

        if (!canDoAction && getReturnValue().getCanDoActionMessages().size() > 0) {
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__IMPORT);
            addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_TEMPLATE);
        }
        return canDoAction;
    }

    private boolean GetAndCheckCandidateInfo() {
        // Get candidate and check that it is OK:
        Object tempVar = Backend.getInstance()
                .runInternalQuery(VdcQueryType.GetCandidateInfo, getParameters().getCandidateInfoParams())
                .getReturnValue();
        _candidateInfo = (TemplateCandidateInfo) ((tempVar instanceof TemplateCandidateInfo) ? tempVar : null);

        if (_candidateInfo == null) {
            log.errorFormat(
                    "VmTemplateHandler::AddVmTemplateFromImportCommand::GetAndCheckCandidateInfo: Cannot import candidate {0}, candidate info returned as null from IRS",
                    getParameters().getCandidateInfoParams().getCandidateIdOrName());
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_PROBLEM_WITH_CANDIDATE_INFO);
            return false;
        }

        // Set the candidate info's name to new name, if exists. Also set
        // AuditLogableBase's VmTemplateName:
        setVmTemplateName(!StringHelper.isNullOrEmpty(getParameters().getVmTemplateNewName()) ? getParameters()
                .getVmTemplateNewName() : _candidateInfo.getVmTemplateData().getname());
        _candidateInfo.getVmTemplateData().setname(getVmTemplateName());

        // Check that there isn't already a template with the same GUID in the
        // VDC:
        Object tempVar2 = Backend
                .getInstance()
                .runInternalQuery(VdcQueryType.GetVmTemplate,
                        new GetVmTemplateParameters(_candidateInfo.getVmTemplateData().getId())).getReturnValue();
        VmTemplate sameGuidTemplate = (VmTemplate) ((tempVar2 instanceof VmTemplate) ? tempVar2 : null);

        if (sameGuidTemplate != null) {
            log.errorFormat(
                    "VmTemplateHandler::AddVmTemplateFromImportCommand::GetAndCheckCandidateInfo: Cannot import candidate {0}, there is already a template with its GUID ({1}) in VDC.",
                    _candidateInfo.getCandidateDisplayName(),
                    _candidateInfo.getVmTemplateData().getId());
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_GUID_ALREADY_EXISTS);
            return false;
        }

        return true;
    }

    private boolean CheckCandidateImagesGUIDsLegal() {
        Map<String, List<DiskImage>> imagesToImport = _candidateInfo.getImagesData();
        if (imagesToImport != null && imagesToImport.size() > 0) {
            for (List<DiskImage> driveImagesToImport : imagesToImport.values()) {
                if (driveImagesToImport != null && driveImagesToImport.size() > 0) {
                    for (DiskImage image : driveImagesToImport) {
                        // check that each image's GUID doesn't exist in the
                        // VDC:
                        Guid imageGUID = image.getId();
                        Guid storagePoolId = image.getstorage_pool_id() != null ? image.getstorage_pool_id().getValue()
                                : Guid.Empty;
                        Guid storageDomainId = image.getstorage_id() != null ? image.getstorage_id().getValue()
                                : Guid.Empty;
                        Guid imageGroupId = image.getimage_group_id() != null ? image.getimage_group_id().getValue()
                                : Guid.Empty;
                        VDSReturnValue retValue = Backend
                                .getInstance()
                                .getResourceManager()
                                .RunVdsCommand(
                                        VDSCommandType.DoesImageExist,
                                        new GetImageInfoVDSCommandParameters(storagePoolId, storageDomainId,
                                                imageGroupId, imageGUID));

                        if (retValue == null || retValue.getReturnValue() == null
                                || !(retValue.getReturnValue() instanceof Boolean)
                                || (Boolean) (retValue.getReturnValue())) {
                            log.errorFormat(
                                    "VmTemplateHandler::AddVmTemplateFromImportCommand::CheckCandidateImagesGUIDsLegal: Cannot import candidate, Image {0} already exists in IRS",
                                    imageGUID);
                            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_IMAGE_ALREADY_EXISTS);
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    @Override
    protected void AddVmTemplateToDb() {
        _candidateInfo.getVmTemplateData().setstatus(VmTemplateStatus.Locked);
        DbFacade.getInstance().getVmTemplateDAO().save(_candidateInfo.getVmTemplateData());

        setActionReturnValue(_candidateInfo.getVmTemplateData().getId());
    }

    @Override
    protected void AddVmTemplateImages() {
        Guid candidateID = _candidateInfo.getVmTemplateData().getId();
        // LINQ 29456
        // Dictionary<string, Guid> baseImageIDs =
        // _candidateInfo.ImagesData.ToDictionary<KeyValuePair<string,
        // List<DiskImage>>, string, Guid>
        // (a => a.Key,
        // a => a.Value[0].image_guid);

        HashMap<String, Guid> baseImageIds = new HashMap<String, Guid>();
        for (String key : _candidateInfo.getImagesData().keySet()) {
            baseImageIds.put(key, _candidateInfo.getImagesData().get(key).get(0).getId());
        }

        Backend.getInstance().runInternalAction(
                VdcActionType.AddTemplateImagesFromImport,
                new AddImagesFromImportParameters(candidateID.toString(), candidateID, baseImageIds, getParameters()
                        .getCandidateInfoParams().getPath(), getParameters().getCandidateInfoParams()
                        .getCandidateSource(), getParameters().getForce(), _candidateInfo.getImagesData()),
                ExecutionHandler.createDefaultContexForTasks(executionContext));

    }

    @Override
    protected void executeCommand() {
        // TODO: einav - consider new ovf structure including vm interfaces
        setActionReturnValue(Guid.Empty);
        AddCustomValue("ImportedVmTemplateName", _candidateInfo.getVmTemplateData().getname());

        AddVmTemplateToDb();
        AddVmTemplateImages();

        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.TEMPLATE_IMPORT : AuditLogType.TEMPLATE_IMPORT_FAILED;
    }

    private static Log log = LogFactory.getLog(AddVmTemplateFromImportCommand.class);
}
