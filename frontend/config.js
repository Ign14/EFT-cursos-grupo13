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
  // Base de la API por el API Gateway (misma Invoke URL para cursos y bff; el gateway enruta por path).
  apiCursos: "https://j75s3j3uh6.execute-api.us-east-1.amazonaws.com",
  apiBff:    "https://j75s3j3uh6.execute-api.us-east-1.amazonaws.com"
};
