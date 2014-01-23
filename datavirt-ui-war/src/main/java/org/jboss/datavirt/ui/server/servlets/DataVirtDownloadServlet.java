/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.datavirt.ui.server.servlets;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.jboss.datavirt.ui.client.shared.beans.Constants;
import org.jboss.datavirt.ui.server.services.VdbService;
import org.overlord.sramp.atom.MediaType;

/**
 * A standard servlet that makes it easy to download VDB content.
 *
 * @author mdrillin@redhat.com
 */
public class DataVirtDownloadServlet extends HttpServlet {

	private static final long serialVersionUID = DataVirtDownloadServlet.class.hashCode();

    @Inject
    protected VdbService vdbService;

    /**
	 * Constructor.
	 */
	public DataVirtDownloadServlet() {
	}

	/**
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
			IOException {
        HttpServletResponse httpResponse = resp;
		try {
	        String vdbName = req.getParameter("vdbname"); //$NON-NLS-1$
			
    		String vdbXml =vdbService.getVdbXml(vdbName);

			doDownloadVdb(httpResponse, vdbName + Constants.DYNAMIC_VDB_SUFFIX, new ByteArrayInputStream(vdbXml.getBytes("UTF-8")));
			
		} catch (Exception e) {
			// TODO throw sensible errors (http responses - 404, 500, etc)
			throw new ServletException(e);
		}
	}
	
    protected void doDownloadVdb(HttpServletResponse httpResponse, String fileName, InputStream inputStream) throws Exception {
        try {
            // Set the content-disposition
            String disposition = String.format("attachment; filename=\"%1$s\"", fileName); //$NON-NLS-1$
            httpResponse.setHeader("Content-Disposition", disposition); //$NON-NLS-1$

            // Set the content-type
            httpResponse.setHeader("Content-Type", MediaType.APPLICATION_XML_TYPE.toString()); //$NON-NLS-1$

            // Make sure the browser doesn't cache it
            Date now = new Date();
            httpResponse.setDateHeader("Date", now.getTime()); //$NON-NLS-1$
            httpResponse.setDateHeader("Expires", now.getTime() - 86400000L); //$NON-NLS-1$
            httpResponse.setHeader("Pragma", "no-cache"); //$NON-NLS-1$ //$NON-NLS-2$
            httpResponse.setHeader("Cache-control", "no-cache, no-store, must-revalidate"); //$NON-NLS-1$ //$NON-NLS-2$

            IOUtils.copy(inputStream, httpResponse.getOutputStream());
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

}
