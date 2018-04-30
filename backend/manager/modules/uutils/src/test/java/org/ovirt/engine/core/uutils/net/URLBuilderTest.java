package org.ovirt.engine.core.uutils.net;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class URLBuilderTest {

    @Test
    public void test1() throws Exception {
        assertEquals(
            "http://www.google.com",
            new URLBuilder("http://www.google.com").build()
        );
        assertEquals(
            "http://www.google.com?",
            new URLBuilder("http://www.google.com?").build()
        );
        assertEquals(
            "http://www.google.com/x",
            new URLBuilder("http://www.google.com", "/x").build()
        );
        assertEquals(
            "http://www.google.com/test1?a=b&c=d",
            new URLBuilder("http://www.google.com/test1?")
                .addParameter("a", "b")
                .addParameter("c", "d")
                .build()
        );
        assertEquals(
            "http://www.google.com/test1?a=b&c=d",
            new URLBuilder("http://www.google.com", "/test1?")
                .addParameter("a", "b")
                .addParameter("c", "d")
                .build()
        );
        assertEquals(
            "http://www.google.com/test1?a=b&c=d&X=Y",
            new URLBuilder("http://www.google.com", "/test1")
                .addParameter("a", "b")
                .addParameter("c", "d")
                .addParameter("X", "Y")
                .build()
        );
        assertEquals(
            "http://www.google.com/test1?a=b&c=d&X=Y&a=bb",
            new URLBuilder("http://www.google.com", "/test1")
                .addParameter("a", "b")
                .addParameter("c", "d")
                .addParameter("X", "Y")
                .addParameter("a", "bb")
                .build()
        );
        assertEquals(
            "http://www.google.com/test1?A=B&a=b&c=d",
            new URLBuilder("http://www.google.com", "/test1?A=B")
                .addParameter("a", "b")
                .addParameter("c", "d")
                .build()
        );
    }

}
