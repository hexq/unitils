/*
 * Copyright 2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unitils.hibernate;

import org.hibernate.SessionFactory;
import org.unitils.UnitilsJUnit3;
import org.unitils.core.ConfigurationLoader;
import org.unitils.hibernate.annotation.HibernateSessionFactory;

/**
 * Test class for injection methods of the HibernateModule
 *
 * @author Tim Ducheyne
 * @author Filip Neven
 */
public class HibernateModuleInjectionTest extends UnitilsJUnit3 {

    /* Tested object */
    private HibernateModule hibernateModule;


    /**
     * Initializes the test fixture.
     */
    protected void setUp() throws Exception {
        super.setUp();

        org.apache.commons.configuration.Configuration configuration = new ConfigurationLoader().loadConfiguration();
        hibernateModule = new HibernateModule();
        hibernateModule.init(configuration);
    }


    /**
     * Tests hibernate session factory injection for a field and a setter method.
     */
    public void testInjectHibernateSessionFactory() {
        HibernateTestSessionFactory hibernateTestSessionFactory = new HibernateTestSessionFactory();
        hibernateModule.injectHibernateSessionFactory(hibernateTestSessionFactory);

        assertNotNull(hibernateTestSessionFactory.sessionFactoryField);
        assertSame(hibernateTestSessionFactory.sessionFactoryField, hibernateTestSessionFactory.sessionFactorySetter);
    }


    /**
     * Test hibernate test for session factory injection.
     */
    @HibernateSessionFactory("org/unitils/hibernate/hibernate.cfg.xml")
    public class HibernateTestSessionFactory {

        @HibernateSessionFactory
        private SessionFactory sessionFactoryField = null;

        private SessionFactory sessionFactorySetter;

        @HibernateSessionFactory
        public void setSessionFactorySetter(SessionFactory sessionFactorySetter) {
            this.sessionFactorySetter = sessionFactorySetter;
        }
    }

}