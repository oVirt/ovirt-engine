package org.ovirt.engine.core.dal.job;

import java.util.EnumMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.MessageResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains messages by the context of Job or Step, as read from <i>bundles/ExecutionMessages.properties</i>
 */
public class ExecutionMessageDirector {

    public static final String EXECUTION_MESSAGES_FILE_PATH = "bundles/ExecutionMessages";

    /**
     * The prefix of the job message in the properties file
     */
    protected static final String JOB_MESSAGE_PREFIX = "job.";

    /**
     * The prefix of the step message in the properties file
     */
    protected static final String STEP_MESSAGE_PREFIX = "step.";

    /**
     * A single instance of the {@code ExecutionMessageDirector}
     */
    private static ExecutionMessageDirector instance = new ExecutionMessageDirector();

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(ExecutionMessageDirector.class);

    /**
     * Stores the job messages
     */
    private Map<ActionType, String> jobMessages = new EnumMap<>(ActionType.class);

    /**
     * Stores the step messages
     */
    private Map<StepEnum, String> stepMessages = new EnumMap<>(StepEnum.class);

    private ExecutionMessageDirector() {
    }

    /**
     * Load resources files and initialize the messages cache.
     *
     * @param bundleBaseName
     *            The base name of the resource bundle
     */
    public void initialize(String bundleBaseName) {
        log.info("Start initializing {}", getClass().getSimpleName());
        ResourceBundle bundle = ResourceBundle.getBundle(bundleBaseName);
        final int jobMessagePrefixLength = JOB_MESSAGE_PREFIX.length();
        final int stepMessagePrefixLength = STEP_MESSAGE_PREFIX.length();

        for (String key : bundle.keySet()) {

            if (key.startsWith(JOB_MESSAGE_PREFIX)) {
                addMessage(key, bundle.getString(key), jobMessages, ActionType.class, jobMessagePrefixLength);
            } else if (key.startsWith(STEP_MESSAGE_PREFIX)) {
                addMessage(key, bundle.getString(key), stepMessages, StepEnum.class, stepMessagePrefixLength);
            } else {
                log.error("The message key '{}' cannot be categorized since not started with '{}' nor '{}'",
                        key,
                        JOB_MESSAGE_PREFIX,
                        STEP_MESSAGE_PREFIX);
            }
        }
        log.info("Finished initializing {}", getClass().getSimpleName());
    }

    /**
     * Adds a pair of {@code Enum} and message to the messages map. If the key is not valid, an error message is logged.
     * The key should be resolvable as an {@code Enum}, once its prefix is trimmed and the searched for an {@code Enum}
     * match by name. Possible entries of (key,value) from the resource bundle:
     *
     * <pre>
     * job.ChangeVMCluster=Change VM ${VM} Cluster to ${Cluster}
     * step.VALIDATING=Validating
     * </pre>
     *
     * @param key
     *            The key of the pair to be added, by which the enum is searched.
     * @param value
     *            The message of the pair to be added
     * @param enumClass
     *            The enum class search for an instance which match the key
     * @param messagesMap
     *            The map whic the message should be added to
     * @param prefixLength
     *            The length of the key prefix
     */
    private <T extends Enum<T>> void addMessage(String key,
            String value,
            Map<T, String> messagesMap,
            Class<T> enumClass,
            int prefixLength) {

        T enumKey;

        try {
            enumKey = T.valueOf(enumClass, key.substring(prefixLength));
        } catch (IllegalArgumentException e) {
            log.error("Message key '{}' is not valid for enum '{}'", key, enumClass.getSimpleName());
            return;
        }

        if (!messagesMap.containsKey(key)) {
            messagesMap.put(enumKey, value);
        } else {
            log.warn("Code '{}' appears more than once in '{}' table.", key, enumClass.getSimpleName());
        }
    }

    public static ExecutionMessageDirector getInstance() {
        return instance;
    }

    /**
     * Retrieves a message by the step name.
     *
     * @param stepName
     *            The name by which the message is retrieved
     * @return A message describing the step, or the step name by {@code StepEnum.name()} if not found.
     */
    public String getStepMessage(StepEnum stepName) {
        return getMessage(stepMessages, stepName);
    }

    /**
     * Retrieves a message by the action type.
     *
     * @param actionType
     *            The type by which the message is retrieved
     * @return A message describing the action type, or the action type name by {@code ActionType.name()} if not
     *         found.
     */
    public String getJobMessage(ActionType actionType) {
        return getMessage(jobMessages, actionType);
    }

    private <T extends Enum<T>> String getMessage(Map<T, String> map, T type) {
        String message = map.get(type);
        if (message == null) {
            log.warn("The message key '{}' is missing from '{}'", type.name(), EXECUTION_MESSAGES_FILE_PATH);
            message = type.name();
        }
        return message;
    }

    public static String resolveJobMessage(ActionType actionType, Map<String, String> values) {
        String jobMessage = getInstance().getJobMessage(actionType);
        if (jobMessage != null) {
            return MessageResolver.resolveMessage(jobMessage, values);
        } else {
            return actionType.name();
        }
    }

    public static String resolveStepMessage(StepEnum stepName, Map<String, String> values) {
        String stepMessage = getInstance().getStepMessage(stepName);
        if (stepMessage != null) {
            return MessageResolver.resolveMessage(stepMessage, values);
        } else {
            return stepName.name();
        }
    }
}
