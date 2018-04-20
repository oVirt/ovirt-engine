package org.ovirt.engine.ui.extension;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

/**
 * Handles serialization of {@code java.lang.Object} instances transferred as part of GWT RPC
 * request payloads.
 * <p>
 * Each type used in GWT RPC interface must meet the following
 * <a href="http://www.gwtproject.org/doc/latest/tutorial/RPC.html#serialize">serialization rules</a>:
 * <ul>
 *  <li>implements {@linkplain java.io.Serializable Serializable} or
 *  {@linkplain com.google.gwt.user.client.rpc.IsSerializable IsSerializable} interface
 *  <li>has a default (zero argument) constructor with any access modifier
 *  <li>its non-final, non-transient instance fields are themselves serializable
 * </ul>
 */
public final class ObjectSerializer {

    // Core Java types supported by GWT RPC protocol.
    private static final String TYPE_STRING = "java.lang.String";
    private static final String TYPE_INTEGER = "java.lang.Integer";
    private static final String TYPE_BOOLEAN = "java.lang.Boolean";
    private static final String TYPE_DOUBLE = "java.lang.Double";
    private static final String TYPE_FLOAT = "java.lang.Float";
    private static final String TYPE_CHARACTER = "java.lang.Character";
    private static final String TYPE_SHORT = "java.lang.Short";
    private static final String TYPE_BYTE = "java.lang.Byte";

    // GWT RPC compatible Object type.
    private static final String TYPE_OBJECT = "<Object>";

    /**
     * {@link List} classes to serialize as {@link ArrayList} due to their GWT RPC incompatibility.
     */
    private static final Set<String> SPECIAL_LIST_CLASSES = new HashSet<>();

    static {
        // missing no-arg constructor
        SPECIAL_LIST_CLASSES.add("java.util.Arrays$ArrayList");
        // missing no-arg constructor, doesn't implement java.io.Serializable
        SPECIAL_LIST_CLASSES.add("java.util.ArrayList$SubList");
        // missing no-arg constructor, doesn't implement java.io.Serializable
        SPECIAL_LIST_CLASSES.add("java.util.SubList");
        // missing no-arg constructor, doesn't implement java.io.Serializable
        SPECIAL_LIST_CLASSES.add("java.util.RandomAccessSubList");
    }

    /**
     * Serialize (write) object into the stream.
     */
    public static void serialize(SerializationStreamWriter streamWriter, Object instance)
            throws SerializationException {
        // Instance is null, write a null string into the stream.
        if (instance == null) {
            streamWriter.writeString(null);
            return;
        }

        // TODO(vs) implement mechanism to detect unexpected types, which would
        // allow us to reduce the total amount of types visible to GWT compiler
        // and consequently reduce GWT-generated JavaScript footprint
        String className = instance.getClass().getName();

        // Write two objects: type (string) and value (depending on type).
        if (instance instanceof String) {
            streamWriter.writeString(TYPE_STRING);
            streamWriter.writeString((String) instance);
        } else if (instance instanceof Integer) {
            streamWriter.writeString(TYPE_INTEGER);
            streamWriter.writeInt((Integer) instance);
        } else if (instance instanceof Boolean) {
            streamWriter.writeString(TYPE_BOOLEAN);
            streamWriter.writeBoolean((Boolean) instance);
        } else if (instance instanceof Double) {
            streamWriter.writeString(TYPE_DOUBLE);
            streamWriter.writeDouble((Double) instance);
        } else if (instance instanceof Float) {
            streamWriter.writeString(TYPE_FLOAT);
            streamWriter.writeFloat((Float) instance);
        } else if (instance instanceof Character) {
            streamWriter.writeString(TYPE_CHARACTER);
            streamWriter.writeChar((Character) instance);
        } else if (instance instanceof Short) {
            streamWriter.writeString(TYPE_SHORT);
            streamWriter.writeShort((Short) instance);
        } else if (instance instanceof Byte) {
            streamWriter.writeString(TYPE_BYTE);
            streamWriter.writeByte((Byte) instance);
        } else if (SPECIAL_LIST_CLASSES.contains(className)) {
            // Handle special (GWT RPC incompatible) classes.
            streamWriter.writeString(TYPE_OBJECT);
            streamWriter.writeObject(new ArrayList((List) instance));
        } else {
            // Fall through to default Object serialization.
            streamWriter.writeString(TYPE_OBJECT);
            streamWriter.writeObject(instance);
        }
    }

    /**
     * Deserialize (read) object from the stream.
     */
    public static Object deserialize(SerializationStreamReader streamReader)
            throws SerializationException {
        String serializedAsType = streamReader.readString();

        // Instance is null.
        if (serializedAsType == null) {
            return null;
        }

        // Instance is not null, read its value.
        if (TYPE_STRING.equals(serializedAsType)) {
            return streamReader.readString();
        } else if (TYPE_INTEGER.equals(serializedAsType)) {
            return streamReader.readInt();
        } else if (TYPE_BOOLEAN.equals(serializedAsType)) {
            return streamReader.readBoolean();
        } else if (TYPE_DOUBLE.equals(serializedAsType)) {
            return streamReader.readDouble();
        } else if (TYPE_FLOAT.equals(serializedAsType)) {
            return streamReader.readFloat();
        } else if (TYPE_CHARACTER.equals(serializedAsType)) {
            return streamReader.readChar();
        } else if (TYPE_SHORT.equals(serializedAsType)) {
            return streamReader.readShort();
        } else if (TYPE_BYTE.equals(serializedAsType)) {
            return streamReader.readByte();
        } else {
            // Fall through to default Object deserialization.
            return streamReader.readObject();
        }
    }

}
