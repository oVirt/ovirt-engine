package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.Icon;
import org.ovirt.engine.api.model.Icons;
import org.ovirt.engine.api.resource.IconResource;
import org.ovirt.engine.api.resource.IconsResource;
import org.ovirt.engine.core.common.businessentities.VmIcon;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendIconsResource
        extends AbstractBackendCollectionResource<Icon, VmIcon>
        implements IconsResource {

    public BackendIconsResource() {
        super(Icon.class, VmIcon.class);
    }

    @Override
    public Icons list() {
        return mapCollection(getBackendCollection(QueryType.GetAllVmIcons, new QueryParametersBase()));
    }

    private Icons mapCollection(List<VmIcon> backendEntities) {
        Icons collection = new Icons();
        for (VmIcon icon : backendEntities) {
            collection.getIcons().add(addLinks(map(icon)));
        }
        return collection;
    }

    @Override
    public IconResource getIconResource(String id) {
        return inject(new BackendIconResource(id));
    }
}
