package org.ovirt.engine.core.utils.customprop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.ovirt.engine.core.common.utils.customprop.PropertiesUtilsTestHelper.validateFailure;
import static org.ovirt.engine.core.common.utils.customprop.PropertiesUtilsTestHelper.validatePropertyMap;
import static org.ovirt.engine.core.common.utils.customprop.PropertiesUtilsTestHelper.validatePropertyValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.utils.customprop.ValidationError;
import org.ovirt.engine.core.common.utils.customprop.ValidationFailureReason;
import org.ovirt.engine.core.common.utils.exceptions.InitializationException;
import org.ovirt.engine.core.compat.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for custom device properties handling
 */
public class DevicePropertiesUtilsTest {
    private static final Logger log = LoggerFactory.getLogger(DevicePropertiesUtilsTest.class);

    /**
     * Initializes {@code DevicePropertiesUtils} instance for test
     *
     * @return initialized instance
     */
    private DevicePropertiesUtils mockDevicePropertiesUtils() {
        return mockDevicePropertiesUtils("{type=disk;prop={bootable=^(true|false)$}};"
                + "{type=interface;prop={speed=^([0-9]{1,5})$;duplex=^(full|half)$;debug=([a-z0-9A-Z]*)$}}");
    }

    /**
     * Initializes {@code DevicePropertiesUtils} instance with specified custom device properties for test
     *
     * @param customDevPropSpec
     *            custom device properties specification
     * @return initialized instance
     */
    private DevicePropertiesUtils mockDevicePropertiesUtils(String customDevPropSpec) {
        DevicePropertiesUtils mockedUtils = spy(new DevicePropertiesUtils());
        doReturn(customDevPropSpec).
                when(mockedUtils)
                .getCustomDeviceProperties(Version.getLast());
        doReturn(Collections.singleton(Version.getLast())).
                when(mockedUtils)
                .getSupportedClusterLevels();
        try {
            mockedUtils.init();
        } catch (InitializationException ex) {
            log.error("Error initializing DevicePropertiesUtils instance", ex);
        }
        return mockedUtils;
    }

    /**
     * Tries to set property with no value (value is invalid due provided REGEX) to a device with supported version
     */
    @Test
    public void invalidPropertyValue1() {
        DevicePropertiesUtils utils = mockDevicePropertiesUtils();
        Map<String, String> map = new HashMap<>();
        map.put("bootable", null);

        List<ValidationError> errors = utils.validateProperties(Version.getLast(), VmDeviceGeneralType.DISK, map);

        validateFailure(errors, ValidationFailureReason.INCORRECT_VALUE);
    }

    /**
     * Tries to set invalid property value to a device with supported version
     */
    @Test
    public void invalidPropertyValue2() {
        DevicePropertiesUtils utils = mockDevicePropertiesUtils();
        List<ValidationError> errors =
                utils.validateProperties(Version.getLast(),
                        VmDeviceGeneralType.DISK,
                        utils.convertProperties("bootable=x"));

        validateFailure(errors, ValidationFailureReason.INCORRECT_VALUE);
    }

    /**
     * Tries to set valid property to a device (device type differs from the type the properties are defined for) with
     * supported version
     */
    @Test
    public void validPropertyInvalidDeviceType() {
        DevicePropertiesUtils utils = mockDevicePropertiesUtils();
        List<ValidationError> errors =
                utils.validateProperties(Version.getLast(),
                        VmDeviceGeneralType.DISK,
                        utils.convertProperties("speed=10;duplex=half"));

        validateFailure(errors, ValidationFailureReason.KEY_DOES_NOT_EXIST);
    }

    /**
     * Tries to set valid property to unknown device type with supported version
     */
    @Test
    public void validPropertyUnknownDeviceType() {
        DevicePropertiesUtils utils = mockDevicePropertiesUtils();
        List<ValidationError> errors =
                utils.validateProperties(Version.getLast(),
                        VmDeviceGeneralType.UNKNOWN,
                        utils.convertProperties("speed=10;duplex=half"));

        validateFailure(errors, ValidationFailureReason.INVALID_DEVICE_TYPE);
    }

    /**
     * Tries to set valid properties to valid device type with supported version
     */
    @Test
    public void validPropertyValidDeviceType() {
        DevicePropertiesUtils utils = mockDevicePropertiesUtils();
        List<ValidationError> errors =
                utils.validateProperties(Version.getLast(),
                        VmDeviceGeneralType.INTERFACE,
                        utils.convertProperties("speed=10;duplex=half;debug="));

        assertTrue(errors.isEmpty());
    }

