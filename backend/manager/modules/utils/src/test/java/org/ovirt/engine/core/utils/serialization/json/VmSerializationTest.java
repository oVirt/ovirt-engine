package org.ovirt.engine.core.utils.serialization.json;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.VM;

public class VmSerializationTest {

    private VM vm = new VM();

    @Test
    public void serializable() {
        String serialized = new JsonObjectSerializer().serialize(vm);
        VM deserialized = new JsonObjectDeserializer().deserialize(serialized, VM.class);
        assertEquals(vm, deserialized);
    }
}
