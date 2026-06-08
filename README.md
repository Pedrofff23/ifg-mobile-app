# IFG Gym App - Academia IFG Anápolis

Aplicativo Android para gerenciamento de academia do Instituto Federal de Goiás - campus Anápolis.
Atende alunos e professores com funcionalidades de treino, acompanhamento de progresso e administração.

## Visão Geral

O app possui dois perfis:
- **Aluno**: visualiza treinos, executa sessões, acompanha progresso, registra medições
- **Professor/Admin**: gerencia alunos, cria treinos/exercícios, atribui treinos, publica avisos

## Stack Tecnologia

| Tecnologia | Versão |
|---|---|
| Kotlin | 2.2.10 |
| Jetpack Compose (Material 3) | BOM 2024.02.01 |
| AGP (Android Gradle Plugin) | 9.2.1 |
| Hilt (DI) | 2.59.2 |
| Retrofit + OkHttp | 2.9.0 |
| Coil (imagens/GIF) | 2.6.0 |
| DataStore Preferences | 1.1.1 |
| Navigation Compose | 2.7.7 |
| KSP | 2.0.0-1.0.21 |

## Requisitos

- JDK 17
- Android SDK 36 (compileSdk/targetSdk)
- Dispositivo/emulador Android 7.0+ (API 24)
- Backend Go rodando e acessível na rede local
- Supabase Storage (mídias de exercícios)

## Instalação

```bash
# 1. Clonar o repositório
cd ~/Documents/TCC/app

# 2. Verificar local.properties
echo "sdk.dir=/home/pedrofff/Android/Sdk" > local.properties

# 3. Build e instalação
make debug
```

## Configuração

### URLs da API

As URLs são configuradas via `BuildConfig` no `app/build.gradle.kts`:

| Constante | BuildConfig Field | Valor Padrão |
|---|---|---|
| API Base URL | `BASE_URL` | `http://192.168.240.1:8080/` |
| Supabase Storage | `SUPABASE_URL` | `http://192.168.240.1:8000/storage/v1/object/public/exercises/` |

Para produção, altere os valores em `app/build.gradle.kts` > `defaultConfig` > `buildConfigField`.

### Para Produção

1. Alterar `BASE_URL` e `SUPABASE_URL` no `build.gradle.kts`
2. Ativar `isMinifyEnabled = true` no `build.gradle.kts`
3. Configurar `signingConfig` para release
4. Gerar AAB: `make release`

## Comandos Principais

| Comando | Descrição |
|---|---|
| `make build` | Build debug APK |
| `make debug` | Build e instala debug APK |
| `make release` | Build release AAB |
| `make install` | Instalar debug no dispositivo |
| `make clean` | Limpar build |
| `make lint` | Análise estática |
| `make test` | Testes unitários |
| `make all-checks` | Lint + test + build |

Saídas:
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release AAB: `app/build/outputs/bundle/release/app-release.aab`

## Debug

```bash
# Logs de erro do app
adb logcat -s "GymApp/Error"

# Logs gerais filtrados
adb logcat | grep "gymapp"

# Verificar dispositivo conectado
adb devices
```

### Pontos de breakpoint sugeridos
- `AuthInterceptor.kt:30` — Anexação de token JWT
- `AuthViewModel.kt:46` — Login
- `NetworkModule.kt:24` — Base URL (BuildConfig)
- `TokenManager.kt:46` — Persistência de sessão

## Estrutura do Projeto

