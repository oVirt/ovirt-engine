package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

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
    public Response remove() {
        get();
        return performRemove();
    }

    protected abstract Response performRemove();
}
