package org.ovirt.engine.api.restapi.types;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.Boot;
import org.ovirt.engine.api.model.OperatingSystem;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.TemplateStatus;
import org.ovirt.engine.api.model.TemplateVersion;
import org.ovirt.engine.core.common.action.UpdateVmTemplateParameters;
import org.ovirt.engine.core.common.businessentities.VmInit;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.compat.Guid;

public class TemplateMapper extends VmBaseMapper {

    @Mapping(from = Template.class, to = VmTemplate.class)
    public static VmTemplate map(Template model, VmTemplate incoming) {
        VmTemplate entity = incoming != null ? incoming : new VmTemplate();

        mapVmBaseModelToEntity(entity, model);

        if (model.isSetInitialization()) {
            entity.setVmInit(InitializationMapper.map(model.getInitialization(), entity.getVmInit()));
        }

        if (model.isSetCpu() && model.getCpu().isSetArchitecture()) {
            entity.setClusterArch(CPUMapper.map(model.getCpu().getArchitecture(), null));
        }
        if (model.isSetDomain() && model.getDomain().isSetName()) {
            if (entity.getVmInit() == null) {
                entity.setVmInit(new VmInit());
            }
            entity.getVmInit().setDomain(model.getDomain().getName());
        }
        if (model.getVersion() != null) {
            if (model.getVersion().getBaseTemplate() != null
                    && StringUtils.isNotEmpty(model.getVersion().getBaseTemplate().getId())) {
                entity.setBaseTemplateId(Guid.createGuidFromString(model.getVersion().getBaseTemplate().getId()));
            }
            if (model.getVersion().isSetVersionName()) {
                entity.setTemplateVersionName(model.getVersion().getVersionName());
            }
            // numbering is generated in the backend, hence even if user specified version number, we ignore it.
        }

        return entity;
    }

    @Mapping(from = Template.class, to = VmStatic.class)
    public static VmStatic map(Template model, VmStatic incoming) {
        VmStatic staticVm = incoming != null ? incoming : new VmStatic();

        mapVmBaseModelToEntity(staticVm, model);

        if (model.isSetInitialization()) {
            staticVm.setVmInit(InitializationMapper.map(model.getInitialization(), staticVm.getVmInit()));
        }

        if (model.isSetDomain() && model.getDomain().isSetName()) {
            if (staticVm.getVmInit() == null) {
                staticVm.setVmInit(new VmInit());
            }
            staticVm.getVmInit().setDomain(model.getDomain().getName());
        }
        return staticVm;
    }

    @Mapping(from = VmTemplate.class, to = Template.class)
    public static Template map(VmTemplate entity, Template incoming) {
        Template model = incoming != null ? incoming : new Template();

        mapVmBaseEntityToModel(model, entity);

        if (entity.getVmInit() != null) {
            model.setInitialization(InitializationMapper.map(entity.getVmInit(), null));
        }
        if (entity.getStatus() != null) {
            model.setStatus(mapTemplateStatus(entity.getStatus()));
        }
        if (entity.getDefaultBootSequence() != null ||
            entity.getKernelUrl() != null ||
            entity.getInitrdUrl() != null ||
            entity.getKernelParams() != null) {
            OperatingSystem os = new OperatingSystem();

            os.setType(SimpleDependencyInjector.getInstance().get(OsRepository.class).getUniqueOsNames().get(entity.getOsId()));

            if (entity.getDefaultBootSequence() != null) {
                Boot boot = VmMapper.map(entity.getDefaultBootSequence(), null);
                os.setBoot(boot);
            }
            os.setKernel(entity.getKernelUrl());
            os.setInitrd(entity.getInitrdUrl());
            os.setCmdline(entity.getKernelParams());
            model.setOs(os);
        }
        if (entity.getClusterArch() != null) {
            model.getCpu().setArchitecture(CPUMapper.map(entity.getClusterArch(), null));
        }
        model.setDisplay(DisplayMapper.map(entity, null));
        model.getDisplay().setDisconnectAction(map(entity.getConsoleDisconnectAction(), null).toString());

        TemplateVersion version = new TemplateVersion();
        version.setVersionName(entity.getTemplateVersionName());
        version.setVersionNumber(entity.getTemplateVersionNumber());
        Template baseTemplate = new Template();
        baseTemplate.setId(entity.getBaseTemplateId().toString());
        version.setBaseTemplate(baseTemplate);
        model.setVersion(version);

        return model;
    }

    @Mapping(from = Template.class, to = UpdateVmTemplateParameters.class)
    public static UpdateVmTemplateParameters map(Template template, UpdateVmTemplateParameters paramsTemplate) {
        UpdateVmTemplateParameters params = paramsTemplate != null ? paramsTemplate : new UpdateVmTemplateParameters();
        if (template.isSetConsole() && template.getConsole().isSetEnabled()) {
            params.setConsoleEnabled(template.getConsole().isEnabled());
        }
        return params;
    }

    private static TemplateStatus mapTemplateStatus(VmTemplateStatus status) {
        switch (status) {
        case OK:
            return TemplateStatus.OK;
        case Locked:
            return TemplateStatus.LOCKED;
        case Illegal:
            return TemplateStatus.ILLEGAL;
        default:
            return null;
        }
    }
}
