package org.ovirt.engine.api.extensions.aaa;

import java.util.Collection;

import org.ovirt.engine.api.extensions.ExtKey;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.ExtUUID;

/**
 * Authentication related constants.
 */
public class Authn {

    /**
     * Configuration keys.
     * Configuration keys for the extension configuration.
     */
    public static class ConfigKeys {
        /** Profile is the default profile for login page. */
        public static final String DEFAULT_PROFILE = "ovirt.engine.aaa.authn.default.profile";
        /** Profile name. */
        public static final String PROFILE_NAME = "ovirt.engine.aaa.authn.profile.name";
        /** Authorization extension name. */
        public static final String AUTHZ_PLUGIN = "ovirt.engine.aaa.authn.authz.plugin";
        /** Optional mapping extension name. */
        public static final String MAPPING_PLUGIN = "ovirt.engine.aaa.authn.mapping.plugin";
        /**
         * Negotiation priority.
         * Less is higher priority.
         * Default: 50.
         */
        public static final String NEGOTIATION_PRIORITY = "ovirt.engine.aaa.authn.negotiation.priority";
    }

    /**
     * Context keys.
     */
    public static class ContextKeys {
        /**
         * Provider capabilities.
         * Bitwise or of capabilities flags.
         * @see Capabilities
         */
        public static final ExtKey CAPABILITIES = new ExtKey("AAA_AUTHN_CAPABILITIES", Long.class, "9d16bee3-10fd-46f2-83f9-3d3c54cf258d");

        /**
         * HTTP authentication scheme. A list of authentication
         * scheme elements a client will receive with an HTTP
         * (unauthorized) response message as a value of the
         * WWW-Authenticate header.
         */
        public static final ExtKey HTTP_AUTHENTICATION_SCHEME = new ExtKey("AAA_HTTP_AUTHENTICATION_SCHEME", Collection/*<String>*/.class, "c0a5c8b4-870b-436e-aeb9-1cd5307b2058");
    }

    /**
     * Invoke keys.
     */
    public static class InvokeKeys {
        /** Raw user. */
        public static final ExtKey USER = new ExtKey("AAA_AUTHN_USER", String.class, "1ceaba26-1bdc-4663-a3c6-5d926f9dd8f0");
        /** Principal user. */
        public static final ExtKey PRINCIPAL = new ExtKey("AAA_AUTHN_PRINCIPAL", String.class, "bc637d1d-f93f-45e1-bd04-646c6dc38279");
        /** Credentials to use. */
        public static final ExtKey CREDENTIALS = new ExtKey("AAA_AUTHN_CREDENTIALS", String.class, "03b96485-4bb5-4592-8167-810a5c909706", ExtKey.Flags.SENSITIVE);
        /** Credentials to change into. */
        public static final ExtKey CREDENTIALS_NEW = new ExtKey("AAA_AUTHN_CREDENTIALS_NEW", String.class, "3455434d-63ab-42b0-b17b-a92621dd6dd1", ExtKey.Flags.SENSITIVE);
        /*
         * HttpServletRequest.
         * Used for negitiatation.
         */
        public static final ExtKey HTTP_SERVLET_REQUEST = new ExtKey("AAA_AUTHN_HTTP_SERVLET_REQUEST", Object.class, "e1cd5eb2-8f63-4617-bcd4-9863bbc788d7");
        /**
         * HttpServletResponse.
         * Used for negitiatation.
         */
        public static final ExtKey HTTP_SERVLET_RESPONSE = new ExtKey("AAA_AUTHN_HTTP_SERVLET_RESPONSE", Object.class, "834598d4-4b27-4588-8176-72f57d5ac007");
        /**
         * User message.
         * Examples: message of the day, password about to expire notice.
         */
        public static final ExtKey USER_MESSAGE = new ExtKey("AAA_AUTHN_USER_MESSAGE", String.class, "b1f6b062-fd42-4cd4-a22d-5b4ab23c93ff");
        /**
         * Credentials change URL.
         * Read when password is expired.
         * @see AuthResult#CREDENTIALS_EXPIRED
         */
        public static final ExtKey CREDENTIALS_CHANGE_URL = new ExtKey("AAA_AUTHN_CREDENTIALS_CHANGE_URL", String.class, "3276a8b4-e8f9-4b01-a5e0-7e9ee416b8e7");
        /**
         * AuthResult of authentication.
         * @see AuthResult
         */
        public static final ExtKey RESULT = new ExtKey("AAA_AUTHN_RESULT", Integer.class, "af9771dc-a0bb-417d-a700-277616aedd85");
        /**
         * Redirect to this URL.
         * @see AuthResult#REDIRECT
         */
        public static final ExtKey REDIRECT = new ExtKey("AAA_AUTHN_REDIRECT", String.class, "2eb81f5d-59a9-4e0e-aaf6-809d259f0a64");
        /**
         * Authetication record.
         * This will be forwarded to the authorization. Provider can
         * add fields as it wishes to the record to communicate with
         * authorization module.
         * @see AuthRecord
         */
        public static final ExtKey AUTH_RECORD = new ExtKey("AAA_AUTHN_AUTH_RECORD", ExtMap.class, "e9462168-b53b-44ac-9af5-f25e1697173e");
        /**
         * Credentials challenge record.
         * Collection of {@link ExtMap}
         * @see CredentialsChallengeRecord
         */
        public static final ExtKey CREDENTIALS_CHALLENGE_RECORD = new ExtKey("AAA_AUTHN_CREDENTIALS_CHALLENGE_RECORD", Collection/*<ExtMap>*/.class, "c3fce607-4a07-43a5-986c-399cbdb5ac90");
        /**
         * Credentials challenge response record.
         * Collection of {@link ExtMap}
         * @see CredentialsChallengeResponseRecord
         */
        public static final ExtKey CREDENTIALS_CHALLENGE_RESPONSE_RECORD = new ExtKey("AAA_AUTHN_CREDENTIALS_CHALLENGE_RESPONSE_RECORD", Collection/*<ExtMap>*/.class, "5b25f21e-f2df-4f50-a110-a60b360d67e8");
    }

