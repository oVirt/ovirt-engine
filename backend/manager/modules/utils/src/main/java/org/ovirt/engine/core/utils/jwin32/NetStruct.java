package org.ovirt.engine.core.utils.jwin32;

import com.sun.jna.ptr.IntByReference;
import com.sun.jna.WString;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;

import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public abstract class NetStruct extends Structure {
    private static Log log = LogFactory.getLog(USER_INFO_20.class);

    protected String getSID(WString strAccountName) throws ConvertSidException {
        log.debug("getSID entry");

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

        if (!jwin32.advapi32.LookupAccountNameW(
                new WString(""),
                strAccountName,
                sid,
                cbSid,
                referencedDomainName,
                cchReferencedDomainName,
                peUse
                )) {
            log.error(
                    "LookupAccountNameA failed for " + strAccountName +
                            " GetLastError=" + jwin32.kernel32.GetLastError()
                    );
            throw new ConvertSidException("LookupAccountNameA failed for" + strAccountName);
        }

        if (!jwin32.advapi32.ConvertSidToStringSidA(sid, stringSID)) {
            log.error(
                    "ConvertSidToStringSidA failed for " + strAccountName +
                            " GetLastError=" + jwin32.kernel32.GetLastError()
                    );
            throw new ConvertSidException("ConvertSidToStringSidA failed for" + strAccountName);
        }

        retSID = stringSID.getValue().getString(0, false);
        jwin32.kernel32.LocalFree(stringSID.getValue());

        return (retSID);
    }
}
