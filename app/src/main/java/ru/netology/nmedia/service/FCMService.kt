package ru.netology.nmedia.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.PermissionChecker
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import kotlin.random.Random


class FCMService : FirebaseMessagingService() {
    private val action = "action"
    private val content = "content"
    private val channelId = "remote"
    private val gson = Gson()

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_remote_name)
            val descriptionText = getString(R.string.channel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        println(message.data)
        message.data[action]?.let {
           when (Action.valueOf(it)) {
              Action.LIKE -> handleLike(gson.fromJson(message.data[content], Like::class.java))
           }
        }

        val testInputPushValue = gson.fromJson(message.data[content], RecipientIdCheck::class.java)
        println(testInputPushValue)
        val myId = AppAuth.getInstance().authStateFlow.value.id
        println("myId: $myId")
        println("recipientId: ${testInputPushValue.recipientId}")
        when {
            testInputPushValue.recipientId == myId -> {
                handleTestAction(testInputPushValue, "Персональная рассылка")
            }

            testInputPushValue.recipientId == null -> {
                handleTestAction(testInputPushValue, "Массовая рассылка")
            }

            testInputPushValue.recipientId == 0L -> {
                println("сервер считает, что у нас анонимная аутентификация, переотправляем токен")
                AppAuth.getInstance().uploadPushToken()
            }

            testInputPushValue.recipientId != 0L -> {
                println("сервер считает, что у на нашем устройстве другая аутентификация, переотправляем токен")
                AppAuth.getInstance().uploadPushToken()
            }
        }


    }

    override fun onNewToken(token: String) {
        AppAuth.getInstance().uploadPushToken(token)
    }

    private fun handleLike(content: Like) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(
                getString(
                    R.string.notification_user_liked,
                    content.userName,
                    content.postAuthor,
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        if (PermissionChecker.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PermissionChecker.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(this)
                .notify(Random.nextInt(100_000), notification)
        }
    }


    @SuppressLint("MissingPermission")
    private fun handleTestAction(content: RecipientIdCheck, message: String) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(content.content)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(this)
            .notify(Random.nextInt(100_000), notification)
    }
}

enum class Action {
    LIKE,
}

data class Like(
    val userId: Long,
    val userName: String,
    val postId: Long,
    val postAuthor: String,
)

data class RecipientIdCheck(
    val recipientId: Long? = null,
    val content: String,
)
