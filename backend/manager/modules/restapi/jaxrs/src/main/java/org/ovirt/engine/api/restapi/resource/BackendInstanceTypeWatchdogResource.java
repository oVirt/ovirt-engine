/*
Copyright (c) 2015 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.engine.api.restapi.resource;

import java.util.List;
import java.util.Objects;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.InstanceType;
import org.ovirt.engine.api.model.Watchdog;
import org.ovirt.engine.api.resource.CreationResource;
import org.ovirt.engine.api.resource.InstanceTypeWatchdogResource;
import org.ovirt.engine.api.restapi.types.WatchdogMapper;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.WatchdogParameters;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendInstanceTypeWatchdogResource
        extends AbstractBackendActionableResource<Watchdog, VmWatchdog>
        implements InstanceTypeWatchdogResource {

    private Guid instanceTypeId;

    public BackendInstanceTypeWatchdogResource(String watchdogId, Guid instanceTypeId) {
        super(watchdogId, Watchdog.class, VmWatchdog.class);
        this.instanceTypeId = instanceTypeId;
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
            VdcQueryType.GetWatchdog,
            new IdQueryParameters(instanceTypeId)
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
        InstanceType instanceType = new InstanceType();
        instanceType.setId(instanceTypeId.toString());
        watchdog.setTemplate(instanceType);
        return watchdog;
    }

    @Override
    public Watchdog update(Watchdog watchdog) {
        return performUpdate(watchdog, new WatchdogResolver(), VdcActionType.UpdateWatchdog, new UpdateParametersProvider());
    }

    @Override
    public Response remove() {
        get();
        WatchdogParameters parameters = new WatchdogParameters();
        parameters.setId(instanceTypeId);
        parameters.setVm(false);
        return performAction(VdcActionType.RemoveWatchdog, parameters);
    }

    private class UpdateParametersProvider implements ParametersProvider<Watchdog, VmWatchdog> {
        @Override
        public VdcActionParametersBase getParameters(Watchdog model, VmWatchdog entity) {
            WatchdogParameters parameters = new WatchdogParameters();
            if (model.isSetAction()) {
                parameters.setAction(WatchdogMapper.map(model.getAction()));
            }
            else {
                parameters.setAction(entity.getAction());
            }
            if (model.isSetModel()) {
                parameters.setModel(WatchdogMapper.map(model.getModel()));
            }
            else {
                parameters.setModel(entity.getModel());
            }
            parameters.setId(instanceTypeId);
            parameters.setVm(false);
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
