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
import android.content.Context;

import com.hadisatrio.libs.android.viewmodelprovider.GeneratedProvider;
import com.hadisatrio.libs.android.viewmodelprovider.Main;

import java.util.Collections;
import java.util.List;

@GeneratedProvider
public final class AnotherDopeViewModel extends ViewModel {

    private final Context context;
    private final Long fucksGiven;
    private final String whatNot;
    private final List<Integer> someNumbers;

    public AnotherDopeViewModel(Context context, Long fucksGiven) {
        this(context, fucksGiven, "", Collections.<Integer>emptyList());
    }

    @Main
    public AnotherDopeViewModel(Context context, Long fucksGiven, String whatNot, List<Integer> someNumbers) {
        this.context = context;
        this.fucksGiven = fucksGiven;
        this.whatNot = whatNot;
        this.someNumbers = someNumbers;
    }
}
