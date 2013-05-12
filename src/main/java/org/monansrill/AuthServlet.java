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
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.monansrill.password.DefaultPasswordEncoder;
import org.monansrill.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class AuthServlet extends HttpServlet {

    Logger logger = LoggerFactory.getLogger(AuthServlet.class);

    static String isDebugString = ConfluenceDBBasedUserInfoGetter.getSecretProperties().getProperty("isdebug");
	
	
	public static boolean debugging;
	static {
		if(isDebugString != null) {
			debugging = Boolean.parseBoolean(isDebugString);
		}
	}
    PasswordEncoder pe = DefaultPasswordEncoder.getDefaultEncoder();

    ConfluenceDBBasedUserInfoGetter getter = new ConfluenceDBBasedUserInfoGetter();
    
    ObjectMapper mapper = new ObjectMapper();

	private static String tokenName = "loggedInTokens";
	public static String cookieName = "proxyAuth";

	public static Set<LoginToken> tokens = new HashSet<LoginToken>();
	
	private SecureRandom random = new SecureRandom();

    
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		logger.debug("req->"+req);

		if(req.getPathInfo().contains("login")) {
			
			java.util.Scanner s = new java.util.Scanner(req.getInputStream()).useDelimiter("\\A");
		    String q =  s.hasNext() ? s.next() : "";
		    UserAuth userAuth = mapper.readValue(q, UserAuth.class);
		    logger.debug("ok userAuth is "+userAuth);
		    UserInfo userInfo = getter.getUserInfoFor(userAuth.getUsername());
		    if(userInfo != null) {
		    	logger.debug("ok userInfo is "+userInfo);
		    	if((debugging) || (pe.isValidPassword(userAuth.getPassword(), userInfo.getCredential()))) {
		    		logger.debug("and that is valid");
			    	Cookie cookie = makeCookie(userAuth,userInfo);
			    	resp.addCookie(cookie);
			    	resp.setStatus(HttpServletResponse.SC_OK);
			    	tokens.add(new LoginToken(cookie));
			    } else {
			    	logger.debug("and NOT valid");
			    	resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
			    }
		    } else {
		    	logger.debug("no user");
		    	resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
		    
		    }
		} else if(req.getPathInfo().contains("logout")) {
			Cookie[] cookies = ((HttpServletRequest) req).getCookies();
			boolean auth=false;
			if(cookies != null) {
				logger.debug("there are cookies!!!");
				for(Cookie c : cookies) {			
					logger.debug("one is :"+c.getName()+"->"+c.getValue());
					if(c.getName().equals(cookieName)) {
						LoginToken lt = new LoginToken(c);
						tokens.remove(lt);
					}
				}
			}
						
	    	Cookie cookie = makeEraseCookie();
	    	resp.addCookie(cookie);
	    	resp.setStatus(HttpServletResponse.SC_OK);
		}
	    
	}

	private Cookie makeEraseCookie() {
		Cookie c = new Cookie(cookieName,null);
		c.setPath("/ictask/");
		return c;
	}

	private Cookie makeCookie(UserAuth userAuth, UserInfo userInfo) {
	    String s = new BigInteger(130, random).toString(32);
		String val =userAuth.getUsername()+":"+s;
		Cookie c = new Cookie(cookieName,val);
		c.setPath("/ictask/");
		return c;
	}

	
}
