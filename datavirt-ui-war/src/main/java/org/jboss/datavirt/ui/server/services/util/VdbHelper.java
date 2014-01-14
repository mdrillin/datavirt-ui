package org.jboss.datavirt.ui.server.services.util;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jboss.datavirt.ui.client.shared.beans.VdbDetailsBean;
import org.jboss.datavirt.ui.client.shared.beans.VdbModelBean;
import org.jboss.datavirt.ui.client.shared.exceptions.DataVirtUiException;
import org.teiid.adminapi.Model;
import org.teiid.adminapi.VDB;
import org.teiid.adminapi.VDB.Status;
import org.teiid.adminapi.impl.ModelMetaData;
import org.teiid.adminapi.impl.VDBImportMetadata;
import org.teiid.adminapi.impl.VDBMetaData;
import org.teiid.adminapi.impl.VDBMetadataParser;

/**
 * Contains methods for working with VDBs
 */
public class VdbHelper {

	// ============================================
	// Static Variables

	private static VdbHelper instance = new VdbHelper();

	// ============================================
	// Static Methods
	/**
	 * Get the singleton instance
	 *
	 * @return instance
	 */
	public static VdbHelper getInstance() {
		return instance;
	}

	/*
	 * Create a VdbHelper
	 */
	private VdbHelper() {
	}

	/**
	 * Create a VDB object
	 * @param vdbName the name of the VDB
	 * @param vdbVersion the vdb version
	 * @return the VDBMetadata
	 */
	public VDBMetaData createVdb(String vdbName, int vdbVersion) {
		VDBMetaData vdb = new VDBMetaData();
		vdb.setName(vdbName);
		vdb.setDescription("VDB for: "+vdbName+", Version: "+vdbVersion);
		vdb.setVersion(vdbVersion);
		return vdb;
	}

	/**
	 * Get the bytearray version of the VDBMetaData object
	 * @param vdb the VDB
	 * @return the vdb in bytearray form
	 */
	public byte[] getVdbByteArray(VDBMetaData vdb) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		VDBMetadataParser.marshell(vdb, out);

