package org.ovirt.engine.core.utils.jwin32;

import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.ovirt.engine.core.compat.*;

/**
 * Unit test for simple App.
 */
public class AppTest {
    private static boolean isRunningOnWindows() {
        String osName = System.getProperty("os.name");
        return osName.contains("Windows");
    }

    /**
     * Rigourous Test :-)
     */
    /*
     * public void testApp() { IntByReference i = new IntByReference();
     *
     * assertTrue ( advapi32.INSTANCE.LogonUserA ( "TestUser", "", "TestUser", 2, 0, i ) );
     *
     * assertTrue ( advapi32.INSTANCE.LogonUserW ( new WString("TestUser"), new WString(""), new WString("TestUser"), 2,
     * advapi32.LOGON32_PROVIDER_DEFAULT, i ) ); }
     */
    public static String getAccountSid(String account) {

        if (!isRunningOnWindows()) {
            System.out.println("getAccountSid should be run on windows");
            return null;
        }

        byte[] sid;
        String retSID = "";
        IntByReference cbSid;
        PointerByReference peUse;
        IntByReference cchReferencedDomainName;
        PointerByReference stringSID = new PointerByReference();
        char[] referencedDomainName;

        cbSid = new IntByReference(jwin32.SECURITY_MAX_SID_SIZE);
        sid = new byte[cbSid.getValue()];
        cchReferencedDomainName = new IntByReference(100);
        referencedDomainName = new char[cchReferencedDomainName.getValue()];
        peUse = new PointerByReference();

        if (!jwin32.advapi32.LookupAccountNameA(
                "",
                account,
                sid,
                cbSid,
                referencedDomainName,
                cchReferencedDomainName,
                peUse
                )) {
            System.out.println("Error");
        }

        if (!jwin32.advapi32.ConvertSidToStringSidA(sid, stringSID)) {
            // ERROR
            System.out.println("!ERROR=!" + jwin32.kernel32.GetLastError() + " cbSid=" + cbSid.getValue());
        }

        retSID = stringSID.getValue().getString(0, false);
        jwin32.kernel32.LocalFree(stringSID.getValue());

        return (retSID);
    }

    @org.junit.Test
    public void testGroupsEnum() throws UnsupportedEncodingException {

        if (!isRunningOnWindows()) {
            System.out.println("testGroupsEnum should be run on windows");
            return;
        }
        int nStatus;
        PointerByReference pGI0 = new PointerByReference();
        PointerByReference pGI1 = new PointerByReference();
        IntByReference nEntriesRead = new IntByReference();
        IntByReference nTotalEntries = new IntByReference();
        IntByReference nResumeHandle = new IntByReference();

        PrintStream out = new PrintStream(System.out, true, "UTF-8");

        try {
            nStatus = jwin32.netapi32.NetLocalGroupEnum(
                    new WString(""),
                    0,
                    pGI0,
                    jwin32.MAX_PREFERRED_LENGTH,
                    nEntriesRead,
                    nTotalEntries,
                    null
                    );

            System.out.println("NetGroupEnum return=" + nStatus + " nEntriesRead=" + nEntriesRead.getValue());

            if (nStatus == jwin32.NERR_Success ||
                    nStatus == jwin32.ERROR_MORE_DATA) {
                LOCAL_GROUP_INFO_0 groupsArr = new LOCAL_GROUP_INFO_0(pGI0.getValue());
                LOCAL_GROUP_INFO_0[] groups = (LOCAL_GROUP_INFO_0[]) groupsArr.toArray(nEntriesRead.getValue());

                for (LOCAL_GROUP_INFO_0 group : groups) {
                    String retSID;
                    PointerByReference group_sid;
                    PointerByReference stringSID = new PointerByReference();
                    IntByReference nSIDSize = new IntByReference();

                    out.println("!Group name=" + group.lgrpi0_name.toString() + "!");
                    out.println("!    sid=!" + getAccountSid(group.lgrpi0_name.toString()));
                }

            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public static byte[] toByta(long data) {
        return new byte[] {
                (byte) ((data >> 56) & 0xff),
                (byte) ((data >> 48) & 0xff),
                (byte) ((data >> 40) & 0xff),
                (byte) ((data >> 32) & 0xff),
                (byte) ((data >> 24) & 0xff),
                (byte) ((data >> 16) & 0xff),
                (byte) ((data >> 8) & 0xff),
                (byte) ((data >> 0) & 0xff), };
    }

    @org.junit.Test
    public void testSidtoGuid() throws UnsupportedEncodingException {

        if (!isRunningOnWindows()) {
            System.out.println("testSidtoGuid should be run on windows");
            return;
        }

        String strSid = getAccountSid("Administrator");
        int x = 0;

        PrintStream out = new PrintStream(System.out, true, "UTF-8");

        try {
            ByteBuffer bb = ByteBuffer.allocate(16);
            bb.order(ByteOrder.LITTLE_ENDIAN);

            String[] arrSidParts = strSid.split("-");
            for (int i = 4; i < arrSidParts.length; i++) {
                bb.putInt((int) Long.parseLong(arrSidParts[i]));
            }

            Guid guid = new Guid(bb.array(), false);
            out.println(guid.toString());

        } catch (Exception e) {
            out.println("!" + e.getMessage() + "!");
            e.printStackTrace();
        }
    }

    @org.junit.Test
    public void testGroupDisplay() {
        try {

            if (!isRunningOnWindows()) {
                System.out.println("test should be run on windows");
                return;
            }

            PointerByReference pNDG = new PointerByReference();
            IntByReference nEntriesRead = new IntByReference();
            int nStatus = jwin32.netapi32.NetQueryDisplayInformation(
                    new WString(""),
                    3,
                    0,
                    100,
                    jwin32.MAX_PREFERRED_LENGTH,
                    nEntriesRead,
                    pNDG);

            System.out.println("NetQueryDisplayInformation return " + nStatus);

            if (nStatus == jwin32.NERR_Success ||
                    nStatus == jwin32.ERROR_MORE_DATA) {

                System.out.println("Returned " + nEntriesRead.getValue() + " entries ");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
