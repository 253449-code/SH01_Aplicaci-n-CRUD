package com.upchiapas.kt_template

import android.app.Application
import com.upchiapas.kt_template.di.AppContainer
import com.upchiapas.kt_template.di.DefaultAppContainer

class KtApplication : Application() {
    val appContainer: AppContainer by lazy { DefaultAppContainer(this) }
}
