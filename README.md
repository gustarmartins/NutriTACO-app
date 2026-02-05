# 🥗 NutriTACO [![License](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0) [![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/) [![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)

**Aplicativo Android com planejamento de dietas e consulta nutricional para brasileiros.**

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Download (Beta)](https://img.shields.io/badge/Download%20(Beta)-FF69B4?style=flat-square&logo=android&logoColor=white)](https://github.com/gustarmartins/NutriTACO-app/releases/latest)


> [!IMPORTANT]
> O projeto NutriTACO foi construído por mim de forma voluntária, é distribuído "as-is" (como
> está), e se tornou possível graças aos dados da Tabela Brasileira de Composição de Alimentos (
> TACO).
>
> Todos os dados do arquivo `@taco_preload.sql` são fiéis à mesma.
>
> Para saber mais sobre a importância e a metodologia da Tabela TACO, consulte:
>
> - [Sobre o Projeto TACO](https://www.nepa.unicamp.br/taco/)
> - [Download da Tabela (PDF - 4ª edição)](https://nepa.unicamp.br/wp-content/uploads/sites/27/2023/10/taco_4_edicao_ampliada_e_revisada.pdf)

---
[Sobre](#sobre) • [Funcionalidades](#funcionalidades) • [Tecnologias](#tecnologias) • [TACO](#a-tabela-taco) • [Licença](#licença)

## Sobre

O NutriTACO é um aplicativo nativo para a plataforma Android (em breve disponível na Play Store)
feito **por um brasileiro, para brasileiros.**

O objetivo é garantir uma interface moderna, sem
anúncios e bastante funcional para atender ao objetivo proposto - o planejamento de dietas e
consulta rápida de alimentos, garantindo a completa confiabilidade dos dados disponibilizados.

---

## Funcionalidades

O aplicativo está em processo de desenvolvimento. Atualmente, as seguintes funcionalidades estão
implementadas, com muito mais a vir no futuro:

| Funcionalidade              | Descrição                                                                                                                              |
|-----------------------------|----------------------------------------------------------------------------------------------------------------------------------------|
| 🔎 **Busca Inteligente**    | Pesquise todos os alimentos da TACO com FTS (Full-Text Search), sinônimos e stemming.                                                  |
| 📊 **Dashboards Dinâmico**  | Visualize os macronutrientes da sua dieta com gráficos interativos e cartões modernos e elegantes.                                     |
| 🍽️ **Criação de Dietas**   | Monte planos alimentares personalizados com metas calóricas baseadas no seu perfil.                                                    
| **Personalize Alimentos**   | Não se limite aos dados pré-existentes. Você pode criar e modificar alimentos conforme desejado.                                       |
| **Tela de Alimentos**       | Veja e ordene todos os alimentos pré-existentes e customizados por seus valores nutricionais ou ordem alfabética em uma tela dedicada. |
| 📝 **Diário Alimentar**     | Registre o que você consumiu ao longo do dia.                                                                                          |
| 📷 **Scanner de Alimentos** | Use a câmera para escanear rótulos e tabelas nutricionais com OCR + IA. (Necessita configurar a sua chave API no momento.)             |
| 🌙 **Tema Escuro**          | Suporte completo ao tema escuro e cores apropriadas para um aplicativo de nutrição.                                                    |

---

## Tecnologias

Apesar de ser um projeto (pt-BR), a base de código foi refatorada para
**Inglês** (entidades, variáveis, comentários). Essa decisão segue o padrão da indústria e facilita
a colaboração open-source global, permitindo que desenvolvedores de qualquer lugar ajudem a manter o
projeto.

O projeto utiliza o que há de mais moderno no desenvolvimento Android, como:

| Categoria                  | Tecnologia                                                                           |
|----------------------------|--------------------------------------------------------------------------------------|
| **Linguagem**              | [Kotlin](https://kotlinlang.org/)                                                    |
| **UI**                     | [Jetpack Compose](https://developer.android.com/jetpack/compose) + Material Design 3 |
| **Arquitetura**            | MVVM + Clean Architecture                                                            |
| **Banco de Dados**         | [Room](https://developer.android.com/training/data-storage/room) (SQLite) com FTS4   |
| **IA - OCR**               | [ML Kit](https://developers.google.com/ml-kit) (Text Recognition)                    |
| **IA - LLM**               | [Vertex AI (Firebase)](https://firebase.google.com/docs/vertex-ai)                   |
| **Injeção de Dependência** | [Dagger Hilt](https://dagger.dev/hilt/)                                              |

---

## A Tabela TACO

Este projeto tem como diferencial o uso exclusivo da **Tabela Brasileira de Composição de
Alimentos (TACO)** como sua base de verdade.

A Tabela TACO é um projeto de pesquisa amplo, coordenado pelo **Núcleo de Estudos e Pesquisas em
Alimentação (NEPA) da UNICAMP**. Fornece dados detalhados sobre a composição química e
nutricional dos principais alimentos consumidos no país, garantindo informações precisas
e relevantes para a nossa população.

> * Tabela Brasileira de Composição de Alimentos (TACO). 4. ed. rev. e ampl. Campinas: NEPA-UNICAMP,
    2011. 161 p.

## Licença

Este projeto está sob a licença **GPLv3**.
Isso significa que você é livre para usar, estudar, modificar e distribuir este software, desde que
mantenha o espírito gratuito, voluntário e de código aberto do NutriTACO.

Consulte o arquivo `LICENSE` para mais detalhes.
