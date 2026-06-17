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
 if (jsonObject.has("error")) {
 val errorVal = jsonObject.get("error")
 val message = if (errorVal is JSONObject) {
 errorVal.optString("message", errorVal.toString())
 } else {
 errorVal.toString()
 }
 return translateCommonBackendErrors(message)
 } else if (jsonObject.has("message")) {
 return translateCommonBackendErrors(jsonObject.getString("message"))
 }
 }
 // Fallback to HTTP Status code translation if no specific message
 "HTTP ${e.code()}: " + translateHttpCode(e.code())
 } catch (jsonException: Exception) {
 "HTTP ${e.code()}: " + translateHttpCode(e.code())
 }
 }
 is IOException -> "Erro de conexão (${e.javaClass.simpleName}): Verifique sua internet."
 else -> e.message ?: (e.javaClass.simpleName + ": " + fallback)
 }
 }

    private fun translateHttpCode(code: Int): String {
        return when (code) {
            400 -> "Dados inválidos ou incompletos."
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
        if (lowerMsg.contains("invalid credentials") || lowerMsg.contains("invalid_grant") || lowerMsg.contains("invalid login")) 
            return "Email ou senha incorretos."
        if (lowerMsg.contains("email already registered") || lowerMsg.contains("user already exists")) 
            return "Este email já está em uso."
        if (lowerMsg.contains("user not found")) return "Usuário não encontrado."
        if (lowerMsg.contains("email not confirmed") || lowerMsg.contains("confirm your email"))
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
