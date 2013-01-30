package org.ovirt.engine.core.dal.dbbroker;

import org.ovirt.engine.core.compat.Guid;

public class user_sessions {

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((browser == null) ? 0 : browser.hashCode());
        result = prime * result + ((clientType == null) ? 0 : clientType.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((loginTime == null) ? 0 : loginTime.hashCode());
        result = prime * result + ((os == null) ? 0 : os.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        user_sessions other = (user_sessions) obj;
        if (browser == null) {
            if (other.browser != null)
                return false;
        } else if (!browser.equals(other.browser))
            return false;
        if (clientType == null) {
            if (other.clientType != null)
                return false;
        } else if (!clientType.equals(other.clientType))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (loginTime == null) {
            if (other.loginTime != null)
                return false;
        } else if (loginTime != null && other.loginTime == null)
            return false;
        else if (loginTime != null && other.loginTime != null) {
            // TODO remvoe this when moving to Hibernate
            long thisLoginTimeNoMillis = loginTime.getTime();
            thisLoginTimeNoMillis -= thisLoginTimeNoMillis % 1000l;

            long thatLoginTimeNoMillis = other.loginTime.getTime();
            thatLoginTimeNoMillis -= thatLoginTimeNoMillis % 1000l;

            if (thisLoginTimeNoMillis != thatLoginTimeNoMillis)
                return false;
        }
        if (os == null) {
            if (other.os != null)
                return false;
        } else if (!os.equals(other.os))
            return false;
        return true;
    }

    private user_sessions_id id = new user_sessions_id();

    public user_sessions() {
    }

    public user_sessions(String browser, String client_type, java.util.Date login_time, String os, String session_id,
            Guid user_id) {
        this.browser = browser;
        this.clientType = client_type;
        this.loginTime = login_time;
        this.os = os;
        this.id.sessionId = session_id;
        this.id.userId = user_id;
    }

    private String browser;

    public String getbrowser() {
        return this.browser;
    }

    public void setbrowser(String value) {
        this.browser = value;
    }

    private String clientType;

    public String getclient_type() {
        return this.clientType;
    }

    public void setclient_type(String value) {
        this.clientType = value;
    }

    private java.util.Date loginTime;

    public java.util.Date getlogin_time() {
        return this.loginTime;
    }

    public void setlogin_time(java.util.Date value) {
        this.loginTime = value;
    }

    private String os;

    public String getos() {
        return this.os;
    }

    public void setos(String value) {
        this.os = value;
    }

    public String getsession_id() {
        return this.id.sessionId;
    }

    public void setsession_id(String value) {
        this.id.sessionId = value;
    }

    public Guid getuser_id() {
        return this.id.userId;
    }

    public void setuser_id(Guid value) {
        this.id.userId = value;
    }
}
