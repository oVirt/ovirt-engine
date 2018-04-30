package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringContains.containsString;

import org.junit.jupiter.api.Test;

public class SysprepHandlerTest {

    private String sysprepFile = "<component name=\"Microsoft-Windows-Shell-Setup\" processorArchitecture=\"amd64\" publicKeyToken=\"31bf3856ad364e35\" language=\"neutral\" versionScope=\"nonSxS\" xmlns:wcm=\"http://schemas.microsoft.com/WMIConfig/2002/State\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
            "            <ComputerName><![CDATA[$ComputerName$]]></ComputerName>\n" +
            "            <RegisteredOrganization><![CDATA[$OrgName$]]></RegisteredOrganization>\n" +
            "            <RegisteredOwner>User</RegisteredOwner>\n" +
            "            <ProductKey><![CDATA[$ProductKey$]]></ProductKey>\n" +
            "        </component>\n" +
            "<component name=\"Microsoft-Windows-Setup\" processorArchitecture=\"amd64\" publicKeyToken=\"31bf3856ad364e35\" language=\"neutral\" versionScope=\"nonSxS\" xmlns:wcm=\"http://schemas.microsoft.com/WMIConfig/2002/State\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
            "            <UserData>\n" +
            "                <ProductKey>\n" +
            "                    <Key><![CDATA[$ProductKey$]]></Key>\n" +
            "                    <WillShowUI>Never</WillShowUI>\n" +
            "                </ProductKey>\n" +
            "                <AcceptEula>true</AcceptEula>\n" +
            "                <Organization><![CDATA[$OrgName$]]></Organization>\n" +
            "                <FullName>User</FullName>\n" +
            "            </UserData>\n" +
            "        </component>";

    @Test
    public void replaceProductKeyBothEmpty() {
        String res = SysprepHandler.replaceProductKey("", "", false);
        assertThat(res, is(equalTo("")));
    }

    @Test
    public void replaceProductKeyDefinedKeyShouldBeReplaced() {
        String res = SysprepHandler.replaceProductKey(sysprepFile, "someKey", false);
        assertThat(res, containsString("<Key><![CDATA[someKey]]></Key>"));
    }

    @Test
    public void replaceProductKeyEmptyKeyShouldRemoveTheWholeSection() {
        String res = SysprepHandler.replaceProductKey(sysprepFile, "", false);
        assertThat(res, not(containsString("<ProductKey>")));
        assertThat(res, not(containsString("</ProductKey>")));
    }

    @Test
    public void replaceProductKeyEmptyKeyShouldNotRemoveTheWholeSectionForCustomScript() {
        String res = SysprepHandler.replaceProductKey(sysprepFile, "", true);
        assertThat(res, containsString("<ProductKey>"));
        assertThat(res, containsString("</ProductKey>"));
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
