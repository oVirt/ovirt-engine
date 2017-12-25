/*
Copyright (c) 2017 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.engine.core.common.utils.ansible;

import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang.StringUtils;
import org.junit.ClassRule;
import org.junit.Test;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.utils.MockEngineLocalConfigRule;

public class AnsibleCommandBuilderTest {

    private static final String PRIVATE_KEY = "--private-key=/etc/pki/ovirt-engine/keys/engine_id_rsa";
    private static final String ANSIBLE_PLAYBOOK = "myplaybook.yml";
    private static final String ANSIBLE_PLAYBOOK_FULL_PATH = "/usr/share/ovirt-engine/playbooks/myplaybook.yml";
    private static final String ANSIBLE_LOG_LEVEL = "-v";

    @ClassRule
    public static MockEngineLocalConfigRule mockEngineLocalConfigRule;

    static {
        mockEngineLocalConfigRule = new MockEngineLocalConfigRule(
            new MockEngineLocalConfigRule.KeyValue("ENGINE_PKI", "/etc/pki/ovirt-engine/"),
            new MockEngineLocalConfigRule.KeyValue("ENGINE_USR", "/usr/share/ovirt-engine/"),
            new MockEngineLocalConfigRule.KeyValue("ENGINE_VAR", "/var/lib/ovirt-engine/"),
            new MockEngineLocalConfigRule.KeyValue("ENGINE_LOG", "/var/log/ovirt-engine/")
        );
    }

    @Test
    public void testAllEmpty() {
        String command = createCommand(new AnsibleCommandBuilder().playbook(ANSIBLE_PLAYBOOK));
        assertEquals(
            join(
                AnsibleCommandBuilder.ANSIBLE_COMMAND,
                ANSIBLE_LOG_LEVEL,
                PRIVATE_KEY,
                ANSIBLE_PLAYBOOK_FULL_PATH
            ),
            command
        );
    }

    @Test
    public void testDisableVerboseMode() {
        String command = createCommand(
            new AnsibleCommandBuilder()
                .verboseLevel(AnsibleVerbosity.LEVEL0)
                .playbook(ANSIBLE_PLAYBOOK)
        );

        assertEquals(join(AnsibleCommandBuilder.ANSIBLE_COMMAND, PRIVATE_KEY, ANSIBLE_PLAYBOOK_FULL_PATH), command);
    }

    @Test
    public void testInventoryFile() {
        Path inventoryFile = Paths.get("myfile");
        String command = createCommand(
            new AnsibleCommandBuilder()
                .inventoryFile(inventoryFile)
                .playbook(ANSIBLE_PLAYBOOK)
        );
        assertEquals(
            join(
                AnsibleCommandBuilder.ANSIBLE_COMMAND,
                ANSIBLE_LOG_LEVEL,
                PRIVATE_KEY,
                "--inventory=" + inventoryFile,
                ANSIBLE_PLAYBOOK_FULL_PATH
            ),
            command
        );
    }

    @Test
    public void testDifferentVerbosity() {
        String command = createCommand(
            new AnsibleCommandBuilder()
                .verboseLevel(AnsibleVerbosity.LEVEL2)
                .playbook(ANSIBLE_PLAYBOOK)
        );
        assertEquals(
            join(AnsibleCommandBuilder.ANSIBLE_COMMAND, "-vv", PRIVATE_KEY, ANSIBLE_PLAYBOOK_FULL_PATH),
            command
        );
    }

    @Test
    public void testVerbosityLevelZero() {
        String command = createCommand(
            new AnsibleCommandBuilder()
                .verboseLevel(AnsibleVerbosity.LEVEL0)
                .playbook(ANSIBLE_PLAYBOOK)
        );
        assertEquals(
            join(AnsibleCommandBuilder.ANSIBLE_COMMAND, PRIVATE_KEY, ANSIBLE_PLAYBOOK_FULL_PATH),
            command
        );
    }

    @Test
    public void testExtraVariables() {
        String command = createCommand(
            new AnsibleCommandBuilder()
                .variables(
                    new Pair<>("a", "1"),
                    new Pair<>("b", "2"),
                    new Pair<>("c", "3")
                )
                .playbook(ANSIBLE_PLAYBOOK)
        );
        assertEquals(
            join(
                AnsibleCommandBuilder.ANSIBLE_COMMAND,
                ANSIBLE_LOG_LEVEL,
                PRIVATE_KEY,
                "--extra-vars=a=1",
                "--extra-vars=b=2",
                "--extra-vars=c=3",
                ANSIBLE_PLAYBOOK_FULL_PATH
            ),
            command
        );
    }

    @Test
    public void testComplexCommand() {
        String command = createCommand(
            new AnsibleCommandBuilder()
                .privateKey(Paths.get("/mykey"))
                .inventoryFile(Paths.get("/myinventory"))
                .limit("mylimit")
                .verboseLevel(AnsibleVerbosity.LEVEL3)
                .variables(
                    new Pair<>("a", "1"),
                    new Pair<>("b", "2")
                )
                .playbook(ANSIBLE_PLAYBOOK)
        );
        assertEquals(
            join(
                AnsibleCommandBuilder.ANSIBLE_COMMAND,
                "-vvv",
                "--private-key=/mykey",
                "--inventory=/myinventory",
                "--limit=mylimit",
                "--extra-vars=a=1",
                "--extra-vars=b=2",
                ANSIBLE_PLAYBOOK_FULL_PATH
            ),
            command
        );
    }


    private String createCommand(AnsibleCommandBuilder command) {
        return StringUtils.join(command.build(), " ").trim();
    }

    private String join(String ... params) {
        return StringUtils.join(params, " ");
    }
}
