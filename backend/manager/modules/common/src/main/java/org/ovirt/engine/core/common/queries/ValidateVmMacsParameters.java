package org.ovirt.engine.core.common.queries;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;

public class ValidateVmMacsParameters extends QueryParametersBase {

    private Map<Guid, List<VM>> vmsByCluster;

    // Hide me. Exists in order to please serialization framework we use.
    private ValidateVmMacsParameters() {}

    public ValidateVmMacsParameters(Map<Guid, List<VM>> vmsByCluster) {
        // As long as the default constructor exists, calling it is to be on the safe side for the case it'd contain
        // some logic.
        this();
        this.vmsByCluster = vmsByCluster;
    }

    public Map<Guid, List<VM>> getVmsByCluster() {
        return vmsByCluster;
    }
}
