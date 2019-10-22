/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import java.util.List;
import java.util.Objects;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.Watchdog;
import org.ovirt.engine.api.model.Watchdogs;
import org.ovirt.engine.api.resource.TemplateWatchdogResource;
import org.ovirt.engine.api.resource.TemplateWatchdogsResource;
import org.ovirt.engine.api.restapi.types.WatchdogMapper;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.WatchdogParameters;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendTemplateWatchdogsResource
        extends AbstractBackendCollectionResource<Watchdog, VmWatchdog>
        implements TemplateWatchdogsResource {

    private Guid templateId;

    public BackendTemplateWatchdogsResource(Guid templateId) {
        super(Watchdog.class, VmWatchdog.class);
        this.templateId = templateId;
    }

    @Override
    public Watchdogs list() {
        return mapCollection(getBackendCollection(QueryType.GetWatchdog, new IdQueryParameters(templateId)));
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
        parameters.setId(templateId);
        parameters.setVm(false);
        return performCreate(ActionType.AddWatchdog, parameters, new WatchdogResolver());
    }

    @Override
    public TemplateWatchdogResource getWatchdogResource(String watchdogId) {
        return inject(new BackendTemplateWatchdogResource(watchdogId, templateId));
    }

    @Override
    public Watchdog addParents(Watchdog watchdog) {
        Template template = new Template();
        template.setId(templateId.toString());
        watchdog.setTemplate(template);
        return watchdog;
    }

    private class WatchdogResolver implements IResolver<Guid, VmWatchdog> {
        @Override
        public VmWatchdog resolve(Guid id) throws BackendFailureException {
            List<VmWatchdog> watchdogs = getBackendCollection(QueryType.GetWatchdog, new IdQueryParameters(templateId));
            for (VmWatchdog watchdog : watchdogs) {
                if (Objects.equals(watchdog.getId(), id)) {
                    return watchdog;
                }
            }
            return null;
        }
    }
}
