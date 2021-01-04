/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import java.util.List;
import java.util.Objects;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Nic;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.resource.CreationResource;
import org.ovirt.engine.api.resource.TemplateNicResource;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVmTemplateInterfaceParameters;
import org.ovirt.engine.core.common.action.RemoveVmTemplateInterfaceParameters;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendTemplateNicResource
        extends AbstractBackendActionableResource<Nic, VmNetworkInterface>
        implements TemplateNicResource {

    private Guid templateId;

    protected BackendTemplateNicResource(String nicId, Guid templateId) {
        super(nicId, Nic.class, VmNetworkInterface.class);
        this.templateId = templateId;
    }

    public Nic get() {
        VmNetworkInterface nic = lookupNic(guid);
        if (nic != null) {
            return addLinks(populate(map(nic), nic), Template.class);
        }
        return notFound();
    }

    private VmNetworkInterface lookupNic(Guid nicId) {
        List<VmNetworkInterface> nics = getBackendCollection(
            VmNetworkInterface.class,
            QueryType.GetTemplateInterfacesByTemplateId,
            new IdQueryParameters(templateId)
        );
        for (VmNetworkInterface nic : nics) {
            if (Objects.equals(nic.getId(), nicId)) {
                return nic;
            }
        }
        return null;
    }

    @Override
    public Nic update(Nic nic) {
        return performUpdate(
            nic,
            new NicResolver(),
            ActionType.UpdateVmTemplateInterface, new UpdateParametersProvider()
        );
    }

    @Override
    public Response remove() {
        get();
        return performAction(
            ActionType.RemoveVmTemplateInterface,
            new RemoveVmTemplateInterfaceParameters(templateId, guid)
        );
    }

    @Override
    public CreationResource getCreationResource(String oid) {
        return inject(new BackendCreationResource(oid));
    }

    @Override
    protected Nic addParents(Nic nic) {
        Template template = new Template();
        template.setId(templateId.toString());
        nic.setTemplate(template);
        return nic;
    }

    private class NicResolver extends EntityIdResolver<Guid> {
        @Override
        public VmNetworkInterface lookupEntity(Guid nicId) throws BackendFailureException {
            return lookupNic(nicId);
        }
    }

    private class UpdateParametersProvider implements ParametersProvider<Nic, VmNetworkInterface> {
        @Override
        public ActionParametersBase getParameters(Nic incoming, VmNetworkInterface entity) {
            VmNetworkInterface nic = map(incoming, entity);
            return new AddVmTemplateInterfaceParameters(templateId, nic);
        }
    }
}
