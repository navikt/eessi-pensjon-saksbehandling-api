DOCKER  := docker
GRADLE  := ./gradlew
NAIS    := nais
GIT     := git
VERSION := $(shell cat ./VERSION)
REGISTRY:= repo.adeo.no:5443

.PHONY: all build test docker docker-push bump-version release manifest

all: build test docker
release: tag docker-push

build:
	$(GRADLE) assemble

test:
	$(GRADLE) test

docker:
	$(NAIS) validate --file nais.yaml
	$(DOCKER) build -f Dockerfile --pull -t $(REGISTRY)/eessi-pensjon-frontend-api-fss .

docker-push:
	$(DOCKER) tag $(REGISTRY)/eessi-pensjon-frontend-api-fss $(REGISTRY)/eessi-pensjon-frontend-api-fss:$(VERSION)
	$(DOCKER) push $(REGISTRY)/eessi-pensjon-frontend-api-fss:$(VERSION)

bump-version:
	@echo $$(($$(cat ./VERSION) + 1)) > ./VERSION

tag:
	git add VERSION
	git commit -m "Bump version to $(VERSION) [skip ci]"
	git tag -a $(VERSION) -m "auto-tag from Makefile"


manifest:
	$(NAIS) upload --app eessi-pensjon-frontend-api-fss -v $(VERSION) --file nais.yaml
