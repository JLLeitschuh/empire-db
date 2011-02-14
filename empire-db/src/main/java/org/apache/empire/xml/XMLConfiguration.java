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
package org.apache.empire.xml;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.empire.commons.ErrorObject;
import org.apache.empire.commons.Errors;
import org.apache.empire.commons.ObjectUtils;
import org.apache.empire.commons.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * <PRE>
 * This class manages the configuration of a Java Bean by an xml configuration file.
 * It also supports configuration of Log4J.
 * </PRE>
 *
 */
public class XMLConfiguration extends ErrorObject
{
    private static final Logger log = LoggerFactory.getLogger(XMLConfiguration.class);

    private Element configRootNode = null;

    /**
     * Initialize the configuration.
     * 
     * @param filename the file
     * @param fromResource will read from the classpath if true
     * 
     * @return true on success
     */
    public boolean init(String filename, boolean fromResource)
    {
        // Read the properties file
        if (readConfiguration(filename, fromResource) == false)
            return false;
        // Done
        return success();
    }
    
    /**
     * returns the configuration root element or null if init() has not been called.
     * @return the configuration root element
     */
    public Element getRootNode()
    {
        return configRootNode;
    }

    /**
     * Reads the configuration file and parses the XML Configuration.
     */
    protected boolean readConfiguration(String fileName, boolean fromResource)
    {
        FileReader reader = null;
        InputStream inputStream = null;
        try
        {
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = null;
            if (fromResource)
            {   // Open Resource
                log.info("reading resource file: " + fileName);
                inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
                // Parse File
                doc = docBuilder.parse(inputStream);
            }
            else
            {   // Open File
                log.info("reading configuration file: " + fileName);
                reader = new FileReader(fileName);
                // Parse File
                doc = docBuilder.parse(new InputSource(reader));
            }
            // Get Root Element
            configRootNode = doc.getDocumentElement();
            return success();
        } catch (FileNotFoundException e)
        {
            log.error("Configuration file {} not found!", fileName, e);
            return error(Errors.FileNotFound, fileName);
        } catch (IOException e)
        {
            log.error("Error reading configuration file {}", fileName, e);
            return error(Errors.FileReadError, fileName);
        } catch (SAXException e)
        {
            log.error("Invalid XML in configuration file {}", fileName, e);
            return error(e);
        } catch (ParserConfigurationException e)
        {
            log.error("ParserConfigurationException: {}", e.getMessage(), e);
            return error(e);
        } finally
        { 
        	close(reader);
        	close(inputStream);
        }
    }

    public boolean readProperties(Object bean, String propertiesNodeName)
    {
        // Check state
        if (configRootNode == null)
            return error(Errors.ObjectNotValid, getClass().getName());
        // Check arguments
        if (bean == null)
            return error(Errors.InvalidArg, null, "bean");
        if (StringUtils.isEmpty(propertiesNodeName))
            return error(Errors.InvalidArg, null, "propertiesNodeName");
        // Get configuration node
        Element propertiesNode = XMLUtil.findFirstChild(configRootNode, propertiesNodeName);
        if (propertiesNode == null)
        { // Configuration
            log.error("Property-Node {} has not been found.", propertiesNodeName);
            return error(Errors.ItemNotFound, propertiesNodeName);
        }
        // configure Connection
        log.info("reading bean properties from node: {}", propertiesNodeName);
        NodeList nodeList = propertiesNode.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++)
        {
            Node item = nodeList.item(i);
            if (item.getNodeType() != Node.ELEMENT_NODE)
                continue;
            // Get the Text and set the Property
            setPropertyValue(bean, item);
        }
        // done
        return success();
    }
    
    protected void setPropertyValue(Object bean, Node item)
    {
        // Get the Text and set the Property
        String name = item.getNodeName();
        try
        {
            String newValue = XMLUtil.getElementText(item);
            BeanUtils.setProperty(bean, name, newValue);

            Object value = BeanUtils.getProperty(bean, name);
            if (ObjectUtils.compareEqual(newValue, value))
            {
            	log.info("Configuration property '{}' set to \"{}\"", name, newValue);
            }
            else
            {
            	log.error("Failed to set property '{}'. Value is \"{}\"", name, value);
            }

        } catch (IllegalAccessException e)
        {
            log.error("Access to Property {} denied.", name);
        } catch (InvocationTargetException e)
        {
            log.error("Unable to set Property {}", name);
        } catch (NoSuchMethodException e)
        {
            log.error("Property '{}' not found in {}", name, bean.getClass().getName());
        }
    }
    
	private void close(final Closeable closeable) {
		if (closeable != null)
		{
			try
			{
				closeable.close();
			}
			catch(IOException e)
			{
				log.debug(e.getMessage());
			}
		}
	}

}
