package org.ovirt.engine.api.extensions.aaa;

import java.util.Collection;

import org.ovirt.engine.api.extensions.ExtKey;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.ExtUUID;

/**
 * Authorization related constants.
 */
public class Authz {

    /**
     * Context keys.
     */
    public static class ContextKeys {
        /**
         * Available namespaces within provider.
         * Query can be done within the context of namespace, to avoid
         * scanning entire network. At least one namespace must be available.
         */
        public static final ExtKey AVAILABLE_NAMESPACES = new ExtKey("AAA_AUTHZ_AVAILABLE_NAMESPACES", Collection/*<String>*/.class, "6dffa34c-955f-486a-bd35-0a272b45a711");
        /**
         * Maximum query filter size.
         * Limit the number of entries within {@link InvokeKeys#QUERY_FILTER}.
         * No more than this may be provided.
         */
        public static final ExtKey QUERY_MAX_FILTER_SIZE = new ExtKey("AAA_AUTHZ_QUERY_MAX_FILTER_SIZE", Integer.class, "2eb1f541-0f65-44a1-a6e3-014e247595f5");
        /**
         * Provider capabilities.
         * Bitwise or of capabilities flags.
         * @see Capabilities
         */
        public static final ExtKey CAPABILITIES = new ExtKey("AAA_AUTHZ_CAPABILITIES", Long.class, "6106d1fb-9291-4351-a947-b897b9540a23");
    }

    /**
     * Invoke keys.
     */
    public static class InvokeKeys {
        /**
         * Page size for queries.
         * This is only a hint, result may be at smaller page size
         * or higher.
         */
        public static final ExtKey PAGE_SIZE = new ExtKey("AAA_AUTHZ_PAGE_SIZE", Integer.class, "03197cd2-2d0f-4636-bd88-f65c4a543efe");
        /**
         * Flag to determine how entries will be queried.
         * @see QueryFlags
         * */
        public static final ExtKey QUERY_FLAGS = new ExtKey("AAA_AUTHZ_QUERY_FLAGS", Integer.class, "97d226e9-8d87-49a0-9a7f-af689320907b");
        /**
         * Principal record.
         * @see PrincipalRecord
         */
        public static final ExtKey PRINCIPAL_RECORD = new ExtKey("AAA_AUTHZ_PRINCIPAL_RECORD", ExtMap.class, "ebc0d5ca-f1ea-402c-86ae-a8ecbdadd6b5");
        /**
         * Principal value.
         * @see PrincipalRecord
         */
        public static final ExtKey PRINCIPAL = new ExtKey("AAA_AUTHZ_PRINCIPAL", String.class, "a3c1d5ca-f1ea-131c-86ae-a1ecbcadd6b7");
        /**
         * AuthResult of operation.
         * @see Status
         */
        public static final ExtKey STATUS = new ExtKey("AAA_AUTHZ_STATUS", Integer.class, "566f0ba5-8329-4de1-952a-7a81e4bedd3e");
        /**
         * Namespace to use.
         * @see ContextKeys#AVAILABLE_NAMESPACES
         */
        public static final ExtKey NAMESPACE = new ExtKey("AAA_AUTHZ_NAMESPACE", String.class, "7e12d802-86ff-4162-baaa-d6f6fe73201e");
        /**
         * Query filter.
         * @see QueryFilterRecord
         */
        public static final ExtKey QUERY_FILTER = new ExtKey("AAA_AUTHZ_QUERY_FILTER", ExtMap.class, "93086835-fef1-4d69-8173-a45d738b932a");
        /**
         * Query filter entity.
         * @see QueryEntity
         */
        public static final ExtKey QUERY_ENTITY = new ExtKey("AAA_AUTHZ_QUERY_ENTITY", ExtUUID.class, "d0a55f21-b604-43c4-84a0-2bf459b32fa8");
        /**
         * Query opaque.
         * Returned by query open, must be provided as input to query execute.
         */
        public static final ExtKey QUERY_OPAQUE = new ExtKey("AAA_AUTHZ_QUERY_OPAQUE", Object.class, "3e2491e9-2b2d-4108-ad4c-8048e2308f3e");
        /**
         * Query result.
         * Execute query until no results.
         * Output is Collection of {@link ExtMap}.
         * Actual content depends on the query.
         */
        public static final ExtKey QUERY_RESULT = new ExtKey("AAA_AUTHZ_QUERY_RESULT", Collection/*<ExtMap>*/.class, "0cde6caf-b851-41cb-8de2-cd34327d7249");
    }

