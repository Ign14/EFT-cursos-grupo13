// Configuracion del frontend EFT (editar segun el entorno).
window.EFT_CONFIG = {
  // Azure AD B2C (IDaaS) - valores del Grupo 13
  b2c: {
    clientId: "259dff0d-8d49-41ef-8f85-18bebb472ec0",
    authority: "https://duocgrupo13.b2clogin.com/duocgrupo13.onmicrosoft.com/B2C_1_signupsignin",
    knownAuthority: "duocgrupo13.b2clogin.com",
    // Debe estar registrado como Redirect URI (SPA) en la app de B2C:
    redirectUri: window.location.origin,
    // Scope del access token (el App ID URI / client id de la API)
    scopes: ["openid", "259dff0d-8d49-41ef-8f85-18bebb472ec0"]
  },
  // Base de la API. En local: http://localhost:8081 (cursos) / :8082 (bff).
  // En la nube: la Invoke URL del API Gateway.
  apiCursos: "http://localhost:8081",
  apiBff:    "http://localhost:8082"
};
