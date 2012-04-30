/**
 *
 */
package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

/**
 * Parameters class to the GetVdsHOoksById query
 */
public class GetVdsHooksByIdParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 3078145747744171090L;
    private Guid vdsId;

    public GetVdsHooksByIdParameters(Guid id) {
        setVdsId(id);
    }

    public GetVdsHooksByIdParameters() {
    }

    public void setVdsId(Guid id) {
        this.vdsId = id;
    }

    public Guid getVdsId() {
        return vdsId;
    }

}
