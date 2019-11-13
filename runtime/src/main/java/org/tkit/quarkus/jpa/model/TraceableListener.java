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
import java.security.Principal;
import java.util.Date;

import javax.inject.Inject;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

/**
 * The traceable entity listener.
 */
public class TraceableListener implements Serializable {

    /**
     * The principal
     */
    @Inject
    Principal principal;

    /**
     * The UID for this class.
     */
    private static final long serialVersionUID = -7253672246009843767L;

    /**
     * Marks the entity as created.
     *
     * @param entity the entity.
     */
    @PrePersist
    public void prePersist(TraceablePersistent entity) {
        if (!entity.isControlTraceabilityManual()) {
            if (principal != null) {
                entity.setCreationUser(principal.getName());
                entity.setModificationUser(entity.getCreationUser());
            }
            entity.setCreationDate(new Date());
            entity.setModificationDate(entity.getCreationDate());
        }
    }

    /**
     * Marks the entity as changed.
     *
     * @param entity the entity.
     */
    @PreUpdate
    public void preUpdate(TraceablePersistent entity) {
        if (!entity.isControlTraceabilityManual()) {
            if (principal != null) {
                entity.setModificationUser(principal.getName());
            }
            entity.setCreationDate(new Date());
        }
    }

}