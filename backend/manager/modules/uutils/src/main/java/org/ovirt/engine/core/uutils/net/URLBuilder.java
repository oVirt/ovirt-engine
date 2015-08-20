package org.ovirt.engine.core.uutils.net;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class URLBuilder {

    String base;
    String suffix;
    List<String> parameters = new ArrayList<>();

    public URLBuilder(URL url) {
        this(url, null);
    }

    public URLBuilder(URL url, String suffix) {
        this(url.toString(), suffix);
    }

    public URLBuilder(String base) {
        this(base, null);
    }

    public URLBuilder(String base, String suffix) {
        this.base = base;
        this.suffix = suffix;
    }

    public URLBuilder setSuffix(String suffix) {
        this.suffix = suffix;
        return this;
    }

    public URLBuilder addParameter(String name, String value) {
        try {
            parameters.add(
                URLEncoder.encode(name, "UTF-8") +
                "=" +
                URLEncoder.encode(value, "UTF-8")
            );
            return this;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public String build() throws MalformedURLException {
        StringBuilder ret = new StringBuilder();

        ret.append(base);
        if (suffix != null) {
            ret.append(suffix);
        }
        boolean addAmp = false;
        if (!parameters.isEmpty()) {
            String query = new URL(ret.toString()).getQuery();
            if (query == null) {
                ret.append('?');
            } else if (!query.isEmpty()) {
                addAmp = true;
            }
        }
        for (String parameter : parameters) {
            if (addAmp) {
                ret.append('&');
            }
            addAmp = true;
            ret.append(parameter);
        }

        return ret.toString();
    }

    public URL buildURL() throws MalformedURLException {
        return new URL(build());
    }

}
