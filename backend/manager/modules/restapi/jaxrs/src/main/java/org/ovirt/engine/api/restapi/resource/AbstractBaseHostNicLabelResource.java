package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.Label;
import org.ovirt.engine.api.model.Labels;
import org.ovirt.engine.api.resource.LabelResource;
import org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel;

public abstract class AbstractBaseHostNicLabelResource extends AbstractBackendSubResource<Label, NetworkLabel>
        implements LabelResource {

    private String id;
    private AbstractBaseHostNicLabelsResource parent;

    protected AbstractBaseHostNicLabelResource(String id, AbstractBaseHostNicLabelsResource parent) {
        super("", Label.class, NetworkLabel.class);
        this.id = id;
        this.parent = parent;
    }

    public AbstractBaseHostNicLabelsResource getParent() {
        return parent;
    }

    @Override
    public Label get() {
        Labels labels = parent.list();
        if (labels != null) {
            for (Label label : labels.getLabels()) {
                if (label.getId().equals(id)) {
                    parent.addParents(label);
                    return addLinks(label);
                }
            }
        }

        return notFound();
    }

    @Override
    protected Label addLinks(Label label, String... subCollectionMembersToExclude) {
        super.addLinks(label, subCollectionMembersToExclude);
        overrideHref(label);
        return label;
    }

    void overrideHref(Label label) {
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
