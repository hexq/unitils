/*
 * Copyright 2012,  Unitils.org
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

package org.unitilsnew.core.engine;

import org.unitilsnew.core.Factory;
import org.unitilsnew.core.TestListener;
import org.unitilsnew.core.context.UnitilsContext;
import org.unitilsnew.core.spring.SpringTestListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tim Ducheyne
 */
public class UnitilsTestListenerFactory implements Factory<UnitilsTestListener> {

    protected UnitilsContext unitilsContext;
    protected WrapperForFieldAnnotationListenerFactory wrapperForFieldAnnotationListenerFactory;
    protected WrapperForTestAnnotationListenerFactory wrapperForTestAnnotationListenerFactory;
    protected SpringTestListener springTestListener;


    public UnitilsTestListenerFactory(UnitilsContext unitilsContext, WrapperForFieldAnnotationListenerFactory wrapperForFieldAnnotationListenerFactory, WrapperForTestAnnotationListenerFactory wrapperForTestAnnotationListenerFactory, SpringTestListener springTestListener) {
        this.unitilsContext = unitilsContext;
        this.wrapperForFieldAnnotationListenerFactory = wrapperForFieldAnnotationListenerFactory;
        this.wrapperForTestAnnotationListenerFactory = wrapperForTestAnnotationListenerFactory;
        this.springTestListener = springTestListener;
    }


    public UnitilsTestListener create() {
        List<TestListener> testListeners = createTestListeners();
        return new UnitilsTestListener(testListeners, wrapperForFieldAnnotationListenerFactory, wrapperForTestAnnotationListenerFactory);
    }


    protected List<TestListener> createTestListeners() {
        List<TestListener> testListeners = new ArrayList<TestListener>();

        for (Class<?> testListenerType : unitilsContext.getTestListenerTypes()) {
            TestListener testListener = (TestListener) unitilsContext.getInstanceOfType(testListenerType);
            testListeners.add(testListener);
        }
        testListeners.add(springTestListener);
        return testListeners;
    }
}
