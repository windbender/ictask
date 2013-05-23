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
					// /ictask/jictask/items/517c6e71e4b08e87c950fcb9
					logger.info("this request is on uri "+uri);
					// {"votes":["chris"],"notes":["I Did the trail between CnA's toward Maija and Gabriels.\n\nStill need trails below and to east of Uta and Ken's"
					//,{"who":"chris","note":"I did the trail from CnA  to MnG"},{"who":"chris","note":"third note"}]
					//,"desc":"weed whip paths.  hillhouse to chris and amy.    ChrisAndAmy to newmans and road to Maija and Gabe.    Road nr Ken's to Hub.   Hub to Linda's"
					//,"jobs":[{"job":"shepherd","name":"chris"}
					//,{"job":"breaking my program","name":"renee"}],"addedBy":"chris","committee":"Land Plans","sizeInHours":3
					//,"addedDate":"2013-04-28T00:33:52.611Z","doneDate":""}
					logger.info("and the message is "+message);
					
					Broadcaster b = lookupBroadcaster(uri);
					b.broadcast(message);
					
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
