# Divisão de tarefas — Campo Minado (A1)

Cada pessoa faz os commits da própria parte, no formato `tipo-descricao_com_underscores`.
Antes de começar e antes de cada push: `git pull`. Cada um mexe só nos arquivos da sua parte.

## Pessoa 1 — núcleo do app (dupla: integrante + Claude)

| # | Tarefa | Commit sugerido | Status |
|---|---|---|---|
| 0 | Camada `logic` (regras do jogo) e `data.repository` | `feat-adicionar_regras_do_jogo`, `feat-adicionar_jogo_repository` | feito |
| 1 | Firestore: dependências e `data.remote` | `feat-adicionar_firestore_niveis` | em andamento |
| 2 | Ligar o Firestore ao `JogoRepository` (fallback `NiveisPadrao`) | `feat-integrar_firestore_no_repository` | |
| 3 | `SessaoViewModel` e `JogoViewModel` com `UiState` | `feat-adicionar_viewmodels_sessao_e_jogo` | |
| 4 | `HistoricoViewModel`, `AdminViewModel` e `ViewModelFactory` | `feat-adicionar_viewmodels_historico_e_admin` | |
| 5 | Tela do Jogo (mapa N×N, cronômetro, Abrir/Placa, toque longo) | `feat-adicionar_tela_jogo` | |
| 6 | Navegação: NavHost e Bottom Bar por perfil | `feat-adicionar_navegacao` | |
| 7 | Painel do Admin e Editar nível | `feat-adicionar_telas_admin` | |
| 8 | Ligar todas as telas aos ViewModels | `feat-integrar_telas_aos_viewmodels` | |

## Pessoa 2 — visual e documentação

| # | Tarefa | Commit sugerido |
|---|---|---|
| 1 | Tema claro com as cores do CLAUDE.md (`ui/theme`) | `feat-aplicar_tema_claro` |
| 2 | Atualizar o README para a mecânica nova (galhos, placas, 15 níveis) | `docs-atualizar_readme` |
| 3 | Tela de Login, só o visual (nome, lista de jogadores, campos do Admin) | `feat-adicionar_tela_login` |
| 4 | Tela de Resultado, só o visual (vitória/derrota, tempo, botões) | `feat-adicionar_tela_resultado` |

## Pessoa 3 — telas de lista

| # | Tarefa | Commit sugerido |
|---|---|---|
| 1 | Tela de Níveis: `LazyVerticalGrid` com 15 cartões (número, cadeado, melhor tempo) | `feat-adicionar_tela_niveis` |
| 2 | Tela de Recordes: `LazyColumn` com posição, jogador e tempo | `feat-adicionar_tela_recordes` |
| 3 | Tela de Histórico e Perfil: `LazyColumn` de partidas, renomear e excluir | `feat-adicionar_tela_historico` |
| 4 | Testes do `NiveisPadrao` (15 níveis, tamanhos, limite de 25%) | `test-adicionar_testes_niveis_padrao` |

## Regras para as Pessoas 2 e 3

- Cada tela é uma função `@Composable` em `ui/screens` que recebe dados e cliques por parâmetro,
  por exemplo `fun TelaRecordes(recordes: List<...>, onVoltar: () -> Unit)`.
- Cada tela tem um `@Preview` com dados falsos, para testar sem rodar o app.
- Não mexer em `data`, `logic` nem `ui/viewmodel`.
- Cores e requisitos: ver o `CLAUDE.md`.

## Ordem

1. Pessoas 2 e 3 começam já, em paralelo (nada depende de ninguém).
2. A Pessoa 1 faz os itens 1 a 4 enquanto isso.
3. As telas e os ViewModels se encontram no item 8 da Pessoa 1.
