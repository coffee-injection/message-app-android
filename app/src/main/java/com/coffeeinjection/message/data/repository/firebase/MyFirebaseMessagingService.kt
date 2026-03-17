package com.coffeeinjection.message.data.repository.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.coffeeinjection.message.R
import com.coffeeinjection.message.data.local.AuthDataStore
import com.coffeeinjection.message.data.model.PushBus
import com.coffeeinjection.message.data.model.PushEvent
import com.coffeeinjection.message.presentation.activity.MainActivity
import com.coffeeinjection.message.util.AppState
import com.coffeeinjection.message.util.Logger
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject lateinit var authDataStore: AuthDataStore
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "MyFirebaseMessagingService"
        private const val DEFAULT_CHANNEL_ID = "default_channel" // 고정 ID
    }

    /**
     * FCM 토큰이 새로 발급/갱신될 때만 호출됨.
     * 여기서는 "저장만" 수행하고, 실제 서버 등록은 로그인 완료 후 별도 로직에서 처리.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Logger.d(TAG, "onNewToken: $token")

        scope.launch {
            runCatching { authDataStore.saveFcmToken(token) }
                .onSuccess { Logger.d(TAG, "Saved FCM token locally") }
                .onFailure { e -> Logger.e(TAG, "Failed to save FCM token: ${e.message}") }
        }
        // 서버 전송 금지: 로그인 후 ViewModel/UseCase에서 등록
    }

    /**
     * 포그라운드에서 메시지 수신 시 호출
     * (백그라운드 + notification payload만 있을 땐 시스템이 자동 표시)
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Logger.d(TAG, "From: ${remoteMessage.from}")
        Logger.d(TAG, "Data Payload: ${remoteMessage.data}")
        Logger.d(TAG, "Notification Payload: ${remoteMessage.notification}")

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: getString(R.string.app_name)

        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: ""

        val link = remoteMessage.notification?.link
            ?: remoteMessage.data["url"]
            ?: extractUrlFromBody(body)

        Logger.d(TAG, "Notification data: title=$title, body=$body, link=$link")

        val event = PushEvent(
            title = remoteMessage.data["title"],
            body = remoteMessage.data["body"]
        )

        if (AppState.isForeground) {
            PushBus.message.postValue(event)
            sendNotification(title, body, link?.toString())

        } else {
            sendNotification(title, body, link?.toString())
        }
    }

    // body에서 URL 추출 (http/https 모두)
    private fun extractUrlFromBody(body: String): String? {
        val urlPattern = Pattern.compile("https?://[\\w\\-./?=&%]+")
        val matcher = urlPattern.matcher(body)
        return if (matcher.find()) matcher.group(0) else null
    }

    // 알림 생성 및 표시
    private fun sendNotification(title: String, body: String, url: String? = null) {
        if (title.isBlank() || body.isBlank()) {
            Logger.i(TAG, "Empty title or body, skipping notification")
            return
        }

        val uniId = (System.currentTimeMillis() / 1000).toInt()

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

            // 푸시 탭 시 홈으로 시작하도록 강제
            putExtra("startDestination", "home")

            if (!url.isNullOrBlank()) putExtra("fcmLink", url)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, uniId, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 채널 준비 (O 이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = getString(R.string.default_notification_channel_id)
            val channel = NotificationChannel(
                DEFAULT_CHANNEL_ID,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, DEFAULT_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body)) // 긴 본문 대응
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(uniId, notification)
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
