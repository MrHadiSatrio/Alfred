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

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public final class SeeAlfredInAction extends AppCompatActivity {

    private DopeViewModel dopeViewModel;
    private AnotherDopeViewModel anotherDopeViewModel;
    private LameViewModel lameViewModel;
    private EvenLamerViewModel evenLamerViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_see_alfred_in_action);

        // These are dope...
        dopeViewModel = DopeViewModelProvider.get(this, this, 0L);
        anotherDopeViewModel = AnotherDopeViewModelProvider.get(this, this, 0L, "");

        // ...this is lame..
        lameViewModel = ViewModelProviders.of(this, new CustomViewModelFactory(this, 0L)).get(LameViewModel.class);

        // ..and don't even get me started with this.
        evenLamerViewModel = ViewModelProviders.of(this).get(EvenLamerViewModel.class);
        evenLamerViewModel.setContext(this);
        evenLamerViewModel.setFucksGiven(1000L);
    }
}
