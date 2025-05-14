package com.Auxiliares;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.Erro.LexerException;

public class Parser {

  // Atributos
  private AnalisadorLexico analisadorLexico;
  private Token token;
  private String mensagemErro = "";
  private Map<String, Tipo> tabelaSimbolos = new HashMap<>(); // Tabela de símbolos

  // Enum para tipos de dados
  enum Tipo {
    INTEIRO, BOOLEANO, DESCONHECIDO
  }

  public Parser(AnalisadorLexico analisadorLexico) {
    this.analisadorLexico = analisadorLexico;
  }

  public boolean parse() throws IOException, LexerException {
    boolean resultado;

    token = analisadorLexico.pegarProximoToken();

    // Símbolo/variável inicial da gramática que ele vai produzir a regra
    resultado = SE();

    if (resultado && token.getTipo() != Token.EOF) {
      System.err.println("\nFim de arquivo esperado, token = " + token);
      resultado = false;
    }

    if (!resultado) {
      mensagemErro = "Token não esperado: " + token;
    }

    return resultado;
  }

  public String errorMessage() {
    return mensagemErro;
  }

  // Função SE
  private boolean SE() throws IOException, LexerException {
    boolean resultado = false;

    System.out.print("SE[ if ");

    if (match(Token.IF)) {
      if (match(Token.PONTUACAO, Token.AP)) {
        resultado = LE() != Tipo.DESCONHECIDO && match(Token.PONTUACAO, Token.FP);

        if (match(Token.PONTUACAO, Token.AC)) {
          resultado = comandos() && match(Token.PONTUACAO, Token.FC);
        }
      }
    }

    System.out.print(" ]se");
    return resultado;
  }

  // Função CHAVES
  private boolean CHAVES() throws IOException, LexerException {
    boolean resultado;

    System.out.print("Chaves[ { ");

    if (match(Token.PONTUACAO, Token.AC)) resultado = comandos() && match(Token.PONTUACAO, Token.FC);
    else if (comando()) {
      resultado = true;
    } else {
      resultado = false;
    }

    System.out.print(" ]CHAVES");

    return resultado;
  }

  // Função comando
  private boolean comando() throws IOException, LexerException {
    boolean resultado;

    System.out.print("comando[ { ");

    resultado = match(Token.ID) && match(Token.AT) && LE() != Tipo.DESCONHECIDO;

    System.out.print(" ]comando");

    return resultado;
  }

  // Função comandos (apenas para demonstrar a recursão para blocos)
  private boolean comandos() throws IOException, LexerException {
    return true;
  }

  // Função LE (Lado esquerdo de uma expressão)
  private Tipo LE() throws IOException, LexerException {
    Tipo tipo = RE();

    if (match(Token.LOG, Token.AND) || match(Token.LOG, Token.OR)) {
      Tipo tipoDireito = LE();
      if (tipo != tipoDireito) {
        mensagemErro = "Operação lógica entre tipos incompatíveis.";
        return Tipo.DESCONHECIDO;
      }
    }

    System.out.print(" ]le");
    return tipo;
  }

  // Função RE (relacionais)
  private Tipo RE() throws IOException, LexerException {
    Tipo tipo = AE();

    if (match(Token.RELOP, Token.GT) || match(Token.RELOP, Token.GE) || match(Token.RELOP, Token.LT)
            || match(Token.RELOP, Token.LE) || match(Token.RELOP, Token.EQ)) {
      Tipo tipoDireito = RE();
      if (tipo != tipoDireito) {
        mensagemErro = "Operação relacional entre tipos incompatíveis.";
        return Tipo.DESCONHECIDO;
      }
    }

    System.out.print(" ]re");
    return tipo;
  }

  // Função AE (Aditivos)
  private Tipo AE() throws IOException, LexerException {
    Tipo tipo = ME();

    if (match(Token.OP, Token.AD) || match(Token.OP, Token.SUB)) {
      Tipo tipoDireito = AE();
      if (tipo != tipoDireito) {
        mensagemErro = "Operação aditiva entre tipos incompatíveis.";
        return Tipo.DESCONHECIDO;
      }
    }

    System.out.print(" ]ae");
    return tipo;
  }

