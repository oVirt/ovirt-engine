package org.ovirt.engine.core.bll.host.provider.foreman;

import org.codehaus.jackson.map.annotate.JsonRootName;

@JsonRootName("host")
public class ForemanHost {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
