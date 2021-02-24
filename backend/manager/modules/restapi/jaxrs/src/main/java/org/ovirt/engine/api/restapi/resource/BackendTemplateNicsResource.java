package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Nic;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.resource.TemplateNicResource;
import org.ovirt.engine.api.resource.TemplateNicsResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVmTemplateInterfaceParameters;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendTemplateNicsResource extends AbstractBackendNicsResource implements TemplateNicsResource {
    private Guid templateId;

    public BackendTemplateNicsResource(Guid templateId) {
        super(templateId, QueryType.GetTemplateInterfacesByTemplateId);
        this.templateId = templateId;
    }

    @Override
    public Nics list() {
        Nics nics = new Nics();
        List<VmNetworkInterface> entities = getBackendCollection(
            QueryType.GetTemplateInterfacesByTemplateId,
            new IdQueryParameters(templateId)
        );
        for (VmNetworkInterface entity : entities) {
            Nic nic = populate(map(entity), entity);
            nics.getNics().add(addLinks(nic, Template.class));
        }
        return nics;
    }

    public Response add(Nic nic) {
        validateParameters(nic, "name");
        return performCreate(
            ActionType.AddVmTemplateInterface,
            new AddVmTemplateInterfaceParameters(templateId, map(nic)),
            new NicResolver(nic.getName()),
            Template.class
        );
    }

    @Override
    public TemplateNicResource getNicResource(String id) {
        return inject(new BackendTemplateNicResource(id, templateId));
    }

    @Override
    public Nic addParents(Nic nic) {
        Template template = new Template();
        template.setId(templateId.toString());
        nic.setTemplate(template);
        return nic;
    }
}
