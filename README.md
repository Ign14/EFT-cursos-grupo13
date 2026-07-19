# EFT S9 — Plataforma de Cursos en linea (Grupo 13)

Evaluacion Final Transversal de Desarrollo Cloud Native (CDY2204). Solucion Cloud
Native para gestion de cursos en linea: estudiantes se inscriben, acceden a
material y son calificados; instructores gestionan cursos y notas.

## Arquitectura

```
Frontend SPA (MSAL/B2C)
        │  (JWT Bearer)
        ▼
   AWS API Gateway (API Manager, autorizador JWT)
        ├──────────────▶  cursos-service (8081)  ── Oracle/H2 + Amazon S3 (material)
        └──────────────▶  bff-service   (8082)  ── RabbitMQ (cola principal → DLX → DLQ)
                                   (BFF: orquesta las colas)
Identidad: Azure AD B2C (IDaaS) — roles ESTUDIANTE / INSTRUCTOR (claim extension_Role)
Despliegue: Docker Compose sobre EC2, via pipeline CI/CD (GitHub Actions).
```

## Microservicios

| Servicio | Puerto | Responsabilidad |
|---|---|---|
| cursos-service | 8081 | Cursos, inscripciones, calificaciones; material en S3; Oracle/H2; Spring Security B2C |
| bff-service | 8082 | BFF que orquesta las colas RabbitMQ (producir / consumir / DLQ) |
| frontend | 8080 | SPA con login Azure AD B2C (MSAL) que consume la API |

## Endpoints principales

cursos-service (rol entre parentesis):
| Metodo | Ruta | Rol |
|---|---|---|
| POST | /api/cursos | INSTRUCTOR |
| GET | /api/cursos | autenticado |
| GET | /api/cursos/{id} | autenticado |
| PUT | /api/cursos/{id} | INSTRUCTOR |
| DELETE | /api/cursos/{id} | INSTRUCTOR |
| POST | /api/cursos/{id}/material | INSTRUCTOR (sube a S3) |
| GET | /api/cursos/{id}/material | ESTUDIANTE/INSTRUCTOR (descarga de S3) |
| POST | /api/inscripciones | ESTUDIANTE |
| GET | /api/inscripciones/mias | autenticado |
| POST | /api/calificaciones | INSTRUCTOR |
| GET | /api/calificaciones/mias | autenticado |

bff-service (colas):
| Metodo | Ruta | Rol | Descripcion |
|---|---|---|---|
| POST | /api/bff/cola/producir | ESTUDIANTE/INSTRUCTOR | PRODUCTOR: publica en la cola principal |
| POST | /api/bff/cola/consumir | INSTRUCTOR | CONSUMIDOR: procesa; fallos → DLQ via DLX |
| GET | /api/bff/cola/errores | INSTRUCTOR | Muestra la DLQ (mensajes dead-lettered) |

## Ejecutar en local

```bash
docker compose up -d --build
# frontend:  http://localhost:8080
# cursos:    http://localhost:8081/actuator/health
# bff:       http://localhost:8082/actuator/health
# RabbitMQ:  http://localhost:15672  (guest/guest)
```

Los servicios validan el JWT de Azure AD B2C. Obten un token (Postman OAuth2 o el
frontend) para consumir los endpoints protegidos.

## Variables de entorno clave

| Variable | Servicio | Descripcion |
|---|---|---|
| AZURE_B2C_ISSUER / AZURE_B2C_JWK_SET_URI | cursos, bff | Validacion del JWT de B2C |
| AZURE_B2C_ROLES_CLAIM | cursos, bff | Claim de rol (extension_Role) |
| AWS_S3_BUCKET / AWS_REGION | cursos | Almacenamiento Cloud (material) |
| RABBITMQ_HOST | bff | Host de RabbitMQ (rabbitmq en compose) |
| DB_URL/DB_DRIVER/DB_USERNAME/DB_PASSWORD | cursos | Oracle (por defecto H2) |

## Despliegue (CI/CD)

`.github/workflows/main.yml` construye las 3 imagenes, las publica en Docker Hub y
despliega en la EC2 con Docker Compose. Requiere los GitHub Secrets: DOCKERHUB_USERNAME,
DOCKERHUB_TOKEN, EC2_HOST, EC2_USER, EC2_SSH_KEY, AZURE_B2C_ISSUER, AZURE_B2C_JWK_SET_URI,
AWS_S3_BUCKET.

## Documentacion

- `docs/GUIA-CONFIGURACION-EFT.md` — paso a paso de IDaaS, API Manager y colas.
- `docs/postman/EFT-Cursos-S9.postman_collection.json` — coleccion de pruebas.
- `cursos-service/src/main/resources/db/schema-oracle.sql` — scripts de Oracle.

## Notas de auditoria / consideraciones

- **Repositorio**: este proyecto (`EFT_S9_Grupo13/`) es la RAIZ del repositorio GIT.
  El pipeline `.github/workflows/main.yml` asume esa raiz (contextos `./cursos-service`,
  `./bff-service`, `./frontend`).
- **Bucket S3**: crea el bucket `eft-cursos-grupo13` en S3 (o cambia `AWS_S3_BUCKET`
  por uno existente) antes de probar la subida/descarga de material.
- **Frontend en la nube**: `frontend/config.js` apunta a `localhost` por defecto. Para la
  demo en la nube, cambia `apiCursos`/`apiBff` a la **Invoke URL del API Gateway** y
  registra la URL del frontend como **Redirect URI (SPA)** en Azure AD B2C.
- **CORS + API Gateway**: si el frontend consume por el gateway, habilita **CORS** en la
  HTTP API (los preflight `OPTIONS` no llevan token). En local, el backend ya expone CORS.
- **Colas (demo)**: `GET /api/bff/cola/errores` **drena** la DLQ (lectura destructiva),
  igual que `consumir`; una segunda consulta puede salir vacia. Es intencional para evidenciar.
- **Base de datos**: por defecto H2 (archivo local). Para Oracle Cloud, define
  `DB_URL/DB_DRIVER/DB_USERNAME/DB_PASSWORD` y ejecuta `db/schema-oracle.sql`.
- **Token de roles**: los usuarios de B2C deben tener el atributo `Role` = `ESTUDIANTE`
  o `INSTRUCTOR`; el claim viaja como `extension_Role`.
