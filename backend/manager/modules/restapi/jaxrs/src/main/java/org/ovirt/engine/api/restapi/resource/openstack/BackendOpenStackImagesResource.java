/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource.openstack;

import java.util.List;

import org.ovirt.engine.api.model.OpenStackImage;
import org.ovirt.engine.api.model.OpenStackImageProvider;
import org.ovirt.engine.api.model.OpenStackImages;
import org.ovirt.engine.api.resource.openstack.OpenstackImageResource;
import org.ovirt.engine.api.resource.openstack.OpenstackImagesResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResource;
import org.ovirt.engine.core.common.businessentities.storage.ImageFileType;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.queries.GetImagesListParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendOpenStackImagesResource
        extends AbstractBackendCollectionResource<OpenStackImage, RepoImage>
        implements OpenstackImagesResource {
    private String providerId;

    public BackendOpenStackImagesResource(String providerId) {
        super(OpenStackImage.class, RepoImage.class);
        this.providerId = providerId;
    }

    @Override
    public OpenStackImages list() {
        Guid storageDomainId = BackendOpenStackStorageProviderHelper.getStorageDomainId(this, providerId);
        GetImagesListParameters parameters = new GetImagesListParameters(storageDomainId, ImageFileType.All);
        return mapCollection(getBackendCollection(QueryType.GetImagesList, parameters));
    }

    protected OpenStackImages mapCollection(List<RepoImage> entities) {
        OpenStackImages collection = new OpenStackImages();
        for (RepoImage image : entities) {
            collection.getOpenStackImages().add(addLinks(populate(map(image), image)));
        }
        return collection;
    }

    @Override
    protected OpenStackImage addParents(OpenStackImage image) {
        OpenStackImageProvider provider = new OpenStackImageProvider();
        provider.setId(providerId);
        image.setOpenstackImageProvider(provider);
        return super.addParents(image);
    }

    @Override
    public OpenstackImageResource getImageResource(String id) {
        return inject(new BackendOpenStackImageResource(providerId, id));
    }
}
