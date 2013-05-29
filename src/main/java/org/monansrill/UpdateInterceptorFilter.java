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

import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UpdateInterceptorFilter implements Filter {

    Logger logger = LoggerFactory.getLogger(UpdateInterceptorFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
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
					InputStream is = wrappedRequest.getInputStream();
					String message = convertStreamToString(is);
					// broadcast the request body somehow!!!
					String uri = wrappedRequest.getRequestURI();

					logger.info("this request is on uri "+uri);
					String part = null;
					String id = null;
					if(method.equals("POST")) {
						// new something
						part = uri;
					} else {
						// DELETE or PUT
						int x = uri.lastIndexOf("/");
						if(x > 0) {
							part = uri.substring(0,x);
							id = uri.substring(x+1);
						}
					}
					logger.info("message broadcast channel will be"+part);
					
					ObjectMapper mapper = new ObjectMapper();
					JsonNode n = mapper.readValue(message,JsonNode.class);
					logger.info("and the message is "+message);
					RestAction ra = new RestAction(method.toString(),id,n);
					String toSend = mapper.writeValueAsString(ra);
					logger.info("and finally "+toSend);
					String nuri = part.replaceAll("/", "");

					logger.info("the broadcaster name is "+nuri);
					Broadcaster b = lookupBroadcaster(nuri);
					logger.info("the broadcaster is "+b);
					b.broadcast(toSend);
					
					chain.doFilter(wrappedRequest, response);
				}
			}
		}
	}

	public static String convertStreamToString(java.io.InputStream is) {
	    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
	
	Broadcaster lookupBroadcaster(String pathInfo) {
        String[] decodedPath = pathInfo.split("/");
        Broadcaster b;
        if (decodedPath.length > 0) {
            b = BroadcasterFactory.getDefault().lookup(decodedPath[decodedPath.length - 1], true);
        } else {
            b = BroadcasterFactory.getDefault().lookup("/", true);
        }
        return b;
    }
	
	@Override
	public void destroy() {

	}

}
