package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockedConfig;

public class VmCommandTest {

    // Concrete implementation of abstract VmCommand for testing
    private static class TestVmCommand extends VmCommand<VmOperationParameterBase> {
        public TestVmCommand(VmOperationParameterBase params, CommandContext context) {
            super(params, context);
        }
        // Expose protected method for testing
        public boolean publicCheckPayload(VmPayload payload) {
            return checkPayload(payload);
        }

        public List<String> publicExtractVariableDeclarations(Iterable<String> msgs) {
            return extractVariableDeclarations(msgs);
        }
    }

    private TestVmCommand vmCommand;
    private VmOperationParameterBase params;

    @BeforeEach
    public void setUp() {
        params = mock(VmOperationParameterBase.class);
        CommandContext context = CommandContext.createContext("");
        vmCommand = new TestVmCommand(params, context);
    }

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.PayloadSize, 2048));
    }

    @Test
    public void testCheckPayloadInvalidDeviceType() {
        VmPayload payload = new VmPayload();
        payload.setDeviceType(VmDeviceType.DISK); // Not CDROM or FLOPPY

        assertFalse(vmCommand.publicCheckPayload(payload));
        // Verify that the specific error message was added (depends on your CommandBase implementation)
        assertTrue(vmCommand.getReturnValue().getValidationMessages().contains(EngineMessage.VMPAYLOAD_INVALID_PAYLOAD_TYPE.toString()));
    }

    @Test
    public void testCheckPayloadNullContent() {
        VmPayload payload = new VmPayload();
        payload.getFiles().put("test.txt", null);

        assertFalse(vmCommand.publicCheckPayload(payload));
        assertTrue(vmCommand.getReturnValue().getValidationMessages().contains(EngineMessage.VMPAYLOAD_CONTENT_NULL.toString()));
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void testCheckPayloadSizeExceeded() {
        String longContent = "a".repeat(1024 * 4); // 4 Kb

        VmPayload payload = new VmPayload();
        payload.getFiles().put("test.txt", longContent);

        assertFalse(vmCommand.publicCheckPayload(payload));
        assertTrue(vmCommand.getReturnValue().getValidationMessages().contains(EngineMessage.VMPAYLOAD_SIZE_EXCEEDED.toString()));

        List<String> extracted =
            vmCommand.publicExtractVariableDeclarations(vmCommand.getReturnValue().getValidationMessages());

        Map<String, String> vars = extracted.stream()
            .filter(s -> s.startsWith("$"))
            .map(s -> s.substring(1).split(" ", 2))
            .collect(Collectors.toMap(a -> a[0], a -> a[1]));

        assertEquals("2", vars.get("maxSize"));
        assertEquals("4", vars.get("currentSize"));
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void testCheckPayloadValid() {
        // Case: Below limit, not null
        VmPayload payload = new VmPayload();
        payload.getFiles().put("test.txt", "Valid content");
        assertTrue(vmCommand.publicCheckPayload(payload));

        // Case: Exactly at limit
        VmPayload exactPayload = new VmPayload();
        exactPayload.getFiles().put("test.txt", "a".repeat(2048));
        assertTrue(vmCommand.publicCheckPayload(exactPayload));

        // Check no validation messages were added
        assertEquals(0, vmCommand.getReturnValue().getValidationMessages().size());
    }
}
