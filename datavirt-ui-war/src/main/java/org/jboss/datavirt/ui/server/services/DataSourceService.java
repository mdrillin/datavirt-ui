/*
 * Copyright 2013 JBoss Inc
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;

import org.jboss.datavirt.ui.client.shared.beans.DataSourceDetailsBean;
import org.jboss.datavirt.ui.client.shared.beans.DataSourcePropertyBean;
import org.jboss.datavirt.ui.client.shared.beans.DataSourceResultSetBean;
import org.jboss.datavirt.ui.client.shared.beans.DataSourceSummaryBean;
import org.jboss.datavirt.ui.client.shared.beans.DataSourceTypeBean;
import org.jboss.datavirt.ui.client.shared.beans.DataSourceTypeResultSetBean;
import org.jboss.datavirt.ui.client.shared.exceptions.DataVirtUiException;
import org.jboss.datavirt.ui.client.shared.services.IDataSourceService;
import org.jboss.datavirt.ui.server.api.AdminApiClientAccessor;
import org.jboss.datavirt.ui.server.services.util.TranslatorHelper;
import org.jboss.errai.bus.server.annotations.Service;
import org.teiid.adminapi.PropertyDefinition;

/**
 * Concrete implementation of the DataSource service.
 *
 * @author mdrillin@redhat.com
 */
@Service
public class DataSourceService implements IDataSourceService {

    private static final String DRIVER_KEY = "driver-name";
    private static final String CLASSNAME_KEY = "class-name";
    
    @Inject
    private AdminApiClientAccessor clientAccessor;

    /**
     * Constructor.
     */
    public DataSourceService() {
    }

    /**
     * @see org.jboss.datavirt.ui.client.shared.services.IDataSourceSearchService#search(java.lang.String, int, java.lang.String, boolean)
     */
    @Override
    public DataSourceResultSetBean search(String searchText, int page, String sortColumnId, boolean sortAscending) throws DataVirtUiException {
        int pageSize = 15;
        
        DataSourceResultSetBean data = new DataSourceResultSetBean();
        
        Collection<Properties> dsSummaryPropsCollection = null;
        try {
        	dsSummaryPropsCollection = clientAccessor.getClient().getDataSourceSummaryPropsCollection();
		} catch (AdminApiClientException e) {
		}
        
        // List of all the names
        List<Properties> propertiesList = new ArrayList<Properties>(dsSummaryPropsCollection);
        // Save complete list
        Collection<String> allDsNames = new ArrayList<String>(dsSummaryPropsCollection.size());
        for(Properties dsProps : propertiesList) {
            String sourceName = dsProps.getProperty("name");
            if(sourceName!=null && !sourceName.isEmpty()) {
            	allDsNames.add(sourceName);
            }
        }
        
        int totalSources = propertiesList.size();
        
        // Start and End Index for this page
        int page_startIndex = (page - 1) * pageSize;
        int page_endIndex = page_startIndex + (pageSize-1);
        // If page endIndex greater than total rows, reset to end
        if(page_endIndex > (totalSources-1)) {
        	page_endIndex = totalSources-1;
        }
        
        List<DataSourceSummaryBean> rows = new ArrayList<DataSourceSummaryBean>();
        for(int i=page_startIndex; i<=page_endIndex; i++) {
            DataSourceSummaryBean summaryBean = new DataSourceSummaryBean();
            Properties dsSummaryProps = propertiesList.get(i);
            String sourceName = dsSummaryProps.getProperty("name");
            summaryBean.setName(sourceName);
            summaryBean.setType(dsSummaryProps.getProperty("type"));
            summaryBean.setUpdatedOn(new Date());
            rows.add(summaryBean);
        }
        data.setAllDsNames(allDsNames);
        data.setDataSources(rows);
        data.setItemsPerPage(pageSize);
        data.setStartIndex(page_startIndex);
        data.setTotalResults(totalSources);
        
        return data;
    }
    
