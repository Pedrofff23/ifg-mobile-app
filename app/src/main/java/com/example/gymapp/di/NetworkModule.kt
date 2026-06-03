package com.example.gymapp.di

import com.example.gymapp.data.remote.AuthInterceptor
import com.example.gymapp.data.remote.AuthService
import com.example.gymapp.data.remote.ErpService
import com.example.gymapp.data.remote.GroupService
import com.example.gymapp.data.remote.ProfileService
import com.example.gymapp.data.remote.UserService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

 private const val BASE_URL = "http://192.168.240.1:8080/"

 // AuthInterceptor is now @Inject-constructed by Hilt (TokenManager + Lazy<AuthService>)

 @Provides
 @Singleton
 fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
 return OkHttpClient.Builder()
 .addInterceptor(authInterceptor)
 .connectTimeout(30, TimeUnit.SECONDS)
 .readTimeout(30, TimeUnit.SECONDS)
 .build()
 }

 @Provides
 @Singleton
 fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
 return Retrofit.Builder()
 .baseUrl(BASE_URL)
 .client(okHttpClient)
 .addConverterFactory(GsonConverterFactory.create())
 .build()
 }

 @Provides
 @Singleton
 fun provideAuthService(retrofit: Retrofit): AuthService {
 return retrofit.create(AuthService::class.java)
 }

 @Provides
 @Singleton
 fun provideErpService(retrofit: Retrofit): ErpService {
 return retrofit.create(ErpService::class.java)
 }

 @Provides
 @Singleton
 fun provideUserService(retrofit: Retrofit): UserService {
 return retrofit.create(UserService::class.java)
 }

 @Provides
 @Singleton
 fun provideProfileService(retrofit: Retrofit): ProfileService {
 return retrofit.create(ProfileService::class.java)
 }

 @Provides
 @Singleton
 fun provideGroupService(retrofit: Retrofit): GroupService {
 return retrofit.create(GroupService::class.java)
 }
}
