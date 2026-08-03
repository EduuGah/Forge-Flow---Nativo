# 0005 - Firebase para identidade e sincronização opcional

## Status

Aceita para a Fase 5.

## Contexto

O ForgeFlow precisa restaurar dados entre aparelhos e manter conta Google e e-mail sem
transformar conectividade em requisito para treinar. Fotos e localizações também exigem
isolamento por usuário e regras de acesso verificáveis.

## Decisão

Usar Credential Manager para a experiência nativa de login e Firebase Authentication para
identidade. O backup integral é dividido em blocos versionados no Cloud Firestore, com tamanho,
proprietário e hash SHA-256 validados. Room e DataStore continuam como fonte local. A sincronização
granular futura usará uma fila persistente executada pelo WorkManager.

Cada documento remoto pertence a `/users/{uid}`. Regras do backend validam o `uid`; o cliente
não recebe credenciais administrativas. A primeira autenticação solicita uma mesclagem
explícita entre dados locais e remotos.

## Consequências

- Login Google segue a interface recomendada pelo Android.
- O aplicativo continua funcional sem Google Play Services, exceto pelo login Google.
- O backup no Firestore possui limites explícitos de tamanho e quantidade de blocos.
- Ambientes de desenvolvimento e produção precisam de projetos Firebase separados.
- Migrações e exclusões precisam de versão, `updatedAt` e marcador de remoção.
- Regras, exportação, exclusão e consentimentos passam a fazer parte dos critérios de entrega.
