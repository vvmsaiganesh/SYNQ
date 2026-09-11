# Keep kotlinx.serialization models
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.vitalis.app.**$$serializer { *; }
-keepclassmembers class com.vitalis.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.vitalis.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Health Connect
-keep class androidx.health.connect.client.** { *; }
