#!/bin/bash
# Uso: ./scripts/release.sh 1.1.0

set -e

VERSION_NAME="${1:-}"
if [[ -z "$VERSION_NAME" ]]; then
    echo "Uso: $0 <version-name> (ex: 1.1.0)"
    exit 1
fi

# Extrair versionCode do versionName (semantic: major*10000 + minor*100 + patch)
IFS='.' read -r major minor patch <<< "$VERSION_NAME"
VERSION_CODE=$((major * 10000 + minor * 100 + patch))

echo "📦 Preparando release v$VERSION_NAME (code: $VERSION_CODE)"

# 1. Validar keystore
if [[ ! -f "gymapp-release.jks" ]]; then
    echo "❌ Keystore não encontrado. Gere com:"
    echo "   keytool -genkey -v -keystore gymapp-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias gymapp-key"
    exit 1
fi

# 2. Validar google-services.json
if [[ ! -f "app/google-services.json" ]]; then
    echo "❌ google-services.json não encontrado em app/"
    exit 1
fi

# 3. Validar variáveis de ambiente
: "${KEYSTORE_PASSWORD:?Defina KEYSTORE_PASSWORD}"
: "${KEY_PASSWORD:?Defina KEY_PASSWORD}"
: "${KEY_ALIAS:?Defina KEY_ALIAS}"

# 4. Build release
echo "🔨 Building release AAB..."
VERSION_CODE=$VERSION_CODE VERSION_NAME=$VERSION_NAME ./gradlew bundleRelease

AAB_PATH="app/build/outputs/bundle/release/app-release.aab"
if [[ ! -f "$AAB_PATH" ]]; then
    echo "❌ AAB não gerado em $AAB_PATH"
    exit 1
fi

echo "✅ Release AAB gerado: $AAB_PATH"
echo "📤 Faça upload manual no Google Play Console ou configure automação"
echo ""
echo "Checklist pós-build:"
echo "  [ ] Testar AAB em device real: ./gradlew assembleRelease && adb install app/build/outputs/apk/release/app-release.apk"
echo "  [ ] Verificar se FCM funciona (push real)"
echo "  [ ] Verificar URLs de produção (HTTPS)"
echo "  [ ] Incrementar versionCode para próximo release"