package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.InstanceType;
import org.ovirt.engine.core.common.action.UpdateVmTemplateParameters;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;

public class InstanceTypeMapper extends TemplateMapper {

    @Mapping(from = InstanceType.class, to = org.ovirt.engine.core.common.businessentities.InstanceType.class)
    public static org.ovirt.engine.core.common.businessentities.InstanceType map(
            InstanceType model,
            org.ovirt.engine.core.common.businessentities.InstanceType incoming) {
        return TemplateMapper.map(model, (VmTemplate) incoming);
    }

    @Mapping(from = InstanceType.class, to = VmStatic.class)
    public static VmStatic map(InstanceType model, VmStatic incoming) {
        return TemplateMapper.map(model, incoming);
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.InstanceType.class, to = InstanceType.class)
    public static InstanceType map(org.ovirt.engine.core.common.businessentities.InstanceType entity, InstanceType incoming) {
        InstanceType res = incoming != null ? incoming : new InstanceType();
        TemplateMapper.map((VmTemplate) entity, res);
        return res;
    }

    @Mapping(from = InstanceType.class, to = UpdateVmTemplateParameters.class)
    public static UpdateVmTemplateParameters map(InstanceType template, UpdateVmTemplateParameters paramsTemplate) {
        return TemplateMapper.map(template, paramsTemplate);
    }

}
