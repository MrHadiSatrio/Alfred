`Alfred` – Android `ViewModel` instantiation made effortless
---

[![JitPack][1]][2]

![Alfred, your trusty View Model butler.](http://i.imgur.com/EDNJaWB.jpg)


### Why should I use it?

```java
   // It's so you can just do this...
   
   SomeViewModel viewModel = SomeViewModelProvider.get(this, "...", 0L);
```

```java
   // Instead this...
   
   SomeViewModel viewModel = SomeViewModelProviders.of(this).get(SomeViewModel.class);
   viewModel.setMessage("...");
   viewModel.setEffGiven(1L);
```

```java
   // Or this...
   
   SomeViewModel viewModel = ViewModelProviders.of(this, new CustomViewModelFactory("...", 1000L)).get(SomeViewModel.class);
   
   ...
   
   public final class CustomViewModelFactory implements ViewModelProvider.Factory {
   
       // About 28 lines of code here (given your ViewModel takes 2 arguments like above).
   }
```


### Usage guide

Getting `Alfred` to simplify your `ViewModel` instantiation is simple:

1. Make sure your `ViewModel` has a constructor containing all the things it needs supplied as parameters:

```java
   public final class SomeViewModel extends ViewModel {
   
      private final int someInt;
      private final String someString;
      private final SomeClass someClass;
      
      public SomeViewModel(int anInt, String aString, SomeClass yourClass) {
         // Initiate the fields..
      }
      
      ...
   }
```

2. Annotate the class with `@GeneratedProvider`:

```java
   @GeneratedProvider
   public final class SomeViewModel extends ViewModel {
      ...
   }
```

3. Build and voila!

```java
   SomeViewModel viewModel = SomeViewModelProvider.get(99, "Problems", new ViewModelsAintOne());
```


### Current limitation

As of its current state, `Alfred` is limited to process only the first constructor declared on each `ViewModel`.
You could override this behavior by explicitly annotating the constructor you want it to process instead with `@Main`:

```java
   @GeneratedProvider
   public final class SomeViewModel extends ViewModel {
   
      private final int someInt;
      private final String someString;
      private final SomeClass someClass;
      private final SomeEnum someEnum;
      
      public SomeViewModel(int anInt, String aString, SomeClass yourClass) {
         this(anInt, aString, yourClass, SomeEnum.NONE);
      }
      
      @Main /* Tells Alfred to process this instead of the first one above. */
      public SomeViewModel(int anInt, String aString, SomeClass yourClass, SomeEnum someEnum) {
         ...
      }
      
      ...
   }
```

Please note that there could only be one `@Main`-annotated constructor declared in your `ViewModel`.


### Including `Alfred` to your project

Include `Alfred` to your Gradle project by adding it as a dependency in your `build.gradle`:

```groovy
   apply plugin: 'com.neenbedankt.android-apt'

   repositories {
       maven { url "https://jitpack.io" }
   }

   dependencies {
       compile 'com.hadisatrio.Alfred:annotations:v1.0.0-RC.2'
       apt 'com.hadisatrio.Alfred:compiler:v1.0.0-RC.2'
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
