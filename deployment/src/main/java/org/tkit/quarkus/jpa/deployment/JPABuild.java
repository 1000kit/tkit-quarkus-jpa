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

package org.tkit.quarkus.jpa.deployment;

import io.quarkus.arc.deployment.UnremovableBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.BytecodeTransformerBuildItem;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.pkg.steps.NativeBuild;
import io.quarkus.hibernate.orm.deployment.HibernateEnhancersRegisteredBuildItem;
import org.jboss.jandex.*;
import org.tkit.quarkus.jpa.daos.AbstractDAO;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.StaticMetamodel;
import java.util.Collection;
import java.util.List;


/**
 * The JPA build extension.
 */
public class JPABuild {

    /**
     * The extension name.
     */
    private static final String FEATURE_NAME = "tkit-jpa";

    /**
     * The abstract entity service class.
     */
    private static final DotName DOT_NAME_REPOSITORY = DotName.createSimple(AbstractDAO.class.getName());

    /**
     * The entity class.
     */
    private static final DotName ENTITY = DotName.createSimple(Entity.class.getName());

    private static final DotName DOT_NAME_ENTITY_MANAGER = DotName.createSimple(EntityManager.class.getName());

    /**
     * The name of the entity annotation attribute name.
     */
    private static final String ATTRIBUTE_NAME = "name";

    /**
     * The extension name.
     *
     * @return the feature build item.
     */
    @BuildStep
    FeatureBuildItem createFeatureItem() {
        return new FeatureBuildItem(FEATURE_NAME);
    }

    @BuildStep
    UnremovableBeanBuildItem ensureBeanLookupAvailable() {
        return new UnremovableBeanBuildItem(new UnremovableBeanBuildItem.BeanTypeExclusion(DOT_NAME_ENTITY_MANAGER));
    }

    /**
     * Update entity dao services to have entity class name and entity name.
     *
     * @param index        the index.
     * @param transformers the transformer
     */
    @BuildStep
    void build(CombinedIndexBuildItem index,
               HibernateEnhancersRegisteredBuildItem hibernateMarker,
               BuildProducer<BytecodeTransformerBuildItem> transformers) {

        for (ClassInfo classInfo : index.getIndex().getAllKnownSubclasses(DOT_NAME_REPOSITORY)) {
            if (classInfo.superClassType().kind() == Type.Kind.PARAMETERIZED_TYPE) {
                Type entity = classInfo.superClassType().asParameterizedType().arguments().get(0);
                ClassInfo ec = index.getIndex().getClassByName(entity.name());
                String name = entity.name().withoutPackagePrefix();

                if (ec.annotations() != null) {
                    List<AnnotationInstance> annotations = ec.annotations().get(ENTITY);
                    if (annotations != null && !annotations.isEmpty()) {
                        AnnotationInstance annotationInstance = annotations.get(0);
                        for (AnnotationValue a : annotationInstance.values()) {
                            if (ATTRIBUTE_NAME.equals(a.name())) {
                                name = a.asString();
                            }
                        }
                    }
                }

                transformers.produce(new BytecodeTransformerBuildItem(classInfo.name().toString(), new EntityServiceBuilderEnhancer(name, entity.name().toString())));
            }
        }
    }

    @BuildStep(onlyIf = NativeBuild.class)
    public void test(CombinedIndexBuildItem index,
                     BuildProducer<ReflectiveClassBuildItem> reflective,
                     HibernateEnhancersRegisteredBuildItem hibernateMarker) {
            Collection<AnnotationInstance> annos = index.getIndex().getAnnotations(DotName.createSimple(StaticMetamodel.class.getName()));
            if (annos != null) {
                String[] metamodelClasses = annos.stream().map(a -> a.target().asClass().toString()).toArray(String[]::new);
                if (metamodelClasses.length > 0) {
                    reflective.produce(new ReflectiveClassBuildItem(false, false, true, metamodelClasses));
                }
            }
    }

}
