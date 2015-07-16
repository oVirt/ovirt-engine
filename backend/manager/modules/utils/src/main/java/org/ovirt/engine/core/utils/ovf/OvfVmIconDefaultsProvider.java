package org.ovirt.engine.core.utils.ovf;

import java.util.Map;

import org.ovirt.engine.core.common.queries.VmIconIdSizePair;

public interface OvfVmIconDefaultsProvider {

    Map<Integer, VmIconIdSizePair> getVmIconDefaults();
}
