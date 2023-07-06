package com.example.todoapp.network

import com.example.todoapp.fragments.util.Const.AUTHORIZATION
import com.example.todoapp.fragments.util.Const.LAST_KNOWN_REVISION
import com.example.todoapp.fragments.util.Const.LIST
import com.example.todoapp.fragments.util.Const.LIST_ID
import com.example.todoapp.fragments.util.Const.SMALL_ID
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface APIService {

    @Headers(AUTHORIZATION)
    @GET(LIST)
    suspend fun downloadTodoItems(): Response<ListResponseParams>

    @Headers(AUTHORIZATION)
    @POST(LIST)
    suspend fun makeTodoItem(
        @Body request: ListRequestParams,
        @Header(LAST_KNOWN_REVISION) revision: String
    ): Response<ElementResponseParams>

    @Headers(AUTHORIZATION)
    @GET(LIST_ID)
    suspend fun downloadTodoItem(
        @Path(SMALL_ID) id: String
    ): Response<ElementResponseParams>

    @Headers(AUTHORIZATION)
    @PUT(LIST_ID)
    suspend fun changeTodoItemById(
        @Body item:ListRequestParams,
        @Path(SMALL_ID) id: String,
        @Header(LAST_KNOWN_REVISION) revision: String
    ): Response<ElementResponseParams>

    @Headers(AUTHORIZATION)
    @DELETE(LIST_ID)
    suspend fun deleteTodoItem(
        @Path(SMALL_ID) id: String,
        @Header(LAST_KNOWN_REVISION) revision: String
    ): Response<ElementResponseParams>

    @Headers(AUTHORIZATION)
    @PATCH(LIST)
    suspend fun updateServerList(
        @Body newList: ListResponseParams,
        @Header(LAST_KNOWN_REVISION) revision: String
    ): Response<ListResponseParams>
}