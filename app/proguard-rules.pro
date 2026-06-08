# Add project specific ProGuard rules here.
if you need to add custom rules for libraries, do so here.

# Keep data classes used by Retrofit/Gson (serialization via reflection)
-keep class com.example.gymapp.domain.model.** { *; }

# Keep Retrofit service interfaces
-keep interface com.example.gymapp.data.remote.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltAndroidApp

# Keep DataStore classes
-keep class androidx.datastore.** { *; }

# Keep Compose classes used at runtime
-keep class androidx.compose.** { *; }

# Keep BuildConfig
-keep class com.example.gymapp.BuildConfig { *; }

# Keep TokenManager
-keep class com.example.gymapp.data.local.TokenManager { *; }

# Gson specifics
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }

# OkHttp / Retrofit
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keepattributes Exceptions

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Coil
-keep class coil.** { *; }
