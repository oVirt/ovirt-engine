package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.NetworkLabel;
import org.ovirt.engine.api.model.NetworkLabels;
import org.ovirt.engine.api.resource.NetworkLabelResource;

public abstract class AbstractBaseHostNicLabelResource
    extends AbstractBackendSubResource<NetworkLabel, org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel>
    implements NetworkLabelResource {

    private String id;
    private AbstractBaseHostNicLabelsResource parent;

    protected AbstractBaseHostNicLabelResource(String id, AbstractBaseHostNicLabelsResource parent) {
        super("", NetworkLabel.class, org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel.class);
        this.id = id;
        this.parent = parent;
    }

    public AbstractBaseHostNicLabelsResource getParent() {
        return parent;
    }

    @Override
    public NetworkLabel get() {
        NetworkLabels labels = parent.list();
        if (labels != null) {
            for (NetworkLabel label : labels.getNetworkLabels()) {
                if (label.getId().equals(id)) {
                    parent.addParents(label);
                    return addLinks(label);
                }
            }
        }

        return notFound();
    }

    @Override
    protected NetworkLabel addLinks(NetworkLabel label, String... subCollectionMembersToExclude) {
        super.addLinks(label, subCollectionMembersToExclude);
        overrideHref(label);
        return label;
    }

    void overrideHref(NetworkLabel label) {
        final String href = label.getHref();
        if (href != null) {
            final String[] hRefSegments = href.split("/");
            if (hRefSegments.length>=2) {
                hRefSegments[hRefSegments.length - 2] = getUriPath();
                final String fixedHref = StringUtils.join(hRefSegments, '/');
                label.setHref(fixedHref);
            }
        }
    }

    protected abstract String getUriPath();

    @Override
    public Response remove() {
        get();
        return performRemove();
    }

    protected abstract Response performRemove();
}
