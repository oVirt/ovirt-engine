package org.ovirt.engine.core.bll.adbroker;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.ovirt.engine.core.common.businessentities.AdUser;
import org.ovirt.engine.core.common.businessentities.ad_groups;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.LogCompat;
import org.ovirt.engine.core.utils.log.LogFactoryCompat;
import org.ovirt.engine.core.compat.Regex;
import org.ovirt.engine.core.compat.RegexOptions;
import org.ovirt.engine.core.utils.jwin32.ConvertSidException;
import org.ovirt.engine.core.utils.jwin32.LOCAL_GROUP_INFO_0;
import org.ovirt.engine.core.utils.jwin32.USER_INFO_20;
import org.ovirt.engine.core.utils.jwin32.jwin32;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public abstract class LUBrokerCommandBase extends BrokerCommandBase {
    private static LogCompat log = LogFactoryCompat.getLog(LUBrokerCommandBase.class);

    @Override
    protected String getPROTOCOL() {
        return "LDAP://";
    }

    protected LUBrokerCommandBase(LdapBrokerBaseParameters parameters) {
        super(parameters);
    }

    @Override
    public LdapReturnValueBase Execute() {
        try {
            ExecuteQuery();
        } catch (RuntimeException e) {
            log.errorFormat("Error in executing LU broker command. Exception is {0} ", e.getMessage());
            _ldapReturnValue.setSucceeded(false);
            _ldapReturnValue.setReturnValue(null);
        }
        return _ldapReturnValue;
    }

    protected abstract void ExecuteQuery();

    /**
     * This method convert a single condition from the ldap query string into a
     * regex. The pattern describe the condition to be converted. If the pattern
     * does not exist in the query it will return a null.
     *
     * @param query
     * @param pattern
     * @return
     */
    protected Regex queryToRegex(String query, String pattern) {
        Regex retval = null;
        if (query.contains(pattern)) {
            String value = query.substring(query.indexOf(pattern)).split("[()=]")[1].trim();
            log.debug("queryToRegex, value=" + value);
            retval = new Regex(value, RegexOptions.IgnoreCase);
        }
        return retval;
    }

    protected Pattern queryToPattern(String query, String pattern) {
        Pattern retval = null;
        if (query.contains(pattern)) {
            String value = query.substring(query.indexOf(pattern)).split("[()=]")[1].trim();
            log.debug("queryToPattern, value=" + value.replace("*", ".*"));
            retval = Pattern.compile(value.replace("*", ".*"), Pattern.CASE_INSENSITIVE);
        }
        return retval;
    }

    /**
     * This method return an array list of local users with their info. I had to
     * hack around quite a few bits here, so I guess it worth some through
     * explaining. But first please follow the most important rule of WIN32 APIs
     * - get into MSDN and read the remarks section about NetUserEnum. Ok, so as
     * you can see, NetUserEnum allocate memory area by itself. this memory area
     * is later on populated with an array of structures (pointer to pointer)
     * which hold the wanted information. This required some trickery. which I
     * couldnt get working any other way, so please test this throughly if you
     * think of any cleaner solution. I was mapping the needed structure into
     * java class according to JNA instructions. adding it a ByReference class
     * (again, according to instructions). Then (here come the tricky part) I've
     * created another class which have a member of that previous ByReference
     * type and is overriding the toArray method so that it will actually call
     * the toArray of the original structure mapping class. Doing the same with
     * PointerByRefernce and useMemory failed! (you can try again if you like, I
     * might have gotten something wrong there.) Ok, so we've got that part
     * working, now, we need the following info about each user: full name, SID
     * and flags (expired, locked, etc.) it seems very apropriate to use
     * USER_INFO_20 but!!! it seems that calling NetUserEnum with level=23 fails
     * with error 124 or 1722, another trickery is needed! So I'm calling
     * NetUserEnum with level=0 which gives only the username, then calling
     * NetUserGetInfo (again, highly advasiable, no required, to read the
     * remarks section) with level=23 which miraculously works ;)
     *
     * Update: made it working by useMemory and PointerByReference. but had to
     * drop the sid for that so now, this is used only with USER_INFO_20 (no
     * need anymore for 0 and 23) and a special Implementation, using
     * LookupAccountName to get the sid, which is shared with LOCAL_GROUP class
     */

    protected ArrayList<AdUser> getAdUsers() {
        int nStatus;
        ArrayList<AdUser> retVal = new ArrayList<AdUser>();
        IntByReference nEntriesRead = new IntByReference();
        IntByReference nTotalEntries = new IntByReference();
        PointerByReference pUI20 = new PointerByReference();

        nStatus = jwin32.netapi32.NetUserEnum(new WString(""), 20, jwin32.FILTER_NORMAL_ACCOUNT, pUI20,
                                              jwin32.MAX_PREFERRED_LENGTH, // This
                                                                           // have
                                                                           // high
                                                                           // potential
                                                                           // of
                                                                           // breaking
                                                                           // on
                                                                           // systems
                                                                           // with
                                                                           // a
                                                                           // large
                                                                           // number
                                                                           // of
                                                                           // users!
                                              nEntriesRead,
                                              nTotalEntries,
                                              null);

        if (nStatus == jwin32.NERR_Success || nStatus == jwin32.ERROR_MORE_DATA) {

            // Allocates a USER_INFO_0 object that maps to a USER_INFO_0
            // struct that is located in the address that is held by the
            // value of pUI0.
            // This address is the beginning of a sequence of structures of
            // USER_INFO_0, so retrieving array is possible.
            USER_INFO_20 userTemp = new USER_INFO_20(pUI20.getValue());
            USER_INFO_20 users[] = (USER_INFO_20[]) userTemp.toArray(nTotalEntries.getValue());

            for (USER_INFO_20 user : users) {
                retVal.add(populateUser(user));
            }

            if (nEntriesRead.getValue() > 0) {
                jwin32.netapi32.NetApiBufferFree(pUI20.getValue());
            }
        }

        return (retVal);
    }

    protected ArrayList<ad_groups> getAdGroups() {
        log.debug("getAdGroups Entry");

        int nStatus;
        PointerByReference pGI0 = new PointerByReference();
        IntByReference nEntriesRead = new IntByReference();
        IntByReference nTotalEntries = new IntByReference();
        ArrayList<ad_groups> retVal = new ArrayList<ad_groups>();

        nStatus = jwin32.netapi32.NetLocalGroupEnum(
                                                    new WString(""),
                                                    0,
                                                    pGI0,
                                                    jwin32.MAX_PREFERRED_LENGTH,
                                                    nEntriesRead,
                                                    nTotalEntries,
                                                    null
                                 );

        if (nStatus == jwin32.NERR_Success ||
                nStatus == jwin32.ERROR_MORE_DATA) {
            log.debug("NetLocalGroupEnum returned " + nEntriesRead.getValue() + " entries");

            LOCAL_GROUP_INFO_0 groupsArr = new LOCAL_GROUP_INFO_0(pGI0.getValue());
            LOCAL_GROUP_INFO_0[] groups = (LOCAL_GROUP_INFO_0[]) groupsArr.toArray(nEntriesRead.getValue());

            for (LOCAL_GROUP_INFO_0 group : groups) {
                String retSID;
                PointerByReference group_sid;
                PointerByReference stringSID = new PointerByReference();
                IntByReference nSIDSize = new IntByReference();

                log.debug("populating group name=" + group.lgrpi0_name);

                retVal.add(populateGroup(group));
            }

            if (nEntriesRead.getValue() > 0) {
                jwin32.netapi32.NetApiBufferFree(pGI0.getValue());
            }
        } else {
            log.error("getAdGroups, NetLocalGroupEnum error (" + jwin32.kernel32.GetLastError() + ") return=" + nStatus);
        }

        log.debug("getAdGroups, Return size=" + retVal.size());
        return (retVal);
    }

    protected AdUser populateUser(USER_INFO_20 child) {
        log.debug("populateUser Entry");

        try {
            AdUser user = new AdUser();
            user.setUserId(sidToGuid(child.getSID()));
            user.setName(child.usri20_full_name.toString());
            user.setUserName(child.usri20_name.toString());
            /*
             * According to MSDN documentation for C# Environment.MachineName it
             * seems the class is getting the machine name from environment
             * variable named COMPUTERNAME
             */
            user.setDomainControler(System.getenv("COMPUTERNAME"));

            user.setPasswordExpired((child.usri20_flags & jwin32.UF_PASSWORD_EXPIRED) == jwin32.UF_PASSWORD_EXPIRED);

            // debug print all properties
            // List<string> test = new List<string>();
            // foreach (string key in child.Properties.PropertyNames)
            // {
            // test.Add(key + ":" + child.Properties[key].Value);
            // }
            return user;
        } catch (ConvertSidException ex) {
            log.error("convert sid failed");
            return null;
        }
    }

    protected ad_groups populateGroup(LOCAL_GROUP_INFO_0 child) {
        log.debug("populateGroup, Entry");

        try {
            ad_groups group = new ad_groups();
            group.setid(sidToGuid(child.getSID()));
            group.setname(child.lgrpi0_name.toString()); // todo: getName()
            group.setdomain(System.getenv("COMPUTERNAME"));

            log.debug("populateGroup, Return");
            return group;
        } catch (ConvertSidException ex) {
            log.error("convert sid failed");
            return null;
        }
    }

    /* do we really need a byte[] format ? */
    protected static Guid sidToGuid(byte[] sid) {
        if (sid == null) {
            return (null);
        }

        byte[] barray = new byte[16];
        if (sid.length == 16) {
            barray = sid;
        } else if (sid.length > 16) {
            int offset = sid.length - 16;
            for (int i = 0; i < 15; i++) {
                barray[i] = sid[i + offset];
            }
        }
        return new Guid(barray, true);
    }

    private static Guid sidToGuid(String sid) throws ConvertSidException {
        log.debug("sidToGuid Entry, sid=" + sid + " length=" + sid.length());

        try {
            ByteBuffer bb = ByteBuffer.allocate(16);
            bb.order(ByteOrder.LITTLE_ENDIAN);

            String[] arrSidParts = sid.split("-");
            for (int i = 4; i < arrSidParts.length; i++) {
                bb.putInt((int) Long.parseLong(arrSidParts[i]));
            }

            return new Guid(bb.array(), false);
        } catch (java.lang.ArrayIndexOutOfBoundsException aioobe) {
            throw new ConvertSidException("Given sid has a wrong length. Please validate it: " + sid);
        } catch (java.nio.BufferOverflowException boe) {
            throw new ConvertSidException("Given sid length is too long. Please validate it: " + sid);
        }
    }
}
