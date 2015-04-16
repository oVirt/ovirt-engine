package org.ovirt.engine.core.common.businessentities.storage;

import java.io.Serializable;
import java.util.Map;

import org.ovirt.engine.core.common.utils.ToStringBuilder;

public class CinderVolumeType implements Serializable {

    private String id;

    private String name;

    private Map<String, String> extraSpecs;

    public CinderVolumeType() {
    }

    public CinderVolumeType(String id, String name, Map<String, String> extraSpecs) {
        this.id = id;
        this.name = name;
        this.extraSpecs = extraSpecs;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the extra_specs
     */
    public Map<String, String> getExtraSpecs() {
        return extraSpecs;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setExtraSpecs(Map<String, String> extraSpecs) {
        this.extraSpecs = extraSpecs;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("id", id)
                .append("name", name)
                .append("extra_specs", extraSpecs)
                .build();
    }

}

