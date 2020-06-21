package org.ovirt.engine.core.common.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Version;

public class MDevTypesUtilsTest {

    @Test
    public void isMdevDisplayOnNoDisplayFirst() {
        isMdevDisplayOn("nodisplay,nvidia-11,nvidia-12", false);
    }

    @Test
    public void isMdevDisplayOnNoDisplayLast() {
        isMdevDisplayOn("nvidia-11,nvidia-12,nodisplay", true);
    }

    @Test
    public void isMdevDisplayOnNoNoDisplay() {
        isMdevDisplayOn("nvidia-11,nvidia-12", true);
    }

    @Test
    public void isMdevDisplayOnEmptyMDevs() {
        isMdevDisplayOn("", false);
    }

    @Test
    public void isMdevDisplayOnNoMDevs() {
        isMdevDisplayOn(null, false);
    }

    @Test
    public void isMdevDisplayOnYesUnsupportedVersion() {
        isMdevDisplayOn("nodisplay,nvidia-11,nvidia-12", Version.v4_2, false);
    }

    @Test
    public void isMdevDisplayOnNoUnsupportedVersion() {
        isMdevDisplayOn("nvidia-11,nvidia-12", Version.v4_2, false);
    }

    @Test
    public void parseCustomPropertiesWithNodisplayFirst() {
        parseCustomProperties("nodisplay,nvidia-11,nvidia-12", "nvidia-11", "nvidia-12");
    }

    @Test
    public void parseCustomPropertiesWithNodisplayLast() {
        parseCustomProperties("nvidia-11,nvidia-12,nodisplay", "nvidia-11", "nvidia-12", "nodisplay");
    }

    @Test
    public void parseCustomPropertiesWithoutNodisplay() {
        parseCustomProperties("nvidia-11,nvidia-12", "nvidia-11", "nvidia-12");
    }

    @Test
    public void parseCustomPropertiesEmptyMDevs() {
        parseCustomProperties("  ");
    }

    @Test
    public void parseCustomPropertiesNoDisplayUnsupportedVersion() {
        parseCustomProperties("nodisplay,nvidia-11,nvidia-12", Version.v4_2, "nodisplay", "nvidia-11", "nvidia-12");
    }

    @Test
    public void parseCustomPropertiesUnsupportedVersion() {
        parseCustomProperties("nodisplay,nvidia-11,nvidia-12", Version.v4_2, "nodisplay", "nvidia-11", "nvidia-12");
    }

    @Test
    public void parseCustomPropertiesNoMDevs() {
        parseCustomProperties(null);
    }

    private void isMdevDisplayOn(String customProperties, boolean expected) {
        isMdevDisplayOn(customProperties, Version.v4_3, expected);
    }

    private void isMdevDisplayOn(String customProperties, Version version, boolean expected) {
        VM vm = new VM();
        vm.setCustomProperties(createCustomProperties(customProperties));
        vm.setCustomCompatibilityVersion(version);

        assertThat(MDevTypesUtils.isMdevDisplayOn(vm), is(expected));
    }

    private void parseCustomProperties(String customProperties, String...expected) {
        parseCustomProperties(customProperties, Version.v4_3, expected);
    }

    private void parseCustomProperties(String customProperties, Version version, String...expected) {
        VM vm = new VM();
        vm.setCustomProperties(createCustomProperties(customProperties));
        vm.setCustomCompatibilityVersion(version);

        assertThat(MDevTypesUtils.getMDevTypes(vm), is(Arrays.asList(expected)));
    }

    private String createCustomProperties(String mdevProperties) {
        if (mdevProperties == null) {
            return String.format("key=value;key2=value");
        }
        return String.format("key=value;mdev_type=%s;key2=value", mdevProperties);
    }
}
