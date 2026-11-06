# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

Panoptes is MBARI's image upload/archiving HTTP service. Clients exchange an API key for a JWT, then POST images (multipart) which are written to a disk archive organized by camera ID and deployment ID; the service returns the URL where the image can be retrieved.

Stack: Scala 3, sbt, Tapir endpoints served by Vert.x (`VertxFutureServerInterpreter`, `Future`-based), Circe JSON, Typesafe Config, auth0 java-jwt, MUnit tests. Targets Java 21.

## Commands

```bash
sbt compile
sbt test                                             # sbt 2: incremental, only re-runs changed/failed tests
sbt testFull                                         # run every test (forked, sequential, UTC timezone)
sbt "testOnly org.mbari.panoptes.endpoints.ImageEndpointsSuite"
sbt "testOnly *ImageEndpointsSuite -- *upload*"      # single MUnit test by name
sbt run                                              # starts server on port 8080 (HTTP_PORT overrides)
sbt scalafmtAll                                      # format (see .scalafmt.conf: 4-space indent, 120 cols)
sbt headerCreate                                     # add Apache license headers (AutomateHeaderPlugin)
sbt stage                                            # sbt-native-packager build -> target/universal/stage
sbt dependencyUpdates                                # check for dependency updates
```

Version comes from git tags via sbt-git (tags like `1.2.3`, no leading `v`).

Docker: `docker run --name=panoptes -p 8080:8080 mbari/panoptes`

## Architecture

- `org.mbari.panoptes.Main` — boots Vert.x, wires the Prometheus metrics interceptor, and calls `Endpoints.attachRoutes`.
- `org.mbari.panoptes.Endpoints` (object) — composition root: builds the `JwtService`, instantiates each endpoint group, collects their `allImpl`, and mounts them plus `/metrics` and Swagger docs. Application endpoints are mounted with `blockingRoute` (file I/O happens on worker threads). New endpoint groups must be added to the list here.
- `org.mbari.panoptes.endpoints.Endpoints` (trait) — base for every endpoint group. Provides `openEndpoint` / `secureEndpoint` (bearer-JWT, with the shared `ErrorMsg` `oneOf` error mapping), context-path prefixing from `http.context.path`, `verify` for JWT security logic, and `handleErrors` / `handleOption` to turn `Future`s into `Either[ErrorMsg, T]`. Each group exposes `all` (endpoint definitions) and `allImpl` (server endpoints).
- Endpoint groups: `AuthorizationEndpoints` (`POST /v1/auth` with `Authorization: APIKEY <key>` → JWT), `HealthEndpoints`, `ImageEndpoints` (`POST`/`GET /v1/images/{camera_id}/{deployment_id}/{name}`).
- `AppConfig` — reads all config (`src/main/resources/reference.conf`, overridable via env vars such as `HTTP_PORT`, `BASICJWT_CLIENT_SECRET`, `BASICJWT_SIGNING_SECRET`, `PANOPTES_FILE_ARCHIVER`, `PANOPTES_MBARI_IMAGE_ARCHIVE_ROOT`, `PANOPTES_MBARI_IMAGE_ARCHIVE_URL`, `PANOPTES_MAX_SIZE_MB`).
- Archivers live in the older package `org.mbari.m3.panoptes.services`. `FileArchiver` (trait + companion with `Instance`) → `DiskArchiver` → `MbariDiskArchiver` (`<root>/<cameraId>/images/<deploymentId>/<name>`) → `OldStyleMbariDiskArchiver` (default; uses a zero-padded trailing dive number, e.g. `Ventana/images/2034`). The implementation is chosen **by class name via reflection** from `panoptes.file.archiver`, so implementations need a no-arg constructor.
- `etc/` holds cross-cutting helpers: Circe codecs/printer (snake_case JSON), JDK `System.Logger` extensions (`log.atInfo.log(...)`), `JwtService`, Future helpers (`.join` in tests).

## Testing

- Tests use Tapir's `TapirStubInterpreter` (no real server). `EndpointsSuite` provides `newBackendStub` / `runGet` helpers that the endpoint suites extend.
- sbt 2 packages resources into jars (`exportJars`), so `getResource` can return a `jar:` URL — don't call `Paths.get(url.toURI)` on it; read via `openStream()` (copy to a temp file if a `Path` is needed).
- `src/test/resources/application.conf` writes archived images to `target/trashme/panoptes`.
- `src/universal/bin/test.sh <url> <jwt> <file>` is a curl helper for manual upload tests against a running server.

## Notes

- `src/universal/conf/` (application.conf, logback.xml) is the production config shipped with the staged app; the launcher scripts point `-Dconfig.file` / `-Dlogback.configurationFile` at it.
- `*.orginal` / `*.original` / `*.orig` files are leftovers from the old Scalatra/Jetty implementation and are not compiled.