```
app/src/main/java/com/example/gymapp/
├── GymApplication.kt                  # Entry point Hilt
├── MainActivity.kt                    # Activity principal (edge-to-edge)
├── data/
│   ├── local/
│   │   └── TokenManager.kt            # DataStore: tokens, user data
│   └── remote/
│       ├── AuthService.kt             # POST signin/signup/refresh-token, GET me
│       ├── ErpService.kt              # CRUD exercícios, templates, assignments, sessions, announcements
│       ├── GroupService.kt            # CRUD grupos + membros
│       ├── ProfileService.kt          # Profile + body measurements
│       ├── UserService.kt             # CRUD usuários (admin)
│       └── AuthInterceptor.kt         # JWT attachment + refresh automático
├── di/
│   └── NetworkModule.kt               # Módulo Hilt: OkHttp, Retrofit, services
├── domain/model/
│   ├── ApiResponse.kt                 # Wrapper API responses
│   ├── AuthModels.kt                  # Login, Register, User, Me
│   ├── ErpModels.kt                   # Exercise, Template, Assignment, Session, Announcement, Group, Profile, Stats
│   └── RequestDTOs.kt                 # Request DTOs
├── presentation/
│   ├── auth/
│   │   ├── LoginScreen.kt             # Tela de login
│   │   ├── RegisterScreen.kt          # Tela de registro
│   │   └── AuthViewModel.kt           # Estado de autenticação
│   ├── navigation/
│   │   └── AppNavigation.kt           # NavHost com rotas
│   ├── student/
│   │   ├── StudentMainScreen.kt       # Bottom bar (5 tabs)
│   │   ├── StudentHomeScreen.kt       # Dashboard do aluno
│   │   ├── StudentWorkoutHubScreen.kt # Hub: treinos + exercícios
│   │   ├── StudentWorkoutsScreen.kt   # Lista de treinos atribuídos
│   │   ├── StudentExercisesScreen.kt  # Biblioteca de exercícios
│   │   ├── WorkoutSessionScreen.kt    # Execução de treino
│   │   ├── WorkoutSessionViewModel.kt # Estado da sessão
│   │   ├── StudentProgressScreen.kt   # Progresso e estatísticas
│   │   ├── StudentCommunicationScreen.kt # Avisos, instruções, notícias
│   │   ├── StudentProfileScreen.kt    # Perfil e configurações
│   │   └── StudentViewModel.kt        # Estado do aluno
│   └── trainer/
│       ├── TrainerMainScreen.kt        # Bottom bar (6 tabs)
│       ├── ProfessorDashboardScreen.kt # Dashboard do professor
│       ├── TrainerWorkoutHubScreen.kt  # Hub: gerenciar treinos + exercícios
│       ├── ManageWorkoutsScreen.kt     # CRUD treinos
│       ├── ManageExercisesScreen.kt    # CRUD exercícios com mídia
│       ├── CreateWorkoutScreen.kt      # Criar treino do zero
│       ├── TrainerStudentsHubScreen.kt # Hub: alunos + admin
│       ├── StudentsOverviewScreen.kt   # Lista de alunos
│       ├── StudentDetailScreen.kt      # Detalhes do aluno
│       ├── ManageGroupsScreen.kt       # CRUD grupos
│       ├── CreateAnnouncementScreen.kt # Mural de avisos
│       ├── AdminScreen.kt             # Gerenciar usuários (admin)
│       ├── TrainerProfileScreen.kt     # Perfil do professor
│       └── ProfessorViewModel.kt       # Estado do professor
├── ui/theme/
│   ├── Color.kt                       # Cores IFG + paletas de tema
│   ├── Theme.kt                       # 6 temas (3 claros + 3 escuros)
│   ├── ThemeManager.kt                # Persistência de tema
│   ├── ThemeComponents.kt             # ThemeSelectionDialog e ThemeOptionRow reutilizáveis
│   └── Type.kt                        # Tipografia
└── utils/
    └── ErrorUtils.kt                  # Parser de erros HTTP em PT-BR

app/src/test/java/com/example/gymapp/
├── utils/ErrorUtilsTest.kt            # Testes de parsing de erros
└── domain/model/ModelParsingTest.kt   # Testes de deserialização de modelos
```

## API Endpoints Consumidos

### Auth
- `POST /auth/signin` — Login
- `POST /auth/signup` — Registro
- `POST /auth/refresh-token` — Refresh JWT
- `GET /auth/me` — Dados do usuário logado

### Exercícios
- `GET /exercises` — Listar (filtros: limit, offset, muscle_group, search)
- `POST /exercises` — Criar (multipart)
- `PUT /exercises/{id}` — Atualizar (multipart)
- `DELETE /exercises/{id}` — Excluir

### Templates (Treinos)
- `GET /templates` — Listar
- `GET /templates/{id}` — Detalhe
- `POST /templates` — Criar
- `PUT /templates/{id}` — Atualizar
- `DELETE /templates/{id}` — Excluir

### Assignments
- `GET /assignments/aluno/{aluno_id}` — Listar por aluno
- `GET /assignments/aluno/{aluno_id}/current` — Assignment atual
- `POST /assignments` — Atribuir treino a aluno
- `POST /assignments/group` — Atribuir treino a grupo

### Sessions
- `GET /sessions/aluno/{aluno_id}` — Listar sessões
- `POST /sessions/start` — Iniciar sessão
- `GET /sessions/{id}` — Detalhe
- `PATCH /sessions/sets/{setId}` — Atualizar série
- `PATCH /sessions/exercises/{exerciseId}/status` — Status do exercício
- `POST /sessions/{id}/finish` — Finalizar sessão
- `GET /sessions/exercises/{exerciseId}/progress` — Histórico de carga
- `GET /sessions/aluno/{aluno_id}/stats` — Estatísticas

### Groups
- `GET /groups` — Listar
- `GET /groups/{id}` — Detalhe
- `POST /groups` — Criar
- `PUT /groups/{id}` — Atualizar
- `DELETE /groups/{id}` — Excluir
- `POST /groups/{id}/members` — Adicionar membro
- `DELETE /groups/{id}/members/{userId}` — Remover membro

### Profiles
- `GET /profiles/{id}` — Perfil do aluno
- `POST /profiles/me` — Criar/atualizar perfil
- `GET /profiles/{id}/measurements` — Medições
- `POST /profiles/me/measurements` — Adicionar medição

### Users
- `GET /users` — Listar usuários
- `GET /users/{id}` — Detalhe
- `PATCH /users/{id}` — Atualizar perfil
- `PATCH /users/{id}/role` — Alterar role (admin)
- `PATCH /users/{id}/status` — Ativar/desativar (admin)

### Announcements
- `GET /announcements` — Listar
- `POST /announcements` — Criar
- `DELETE /announcements/{id}` — Excluir

## Troubleshooting

| Problema | Solução |
|---|---|
| `SDK not found` | Verificar `local.properties` com path correto do SDK |
| `Connection refused` | Verificar se o backend está rodando |
| `401 Unauthorized` | Token expirado; fazer logout e login novamente |
| `Cleartext traffic not permitted` | `usesCleartextTraffic=true` já configurado |
| `Build falha` | Rodar `make clean` e tentar novamente |
| `App crash na abertura` | Verificar `adb logcat -s "GymApp/Error"` |
