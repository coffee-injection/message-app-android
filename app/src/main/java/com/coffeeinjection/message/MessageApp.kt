package com.coffeeinjection.message

import android.app.Application
import com.coffeeinjection.message.util.ReleaseTree
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * 앱 전역에서 사용할 로깅을 초기화하는 Application 클래스.
 * - Debug: 상세 로그(파일/라인/메서드 포함)
 * - Release: WARN 이상만 출력(ReleaseTree)
 *
 * AndroidManifest.xml 의 <application android:name=".MessageApp" /> 로 등록 필수.
 */
@HiltAndroidApp
class MessageApp:Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            // 디버그 빌드: 보기 좋은 태그(파일/라인/메서드)
            Timber.plant(object : Timber.DebugTree() {
                override fun createStackElementTag(element: StackTraceElement): String? {
                    return String.format(
                        "Class:%s: Line: %s, Method: %s",
                        super.createStackElementTag(element),
                        element.lineNumber,
                        element.methodName
                    )
                }
            })
        } else {
            // 릴리스 빌드: 릴리스 정책(민감정보/과도한 로그 차단)
            Timber.plant(ReleaseTree())
        }
    }
}