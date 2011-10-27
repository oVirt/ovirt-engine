package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetDomainListParameters")
public class GetDomainListParameters extends VdcQueryParametersBase{
    @XmlElement(name = "FilterInternalDomain", defaultValue = "false")
    private boolean filterInternalDomain;

    public GetDomainListParameters() {
    }

    public GetDomainListParameters(boolean filterInternalDomain) {
        this.setFilterInternalDomain(filterInternalDomain);
    }

    public void setFilterInternalDomain(boolean filterInternalDomain) {
        this.filterInternalDomain = filterInternalDomain;
    }

    public boolean getFilterInternalDomain() {
        return filterInternalDomain;
    }

}
