# Dependencies
refresh-dependencies:
	./gradlew build --refresh-dependencies

dependencies:
	./gradlew dependencies

.PHONY: refresh-dependencies dependencies


# Build
build:
	./gradlew build
.PHONY: build


# Tests
test:
	./gradlew test

testIntegration:
	./gradlew testIntegration

.PHONY: test testIntegration


# Aliases
build-dependencies: refresh-dependencies dependencies
all-tests: test testIntegration
.PHONY: build-dependencies all-tests