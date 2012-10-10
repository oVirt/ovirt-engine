package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "vm_template_image_map")
public class vm_template_image_map implements Serializable {
    private static final long serialVersionUID = 2739919639921210074L;

    @EmbeddedId
    @AttributeOverrides({
            @AttributeOverride(name = "imageTemplateId", column = @Column(name = "it_guid")),
            @AttributeOverride(name = "vmTemplateId", column = @Column(name = "vmt_guid")) })
    public vm_template_image_map_id id = new vm_template_image_map_id();

    @Column(name = "internal_drive_mapping", length = 50)
    public String internalDriveMapping;
}
