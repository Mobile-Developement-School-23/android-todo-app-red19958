package com.example.todoapp.ioc.modules

import android.app.Application
import android.content.Context
import com.example.todoapp.ioc.scopes.AppModuleScope
import dagger.Module
import dagger.Provides
import javax.inject.Scope
import javax.inject.Singleton

@Module
class AppModule(private val application: Application) {

    @Provides
    @AppModuleScope
    fun provideApplicationContext(): Context {
        return application.applicationContext
    }
}