    /**
     * Invoke commands.
     */
    public static class InvokeCommands {
        /**
         * Negotiate authentication.
         *
         * Inspect HttpServletRequest and optionally interact with remote using
         * HttpServletResponse. {@link InvokeKeys#RESULT} must be set with
         * negotiation result.
         * {@link ContextKeys#HTTP_AUTHENTICATION_SCHEME} value is
         * sent when HTTP 401 response is sent, to enable client
         * negotiation.
         *
         * <p>
         * Input:
         * </p>
         * <ul>
         * <li>{@link InvokeKeys#HTTP_SERVLET_REQUEST}[M]</li>
         * <li>{@link InvokeKeys#HTTP_SERVLET_RESPONSE}[M]</li>
         * </ul>
         *
         * <p>
         * Output:
         * </p>
         * <ul>
         * <li>{@link InvokeKeys#AUTH_RECORD}[O] - must be set on success.</li>
         * <li>{@link InvokeKeys#CREDENTIALS_CHANGE_URL}[O]</li>
         * <li>{@link InvokeKeys#PRINCIPAL}[O] - should be set if available even if login failed if principal is known.</li>
         * <li>{@link InvokeKeys#RESULT}[M]</li>
         * <li>{@link InvokeKeys#USER_MESSAGE}[O]</li>
         * </ul>
         *
         * @see AuthResult#NEGOTIATION_INCOMPLETE
         * @see AuthResult#NEGOTIATION_UNAUTHORIZED
         */
        public static final ExtUUID AUTHENTICATE_NEGOTIATE = new ExtUUID("AAA_AUTHN_NEGOTIATE", "fbfee86d-afe7-4465-bfcf-30d91be9adc1");
        /**
         * Credentials based authentication.
         *
         * <p>
         * Input:
         * </p>
         * <ul>
         * <li>{@link InvokeKeys#CREDENTIALS_CHALLENGE_RESPONSE_RECORD}[O]</li>
         * <li>{@link InvokeKeys#CREDENTIALS}[M]</li>
         * <li>{@link InvokeKeys#USER}[M]</li>
         * </ul>
         *
         * <p>
         * Output:
         * </p>
         * <ul>
         * <li>{@link InvokeKeys#AUTH_RECORD}[O] - must be set on success.</li>
         * <li>{@link InvokeKeys#CREDENTIALS_CHALLENGE_RECORD}[O]</li>
         * <li>{@link InvokeKeys#CREDENTIALS_CHANGE_URL}[O]</li>
         * <li>{@link InvokeKeys#PRINCIPAL}[O] - should be set if available even if login failed if principal is known.</li>
         * <li>{@link InvokeKeys#RESULT}[M]</li>
         * <li>{@link InvokeKeys#USER_MESSAGE}[O]</li>
         * </ul>
         */
        public static final ExtUUID AUTHENTICATE_CREDENTIALS = new ExtUUID("AAA_AUTHN_AUTHENTICATE_CREDENTIALS", "d9605c75-6b43-4b00-b32c-06bdfa80244c");
        /**
         * Logout.
         *
         * <p>
         * Input:
         * </p>
         * <ul>
         * <li>{@link InvokeKeys#AUTH_RECORD}[M]</li>
         * </ul>
         */
        public static final ExtUUID LOGOUT = new ExtUUID("AAA_AUTHN_LOGOUT", "3acac9f1-e123-46f6-a6ee-94b89dd54f42");
        /**
         * Credentials change.
         *
         * <p>
         * Input:
         * </p>
         * <ul>
         * <li>{@link InvokeKeys#CREDENTIALS_CHALLENGE_RESPONSE_RECORD}[O]</li>
         * <li>{@link InvokeKeys#CREDENTIALS_NEW}[M]</li>
         * <li>{@link InvokeKeys#CREDENTIALS}[M]</li>
         * <li>{@link InvokeKeys#USER} or {@link InvokeKeys#PRINCIPAL}[M]</li>
         * </ul>
         *
         * <p>
         * Output:
         * </p>
         * <ul>
         * <li>{@link InvokeKeys#RESULT}[M]</li>
         * <li>{@link InvokeKeys#CREDENTIALS_CHALLENGE_RECORD}[O]</li>
         * </ul>
         */
        public static final ExtUUID CREDENTIALS_CHANGE = new ExtUUID("AAA_AUTHN_CREDENTIALS_CHANGE", "3392d839-890d-404e-a093-a13242e537c2");
        /**
         * Credentials self reset.
         *
         * <p>
         * Input:
         * </p>
         * <ul>
         * <li>{@link InvokeKeys#USER} or {@link InvokeKeys#PRINCIPAL}[M]</li>
         * <li>{@link InvokeKeys#CREDENTIALS_CHALLENGE_RESPONSE_RECORD}[O]</li>
         * </ul>
         *
         * <p>
         * Output:
         * </p>
         * <ul>
         * <li>{@link InvokeKeys#CREDENTIALS_CHALLENGE_RECORD}[O]</li>
         * </ul>
         */
        public static final ExtUUID CREDENTIALS_SELF_RESET = new ExtUUID("AAA_AUTHN_CREDENTIALS_SELF_RESET", "49094e34-f44a-4003-884e-e78bff9d1c84");
    }

