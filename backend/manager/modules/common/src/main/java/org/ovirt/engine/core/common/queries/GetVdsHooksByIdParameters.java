/**
 *
 */
package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

/**
 * Parameters class to the GetVdsHOoksById query
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetVdsHooksByIdParameters")
public class GetVdsHooksByIdParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 3078145747744171090L;
    @XmlElement(name = "VdsId")
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
