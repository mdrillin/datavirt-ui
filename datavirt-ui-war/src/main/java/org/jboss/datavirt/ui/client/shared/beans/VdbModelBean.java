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

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;

/**
 * A data bean for returning information for a VDB Model.
 *
 * @author mdrillin@redhat.com
 */
@Portable
@Bindable
public class VdbModelBean {

    private String name;
    private String status;
    private String type;
    private String translator;
    private String jndiSource;
    private String ddl;

	/**
     * Constructor.
     */
    public VdbModelBean() {
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }
    
    /**
     * @return the translator
     */
    public String getTranslator() {
        return translator;
    }
    
    /**
     * @return the jndiSource
     */
    public String getJndiSource() {
        return jndiSource;
    }
    
    public String getDdl() {
		return ddl;
	}

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @param translator the translator to set
     */
    public void setTranslator(String translator) {
        this.translator = translator;
    }

    /**
     * @param type the type to set
     */
    public void setJndiSource(String jndiSource) {
        this.jndiSource = jndiSource;
    }

	public void setDdl(String ddl) {
		this.ddl = ddl;
	}

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result;
        return result;
    }

}