    /**
     * Tries to validate valid custom device properties specification
     */
    @Test
    public void validCustomDevPropSpec() {
        String customDevPropSpec = "{type=disk;prop={bootable=^(true|false)$}};"
                + "{type=interface;prop={speed=[0-9]{1,5};duplex=^(full|half)$;debug=([a-z0-9A-Z]*)$}};"
                + "{type=video;prop={turned_on=^(true|false)$}};"
                + "{type=sound;prop={volume=[0-9]{1,2}}};"
                + "{type=controller;prop={hotplug=^(true|false)$}};"
                + "{type=balloon;prop={max_size=[0-9]{1,15}}};"
                + "{type=channel;prop={auth_type=^(plain|md5|kerberos)$}};"
                + "{type=redir;prop={max_len=[0-9]{1,15}}};"
                + "{type=console;prop={type=^(text|vnc)$}};"
                + "{type=smartcard;prop={version=([1-9]{1}).([0-9]{1})}}";
        DevicePropertiesUtils utils = DevicePropertiesUtils.getInstance();

        assertTrue(utils.isDevicePropertiesDefinitionValid(customDevPropSpec));
    }

    /**
     * Tries to validate valid custom device properties specification with special characters
     */
    @Test
    public void validCustomDevPropSpecWithSpecChars() {
        String customDevPropSpec =
                "{type=disk;prop={bootable=[\\@\\#\\$\\%\\^\\&\\*\\(\\)\\{\\}\\:\\<\\>\\,\\.\\?\\[\\]]?}};";
        DevicePropertiesUtils utils = DevicePropertiesUtils.getInstance();

        assertTrue(utils.isDevicePropertiesDefinitionValid(customDevPropSpec));
    }

    /**
     * Tries to validate custom device properties specification with invalid device type
     */
    @Test
    public void customDevPropSpecWithInvalidType() {
        String customDevPropSpec = "{type=diks;prop={bootable=^(true|false)$}}";
        DevicePropertiesUtils utils = DevicePropertiesUtils.getInstance();

        assertFalse(utils.isDevicePropertiesDefinitionValid(customDevPropSpec));
    }

    /**
     * Tries to validate custom device properties specification with invalid type key
     */
    @Test
    public void customDevPropSpecWithInvalidTypeKey() {
        String customDevPropSpec = "{typ=disk;prop={bootable=^(true|false)$}}";
        DevicePropertiesUtils utils = DevicePropertiesUtils.getInstance();

        assertFalse(utils.isDevicePropertiesDefinitionValid(customDevPropSpec));
    }

    /**
     * Tries to validate custom device properties specification with incomplete brackets
     */
    @Test
    public void customDevPropSpecWithIncompleteBrackets() {
        String customDevPropSpec = "{type=disk;prop={bootable=^(true|false)$}";
        DevicePropertiesUtils utils = DevicePropertiesUtils.getInstance();

        assertFalse(utils.isDevicePropertiesDefinitionValid(customDevPropSpec));
    }

    /**
     * Tries to validate custom device properties specification with invalid syntax
     */
    @Test
    public void customDevPropSpecWithInvalidTypePropDelimiter() {
        String customDevPropSpec = "{type=disk,prop={bootable=^(true|false)$}}";
        DevicePropertiesUtils utils = DevicePropertiesUtils.getInstance();

        assertFalse(utils.isDevicePropertiesDefinitionValid(customDevPropSpec));
    }

    /**
     * Tries to validate custom device properties specification with invalid property key name
     */
    @Test
    public void customDevPropSpecWithInvalidPropertyKeyName() {
        String customDevPropSpec = "{type=disk;prop={boot*able=^(true|false)$}}";
        DevicePropertiesUtils utils = DevicePropertiesUtils.getInstance();

        assertFalse(utils.isDevicePropertiesDefinitionValid(customDevPropSpec));
    }

    /**
     * Tries to validate custom device properties specification with invalid property value
     */
    @Test
    public void customDevPropSpecWithInvalidPropertyValue() {
        String customDevPropSpec = "{type=disk;prop={bootable=^(true;false)$}}";
        DevicePropertiesUtils utils = DevicePropertiesUtils.getInstance();

        assertFalse(utils.isDevicePropertiesDefinitionValid(customDevPropSpec));
    }

    /**
     * Tries to validate custom device properties specification with property value containing invalid characters
     */
    @Test
    public void customDevPropSpecWithInvalidCharsPropertyValue() {
        String customDevPropSpec = "{type=disk;prop={bootable=([{};])?}}";
        DevicePropertiesUtils utils = DevicePropertiesUtils.getInstance();

        assertFalse(utils.isDevicePropertiesDefinitionValid(customDevPropSpec));
    }

