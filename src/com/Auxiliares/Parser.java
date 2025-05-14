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
  private Map<String, Tipo> tabelaSimbolos = new HashMap<>(); // Tabela de s�mbolos

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

    // S�mbolo/vari�vel inicial da gram�tica que ele vai produzir a regra
    resultado = SE();

    if (resultado && token.getTipo() != Token.EOF) {
      System.err.println("\nFim de arquivo esperado, token = " + token);
      resultado = false;
    }

    if (!resultado) {
      mensagemErro = "Token n�o esperado: " + token;
    }

    return resultado;
  }

  public String errorMessage() {
    return mensagemErro;
  }

  // Fun��o SE
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

  // Fun��o CHAVES
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

  // Fun��o comando
  private boolean comando() throws IOException, LexerException {
    boolean resultado;

    System.out.print("comando[ { ");

    resultado = match(Token.ID) && match(Token.AT) && LE() != Tipo.DESCONHECIDO;

    System.out.print(" ]comando");

    return resultado;
  }

  // Fun��o comandos (apenas para demonstrar a recurs�o para blocos)
  private boolean comandos() throws IOException, LexerException {
    return true;
  }

  // Fun��o LE (Lado esquerdo de uma express�o)
  private Tipo LE() throws IOException, LexerException {
    Tipo tipo = RE();

    if (match(Token.LOG, Token.AND) || match(Token.LOG, Token.OR)) {
      Tipo tipoDireito = LE();
      if (tipo != tipoDireito) {
        mensagemErro = "Opera��o l�gica entre tipos incompat�veis.";
        return Tipo.DESCONHECIDO;
      }
    }

    System.out.print(" ]le");
    return tipo;
  }

  // Fun��o RE (relacionais)
  private Tipo RE() throws IOException, LexerException {
    Tipo tipo = AE();

    if (match(Token.RELOP, Token.GT) || match(Token.RELOP, Token.GE) || match(Token.RELOP, Token.LT)
            || match(Token.RELOP, Token.LE) || match(Token.RELOP, Token.EQ)) {
      Tipo tipoDireito = RE();
      if (tipo != tipoDireito) {
        mensagemErro = "Opera��o relacional entre tipos incompat�veis.";
        return Tipo.DESCONHECIDO;
      }
    }

    System.out.print(" ]re");
    return tipo;
  }

  // Fun��o AE (Aditivos)
  private Tipo AE() throws IOException, LexerException {
    Tipo tipo = ME();

    if (match(Token.OP, Token.AD) || match(Token.OP, Token.SUB)) {
      Tipo tipoDireito = AE();
      if (tipo != tipoDireito) {
        mensagemErro = "Opera��o aditiva entre tipos incompat�veis.";
        return Tipo.DESCONHECIDO;
      }
    }

    System.out.print(" ]ae");
    return tipo;
  }

  // Fun��o ME (Multiplicativos)
  private Tipo ME() throws IOException, LexerException {
    Tipo tipo = UE();

    if (match(Token.OP, Token.MUL) || match(Token.OP, Token.DIV)) {
      Tipo tipoDireito = ME();
      if (tipo != tipoDireito) {
        mensagemErro = "Opera��o multiplicativa entre tipos incompat�veis.";
        return Tipo.DESCONHECIDO;
      }
    }

    System.out.print(" ]me");
    return tipo;
  }

  // Fun��o UE (Unit�rios)
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

  // Fun��o match (verifica se o token atual corresponde ao esperado)
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

  // Fun��o match com valor espec�ficoo
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

  // Fun��o que busca o tipo de uma vari�vel na tabela de s�mbolos
  private Tipo buscarTipoDoIdentificador(String nome) {
    return tabelaSimbolos.getOrDefault(nome, Tipo.DESCONHECIDO);
  }
}
