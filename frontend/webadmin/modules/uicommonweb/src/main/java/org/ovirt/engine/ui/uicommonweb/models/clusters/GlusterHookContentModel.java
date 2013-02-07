package org.ovirt.engine.ui.uicommonweb.models.clusters;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;

public class GlusterHookContentModel extends Model {

    EntityModel content;

    EntityModel md5Checksum;

    EntityModel status;

    public EntityModel getContent() {
        return content;
    }

    public void setContent(EntityModel content) {
        this.content = content;
    }

    public EntityModel getMd5Checksum() {
        return md5Checksum;
    }

    public void setMd5Checksum(EntityModel md5Checksum) {
        this.md5Checksum = md5Checksum;
    }

    public EntityModel getStatus() {
        return status;
    }

    public void setStatus(EntityModel status) {
        this.status = status;
    }

    public GlusterHookContentModel() {
        setContent(new EntityModel("")); //$NON-NLS-1$
        setMd5Checksum(new EntityModel("")); //$NON-NLS-1$
        setStatus(new EntityModel());
    }
}
