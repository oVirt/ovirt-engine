/*
* Copyright (c) 2010 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*           http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.api.model;

public enum PermitType {
    CREATE_VM(1, RoleType.USER),
    DELETE_VM(2, RoleType.USER),
    EDIT_VM_PROPERTIES(3, RoleType.USER),
    VM_BASIC_OPERATIONS(4, RoleType.USER),
    CHANGE_VM_CD(5, RoleType.USER),
    MIGRATE_VM(6, RoleType.USER),
    CONNECT_TO_VM(7, RoleType.USER),
    IMPORT_EXPORT_VM(8, RoleType.ADMIN),
    CONFIGURE_VM_NETWORK(9, RoleType.USER),
    CONFIGURE_VM_STORAGE(10, RoleType.USER),
    MOVE_VM(11, RoleType.USER),
    MANIPULATE_VM_SNAPSHOTS(12, RoleType.USER),
    RECONNECT_TO_VM(13, RoleType.ADMIN),
    // host (vds) actions groups
    CREATE_HOST(100, RoleType.ADMIN),
    EDIT_HOST_CONFIGURATION(101, RoleType.ADMIN),
    DELETE_HOST(102, RoleType.ADMIN),
    MANIPUTLATE_HOST(103, RoleType.ADMIN),
    CONFIGURE_HOST_NETWORK(104, RoleType.ADMIN),
    // templates actions groups
    CREATE_TEMPLATE(200, RoleType.USER),
    EDIT_TEMPLATE_PROPERTIES(201, RoleType.USER),
    DELETE_TEMPLATE(202, RoleType.USER),
    COPY_TEMPLATE(203, RoleType.USER),
    CONFIGURE_TEMPLATE_NETWORK(204, RoleType.USER),
    // vm pools actions groups
    CREATE_VM_POOL(300, RoleType.USER),
    EDIT_VM_POOL_CONFIGURATION(301, RoleType.USER),
    DELETE_VM_POOL(302, RoleType.USER),
    VM_POOL_BASIC_OPERATIONS(303, RoleType.USER),
    // clusters actions groups
    CREATE_CLUSTER(400, RoleType.ADMIN),
    EDIT_CLUSTER_CONFIGURATION(401, RoleType.ADMIN),
    DELETE_CLUSTER(402, RoleType.ADMIN),
    CONFIGURE_CLUSTER_NETWORK(403, RoleType.ADMIN),
    // users and MLA actions groups
    MANIPULATE_USERS(500, RoleType.ADMIN),
    MANIPULATE_ROLES(501, RoleType.ADMIN),
    MANIPULATE_PERMISSIONS(502, RoleType.USER),
    // storage domains actions groups
    CREATE_STORAGE_DOMAIN(600, RoleType.ADMIN),
    EDIT_STORAGE_DOMAIN_CONFIGURATION(601, RoleType.ADMIN),
    DELETE_STORAGE_DOMAIN(602, RoleType.ADMIN),
    MANIPULATE_STORAGE_DOMAIN(603, RoleType.ADMIN),
    // storage pool actions groups
    CREATE_STORAGE_POOL(700, RoleType.ADMIN),
    DELETE_STORAGE_POOL(701, RoleType.ADMIN),
    EDIT_STORAGE_POOL_CONFIGURATION(702, RoleType.ADMIN),
    CONFIGURE_STORAGE_POOL_NETWORK(703, RoleType.ADMIN),

    // rhevm generic
    CONFIGURE_RHEVM(800, RoleType.ADMIN),

    // Quota
    CONFIGURE_QUOTA(900, RoleType.ADMIN),
    CONSUME_QUOTA(901, RoleType.USER),

    // Gluster
    CREATE_GLUSTER_VOLUME(1000, RoleType.ADMIN),
    MANIPULATE_GLUSTER_VOLUME(1001, RoleType.ADMIN),
    DELETE_GLUSTER_VOLUME(1002, RoleType.ADMIN),

    // Disks action groups
    CREATE_DISK(1100, RoleType.USER),
    ATTACH_DISK(1101, RoleType.USER),
    EDIT_DISK_PROPERTIES(1102, RoleType.USER),
    CONFIGURE_DISK_STORAGE(1103, RoleType.USER),
    DELETE_DISK(1104, RoleType.USER),

    // Vm Interface action groups
    PORT_MIRRORING(1104, RoleType.ADMIN),

    // Login action group
    LOGIN(1300, RoleType.USER);

    private int id;
    private RoleType role;
    private PermitType(int id, RoleType role) {
        this.id = id;
        this.role = role;
    }

    public String value() {
        return name().toLowerCase();
    }

    public RoleType getRole() {
        return role;
    }

    public int getId() {
        return id;
    }
}