    @Override
    public DataSourceTypeResultSetBean getDataSourceTypeResultSet(int page, String sortColumnId, boolean sortAscending) throws DataVirtUiException {
        int pageSize = 15;
        
        DataSourceTypeResultSetBean data = new DataSourceTypeResultSetBean();
        
        Collection<String> dsTypesCollection = null;
        try {
        	dsTypesCollection = clientAccessor.getClient().getDataSourceTypes();
		} catch (AdminApiClientException e) {
		}
        
    	// Filter out 'types' ending with .war
        List<String> dsTypesList = new ArrayList<String>(dsTypesCollection.size());
    	for(String dsType : dsTypesCollection) {
    	   if(dsType!=null && !dsType.endsWith(".war")) {
    		   dsTypesList.add(dsType);
    	   }
    	}
    	
        // List of all the names
        Collections.sort(dsTypesList);
        
        int totalTypes = dsTypesList.size();
        
        // Start and End Index for this page
        int page_startIndex = (page - 1) * pageSize;
        int page_endIndex = page_startIndex + (pageSize-1);
        // If page endIndex greater than total rows, reset to end
        if(page_endIndex > (totalTypes-1)) {
        	page_endIndex = totalTypes-1;
        }
        
        List<DataSourceTypeBean> rows = new ArrayList<DataSourceTypeBean>();
        for(int i=page_startIndex; i<=page_endIndex; i++) {
        	DataSourceTypeBean typeBean = new DataSourceTypeBean();
            String typeName = dsTypesList.get(i);
            typeBean.setName(typeName);
            typeBean.setUpdatedOn(new Date());
            rows.add(typeBean);
        }
        data.setDataSourceTypes(rows);
        data.setItemsPerPage(pageSize);
        data.setStartIndex(page_startIndex);
        data.setTotalResults(totalTypes);
        
        return data;
    }
    
    @Override
    public DataSourceDetailsBean getDataSourceDetails(String dsName) throws DataVirtUiException {
    	
    	DataSourceDetailsBean dsDetailsBean = new DataSourceDetailsBean();
    	dsDetailsBean.setName(dsName);

    	Properties dsProps = null;
    	try {
			dsProps = clientAccessor.getClient().getDataSourceProperties(dsName);
		} catch (AdminApiClientException e) {
			throw new DataVirtUiException(e.getMessage());
		}
    	
    	String dsType = getDataSourceType(dsProps);
    	// Get the Default Properties for the DS type
    	List<DataSourcePropertyBean> dataSourcePropertyBeans = getDataSourceTypeProperties(dsType);
    	    	
        // DS type default property to data source specific value
        for(DataSourcePropertyBean propBean: dataSourcePropertyBeans) {
            String propName = propBean.getName();
            String propValue = dsProps.getProperty(propName);
            if(dsProps.containsKey(propName)) {
                propValue = dsProps.getProperty(propName);
                if(propValue!=null) {
                	propBean.setValue(propValue);
                	propBean.setOriginalValue(propValue);
                }
            }
        }
    	
    	dsDetailsBean.setProperties(dataSourcePropertyBeans);

    	return dsDetailsBean;
    }

//    /**
//     * @see org.jboss.datavirt.ui.client.shared.services.IArtifactService#get(java.lang.String)
//     */
//    @Override
//    public DataSourcePropertiesBean getDefaultProperties(String dsName) throws DataVirtUiException {
//    	
//    	DataSourcePropertiesBean dsPropsBean = new DataSourcePropertiesBean();
//    	
//    	Properties dsProps = null;
//    	try {
//			dsProps = clientAccessor.getClient().getDataSourceProperties(dsName);
//		} catch (AdminApiClientException e) {
//			throw new DataVirtUiException(e.getMessage());
//		}
//    	
//    	if(dsProps!=null) {
//    		//dsBean.setProperties(dsProps);
//    	}
//
//    	return dsPropsBean;
//    }

    /**
     * Gets the current DataSources
     * @throws DataVirtUiException
     */
    public List<String> getDataSources( ) throws DataVirtUiException {
    	List<String> dsList = new ArrayList<String>();
    	
		Collection<String> sourceNames = null;
    	try {
    		sourceNames = clientAccessor.getClient().getDataSourceNames();
		} catch (AdminApiClientException e) {
			throw new DataVirtUiException(e.getMessage());
		}
    	
    	if(sourceNames==null || sourceNames.isEmpty()) {
    		return Collections.emptyList();
    	}
    	
    	dsList.addAll(sourceNames);    	
    	// Alphabetically sort the list
    	Collections.sort(dsList);

    	return dsList;    	
   }

