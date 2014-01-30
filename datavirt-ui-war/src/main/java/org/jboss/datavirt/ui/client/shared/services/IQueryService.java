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

import java.util.List;

import org.jboss.datavirt.ui.client.shared.beans.QueryColumnResultSetBean;
import org.jboss.datavirt.ui.client.shared.beans.QueryResultSetBean;
import org.jboss.datavirt.ui.client.shared.beans.QueryTableProcBean;
import org.jboss.datavirt.ui.client.shared.exceptions.DataVirtUiException;
import org.jboss.errai.bus.server.annotations.Remote;

/**
 * Provides service for running queries against jdbc sources on the server
 *
 * @author mdrillin@redhat.com
 */
@Remote
public interface IQueryService {

    /**
     * Get the list of all Data Source Names.
     * @param teiidOnly if 'true' returns only the teiid sources
     * @return the list of data source names
     * @throws DataVirtUiException
     */
    public List<String> getDataSourceNames(boolean teiidOnly) throws DataVirtUiException;

    public List<QueryTableProcBean> getTablesAndProcedures(String dataSource) throws DataVirtUiException;

    public QueryColumnResultSetBean getQueryColumnResultSet(int page, String dataSource, String fullTableName) throws DataVirtUiException;

    public QueryResultSetBean executeSql(int page, String dataSource, String sql) throws DataVirtUiException;

}
