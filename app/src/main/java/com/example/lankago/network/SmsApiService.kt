package com.example.lankago.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path


  //Standard SMS Gateway Response Model compatible with Twilio and REST SMS providers.

data class SmsResponse(
    @SerializedName("sid")
    val sid: String? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("to")
    val to: String? = null,

    @SerializedName("from")
    val from: String? = null,

    @SerializedName("body")
    val body: String? = null,

    @SerializedName("date_created")
    val dateCreated: String? = null,

    @SerializedName("error_message")
    val errorMessage: String? = null,

    @SerializedName("error_code")
    val errorCode: Int? = null
)


  //Retrofit API interface for SMS Gateways (Twilio / Standard REST SMS).

interface SmsApiService {


      //Dispatches an SMS via Twilio Programmable Messaging HTTP POST.

    @FormUrlEncoded
    @POST("2010-04-01/Accounts/{accountSid}/Messages.json")
    suspend fun sendTwilioSms(
        @Path("accountSid") accountSid: String,
        @Header("Authorization") authHeader: String,
        @Field("To") to: String,
        @Field("From") from: String,
        @Field("Body") body: String
    ): Response<SmsResponse>


      //Dispatches an SMS via a standard generic SMS REST Gateway.

    @FormUrlEncoded
    @POST("api/v1/sms/send")
    suspend fun sendGenericSms(
        @Field("to") to: String,
        @Field("from") from: String,
        @Field("message") message: String
    ): Response<SmsResponse>
}
