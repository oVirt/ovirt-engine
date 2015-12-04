package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.IConfigUtilsInterface;

public class SysprepHandlerTest {

    @Before
    public void setup() {
        IConfigUtilsInterface configUtils = Mockito.mock(IConfigUtilsInterface.class);
        Config.setConfigUtils(configUtils);
    }

    @Test
    public void replaceEmptyBuilder() {
        runAndCheck("", "a", "b", "");
    }

    @Test
    public void replacePatternNotPresent() {
        runAndCheck("abcd", "X", "Y", "abcd");
    }

    @Test
    public void replaceValueNotContainsDollar() {
        runAndCheck("AdminPassword=$AdminPassword$", "$AdminPassword$", "AAA", "AdminPassword=AAA");
    }

    @Test
    public void replaceKeyNotContainsDollar() {
        runAndCheck("AdminPassword=someKey", "someKey", "AAA", "AdminPassword=AAA");
    }

    @Test
    public void replaceValueContainsDollar() {
        runAndCheck("AdminPassword=$AdminPassword$",
                "$AdminPassword$",
                "$A$AA$",
                "AdminPassword=$A$AA$");
    }

    @Test
    public void replaceCallReplaceTwoTimes() {
        String text = "AdminName=$AdminName$ AdminPassword=$AdminPassword$";
        String firstPart =
                runAndCheck(text,
                        "$AdminPassword$",
                        "$A$AA$",
                        "AdminName=$AdminName$ AdminPassword=$A$AA$");
        runAndCheck(firstPart, "$AdminName$", "$B$BB$", "AdminName=$B$BB$ AdminPassword=$A$AA$");
    }

    @Test
    public void replaceCallReplaceTwoOccurrences() {
        runAndCheck("AdminName=$AdminName$ AdminPassword=$AdminName$",
                "$AdminName$",
                "$B$BB$",
                "AdminName=$B$BB$ AdminPassword=$B$BB$");
    }

    private String runAndCheck(String original, String pattern, String value, String expected) {
        String res = SysprepHandler.replace(original, pattern, value);
        assertThat(res, is(equalTo(expected)));
        return res;
    }
}
