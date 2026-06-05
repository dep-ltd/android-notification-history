# ─────────────────────────────────────────────────────────────────
# Room
# ─────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keepclassmembers @androidx.room.Dao interface * { *; }
-keepclassmembers class * {
    @androidx.room.TypeConverter <methods>;
}
-dontwarn androidx.room.paging.**

# ─────────────────────────────────────────────────────────────────
# SQLCipher (net.zetetic:sqlcipher-android 4.5.6+)
# ─────────────────────────────────────────────────────────────────
-keep class net.zetetic.database.** { *; }
-dontwarn net.zetetic.database.**

# ─────────────────────────────────────────────────────────────────
# Hilt — DI graph, ViewModels, entry points, modules
# ─────────────────────────────────────────────────────────────────
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep @dagger.hilt.EntryPoint interface * { *; }
-keep @dagger.Module class * { *; }
-keep class dagger.hilt.** { *; }
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# ─────────────────────────────────────────────────────────────────
# WorkManager + HiltWorker / AssistedInject
# ─────────────────────────────────────────────────────────────────
-keep class * extends androidx.work.ListenableWorker { *; }
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }
-keep @androidx.hilt.work.HiltWorker class * { *; }
-keepclassmembers class * {
    @dagger.assisted.AssistedInject <init>(...);
}
-keep class androidx.work.WorkerParameters { *; }
-keep interface androidx.work.WorkerFactory { *; }

# ─────────────────────────────────────────────────────────────────
# ViewModel (lifecycle)
# ─────────────────────────────────────────────────────────────────
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# ─────────────────────────────────────────────────────────────────
# EncryptedSharedPreferences + Tink (MasterKey, security-crypto)
# ─────────────────────────────────────────────────────────────────
-keep class androidx.security.crypto.** { *; }
-keep class com.google.crypto.tink.** { *; }
-keepclassmembers class com.google.crypto.tink.** { *; }
-dontwarn com.google.crypto.tink.**
-dontwarn com.google.errorprone.annotations.CanIgnoreReturnValue
-dontwarn com.google.errorprone.annotations.CheckReturnValue
-dontwarn com.google.errorprone.annotations.Immutable
-dontwarn com.google.errorprone.annotations.RestrictedApi

# ─────────────────────────────────────────────────────────────────
# DataStore Preferences
# ─────────────────────────────────────────────────────────────────
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**
-dontwarn com.google.protobuf.**

# ─────────────────────────────────────────────────────────────────
# NotificationListenerService
# ─────────────────────────────────────────────────────────────────
-keep class * extends android.service.notification.NotificationListenerService { *; }

# ─────────────────────────────────────────────────────────────────
# Kotlin
# ─────────────────────────────────────────────────────────────────
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings { <fields>; }
-dontwarn kotlin.**
-dontwarn kotlin.reflect.**

# ─────────────────────────────────────────────────────────────────
# Kotlin Coroutines
# ─────────────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** { volatile <fields>; }
-dontwarn kotlinx.coroutines.**

# ─────────────────────────────────────────────────────────────────
# Coil (image loading from file://)
# ─────────────────────────────────────────────────────────────────
-dontwarn coil.**
-dontwarn okhttp3.**
-dontwarn okio.**