		return out.toByteArray();
	}

	/**
	 * Get the stringified version of the VDBMetaData object
	 * @param vdb the VDB
	 * @return the vdb in string form
	 */
	public String getVdbString(VDBMetaData vdb) throws Exception {
		return new String(getVdbByteArray(vdb));
	}

	/**
	 * Create a VDB import object
	 * @param vdbName the name of the VDB to import
	 * @param vdbVersion the vdb version
	 * @return the VDBImportMetadata
	 */
	public VDBImportMetadata createVdbImport(String vdbName, int vdbVersion) {
		VDBImportMetadata vdbImport = new VDBImportMetadata();
		vdbImport.setName(vdbName);
		vdbImport.setVersion(vdbVersion);
		return vdbImport;
	}

	/**
	 * Create a Source Model
	 * @param modelName the name of the Model
	 * @param sourceName the name of the jndi source
	 * @param translator the translator name
	 * @return the ModelMetaData
	 */
	public ModelMetaData createSourceModel(String modelName, String sourceName, String translator) {
		ModelMetaData modelMetaData = new ModelMetaData();
		modelMetaData.addSourceMapping(sourceName, translator, "java:/"+sourceName);
		modelMetaData.setName(modelName);
		return modelMetaData;
	}

	/**
	 * Create a View Model
	 * @param modelName the name of the Model
	 * @param ddl the DDL which defines the view
	 * @return the ModelMetaData
	 */
	public ModelMetaData createViewModel(String modelName, String ddl) {
		ModelMetaData modelMetaData = new ModelMetaData();
		modelMetaData.setName(modelName);
		modelMetaData.setModelType(Model.Type.VIRTUAL);
		modelMetaData.setSchemaSourceType("DDL");
		modelMetaData.setSchemaText(ddl);
		return modelMetaData;
	}
	
    /**
     * Get ModelInfo for the models in the specified VDB
     * @param vdb the vdb
     * @return the VdbDetailsBean
     */
	public VdbDetailsBean getVdbDetails(VDBMetaData vdb) {
		VdbDetailsBean vdbDetailsBean = new VdbDetailsBean();

		// Add a model to the vdb, then re-deploy it.
		if(vdb!=null) {
			// ------------
			// VDB Name
			// ------------
			vdbDetailsBean.setName(vdb.getName());
			
			// ------------
			// VDB Status
			// ------------
			VDB.Status status = vdb.getStatus();
			String vdbStatus = null;
			// Change FAILED or REMOVED status to INACTIVE
			if(status!=null) {
				vdbStatus = status.toString();
				if( vdbStatus.equalsIgnoreCase("FAILED") || vdbStatus.equalsIgnoreCase("REMOVED") ) {
					vdbStatus="INACTIVE";
				}
			}

			// ------------
			// VDB Models
			// ------------
			List<Model> models = vdb.getModels();

			for(Model model: models) {
				VdbModelBean modelBean = new VdbModelBean();
				
				ModelMetaData modelMeta = (ModelMetaData)model;
				String modelName = modelMeta.getName();
				String modelType = modelMeta.getModelType().toString();
				String jndiName = null;
				String translatorName = null;
				String modelStatus = null;
				String ddl = "";

				// Virtual Model, use placeholders for jndiName and translatorName
				if(modelType.equals(Model.Type.VIRTUAL.toString())) {
					jndiName = "-----";
					translatorName = "teiid";
					ddl = modelMeta.getSchemaText();
			    // Physical Model, get source info
				} else {
					List<String> sourceNames = modelMeta.getSourceNames();
					for(String sourceName: sourceNames) {
						jndiName = modelMeta.getSourceConnectionJndiName(sourceName);
						translatorName = modelMeta.getSourceTranslatorName(sourceName);
					}
				}
				
				// If this is not an XML Deployment, show the Status as Unknown
				if(!vdb.isXmlDeployment()) {
					modelStatus = "Unknown";
					// Is XML Deployment, look at model errors
				} else {
					List<String> errors = modelMeta.getValidityErrors();
					if(errors.size()==0) {
						modelStatus = "ACTIVE";
					} else {
						// There may be multiple errors - process the list...
						boolean connectionError = false;
						boolean validationError = false;
						boolean isLoading = false;
						// Iterate Errors and set status flags
						for(String error: errors) {
							if(error.indexOf("TEIID11009")!=-1 || error.indexOf("TEIID60000")!=-1 || error.indexOf("TEIID31097")!=-1) {
								connectionError=true;
							} else if(error.indexOf("TEIID31080")!=-1 || error.indexOf("TEIID31071")!=-1) {
								validationError=true;
							} else if(error.indexOf("TEIID50029")!=-1) {
								isLoading=true;
							}
						}
						// --------------------------------------------------
						// Set model status string according to errors found
						// --------------------------------------------------
						// Connection Error. Reset the VDB overall status, as it may say loading
						if(connectionError) {
							modelStatus = "INACTIVE: Data Source connection failed...";
							if(vdbStatus!=null && "LOADING".equalsIgnoreCase(vdbStatus)) {
								vdbStatus = "INACTIVE";
							}
							// Validation Error with View SQL
						} else if(validationError) {
							modelStatus = "INACTIVE: Validation Error with SQL";
							// Loading in progress
						} else if(isLoading) {
							modelStatus = "INACTIVE: Metadata loading in progress...";
							// Unknown - use generic message
						} else {
							modelStatus = "INACTIVE: unknown source issue";
						}
					}
				}
				modelBean.setName(modelName);
				modelBean.setType(modelType);
				modelBean.setJndiSource(jndiName);
				modelBean.setTranslator(translatorName);
				modelBean.setStatus(modelStatus);
				modelBean.setDdl(ddl);
				
				vdbDetailsBean.addModel(modelBean);
			}
			vdbDetailsBean.setStatus(vdbStatus);
		}

		return vdbDetailsBean;
	}
	
    /**
     * Get the imports for the specified VDB
     * @param vdb the vdb
     * @return the List of VDBImportMetadata
     */
	public List<VDBImportMetadata> getVdbImports(VDBMetaData vdb) {
		if(vdb!=null) {
			List<VDBImportMetadata> vdbImports = new ArrayList<VDBImportMetadata>(vdb.getVDBImports());
			return vdbImports;
		} 

		return Collections.emptyList();
	}

    /**
     * Get the ViewModels for the specified VDB
     * @param vdb the vdb
     * @return the List of View Models
     */
	public List<ModelMetaData> getVdbViewModels(VDBMetaData vdb) {
		if(vdb!=null) {
			// Get current vdb ViewModels
			List<ModelMetaData> viewModels = new ArrayList<ModelMetaData>();

			List<Model> allModels = vdb.getModels();
			for(Model theModel : allModels) {
				if(theModel.getModelType()==Model.Type.VIRTUAL) {
					viewModels.add((ModelMetaData)theModel);
				}
			}
			return viewModels;
		}

		return Collections.emptyList();
	}
	
    /**
     * Get the Properties for the specified VDB
     * @param vdb the vdb
     * @return the Vdb Properties
     */
	public Properties getVdbProperties(VDBMetaData vdb) {
		if(vdb!=null) {
			return vdb.getProperties();
		}

		return new Properties();
	}
	
	/**
	 * Adds the Import to supplied VDB deployment. The new VDB is returned.
	 * @param vdb the VDB
	 * @param importVdbName the name of the VDB to import
	 * @param importVdbVersion the version of the VDB to import
	 * @return the new VDB
	 */
	public VDBMetaData addImport(VDBMetaData vdb, String importVdbName, int importVdbVersion) {
		String vdbName = vdb.getName();
		int vdbVersion = vdb.getVersion();
		
		// First Check the VDB being added. If it has errors, dont add
//		String sourceStatus = getVDBStatusMessage(importVdbName);
//		if(!sourceStatus.equals("success")) {
//			return "<bold>Import Source has errors and was not added:</bold><br>"+sourceStatus;
//		}

		// Get current vdb imports
		List<VDBImportMetadata> currentVdbImports = getVdbImports(vdb);
		List<ModelMetaData> currentViewModels = getVdbViewModels(vdb);
		Properties currentProperties = getVdbProperties(vdb);

		// Clear any prior Model Messages (needed for successful redeploy)
		clearModelMessages(currentViewModels);

		// Create a new vdb
		VDBMetaData newVdb = createVdb(vdbName,vdbVersion);

		// Add the existing ViewModels
		newVdb.setModels(currentViewModels);

		// Transfer the existing properties
		newVdb.setProperties(currentProperties);

		// Add new import to current imports
		currentVdbImports.add(createVdbImport(importVdbName, importVdbVersion));
		newVdb.getVDBImports().addAll(currentVdbImports);

		return newVdb;
	}
	
	/**
	 * Adds the ViewModel to supplied VDB deployment. The new VDB is returned.
	 * @param vdb the VDB
	 * @param importVdbName the name of the VDB to import
	 * @param importVdbVersion the version of the VDB to import
	 * @return the new VDB
	 */
	public VDBMetaData addViewModel(VDBMetaData vdb, String viewModelName, String ddlString) {
		String vdbName = vdb.getName();
		int vdbVersion = vdb.getVersion();
		
		// Get current vdb imports
		List<VDBImportMetadata> currentVdbImports = getVdbImports(vdb);
		List<ModelMetaData> currentViewModels = getVdbViewModels(vdb);
		Properties currentProperties = getVdbProperties(vdb);

		// Clear any prior Model Messages (needed for successful redeploy)
		clearModelMessages(currentViewModels);

		// Create a new vdb
		VDBMetaData newVdb = createVdb(vdbName,vdbVersion);

	    // Create View Model and add to current view models
	    ModelMetaData modelMetaData = createViewModel(viewModelName,ddlString);
	    currentViewModels.add(modelMetaData);
	    
	    // Set ViewModels on new VDB
	    newVdb.setModels(currentViewModels);

		// Transfer the existing properties
		newVdb.setProperties(currentProperties);

		// Add new import to current imports
		newVdb.getVDBImports().addAll(currentVdbImports);

		return newVdb;
	}

	/**
	 * Removes the imports from the supplied VDB - if they exist. The new VDB is returned.
	 * @param vdb the VDB
	 * @param removeImportNameList the list of import names to remove
	 * @return the List of ImportInfo data
	 */
	public VDBMetaData removeImports(VDBMetaData vdb, List<String> removeImportNameList) {                
		String vdbName = vdb.getName();
		int vdbVersion = vdb.getVersion();
		
		// Get current vdb imports
		List<VDBImportMetadata> currentVdbImports = getVdbImports(vdb);
		List<ModelMetaData> currentViewModels = getVdbViewModels(vdb);
		Properties currentProperties = getVdbProperties(vdb);

		// Clear any prior Model Messages (needed for successful redeploy)
		clearModelMessages(currentViewModels);

		// Create a new vdb
		VDBMetaData newVdb = createVdb(vdbName,vdbVersion);

		// Add the existing ViewModels
		newVdb.setModels(currentViewModels);

		// Transfer the existing properties
		newVdb.setProperties(currentProperties);

		// Create import list for new model
		List<VDBImportMetadata> newImports = new ArrayList<VDBImportMetadata>();
		for(VDBImportMetadata vdbImport: currentVdbImports) {
			String currentName = vdbImport.getName();
			// Keep the import - unless its in the remove list
			if(!removeImportNameList.contains(currentName)) {
				newImports.add((VDBImportMetadata)vdbImport);
			}
		}
		newVdb.getVDBImports().addAll(newImports);

		return newVdb;
	}
	
	/**
	 * Removes the models from the supplied VDB - if they exist. The new VDB is returned.
	 * @param vdb the VDB
	 * @param removeModelNameAndTypeMap the Map of modelName to type
	 * @return the new VDB
	 */
	public VDBMetaData removeModels(VDBMetaData vdb, Map<String,String> removeModelNameAndTypeMap) {                
		String vdbName = vdb.getName();
		int vdbVersion = vdb.getVersion();
		
		// Sort the list into separate viewModel and sourceModel(Imports) lists
		List<String> removeViewModelNameList = new ArrayList<String>();
		List<String> removeImportNameList = new ArrayList<String>();
		for(String modelName : removeModelNameAndTypeMap.keySet()) {
			String modelType = removeModelNameAndTypeMap.get(modelName);
			if(modelType.equalsIgnoreCase("VIEW")) {
				removeViewModelNameList.add(modelName);
			} else {
				removeImportNameList.add(modelName);
			}
		}

		// Get current vdb imports
		List<VDBImportMetadata> currentVdbImports = getVdbImports(vdb);
		List<ModelMetaData> currentViewModels = getVdbViewModels(vdb);
		Properties currentProperties = getVdbProperties(vdb);

		// Clear any prior Model Messages (needed for successful redeploy)
		clearModelMessages(currentViewModels);

		// Create a new vdb
		VDBMetaData newVdb = createVdb(vdbName,vdbVersion);

		// Determine list of view models
		List<ModelMetaData> newViewModels = new ArrayList<ModelMetaData>();
		for(Model model: currentViewModels) {
			String currentName = model.getName();
			// Keep the model - unless its in the remove list
			if(!removeViewModelNameList.contains(currentName)) {
				newViewModels.add((ModelMetaData)model);
			}
		}
		newVdb.setModels(newViewModels);

		// Transfer the existing properties
		newVdb.setProperties(currentProperties);

		// Create import list for new model
		List<VDBImportMetadata> newImports = new ArrayList<VDBImportMetadata>();
		for(VDBImportMetadata vdbImport: currentVdbImports) {
			String currentName = vdbImport.getName();
			// Keep the import - unless its in the remove list
			if(!removeImportNameList.contains(currentName)) {
				newImports.add((VDBImportMetadata)vdbImport);
			}
		}
		newVdb.getVDBImports().addAll(newImports);

		return newVdb;
	}

	public String getVDBStatusMessage(VDBMetaData vdb) throws DataVirtUiException {
    	if(vdb!=null) {
    		Status vdbStatus = vdb.getStatus();
    		if(vdbStatus!=Status.ACTIVE) {
    			List<String> allErrors = vdb.getValidityErrors();
    			if(allErrors!=null && !allErrors.isEmpty()) {
    				StringBuffer sb = new StringBuffer();
    				for(String errorMsg : allErrors) {
    					sb.append("ERROR: " +errorMsg+"<br>");
    				}
    				return sb.toString();
    			}
    		}
    	}
    	return "success";
    }
	
	private void clearModelMessages(List<ModelMetaData> models) {
		for(ModelMetaData model: models) {
			model.clearMessages();
		}
	}

}

