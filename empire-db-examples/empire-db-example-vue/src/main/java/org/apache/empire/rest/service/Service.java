/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.empire.rest.service;

import java.sql.Connection;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.empire.exceptions.EmpireException;
import org.apache.empire.rest.app.SampleServiceApp;
import org.apache.empire.rest.app.TextResolver;
import org.apache.empire.rest.json.JsoErrorInfo;
import org.apache.empire.vue.sample.db.RecordContext;
import org.apache.empire.vue.sample.db.SampleDB;
import org.glassfish.jersey.server.ContainerRequest;

public abstract class Service {

    /**
     * Some constants used by the services
     * @author doebele
     */
    public static class Consts {
    
        public static final String  LOGIN_COOKIE_NAME       = "EmployeeVueLoginCookie";
        
        public static final String  ATTRIBUTE_DB            = "db";
    
        public static final String  ATTRIBUTE_CONNECTION    = "connection";
    
        public static final String  ATTRIBUTE_DATASOURCE    = "ds";
    
        public static final String  ATTRIBUTE_CONFIG        = "config";
    
    }

    /**
     * Implementation for RecordContext
     * Holds a connection and therefore must not live for longer than the request  
     * @author doebele
     */
    public static class ServiceRecordContext implements RecordContext
    {
        private final Connection conn;
        private final TextResolver textResolver;
        
        public ServiceRecordContext(Connection conn)
        {
            this.conn = conn;
            this.textResolver = SampleServiceApp.instance().getTextResolver(Locale.ENGLISH);
        }

        @Override
        public Connection getConnection()
        {
            return conn;
        }

        @Override
        public TextResolver getTextResolver()
        {
            return textResolver;
        }
    }
    
	@Context
	private ServletContext			context;

	@Context
	private ContainerRequestContext	containerRequestContext;

	@Context
	private HttpServletRequest		req;

	public SampleDB getDatabase() {
		return (SampleDB) this.context.getAttribute(Service.Consts.ATTRIBUTE_DB);
	}

	public Connection getConnection() {
		ContainerRequest containerRequest = (ContainerRequest) this.containerRequestContext;
		return (Connection) containerRequest.getProperty(Service.Consts.ATTRIBUTE_CONNECTION);
	}

    public RecordContext getRecordContext() {
        return new ServiceRecordContext(getConnection());
    }

	public ServletRequest getServletRequest() {
		return this.req;
	}

	/**
	 * EmpireException
	 * @param e the exception
	 * @param ctx the record context
     * @return the error response
	 */
    
	public Response getErrorResponse(EmpireException e, RecordContext ctx) {
        return Response.serverError().entity(new JsoErrorInfo(e, ctx.getTextResolver())).build();
    }

	/**
	 * Multiple Exceptions
	 * @param extns the list of exceptions
     * @param ctx the record context
	 * @return the error response
	 */
	public Response getErrorResponse(List<? extends EmpireException> extns, RecordContext ctx) {
        return Response.serverError().entity(new JsoErrorInfo(extns, ctx.getTextResolver())).build();
    }
	
	/**
	 * General error
	 * @param e
     * @return the error response
	 */
    public Response getErrorResponse(Exception e) {
        return Response.serverError().build();
    }

	public String getRemoteAddr() {
		// TODO check X-Real-IP / X-Forwarded-For
		return this.req.getRemoteAddr();
	}

}
