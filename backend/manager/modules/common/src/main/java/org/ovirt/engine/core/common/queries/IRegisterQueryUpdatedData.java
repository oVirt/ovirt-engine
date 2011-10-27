package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Serializable;

@XmlType(name = "IRegisterQueryUpdatedData", namespace = "http://service.engine.ovirt.org")
public class IRegisterQueryUpdatedData implements Serializable {

    private static final long serialVersionUID = -8363214825616991163L;

    public IRegisterQueryUpdatedData() {
    }
}
