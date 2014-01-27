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
package org.jboss.datavirt.ui.server;

import javax.inject.Singleton;

import org.apache.commons.configuration.Configuration;
import org.jboss.datavirt.ui.server.api.AdminApiClientAccessor;
import org.overlord.commons.config.ConfigurationFactory;

/**
 * Global access to configuration information.
 *
 * @author eric.wittmann@redhat.com
 */
@Singleton
public class DataVirtUIConfig {

    public static final String DATAVIRT_UI_CONFIG_FILE_NAME     = "datavirt-ui.config.file.name"; //$NON-NLS-1$
    public static final String DATAVIRT_UI_CONFIG_FILE_REFRESH  = "datavirt-ui.config.file.refresh"; //$NON-NLS-1$

    public static final String DATAVIRT_API_ENDPOINT = "datvirt-ui.api.endpoint"; //$NON-NLS-1$
    public static final String DATAVIRT_API_VALIDATING = "datavirt-ui.api.validating"; //$NON-NLS-1$
    public static final String DATAVIRT_API_AUTH_PROVIDER = "datavirt-ui.api.authentication.provider"; //$NON-NLS-1$
    public static final String DATAVIRT_API_BASIC_AUTH_USER = "datavirt-ui.api.authentication.basic.user"; //$NON-NLS-1$
    public static final String DATAVIRT_API_BASIC_AUTH_PASS = "datavirt-ui.api.authentication.basic.password"; //$NON-NLS-1$
    public static final String DATAVIRT_API_SAML_AUTH_ISSUER = "datavirt-ui.api.authentication.saml.issuer"; //$NON-NLS-1$
    public static final String DATAVIRT_API_SAML_AUTH_SERVICE = "datavirt-ui.api.authentication.saml.service"; //$NON-NLS-1$
    public static final String DATAVIRT_API_SAML_AUTH_SIGN_ASSERTIONS = "datavirt-ui.api.authentication.saml.sign-assertions"; //$NON-NLS-1$
    public static final String DATAVIRT_API_SAML_AUTH_KEYSTORE = "datavirt-ui.api.authentication.saml.keystore"; //$NON-NLS-1$
    public static final String DATAVIRT_API_SAML_AUTH_KEYSTORE_PASSWORD = "datavirt-ui.api.authentication.saml.keystore-password"; //$NON-NLS-1$
    public static final String DATAVIRT_API_SAML_AUTH_KEY_ALIAS = "datavirt-ui.api.authentication.saml.key-alias"; //$NON-NLS-1$
    public static final String DATAVIRT_API_SAML_AUTH_KEY_PASSWORD = "datavirt-ui.api.authentication.saml.key-password"; //$NON-NLS-1$

    public static Configuration config;
    static {
    	// These are overrides - if the system config file name is set.  otherwise loads server configuration
        String configFile = System.getProperty(DATAVIRT_UI_CONFIG_FILE_NAME);
        String refreshDelayStr = System.getProperty(DATAVIRT_UI_CONFIG_FILE_REFRESH);
        Long refreshDelay = 5000l;
        if (refreshDelayStr != null) {
            refreshDelay = new Long(refreshDelayStr);
        }

        config = ConfigurationFactory.createConfig(
                configFile,
                "datavirt-ui.properties", //$NON-NLS-1$
                refreshDelay,
                "/META-INF/config/org.jboss.datavirt.ui.server.api.properties", //$NON-NLS-1$
                AdminApiClientAccessor.class);
    }

    /**
     * Constructor.
     */
    public DataVirtUIConfig() {
    }

    /**
     * @return the configuration
     */
    public Configuration getConfig() {
        return config;
    }

}
