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

testComponent:
	./gradlew testComponent

.PHONY: test testIntegration testComponent


# Aliases
build-dependencies: refresh-dependencies dependencies
all-tests: test testIntegration testComponent
.PHONY: build-dependencies all-tests
