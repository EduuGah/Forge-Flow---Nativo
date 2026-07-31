# ForgeFlow Android

ForgeFlow é um aplicativo Android nativo para acompanhamento de treinos de musculação.
O projeto prioriza uso offline e será expandido gradualmente para rotinas, treino ativo,
histórico, tutorial e sincronização.

## Estado atual

O aplicativo já contém:

- dashboard, calendário, metas, horários e evolução;
- biblioteca de exercícios, rotinas em pastas e treino ativo persistente;
- histórico, recordes, gráficos, filtros e mapas de locais;
- perfil local, editor de avatar e galeria comparativa de fotos;
- evolução separada por treinos, volume, peso corporal e recordes;
- diário de nutrição local com fotos, metas e macronutrientes;
- navegação principal compacta com menu lateral;
- notificações, widget, atalhos e integração com Health Connect;
- backup criptografado e transferência de dados oferecidos pelo Android;
- testes locais, instrumentados e verificação com Android Lint.

Não há WebView, Capacitor, React, TypeScript ou código reaproveitado da versão web.

## Requisitos

- Android Studio compatível com Android Gradle Plugin 9.2;
- JDK 17 ou superior compatível com o AGP (o projeto usa toolchain 17);
- Android SDK 36.1;
- `minSdk` 26 e `targetSdk` 36.

## Abrir no Android Studio

1. Abra o diretório raiz `ForgeFlow-Android`.
2. Aguarde a sincronização do Gradle.
3. Selecione a configuração `app`.
4. Execute em um dispositivo ou emulador com API 26 ou superior.

## Comandos

No PowerShell:

```powershell
.\gradlew.bat test
.\gradlew.bat lint
.\gradlew.bat assembleDebug
```

Para compilar os testes instrumentados:

```powershell
.\gradlew.bat assembleDebugAndroidTest
```

Para executá-los com um dispositivo ou emulador conectado:

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

O APK de debug é gerado em:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Módulos

```text
:app
:core:common
:core:model
:core:designsystem
:core:database
:core:data
:core:navigation
:core:testing
:feature:home
:feature:nutrition
:feature:exercises
:feature:routines
:feature:workout
:feature:history
:feature:settings
```

Consulte [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) para o fluxo de dados e
[docs/DATABASE.md](docs/DATABASE.md) para a modelagem persistente. O planejamento
atual está em [docs/ROADMAP.md](docs/ROADMAP.md), e o roteiro para testar o APK
está em [docs/VALIDATION.md](docs/VALIDATION.md).
