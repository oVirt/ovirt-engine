package org.ovirt.engine.api.restapi.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.api.model.BaseResource;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Directory", propOrder = "domain")
public class Directory
    extends BaseResource {

    @XmlElement(required = true)
    protected String domain;

    /**
     * Gets the value of the domain property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Sets the value of the domain property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setDomain(String value) {
        this.domain = value;
    }

    public boolean isSetDomain() {
        return this.domain!= null;
    }
}
