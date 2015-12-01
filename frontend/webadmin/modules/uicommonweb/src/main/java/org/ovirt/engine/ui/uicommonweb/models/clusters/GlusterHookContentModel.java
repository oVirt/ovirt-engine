package org.ovirt.engine.ui.uicommonweb.models.clusters;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookStatus;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;

public class GlusterHookContentModel extends Model {

    EntityModel<String> content;

    EntityModel<String> md5Checksum;

    EntityModel<GlusterHookStatus> status;

    public EntityModel<String> getContent() {
        return content;
    }

    public void setContent(EntityModel<String> content) {
        this.content = content;
    }

    public EntityModel<String> getMd5Checksum() {
        return md5Checksum;
    }

    public void setMd5Checksum(EntityModel<String> md5Checksum) {
        this.md5Checksum = md5Checksum;
    }

    public EntityModel<GlusterHookStatus> getStatus() {
        return status;
    }

    public void setStatus(EntityModel<GlusterHookStatus> status) {
        this.status = status;
    }

    public GlusterHookContentModel() {
        setContent(new EntityModel<>("")); //$NON-NLS-1$
        setMd5Checksum(new EntityModel<>("")); //$NON-NLS-1$
        setStatus(new EntityModel<GlusterHookStatus>());
    }
}