    /**
     * Gets the current Translators
     * @throws DataVirtUiException
     */
    public List<String> getTranslators( ) throws DataVirtUiException {
    	List<String> resultList = new ArrayList<String>();
    	
		Collection<String> translatorNames = null;
    	try {
    		translatorNames = clientAccessor.getClient().getTranslatorNames();
		} catch (AdminApiClientException e) {
			throw new DataVirtUiException(e.getMessage());
		}
    	
    	if(translatorNames==null || translatorNames.isEmpty()) {
    		return Collections.emptyList();
    	}
    	
    	resultList.addAll(translatorNames);
    	// Alphabetically sort the list
    	Collections.sort(resultList);

    	return resultList;    	
    }
    
    public Map<String,String> getDefaultTranslatorMap() throws DataVirtUiException {
		Map<String,String> mappings = null;
    	try {
    		mappings = clientAccessor.getClient().getDefaultTranslatorMap();
		} catch (AdminApiClientException e) {
			throw new DataVirtUiException(e.getMessage());
		}
    	
    	if(mappings==null || mappings.isEmpty()) {
    		return Collections.emptyMap();
    	}
    	return mappings;
    }

    public List<String> getDataSourceTypes() throws DataVirtUiException {
    	List<String> dsTypeList = new ArrayList<String>();
    	    	
		Collection<String> dsTypes = null;
    	try {
    		dsTypes = clientAccessor.getClient().getDataSourceNames();
		} catch (AdminApiClientException e) {
			throw new DataVirtUiException(e.getMessage());
		}
    	
    	if(dsTypes==null || dsTypes.isEmpty()) {
    		return Collections.emptyList();
    	}
    	
    	// Filter out 'types' ending with .war
    	for(String dsType : dsTypes) {
    	   if(dsType!=null && !dsType.endsWith(".war")) {
    		   dsTypeList.add(dsType);
    	   }
    	}
    	
    	// Alphabetically sort the list
    	Collections.sort(dsTypeList);

    	return dsTypeList;
    }
    
    public List<DataSourcePropertyBean> getDataSourceTypeProperties(String typeName) throws DataVirtUiException {
    	List<DataSourcePropertyBean> propertyDefnList = new ArrayList<DataSourcePropertyBean>();
    	
		Collection<? extends PropertyDefinition> propDefnList = null;
    	try {
    		propDefnList = clientAccessor.getClient().getDataSourceTypePropertyDefns(typeName);
		} catch (AdminApiClientException e) {
			throw new DataVirtUiException(e.getMessage());
		}
    	
    	if(propDefnList==null || propDefnList.isEmpty()) {
    		return Collections.emptyList();
    	}
    	
		// Get the Managed connection factory class for rars
//		String rarConnFactoryValue = null;
//		if(isRarDriver(driverName)) {
//			rarConnFactoryValue = getManagedConnectionFactoryClassDefault(propDefnList);
//		}
    	
		for(PropertyDefinition propDefn: propDefnList) {
			DataSourcePropertyBean propBean = new DataSourcePropertyBean();
			
			// ------------------------
			// Set PropertyObj fields
			// ------------------------
			// Name
			String name = propDefn.getName();
			propBean.setName(name);
			// DisplayName
			String displayName = propDefn.getDisplayName();
			propBean.setDisplayName(displayName);
			// isModifiable
			boolean isModifiable = propDefn.isModifiable();
			propBean.setModifiable(isModifiable);
			// isRequired
			boolean isRequired = propDefn.isRequired();
			propBean.setRequired(isRequired);
			// isMasked
			boolean isMasked = propDefn.isMasked();
			propBean.setMasked(isMasked);
			// defaultValue
			Object defaultValue = propDefn.getDefaultValue();
			if(defaultValue!=null) {
				propBean.setDefaultValue(defaultValue.toString());
			}
			// Set the value and original Value
			if(defaultValue!=null) {
				propBean.setValue(defaultValue.toString());
				propBean.setOriginalValue(defaultValue.toString());
				// Set Connection URL to template if available and value was null
//			} else if(displayName.equalsIgnoreCase(PropertyItem.CONNECTION_URL_DISPLAYNAME)) {
//				String urlTemplate = TranslatorHelper.getUrlTemplate(driverName);
//				if(!StringUtil.isEmpty(urlTemplate)) {
//					propDefnBean.setValue(urlTemplate);
//					propDefnBean.setOriginalValue(urlTemplate);
//				}
			}

			// Copy the 'managedconnectionfactory-class' default value into the 'class-name' default value
//			if(name.equals(CLASSNAME_KEY)) {
//				propDefnBean.setDefaultValue(rarConnFactoryValue);
//				propDefnBean.setValue(rarConnFactoryValue);
//				propDefnBean.setOriginalValue(rarConnFactoryValue);
//				propDefnBean.setRequired(true);
//			}

			// ------------------------
			// Add PropertyObj to List
			// ------------------------
			propertyDefnList.add(propBean);
		}

    	return propertyDefnList;
    }  
    
