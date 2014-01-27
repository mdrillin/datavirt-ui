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
package org.jboss.datavirt.ui.client.shared.services;

import java.util.Collection;
import java.util.Map;

import org.jboss.datavirt.ui.client.shared.beans.VdbDetailsBean;
import org.jboss.datavirt.ui.client.shared.beans.VdbResultSetBean;
import org.jboss.datavirt.ui.client.shared.exceptions.DataVirtUiException;
import org.jboss.errai.bus.server.annotations.Remote;

/**
 * Provides a way to get VDBs and related info.
 *
 * @author mdrillin@redhat.com
 */
@Remote
public interface IVdbService {

    /**
     * Get the Data Sources using the provided info.
     * @param searchText
     * @param page
     * @param showDataVirtUiVDBs
     * @param sortColumnId
     * @param sortAscending
     * @throws DataVirtUiException
     */
    public VdbResultSetBean search(String searchText, int page, boolean showDataVirtUiVDBs, String sortColumnId, boolean sortAscending) throws DataVirtUiException;

    /**
     * Gets the VDB Details for the supplied vdb name
     * @param vdbName
     * @param modelsPage the page number of models
     * @return the VDB Details
     * @throws DataVirtUiException
     */
    public VdbDetailsBean getVdbDetails(String vdbName, int modelsPage) throws DataVirtUiException;

    public String getVdbXml(String vdbName) throws DataVirtUiException;

    public void createAndDeployDynamicVdb(String vdbName) throws DataVirtUiException;

    public VdbDetailsBean deploySourceVDBAddImportAndRedeploy(String vdbName, int modelsPageNumber, String sourceVDBName, String modelName, String dataSourceName, String translator) throws DataVirtUiException;

    public VdbDetailsBean addOrReplaceViewModelAndRedeploy(String vdbName, int modelsPageNumber, String viewModelName, String ddlString) throws DataVirtUiException;

    public VdbDetailsBean removeModelsAndRedeploy(String vdbName, int modelsPageNumber, Map<String,String> removeModelNameAndTypeMap) throws DataVirtUiException;               

    /**
     * Called to delete Vdbs.
     * @param vdbNames the VDB names
     * @throws DataVirtUiException
     */
    public void delete(Collection<String> vdbNames) throws DataVirtUiException;
}
