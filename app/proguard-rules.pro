############################################
# 공통: 어노테이션/제네릭/코틀린 메타데이터 유지
############################################
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses,EnclosingMethod
-keepattributes RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations,RuntimeInvisibleParameterAnnotations
-keepattributes AnnotationDefault

# Kotlin (특히 Moshi/Reflect/Serialization에서 필요)
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-dontwarn kotlin.reflect.**

############################################
# Retrofit / OkHttp
############################################
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**

# Retrofit의 HTTP 어노테이션 유지(보통 R8가 알아서 처리하지만 안전빵)
-keepclassmembers interface * {
    @retrofit2.http.* <methods>;
}

############################################
# Moshi (현재 코드상 "moshi-kotlin" = 리플렉션 기반 가능성 높음)
############################################
-dontwarn com.squareup.moshi.**
-keep class com.squareup.moshi.** { *; }

# @Json, @JsonClass 등을 사용하는 모델 클래스/필드가 난독화로 깨지지 않게
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}
-keep @com.squareup.moshi.JsonClass class * { *; }

# (만약 @JsonClass(generateAdapter = true) + moshi-codegen을 나중에 붙이면 아래도 유효)
-keep class **_JsonAdapter { *; }

############################################
# Kotlinx Serialization (plugin.serialization 사용 중이라 안전하게)
############################################
-dontwarn kotlinx.serialization.**
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class **$$serializer { *; }
-keep @kotlinx.serialization.Serializable class * { *; }

############################################
# Hilt / Dagger
############################################
-dontwarn dagger.**
-dontwarn javax.inject.**
-dontwarn javax.annotation.**

# Hilt 내부/생성 코드 유지(릴리즈에서 간헐적으로 죽는 케이스 방지)
-keep class dagger.hilt.internal.** { *; }
-keep class dagger.hilt.android.internal.** { *; }
-keep class dagger.hilt.android.** { *; }

# 생성되는 Factory/Injector 계열이 최적화로 날아가는 케이스 방지
-keep class **_Factory { *; }
-keep class **_MembersInjector { *; }
-keep class **_HiltModules* { *; }
-keep class hilt_aggregated_deps.** { *; }

############################################
# Glide
############################################
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public class * extends com.bumptech.glide.module.LibraryGlideModule
-dontwarn com.bumptech.glide.**

############################################
# Firebase (대부분 불필요하지만 경고/빌드 실패 방지용)
############################################
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

############################################
# Timber (릴리즈에서 로그 제거 원하면)
############################################
-assumenosideeffects class timber.log.Timber {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

############################################
# (선택) androidx / lifecycle 관련 경고 억제
############################################
-dontwarn androidx.**

# Moshi가 쓰는 DTO들은 난독화/최적화에서 제외(필드/생성자 이름 유지)
-keep class com.coffeeinjection.message.data.remote.dto.** { *; }
-keepclassmembers class com.coffeeinjection.message.data.remote.dto.** { *; }