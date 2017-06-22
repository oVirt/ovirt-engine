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

/**
 * These error codes can be found at ansible-playbook manual page.
 *
 * To see more details explanation run: `man ansible-playbook`
 */
public enum AnsibleReturnCode {
    OK(0),
    ERROR(1),
    FAIL(2),
    UNREACHABLE(3),
    PARSE_ERROR(4),
    BAD_OPTIONS(5),
    USER_INTERRUPTED(99),
    UNEXPECTED_ERROR(250);

    private final int returnCode;

    AnsibleReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }

    @Override
    public String toString() {
        return String.valueOf(returnCode);
    }
}