    /**
     * Authz flags
     */
    public static class QueryFlags {
        /** Resolve groups. */
        public static final int RESOLVE_GROUPS = 1 << 0;
        /** Resolve groups recursively when resolving groups. */
        public static final int RESOLVE_GROUPS_RECURSIVE = 1 << 1;
    }

    /**
     * Invoke commands.
     */
    public static class InvokeCommands {
        /**
         * Fetch principal record.
         * Used for user login.
         *
         * <p>
         * Input: Either one of AUTH_RECORD or PRINCIPAL must be present:
         * </p>
         * <ul>
         * <li>{@link Authn.InvokeKeys#AUTH_RECORD}[O] - authentication record.</li>
         * <li>{@link Authz.InvokeKeys#PRINCIPAL}[O] - principal.</li>
         * <li>{@link InvokeKeys#QUERY_FLAGS}[O] - query flags.</li>
         * </ul>
         *
         * <p>
         * Output:
         * </p>
         * <ul>
         * <li>{@link InvokeKeys#PRINCIPAL_RECORD}</li>
         * </ul>
         *
         * @see Authn.AuthRecord
         * @see PrincipalRecord
         */
        public static final ExtUUID FETCH_PRINCIPAL_RECORD = new ExtUUID("AAA_AUTHZ_FETCH_PRINCIPAL_RECORD", "5a5bf9bb-9336-4376-a823-26efe1ba26df");
        /**
         * Query records.
         *
         * <p>
         * Input:
         * </p>
         * <ul>
         * <li>{@link InvokeKeys#NAMESPACE}[M]</li>
         * <li>{@link InvokeKeys#QUERY_ENTITY}[M]</li>
         * <li>{@link InvokeKeys#QUERY_FILTER}[M]</li>
         * <li>{@link InvokeKeys#QUERY_FLAGS}[O] - query flags.</li>
         * </ul>
         *
         * <p>
         * Output:
         * </p>
         * <ul>
         * <li>{@link InvokeKeys#QUERY_OPAQUE}</li>
         * </ul>
         *
         * <p>
         * Search execute output based on entity.
         * </p>
         */
        public static final ExtUUID QUERY_OPEN = new ExtUUID("AAA_AUTHZ_QUERY_OPEN", "8879cfd1-17f8-477b-a057-c0fa849dc97f");
        /**
         * Execute query.
         *
         * <p>
         * Input:
         * </p>
         * <ul>
         * <li>{@link InvokeKeys#PAGE_SIZE}[O]</li>
         * <li>{@link InvokeKeys#QUERY_OPAQUE}[M]</li>
         * </ul>
         *
         * <p>
         * Output:
         * </p>
         * <ul>
         * <li>{@link InvokeKeys#QUERY_RESULT} - Actual content depends on the query.</li>
         * </ul>
         */
        public static final ExtUUID QUERY_EXECUTE = new ExtUUID("AAA_AUTHZ_QUERY_EXECUTE", "b572eb07-11b6-4337-89e3-d1a4e0dafe41");
        /**
         * Close query.
         *
         * <p>
         * Input:
         * </p>
         * <ul>
         * <li>{@link InvokeKeys#QUERY_OPAQUE}[M]</li>
         * </ul>
         */
        public static final ExtUUID QUERY_CLOSE = new ExtUUID("AAA_AUTHZ_QUERY_CLOSE", "3e049bc0-055e-4789-a4e3-0ef51bfe6685");
    }

    /**
     * Capabilities.
     */
    public static class Capabilities {
        /**
         * Provider always resolves groups recursively. This implies provider ignores
         * {@link QueryFlags#RESOLVE_GROUPS_RECURSIVE} flag.
         */
        public static final long RECURSIVE_GROUP_RESOLUTION = 1 << 0;
    }

