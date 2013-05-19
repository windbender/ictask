package org.monansrill;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.util.JsonLoader;

public final class MyJsonUtils {

	
	    private static final String PKGBASE;

	    static {
	        final String pkgName = MyJsonUtils.class.getPackage().getName();
	        PKGBASE = '/' + pkgName.replace(".", "/");
	    }

	    private MyJsonUtils()
	    {
	    }

	    /**
	     * Load one resource from the current package as a {@link JsonNode}
	     *
	     * @param name name of the resource (<b>MUST</b> start with {@code /}
	     * @return a JSON document
	     * @throws IOException resource not found
	     */
	    public static JsonNode loadResource(final String name)
	        throws IOException
	    {
	        return JsonLoader.fromResource(PKGBASE + name);
	    }
	
}
