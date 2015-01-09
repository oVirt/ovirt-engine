package org.ovirt.engine.api.extensions.aaa;

import java.util.Collection;

import org.ovirt.engine.api.extensions.ExtKey;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.ExtUUID;

/**
 * Acct related constants.
 */
public class Acct {

    /**
     * Invoke keys.
     */
    public static class InvokeKeys {
        /** Authz name. */
        public static final ExtKey AUTHZ_NAME = new ExtKey("AAA_ACCT_AUTHZ_NAME", String.class, "27aea820-60c7-4390-9953-1f2a254e314b");
        /** Principal name. */
        public static final ExtKey PRINCIPAL_NAME = new ExtKey("AAA_ACCT_PRINCIPAL_NAME", String.class, "3dfc4089-d2ec-40ec-89a5-71188ab89a4c");
        /** Principal id.*/
        public static final ExtKey PRINCIPAL_ID = new ExtKey("AAA_ACCT_PRINCIPAL_ID", String.class, "c34f7381-5c15-4666-b8b5-39cca36eca78");
        /** Message. */
        public static final ExtKey MESSAGE = new ExtKey("AAA_ACCT_MESSAGE", String.class, "c0dec21f-c6c1-40dc-9e0d-40e99ae0c70d");
        /**
         * Report reason.
         * @see ReportReason
         */
        public static final ExtKey REASON = new ExtKey("AAA_ACCT_REASON", Integer.class, "33fd1b4a-e098-4054-a3d5-088646689538");
        /**
         * Principal record.
         * @see PrincipalRecord
         */
        public static final ExtKey PRINCIPAL_RECORD = new ExtKey("AAA_ACCT_PRINCIPAL_RECORD", ExtMap.class, "fc7eca10-a3af-4cc2-befc-1c857b859257");
        /**
         * Resource record.
         * @see ResourceRecord
         */
        public static final ExtKey RESOURCE_RECORD = new ExtKey("AAA_ACCT_RESOURCE_RECORD", ExtMap.class, "ca20ab53-0b49-42a5-976a-e45c6c833985");
        /**
         * Operation record.
         * @see OperationRecord
         */
        public static final ExtKey OPERATION_RECORD = new ExtKey("AAA_ACCT_OPERATION_RECORD", ExtMap.class, "e85b41ff-054b-4fc5-9833-bb2581ec848d");
        /**
         * Application specific record.
         * May be sent by other extensions.
         */
        public static final ExtKey APPLICATION_SPECIFIC_RECORD = new ExtKey("AAA_ACCT_APPLICATION_SPECIFIC_RECORD", ExtMap.class, "cac7d81b-8f76-4b3e-a486-3e9a1db4d1c3");
    }

    /**
     * Invoke commands.
     */
    public static class InvokeCommands {
        /** Report. */
        public static final ExtUUID REPORT = new ExtUUID("AAA_ACCT_REPORT", "8cf4fd74-5b8f-4cd3-9b56-31804f9f8b27");
    }

    /**
     * Report reason.
     */
    public static class ReportReason {
        /** Application startup. */
        public static final int STARTUP = 0;
        /** Application shutdown. */
        public static final int SHUTDOWN = 1;
        /**
         * Authz Principal record was not found.
         * Will have {@link PrincipalRecord}.
         */
        public static final int PRINCIPAL_NOT_FOUND = 2;
        /**
         * Login failed by any reason but locked.
         * Will have {@link PrincipalRecord}.
         */
        public static final int PRINCIPAL_LOGIN_FAILED = 3;
        /**
         * Login failed as account is locked.
         * Will have {@link PrincipalRecord}.
         */
        public static final int PRINCIPAL_LOGIN_LOCKED = 4;
        /**
         * Login failed as no permission.
         * Will have {@link PrincipalRecord}.
         */
        public static final int PRINCIPAL_LOGIN_NO_PERMISSION = 5;
        /**
         * Credentials based login.
         * Will have {@link PrincipalRecord}.
         */
        public static final int PRINCIPAL_LOGIN_CREDENTIALS = 6;
        /**
         * Login on behalf of user.
         * Will have {@link PrincipalRecord}.
         */
        public static final int PRINCIPAL_LOGIN_NO_CREDENTAILS = 7;
        /**
         * Negotiation based login.
         * Will have {@link PrincipalRecord}.
         */
        public static final int PRINCIPAL_LOGIN_NEGOTIATE = 8;
        /**
         * Logout.
         * Will have {@link PrincipalRecord}.
         */
        public static final int PRINCIPAL_LOGOUT = 9;
        /**
         * Session expired.
         * Will have {@link PrincipalRecord}.
         */
        public static final int PRINCIPAL_SESSION_EXPIRED = 10;
        /**
         * Credentials changed, such as password changed.
         * Will have {@link PrincipalRecord}.
         */
        public static final int PRINCIPAL_CREDENTIALS_CHANGED = 11;
        /**
         * Access denied.
         */
        public static final int ACCESS_DENIED = 12;
        /**
         * Resource access.
         * Will have {@link ResourceRecord}.
         */
        public static final int RESOURCE_ACCESS = 13;
        /**
         * Privileged operation.
         * Will have {@link OperationRecord}.
         */
        public static final int PRIVILEDGED_OPERATION = 14;
        /**
         * Application specific.
         * Will have proprietary record.
         */
        public static final int APPLICATION_SPECIFIC = 15;
    }

