package org.ovirt.engine.core.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class ObjectStreamDeserializer implements Deserializer {

    public ObjectStreamDeserializer() {
    }
    @Override
    public <T extends Serializable> T deserialize(Object source, Class<T> type) throws SerializationExeption {
        try {
            ObjectInputStream objectInputStream = null;
            if (source instanceof InputStream) {
                objectInputStream = createFromInputStream((InputStream)source);
            } else if (source instanceof byte[]) {
                objectInputStream = createFromByteArray((byte[])source);
            } else if (source instanceof String) {
                byte[] bytes = ((String)source).getBytes("UTF-8");
                objectInputStream = createFromByteArray(bytes);
            } else throw new UnsupportedOperationException("deserialize can deserialize only if source is InputStream, or a array of bytes, or String");
            Object object = objectInputStream.readObject();
            return type.cast(object);
        } catch (IOException e) {
            throw new SerializationExeption(e);
        } catch (ClassNotFoundException e) {
            throw new SerializationExeption(e);
        }
    }

    private ObjectInputStream createFromInputStream(InputStream source) throws IOException {
        return new ObjectInputStream(source);
    }

    private ObjectInputStream createFromByteArray(byte[] source) throws IOException {
        return createFromInputStream(new ByteArrayInputStream(source));
    }

}
