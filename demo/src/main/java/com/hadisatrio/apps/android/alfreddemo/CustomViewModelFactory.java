/*
 *    Copyright (C) 2017 Hadi Satrio
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.hadisatrio.apps.android.alfreddemo;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.content.Context;

import java.lang.reflect.InvocationTargetException;

public final class CustomViewModelFactory implements ViewModelProvider.Factory {

    private final Context context;
    private final Long fucksGiven;

    CustomViewModelFactory(Context context, Long fucksGiven) {
        this.context = context;
        this.fucksGiven = fucksGiven;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (LameViewModel.class.isAssignableFrom(modelClass)) {
            try {
                return modelClass.getConstructor(android.content.Context.class, java.lang.Long.class)
                        .newInstance(context, fucksGiven);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Cannot create an instance of " + modelClass, e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Cannot create an instance of " + modelClass, e);
            } catch (InstantiationException e) {
                throw new RuntimeException("Cannot create an instance of " + modelClass, e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException("Cannot create an instance of " + modelClass, e);
            }
        } else {
            throw new RuntimeException("Couldn't create an instance of " + modelClass);
        }
    }
}
