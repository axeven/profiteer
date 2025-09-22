# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep Google Play Services
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Keep Google Auth classes
-keep class com.google.android.gms.auth.** { *; }
-keep class com.google.android.gms.common.** { *; }

# Logging optimization rules for release builds
# Remove debug and info logging in release builds to improve performance and reduce APK size
-assumenosideeffects class com.axeven.profiteerapp.utils.logging.Logger {
    public void d(...);
    public void i(...);
}

-assumenosideeffects class com.axeven.profiteerapp.utils.logging.DebugLogger {
    public void d(...);
    public void i(...);
}

-assumenosideeffects class com.axeven.profiteerapp.utils.logging.ReleaseLogger {
    public void d(...);
    public void i(...);
}

-assumenosideeffects class com.axeven.profiteerapp.utils.logging.PerformanceOptimizedLogger {
    public void d(...);
    public void i(...);
}

# Remove debug-only logging utilities in release builds
-assumenosideeffects class com.axeven.profiteerapp.utils.logging.LogFormatter {
    public static java.lang.String formatUserAction(...);
    public static java.lang.String formatTransaction(...);
    public static java.lang.String formatPerformance(...);
}

# Keep error and warning logging in all builds for crash reporting
-keep class com.axeven.profiteerapp.utils.logging.Logger {
    public void w(...);
    public void e(...);
}

# Preserve logging infrastructure for dependency injection
-keep class com.axeven.profiteerapp.utils.logging.** { *; }
-keep @dagger.hilt.android.qualifiers.* class * { *; }
-keep @javax.inject.* class * { *; }