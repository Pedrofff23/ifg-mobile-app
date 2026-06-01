# Antigravity ERP - App (Android / Jetpack Compose)

Este é o aplicativo Android do sistema Antigravity ERP, voltado tanto para **Alunos** quanto para **Professores/Admins**.

## 🚀 Arquitetura e Stack

- **Linguagem:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Injeção de Dependência:** Dagger Hilt
- **Network / API:** Retrofit + OkHttp + Gson
- **Carregamento de Imagens:** Coil (com suporte a GIF)
- **Navegação:** Jetpack Navigation Compose
- **Armazenamento Local:** DataStore Preferences (Tema, Sessão, Tokens)
- **Arquitetura:** MVVM (Model-View-ViewModel)

## 📁 Estrutura de Diretórios (`app/src/main/java/com/example/gymapp/`)

- `di/`: Módulos de injeção de dependência do Hilt (Configuração do Retrofit, DataStore).
- `data/`: 
  - `remote/`: Serviços do Retrofit (AuthService, ErpService, etc) e Interceptors.
  - `local/`: Gerenciamento do DataStore local.
- `domain/`: Modelos de Dados (DTOs, Entidades) como `ErpModels.kt`.
- `presentation/`: Interfaces de Usuário em Compose (Telas separadas por `student`, `trainer`, `auth`, etc) e seus respectivos ViewModels.
- `ui/theme/`: Definições globais de Design System, cores flexíveis, tipografia e temas.

## ⚠️ AVISOS PARA PRODUÇÃO (CRÍTICO)

No ambiente de **desenvolvimento**, o aplicativo consome APIs a partir do IP local da máquina via rede Wi-Fi (ex: `192.168.240.1:8080`).

Ao compilar o aplicativo para **Produção (Release)**, você precisará alterar as seguintes chaves e configurações **ANTES** de gerar o `.apk` / `.aab`:

1. **Alterar a Base URL da API (Retrofit):**
   Vá até o arquivo `app/src/main/java/com/example/gymapp/di/NetworkModule.kt` e altere a constante `BASE_URL`:
   ```kotlin
   // De:
   private const val BASE_URL = "http://192.168.240.1:8080/"
   // Para a URL de produção do seu backend Go (Exemplo):
   private const val BASE_URL = "https://api.seugym.com.br/"
   ```

2. **Alterar a Base URL do Supabase Storage:**
   Ao exibir imagens, GIFs ou vídeos nas telas de exercícios (`StudentExercisesScreen` e `ManageExercisesScreen`), o app formata o caminho da mídia local anexando à URL base do Supabase local (ex: `http://192.168.240.1:8000/storage/...`).
   **Você deve** alterar essa constante (geralmente injetada nos ViewModels ou configurada globalmente) para apontar para o host oficial de produção do seu Supabase.

3. **Configuração de Proguard/Minify:**
   No arquivo `app/build.gradle.kts`, a chave `isMinifyEnabled` geralmente fica como `false` durante o desenvolvimento. Para release na Google Play, certifique-se de configurar a assinatura do APK, ajustar as regras no arquivo `proguard-rules.pro` (ex: para o Retrofit/Gson) e ativar a ofuscação de código se desejar.

4. **Permissões do AndroidManifest:**
   Atualmente, o app requer acesso à internet (`INTERNET`). Se no futuro ele fizer o download real dos vídeos ou precisar do status de rede para offline cache, adicione `ACCESS_NETWORK_STATE`.