    /**
     * Capabilities.
     */
    public static class Capabilities {
        /**
         * Provider supports negotiate non interactive mode authentication.
         * Used strictly HTTP headers/state to negotiate authentication.
         */
        public static final long AUTHENTICATE_NEGOTIATE_NON_INTERACTIVE = 1 << 0;
        /**
         * Provider supports negotiate interactive authentication.
         * May use form or any user interactive sequence.
         */
        public static final long AUTHENTICATE_NEGOTIATE_INTERACTIVE = 1 << 1;
        /** Provider supports credentials based authentication. */
        public static final long AUTHENTICATE_CREDENTIALS = 1 << 2;
        /** Provider supports password based authentication. */
        public static final long AUTHENTICATE_PASSWORD = 1 << 3;
        /** Provider supports logout. */
        public static final long LOGOUT = 1 << 4;
        /** Provider supports credential change. */
        public static final long CREDENTIALS_CHANGE = 1 << 5;
        /** Provider supports self reset credential, example will send new password via email. */
        public static final long CREDENTIALS_SELF_RESET = 1 << 6;
    }

    /**
     * Authentication result.
     */
    public static class AuthResult {
        /** Success. */
        public static final int SUCCESS = 0;
        /** General error. */
        public static final int GENERAL_ERROR = 1;
        /** Configuration is invalid. */
        public static final int CONFIGURATION_INVALID = 2;
        /** Account is disabled. */
        public static final int ACCOUNT_DISABLED = 3;
        /** Account is expired. */
        public static final int ACCOUNT_EXPIRED = 4;
        /** Account is locked. */
        public static final int ACCOUNT_LOCKED = 5;
        /** Account login time violation. */
        public static final int ACCOUNT_TIME_VIOLATION = 6;
        /** Account restriction violation. */
        public static final int ACCOUNT_RESTRICTION = 7;
        /**
         * Redirect is required.
         * @see InvokeKeys#REDIRECT
         */
        public static final int REDIRECT = 8;
        /**
         * Challenge is required.
         * @see InvokeKeys#CREDENTIALS_CHALLENGE_RECORD
         */
        public static final int CHALLENGE_REQUIRED = 9;
        /** Credentials are expired. */
        public static final int CREDENTIALS_EXPIRED = 10;
        /** Credentials are incorrect. */
        public static final int CREDENTIALS_INCORRECT = 11;
        /** Credentials are invalid. */
        public static final int CREDENTIALS_INVALID = 12;
        /** Remote server is unavailable. */
        public static final int REMOTE_UNAVAILABLE = 13;
        /** Request timeout. */
        public static final int TIMED_OUT = 14;
        /** Negotiate is incomplete/in progress. */
        public static final int NEGOTIATION_INCOMPLETE = 15;
        /** Negotiate is complete and no authorization is established. */
        public static final int NEGOTIATION_UNAUTHORIZED = 16;
    }

