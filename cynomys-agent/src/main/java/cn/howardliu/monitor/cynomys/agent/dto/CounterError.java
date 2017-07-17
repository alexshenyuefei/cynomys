/*
 * Copyright 2008-2016 by Emeric Vernat
 *
 *     This file is part of Java Melody.
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
package cn.howardliu.monitor.cynomys.agent.dto;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Date;

/**
 * Objet correspondant à une erreur dans filtre http ou dans logs à un instant
 * t, avec son message, sa stackTrace éventuelle et son utilisateur courant s'il
 * est défini.
 *
 * @author Emeric Vernat
 */
public class CounterError implements Serializable {
    public static final String REQUEST_KEY = "wfj.netty.monitor.request";
    private static final long serialVersionUID = 5690702786722045646L;
    @SuppressWarnings("all")
    private static final ThreadLocal<HttpServletRequest> HTTP_SERVLET_REQUEST_CONTEXT = new ThreadLocal<HttpServletRequest>();
    private final long time;
    private final String remoteUser;
    private final String httpRequest;
    private final String message;
    private final String stackTrace;

    public CounterError(String message, String stackTrace) {
        super();
        assert message != null;
        this.time = System.currentTimeMillis();
        this.message = message;
        this.stackTrace = stackTrace;
        final HttpServletRequest currentRequest = getCurrentRequest();
        if (currentRequest == null) {
            this.remoteUser = null;
            this.httpRequest = null;
        } else {
            // unbindRequest pour éviter StackOverflowError dans le cas où
            // getRemoteUser()
            // fait un log warn ou error. Par exemple, dans le cas d'un SSO
            // comme
            // com.pixelpark.seraph.SSOAuthenticator.getUser (cf issue 24).
            unbindRequest();
            try {
                this.httpRequest = (String) currentRequest.getAttribute(REQUEST_KEY);
                this.remoteUser = currentRequest.getRemoteUser();
            } finally {
                bindRequest(currentRequest);
            }
        }
    }

    /**
     * Définit la requête http (peut être nulle) pour le thread courant.
     *
     * @param request HttpServletRequest
     */
    public static void bindRequest(HttpServletRequest request) {
        if (request != null) {
            HTTP_SERVLET_REQUEST_CONTEXT.set(request);
        }
    }

    /**
     * Retourne la requête http pour le thread courant ou null si elle n'a pas
     * été définie.
     *
     * @return HttpServletRequest
     */
    private static HttpServletRequest getCurrentRequest() {
        return HTTP_SERVLET_REQUEST_CONTEXT.get();
    }

    /**
     * Enlève le lien entre la requête http et le thread courant.
     */
    public static void unbindRequest() {
        HTTP_SERVLET_REQUEST_CONTEXT.remove();
    }

    public long getTime() {
        return time;
    }

    public Date getDate() {
        return new Date(time);
    }

    public String getRemoteUser() {
        return remoteUser;
    }

    public String getHttpRequest() {
        return httpRequest;
    }

    public String getMessage() {
        return message;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[message=" + getMessage() + ']';
    }
}
