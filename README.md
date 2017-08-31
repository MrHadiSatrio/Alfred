Alfred – Android `ViewModel` instantiation made effortless
---

[![JitPack][1]][2]

![Alfred, your trusty View Model butler.](http://i.imgur.com/EDNJaWB.jpg)


### Why should I use it?

```
   // It's so you can just do this...
   
   DopeViewModel dopeViewModel = DopeViewModelProvider.get(this, "...", 0L);
```

```
   // Instead this...
   
   LameViewModel lameViewModel = ViewModelProviders.of(this).get(LameViewModel.class);
   lameViewModel.setMessage("...");
   lameViewModel.setFucksGiven(1L);
```

```
   // Or, God forbid, this...
   
   EvenLamerViewModel evenLamerViewModel = ViewModelProviders.of(this, new CustomViewModelFactory("...", 1000L)).get(EvenLamerViewModel.class);
   
   ...
   
   public final class CustomViewModelFactory implements ViewModelProvider.Factory {
   
       // About 28 lines of code here (given your ViewModel takes 2 arguments like above).
   }
```


### Including `Alfred` to your project

Include `Alfred` to your Gradle project by adding it as a dependency in your `build.gradle`:

```
   apply plugin: 'com.neenbedankt.android-apt'

   repositories {
       maven { url "https://jitpack.io" }
   }

   dependencies {
       compile 'com.hadisatrio.Alfred:annotations:v1.0.0-RC.1'
       apt 'com.hadisatrio.Alfred:compiler:v1.0.0-RC.1'
   }
```

You'll also need `android-apt` in your project. See how to do it [here.](https://bitbucket.org/hvisser/android-apt)


### Contributions

Any kind of contribution will be appreciated. PR away!


### License

`Alfred` is published under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

---

_**p.s.**, Please let me know if you're using `Alfred` in your projects. Drop an email at
hi[you-know-what-to-put-here]hadisatrio.com._ ;)

[1]: https://jitpack.io/v/MrHadiSatrio/Alfred.svg?style=flat-square
[2]: https://jitpack.io/#MrHadiSatrio/Alfred
