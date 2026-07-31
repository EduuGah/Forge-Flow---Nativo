# 0005 - Firebase para identidade e sincronização opcional

## Status

Aceita para a Fase 5.

## Contexto

O ForgeFlow precisa restaurar dados entre aparelhos e manter conta Google e e-mail sem
transformar conectividade em requisito para treinar. Fotos e localizações também exigem
isolamento por usuário e regras de acesso verificáveis.

## Decisão

Usar Credential Manager para a experiência nativa de login, Firebase Authentication para
identidade, Cloud Firestore para documentos e Cloud Storage para mídia. Room e DataStore
continuam como fonte local, com uma fila persistente de sincronização executada pelo
WorkManager.

Cada documento remoto pertence a `/users/{uid}`. Regras do backend validam o `uid`; o cliente
não recebe credenciais administrativas. A primeira autenticação solicita uma mesclagem
explícita entre dados locais e remotos.

## Consequências

- Login Google segue a interface recomendada pelo Android.
- O aplicativo continua funcional sem Google Play Services, exceto pelo login Google.
- Há custo e cota de armazenamento para fotos e futuras mídias de nutrição.
- Ambientes de desenvolvimento e produção precisam de projetos Firebase separados.
- Migrações e exclusões precisam de versão, `updatedAt` e marcador de remoção.
- Regras, exportação, exclusão e consentimentos passam a fazer parte dos critérios de entrega.
