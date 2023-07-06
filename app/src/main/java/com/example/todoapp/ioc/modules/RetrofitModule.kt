package com.example.todoapp.ioc.modules

import com.example.todoapp.fragments.util.Const.URL_API
import com.example.todoapp.ioc.scopes.RetrofitModuleScope
import com.example.todoapp.network.APIService
import com.example.todoapp.network.util.LocalDateLongConverter
import com.example.todoapp.network.util.LocalDateTimeLongConverter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.time.LocalDateTime

@Module
class RetrofitModule {

    @Provides
    @RetrofitModuleScope
    fun provideGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeLongConverter())
            .registerTypeAdapter(LocalDate::class.java, LocalDateLongConverter())
            .create()
    }

    @Provides
    @RetrofitModuleScope
    fun provideRetrofit(gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(URL_API)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @RetrofitModuleScope
    fun provideApiService(retrofit: Retrofit): APIService {
        return retrofit.create(APIService::class.java)
    }
}