package org.ovirt.engine.api.extensions;

import java.util.List;
import java.util.Properties;

/**
 * Extension related constants.
 */
public class Base {

    /**
     * Interface version of these sources.
     * Should be set by extension into {@link ContextKeys#BUILD_INTERFACE_VERSION}.
     */
    public static final int INTERFACE_VERSION_CURRENT = 0;

    /**
     * Configuration keys.
     * Configuration keys for the extension configuration.
     */
    public static class ConfigKeys {
        /**
         * Extension instance name.
         * Variable like name, no special characters.
         * <br/>
         * Default: unique random.
         */
        public static final String NAME = "ovirt.engine.extension.name";
        /**
         * Extension services.
         * Value: Comma seperated strings.
         * <br/>
         * Default: none.
         */
        public static final String PROVIDES = "ovirt.engine.extension.provides";
        /**
         * Extension enabled.
         * <br/>
         * Value: true/false.
         * <br/>
         * Default: true.
         */
        public static final String ENABLED = "ovirt.engine.extension.enabled";
        /**
         * Extension module name.
         * Jboss module to load.
         * <br>
         * Mandatory.
         */
        public static final String MODULE = "ovirt.engine.extension.module";
        /**
         * Extension class name within module.
         * Used to locate the service at META-INF/services/&lt;class&gt;.Extension.
         * <br>
         * Mandatory.
         */
        public static final String CLASS = "ovirt.engine.extension.class";
        /**
         * Sensitive keys of configuration.
         * These will should not appear in debugging.
         */
        public static final String SENSITIVE_KEYS = "ovirt.engine.extension.sensitiveKeys";
    }

    /**
     * Global context key.
     */
    public static class GlobalContextKeys {
        /**
         * Extensions.
         * Loaded extension list.
         * @see ExtensionRecord
         */
        public static final ExtKey EXTENSIONS = new ExtKey("GLOBAL_EXTENSIONS", List/*<ExtMap>*/.class, "246498c0-2f4d-4135-8cb7-c5eabfd2f6ff");
    }

    /**
     * Context keys.
     */
    public static class ContextKeys {
        /** Global context. */
        public static final ExtKey GLOBAL_CONTEXT = new ExtKey("EXTENSION_GLOBAL_CONTEXT", ExtMap.class, "9799e72f-7af6-4cf1-bf08-297bc8903676");
        /** Minimum usable interface version. */
        public static final ExtKey INTERFACE_VERSION_MIN = new ExtKey("EXTENSION_INTERFACE_VERSION_MIN", Integer.class, "2b84fc91-305b-497b-a1d7-d961b9d2ce0b");
        /** Maximum usable interface version. */
        public static final ExtKey INTERFACE_VERSION_MAX = new ExtKey("EXTENSION_INTERFACE_VERSION_MAX", Integer.class, "f4cff49f-2717-4901-8ee9-df362446e3e7");
        /**
         * Sensitive configuration keys.
         * List of String.
         * Values should not be printed.
         */
        public static final ExtKey CONFIGURATION_SENSITIVE_KEYS = new ExtKey("EXTENSION_CONFIGURATION_SENSITIVE_KEYS", List/*<String>*/.class, "a456efa1-73ff-4204-9f9b-ebff01e35263");
        /** Locale to use. */
        public static final ExtKey LOCALE = new ExtKey("EXTENSION_LOCALE", String.class, "0780b112-0ce0-404a-b85e-8765d778bb29");
        /**
         * Extensions' interfaces.
         * List of String.
         */
        public static final ExtKey PROVIDES = new ExtKey("EXTENSION_PROVIDES", List/*<String>*/.class, "8cf373a6-65b5-4594-b828-0e275087de91");
        /** Extension instance name. */
        public static final ExtKey INSTANCE_NAME = new ExtKey("EXTENSION_INSTANCE_NAME", String.class, "65c67ff6-aeca-4bd5-a245-8674327f011b");
        /**
         * Extension configuration.
         * Extension configuration as loaded.
         * @see ConfigKeys
         */
        public static final ExtKey CONFIGURATION = new ExtKey("EXTENSION_CONFIGURATION", Properties.class, "2d48ab72-f0a1-4312-b4ae-5068a226b0fc", true);
        /**
         * Extension's build interface version.
         * Set by extension during {@link InvokeCommands#INITIALIZE} to {@link #INTERFACE_VERSION_CURRENT}.
         * @see InvokeCommands#INITIALIZE
         * @see #INTERFACE_VERSION_CURRENT
         */
        public static final ExtKey BUILD_INTERFACE_VERSION = new ExtKey("EXTENSION_BUILD_INTERFACE_VERSION", Integer.class, "cb479e5a-4b23-46f8-aed3-56a4747a8ab7");
        /**
         * Extension's license.
         * Set by extension.
         * @see InvokeCommands#INITIALIZE
         */
        public static final ExtKey LICENSE = new ExtKey("EXTENSION_LICENSE", String.class, "8a61ad65-054c-4e31-9c6d-1ca4d60a4c18");
        /**
         * Extensions' version.
         * Set by extension.
         * @see InvokeCommands#INITIALIZE
         */
        public static final ExtKey VERSION = new ExtKey("EXTENSION_VERSION", String.class, "fe35f6a8-8239-4bdb-ab1a-af9f779ce68c");
        /**
         * Extensions' author.
         * Set by extension.
         * @see InvokeCommands#INITIALIZE
         */
        public static final ExtKey AUTHOR = new ExtKey("EXTENSION_AUTHOR", String.class, "ef242f7a-2dad-4bc5-9aad-e07018b7fbcc");
        /**
         * Extensions' home URL.
         * Set by extension.
         * @see InvokeCommands#INITIALIZE
         */
        public static final ExtKey HOME_URL = new ExtKey("EXTENSION_HOME_URL", String.class, "4ad7a2f4-f969-42d4-b399-72d192e18304");
        /**
         * Extension name.
         * Set by extension.
         * @see InvokeCommands#INITIALIZE
         */
        public static final ExtKey EXTENSION_NAME = new ExtKey("EXTENSION_NAME", String.class, "651381d3-f54f-4547-bf28-b0b01a103184");
    }

