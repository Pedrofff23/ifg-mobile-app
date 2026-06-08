.PHONY: help build debug release install clean lint test all

# Default target
all: help

# Show available commands
help:
	@echo "IFG Gym App - Available Commands"
	@echo "================================="
	@echo ""
	@echo "  make build         - Build debug APK"
	@echo "  make debug         - Build and install debug APK"
	@echo "  make release       - Build release AAB"
	@echo "  make install       - Install debug APK on connected device"
	@echo "  make clean         - Clean build artifacts"
	@echo "  make lint          - Run static analysis"
	@echo "  make test          - Run unit tests"
	@echo "  make test-ui       - Run instrumentation tests"
	@echo "  make all-checks    - Run lint + test + build"
	@echo ""

# Build debug APK
build:
	@echo "Building debug APK..."
	./gradlew assembleDebug
	@echo "Output: app/build/outputs/apk/debug/app-debug.apk"

# Build and install debug APK
debug:
	@echo "Building and installing debug APK..."
	./gradlew installDebug

# Build release AAB
release:
	@echo "Building release AAB..."
	./gradlew bundleRelease
	@echo "Output: app/build/outputs/bundle/release/app-release.aab"

# Install debug APK on connected device
install:
	@echo "Installing debug APK..."
	./gradlew installDebug

# Clean build artifacts
clean:
	@echo "Cleaning build..."
	./gradlew clean

# Run static analysis
lint:
	@echo "Running lint..."
	./gradlew lint

# Run unit tests
test:
	@echo "Running unit tests..."
	./gradlew test

# Run instrumentation tests
test-ui:
	@echo "Running instrumentation tests..."
	./gradlew connectedAndroidTest

# Run all checks
all-checks: lint test build
	@echo "All checks passed!"
