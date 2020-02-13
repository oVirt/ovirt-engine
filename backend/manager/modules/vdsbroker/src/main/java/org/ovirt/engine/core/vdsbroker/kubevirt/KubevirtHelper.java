package org.ovirt.engine.core.vdsbroker.kubevirt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class KubevirtHelper {

    public static Map<String, Object> getConfig(String url, String token, String certificateAuthority) {
        String clusterName = "cluster";
        String userName = "user";
        String ctxName = "ctx";

        ArrayList<Object> contexts = new ArrayList<>();
        Map<String, Object> ctx = new HashMap<>();
        Map<String, Object> def = new HashMap<>();
        def.put("cluster", clusterName);
        def.put("user", userName);
        ctx.put("context", def);
        ctx.put("name", ctxName);
        contexts.add(ctx);

        ArrayList<Object> clusters = new ArrayList<>();
        Map<String, Object> cluster = new HashMap<>();
        cluster.put("name", clusterName);
        Map<String, Object> conf = new HashMap<>();
        if (StringUtils.isEmpty(certificateAuthority)) {
            conf.put("insecure-skip-tls-verify", Boolean.TRUE);
        } else {
            conf.put("certificate-authority-data", certificateAuthority);
        }
        conf.put("server", url);
        cluster.put("cluster", conf);
        clusters.add(cluster);

        ArrayList<Object> users = new ArrayList<>();
        Map<String, Object> user = new HashMap<>();
        user.put("name", userName);
        Map<String, String> tkn = new HashMap<>();
        tkn.put("token", token);
        user.put("user", tkn);
        users.add(user);

        // commons.lang version conflict we should not depend on io.kubernetes from this module
        // We need to rethink how to resolve version conflict
        Map<String, Object> ret = new HashMap<>();
        ret.put("contexts", contexts);
        ret.put("clusters", clusters);
        ret.put("users", users);
        ret.put("name", ctxName);

        return ret;
    }

}
