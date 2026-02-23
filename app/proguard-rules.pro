# Keep ExoPlayer/Media3
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Keep subtitle and player interfaces
-keep interface com.videomaster.app.interfaces.** { *; }
-keep class com.videomaster.app.subtitle.** { *; }
-keep class com.videomaster.app.model.** { *; }
