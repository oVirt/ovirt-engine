package org.ovirt.engine.core.common.queries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.IVdcQueryable;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

public class VdcQueryReturnValue_CustomFieldSerializer {

    public static void deserialize(SerializationStreamReader streamReader,
            VdcQueryReturnValue instance) throws SerializationException {
        instance.setSucceeded(streamReader.readBoolean());
        instance.setExceptionString(streamReader.readString());

        String type = streamReader.readString();
        Object returnValue;
        String deserializedAs = null;

        if (type == null) {
            returnValue = null;
        } else if (type.equals("java.lang.String")) {
            deserializedAs = "java.util.String";
            instance.setReturnValue(streamReader.readString());
        } else if (type.equals("java.lang.Integer")) {
            deserializedAs = "java.lang.Integer";
            instance.setReturnValue(streamReader.readInt());
        } else if (type.equals("java.lang.Boolean")) {
            deserializedAs = "java.lang.Boolean";
            instance.setReturnValue(streamReader.readBoolean());
        } else if (type.equals("java.lang.Double")) {
            deserializedAs = "java.lang.Double";
            instance.setReturnValue(streamReader.readDouble());
        } else if (type.equals("java.lang.Float")) {
            deserializedAs = "java.lang.Float";
            instance.setReturnValue(streamReader.readFloat());
        } else if (type.equals("java.lang.Character")) {
            deserializedAs = "java.lang.Character";
            instance.setReturnValue(streamReader.readChar());
        } else if (type.equals("java.lang.Short")) {
            deserializedAs = "java.lang.Short";
            instance.setReturnValue(streamReader.readShort());
        } else if (type.equals("java.lang.Byte")) {
            deserializedAs = "java.lang.Byte";
            instance.setReturnValue(streamReader.readByte());
        } else if (type.equals("java.util.List")) {
            deserializedAs = "java.util.List";
            instance.setReturnValue(streamReader.readObject());
        } else if (type.equals("java.util.Map")) {
            deserializedAs = "java.util.Map";
            instance.setReturnValue(streamReader.readObject());
        } else if (type.equals("java.util.Set")) {
            deserializedAs = "java.util.Set";
            instance.setReturnValue(streamReader.readObject());
        } else if (type.equals("IVdcQueryable")) {
            deserializedAs = "IVdcQueryable";
            instance.setReturnValue(streamReader.readObject());
        } else if (type.equals("UNKNOWN")) {
            deserializedAs = "UNKNOWN";
            instance.setReturnValue(streamReader.readObject());
        }
    }

    public static VdcQueryReturnValue instantiate(
            SerializationStreamReader streamReader)
            throws SerializationException {
        return new VdcQueryReturnValue();
    }

    public static void serialize(SerializationStreamWriter streamWriter,
            VdcQueryReturnValue instance) throws SerializationException {
        streamWriter.writeBoolean(instance.getSucceeded());
        streamWriter.writeString(instance.getExceptionString());

        Object returnValue = instance.getReturnValue();

        if (returnValue == null) {
            streamWriter.writeString(null);
            return;
        }
        String className = returnValue.getClass().getName();
        String serializedAs = null;
        if (returnValue instanceof String) {
            serializedAs = "java.lang.String";
            streamWriter.writeString(serializedAs);
            streamWriter.writeString((String) instance.getReturnValue());
        } else if (returnValue instanceof Integer) {
            serializedAs = "java.lang.Integer";
            streamWriter.writeString(serializedAs);
            streamWriter.writeInt((Integer) instance.getReturnValue());
        } else if (returnValue instanceof Boolean) {
            serializedAs = "java.lang.Boolean";
            streamWriter.writeString(serializedAs);
            streamWriter.writeBoolean((Boolean) instance.getReturnValue());
        } else if (returnValue instanceof Double) {
            serializedAs = "java.lang.Double";
            streamWriter.writeString(serializedAs);
            streamWriter.writeDouble((Double) instance.getReturnValue());
        } else if (returnValue instanceof Float) {
            serializedAs = "java.lang.Float";
            streamWriter.writeString(serializedAs);
            streamWriter.writeFloat((Float) instance.getReturnValue());
        } else if (returnValue instanceof Character) {
            serializedAs = "java.lang.Character";
            streamWriter.writeString(serializedAs);
            streamWriter.writeChar((Character) instance.getReturnValue());
        } else if (returnValue instanceof Short) {
            serializedAs = "java.lang.Short";
            streamWriter.writeString(serializedAs);
            streamWriter.writeShort((Short) instance.getReturnValue());
        } else if (returnValue instanceof Byte) {
            serializedAs = "java.lang.Byte";
            streamWriter.writeString(serializedAs);
            streamWriter.writeByte((Byte) instance.getReturnValue());
        } else if (className.equals("java.util.ArrayList$SubList") || className.equals("java.util.RandomAccessSubList")
                || className.equals("java.util.SubList")) {
            // SubList and RandomAccessSubList need to be serialized as List since these object aren't implementing
            // java.io.Serializable.
            serializedAs = "java.util.List";
            streamWriter.writeString(serializedAs);
            streamWriter.writeObject(new ArrayList((List) instance.getReturnValue()));
        } else if (returnValue instanceof List) {
            serializedAs = "java.util.List";
            streamWriter.writeString(serializedAs);
            streamWriter.writeObject(instance.getReturnValue());
        } else if (returnValue instanceof Map) {
            serializedAs = "java.util.Map";
            streamWriter.writeString(serializedAs);
            streamWriter.writeObject(instance.getReturnValue());
        } else if (returnValue instanceof Set) {
            serializedAs = "java.util.Set";
            streamWriter.writeString(serializedAs);
            streamWriter.writeObject(instance.getReturnValue());
        } else if (returnValue instanceof IVdcQueryable) {
            serializedAs = "IVdcQueryable";
            streamWriter.writeString(serializedAs);
            streamWriter.writeObject(instance.getReturnValue());
        } else {
            serializedAs = "UNKNOWN";
            streamWriter.writeString(serializedAs);
            streamWriter.writeObject(instance.getReturnValue());
        }
    }

    private static String dumpTypeInfo(boolean serialize, String serializedAs,
            Object obj) {
        StringBuffer dumpInfo = new StringBuffer();

        if (serialize) {
            dumpInfo.append("Serialize: ");
        } else {
            dumpInfo.append("Deserialize: ");
        }

        dumpInfo.append("VdcQueryReturnValue: '" + obj + "' serialized as ["
                + serializedAs + "] ");

        if (obj != null) {
            dumpInfo.append("class: [" + obj.getClass().getName() + "]");
        } else {
            dumpInfo.append(" - Value is null");
        }

        return dumpInfo.toString();
    }

}
