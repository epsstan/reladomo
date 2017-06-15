package com.gs.fw.common.mithra.generator.annotationprocessor.compiler;

import javax.tools.SimpleJavaFileObject;
import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;

public class StringFileObject extends SimpleJavaFileObject
{
    private String src;

    /**
     * Construct a SimpleJavaFileObject of the given kind and with the
     * given URI.
     *
     * @param uri  the URI for this file object
     * @param kind the kind of this file object
     */
    protected StringFileObject(URI uri, Kind kind)
    {
        super(uri, kind);
    }

    public StringFileObject(String fullClassName, String src)
    {
        super(URI.create(fullClassName.replaceAll("\\.", "\\") + Kind.SOURCE.extension), Kind.SOURCE);
        this.src = src;
    }

    @Override
    public InputStream openInputStream() throws IOException
    {
        return new ByteArrayInputStream(src.getBytes(Charset.defaultCharset()));
    }

    @Override
    public Reader openReader(boolean ignoreEncodingErrors) throws IOException
    {
        return new StringReader(src);
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException
    {
        return src;
    }
}
