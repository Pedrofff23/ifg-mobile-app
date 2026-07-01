# IFG Gym App — Academia IFG Anápolis

Aplicativo Android nativo (Kotlin + Jetpack Compose) para gestão de academia: treinos, sessões, progresso, medições, avisos e administração. Backend em Go + PostgreSQL (Supabase).

## Visão Geral

| Perfil | Funcionalidades |
|--------|-----------------|
| **Aluno** | Ver treinos atribuídos, executar sessões offline-first (séries/reps/peso/duração), histórico de carga por exercício, registrar peso corporal com gráfico, receber avisos/notícias/instruções via push |
| **Professor/Admin** | CRUD exercícios (com mídia), templates multi-dia ("Treino A/B"), atribuir a alunos/grupos, gerenciar usuários (role/status/block), publicar anúncios com push FCM |

## Stack Tecnológico

| Camada | Tecnologia |
|--------|------------|
| Linguagem | Kotlin 2.2.10 |
| UI | Jetpack Compose Material 3 (BOM 2024.02.01) |
| DI | Hilt 2.59.2 |
| Rede | Retrofit 2.9.0 + OkHttp |
| BD Local | Room 2.7.0-alpha01 (offline-first sessões) |
| Preferences | DataStore Preferences 1.1.1 |
| Navegação | Navigation Compose 2.7.7 |
| Imagens | Coil 2.6.0 (GIF support) |
| Push | Firebase Cloud Messaging 33.0.0 |
| Build | AGP 9.2.1, KSP 2.0.0-1.0.21 |
| Testes | JUnit 4.13.2, Mockito 5.11.0 |

## Requisitos

- **JDK 17** (Temurin/OpenJDK)
- **Android SDK 36** (compileSdk/targetSdk)
- **Dispositivo/Emulador** Android 7.0+ (API 24)
- **Backend Go** rodando acessível na rede (`http://192.168.240.1:8080/healthz`)
- **Supabase Storage** acessível (`http://192.168.240.1:8000/storage/...`)

## Instalação

```bash
# 1. Entrar no diretório do app
cd ~/Documents/TCC/app

# 2. Configurar o ambiente local usando o arquivo de exemplo
cp local.properties.example local.properties

# 3. Ajuste o caminho do seu Android SDK e as URLs do backend no local.properties recém-criado:
# (Abra o arquivo e altere conforme necessário)

# 4. Verificar conexão com o backend local
curl http://192.168.240.1:8080/healthz
# {"status":"ok"}

# 5. Build e install debug
make debug
# ou: ./gradlew installDebug
```

## Configuração e Variáveis de Ambiente

O aplicativo utiliza leitura dinâmica para configurar as variáveis de endpoint e armazenamento através do Gradle.

### 1. Desenvolvimento Local (`local.properties`)
Para desenvolvimento local, crie ou edite o arquivo `local.properties` na raiz do módulo `app/` (que é ignorado pelo Git) e defina as seguintes chaves:

```properties
sdk.dir=/home/usuario/Android/Sdk
BASE_URL=http://10.0.2.2:8080/api/v1/
SUPABASE_URL=http://10.0.2.2:8000/storage/v1/object/public/exercises/
```
*Dica: use `10.0.2.2` para o emulador Android nativo ou o IP da sua máquina na mesma rede Wi-Fi para dispositivos físicos.*

### 2. Configuração de Produção
Para subir para produção de maneira correta e segura, siga as diretrizes abaixo:

#### A. Injeção de URLs de Produção via Variáveis de Ambiente (CI/CD ou terminal)
Ao invés de deixar URLs de produção em arquivos locais, defina-as como variáveis de ambiente no seu sistema ou pipeline de build antes de compilar o release:

```bash
export BASE_URL="https://api.seudominio.com/api/v1/"
export SUPABASE_URL="https://storage.seudominio.com/storage/v1/object/public/exercises/"
export VERSION_CODE=1
export VERSION_NAME="1.0.0"

# Executar build de produção
make release
```

#### B. Tráfego Seguro (HTTPS) em Produção
Por padrão, o Android impede conexões HTTP não seguras (sem SSL/TLS). No ambiente de produção, certifique-se de desabilitar o tráfego Cleartext.
1. O backend em produção **deve usar HTTPS** obrigatoriamente.
2. Para desabilitar o tráfego inseguro em compilações de produção mantendo a facilidade no desenvolvimento local, a melhor abordagem é manter `android:usesCleartextTraffic="false"` (ou não declará-lo) no `app/src/main/AndroidManifest.xml` principal, e criar um arquivo `AndroidManifest.xml` específico para o flavor/build de desenvolvimento em `app/src/debug/AndroidManifest.xml` contendo:
   ```xml
   <?xml version="1.0" encoding="utf-8"?>
   <manifest xmlns:android="http://schemas.android.com/apk/res/android">
       <application android:usesCleartextTraffic="true" />
   </manifest>
   ```
   Dessa forma, o Gradle fará o merge automático habilitando tráfego claro apenas em compilações de Debug.

