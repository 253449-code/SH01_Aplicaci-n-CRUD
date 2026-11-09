package com.upchiapas.kt_template.auth.data.datasource

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.upchiapas.kt_template.auth.domain.model.AuthSession
import com.upchiapas.kt_template.auth.domain.model.User
import com.upchiapas.kt_template.auth.domain.repository.SessionStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "focustask_session")

class DataStoreSessionStorage(
    private val dataStore: DataStore<Preferences>
) : SessionStorage {

    override val sessionFlow: Flow<AuthSession?> = dataStore.data.map { prefs ->
        val token = prefs[TOKEN_KEY]
        val userId = prefs[USER_ID_KEY]
        val userName = prefs[USER_NAME_KEY]
        val userEmail = prefs[USER_EMAIL_KEY]

        if (!token.isNullOrBlank() && userId != null && userName != null && userEmail != null) {
            AuthSession(
                token = token,
                user = User(
                    id = userId,
                    name = userName,
                    email = userEmail
                )
            )
        } else {
            null
        }
    }

    override suspend fun getToken(): String? {
        return dataStore.data.first()[TOKEN_KEY]
    }

    override suspend fun saveSession(session: AuthSession) {
        dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = session.token
            prefs[USER_ID_KEY] = session.user.id
            prefs[USER_NAME_KEY] = session.user.name
            prefs[USER_EMAIL_KEY] = session.user.email
        }
    }

    override suspend fun clearSession() {
        dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
            prefs.remove(USER_ID_KEY)
            prefs.remove(USER_NAME_KEY)
            prefs.remove(USER_EMAIL_KEY)
        }
    }

    private companion object {
        val TOKEN_KEY = stringPreferencesKey("auth_token")
        val USER_ID_KEY = intPreferencesKey("user_id")
        val USER_NAME_KEY = stringPreferencesKey("user_name")
        val USER_EMAIL_KEY = stringPreferencesKey("user_email")
    }
}
