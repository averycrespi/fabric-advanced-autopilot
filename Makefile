GRADLE=./gradlew

.PHONY: build
build:
	$(GRADLE) build

.PHONLY: gen
gen:
	$(GRADLE) genSources

.PHONY: clean
clean:
	$(GRADLE) clean

.PHONY: refresh
refresh:
	$(GRADLE) --refresh-dependencies
