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

package org.jboss.datavirt.ui.client.local.services;

/**
 * Constants used when storing state in the app state service.
 *
 * @author mdrillin@redhat.com
 */
public final class ApplicationStateKeys {

	public static final String DATASOURCES_PAGE = "datasources.page"; //$NON-NLS-1$
	public static final String DATASOURCES_FILTER_TEXT = "datasources.filter-text"; //$NON-NLS-1$
    public static final String DATASOURCES_SORT_COLUMN = "datasources.sort-column"; //$NON-NLS-1$

    public static final String DATASOURCE_TYPES_PAGE = "datasourcetypes.page"; //$NON-NLS-1$
    public static final String DATASOURCE_TYPES_SORT_COLUMN = "datasourcetypes.sort-column"; //$NON-NLS-1$

    public static final String VDBS_PAGE = "virtualdatabases.page"; //$NON-NLS-1$
	public static final String VDBS_FILTER_TEXT = "virtualdatabases.filter-text"; //$NON-NLS-1$
    public static final String VDBS_SORT_COLUMN = "virtualdatabases.sort-column"; //$NON-NLS-1$
	
    public static final String VDBDETAILS_PAGE = "vdbmodels.page"; //$NON-NLS-1$
    public static final String VDBDETAILS_SORT_COLUMN = "vdbmodels.sort-column"; //$NON-NLS-1$

    public static final String DATASOURCE_DETAILS_SORT_COLUMN_CORE = "datasourcedetails.sort-column-core"; //$NON-NLS-1$
    public static final String DATASOURCE_DETAILS_SORT_COLUMN_ADV = "datasourcedetails.sort-column-adv"; //$NON-NLS-1$
    
    public static final String ADD_DATASOURCE_DIALOG_SORT_COLUMN_CORE = "adddatasourcedialog.sort-column-core"; //$NON-NLS-1$
    public static final String ADD_DATASOURCE_DIALOG_SORT_COLUMN_ADV = "adddatasourcedialog.sort-column-adv"; //$NON-NLS-1$
    
}
