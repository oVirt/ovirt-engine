package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "StorageDomainQueryTopSizeVmsParameters")
public class StorageDomainQueryTopSizeVmsParameters extends StorageDomainQueryParametersBase {

    private static final long serialVersionUID = 6625219123430227225L;

    public StorageDomainQueryTopSizeVmsParameters(Guid storageDomainId, int maxVmsToReturn) {
        super(storageDomainId);
        setMaxVmsToReturn(maxVmsToReturn);
    }

    public StorageDomainQueryTopSizeVmsParameters() {
    }
    /*
     * BZ#700327 requires that we return a maximum entries according to the following logic:
     * According to given parameter we are asked to
     * (-1): means return all available entries.
     * 0: means use whatever we have defined in the DB (vdc_options)
     * otherwise: use the limitation we got in the parameter.
    */

    @XmlElement(name = "MaxVmsToReturn")
    private int privateMaxVmsToReturn = 0;

    public int getMaxVmsToReturn() {
        return privateMaxVmsToReturn;
    }

    private void setMaxVmsToReturn(int value) {
        privateMaxVmsToReturn = value;
    }
}
