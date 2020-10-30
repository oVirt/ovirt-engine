package org.ovirt.engine.core.common.businessentities;

import java.util.Objects;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class UserSshKey {
    private final String loginName;
    private final String content;
    private final String userId;

    public UserSshKey(String loginName, Guid userId, String content) {
        this.loginName = Objects.requireNonNull(loginName);
        this.content = Objects.requireNonNull(content);
        this.userId = userId.toString();
    }

    public String getLoginName() {
        return loginName;
    }

    public String getContent() {
        return content;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserSshKey that = (UserSshKey) o;
        return Objects.equals(loginName, that.loginName) &&
                Objects.equals(content, that.content) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(loginName, content, userId);
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("loginName", loginName)
                .append("content", content)
                .append("userId", userId)
                .toString();
    }
}
