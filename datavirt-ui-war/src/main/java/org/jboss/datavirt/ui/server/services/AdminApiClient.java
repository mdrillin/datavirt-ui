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
package org.jboss.datavirt.ui.server.services;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.jboss.datavirt.ui.server.services.util.StringUtil;
import org.jboss.datavirt.ui.server.services.util.TranslatorHelper;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.client.SrampClientException;
import org.teiid.adminapi.Admin;
import org.teiid.adminapi.AdminException;
import org.teiid.adminapi.AdminFactory;
import org.teiid.adminapi.PropertyDefinition;
import org.teiid.adminapi.Translator;
import org.teiid.adminapi.VDB;
import org.teiid.adminapi.impl.VDBMetaData;

/**
 * Class used to communicate with the Teiid Server Admin API
 *
 * @author mdrillin@redhat.com
 */
public class AdminApiClient {

    private static final String DRIVER_KEY = "driver-name";
    private static final String CLASSNAME_KEY = "class-name";

	private Admin admin;
	private String endpoint;
	private boolean validating;
	//private AuthenticationProvider authProvider;
	private Locale locale;

	/**
	 * Constructor.
	 * @param endpoint
	 */
	public AdminApiClient() {
		if(admin==null) {
		    try {
				admin = getAdminApi("localhost",9999,"admin","1admin1!");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
	}
	
    /**
     * Get an admin api connection with the supplied credentials
     * @param serverHost the server hostname
     * @param serverPort the server port number
     * @param userName the username
     * @param password the password
     * @return the admin api
     */
    private Admin getAdminApi (String serverHost, int serverPort, String userName, String password) throws Exception {
            Admin admin = null;
            try {
                    admin = AdminFactory.getInstance().createAdmin(serverHost, serverPort, userName, password.toCharArray());
            } catch (Exception e) {
                    throw new Exception(e.getMessage());
            }
            if(admin==null) {
                    StringBuffer sb = new StringBuffer("Unable to establish Admin API connection. Please check the supplied credentials: \n");
                    sb.append("\n [Host]: "+serverHost);
                    sb.append("\n [Port]: "+serverPort);
                    
                    throw new Exception(sb.toString());
            }
            return admin;
    }

	/**
	 * Constructor.
	 * @param endpoint
	 */
	public AdminApiClient(String endpoint) {
		this.endpoint = endpoint;
		if (this.endpoint.endsWith("/")) { //$NON-NLS-1$
			this.endpoint = this.endpoint.substring(0, this.endpoint.length()-1);
		}
		if (!this.endpoint.endsWith("/s-ramp")) { //$NON-NLS-1$
		    this.endpoint += "/s-ramp"; //$NON-NLS-1$
		}
	}

	/**
	 * Constructor.
	 * @param endpoint
	 * @param validating
	 * @throws SrampClientException
	 * @throws SrampAtomException
	 */
	public AdminApiClient(String endpoint, boolean validating) {
		this(endpoint);
		this.validating = validating;
		if (this.validating) {
			//discoverAvailableFeatures();
		}
	}
	
    /*
     * Get the Collection of DataSource Summary properties
     * @return the Map of DataSource name - type
     */
	public Collection<Properties> getDataSourceSummaryPropsCollection() throws AdminApiClientException {
		if(this.admin==null) return Collections.emptyList();

		Collection<Properties> dsSummaryPropCollection = new ArrayList<Properties>();
		
		// Get Collection of DataSource Names
		Collection<String> sourceNames = getDataSourceNames();
		// For each datasource, get the 'summary' properties
		for(String sourceName : sourceNames) {
			Properties summaryProperties = new Properties();
			summaryProperties.put("name", sourceName);
			String sourceType = getDataSourceType(sourceName);
			summaryProperties.put("type",sourceType);
			dsSummaryPropCollection.add(summaryProperties);
		}
		
		return dsSummaryPropCollection;
	}
	
    /**
     * Create a Data Source
     * @param deploymentName the name of the deployment
     * @param templateName the source template name
     * @param props the datasource properties
     */
	public void createDataSource(String deploymentName, String templateName, Properties props) throws AdminApiClientException {
		if(this.admin!=null) {
			try {
				this.admin.createDataSource(deploymentName, templateName, props);
			} catch (AdminException e) {
				throw new AdminApiClientException(e.getMessage());
			}
		}
	}
	
    /**
     * Get the current Collection of DataSource names
     * @return the collection of DataSource names
     */
	public Collection<String> getDataSourceNames() throws AdminApiClientException {
		if(this.admin==null) return Collections.emptyList();

		// Get list of DataSource Names
		Collection<String> sourceNames = null;
		try {
			sourceNames = this.admin.getDataSourceNames();
		} catch (AdminException e) {
			throw new AdminApiClientException(e.getMessage());
		}

		if(sourceNames!=null) {
			return sourceNames;
		} else {
			return Collections.emptyList();
		}
	}

	/*
     * Get the current Collection of Translators
     * @return the collection of translators
     */
	public Collection<String> getTranslatorNames() throws AdminApiClientException {
		if(this.admin==null) return Collections.emptyList();

		// Get list of Translators
		Collection<? extends Translator> translators = null;
		try {
			translators = this.admin.getTranslators();
		} catch (AdminException e) {
			throw new AdminApiClientException(e.getMessage());
		}

        // return the names
		List<String> transNames = new ArrayList<String>(translators.size());
		for(Translator translator : translators) {
			if(translator!=null) {
				transNames.add(translator.getName());
			}
		}

		return transNames;
	}
    
    /*
     * Get the Properties for the supplied DataSource name
     * @param dataSourceName the name of the datasource
     * @return the properties for the DataSource names
     */
	public Properties getDataSourceProperties(String dataSourceName) throws AdminApiClientException {
		if(this.admin==null) return null;

		// Get the DataSource properties
		Properties dsProps = null;
		try {
			dsProps = admin.getDataSource(dataSourceName);
		} catch (AdminException e) {
			throw new AdminApiClientException(e.getMessage());
		}

		return dsProps;
	}
	
	/**
	 * Get a Map of default translators for the dataSources
	 * @return the dsName to default translator mappings
	 */
	public Map<String,String> getDefaultTranslatorMap( ) throws AdminApiClientException {
		Map<String,String> defaultTranslatorsMap = new HashMap<String,String>();

		Collection<String> sourceNames = getDataSourceNames();
		Collection<String> translators = getTranslatorNames();

		for(String sourceName : sourceNames) {
			String dsDriver = getDataSourceType(sourceName);
			String defaultTranslator = TranslatorHelper.getTranslator(dsDriver,translators);
			defaultTranslatorsMap.put(sourceName,defaultTranslator);
		}
		return defaultTranslatorsMap;
	}
    
    /**
     * Get the Type(driver) for the supplied DataSource name
     * @param dataSourceName the name of the data source
     * @return the DataSource type
     */
	public String getDataSourceType(String dataSourceName) throws AdminApiClientException {
		if(this.admin==null) return null;

		// Get the DataSource properties
		Properties dsProps = getDataSourceProperties(dataSourceName);
		
		return getDataSourceType(dsProps);
	}
	
	/*
	 * Get the source type from the provided properties
	 * @param dsProps the data source properties
	 * @return the dataSource type name
	 */
	private String getDataSourceType(Properties dsProps) {
		if(dsProps==null) return "unknown";

		String driverName = dsProps.getProperty(DRIVER_KEY);
		// If driver-name not found, look for class name and match up the .rar
		if(StringUtil.isEmpty(driverName)) {
			String className = dsProps.getProperty(CLASSNAME_KEY);
			if(!StringUtil.isEmpty(className)) {
				driverName = TranslatorHelper.getDriverNameForClass(className);
			}
		}
		return driverName;
	}
	    
    /*
     * Get the current Collection of DataSource names
     * @return the collection of DataSource names
     */
	public Collection<String> getDataSourceTypes() throws AdminApiClientException {
		if(this.admin==null) return Collections.emptyList();

		// Get list of DataSource Names
		Collection<String> sourceTypes = null;
		try {
			sourceTypes = this.admin.getDataSourceTemplateNames();
		} catch (AdminException e) {
			throw new AdminApiClientException(e.getMessage());
		}

		if(sourceTypes!=null) {
			return sourceTypes;
		} else {
			return Collections.emptyList();
		}
	}

	public Collection<? extends PropertyDefinition> getDataSourceTypePropertyDefns(String typeName) throws AdminApiClientException {
		if(this.admin==null) return Collections.emptyList();

		// Get list of DataSource Names
		Collection<? extends PropertyDefinition> propDefns = null;
		try {
			propDefns = this.admin.getTemplatePropertyDefinitions(typeName);
		} catch (AdminException e) {
			throw new AdminApiClientException(e.getMessage());
		}

		if(propDefns!=null) {
			return propDefns;
		} else {
			return Collections.emptyList();
		}
	}

	/*
     * Delete the supplied DataSource. 
     * @param dataSourceName the DataSource name
     */
	public void deleteDataSource(String dataSourceName) throws AdminApiClientException {
		if(this.admin==null) return;

		// Get list of DataSource Names. If 'sourceName' is found, delete it...
		Collection<String> dsNames;
		try {
			dsNames = admin.getDataSourceNames();
		} catch (AdminException e1) {
			throw new AdminApiClientException(e1.getMessage());
		}
		if(dsNames.contains(dataSourceName)) {
			try {
				// Undeploy the working VDB
				admin.deleteDataSource(dataSourceName);
			} catch (Exception e) {
				throw new AdminApiClientException(e.getMessage());
			}
		}
	}

	/*
     * Delete the supplied list of DataSources
     * @param dataSourceNames the collection of datasources
     */
	public void deleteDataSources(Collection<String> dataSourceNames) throws AdminApiClientException {
		for(String dsName : dataSourceNames) {
			deleteDataSource(dsName);
		}
	}
	
	/*
     * Delete the supplied list of DataSource types
     * @param dataSourceTypeNames the collection of datasource types
     */
	public void deleteDataSourceTypes(Collection<String> dataSourceTypeNames) throws AdminApiClientException {
		if(this.admin==null) return;

		// Delete the specified DataSource types
		try {
			// Get current list of DS Templates. If 'dsTypeName' is found, undeploy it...
			Collection<String> currentTemplates = (Collection<String>) this.admin.getDataSourceTemplateNames();

			for(String dsTypeName : dataSourceTypeNames) {
				if(currentTemplates.contains(dsTypeName)) {
					// Undeploy the datasource type
					admin.undeploy(dsTypeName);
				}
			}
		} catch (AdminException e) {
			throw new AdminApiClientException(e.getMessage());
		}
	}
	
	/*
     * Deploy the supplied content
     * @param deploymentName the deployment name
     * @param content the deployment content
     */
	public void deploy(String deploymentName, InputStream content) throws AdminApiClientException {
		if(this.admin==null) return;

		// Delete the specified DataSource types
		try {
			// Undeploy the datasource type
			admin.deploy(deploymentName, content);
		} catch (AdminException e) {
			throw new AdminApiClientException(e.getMessage());
		}
	}
	
	/*
     * Undeploy the supplied content
     * @param deploymentName the deployment name
     */
	public void undeploy(String deploymentName) throws AdminApiClientException {
		if(this.admin==null) return;

		// Delete the specified DataSource types
		try {
			// Undeploy the datasource type
			admin.undeploy(deploymentName);
		} catch (AdminException e) {
			throw new AdminApiClientException(e.getMessage());
		}
	}
	
    /*
     * Get the current Collection of Vdb names
     * @return the collection of Vdb names
     */
	public Collection<String> getVdbNames(boolean includeDynamic, boolean includeArchive, boolean includePreview) throws AdminApiClientException {
		if(this.admin==null) return Collections.emptyList();

		// Get list of VDB Names
		Collection<? extends VDB> vdbs = null;
		try {
			vdbs = this.admin.getVDBs();
		} catch (AdminException e) {
			throw new AdminApiClientException(e.getMessage());
		}

		if(vdbs!=null) {
			// Get VDB names
			Collection<String> vdbNames = new ArrayList<String>();
			for(VDB vdb : vdbs) {
				VDBMetaData vdbMeta = (VDBMetaData)vdb;
				String vdbName = vdbMeta.getName();
				boolean isDynamic = vdbMeta.isXmlDeployment();
				boolean isPreview = vdbMeta.isPreview();
				
				// Dynamic VDB
				if(isDynamic) {
					if(includeDynamic) vdbNames.add(vdbName);
				// Archive VDB
				} else if(includeArchive) {
					if(!isPreview) {
						vdbNames.add(vdbName);
					} else if(includePreview) {
						vdbNames.add(vdbName);
					}
				}
			}
			return vdbNames;
		} else {
			return Collections.emptyList();
		}
	}
	
    /*
     * Get the current Collection of Vdb properties
     * @return the collection of Vdb properties
     */
	public Collection<Properties> getVdbSummaryPropCollection(boolean includeDynamic, boolean includeArchive, boolean includePreview) throws AdminApiClientException {
		if(this.admin==null) return Collections.emptyList();

		// Get list of VDB Names
		Collection<? extends VDB> vdbs = getVDBs();

		if(vdbs!=null) {
			// Get VDB names
			Collection<Properties> vdbSummaryProps = new ArrayList<Properties>();
			for(VDB vdb : vdbs) {
				VDBMetaData vdbMeta = (VDBMetaData)vdb;
				String vdbName = vdbMeta.getName();
				boolean isDynamic = vdbMeta.isXmlDeployment();
				boolean isPreview = vdbMeta.isPreview();
				String status = vdbMeta.getStatus().toString();
				
				// Dynamic VDB
				if(isDynamic) {
					if(includeDynamic) {
						Properties vdbSummary = new Properties();
						vdbSummary.put("name", vdbName);
						vdbSummary.put("type", "dynamic");
						vdbSummary.put("status", status);
						vdbSummaryProps.add(vdbSummary);
					}
				// Archive VDB
				} else if(includeArchive) {
					if(!isPreview) {
						Properties vdbSummary = new Properties();
						vdbSummary.put("name", vdbName);
						vdbSummary.put("type", "archive");
						vdbSummary.put("status", status);
						vdbSummaryProps.add(vdbSummary);
					} else if(includePreview) {
						Properties vdbSummary = new Properties();
						vdbSummary.put("name", vdbName);
						vdbSummary.put("type", "archive");
						vdbSummary.put("status", status);
						vdbSummaryProps.add(vdbSummary);
					}
				}
			}
			return vdbSummaryProps;
		} else {
			return Collections.emptyList();
		}
	}
	
	public Collection<? extends VDB> getVDBs() throws AdminApiClientException {
		// Get list of VDB Names
		Collection<? extends VDB> vdbs = null;
		try {
			vdbs = this.admin.getVDBs();
		} catch (AdminException e) {
			throw new AdminApiClientException(e.getMessage());
		}
		return vdbs;
	}
	
	public VDBMetaData getVDB(String vdbName, int vdbVersion) throws AdminApiClientException {
		// Get list of VDBS - get the named VDB
		Collection<? extends VDB> vdbs = getVDBs();

		VDBMetaData vdb = null;
		for(VDB aVdb : vdbs) {
			VDBMetaData vdbMeta = (VDBMetaData)aVdb;
			if(vdbMeta.getName()!=null && vdbMeta.getName().equalsIgnoreCase(vdbName) && vdbMeta.getVersion()==vdbVersion) {
				vdb = vdbMeta;
				break;
			}
		}
		return vdb;
	}

    /*
     * Delete the Dynamic VDB - undeploy it, then delete the source
     * @param vdbName name of the VDB to delete
     */
	public void deleteVDB(String vdbName) throws AdminApiClientException {
		String deploymentName = getDeploymentNameForVDB(vdbName,1);
		if(deploymentName!=null) {                        
			try {
				// Undeploy the VDB
				this.admin.undeploy(deploymentName);

				// Delete the VDB Source
				//deleteDataSource(vdbName);
			} catch (Exception e) {
				throw new AdminApiClientException(e.getMessage());
			}
		}
	}
	
    /*
     * Delete the Dynamic VDB - undeploy it, then delete the source
     * @param vdbName name of the VDB to delete
     */
	public void deleteVDBs(Collection<String> vdbNames) throws AdminApiClientException {
		for(String vdbName : vdbNames) {
			deleteVDB(vdbName);
		}
	}
    
    /*
     * Find the VDB Name for the provided deployment name
     * @param deploymentName
     * @return the VDB Name
     */
	private String getVDBNameForDeployment(String deploymentName) throws AdminApiClientException {
		String vdbName = null;

		// Get VDB name and version for the specified deploymentName
		Collection<? extends VDB> allVdbs = getVDBs();
		for(VDB vdbMeta : allVdbs) {
			String deployName = vdbMeta.getPropertyValue("deployment-name");
			if(deployName!=null && deployName.equals(deploymentName)) {
				vdbName=vdbMeta.getName();
				break;
			}
		}

		return vdbName;
	}
    
    /*
     * Find the Deployment Name for the provided VdbName and Version
     * @param deploymentName
     * @return the VDB Name
     */
	private String getDeploymentNameForVDB(String vdbName, int vdbVersion) throws AdminApiClientException {
		String deploymentName = null;
		VDBMetaData vdbMeta = getVDB(vdbName,vdbVersion);
		if(vdbMeta!=null) {
			deploymentName = vdbMeta.getPropertyValue("deployment-name");
		}
		return deploymentName;
	}

	/**
     * Constructor.
     * @param endpoint
     * @param username
     * @param password
     * @param validating
     * @throws SrampClientException
     */
//    public AdminApiClient(final String endpoint, final String username, final String password,
//            final boolean validating) throws SrampClientException {
//        this(endpoint, new BasicAuthenticationProvider(username, password), validating);
//    }

    /**
     * Constructor.
     * @param endpoint
     * @param authenticationProvider
     * @param validating
     * @throws SrampClientException
     * @throws SrampAtomException
     */
//    public AdminApiClient(final String endpoint, AuthenticationProvider authenticationProvider,
//            final boolean validating) {
//        this(endpoint);
//        this.authProvider = authenticationProvider;
//        this.validating = validating;
//        if (this.validating) {
//            //discoverAvailableFeatures();
//        }
//    }

	/**
	 * @return the s-ramp endpoint
	 */
	public String getEndpoint() {
		return this.endpoint;
	}


//    /**
//     * Creates the RESTEasy client request object, configured appropriately.
//     * @param atomUrl
//     */
//    protected ClientRequest createClientRequest(String atomUrl) {
//        ClientExecutor executor = createClientExecutor();
//        ClientRequest request = new ClientRequest(atomUrl, executor);
//        return request;
//    }
//
//    /**
//     * Creates the client executor that will be used by RESTEasy when
//     * making the request.
//     */
//    private ClientExecutor createClientExecutor() {
//        // TODO I think the http client is thread safe - so let's try to create just one of these
//        DefaultHttpClient httpClient = new DefaultHttpClient();
//        httpClient.addRequestInterceptor(new HttpRequestInterceptor() {
//            @Override
//            public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
//                Locale l = getLocale();
//                if (l == null) {
//                    l = Locale.getDefault();
//                }
//                request.addHeader("Accept-Language", l.toString()); //$NON-NLS-1$
//            }
//        });
//        if (this.authProvider != null) {
//            httpClient.addRequestInterceptor(new HttpRequestInterceptor() {
//                @Override
//                public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
//                    authProvider.provideAuthentication(request);
//                }
//            });
//        }
//        return new ApacheHttpClient4Executor(httpClient);
//    }

    /**
     * @return the locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * @param locale the locale to set
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

}
