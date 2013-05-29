/*
 * Copyright 2012 Jeanfrancois Arcand
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.monansrill.atmosphere;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atmosphere.config.service.MeteorService;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.cpr.Meteor;
import org.atmosphere.websocket.WebSocketEventListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Simple PubSub resource that demonstrate many functionality supported by
 * Atmosphere JQuery Plugin (WebSocket, Comet) and Atmosphere Meteor extension.
 *
 * @author Jeanfrancois Arcand
 */
@MeteorService
public class MeteorPubSub extends HttpServlet implements Runnable {

    Logger logger = LoggerFactory.getLogger("MeteorPubSub");

    Thread t;
    @Override
	public void init() throws ServletException {
		// TODO Auto-generated method stub
		super.init();
		t = new Thread(this,"my timer");
		t.start();
	}

    @Override
	public void run() {
    	do {
	    	try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
			}
	    	
	    	Broadcaster b = lookupBroadcaster("/simple");
	        b.broadcast("The time is "+new Date());
    	} while(true);
	}
	@Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // Create a Meteor
        Meteor m = Meteor.build(req);
        logger.info("registering via GET meteor created");

        // Log all events on the console, including WebSocket events.
        m.addListener(new WebSocketEventListenerAdapter());

        res.setContentType("text/html;charset=ISO-8859-1");

        Broadcaster b = lookupBroadcaster(req.getPathInfo());
        logger.info("registering via GET broadcaster for "+req.getPathInfo()+" is "+b);
        m.setBroadcaster(b);
        m.resumeOnBroadcast(true).suspend(-1);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Broadcaster b = lookupBroadcaster(req.getPathInfo());

        String message = req.getReader().readLine();
        logger.info("messages for "+b+" is "+message);

        if (message != null && message.indexOf("message") != -1) {
            b.broadcast(message.substring("message=".length()));
        }
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

	
}
