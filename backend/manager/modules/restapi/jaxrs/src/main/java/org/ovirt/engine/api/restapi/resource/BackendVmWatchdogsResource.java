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
import org.ovirt.engine.api.model.Watchdogs;
import org.ovirt.engine.api.resource.VmWatchdogResource;
import org.ovirt.engine.api.resource.VmWatchdogsResource;
import org.ovirt.engine.api.restapi.types.WatchdogMapper;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.WatchdogParameters;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmWatchdogsResource
        extends AbstractBackendCollectionResource<Watchdog, VmWatchdog>
        implements VmWatchdogsResource {

    private Guid vmId;
    private List<VmWatchdog> watchdogs;

    public BackendVmWatchdogsResource(Guid vmId) {
        super(Watchdog.class, VmWatchdog.class);
        this.vmId = vmId;
    }

    protected BackendVmWatchdogsResource(Guid vmId, List<VmWatchdog> watchdogs) {
        this(vmId);
        this.watchdogs = watchdogs;
    }

    @Override
    public Watchdogs list() {
        return watchdogs == null ?
                mapCollection(getBackendCollection(QueryType.GetWatchdog, new IdQueryParameters(vmId)))
                : mapCollection(watchdogs);
    }

    private Watchdogs mapCollection(List<VmWatchdog> entities) {
        Watchdogs collection = new Watchdogs();
        for (VmWatchdog entity : entities) {
            collection.getWatchdogs().add(addLinks(map(entity)));
        }
        return collection;
    }

    public Response add(Watchdog watchdog) {
        validateParameters(watchdog, "action", "model");
        WatchdogParameters parameters = new WatchdogParameters();
        if (watchdog.isSetAction()) {
            parameters.setAction(WatchdogMapper.map(watchdog.getAction()));
        }
        if (watchdog.isSetModel()) {
            parameters.setModel(WatchdogMapper.map(watchdog.getModel()));
        }
        parameters.setId(vmId);
        parameters.setVm(true);
        return performCreate(ActionType.AddWatchdog, parameters, new WatchdogResolver());
    }

    @Override
    public VmWatchdogResource getWatchdogResource(String watchdogId) {
        return inject(new BackendVmWatchdogResource(watchdogId, vmId));
    }

    @Override
    public Watchdog addParents(Watchdog watchdog) {
        Vm vm = new Vm();
        vm.setId(vmId.toString());
        watchdog.setVm(vm);
        return watchdog;
    }

    private class WatchdogResolver implements IResolver<Guid, VmWatchdog> {
        @Override
        public VmWatchdog resolve(Guid id) throws BackendFailureException {
            List<VmWatchdog> watchdogs = getBackendCollection(QueryType.GetWatchdog, new IdQueryParameters(vmId));
            for (VmWatchdog watchdog : watchdogs) {
                if (Objects.equals(watchdog.getId(), id)) {
                    return watchdog;
                }
            }
            return null;
        }
    }
}
