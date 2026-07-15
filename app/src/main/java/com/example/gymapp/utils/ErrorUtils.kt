package com.example.gymapp.utils

import android.util.Log
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

object ErrorUtils {
 private const val TAG = "GymApp/Error"

 /**
 * Tries to parse the backend error response and returns a user-friendly Portuguese message.
 * Also logs the full error for debugging.
 */
  fun parseErrorMessage(e: Exception, fallback: String = "Ocorreu um erro inesperado"): String {
  Log.e(TAG, "Exception caught: ${e.javaClass.simpleName}", e)
  return when (e) {
  is HttpException -> {
  val errorBody = e.response()?.errorBody()?.string()
  Log.e(TAG, "HTTP ${e.code()} — errorBody: $errorBody")
  try {
  if (!errorBody.isNullOrBlank()) {
  val jsonObject = JSONObject(errorBody)
  val messageToTranslate = when {
      jsonObject.has("error_description") -> jsonObject.getString("error_description")
      jsonObject.has("error_code") -> jsonObject.getString("error_code")
      jsonObject.has("error") -> {
          val errorVal = jsonObject.get("error")
          if (errorVal is JSONObject) errorVal.optString("message", errorVal.toString())
          else errorVal.toString()
      }
      jsonObject.has("msg") -> jsonObject.getString("msg")
      jsonObject.has("message") -> jsonObject.getString("message")
      else -> null
  }
  if (messageToTranslate != null) {
      return translateCommonBackendErrors(messageToTranslate)
  }
  }
  // Fallback to HTTP Status code translation if no specific message
  translateHttpCode(e.code())
  } catch (jsonException: Exception) {
  translateHttpCode(e.code())
  }
  }
  is IOException -> "Erro de conexão: Verifique sua internet."
  is IllegalStateException -> {
      val msg = e.message ?: ""
      if (msg.contains("Expected BEGIN_OBJECT") || msg.contains("path $.data")) {
          "Erro ao processar dados do servidor."
      } else {
          msg.ifBlank { fallback }
      }
  }
  else -> {
      val msg = e.message ?: ""
      if (msg.contains("JsonSyntaxException") || msg.contains("Expected BEGIN_OBJECT")) {
          "Erro ao processar dados do servidor."
      } else {
          msg.ifBlank { fallback }
      }
  }
  }
  }

    private fun translateHttpCode(code: Int): String {
        return when (code) {
            400 -> "E-mail ou senha incorretos, ou dados inválidos."
            401 -> "Sessão expirada. Faça login novamente."
            403 -> "Você não tem permissão para realizar esta ação."
            404 -> "Recurso não encontrado."
            500 -> "Erro interno no servidor. Tente novamente mais tarde."
            else -> "Ocorreu um erro na requisição (Código $code)."
        }
    }


    private fun translateCommonBackendErrors(backendMessage: String): String {
        val lowerMsg = backendMessage.lowercase()
        
        // Auth / Tokens
        if (lowerMsg.contains("invalid or expired token")) return "Seu acesso expirou, faça login novamente."
        if (lowerMsg.contains("missing authorization token")) return "Token de acesso ausente."
        if (lowerMsg.contains("invalid token")) return "Token de acesso inválido."
        
        // Validation / Auth errors
        if (lowerMsg.contains("invalid credentials") || lowerMsg.contains("invalid_grant") || lowerMsg.contains("invalid login") || lowerMsg.contains("invalid_credentials")) 
            return "Email ou senha incorretos."
        if (lowerMsg.contains("email already registered") || lowerMsg.contains("user already exists")) 
            return "Este email já está em uso."
        if (lowerMsg.contains("user not found")) return "Usuário não encontrado."
        if (lowerMsg.contains("email not confirmed") || lowerMsg.contains("confirm your email") || lowerMsg.contains("email_not_confirmed"))
            return "EMAIL_NOT_CONFIRMED"
        if (lowerMsg.contains("blocked") || lowerMsg.contains("account is blocked"))
            return "ACCOUNT_BLOCKED"
        
        // Templates / Exercises
        if (lowerMsg.contains("invalid exercise id")) return "Exercício inválido selecionado."
        if (lowerMsg.contains("invalid user id")) return "Usuário inválido."
        if (lowerMsg.contains("failed to upload file")) return "Não foi possível enviar o arquivo de mídia."
        if (lowerMsg.contains("cannot view another user")) return "Você não tem permissão para visualizar estes dados."
        
        // General Errors
        if (lowerMsg.contains("an unexpected error occurred") || lowerMsg.contains("internal_error"))
            return "Ocorreu um erro interno no servidor. Tente novamente mais tarde."
        
        // Return original if no common mapping fits (or just fallback to something generic, but seeing the original is better for debugging)
        return backendMessage
    }
}
