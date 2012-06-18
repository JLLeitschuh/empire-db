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
package org.apache.empire.jsf2.websample.web;

import java.util.Map;

import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.context.FacesContext;

import org.apache.empire.exceptions.InvalidArgumentException;
import org.apache.empire.exceptions.ItemExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.faces.application.InjectionApplicationFactory;

public class SampleApplicationFactory extends ApplicationFactory
{
    private static final Logger  log = LoggerFactory.getLogger(SampleApplicationFactory.class);
    private volatile Application application;

    public SampleApplicationFactory()
    {
        log.info("SampleApplicationFactory created");
    }

    @Override
    public Application getApplication()
    {
        if (application == null)
        { // Create Application
            application = new SampleApplication();
            // InjectionApplicationFactory.setApplicationInstance(application);
            Map<String, Object> appMap = FacesContext.getCurrentInstance().getExternalContext().getApplicationMap();
            appMap.put(InjectionApplicationFactory.class.getName(), application);
            // log
            log.info("Fin2Application Application instance created");
        }
        return application;
    }

    @Override
    public void setApplication(Application application)
    {
        if (this.application != null)
            throw new ItemExistsException(this.application);
        if (!(application instanceof SampleApplication))
            throw new InvalidArgumentException("application", application);
        this.application = application;
    }

}
