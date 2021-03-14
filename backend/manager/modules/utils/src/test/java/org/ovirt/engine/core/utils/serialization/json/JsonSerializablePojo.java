package org.ovirt.engine.core.utils.serialization.json;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.RandomUtils;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * This POJO can be serialized & deserialized by Jackson, and is used to test the jacson serialization & deserialization
 * classes.
 */
@SuppressWarnings("serial")
@JsonPropertyOrder(value = { "integer", "object", "string", "guid" })
public class JsonSerializablePojo implements Serializable {

    /* --- Fields for serialization --- */

    public Integer integer;

    protected Guid guid;

    private String string;

    private Object object;

    /* --- Constructors --- */

    /**
     * Construct a new POJO initialized with random values.
     */
    public JsonSerializablePojo() {
        integer = RandomUtils.instance().nextInt();
        setGuid(Guid.newGuid());
        setString(RandomUtils.instance().nextString(100, 100000));
        setObject(Guid.newGuid());
    }

    /* --- Setters & Getters --- */

    /**
     * @return the guid
     */
    public Guid getGuid() {
        return guid;
    }

    /**
     * @param guid
     *            the guid to set
     */
    public void setGuid(Guid guid) {
        this.guid = guid;
    }

    /**
     * @return the string
     */
    public String getString() {
        return string;
    }

    /**
     * @param string
     *            the string to set
     */
    public void setString(String string) {
        this.string = string;
    }

    /**
     * @return the object
     */
    public Object getObject() {
        return object;
    }

    /**
     * @param object
     *            the object to set
     */
    public void setObject(Object object) {
        this.object = object;
    }

    /* --- Json helper methods --- */

    /**
     * Turns this POJO instance into a JSON form of string.
     *
     * @param stripSpaces
     *            Should the string (field) strip all spaces or not.
     * @return The {@link String} which has the JSON representation of the POJO instance.
     */
    public String toJsonForm(boolean stripSpaces) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"integer\":");
        sb.append(integer);
        sb.append(",\"object\":[\"");
        sb.append(getObject().getClass().getName());
        sb.append("\",{\"uuid\":\"");
        sb.append(getObject().toString());
        sb.append("\"}],\"string\":\"");
        String string = getString().replace("\\", "\\\\").replace("\"", "\\\"");
        sb.append(stripSpaces ? string.replaceAll("\\s", "") : string);
        sb.append("\",\"guid\":[\"");
        sb.append(getGuid().getClass().getName());
        sb.append("\",{\"uuid\":\"");
        sb.append(getGuid().toString());
        sb.append("\"}]}");

        return sb.toString();
    }

    /* --- Equals & hashcode implementation --- */

    @Override
    public int hashCode() {
        return Objects.hash(
                guid,
                integer,
                object,
                string
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof JsonSerializablePojo)) {
            return false;
        }
        JsonSerializablePojo other = (JsonSerializablePojo) obj;
        return Objects.equals(guid, other.guid)
                && Objects.equals(integer, other.integer)
                && Objects.equals(object, other.object)
                && Objects.equals(string, other.string);
    }

}
