package org.ovirt.engine.core.common.businessentities.network;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

/**
 * The entity `Tlv` represents the TLV received physically by the oVirt host's NIC.
 * A `Tlv` is associated to a `VdsNetworkInterface`, called
 * [`HostNic` in REST-API](http://ovirt.github.io/ovirt-engine-api-model/4.2/#types/host_nic).
 * Organizationally Specific `Tlv`s have the `type` of `127` and the attributes
 * `oui` and `subtype`.
 */
public class Tlv implements BusinessEntity<Guid>, Nameable {
    private static final long serialVersionUID = 4590943971823861235L;
    @NotNull
    private Guid id;

    /* The `name` is a human readable string to describe what the value is about and
     * may not be unique. The name is redundant, because it could be created from
     * `type` and the optional `oui` and `subtype`. The purpose of `name` is
     * to simplify the reading of the TLV.
     * The `name` of a property is exactly the same string which is used in
     * IEEE 802.1AB chapter 8.
     */
    @NotNull
    private String name;

    /* TLV type of IEEE 802.1AB */
    int type;

    /* optional organizationally unique identifier */
    private Integer oui;

    /* organizationally defined subtype */
    private Integer subtype;

    /* structured information */
    private Map<String, String> properties = new HashMap<>();

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Integer getOui() {
        return oui;
    }

    public void setOui(Integer oui) {
        this.oui = oui;
    }

    public Integer getSubtype() {
        return subtype;
    }

    public void setSubtype(Integer subtype) {
        this.subtype = subtype;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = Objects.requireNonNull(properties);
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("id", id)
                .append("name", name)
                .append("type", type)
                .append("oui", oui)
                .append("subtype", subtype)
                .append("properties", properties)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Tlv)) {
            return false;
        }

        Tlv tlv = (Tlv) o;
        return super.equals(o)
                && Objects.equals(id, tlv.id)
                && Objects.equals(type, tlv.type)
                && Objects.equals(name, tlv.name)
                && Objects.equals(oui, tlv.oui)
                && Objects.equals(subtype, tlv.subtype)
                && Objects.equals(properties, tlv.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                id,
                name,
                type,
                oui,
                subtype,
                properties);
    }
}