  // Função ME (Multiplicativos)
  private Tipo ME() throws IOException, LexerException {
    Tipo tipo = UE();

    if (match(Token.OP, Token.MUL) || match(Token.OP, Token.DIV)) {
      Tipo tipoDireito = ME();
      if (tipo != tipoDireito) {
        mensagemErro = "Operação multiplicativa entre tipos incompatíveis.";
        return Tipo.DESCONHECIDO;
      }
    }

    System.out.print(" ]me");
    return tipo;
  }

  // Função UE (Unitários)
  private Tipo UE() throws IOException, LexerException {
    Tipo tipo;

    System.out.print("UE[ ");

    if (match(Token.PONTUACAO, Token.AP)) {
      tipo = LE();
      if (!match(Token.PONTUACAO, Token.FP)) return Tipo.DESCONHECIDO;
    } else if (match(Token.LITERALNUMERICO)) {
      tipo = Tipo.INTEIRO;
    } else if (match(Token.ID)) {
      tipo = buscarTipoDoIdentificador(token.getValor().toString());
    } else if (match(Token.OP, Token.AD) || match(Token.OP, Token.SUB) || match(Token.LOG, Token.NOT)) {
      tipo = UE();
    } else {
      tipo = Tipo.DESCONHECIDO;
    }

    System.out.print(" ]ue");
    return tipo;
  }

  // Função match (verifica se o token atual corresponde ao esperado)
  private boolean match(int tipoToken) throws IOException, LexerException {
    boolean resultado;

    if (token.getTipo() == tipoToken) {
      if (tipoToken == Token.ID) {
        System.out.print("Id ");
      } else if (tipoToken == Token.LITERALNUMERICO) {
        System.out.print(token.getValor() + " ");
      }

      token = analisadorLexico.pegarProximoToken();
      resultado = true;
    } else {
      resultado = false;
    }

    return resultado;
  }

  // Função match com valor específicoo
  private boolean match(int tipoToken, int valorToken) throws IOException, LexerException {
    boolean resultado;

    if (token.getTipo() == tipoToken && (Integer) token.getValor() == valorToken) {
      switch (tipoToken) {
        case Token.LOG: {
          if (valorToken == Token.AND) {
            System.out.print("&& ");
          } else if (valorToken == Token.NOT) {
            System.out.print("! ");
          } else {
            System.out.print("|| ");
          }
        }
        break;

        case Token.OP: {
          switch (valorToken) {
            case Token.AD:
              System.out.print("+ ");
              break;
            case Token.SUB:
              System.out.print("- ");
              break;
            case Token.DIV:
              System.out.print("/ ");
              break;
            case Token.MUL:
              System.out.print("* ");
              break;
          }
        }
        break;

        case Token.PONTUACAO: {
          if (valorToken == Token.AP) {
            System.out.print("( ");
          } else if (valorToken == Token.FP) {
            System.out.print(") ");
          }
        }
        break;

        case Token.RELOP: {
          switch (valorToken) {
            case Token.GT:
              System.out.print("> ");
              break;
            case Token.GE:
              System.out.print(">= ");
              break;
            case Token.LT:
              System.out.print("< ");
              break;
            case Token.LE:
              System.out.print("<= ");
              break;
            case Token.EQ:
              System.out.print("== ");
              break;
          }
        }
        break;
      }

      token = analisadorLexico.pegarProximoToken();
      resultado = true;
    } else {
      resultado = false;
    }

    return resultado;
  }

  // Função que busca o tipo de uma variável na tabela de símbolos
  private Tipo buscarTipoDoIdentificador(String nome) {
    return tabelaSimbolos.getOrDefault(nome, Tipo.DESCONHECIDO);
  }
}
