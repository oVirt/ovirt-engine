/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import java.util.List;
import java.util.Objects;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.model.Watchdog;
import org.ovirt.engine.api.resource.CreationResource;
import org.ovirt.engine.api.resource.VmWatchdogResource;
import org.ovirt.engine.api.restapi.types.WatchdogMapper;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.WatchdogParameters;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmWatchdogResource
        extends AbstractBackendActionableResource<Watchdog, VmWatchdog>
        implements VmWatchdogResource {

    private Guid vmId;

    public BackendVmWatchdogResource(String watchdogId, Guid vmId) {
        super(watchdogId, Watchdog.class, VmWatchdog.class);
        this.vmId = vmId;
    }

    @Override
    public Watchdog get() {
        VmWatchdog entity = getWatchdog();
        if (entity == null) {
            return notFound();
        }
        return addLinks(populate(map(entity), entity));
    }

    private VmWatchdog getWatchdog() {
        List<VmWatchdog> entities = getBackendCollection(
            VmWatchdog.class,
            QueryType.GetWatchdog,
            new IdQueryParameters(vmId)
        );
        for (VmWatchdog current : entities) {
            if (Objects.equals(current.getId(), guid)) {
                return current;
            }
        }
        return null;
    }

    @Override
    public CreationResource getCreationResource(String ids) {
        return inject(new BackendCreationResource(ids));
    }

    @Override
    public Watchdog addParents(Watchdog watchdog) {
        Vm vm = new Vm();
        vm.setId(vmId.toString());
        watchdog.setVm(vm);
        return watchdog;
    }

    @Override
    public Watchdog update(Watchdog watchdog) {
        return performUpdate(
            watchdog, new WatchdogResolver(), ActionType.UpdateWatchdog, new UpdateParametersProvider());
    }

    @Override
    public Response remove() {
        get();
        WatchdogParameters parameters = new WatchdogParameters();
        parameters.setId(vmId);
        parameters.setVm(true);
        return performAction(ActionType.RemoveWatchdog, parameters);
    }

    private class UpdateParametersProvider implements ParametersProvider<Watchdog, VmWatchdog> {
        @Override
        public ActionParametersBase getParameters(Watchdog model, VmWatchdog entity) {
            WatchdogParameters parameters = new WatchdogParameters();
            if (model.isSetAction()) {
                parameters.setAction(WatchdogMapper.map(model.getAction()));
            } else {
                parameters.setAction(entity.getAction());
            }
            if (model.isSetModel()) {
                parameters.setModel(WatchdogMapper.map(model.getModel()));
            } else {
                parameters.setModel(entity.getModel());
            }
            parameters.setId(vmId);
            parameters.setVm(true);
            return parameters;
        }
    }

    private class WatchdogResolver extends EntityIdResolver<Guid> {
        @Override
        public VmWatchdog lookupEntity(Guid id) throws BackendFailureException {
            return getWatchdog();
        }
    }
}
