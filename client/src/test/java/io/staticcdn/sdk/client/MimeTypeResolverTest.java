package io.staticcdn.sdk.client;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class MimeTypeResolverTest {

    MimeTypeResolver mimeTypeResolver = new MimeTypeResolver();

    @Test
    public void testBasicFileTypes() throws Exception {
        assertEquals(mimeTypeResolver.resolveMime("foo.htm").render(), "text/html;charset=UTF-8");
        assertEquals(mimeTypeResolver.resolveMime("foo.html").render(), "text/html;charset=UTF-8");
        assertEquals(mimeTypeResolver.resolveMime("foo.css").render(), "text/css;charset=UTF-8");
        assertEquals(mimeTypeResolver.resolveMime("foo.js").render(), "application/javascript;charset=UTF-8");
        assertEquals(mimeTypeResolver.resolveMime("foo.json").render(), "application/json;charset=UTF-8");
        assertEquals(mimeTypeResolver.resolveMime("foo.xml").render(), "application/xml;charset=UTF-8");
        assertEquals(mimeTypeResolver.resolveMime("foo.png").render(), "image/png");
        assertEquals(mimeTypeResolver.resolveMime("foo.jpg").render(), "image/jpeg");
        assertEquals(mimeTypeResolver.resolveMime("foo.jpeg").render(), "image/jpeg");
        assertEquals(mimeTypeResolver.resolveMime("foo.txt").render(), "text/plain;charset=UTF-8");
    }

    @Test
    public void testUnsupportedType() throws Exception {
        assertNull(mimeTypeResolver.resolveMime("foo.xyzabc"));
    }


    @Test
    public void testBasicContentTypes() throws Exception {
        assertEquals(mimeTypeResolver.resolveMimeByContentType("text/html;charset=UTF-8").getExtension(), "html");
        assertEquals(mimeTypeResolver.resolveMimeByContentType("text/css;charset=UTF-8").getExtension(), "css");
        assertEquals(mimeTypeResolver.resolveMimeByContentType("application/javascript;charset=UTF-8").getExtension(), "js");
        assertEquals(mimeTypeResolver.resolveMimeByContentType("application/json;charset=UTF-8").getExtension(), "json");
        assertEquals(mimeTypeResolver.resolveMimeByContentType("application/xml;charset=UTF-8").getExtension(), "xml");
        assertEquals(mimeTypeResolver.resolveMimeByContentType("image/png").getExtension(), "png");
        assertEquals(mimeTypeResolver.resolveMimeByContentType("image/jpeg").getExtension(), "jpg");
    }
}