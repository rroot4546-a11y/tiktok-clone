# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Gson
-keep class com.tiktokclone.data.models.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Socket.IO
-keep class io.socket.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }

# ExoPlayer
-keep class androidx.media3.** { *; }
