package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Boot;
import org.ovirt.engine.api.model.InstanceType;
import org.ovirt.engine.api.model.OperatingSystem;
import org.ovirt.engine.core.common.action.UpdateVmTemplateParameters;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;

public class InstanceTypeMapper extends VmBaseMapper {

    @Mapping(from = InstanceType.class, to = org.ovirt.engine.core.common.businessentities.InstanceType.class)
    public static org.ovirt.engine.core.common.businessentities.InstanceType map(
            InstanceType model,
            org.ovirt.engine.core.common.businessentities.InstanceType incoming) {

        VmTemplate entity = incoming != null ? (VmTemplate) incoming : new VmTemplate();
        mapCommonModelToEntity(entity, model);
        return entity;
    }

    @Mapping(from = InstanceType.class, to = VmStatic.class)
    public static VmStatic map(InstanceType model, VmStatic incoming) {
        VmStatic staticVm = incoming != null ? incoming : new VmStatic();
        mapCommonModelToEntity(staticVm, model);
        return staticVm;
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.InstanceType.class, to = InstanceType.class)
    public static InstanceType map(org.ovirt.engine.core.common.businessentities.InstanceType entity, InstanceType incoming) {
        InstanceType model = incoming != null ? incoming : new InstanceType();
        mapCommonEntityToModel(model, (VmTemplate) entity);
        model.setDisplay(DisplayMapper.map(entity, null));

        if (entity.getDefaultBootSequence() != null) {
            OperatingSystem os = model.getOs();
            if (os == null) {
                os = new OperatingSystem();
            }

            if (entity.getDefaultBootSequence() != null) {
                Boot boot = VmMapper.map(entity.getDefaultBootSequence(), null);
                os.setBoot(boot);
            }
            model.setOs(os);
        }
        return model;
    }

    @Mapping(from = InstanceType.class, to = UpdateVmTemplateParameters.class)
    public static UpdateVmTemplateParameters map(InstanceType template, UpdateVmTemplateParameters paramsTemplate) {
        return TemplateMapper.map(template, paramsTemplate);
    }

}
