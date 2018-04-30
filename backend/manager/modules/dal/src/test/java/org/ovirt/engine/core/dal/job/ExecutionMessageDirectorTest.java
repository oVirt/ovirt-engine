package org.ovirt.engine.core.dal.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.ovirt.engine.core.dal.job.ExecutionMessageDirector.EXECUTION_MESSAGES_FILE_PATH;
import static org.ovirt.engine.core.dal.job.ExecutionMessageDirector.JOB_MESSAGE_PREFIX;
import static org.ovirt.engine.core.dal.job.ExecutionMessageDirector.STEP_MESSAGE_PREFIX;

import java.util.ResourceBundle;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.job.StepEnum;

public class ExecutionMessageDirectorTest {

    private static final String UPDATE_CLUSTER_CLUSTERS_MESSAGE = "Update Cluster ${Clusters}";
    private static final String TEST_BUNDLE_NAME = "TestExecutionMessages";
    private static final String INVALID_KEY_TEST_BUNDLE_NAME = "InvalidKeyExecutionMessages";
    private static final String INVALID_JOB_KEY_TEST_BUNDLE_NAME = "InvalidJobKeyExecutionMessages";
    private static final String INVALID_STEP_KEY_TEST_BUNDLE_NAME = "InvalidStepKeyExecutionMessages";

    /**
     * Verifies the engine-core execution messages are aligned with the enumerators {@code ActionType} and
     * {@code StepEnum}.
     */
    @Test
    public void verifyEngineMessagesSupported() {
        ResourceBundle bundle = ResourceBundle.getBundle(EXECUTION_MESSAGES_FILE_PATH);
        for (String key : bundle.keySet()) {
            if (key.startsWith(ExecutionMessageDirector.JOB_MESSAGE_PREFIX)) {
                ActionType.valueOf(key.substring(JOB_MESSAGE_PREFIX.length()));
            } else if (key.startsWith(ExecutionMessageDirector.STEP_MESSAGE_PREFIX)) {
                StepEnum.valueOf(key.substring(STEP_MESSAGE_PREFIX.length()));
            }
        }
    }

    /**
     * Verifies the correct message is retrieved by an existing key
     */
    @Test
    public void testExistJobMessages() {
        ExecutionMessageDirector messageDirector = ExecutionMessageDirector.getInstance();
        messageDirector.initialize(TEST_BUNDLE_NAME);

        String updateClusterMessage = messageDirector.getJobMessage(ActionType.UpdateCluster);
        assertEquals(UPDATE_CLUSTER_CLUSTERS_MESSAGE, updateClusterMessage);
    }

    /**
     * If the message key doesn't appear in the file, return the name itself
     */
    @Test
    public void testMissingJobMessages() {
        ExecutionMessageDirector messageDirector = ExecutionMessageDirector.getInstance();
        messageDirector.initialize(TEST_BUNDLE_NAME);

        assertEquals(ActionType.Unknown.name(), messageDirector.getJobMessage(ActionType.Unknown));
    }

    /**
     * Test should fail for a key is without expected prefix
     */
    @Test
    public void readMissingMessageKey() {
        ExecutionMessageDirector messageDirector = ExecutionMessageDirector.getInstance();
        messageDirector.initialize(INVALID_KEY_TEST_BUNDLE_NAME);
    }

    /**
     * Test should fail for a job key is without expected prefix
     */
    @Test
    public void readMissingMessageJobKey() {
        ExecutionMessageDirector messageDirector = ExecutionMessageDirector.getInstance();
        messageDirector.initialize(INVALID_JOB_KEY_TEST_BUNDLE_NAME);
    }

    /**
     * Test should fail for a step key is without expected prefix
     */
    @Test
    public void readMissingMessageStepKey() {
        ExecutionMessageDirector messageDirector = ExecutionMessageDirector.getInstance();
        messageDirector.initialize(INVALID_STEP_KEY_TEST_BUNDLE_NAME);
    }

}