    /**
     * Principal record.
     */
    public static class PrincipalRecord {
        /** Namespace. */
        public static final ExtKey NAMESPACE = new ExtKey("AAA_AUTHZ_PRINCIPAL_NAMESPACE", String.class, "79703b8c-1e50-462f-9491-f5cf446f49de");
        /** Principal unique (within provider) id. */
        public static final ExtKey ID = new ExtKey("AAA_AUTHZ_PRINCIPAL_ID", String.class, "4f9440bc-9303-4d95-b317-b827515c782f");
        /** User name */
        public static final ExtKey NAME = new ExtKey("AAA_AUTHZ_PRINCIPAL_NAME", String.class, "a0df5bcc-6ead-40a2-8565-2f5cc8773bdd");
        /** Display name. */
        public static final ExtKey DISPLAY_NAME = new ExtKey("AAA_AUTHZ_PRINCIPAL_DISPLAY_NAME", String.class, "1687a9e2-d951-4ee6-9409-36bca8e83ed1");
        /** Email. */
        public static final ExtKey EMAIL = new ExtKey("AAA_AUTHZ_PRINCIPAL_EMAIL", String.class, "47367f40-71ca-472f-81b6-e11a0e0b75ed");
        /** First name. */
        public static final ExtKey FIRST_NAME = new ExtKey("AAA_AUTHZ_PRINCIPAL_FIRST_NAME", String.class, "654c2738-581b-45d4-a486-362d891d2db2");
        /** Last name. */
        public static final ExtKey LAST_NAME = new ExtKey("AAA_AUTHZ_PRINCIPAL_LAST_NAME", String.class, "d1479cd7-a19e-4bd8-a639-1e9db7f398d8");
        /** Department. */
        public static final ExtKey DEPARTMENT = new ExtKey("AAA_AUTHZ_PRINCIPAL_DEPARTMENT", String.class, "636e84bc-1e3a-4537-9407-7c6c3024fb60");
        /** Title. */
        public static final ExtKey TITLE = new ExtKey("AAA_AUTHZ_PRINCIPAL_TITLE", String.class, "506d3833-5c86-495c-af4c-0de2ef2da4ed");
        /**
         * Groups.
         * Collection of {@link GroupRecord}.
         * @see GroupRecord
         */
        public static final ExtKey GROUPS = new ExtKey("AAA_AUTHZ_PRINCIPAL_GROUPS", Collection/*<GroupRecord>*/.class, "738ec045-aade-478f-90f9-13f4aa229a54");
        /**
         * Principal name.
         * Note: this value is output only,
         * and cannot be used for search.
         */
        public static final ExtKey PRINCIPAL = new ExtKey("AAA_AUTHZ_PRINCIPAL_PRINCIPAL",
                String.class,
                "37c1c4ff-5367-480d-950a-1c3092521188");

    }

    /**
     * Group record.
     */
    public static class GroupRecord {
        /** Namespace. */
        public static final ExtKey NAMESPACE = new ExtKey("AAA_AUTHZ_GROUP_NAMESPACE", String.class, "a4763ceb-472f-4f06-a61e-e71289b5afe4");
        /** Group unique (within provider) id. */
        public static final ExtKey ID = new ExtKey("AAA_AUTHZ_GROUP_ID", String.class, "4615d4d3-a1b7-43cc-bc8d-c8a24a2ffd5a");
        /** Group name. */
        public static final ExtKey NAME = new ExtKey("AAA_AUTHZ_GROUP_NAME", String.class, "0eebe54f-b429-44f3-aa80-4704cbb16835");
        /** Display name. */
        public static final ExtKey DISPLAY_NAME = new ExtKey("AAA_AUTHZ_GROUP_DISPLAY_NAME", String.class, "cc2c8f75-bfac-453b-9184-c6ee18d62ef5");
        /**
         * Groups.
         * Collection of {@link GroupRecord}.
         * @see GroupRecord
         */
        public static final ExtKey GROUPS = new ExtKey("AAA_AUTHZ_GROUP_GROUPS", Collection/*<GroupRecord>*/.class, "c4f34760-084b-4f29-b9cf-e77bb539ec18");
    }

