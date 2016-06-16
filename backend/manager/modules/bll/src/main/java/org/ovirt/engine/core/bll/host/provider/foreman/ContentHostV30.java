package org.ovirt.engine.core.bll.host.provider.foreman;

import java.io.Serializable;
import java.util.Date;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.ovirt.engine.core.utils.serialization.json.JsonCustomDateDeserializer;

public class ContentHostV30 implements Serializable {
    private static final long serialVersionUID = -2714992402945915589L;

    private int id;

    @JsonProperty("created_at")
    @JsonDeserialize(using = JsonCustomDateDeserializer.class)
    private Date createdAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
