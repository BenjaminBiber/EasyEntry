# Add project specific ProGuard rules here.
# Keep Moshi generated adapters
-keep class com.easyentry.app.data.remote.dto.** { *; }
-keepclassmembers class ** {
    @com.squareup.moshi.Json <fields>;
    @com.squareup.moshi.JsonQualifier <fields>;
}