### Firebase (Push Notifications)
1. Criar projeto no [Firebase Console](https://console.firebase.google.com)
2. Adicionar app Android (package: `com.example.gymapp`)
3. Baixar `google-services.json` → colocar em `app/`
4. No backend: definir `FCM_SERVER_KEY` e `FCM_PROJECT_ID` no `.env`

### Release Signing (Obrigatório para AAB)
```bash
# 1. Gerar keystore (guarde com segurança!)
keytool -genkey -v -keystore gymapp-release.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias gymapp-key

# 2. Adicionar em app/build.gradle.kts (android { ... })
signingConfigs {
    create("release") {
        val storeFile = System.getenv("KEYSTORE_PATH")?.let { file(it) } ?: file("gymapp-release.jks")
        val storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "change-me"
        val keyAlias = System.getenv("KEY_ALIAS") ?: "gymapp-key"
        val keyPassword = System.getenv("KEY_PASSWORD") ?: "change-me"
        
        this.storeFile = storeFile
        this.storePassword = storePassword
        this.keyAlias = keyAlias
        this.keyPassword = keyPassword
    }
}
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        signingConfig = signingConfigs.getByName("release")
    }
}
```

## Execução

| Comando | Descrição |
|---------|-----------|
| `make debug` | Build + install debug APK no device conectado |
| `make build` | Gera `app/build/outputs/apk/debug/app-debug.apk` |
| `make release` | Gera `app/build/outputs/bundle/release/app-release.aab` (requer signingConfig) |
| `make install` | Alias para `make debug` |
| `make clean` | Limpa artefatos de build |
| `make lint` | Análise estática (Lint) |
| `make test` | Testes unitários (JVM) |
| `make test-ui` | Testes instrumentados (requer device/emulator) |
| `make all-checks` | `lint + test + build` |

## Debug

```bash
# Logs de erro (tag customizada)
adb logcat -s "GymApp/Error"

# FCM service
adb logcat -s "FCMService"

# Auth flow
adb logcat -s "AuthViewModel"

# Todos logs do app
adb logcat | grep -i gymapp

# Room database (shell)
adb shell
run-as com.example.gymapp
sqlite3 databases/gym_app_db ".tables"
```

**Breakpoints sugeridos:**
- `AuthInterceptor.kt:30` — JWT attach + auto-refresh
- `AuthViewModel.kt:90` — Login flow
- `WorkoutSessionRepository.kt:40` — Offline/online session start
- `SyncManager.kt:20` — Auto-sync trigger
- `GymFirebaseMessagingService.kt:50` — Push received

## 🏗 Build

```bash
# Debug APK (desenvolvimento)
make build
# Saída: app/build/outputs/apk/debug/app-debug.apk

# Release AAB (Google Play)
make release
# Saída: app/build/outputs/bundle/release/app-release.aab
# ⚠️ Requer signingConfig configurado
```

### Configurações de Build
- `minSdk = 24`, `targetSdk = 36`, `compileSdk = 36`
- `versionCode` e `versionName` via variáveis de ambiente (`VERSION_CODE`, `VERSION_NAME`)
- ProGuard/R8 habilitado apenas em release (`isMinifyEnabled = true`)
- Kotlin JVM toolchain 17

## Estrutura do Projeto

```
app/src/main/java/com/example/gymapp/
├── GymApplication.kt              # Hilt entry point + NotificationChannel
├── MainActivity.kt                # ComposeView edge-to-edge + deep-link handling
├── data/
│   ├── local/
│   │   ├── TokenManager.kt        # DataStore: tokens + user data (Flow)
│   │   ├── Entities.kt            # 5 Room entities (sessões, exercícios, sets, sync, progresso)
│   │   ├── AnnouncementEntity.kt  # Room entity para cache offline de anúncios
│   │   ├── AnnouncementDao.kt     # DAO para anúncios
│   │   ├── Daos.kt                # WorkoutSessionDao + PendingSyncDao
│   │   ├── GymDatabase.kt         # RoomDatabase v3
│   │   └── notification/
│   │       └── GymFirebaseMessagingService.kt  # FCM @AndroidEntryPoint
│   ├── remote/
│   │   ├── AuthService.kt         # Auth endpoints
│   │   ├── ErpService.kt          # Domínio: exercises, templates, assignments, sessions, announcements
│   │   ├── GroupService.kt        # Groups + members
│   │   ├── ProfileService.kt      # Profile + measurements
│   │   ├── UserService.kt         # Admin users
│   │   ├── AuthInterceptor.kt     # JWT + auto-refresh
│   │   └── ApiResponse.kt         # Wrappers genéricos
│   └── repository/
│       ├── WorkoutSessionRepository.kt  # Offline-first (Room + sync queue)
│       ├── AnnouncementRepository.kt    # Offline-first para anúncios
│       └── SyncManager.kt             # Auto-sync on network change
├── di/
│   ├── NetworkModule.kt           # OkHttp, Retrofit, Services
│   ├── DatabaseModule.kt          # Room DAOs
│   └── RepositoryModule.kt        # Repository bindings
├── domain/model/
│   ├── ApiResponse.kt             # PaginatedResponse, ApiResponse
│   ├── AuthModels.kt              # Login, Register, User, Me
│   ├── ErpModels.kt               # 267 linhas: todos modelos de domínio
│   └── RequestDTOs.kt             # Create/Update DTOs
├── presentation/
│   ├── auth/                      # Login, Register, CompleteProfile, ActivationPending, Blocked + AuthViewModel
│   ├── navigation/AppNavigation.kt # NavHost + rotas + SplashScreen
│   ├── student/                   # 12 telas + StudentViewModel + WorkoutSessionViewModel
│   └── trainer/                   # 13 telas + ProfessorViewModel (1016 linhas)
├── ui/theme/
│   ├── Color.kt                   # Cores IFG + 6 paletas (3 light + 3 dark)
│   ├── Theme.kt                   # MaterialTheme customizado
│   ├── ThemeManager.kt            # Persistência tema no DataStore
│   ├── ThemeComponents.kt         # Componentes reutilizáveis de seleção
│   └── Type.kt                    # Tipografia
└── utils/
    ├── ErrorUtils.kt              # Parser erros HTTP → PT-BR (códigos especiais)
    ├── DateUtils.kt               # ISO dates, formatting
    └── NetworkMonitor.kt          # Connectivity observer (Flow)
```

## API Endpoints Consumidos

| Domínio | Endpoints Principais |
|---------|---------------------|
| **Auth** | `POST /auth/signin`, `POST /auth/signup`, `POST /auth/refresh-token`, `GET /auth/me`, `POST /auth/forgot-password` |
| **Exercícios** | `GET/POST/PUT/DELETE /exercises` (multipart para mídia) |
| **Templates** | `GET/POST/PUT/DELETE /templates`, `__with_workout_days` |
| **Assignments** | `GET /assignments/aluno/{id}`, `GET .../current`, `POST /assignments`, `POST /assignments/group` |
| **Sessions** | `POST /sessions/start`, `PATCH /sessions/sets/{id}`, `PATCH /sessions/exercises/{id}/status`, `POST /sessions/{id}/finish`, `GET /sessions/exercises/{id}/progress` |
| **Groups** | `GET/POST/PUT/DELETE /groups`, `POST /groups/{id}/members`, `__with=assignments` |
| **Profiles** | `GET /profiles/{id}/measurements`, `POST /profiles/me/measurements`, `GET /profiles/{id}/measurements/chart` |
| **Users (Admin)** | `GET /users`, `PATCH /users/{id}`, `PATCH /users/{id}/role`, `PATCH /users/{id}/status`, `PATCH /users/{id}/block` |
| **Announcements** | `GET/POST/PATCH/DELETE /announcements`, `type` filter (noticia/aviso/instrucoes) |
| **Exercise Metrics** | `POST/GET/DELETE /exercise-metrics/{exerciseId}` |

## ❓ Troubleshooting

| Problema | Solução |
|----------|---------|
| `SDK not found` | Verificar `local.properties` com `sdk.dir` correto |
| `Connection refused` | Backend não está rodando ou IP errado (`adb reverse` se emulador) |
| `401 Unauthorized` | Token expirado → logout/login ou aguardar auto-refresh |
| `Cleartext traffic not permitted` | `usesCleartextTraffic=true` no Manifest (dev) / usar HTTPS (prod) |
| `Build falha` | `make clean` e tentar novamente |
| `App crash na abertura` | `adb logcat -s "GymApp/Error" "AndroidRuntime"` |
| `FCM token não registra` | Verificar `google-services.json` em `app/` + backend `FCM_SERVER_KEY` |
| `Room migration crash` | `fallbackToDestructiveMigration()` apaga dados — normal em dev |

---