package com.example.todoapp.ioc.components

import android.app.Application
import android.content.Context
import com.example.todoapp.MyApp
import com.example.todoapp.data.TodoItemDAO
import com.example.todoapp.data.TodoItemsRepository
import com.example.todoapp.ioc.modules.AppModule
import com.example.todoapp.ioc.modules.MyDatabaseModule
import com.example.todoapp.ioc.modules.RetrofitModule
import com.example.todoapp.ioc.modules.RevisionModule
import com.example.todoapp.ioc.scopes.AppModuleScope
import com.example.todoapp.ioc.scopes.MyDatabaseModuleScope
import com.example.todoapp.ioc.scopes.RetrofitModuleScope
import com.example.todoapp.ioc.scopes.RevisionModuleScope
import com.example.todoapp.ioc.util.MutableString
import com.example.todoapp.network.APIService
import com.example.todoapp.network.workers.OnInternetConnectionWorker
import dagger.BindsInstance
import dagger.Component
import dagger.Module

@MyDatabaseModuleScope
@AppModuleScope
@RetrofitModuleScope
@RevisionModuleScope
@Component(modules = [MyDatabaseModule::class, AppModule::class, RetrofitModule::class, RevisionModule::class])
interface AppComponent {
    fun inject(myApp: MyApp)

    fun todoItemsFragmentComponent(): TodoItemsFragmentComponent
    fun addItemFragmentComponent(): AddItemFragmentComponent

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance application: Application, appModule: AppModule): AppComponent
    }
}