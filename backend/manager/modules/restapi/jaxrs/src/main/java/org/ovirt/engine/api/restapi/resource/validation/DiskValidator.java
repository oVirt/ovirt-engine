package org.ovirt.engine.api.restapi.resource.validation;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.DiskFormat;
import org.ovirt.engine.api.model.DiskInterface;

@ValidatedClass(clazz = Disk.class)
public class DiskValidator implements Validator<Disk> {

    @Override
    public void validateEnums(Disk disk) {
        if (disk != null) {
            if (disk.isSetFormat()) {
                validateEnum(DiskFormat.class, disk.getFormat(), true);
            }
            if (disk.isSetInterface()) {
                validateEnum(DiskInterface.class, disk.getInterface(), true);
            }
        }
    }
}
