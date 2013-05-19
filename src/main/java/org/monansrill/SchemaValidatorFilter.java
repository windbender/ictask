package org.monansrill;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.report.ProcessingReport;


public class SchemaValidatorFilter implements Filter {

    Logger logger = LoggerFactory.getLogger(SchemaValidatorFilter.class);

	JsonSchema schema = null;
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		try {
			final JsonNode fstabSchema = MyJsonUtils.loadResource("/taskSchema.json");
			final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
			schema = factory.getJsonSchema(fstabSchema);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		if(request instanceof HttpServletRequest) {
			HttpServletRequest hsr = (HttpServletRequest) request;
			String method = hsr.getMethod();
			if(method.equals("GET")) {
				chain.doFilter(request, response);
			} else {
				if( ((HttpServletRequest) request).getRequestURI().contains("archived") ) {
					chain.doFilter(request, response);
				} else {
					MultiReadHttpServletRequest wrappedRequest = new MultiReadHttpServletRequest( (HttpServletRequest) request);
					try {
						ObjectMapper mapper = new ObjectMapper();
	//					JsonFactory jf = mapper.getJsonFactory();
						
						InputStream is = wrappedRequest.getInputStream();
						JsonNode jn = mapper.readTree(is);
	
						
	//					JsonParser jp = jf.createJsonParser(is);
	//					JsonNode jn = jp.readValueAsTree();
						ProcessingReport report = schema.validate(jn);
						if(report.isSuccess()) {
							chain.doFilter(wrappedRequest, response);
						} else {
							logger.debug("ok failing this .. JSON did not parse");
							((HttpServletResponse) response).setStatus(415);
						}
					} catch (ProcessingException e) {
						logger.debug("ok failing this .. JSON did not parse because ",e);
						((HttpServletResponse) response).setStatus(415);
					}
				}
				
			}
		}
		
        
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	
}
