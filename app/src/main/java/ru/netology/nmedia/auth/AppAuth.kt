package ru.netology.nmedia.auth

import android.content.Context
import androidx.core.content.edit
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dto.PushToken
import ru.netology.nmedia.model.AuthModel
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext
    private val context: Context
) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val idKey = "ID_KEY"
    private val tokenKey = "TOKEN_KEY"
    private val _authStateFlow: MutableStateFlow<AuthModel>

    init {
        val id = prefs.getLong(idKey, 0)
        val token = prefs.getString(tokenKey, null)

        if (id == 0L || token == null) {

            _authStateFlow = MutableStateFlow(AuthModel())
            with(prefs.edit()) {
                clear()
                apply()
            }
        } else {
            _authStateFlow = MutableStateFlow(AuthModel(id, token))
        }
        uploadPushToken()
    }

    val authStateFlow = _authStateFlow.asStateFlow()

    @Synchronized
    fun setUser(user: AuthModel) {
        _authStateFlow.value = user
        prefs.edit {
            putLong(idKey, user.id)
            putString(tokenKey, user.token)
        }
        uploadPushToken()
    }

    @Synchronized
    fun removeUser() {
        _authStateFlow.value = AuthModel()
        prefs.edit { clear() }
        uploadPushToken()
    }


    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AppAuthEntryPoint {
        fun getApiService(): ApiService
    }

    fun uploadPushToken(token: String? = null) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val pushToken = PushToken(token ?: Firebase.messaging.token.await())
                val entryPoint =
                    EntryPointAccessors.fromApplication(context, AppAuthEntryPoint::class.java)
                entryPoint.getApiService().uploadPushToken(pushToken)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

//    companion object {
//        private const val ID_KEY = "ID_KEY"
//        private const val TOKEN_KEY = "TOKEN_KEY"

//        @Volatile
//        private var instance: AppAuth? = null

//        @Synchronized
//        fun initAppAuth(context: Context): AppAuth {
//            return instance ?: AppAuth(context).apply { instance = this }
//        }

//        fun getInstance(): AppAuth = requireNotNull(instance) { "init AppAuth was not invoke" }
//
//
//    }

}
