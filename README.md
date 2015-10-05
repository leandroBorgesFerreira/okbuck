# OkBuck
[ ![Download](https://api.bintray.com/packages/piasy/maven/OkBuck/images/download.svg) ](https://bintray.com/piasy/maven/OkBuck/_latestVersion)
[![Master branch build status](https://travis-ci.org/Piasy/OkBuck.svg?branch=master)](https://travis-ci.org/Piasy/OkBuck)

A gradle plugin that helps developer migrate to code with Android Studio + Gradle, but build &amp;&amp; install with buck.
 	
Only Twelve line config to migrate from Android Studio + gradle to facebook BUCK.

[中文版](README-zh.md)

## Why OkBuck?
Android Studio + Gradle has been many Android developers' option, and to migrate to buck, there are many works to be done, both difficult and buggy. OkBuck aims to provide a gradle plugin, which will do these buggy job for you automaticlly after several lines configuration.

## What will OkBuck do for you?
+  Generate `.buckconfig`.
+  Copy third-party libraries' jar/aar files to your project directory.
+  Generate `BUCK` file for third-party libraries and each sub modules.
+  After you run `./gradlew okbuck` in your project root directory, you can run `buck install app` now (suppose your application module names `app`).

## How to use OkBuck?
1. Add this lines into buildscript dependencies part of root project build.gradle: `classpath "com.github.piasy:okbuck-gradle-plugin:${latest version}"`

2. Add this line into root project build.gradle: `apply plugin: 'com.github.piasy.okbuck-gradle-plugin'`

3. Add this `okbuck` block into root project build.gradle:
    
    ```gradle
    okbuck {
        target "android-23"
        keystore "debug.keystore"
        keystoreProperties "debug.keystore.properties"
        overwrite true
        resPackages = [
            dummylibrary: 'com.github.piasy.okbuck.example.dummylibrary',
            app: 'com.github.piasy.okbuck.example',
        ]
    }
    ```

    `android-23` works equally with `targetSdkVersion 23`; `debug.keystore` and `debug.keystore.properties` are signing related file, which should be put under **application module's root directory**; `overwrite` is used to control whether overwrite existing buck files; `resPackages` is used to set Android library module and Android Application module's package name for generated resources (`R` in common cases), you need substitute dummylibrary/app with your own module name, and set the corrosponding package name inside the single quote, which should be the same package name specified in the corrosponding module's AndroidManifest.xml.
    
4. After executing the okbuck gradle task by `./gradlew okbuck`, you can run `buck install app` now, enjoy your life with buck :)

5. It's really only 12 lines config: ~~step one will only need one line `classpath 'com.github.piasy:okbuck-gradle-plugin:0.0.1'` when OkBuck is sync to jcenter (this will be soon)~~ available now, and then these three steps above only need 12 lines!

## More job need to be done
OkBuck can only generate the buck config for you, so if your source code is incompitable with buck, you need do more job.

+  Dependency conflict:

    If you see `*** has already been defined` or
    
    ```java
    BUILD FAILED: //app:bin#dex_merge failed on step dx with an exception:
    Multiple dex files define***
    com.android.dex.DexException: Multiple dex files define ***
    ```
    
    when you run `buck install app`, you come with dependency confliction, which means multiple modules (let's name them moduleA and moduleB) depend on the same dependency (such as gson), and another module (name it moduleC) depends on moduleA and moduleB, disaster happens. Current work-around is to move all common dependencies into one module, such as moduleA, and make all modules depend on moduleA. This will be addressed in future releases of OkBuck.

+  Dependencies in local libs directory may not be imported correctly. Current work-around is to avoid use local dependencies, but this problem will be definitely addressed soon by OkBuck.
    
+  References to `R`. Buck is not compitable with libraries like ButterKnife, because R definitions generated by buck are not final, current work-around is change `@Bind`/`@InjectView` to `ButterKnife.findById`, and change `@OnClick` to `setOnClickListener`, etc. And reference to `R` resources from another module may also have problems, avoid doing this at this moment please, and also avoid defining resources in different module with the same resources name.

+  `javax.annotation` dependency: if your module depends on `javax.annotation`, use the `compile` scope rather than `provided` scope.

+  buck can't compile with java 8, so it's incompitable with retrolambda, no more lambda :(
    
+  Maybe there are more caveats waiting for you, but for the super fast build brought by buck, it's worthwhile.

+  The rest modules in this repo is a full example usage of OkBuck.

## Known caveats
+  Not compitable with `ButterKnife` (buck)
+  Not compitable with `RetroLambda` (buck)
+  `javax.annotation` dependency should be `compile` scope, rather `provided` (OkBuck)
+  Cross module reference on `R` (buck), see above and avoid it
+  Could not refer to design support library's string resource `appbar_scrolling_view_behavior` (buck), that's the specific scenario of the above caveat, quick solution:
  +  define your own string resource: `<string name="my_appbar_scrolling_view_behavior" translatable="false">android.support.design.widget.AppBarLayout$ScrollingViewBehavior</string>`, and use it in your layout file
  +  or use the content directly in your layout file: `app:layout_behavior="android.support.design.widget.AppBarLayout$ScrollingViewBehavior"`

## Troubleshooting
If you come with bugs of OkBuck, please [open an issue](https://github.com/Piasy/OkBuck/issues/new), and it's really appreciated to post the output of `./gradle okbuck` at the same time.

## TODO
+  ~~handle apt, provided dependencies~~
+  res reference on aar dependency
+  test/androidTest support, product flavor support
+  better solution for dependency conflict
+  better solution for local jar dependency
+  more configuration option
+  ~~ci~~
+  code optimization/java doc

## Acknowledgement
+  Thanks for Facebook open source [buck](https://github.com/facebook/buck) build system.
+  Thanks for the discussion and help from [promeG](https://github.com/promeG/) during my development.