    /**
     * Get the Driver name for the supplied DataSource name - from the TeiidServer
     * @param dsName the data source name
     * @return the dataSource driver name
     */
    public String getDataSourceType(String dsName) throws DataVirtUiException {
    	Properties dsProps = null;
    	try {
			dsProps = clientAccessor.getClient().getDataSourceProperties(dsName);
		} catch (AdminApiClientException e) {
			throw new DataVirtUiException(e.getMessage());
		}
    	return getDataSourceType(dsProps);
    }

    /**
     * Get the Driver name for the supplied DataSource name - from the TeiidServer
     * @param dsProps the data source properties
     * @return the dataSource driver name
     */
    private String getDataSourceType(Properties dsProps) {
    	if(dsProps==null) return "unknown";

    	String driverName = dsProps.getProperty(DRIVER_KEY);
    	// If driver-name not found, look for class name and match up the .rar
    	if(driverName==null || driverName.trim().length()==0) {
    		String className = dsProps.getProperty(CLASSNAME_KEY);
    		if(className!=null && className.trim().length()!=0) {
    			driverName = TranslatorHelper.getDriverNameForClass(className);
    		}
    	}
    	return driverName;
    }
         
    @Override
    public void update(DataSourceDetailsBean bean) throws DataVirtUiException {
//        try {
//            ArtifactType artifactType = ArtifactType.valueOf(bean.getModel(), bean.getRawType(), null);
//            // Grab the latest from the server
//            BaseArtifactType artifact = clientAccessor.getClient().getArtifactMetaData(artifactType, bean.getUuid());
//            // Update it with new data from the bean
//            artifact.setName(bean.getName());
//            artifact.setDescription(bean.getDescription());
//            artifact.setVersion(bean.getVersion());
//            artifact.getProperty().clear();
//            for (String propName : bean.getPropertyNames()) {
//                SrampModelUtils.setCustomProperty(artifact, propName, bean.getProperty(propName));
//            }
//            artifact.getClassifiedBy().clear();
//            for (String classifier : bean.getClassifiedBy()) {
//                artifact.getClassifiedBy().add(classifier);
//            }
//            // Push the changes back to the server
//            clientAccessor.getClient().updateArtifactMetaData(artifact);
//        } catch (SrampClientException e) {
//            throw new DataVirtUiException(e.getMessage());
//        } catch (SrampAtomException e) {
//            throw new DataVirtUiException(e.getMessage());
//        }
    }

    @Override
    public void deleteDataSource(String dsName) throws DataVirtUiException {
    	try {
			clientAccessor.getClient().deleteDataSource(dsName);
		} catch (AdminApiClientException e) {
			throw new DataVirtUiException(e.getMessage());
		}
    }

    @Override
    public void deleteDataSources(Collection<String> dsNames) throws DataVirtUiException {
    	try {
			clientAccessor.getClient().deleteDataSources(dsNames);
		} catch (AdminApiClientException e) {
			throw new DataVirtUiException(e.getMessage());
		}
    }
    
    @Override
    public void deleteTypes(Collection<String> dsTypes) throws DataVirtUiException {
    	try {
			clientAccessor.getClient().deleteDataSourceTypes(dsTypes);
		} catch (AdminApiClientException e) {
			throw new DataVirtUiException(e.getMessage());
		}
    }

}
