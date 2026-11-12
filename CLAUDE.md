# Campo Minado — Praça Osório (contexto para o Claude Code)

Trabalho final (A1) de Desenvolvimento Android, feito em grupo. Responda sempre em português.
Projeto novo e independente: NÃO misturar com projetos antigos (Tun Tun Clock, relógio, Roblox, Food Evolution).

## Como trabalhar neste projeto

- Cada integrante precisa entender e defender o código sozinho perante o professor.
  Explique o que cada arquivo faz e por quê; prefira código simples e comentado a soluções "espertas".
- Antes de mudar algo existente, veja como ele se relaciona com o resto do projeto.
- Desenvolvimento progressivo: uma camada por vez, compilando e testando a cada passo.
- Nunca use `git push --force` nem reescreva o histórico. Não apague commits.
- Mensagens de commit no formato `tipo-descricao_com_underscores`, por exemplo:
  `feat-adicionar_banco_room`, `fix-corrigir_cascata_de_casas`, `docs-atualizar_readme`, `chore-...`, `refactor-...`.
- Commits pequenos, um assunto por commit. Cada integrante deve fazer os commits da própria parte.
- O app é compilado e executado no Android Studio (Gradle + emulador). Não altere versões do Gradle sem necessidade.

## O jogo (decisões fechadas)

Campo Minado tradicional ambientado na Praça Osório (Curitiba).
- Mapas sempre quadrados. Galhos caídos fazem o papel das minas.
- Casa aberta mostra quantos galhos há nas 8 vizinhas; se for 0, as vizinhas abrem em cascata (BFS com fila).
- Placa de aviso = bandeira. Colocada pelo botão "Abrir / Placa" OU por toque longo na casa (os dois).
- Primeiro toque é sempre seguro: os galhos são sorteados depois dele, fora da casa tocada e das vizinhas.
- Abrir um galho encerra a partida na hora (sem vidas). Vitória: todas as casas sem galho abertas.
- Recordes = melhor tempo por nível, entre os jogadores cadastrados no mesmo aparelho (jogo offline).
- Vencer um nível desbloqueia o próximo.

| Níveis | Mapa  | Galhos         |
|--------|-------|----------------|
| 1–3    | 6×6   | 4, 5, 6        |
| 4–6    | 7×7   | 7, 8, 9        |
| 7–9    | 8×8   | 10, 11, 12     |
| 10–12  | 9×9   | 13, 14, 15     |
| 13–15  | 10×10 | 16, 18, 20     |

Os galhos por nível são valores padrão: o Admin pode mudá-los no Firestore (de 1 até 25% das casas).
O tamanho do mapa de cada nível é fixo.

## Requisitos obrigatórios do professor (PDF)

- MVVM com pelo menos 4 pacotes: ui, data.local, data.repository, data.remote/logic.
- Mínimo 2 ViewModels, cada um com seu `data class UiState`.
- `ViewModelFactory` obrigatória para injetar o Repository nos ViewModels.
- Repository com try-catch isolando falhas de Room e Firebase; erro tratado exposto no UiState.
- Room: mínimo 2 `@Entity`, listagens via `Flow<List<...>>` coletadas pelo ViewModel, CRUD completo.
- Integração externa funcional: Firebase Firestore.
- Jetpack Compose, mínimo 6 telas, Bottom Navigation Bar.
- LazyColumn / LazyVerticalGrid para listas.
- Filtros e ordenação no ViewModel com `filter` / `map` / `sortedBy` e lambdas.
- Perfis Admin e Jogador: telas e funções mostradas ou ocultadas conforme o estado no ViewModel.

## Arquitetura

Pacote: `com.example.campominado`

```
data/local       → AppDatabase, entity/ (JogadorEntity, PartidaEntity), dao/ (JogadorDao, PartidaDao)
data/remote      → acesso ao Firestore (coleção "niveis")
data/repository  → JogoRepository: única ponte entre ViewModels e Room/Firestore, com try-catch
logic            → regras do jogo em Kotlin puro (gerar mapa, contar vizinhos, cascata, vitória)
ui/navigation    → NavHost e Bottom Navigation (diferente para cada perfil)
ui/screens       → telas Compose
ui/viewmodel     → SessaoViewModel, JogoViewModel, HistoricoViewModel, AdminViewModel + ViewModelFactory
ui/theme         → cores e tema
```

Room:
- `JogadorEntity` (id, nome, nivelMaximoDesbloqueado, dataCriacao): CRUD completo
  (inserir no Login, listar via Flow, atualizar ao renomear/desbloquear nível, excluir perfil).
- `PartidaEntity` (id, jogadorId FK com CASCADE, nivel, tamanhoMapa, totalGalhos, casasAbertas,
  venceu, tempoSegundos, data): base do Histórico e dos Recordes.

Firestore: coleção `niveis` com a configuração dos 15 níveis. O Jogador lê; o Admin edita e ativa/desativa.
O Admin não exclui níveis (são sempre 15). Sem internet, o Repository usa os valores padrão da tabela acima
e o erro aparece no UiState.

## Telas (planejamento visual aprovado)

1. Login — escolhe Jogador (nome / jogadores salvos) ou Administrador (usuário + senha simulados)
2. Níveis — LazyVerticalGrid com os 15 níveis agrupados por tamanho de mapa, bloqueio e melhor tempo
3. Jogo — mapa N×N, galhos restantes, cronômetro, botão Abrir/Placa
4. Resultado — vitória ou derrota, mapa revelado, jogar de novo / próximo nível
5. Recordes — melhores tempos filtrados por tamanho de mapa e ordenados por tempo; Admin pode excluir
6. Histórico e Perfil — partidas do jogador (filtro vitórias/derrotas, excluir), renomear e excluir perfil
7. Painel do Admin — níveis vindos do Firestore, filtro por tamanho de mapa
8. Editar nível (Admin) — ajustar galhos e ativo, salvar no Firestore

Bottom Navigation — Jogador: Níveis, Recordes, Histórico. Admin: Níveis (Admin), Recordes.
Jogo e Resultado ficam fora da barra.

Cores: verde da praça #2E5E3A, verde claro #DCE8D6, grama #6FA563, calçada #E8DFC9,
fundo #F2F4EC, texto #1D2A1F, alerta #B4541A, azul #2D5F8B. Tema claro.

## Situação atual

- Feito: todas as camadas (Room, Firestore, Repository, logic, ViewModels, navegação) e as 8 telas,
  já ligadas aos ViewModels. Tema claro e README atualizados.
- Feito também: Firebase configurado (`google-services.json` + plugin `com.google.gms.google-services`).
  `PartidaDao`/`JogoRepository` ganharam `atualizar` (CRUD completo nas duas entidades).
- Grupo de 3 pessoas (permitido pelo professor).
- Pendente: README com prints e vídeo (espaços já reservados), testar no emulador (com e sem internet,
  regras do Firestore), revisão para a defesa e entrega do link no Blackboard.
- Antes da entrega: apagar CLAUDE.md e DIVISAO.md do repositório.
