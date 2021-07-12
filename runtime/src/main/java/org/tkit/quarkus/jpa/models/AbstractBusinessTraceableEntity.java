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
package org.tkit.quarkus.jpa.models;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * The persistent entity with string GUID and BusinessId
 *
 */
@MappedSuperclass
public class AbstractBusinessTraceableEntity extends AbstractTraceableEntity<String> {


    
    /**
     * String ID of entity
     */
    @Id
    @Column(name = "GUID")
    private String id = UUID.randomUUID().toString();


    
    /**
     * The business ID.
     * BID Column will be filled in super class
     * 
     */
    @Column(name = "BID", insertable = false, updatable = false)
    private Long bid;


    /**
     * {@inheritDoc }
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void setId(String guid) {
        this.id = guid;
    }

    /**
     * Getter on Business ID
     */
    public Long getBid() {
        return bid;
    }

    /**
     * Setter on Business ID
     */
    public void setBid(Long bid) {
        this.bid = bid;
    }

    /**
     * {@inheritDoc }
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TraceableEntity other = (TraceableEntity) obj;
        Object guid = getId();
        Object otherGuid = other.getId();

        if (guid == null) {
            if (otherGuid != null) {
                return false;
            } else {
                return super.equals(obj);
            }
        } else if (!guid.equals(otherGuid)) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc }
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(getId());
        return result;
    }
}
