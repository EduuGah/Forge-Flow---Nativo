# ForgeFlow Android

ForgeFlow é um aplicativo Android nativo para planejar treinos, registrar sessões e acompanhar
evolução física. O app é escrito em Kotlin com Jetpack Compose, funciona offline e usa recursos
do Android diretamente. Não há WebView, Capacitor, React, TypeScript ou código reaproveitado da
versão web.

## Funcionalidades

### Conta e proteção de dados

- entrada com Google pelo Credential Manager;
- cadastro e entrada com e-mail e senha, redefinição de senha e vínculo de senha à conta Google;
- perfil isolado por conta no aparelho, evitando mistura de dados ao trocar de usuário;
- exportação e restauração integral por arquivo ZIP validado;
- backup privado e versionado da conta no Cloud Firestore, com integridade SHA-256;
- backup e transferência criptografada oferecidos pelo Android em aparelhos elegíveis;
- tags de apoiador lidas em tempo real e tela própria do ForgeFlow Pro.

### Dashboard, planejamento e metas

- dashboard compacto com sequência, meta semanal, resumo de volume e atividade recente;
- calendário de treinos, horários planejados e visão semanal;
- metas únicas, diárias, semanais e mensais, com prazo opcional;
- metas de carga por exercício, quantidade de treinos, volume e duração;
- cálculo automático do progresso a partir do histórico real;
- evolução geral de treinos, volume, peso corporal e recordes pessoais.

### Rotinas e biblioteca

- catálogo local auditado com 126 exercícios, mídia, instruções, erros comuns e aliases;
- pesquisa sem acentos, por nomes alternativos, músculo e equipamento;
- criação de exercícios personalizados com foto;
- página de exercício com histórico, gráficos, melhores séries e PRs de carga e volume;
- rotinas em pastas, busca, cópia, edição, exclusão e reordenação por pressão longa;
- planejamento de séries normais e de aquecimento por exercício;
- comparação opcional limitada aos treinos da mesma pasta.

### Treino ativo

- sessão persistente com cronômetro, volume, progresso e notificação contínua;
- séries normais e de aquecimento, peso, repetições, conclusão e exclusão;
- desempenho anterior resumido, notas persistentes e detecção imediata de PR;
- cronômetro de descanso com ações na notificação e funcionamento em segundo plano;
- adicionar, trocar, remover, minimizar e reordenar exercícios durante a sessão;
- validação contra finalização sem séries válidas;
- localização opcional capturada somente ao concluir o treino;
- decisão final para manter, atualizar ou copiar a rotina de origem.

### Histórico e análises

- histórico expansível com detalhes de exercícios, séries, volume, tempo e PRs;
- busca por treino, exercício, local ou data e filtros por período e localização;
- exclusão com confirmação;
- gráficos interativos e comparação de desempenho por exercício;
- mapa individual do treino e mapa geral de locais, com agrupamento de pontos próximos;
- compartilhamento em formato story com foto, temas, cartões, campos visíveis e gestos;
- recordes com contexto do treino, data, carga, repetições e volume.

### Perfil, medidas e fotos

- nome, nascimento, altura, peso, objetivo e nível de experiência;
- foto de perfil com pré-visualização circular, pinça, movimento e rotação;
- histórico de peso com data, conversão kg/lb e bloqueio de datas futuras;
- galeria privada de fotos de progresso com filtros por período e data exata;
- detalhes por foto, seleção por pressão longa e exclusão;
- comparação de duas fotos com divisor arrastável.

### Nutrição e bem-estar

- diário por data com refeições, fotos, calorias e macronutrientes;
- cálculo por proteína, carboidrato e gordura;
- metas diárias e indicadores circulares de calorias e água;
- registro rápido de hidratação e widgets de nutrição e água;
- lembretes configuráveis de hidratação, pausa e movimento.

### Integrações Android

- Health Connect para peso, passos, distância, calorias, frequência cardíaca, sono e exercícios;
- painel de saúde com origem dos dados, incluindo Samsung Health, e gráficos de 7 e 30 dias;
- três widgets redimensionáveis: treino, nutrição e hidratação;
- atalhos do sistema, notificações por canal, feedback tátil e permissões contextuais;
- ícone adaptativo com variações de cor;
- temas claro, escuro e do sistema, modo compacto e cores de destaque;
- tutorial inicial animado e treino guiado isolado do histórico.

## Arquitetura

Projeto multi-módulo com Hilt, Room, DataStore, Navigation Compose, Coroutines e Material 3:

```text
:app
:core:common       :core:model       :core:designsystem
:core:database     :core:data        :core:navigation
:core:platform     :core:testing
:feature:home      :feature:nutrition
:feature:exercises :feature:routines :feature:workout
:feature:history   :feature:settings
```

Room e DataStore são a fonte local de verdade. Depois de entrar, o registro de treinos continua
funcionando offline; o backup remoto é sempre uma ação explícita. Consulte
[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) e
[docs/DATABASE.md](docs/DATABASE.md).

## Configuração

- Android Studio compatível com Android Gradle Plugin 9.2;
- JDK 17;
- Android SDK 36.1;
- `minSdk` 26 e `targetSdk` 36.

Para autenticação e backup da conta, adicione o arquivo local `app/google-services.json` e siga
[docs/AUTHENTICATION.md](docs/AUTHENTICATION.md). O arquivo e segredos de servidor não devem ser
versionados.

## Compilar e testar

No PowerShell:

```powershell
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
.\gradlew.bat connectedDebugAndroidTest
```

Para gerar e instalar o APK em um aparelho reconhecido pelo ADB:

```powershell
.\gradlew.bat assembleDebug
adb install -r .\app\build\outputs\apk\debug\app-debug.apk
```

O roteiro manual está em [docs/VALIDATION.md](docs/VALIDATION.md) e o estado das próximas entregas
em [docs/ROADMAP.md](docs/ROADMAP.md).

## Limites atuais

- compras do ForgeFlow Pro ainda dependem da configuração de produtos e Google Play Billing;
- o backup integral da conta funciona, mas a sincronização granular em tempo real ainda não está
  ativa;
- análise automática de refeições, banco de alimentos, código de barras e Wear OS permanecem no
  roadmap;
- publicação exige política de privacidade, build assinado, monitoramento e validação final da
  Play Store.
