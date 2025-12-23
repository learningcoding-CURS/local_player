# Add project specific ProGuard rules here.
<<<<<<< HEAD
# ExoPlayer
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Room
=======

# Keep ExoPlayer classes
-keep class androidx.media3.** { *; }
-keep interface androidx.media3.** { *; }

# Keep Room classes
>>>>>>> 2cfdab441d0f865a9efbdae55c2613ef495acca5
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

<<<<<<< HEAD
# Kotlin
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
=======
# Keep Compose
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }

# Keep Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
>>>>>>> 2cfdab441d0f865a9efbdae55c2613ef495acca5