    /**
     * Tries to validate if a valid custom device properties specification have been parsed correctly
     */
    @Test
    public void parseValidCustomDevPropSpec() {
        Map<String, String> devProp;

        String customDevPropSpec = "{type=disk;prop={bootable=^(true|false)$}};"
                + "{type=interface;prop={speed=[0-9]{1,5};duplex=^(full|half)$}};"
                + "{type=video;prop={turned_on=^(true|false)$}};"
                + "{type=sound;prop={volume=[0-9]{1,2}}};"
                + "{type=controller;prop={hotplug=^(true|false)$}};"
                + "{type=balloon;prop={max_size=[0-9]{1,15}}};"
                + "{type=channel;prop={auth_type=^(plain|md5|kerberos)$}};"
                + "{type=redir;prop={max_len=[0-9]{1,15}}};"
                + "{type=console;prop={type=^(text|vnc)$;prop=\\{\\}}};"
                + "{type=smartcard;prop={spec_chars=[\\@\\#\\$\\%\\^\\&\\*\\(\\)\\{\\}\\:\\<\\>\\,\\.\\?\\[\\]]?}}";

        // mock DevicePropertiesUtils
        DevicePropertiesUtils utils = mockDevicePropertiesUtils(customDevPropSpec);

        // test if custom properties spec is valid
        assertTrue(utils.isDevicePropertiesDefinitionValid(customDevPropSpec));

        // test parsed properties
        assertEquals(10, utils.getDeviceTypesWithProperties(Version.getLast()).size());

        // test disk properties
        devProp = utils.getDeviceProperties(Version.getLast(), VmDeviceGeneralType.DISK);
        validatePropertyMap(devProp, 1);
        validatePropertyValue(devProp, "bootable", "^(true|false)$");

        // test interface properties
        devProp = utils.getDeviceProperties(Version.getLast(), VmDeviceGeneralType.INTERFACE);
        validatePropertyMap(devProp, 2);
        validatePropertyValue(devProp, "speed", "[0-9]{1,5}");
        validatePropertyValue(devProp, "duplex", "^(full|half)$");

        // test video properties
        devProp = utils.getDeviceProperties(Version.getLast(), VmDeviceGeneralType.VIDEO);
        validatePropertyMap(devProp, 1);
        validatePropertyValue(devProp, "turned_on", "^(true|false)$");

        // test sound properties
        devProp = utils.getDeviceProperties(Version.getLast(), VmDeviceGeneralType.SOUND);
        validatePropertyMap(devProp, 1);
        validatePropertyValue(devProp, "volume", "[0-9]{1,2}");

        // test video properties
        devProp = utils.getDeviceProperties(Version.getLast(), VmDeviceGeneralType.CONTROLLER);
        validatePropertyMap(devProp, 1);
        validatePropertyValue(devProp, "hotplug", "^(true|false)$");

        // test balloon properties
        devProp = utils.getDeviceProperties(Version.getLast(), VmDeviceGeneralType.BALLOON);
        validatePropertyMap(devProp, 1);
        validatePropertyValue(devProp, "max_size", "[0-9]{1,15}");

        // test channel properties
        devProp = utils.getDeviceProperties(Version.getLast(), VmDeviceGeneralType.CHANNEL);
        validatePropertyMap(devProp, 1);
        validatePropertyValue(devProp, "auth_type", "^(plain|md5|kerberos)$");

        // test redir properties
        devProp = utils.getDeviceProperties(Version.getLast(), VmDeviceGeneralType.REDIR);
        validatePropertyMap(devProp, 1);
        validatePropertyValue(devProp, "max_len", "[0-9]{1,15}");

        // test console properties
        devProp = utils.getDeviceProperties(Version.getLast(), VmDeviceGeneralType.CONSOLE);
        validatePropertyMap(devProp, 2);
        validatePropertyValue(devProp, "type", "^(text|vnc)$");
        validatePropertyValue(devProp, "prop", "\\{\\}");

        // test smartcard properties
        devProp = utils.getDeviceProperties(Version.getLast(), VmDeviceGeneralType.SMARTCARD);
        validatePropertyMap(devProp, 1);
        validatePropertyValue(devProp, "spec_chars", "[\\@\\#\\$\\%\\^\\&\\*\\(\\)\\{\\}\\:\\<\\>\\,\\.\\?\\[\\]]?");
    }

    /**
     * Tries to validate conversion of device properties to {@code Map<String, String>}
     */
    @Test
    public void validateGetRawDeviceProperties() {
        Map<String, String> devProp;

        String customDevPropSpec = "{type=disk;prop={bootable=^(true|false)$}};"
                + "{type=interface;prop={speed=[0-9]{1,5};duplex=^(full|half)$}};";

        // mock DevicePropertiesUtils
        DevicePropertiesUtils utils = mockDevicePropertiesUtils(customDevPropSpec);

        // test if custom properties spec is valid
        assertTrue(utils.isDevicePropertiesDefinitionValid(customDevPropSpec));

        // test parsed properties
        assertEquals(2, utils.getDeviceTypesWithProperties(Version.getLast()).size());

        // test disk properties
        devProp = utils.getDeviceProperties(Version.getLast(), VmDeviceGeneralType.DISK);
        validatePropertyMap(devProp, 1);
        validatePropertyValue(devProp, "bootable", "^(true|false)$");

        // test interface properties
        devProp = utils.getDeviceProperties(Version.getLast(), VmDeviceGeneralType.INTERFACE);
        validatePropertyMap(devProp, 2);
        validatePropertyValue(devProp, "speed", "[0-9]{1,5}");
        validatePropertyValue(devProp, "duplex", "^(full|half)$");
    }
}
