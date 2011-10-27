package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.image_vm_map;
import org.ovirt.engine.core.common.businessentities.image_vm_map_id;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>ImageVmMapDAO</code> defines a type for performing CRUD operations on instances of {@link image_vm_map}.
 */
public interface ImageVmMapDAO extends GenericDao<image_vm_map, image_vm_map_id> {

    image_vm_map getByImageId(Guid imageId);

    List<image_vm_map> getByVmId(Guid vmId);
}
