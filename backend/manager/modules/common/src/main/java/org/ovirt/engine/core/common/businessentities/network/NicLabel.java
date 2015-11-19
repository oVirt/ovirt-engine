package org.ovirt.engine.core.common.businessentities.network;

import java.io.Serializable;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.validation.annotation.NicLabelNicIdOrNicNameIsSet;
import org.ovirt.engine.core.common.validation.annotation.ValidName;
import org.ovirt.engine.core.compat.Guid;

@NicLabelNicIdOrNicNameIsSet
public class NicLabel implements Serializable, Nameable {

    private static final long serialVersionUID = 268337006285648462L;
    private Guid nicId;
    private String nicName;

    @NotNull(message="LABEL_ON_NETWORK_LABEL_CANNOT_BE_NULL")
    @ValidName(message = "NETWORK_LABEL_FORMAT_INVALID")
    private String label;

    public NicLabel() {
    }

    public NicLabel(Guid nicId, String nicName, String label) {
        this.nicId = nicId;
        this.nicName = nicName;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Guid getNicId() {
        return nicId;
    }

    public void setNicId(Guid nicId) {
        this.nicId = nicId;
    }

    public String getNicName() {
        return nicName;
    }

    public void setNicName(String nicName) {
        this.nicName = nicName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NicLabel)) {
            return false;
        }

        NicLabel other = (NicLabel) o;
        return Objects.equals(nicId, other.nicId)
                && Objects.equals(nicName, other.nicName)
                && Objects.equals(label, other.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                nicId,
                nicName,
                label
        );
    }

    @Override
    public String getName() {
        return getLabel();
    }
}
