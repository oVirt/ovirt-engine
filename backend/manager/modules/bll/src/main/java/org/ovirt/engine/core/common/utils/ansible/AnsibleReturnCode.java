/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
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
