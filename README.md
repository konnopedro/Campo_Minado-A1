# Campo Minado — Praça Osório 🌳

Aplicativo Android desenvolvido em Kotlin com Jetpack Compose, inspirado no Campo Minado tradicional e ambientado na Praça Osório, em Curitiba.

## Sobre o jogo

O objetivo é limpar o tabuleiro sem abrir nenhuma casa que esconda um galho. Cada nível apresenta um mapa quadrado; ao abrir uma casa, o jogador vê quantos galhos existem nas oito casas vizinhas.

- **15 níveis** com mapas de 6×6 até 10×10.
- **Galhos** ocupam o lugar das minas do Campo Minado.
- **Placas de aviso** funcionam como bandeiras: podem ser colocadas pelo botão Abrir / Placa ou com um toque longo.
- **Primeiro toque seguro:** os galhos são distribuídos depois da primeira jogada, sem ocupar a casa escolhida nem suas vizinhas.
- **Abertura em cascata:** casas sem galhos ao redor abrem automaticamente as casas vizinhas.
- **Sem vidas:** abrir um galho encerra a partida.
- **Vitória:** abrir todas as casas que não contêm galhos.
- **Progressão:** vencer um nível desbloqueia o próximo.

## Níveis

| Níveis | Tamanho do mapa | Quantidade de galhos |
|:------:|:---------------:|:--------------------:|
| 1–3 | 6×6 | 4, 5 e 6 |
| 4–6 | 7×7 | 7, 8 e 9 |
| 7–9 | 8×8 | 10, 11 e 12 |
| 10–12 | 9×9 | 13, 14 e 15 |
| 13–15 | 10×10 | 16, 18 e 20 |

A configuração padrão dos galhos pode ser ajustada pelo administrador, respeitando o limite de 25% das casas do tabuleiro. O tamanho de cada mapa é fixo.

## Perfis e recursos

- **Jogador:** seleciona seu perfil, joga os níveis desbloqueados, consulta recordes e histórico de partidas.
- **Administrador:** acessa o painel de níveis e pode ajustar a quantidade de galhos e o estado de ativação de cada nível.
- **Recordes:** melhor tempo por nível entre os jogadores cadastrados no aparelho.
- **Histórico:** consulta os resultados das partidas.

O jogo foi planejado para funcionar offline com os níveis padrão quando não houver conexão com o Firestore.

## Tecnologias

- Kotlin
- Android e Jetpack Compose
- Material 3
- Navigation Compose
- Room para persistência local
- Firebase Firestore para configuração remota dos níveis
- Arquitetura MVVM com Repository

## Abrir o projeto

1. Clone este repositório.
2. Abra a pasta do projeto no Android Studio.
3. Aguarde a sincronização do Gradle.
4. Execute o aplicativo em um emulador ou dispositivo Android.

## Organização do código

- `logic`: regras e mecânica do Campo Minado.
- `data/local`: persistência local com Room.
- `data/remote`: comunicação com o Firestore.
- `data/repository`: acesso aos dados para as demais camadas.
- `ui/screens`: telas do aplicativo.
- `ui/theme`: tema e identidade visual.
- `ui/viewmodel`: estados de tela e lógica de apresentação.
