package org.monansrill;
/**
 * Copyright Chris Schaefer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AuthFilter implements Filter {

    Logger logger = LoggerFactory.getLogger(AuthFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		Cookie[] cookies = ((HttpServletRequest) request).getCookies();
		boolean auth=false;
		if(cookies != null) {
			logger.debug("there are cookies!!!");
			for(Cookie c : cookies) {			
				logger.debug("one is :"+c.getName()+"->"+c.getValue());
				if(c.getName().equals(AuthServlet.cookieName)) {
					if(isTokenValid(c)) {
						auth = true;
					}
					
				}
			}
		}
		if(!auth) {
			logger.debug("ok failing this .. not authed");
			((HttpServletResponse) response).setStatus(401);
		} else {
			chain.doFilter(request, response);
		}
	}

	private boolean isTokenValid(Cookie cookie) {
		LoginToken lt = new LoginToken(cookie);
		
		if ( AuthServlet.tokens.contains(lt) ) {
			return true;
		}
		return false;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}