    /**
     * Invoke keys.
     */
    public static class InvokeKeys {
        /** Extension context reference. */
        public static final ExtKey CONTEXT = new ExtKey("EXTENSION_INVOKE_CONTEXT", ExtMap.class, "886d2ebb-312a-49ae-9cc3-e1f849834b7d");
        /** Override locale of context. */
        public static final ExtKey LOCALE = new ExtKey("EXTENSION_INVOKE_LOCALE", String.class, "f9cebeec-43ac-4420-ae7d-1777473f1947");
        /** Command to execute. */
        public static final ExtKey COMMAND = new ExtKey("EXTENSION_INVOKE_COMMAND", ExtUUID.class, "485778ab-bede-4f1a-b823-77b262a2f28d");
        /**
         * Invoke result, set by extension.
         * @see InvokeResult
         */
        public static final ExtKey RESULT = new ExtKey("EXTENSION_INVOKE_RESULT", Integer.class, "0909d91d-8bde-40fb-b6c0-099c772ddd4e");
        /** Result message, set by extension. */
        public static final ExtKey MESSAGE = new ExtKey("EXTENSION_INVOKE_MESSAGE", String.class, "b7b053de-dc73-4bf7-9d26-b8bdb72f5893");
    }

    /**
     * Invoke commands.
     */
    public static class InvokeCommands {
        /**
         * Initialize extension instance.
         * Invoked before any other commands.
         * Extension should set its information within context during this command.
         */
        public static final ExtUUID INITIALIZE = new ExtUUID("EXTENSION_INITIALIZE", "e5ae1b7f-9104-4f23-a444-7b9175ff68d2");
        /** Terminate extension instance. */
        public static final ExtUUID TERMINATE = new ExtUUID("EXTENSION_TERMINATE", "83152bf5-861f-46e8-b0d7-4d6da28303f8");
    }

    /**
     * Extension record.
     */
    public static class ExtensionRecord {
        /** Extension reference. */
        public static final ExtKey EXTENSION = new ExtKey("EXTENSION_RECORD_EXTENSION", Extension.class, "fa110e1b-bf17-441d-8bd6-0fca24542405");
        /** Extension context. */
        public static final ExtKey CONTEXT = new ExtKey("EXTENSION_RECORD_CONTEXT", ExtMap.class, "9003f5f6-ada6-4f5d-8b8b-3d4bbed39be8");
        /** Instance name. */
        public static final ExtKey INSTANCE_NAME = new ExtKey("EXTENSION_RECORD_INSTANCE_NAME", String.class, "6e1f2c27-b89e-42bd-94c1-e709eb8ce0d4");
        /**
         * Extensions' interfaces.
         * List of String.
         */
        public static final ExtKey PROVIDES = new ExtKey("EXTENSION_RECORD_PROVIDES", List/*<String>*/.class, "701129bb-5956-427a-b962-6b1c1a13e4e7");
    }

    /**
     * Invoke result.
     */
    public static class InvokeResult {
        /** Success. */
        public static final int SUCCESS = 0;
        /**
         * Invoke command is unsupported.
         * @see InvokeKeys#COMMAND
         */
        public static final int UNSUPPORTED = 1;
        /** Command failed. */
        public static final int FAILED = 2;
    }

}
