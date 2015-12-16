package org.ovirt.engine.core.dal.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.ovirt.engine.core.dal.job.ExecutionMessageDirector.EXECUTION_MESSAGES_FILE_PATH;
import static org.ovirt.engine.core.dal.job.ExecutionMessageDirector.JOB_MESSAGE_PREFIX;
import static org.ovirt.engine.core.dal.job.ExecutionMessageDirector.STEP_MESSAGE_PREFIX;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ResourceBundle;

import org.junit.Test;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.job.StepEnum;

public class ExecutionMessageDirectorTest {

    private static final String UPDATE_CLUSTER_CLUSTERS_MESSAGE = "Update Cluster ${Clusters}";
    private static String TEST_BUNDLE_NAME = "TestExecutionMessages";
    private static String INVALID_KEY_TEST_BUNDLE_NAME = "InvalidKeyExecutionMessages";
    private static String INVALID_JOB_KEY_TEST_BUNDLE_NAME = "InvalidJobKeyExecutionMessages";
    private static String INVALID_STEP_KEY_TEST_BUNDLE_NAME = "InvalidStepKeyExecutionMessages";

    /**
     * Verifies the engine-core execution messages are aligned with the enumerators {@code VdcActionType and StepEnum}
     */
    @Test
    public void verifyEngineMessagesSupported() throws FileNotFoundException, IOException {
        ResourceBundle bundle = ResourceBundle.getBundle(EXECUTION_MESSAGES_FILE_PATH);
        String testKey = null;
        Class<?> testEnum = null;

        try {
            for (String key : bundle.keySet()) {
                testKey = key;
                if (key.startsWith(ExecutionMessageDirector.JOB_MESSAGE_PREFIX)) {
                    testEnum = VdcActionType.class;
                    VdcActionType.valueOf(key.substring(JOB_MESSAGE_PREFIX.length()));
                } else if (key.startsWith(ExecutionMessageDirector.STEP_MESSAGE_PREFIX)) {
                    testEnum = StepEnum.class;
                    StepEnum.valueOf(key.substring(STEP_MESSAGE_PREFIX.length()));
                }
            }
        } catch (RuntimeException e) {
            String test = (testEnum != null) ? testEnum.getSimpleName() : "[null]";
            fail("Missing entry in enum " + test + " for key " + testKey);
        }
    }

    /**
     * Verifies the correct message is retrieved by an existing key
     */
    @Test
    public void testExistJobMessages() {
        ExecutionMessageDirector messageDirector = ExecutionMessageDirector.getInstance();
        messageDirector.initialize(TEST_BUNDLE_NAME);

        String updateClusterMessage = messageDirector.getJobMessage(VdcActionType.UpdateCluster);
        assertTrue(UPDATE_CLUSTER_CLUSTERS_MESSAGE.equals(updateClusterMessage));
    }

    /**
     * If the message key doesn't appear in the file, return the name itself
     */
    @Test
    public void testMissingJobMessages() {
        ExecutionMessageDirector messageDirector = ExecutionMessageDirector.getInstance();
        messageDirector.initialize(TEST_BUNDLE_NAME);

        assertEquals(VdcActionType.Unknown.name(), messageDirector.getJobMessage(VdcActionType.Unknown));
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
