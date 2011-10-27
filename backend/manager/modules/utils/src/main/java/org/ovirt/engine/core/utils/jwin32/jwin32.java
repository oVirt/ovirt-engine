package org.ovirt.engine.core.utils.jwin32;

import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.Native;
import com.sun.jna.WString;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public interface jwin32 extends StdCallLibrary {
    public static final int LOGON32_PROVIDER_DEFAULT = 0;

    public static final int LOGON32_LOGON_INTERACTIVE = 2;
    public static final int LOGON32_LOGON_NETWORK = 3;
    public static final int LOGON32_LOGON_BATCH = 4;
    public static final int LOGON32_LOGON_SERVICE = 5;
    public static final int LOGON32_LOGON_UNLOCK = 7;
    public static final int LOGON32_LOGON_NETWORK_CLEARTEXT = 8;
    public static final int LOGON32_LOGON_NEW_CREDENTIALS = 9;

    public static final int NERR_Success = 0;
    public static final int ERROR_MORE_DATA = 234;

    public static final int FILTER_TEMP_DUPLICATE_ACCOUNT = 0x0001;
    public static final int FILTER_NORMAL_ACCOUNT = 0x0002;
    public static final int FILTER_INTERDOMAIN_TRUST_ACCOUNT = 0x0008;
    public static final int FILTER_WORKSTATION_TRUST_ACCOUNT = 0x0010;
    public static final int FILTER_SERVER_TRUST_ACCOUNT = 0x0020;

    public static final int MAX_PREFERRED_LENGTH = -1;

    public static final int UF_SCRIPT = 0x0001;
    public static final int UF_ACCOUNTDISABLE = 0x0002;
    public static final int UF_HOMEDIR_REQUIRED = 0x0008;
    public static final int UF_LOCKOUT = 0x0010;
    public static final int UF_PASSWD_NOTREQD = 0x0020;
    public static final int UF_PASSWD_CANT_CHANGE = 0x0040;
    public static final int UF_ENCRYPTED_TEXT_PASSWORD_ALLOWED = 0x0080;

    //
    // Account type bits as part of usri_flags.
    //
    public static final int UF_TEMP_DUPLICATE_ACCOUNT = 0x0100;
    public static final int UF_NORMAL_ACCOUNT = 0x0200;
    public static final int UF_INTERDOMAIN_TRUST_ACCOUNT = 0x0800;
    public static final int UF_WORKSTATION_TRUST_ACCOUNT = 0x1000;
    public static final int UF_SERVER_TRUST_ACCOUNT = 0x2000;

    public static final int UF_MACHINE_ACCOUNT_MASK = (
            UF_INTERDOMAIN_TRUST_ACCOUNT |
                    UF_WORKSTATION_TRUST_ACCOUNT |
            UF_SERVER_TRUST_ACCOUNT
            );

    public static final int UF_ACCOUNT_TYPE_MASK = (
            UF_TEMP_DUPLICATE_ACCOUNT |
                    UF_NORMAL_ACCOUNT |
                    UF_INTERDOMAIN_TRUST_ACCOUNT |
                    UF_WORKSTATION_TRUST_ACCOUNT |
            UF_SERVER_TRUST_ACCOUNT
            );

    public static final int UF_DONT_EXPIRE_PASSWD = 0x10000;
    public static final int UF_MNS_LOGON_ACCOUNT = 0x20000;
    public static final int UF_SMARTCARD_REQUIRED = 0x40000;
    public static final int UF_TRUSTED_FOR_DELEGATION = 0x80000;
    public static final int UF_NOT_DELEGATED = 0x100000;
    public static final int UF_USE_DES_KEY_ONLY = 0x200000;
    public static final int UF_DONT_REQUIRE_PREAUTH = 0x400000;
    public static final int UF_PASSWORD_EXPIRED = 0x800000;
    public static final int UF_TRUSTED_TO_AUTHENTICATE_FOR_DELEGATION = 0x1000000;
    public static final int UF_NO_AUTH_DATA_REQUIRED = 0x2000000;
    public static final int UF_PARTIAL_SECRETS_ACCOUNT = 0x4000000;
    public static final int UF_USE_AES_KEYS = 0x8000000;

    public static final int SECURITY_MAX_SID_SIZE = 68; // This is 32bit need to figure out 64

    public static final int UF_SETTABLE_BITS = (
            UF_SCRIPT |
                    UF_ACCOUNTDISABLE |
                    UF_LOCKOUT |
                    UF_HOMEDIR_REQUIRED |
                    UF_PASSWD_NOTREQD |
                    UF_PASSWD_CANT_CHANGE |
                    UF_ACCOUNT_TYPE_MASK |
                    UF_DONT_EXPIRE_PASSWD |
                    UF_MNS_LOGON_ACCOUNT |
                    UF_ENCRYPTED_TEXT_PASSWORD_ALLOWED |
                    UF_SMARTCARD_REQUIRED |
                    UF_TRUSTED_FOR_DELEGATION |
                    UF_NOT_DELEGATED |
                    UF_USE_DES_KEY_ONLY |
                    UF_DONT_REQUIRE_PREAUTH |
                    UF_PASSWORD_EXPIRED |
                    UF_TRUSTED_TO_AUTHENTICATE_FOR_DELEGATION |
                    UF_NO_AUTH_DATA_REQUIRED |
                    UF_USE_AES_KEYS |
            UF_PARTIAL_SECRETS_ACCOUNT
            );

    jwin32 advapi32 = (jwin32) Native.loadLibrary(
            (Platform.isWindows() ? "advapi32" : "c"),
            jwin32.class
            );

    jwin32 netapi32 = (jwin32) Native.loadLibrary(
            (Platform.isWindows() ? "netapi32" : "c"),
            jwin32.class
            );

    jwin32 kernel32 = (jwin32) Native.loadLibrary(
            (Platform.isWindows() ? "kernel32" : "c"),
            jwin32.class
            );

    /*
     * public boolean LookupAccountName ( String lpSystemName, String lpAccountName, Pointer Sid, IntByReference cbSid,
     * char[] ReferencedDomainName, IntByReference cchReferencedDomainName, PointerByReference peUse );
     */
    public boolean LookupAccountNameA(
            String lpSystemName,
            String lpAccountName,
            byte[] Sid,
            IntByReference cbSid,
            char[] ReferencedDomainName, // not sure it should not be byte, only for the A version
            IntByReference cchReferencedDomainName,
            PointerByReference peUse
            );

    public boolean LookupAccountNameW(
            WString lpSystemName,
            WString lpAccountName,
            byte[] Sid,
            IntByReference cbSid,
            char[] ReferencedDomainName,
            IntByReference cchReferencedDomainName,
            PointerByReference peUse
            );

    public boolean CloseHandle(
            int hObject
            );

    public boolean ConvertSidToStringSidA(
            byte[] Sid,
            PointerByReference stringSID
            );

    public int GetLastError();

    public boolean ImpersonateLoggedOnUser(
            IntByReference hToken
            );

    public Pointer LocalFree(Pointer hMem);

    public boolean LogonUserA(
            String userName,
            String domain,
            String password,
            int dwLogonType,
            int dwLogonProvider,
            IntByReference lToken
            );

    public boolean LogonUserW(
            WString userName,
            WString domain,
            WString password,
            int dwLogonType,
            int dwLogonProvider,
            IntByReference lToken
            );

    public int NetUserGetInfo(
            WString servername,
            WString username,
            int level,
            // PUSER_INFO_23.ByReference bufptr
            PointerByReference bufptr
            );

    public int NetUserEnum(
            WString servername,
            int level,
            int filter,
            PointerByReference bufptr,
            int prefmaxlen,
            IntByReference entriesread,
            IntByReference totalentries,
            IntByReference resume_handle
            );

    public int NetGroupGetInfo(
            WString servername,
            WString username,
            int level,
            PointerByReference bufptr
            );

    public int NetLocalGroupEnum(
            WString servername,
            int level,
            PointerByReference bufptr,
            int prefmaxlen,
            IntByReference entriesread,
            IntByReference totalentries,
            IntByReference resume_handle
            );

    public int NetLocalGroupGetInfo(
            WString servername,
            WString groupname,
            int level,
            PointerByReference bufptr
            );

    public int NetGroupEnum(
            WString servername,
            int level,
            PointerByReference bufptr, // PointerByReference bufptr,
            int prefmaxlen,
            IntByReference entriesread,
            IntByReference totalentries,
            IntByReference resume_handle
            );

    public int NetQueryDisplayInformation(
            WString servername,
            int level,
            int index,
            int entriesRequested,
            int prefferedMaximumLength,
            IntByReference totalentries,
            PointerByReference bufptr
            );

    public int NetUserChangePassword(
            WString domainname,
            WString username,
            WString oldpassword,
            WString newpassword
            );

    public int NetApiBufferFree(
            Pointer Buffer
            );

}
