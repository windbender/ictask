package org.monansrill.password;

import java.io.UnsupportedEncodingException;

/**
 * Can replace this class with {@code org.apache.commons.codec.binary.StringUtils}
 * when we upgrade to commons-codec 1.4.
 */
class StringUtils
{
    private static final String UTF8 = "UTF-8";

    /**
     * Copy of StringUtils.getBytesUtf8 from commons-codec 1.4.
     */
    public static byte[] getBytesUtf8(String string)
    {
        if (string == null)
        {
            return null;
        }
        try
        {
            return string.getBytes(UTF8);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new IllegalStateException("Character set not found: " + UTF8);
        }
    }

    /**
     * Copy of StringUtils.newStringUtf8 from commons-codec 1.4.
     */
    public static String newStringUtf8(byte[] bytes)
    {
        if (bytes == null)
        {
            return null;
        }
        try
        {
            return new String(bytes, UTF8);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new IllegalStateException("Character set not found: " + UTF8);
        }
    }
}
