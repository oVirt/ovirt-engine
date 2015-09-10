package org.ovirt.engine.api.extensions.aaa;

import org.ovirt.engine.api.extensions.ExtKey;
import org.ovirt.engine.api.extensions.ExtUUID;

/**
 * Mapping related constants.
 */
public class Mapping {

    /**
     * Invoke keys.
     */
    public static class InvokeKeys {
        /** Raw user to convert. */
        public static final ExtKey USER = new ExtKey("AAA_MAPPING_USER", String.class, "30f1434e-78b3-4a2a-b013-d180fa2f28c1");
    }

    /**
     * Invoke commands.
     */
    public static class InvokeCommands {
        /**
         * Map user.
         * Called pre authn for {@link Authn.Capabilities#AUTHENTICATE_CREDENTIALS} extensions.
         *
         * <p>
         * Input:
         * </p>
         * <ul>
         * <li>{@link InvokeKeys#USER}[M] - user.</li>
         * </ul>
         *
         * <p>
         * Output:
         * </p>
         * <ul>
         * <li>{@link InvokeKeys#USER}[M] - user.</li>
         * </ul>
         */
        public static final ExtUUID MAP_USER = new ExtUUID("AAA_MAPPING_MAP_USER", "02d37fee-b169-437a-8b8b-15643e481fba");
        /**
         * Map auth record.
         * Called post authn, pre authz.
         *
         * <p>
         * Input:
         * </p>
         * <ul>
         * <li>{@link Authn.InvokeKeys#AUTH_RECORD}[M] - authentication record.</li>
         * </ul>
         *
         * <p>
         * Output:
         * </p>
         * <ul>
         * <li>{@link Authn.InvokeKeys#AUTH_RECORD}[M] - authentication record.</li>
         * </ul>
         *
         * @see Authn.AuthRecord
         */
        public static final ExtUUID MAP_AUTH_RECORD = new ExtUUID("AAA_MAPPING_MAP_AUTH_RECORD", "c15b1814-0099-4ec3-91ae-0541dee1064d");
    }

}
