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
package org.jboss.datavirt.ui.client.shared.beans;

import java.io.Serializable;
import java.util.List;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Models the set of Data Source Types returned by a type retrieval.
 *
 * @author mdrillin@redhat.com
 */
@Portable
public class DataSourceTypeResultSetBean implements Serializable {

    private static final long serialVersionUID = DataSourceTypeResultSetBean.class.hashCode();

    private List<DataSourceTypeBean> dataSourceTypes;
    private long totalResults;
    private int itemsPerPage;
    private int startIndex;

    /**
     * Constructor.
     */
    public DataSourceTypeResultSetBean() {
    }

    /**
     * @return the Data sources
     */
    public List<DataSourceTypeBean> getDataSourceTypes() {
        return dataSourceTypes;
    }

    /**
     * @param datasources the datasources to set
     */
    public void setDataSourceTypes(List<DataSourceTypeBean> dataSourceTypes) {
        this.dataSourceTypes = dataSourceTypes;
    }
    
    /**
     * @return the totalResults
     */
    public long getTotalResults() {
        return totalResults;
    }

    /**
     * @param totalResults the totalResults to set
     */
    public void setTotalResults(long totalResults) {
        this.totalResults = totalResults;
    }

    /**
     * @return the itemsPerPage
     */
    public int getItemsPerPage() {
        return itemsPerPage;
    }

    /**
     * @param itemsPerPage the itemsPerPage to set
     */
    public void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    /**
     * @return the startIndex
     */
    public int getStartIndex() {
        return startIndex;
    }

    /**
     * @param startIndex the startIndex to set
     */
    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

}
