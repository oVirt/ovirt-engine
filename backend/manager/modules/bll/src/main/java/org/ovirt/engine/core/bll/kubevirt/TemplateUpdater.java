package org.ovirt.engine.core.bll.kubevirt;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVmTemplateParameters;
import org.ovirt.engine.core.common.action.VmTemplateManagementParameters;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmTemplateDao;

import openshift.io.V1Template;

@ApplicationScoped
public class TemplateUpdater {
    @Inject
    private Instance<BackendInternal> backend;

    @Inject
    private VmTemplateDao templateDao;

    public boolean addVmTemplate(V1Template template, Guid clusterId) {
        VmStatic vm = EntityMapper.toOvirtVm(template, clusterId);
        // at some point we may want to call AddUnmanagedVm
        AddVmTemplateParameters params = new AddVmTemplateParameters(vm, template.getMetadata().getName(), "");
        params.setPublicUse(true);
        params.setCopyVmPermissions(false);
        ActionReturnValue retVal = backend.get().runInternalAction(ActionType.AddVmTemplate,
                params);
        return retVal.getSucceeded();
    }

    public boolean removeVmTemplate(V1Template commonTemplate, Guid clusterId) {
        List<VmTemplate> templates = templateDao.getAllForCluster(clusterId);
        Predicate<VmTemplate> equalsCommonTemplate = t -> equals(commonTemplate, t);
        Optional<VmTemplate> template = templates.stream().filter(equalsCommonTemplate).findFirst();
        return template.isPresent() ? removeFromDB(template.get()) : false;
    }

    private boolean equals(V1Template commonTemplate, VmTemplate template) {
        // TODO: consider taking the namespace into account (will be more
        // important if the common templates will include something
        // namespaced like PVCs inside them, at the moment they will all
        // probably just be defined in the 'kubevirt' namespace
        return commonTemplate.getMetadata().getName().equals(template.getName());
    }

    public boolean removeFromDB(VmTemplate template) {
        return backend.get().runInternalAction(
                ActionType.RemoveVmTemplate,
                new VmTemplateManagementParameters(template.getId()))
                .getSucceeded();
    }

}
