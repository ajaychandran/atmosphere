/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.atmosphere.client;

import org.atmosphere.cpr.PerRequestBroadcastFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Filter that inject Javascript code to a broadcast so it can be used with the Atmosphere JQuery Plugin.
 *
 * @author Jeanfrancois Arcand
 */
public class JavascriptClientFilter implements PerRequestBroadcastFilter {

    private final AtomicInteger uniqueScriptToken = new AtomicInteger();

    @Override
    public BroadcastAction filter(Object originalMessage, Object message) {

        if (message instanceof String) {
            StringBuilder sb = new StringBuilder("<script id=\"atmosphere_")
                    .append(uniqueScriptToken.getAndIncrement())
                    .append("\">")
                    .append("parent.callback")
                    .append("('")
                    .append(message.toString())
                    .append("');</script>");
            message = sb.toString();
        }
        return new BroadcastAction(BroadcastAction.ACTION.CONTINUE, message);
    }

    @Override
    public BroadcastAction filter(HttpServletRequest request, HttpServletResponse response, Object message) {

        if (request.getHeader("User-Agent") != null && request.getAttribute("X-Atmosphere-Transport") == null
                || request.getAttribute("X-Atmosphere-Transport") != null && ((String)request.getAttribute("X-Atmosphere-Transport")).equalsIgnoreCase("long-polling")) {
            String userAgent = request.getHeader("User-Agent").toLowerCase();
            if (userAgent != null && userAgent.startsWith("opera") && message instanceof String) {
                StringBuilder sb = new StringBuilder("<script id=\"atmosphere_")
                        .append(uniqueScriptToken.getAndIncrement())
                        .append("\">")
                        .append("window.parent.$.atmosphere.streamingCallback")
                        .append("('")
                        .append(message.toString())
                        .append("');</script>");
                message = sb.toString();
                return new BroadcastAction(BroadcastAction.ACTION.CONTINUE, message);
            }
        }
        return new BroadcastAction(BroadcastAction.ACTION.CONTINUE, null);
    }
}





