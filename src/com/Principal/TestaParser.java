
package com.Principal;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import com.Auxiliares.AnalisadorLexico;
import com.Auxiliares.Parser;
import com.Erro.LexerException;

public class TestaParser {

	void nomeFuncao(){}
	
  public static void main(String[] args) {
		
    System.out.println( "Analise Sintática: \n\n" );
	
    try {
		
      BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( new FileInputStream( "Programa.lex" ) ) );

	  //scanner == analisador léxico
      AnalisadorLexico analisadorLexico = new AnalisadorLexico( bufferedReader );

	  //analisador sintático
	  Parser parser = new Parser( analisadorLexico );

	  if ( parser.parse() ) {
		
	    System.out.println( "\n\n\n\n Análise completa" );
	  
	  }	else {
		
	    System.err.println( "\n Erro sintático: " + parser.errorMessage() );
		
	  } 
	  
    }
	catch (LexerException e) {
			
	  System.err.println("Erro léxico: " + e.getMessage());
		
	}
	
	catch (Exception e) {
			
	  System.out.println("Exceção: " + e.getMessage());
		
	  e.printStackTrace();
		
	}

  }

}
