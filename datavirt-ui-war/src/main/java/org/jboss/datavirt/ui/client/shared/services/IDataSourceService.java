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
import java.util.List;
import java.util.Map;

import org.jboss.datavirt.ui.client.shared.beans.DataSourceDetailsBean;
import org.jboss.datavirt.ui.client.shared.beans.DataSourcePropertyBean;
import org.jboss.datavirt.ui.client.shared.beans.DataSourceResultSetBean;
import org.jboss.datavirt.ui.client.shared.beans.DataSourceTypeResultSetBean;
import org.jboss.datavirt.ui.client.shared.exceptions.DataVirtUiException;
import org.jboss.errai.bus.server.annotations.Remote;

/**
 * Provides a way to get and set DataSources and related info
 *
 * @author mdrillin@redhat.com
 */
@Remote
public interface IDataSourceService {

    /**
     * Get the Data Sources using the provided info.
     * @param searchText
     * @param page
     * @param sortColumnId
     * @param sortAscending
     * @throws DataVirtUiException
     */
    public DataSourceResultSetBean search(String searchText, int page, String sortColumnId, boolean sortAscending) throws DataVirtUiException;

    public DataSourceDetailsBean getDataSourceDetails(String dsName) throws DataVirtUiException;

    public List<String> getDataSourceTypes( ) throws DataVirtUiException;

    public List<String> getDataSources( ) throws DataVirtUiException;

    public List<String> getTranslators( ) throws DataVirtUiException;

    public Map<String,String> getDefaultTranslatorMap() throws DataVirtUiException;
    
    public DataSourceTypeResultSetBean getDataSourceTypeResultSet(int page, String sortColumnId, boolean sortAscending) throws DataVirtUiException;

    public List<DataSourcePropertyBean> getDataSourceTypeProperties(String dsType) throws DataVirtUiException;

    public void update(DataSourceDetailsBean dataSource) throws DataVirtUiException;

    public void deleteDataSource(String dsName) throws DataVirtUiException;

    /**
     * Called to delete DataSources.
     * @param dsNames the DataSource names
     * @throws DataVirtUiException
     */
    public void deleteDataSources(Collection<String> dsNames) throws DataVirtUiException;

    /**
     * Called to delete DataSource Types.
     * @param dsTypes the DataSource types
     * @throws DataVirtUiException
     */
    public void deleteTypes(Collection<String> dsTypes) throws DataVirtUiException;

}
