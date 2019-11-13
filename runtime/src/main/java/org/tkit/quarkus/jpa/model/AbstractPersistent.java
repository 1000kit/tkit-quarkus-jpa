/*
 * Copyright 2019 1000kit.org.
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
package org.tkit.quarkus.jpa.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.Transient;
import javax.persistence.Version;

/**
 * The persistent entity interface.
 */
@MappedSuperclass
public abstract class AbstractPersistent implements Serializable {

    /**
     * The UID of this class.
     */
    private static final long serialVersionUID = -8041083748062531412L;


    /**
     * The entity graph name for the load all method.
     */
    public static final String ENTITY_GRAPH_LOAD_ALL = ".loadAll";

    /**
     * The entity graph name for the load by GUID method.
     */
    public static final String ENTITY_GRAPH_LOAD_BY_GUID = ".loadByGuid";

    /**
     * Optimistic lock version
     */
    @Version
    @Column(name = "OPTLOCK", nullable = false)
    private Integer version;

    /**
     * The persisted flag.
     */
    @Transient
    protected boolean persisted;

    /**
     * Gets the optimistic lock version.
     *
     * @return the optimistic lock version.
     */
    public Integer getVersion() {
        return this.version;
    }

    /**
     * Sets the version.
     *
     * @param version actual Version
     */
    public void setVersion(Integer version) {
        this.version = version;
    }

    /**
     * Gets the GUID.
     *
     * @return the GUID.
     */
    public abstract String getGuid();

    /**
     * Sets the GUID.
     *
     * @param guid the new GUID.
     */
    public abstract void setGuid(String guid);

    /**
     * The entity life-cycle method.
     */    
    @PostLoad
    @PostUpdate
    @PostPersist
    public void checkPersistentState() {
        this.persisted = true;
    }

    /**
     * Gets the {@code true} if the entity is persisted.
     *
     * @return {@code true} if the entity is persisted.
     */
    public boolean isPersisted() {
        return persisted;
    }

    /**
     * Sets the persisted flag.
     *
     * @param persisted the persisted flag.
     */
    public void setPersisted(boolean persisted) {
        this.persisted = persisted;
    }

}
