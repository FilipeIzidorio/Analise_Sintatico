﻿# Analise_Sintatico
✅ Parte 1: Análise dos Erros Presentes no Parser

Aqui estão os principais problemas ou lacunas no código atual:
🔴 1. Não há análise semântica (ex: verificação de tipos)

O parser apenas verifica a sintaxe, mas não impede construções como true + 5, pois qualquer token ID ou LITERALNUMERICO é aceito em expressões aritméticas.
🔴 2. Método comandos() está vazio

private boolean comandos() throws IOException, LexerException {
  return true;
}

Isso ignora completamente os blocos de comandos, como:

if (x > 10) {
  y = x + 1;
  z = y - 2;
}

Esse trecho seria aceito sem análise real.
🔴 3. Método comando() imprime fechamento incorreto

System.out.print("comando[ { ");
...
System.out.print(" ]CHAVES");

O método se chama comando, mas imprime ]CHAVES. Isso é apenas estético, mas confuso para debugging.
🔴 4. match(Token.ID) aceita qualquer identificador

Aceitar um ID sem verificar se ele está declarado, tipado ou com valor adequado é permissivo demais para uma linguagem minimamente tipada.
🔴 5. Recursão à esquerda pode causar problemas

Nas chamadas como:

if( match(Token.OP, Token.AD) || match(Token.OP, Token.SUB) ) {
  resultado = AE();
}

Você está indiretamente criando recursão à esquerda, que em parsers recursivos descendentes pode causar loop infinito em gramáticas mais ambíguas (não é erro direto, mas uma limitação de design).
✅ Parte 2: Adaptação com Verificações Semânticas (Tradução Dirigida)
🎯 Objetivo: impedir expressões malformadas como:

    true + 5

    "string" / false

✅ Solução: Implementar Tradução Dirigida por Sintaxe com Retornos de Tipo
🔧 1. Modifique os métodos de parsing para retornarem tipos

Em vez de boolean, use um TipoEnum ou String representando o tipo da expressão avaliada:

enum Tipo {
  INTEIRO, BOOLEANO, DESCONHECIDO
}

✏️ Exemplo: Adaptar o método UE() com retorno de tipo

private Tipo UE() throws IOException, LexerException {
  Tipo tipo;

  System.out.print("UE[ ");

  if( match(Token.PONTUACAO, Token.AP) ) {
    tipo = LE();
    if (!match(Token.PONTUACAO, Token.FP)) return Tipo.DESCONHECIDO;
    
  } else if( match(Token.LITERALNUMERICO) ) {
    tipo = Tipo.INTEIRO;

  } else if( match(Token.ID) ) {
    // Aqui você buscaria o tipo da variável na tabela de símbolos
    tipo = buscarTipoDoIdentificador(token.getValor().toString());

  } else if( match(Token.OP, Token.AD) || match(Token.OP, Token.SUB) || match(Token.LOG, Token.NOT) ) {
    tipo = UE();

  } else {
    tipo = Tipo.DESCONHECIDO;
  }

  System.out.print(" ]ue ");
  return tipo;
}

✏️ Adapte o método ME() para validação de tipos

private Tipo ME() throws IOException, LexerException {
  System.out.print("ME[ ");

  Tipo tipoEsquerdo = UE();
  if (tipoEsquerdo == Tipo.DESCONHECIDO) return Tipo.DESCONHECIDO;

  if (match(Token.OP, Token.MUL) || match(Token.OP, Token.DIV)) {
    Tipo tipoDireito = ME();
    if (tipoDireito != Tipo.INTEIRO || tipoEsquerdo != Tipo.INTEIRO) {
      mensagemErro = "Operação aritmética entre tipos incompatíveis.";
      return Tipo.DESCONHECIDO;
    }
    return Tipo.INTEIRO;
  }

  System.out.print(" ]me ");
  return tipoEsquerdo;
}

🔍 2. Tabela de símbolos para variáveis

Crie uma classe simples para armazenar os identificadores e seus tipos:

Map<String, Tipo> tabelaSimbolos = new HashMap<>();

private Tipo buscarTipoDoIdentificador(String nome) {
  return tabelaSimbolos.getOrDefault(nome, Tipo.DESCONHECIDO);
}

🔐 3. Validação em atribuições

Dentro de comando(), verifique se o tipo do lado direito é compatível com o tipo da variável:

Tipo tipoVariavel = buscarTipoDoIdentificador(nomeVariavel);
Tipo tipoExpressao = LE();

if (tipoVariavel != tipoExpressao) {
  mensagemErro = "Tipos incompatíveis na atribuição.";
  return false;
}

✅ Conclusão e Reforço
🛠 Erros Corrigidos e Melhorias Sugeridas:
Item	Descrição
❌	Falta de verificação semântica para tipos
❌	Métodos como comandos() e comando() incompletos
❌	Impressões inconsistentes nos logs (ex: ]CHAVES)
✅	Sugestão de retornar tipos (Tipo) ao invés de boolean
✅	Implementação de uma tabela de símbolos para variáveis
✅	Adição de ações semânticas em expressões para prevenir erros de tipo