    /**
     * Authentication record.
     */
    public static class AuthRecord {
        /** Full principal name, available post authentication. */
        public static final ExtKey PRINCIPAL = new ExtKey("AAA_AUTHN_AUTH_RECORD_PRINCIPAL", String.class, "c3498f07-11fe-464c-958c-8bd7490b119a");
        /**
         * Session valid to.
         * Application should expire session at most at this time.
         * Format: "yyyyMMddHHmmssX" timezone must be Z (UTC).
         */
        public static final ExtKey VALID_TO = new ExtKey("AAA_AUTHN_AUTH_RECORD_VALID_TO", String.class, "b332d076-5f4d-419f-8fdf-015579f4dfa6");
    }

    /**
     * Credentials self reset record.
     */
    public static class CredentialsChallengeRecord {
        /** Id of this challenge. */
        public static final ExtKey ID = new ExtKey("AAA_AUTHN_CREDENTIALS_CHALLENGE_RECORD_ID", String.class, "49c9f922-3dfe-49e6-a873-3cd36520a1bc");
        /** Mandatory. */
        public static final ExtKey MANDATORY = new ExtKey("AAA_AUTHN_CREDENTIALS_CHALLENGE_RECORD_MANDATORY", Boolean.class, "cad21ab1-5d46-4991-ad51-31e71d2a534e");
        /** Caption. */
        public static final ExtKey CAPTION = new ExtKey("AAA_AUTHN_CREDENTIALS_CHALLENGE_RECORD_CAPTION", String.class, "9dbdba41-4a43-48bc-bc38-2d7a481725dc");
        /** Text to display. */
        public static final ExtKey TEXT = new ExtKey("AAA_AUTHN_CREDENTIALS_CHALLENGE_RECORD_TEXT", String.class, "07532282-282a-44d8-aa8e-2768d49ff58d");
        /** Image to present. */
        public static final ExtKey IMAGE = new ExtKey("AAA_AUTHN_CREDENTIALS_CHALLENGE_RECORD_IMAGE", byte[].class, "233eba82-8793-4123-b0e5-d998f3ac909a");
    }

    /**
     * Credentials self reset record.
     */
    public static class CredentialsChallengeResponseRecord {
        /** Id of this challenge, copied from {@link CredentialsChallengeRecord#ID}. */
        public static final ExtKey ID = new ExtKey("AAA_AUTHN_CREDENTIALS_CHALLENGE_RESPONSE_RECORD_ID", String.class, "720dc74a-89c8-47ec-826a-f03f97e8e65a");
        /** Response. */
        public static final ExtKey RESPONSE = new ExtKey("AAA_AUTHN_CREDENTIALS_CHALLENGE_RESPONSE_RECORD_RESPONSE", String.class, "e8d17f6f-7056-4d2a-9024-baa068e1e9c9");
    }

}
