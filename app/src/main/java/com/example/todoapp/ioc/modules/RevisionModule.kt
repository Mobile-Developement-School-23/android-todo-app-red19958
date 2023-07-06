package com.example.todoapp.ioc.modules

import com.example.todoapp.fragments.util.Const.ZERO
import com.example.todoapp.ioc.scopes.RevisionModuleScope
import com.example.todoapp.ioc.util.MutableString
import dagger.Module
import dagger.Provides
import javax.inject.Provider


@Module
class RevisionModule {
    @Provides
    @RevisionModuleScope
    fun provideString(): MutableString {
        return MutableString()
    }
}