package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

public enum AdActionType {
    AuthenticateUser,
    SearchGroupsByQuery,
    SearchUserByQuery,
    GetAdGroupByGroupId,
    GetAdUserByUserId,
    GetAdUserByUserName,
    GetAdUserByUserIdList,
    IsComputerWithTheSameNameExists;

    public int getValue() {
        return this.ordinal();
    }

    public static AdActionType forValue(int value) {
        return values()[value];
    }
}
