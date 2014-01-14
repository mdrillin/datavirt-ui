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
	public static final String DATASOURCES_SEARCH_TEXT = "datasources.search-text"; //$NON-NLS-1$
    public static final String DATASOURCES_SORT_COLUMN = "datasources.sort-column"; //$NON-NLS-1$

    public static final String DATASOURCE_TYPES_PAGE = "datasourcetypes.page"; //$NON-NLS-1$
	public static final String DATASOURCE_TYPES_SEARCH_TEXT = "datasourcetypes.search-text"; //$NON-NLS-1$
    public static final String DATASOURCE_TYPES_SORT_COLUMN = "datasourcetypes.sort-column"; //$NON-NLS-1$

    public static final String VDBS_PAGE = "virtualdatabases.page"; //$NON-NLS-1$
	public static final String VDBS_SEARCH_TEXT = "virtualdatabases.search-text"; //$NON-NLS-1$
    public static final String VDBS_SORT_COLUMN = "virtualdatabases.sort-column"; //$NON-NLS-1$
	
    public static final String VDBDETAILS_PAGE = "vdbmodels.page"; //$NON-NLS-1$
	public static final String VDBDETAILS_SEARCH_TEXT = "vdbmodels.search-text"; //$NON-NLS-1$
    public static final String VDBDETAILS_SORT_COLUMN = "vdbmodels.sort-column"; //$NON-NLS-1$

    public static final String DATASOURCE_DETAILS_PAGE = "datasourcedetails.page"; //$NON-NLS-1$
	public static final String DATASOURCE_DETAILS_SEARCH_TEXT = "datasourcedetails.search-text"; //$NON-NLS-1$
    public static final String DATASOURCE_DETAILS_SORT_COLUMN = "datasourcedetails.sort-column"; //$NON-NLS-1$
}
