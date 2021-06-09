package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

public class MDevType implements Serializable {

    private static final long serialVersionUID = -484681445786715632L;

    private String name;

    private String humanReadableName;

    private Integer availableInstances;

    private String description;

    public MDevType() {
    }

    public MDevType(String name, String humanReadableName, Integer availableInstances, String description) {
        super();
        this.name = name;
        this.humanReadableName = humanReadableName;
        this.availableInstances = availableInstances;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHumanReadableName() {
        return humanReadableName;
    }

    public void setHumanReadableName(String humanReadableName) {
        this.humanReadableName = humanReadableName;
    }

    public Integer getAvailableInstances() {
        return availableInstances;
    }

    public void setAvailableInstances(Integer availableInstances) {
        this.availableInstances = availableInstances;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((availableInstances == null) ? 0 : availableInstances.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((humanReadableName == null) ? 0 : humanReadableName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        MDevType other = (MDevType) obj;
        if (availableInstances == null) {
            if (other.availableInstances != null) {
                return false;
            }
        } else if (!availableInstances.equals(other.availableInstances)) {
            return false;
        }
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (humanReadableName == null) {
            if (other.humanReadableName != null) {
                return false;
            }
        } else if (!humanReadableName.equals(other.humanReadableName)) {
            return false;
        }
        return true;
    }
}
