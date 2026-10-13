-dontobfuscate
-dontoptimize
-keepattributes *
-keep class app.czyeru.** {
  *;
}
-keep class com.google.** {
  *;
}
-keep class com.eclipsesource.v8.** {
  *;
}
# Proguard can strip away kotlin intrinsics methods that are used by extension Kotlin code. Unclear why.
-keep class kotlin.jvm.internal.Intrinsics {
    public static *;
}
-dontwarn javax.lang.model.element.Modifier