    /**
     * Principal record.
     */
    public static class PrincipalRecord {
        /**
         * Raw user.
         * In case no #PRINCIPAL available, this happens if login fails.
         */
        public static final ExtKey USER = new ExtKey("AAA_ACCT_PRINCIPAL_RECORD_USER", String.class, "3e1a1639-0812-4bf6-9c86-d1435ed5d569");
        /**
         * Authz name.
         */
        public static final ExtKey AUTHZ_NAME = new ExtKey("AAA_ACCT_AUTHZ_NAME", String.class, "019133aa-4425-48b7-bfd6-7fff160dab70");
        /**
         * Principal.
         * Optional.
         */
        public static final ExtKey PRINCIPAL = new ExtKey("AAA_ACCT_PRINCIPAL_RECORD_PRINCIPAL", String.class, "def3d205-19ed-4e9b-bb78-a3532dab64ca");
        /**
         * Authorization's authorization record.
         * Optional.
         * @see Authn.AuthRecord
         */
        public static final ExtKey AUTH_RECORD = new ExtKey("AAA_ACCT_PRINCIPAL_RECORD_AUTH_RECORD", ExtMap.class, "dffa2cd7-9235-4c44-8871-884aaf143bd4");
        /**
         * Authorization's principal record.
         * Optional.
         * @see Authz.PrincipalRecord
         */
        public static final ExtKey PRINCIPAL_RECORD = new ExtKey("AAA_ACCT_PRINCIPAL_RECORD_PRINCIPAL_RECORD", ExtMap.class, "9f8aa88f-3b5d-47aa-bc60-e3cc507a2e65");
        /**
         * Login since.
         * Number of milliseconds since January 1, 1970, 00:00:00 GMT.
         * Optional.
         */
        public static final ExtKey LOGIN_SINCE = new ExtKey("AAA_ACCT_PRINCIPAL_RECORD_LOGIN_SINCE", Long.class, "669ca17e-1419-4091-86c2-d7e66b824fee");
        /**
         * Is principal is administrator.
         * Optional.
         */
        public static final ExtKey ADMINISTRATOR = new ExtKey("AAA_ACCT_PRINCIPAL_RECORD_ADMINISTRATOR", Boolean.class, "9928992c-64b1-47d3-870b-64f51fa1b8d6");
    }

    /**
     * Resource record.
     */
    public static class ResourceRecord {
        /** Resource being accessed. */
        public static final ExtKey NAME = new ExtKey("AAA_ACCT_RESOURCE_RECORD_NAME", String.class, "588f3259-3f15-4d9b-b533-a9b99ae77eb8");
        /** Resource access. */
        public static final ExtKey ACCESS = new ExtKey("AAA_ACCT_RESOURCE_RECORD_ACCESS", String.class, "f01262b1-846b-4d54-814a-f81efba58155");
    }

    /**
     * Operation record.
     */
    public static class OperationRecord {
        /** Operation name. */
        public static final ExtKey NAME = new ExtKey("AAA_ACCT_OPERATION_RECORD_NAME", String.class, "47d9102d-6c70-46f8-a2ec-1e4e728c213b");
        /**
         * Parameters.
         * Collection of {@link ExtMap}.
         */
        public static final ExtKey PARAMETERS = new ExtKey("AAA_ACCT_OPERATION_RECORD_PARAMETERS", Collection/*<ExtMap>*/.class, "e752f6c5-2806-44e2-a80d-3f283aa8b54d");
    }
}
