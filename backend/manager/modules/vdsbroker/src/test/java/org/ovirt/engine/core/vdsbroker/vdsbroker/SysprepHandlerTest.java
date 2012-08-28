package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.config.IConfigUtilsInterface;

public class SysprepHandlerTest {

    @Before
    public void setup() {
        IConfigUtilsInterface configUtils = Mockito.mock(IConfigUtilsInterface.class);
        when(configUtils.GetValue(ConfigValues.AdUserPassword, Config.DefaultConfigurationVersion)).thenReturn(new HashMap<String, String>());
        when(configUtils.GetValue(ConfigValues.AdUserName, Config.DefaultConfigurationVersion)).thenReturn("");
        Config.setConfigUtils(configUtils);
    }

    @Test
    public void replace_emptyBuilder() {
        runAndCheck(new StringBuilder(), "a", "b", "");
    }

    @Test
    public void replace_patternNotPresent() {
        runAndCheck(sb("abcd"), "X", "Y", "abcd");
    }

    @Test
    public void replace_valueNotContainsDollar() {
        runAndCheck(sb("AdminPassword=$AdminPassword$"), "$AdminPassword$", "AAA", "AdminPassword=AAA");
    }

    @Test
    public void replace_keyNotContainsDollar() {
        runAndCheck(sb("AdminPassword=someKey"), "someKey", "AAA", "AdminPassword=AAA");
    }

    @Test
    public void replace_valueContainsDollar() {
        runAndCheck(sb("AdminPassword=$AdminPassword$"),
                "$AdminPassword$",
                "$A$AA$",
                "AdminPassword=$A$AA$");
    }

    @Test
    public void replace_callReplaceTwoTimes() {
        String text = "AdminName=$AdminName$ AdminPassword=$AdminPassword$";
        StringBuilder firstPart =
                runAndCheck(sb(text),
                        "$AdminPassword$",
                        "$A$AA$",
                        "AdminName=$AdminName$ AdminPassword=$A$AA$");
        runAndCheck(firstPart, "$AdminName$", "$B$BB$", "AdminName=$B$BB$ AdminPassword=$A$AA$");
    }

    @Test
    public void replace_callReplaceTwoOccurrences() {
        runAndCheck(sb("AdminName=$AdminName$ AdminPassword=$AdminName$"),
                "$AdminName$",
                "$B$BB$",
                "AdminName=$B$BB$ AdminPassword=$B$BB$");
    }

    // this is here just to simulate perfectly the way how the SysprepHandler creates the StringBuilder
    private StringBuilder sb(String string) {
        StringBuilder builder = new StringBuilder();
        builder.append(string);
        return builder;
    }

    private StringBuilder runAndCheck(StringBuilder original, String pattern, String value, String expected) {
        StringBuilder res = SysprepHandler.replace(original, pattern, value);
        assertThat(res.toString(), is(equalTo(expected)));
        return res;
    }
}
