package org.ovirt.engine.core.common.action;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;

/**
 * Interface that describes classes with graphics devices.
 * It unifies graphics device part of VmManagementParametersBase and VmTemplateParametersBase classes.
 */
public interface HasGraphicsDevices {

    /**
     * Returns map of graphics devices.
     */
    Map<GraphicsType, GraphicsDevice> getGraphicsDevices();

}
