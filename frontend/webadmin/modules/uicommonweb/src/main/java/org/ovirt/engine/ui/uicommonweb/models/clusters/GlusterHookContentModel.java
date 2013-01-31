package org.ovirt.engine.ui.uicommonweb.models.clusters;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;

public class GlusterHookContentModel extends Model {

    EntityModel content;

    public EntityModel getContent() {
        return content;
    }

    public void setContent(EntityModel content) {
        this.content = content;
    }

    public GlusterHookContentModel() {
        setContent(new EntityModel());
    }
}