    /**
     * Query filter record.
     * Either nested filter list or field filter.
     * <p>
     * Example:
     * </p>
     * <pre>{@code
     * Filter = {
     *     OPERATOR: QueryFilterOperator.AND,
     *     FILTER: [
     *         {
     *             OPERATOR: QueryFilterOperator.EQ,
     *             KEY: PrincipalRecord.NAME,
     *             PrincipalRecord.NAME: "name1*",
     *         },
     *         {
     *             OPERATOR: QueryFilterOperator.NOT,
     *             FILTER: [
     *                 {
     *                     OPERATOR: QueryFilterOperator.EQ,
     *                     KEY: PrincipalRecord.DEPARTMENT,
     *                     PrincipalRecord.DEPARTMENT: "dept1",
     *                 },
     *             ],
     *         },
     *     ],
     * }
     * }</pre>
     */
    public static class QueryFilterRecord {
        /**
         * Operator.
         * <p>
         * This operator is applied as if value within filter is at
         * the right of the expression.
         * </p>
         * <p>
         * For nested filter list the operator is used between fields.
         * Permitted operators are boolean operators:
         * {@link QueryFilterOperator#NOT}, {@link QueryFilterOperator#AND} and
         * {@link QueryFilterOperator#OR}.
         * </p>
         * <p>
         * For field filter relational operators are allowed.
         * </p>
         * @see QueryFilterOperator
         */
        public static final ExtKey OPERATOR = new ExtKey("AAA_AUTHZ_QUERY_FILTER_OPERATOR", Integer.class, "c8588111-25a3-40e9-bf82-44acd3d0049d");
        /**
         * Nested filter.
         * Collection of QueryFilterRecord.
         * Either {@link #FILTER} or {@link #KEY} should be available.
         * @see QueryFilterRecord
         */
        public static final ExtKey FILTER = new ExtKey("AAA_AUTHZ_QUERY_FILTER_FILTER", Collection/*<QueryFilterRecord>*/.class, "a84d8b7a-0436-46bc-a49a-4dfda94e3a51");
        /**
         * Key to filter.
         * This key with appropriate value must exist within this record.
         * Either {@link #FILTER} or {@link #KEY} should be available.
         */
        public static final ExtKey KEY = new ExtKey("AAA_AUTHZ_QUERY_FILTER_KEY", ExtKey.class, "2be62864-6a4c-4a1b-80f0-bed68d9eb529");
    }

    /**
     * Query entities.
     */
    public static class QueryEntity {
        /**
         * Principal.
         * Input and output are {@link PrincipalRecord}
         */
        public static final ExtUUID PRINCIPAL = new ExtUUID("AAA_AUTHZ_QUERY_ENTITY_PRINCIPAL", "1695cd36-4656-474f-b7bc-4466e12634e4");
        /**
         * Group.
         * Input and output are {@link GroupRecord}
         */
        public static final ExtUUID GROUP = new ExtUUID("AAA_AUTHZ_QUERY_ENTITY_GROUP", "f91d029b-9140-459e-b452-db75d3d994a2");
    }

    /**
     * Query filter boolean operator.
     * Filter field value is at right side of expression.
     */
    public static class QueryFilterOperator {
        /**
         * Equals.
         * '*' wildcard may be placed at suffix of value to match any.
         */
        public static final int EQ = 0;
        /** Less or equals to. */
        public static final int LE = 1;
        /** Greater or equals to. */
        public static final int GE = 2;
        /** Not. */
        public static final int NOT = 100;
        /** And. */
        public static final int AND = 101;
        /** Or. */
        public static final int OR = 102;
    }

    /**
     * Status.
     * Additional information for failure states.
     */
    public static class Status {
        /** Success. */
        public static final int SUCCESS = 0;
        /** General error. */
        public static final int GENERAL_ERROR = 1;
        /** Configuration is invalid. */
        public static final int CONFIGURATION_INVALID = 2;
        /** Request timeout. */
        public static final int TIMED_OUT = 3;
    }

}
