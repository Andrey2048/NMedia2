package ru.netology.nmedia.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.PermissionChecker
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {
    private val action = "action"
    private val content = "content"
    private val channelId = "remote"
    private val gson = Gson()

    @Inject
    lateinit var appAuth: AppAuth
    override fun onCreate() {
        super.onCreate()

        val name = getString(R.string.channel_remote_name)
        val descriptionText = getString(R.string.channel_remote_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

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
        val myId = appAuth.authStateFlow.value.id
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
                appAuth.uploadPushToken()
            }

            testInputPushValue.recipientId != 0L -> {
                println("сервер считает, что у на нашем устройстве другая аутентификация, переотправляем токен")
                appAuth.uploadPushToken()
            }
        }


    }

    override fun onNewToken(token: String) {
        appAuth.uploadPushToken(token)
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
