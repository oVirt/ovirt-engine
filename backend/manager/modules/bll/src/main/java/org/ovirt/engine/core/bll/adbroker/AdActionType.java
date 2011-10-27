package org.ovirt.engine.core.bll.adbroker;

public enum AdActionType {
    AuthenticateUser,
    SearchGroupsByQuery,
    SearchUserByQuery,
    GetAdGroupByGroupId,
    GetAdUserByUserId,
    GetAdUserByUserName,
    GetAdUserByUserIdList,
    CreateComputerAccount,
    ChangeUserPassword,
    IsComputerWithTheSameNameExists;

    public int getValue() {
        return this.ordinal();
    }

    public static AdActionType forValue(int value) {
        return values()[value];
    }
}
