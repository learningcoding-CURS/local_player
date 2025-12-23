# Add project specific ProGuard rules here.
# ExoPlayer
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

