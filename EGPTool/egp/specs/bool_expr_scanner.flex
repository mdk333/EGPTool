
/**
 *
 * @author Maduka Attamah
 *
 * Copyright 2011-2015 Maduka Attamah
 *
 */
 
 
package kig.multithread;
import java_cup.runtime.*;


%%

%class BoolConditionScanner
%unicode
%cup
%line
%column

%{
  StringBuffer string = new StringBuffer();
    String holdIdentifier = "";
  private Symbol symbol(int type) {
    return new Symbol(type, yyline, yycolumn);
  }
  private Symbol symbol(int type, Object value) {
    return new Symbol(type, yyline, yycolumn, value);
  }
%}

LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]
WhiteSpace     = {LineTerminator} | [ \t\f]

/* comments */
Comment = {TraditionalComment} | {EndOfLineComment}

TraditionalComment   = "/*" [^*] ~"*/" | "/*" "*"+ "/"
EndOfLineComment     = "//" {InputCharacter}* {LineTerminator}


Identifier = 0 | [1-9][0-9]*  //identifier here is merely an integer
//DecIntegerLiteral = 0 | [1-9][0-9]*
//DecDoubleLiteral = 0 | 0 \. 0 | [1-9][0-9]* (\.[0-9]+)?

%%

/* keywords */


<YYINITIAL> {
 /* comments */
  {Comment}                      { /* ignore */ }
 
  /* whitespace */
  {WhiteSpace}                   { /* ignore */ }
  //we omit "knows" here since set_bool_expr is not expected to have a knows operator
  "true"                          { return symbol(boolsym.TRUE); }  
  "false"                         { return symbol(boolsym.FALSE); }  
  "secret"                        { return symbol(boolsym.SECRET); } 
  "init"                          { return symbol(boolsym.INIT); } 
  "fin"                           { return symbol(boolsym.FIN); } 
  "empty"                         { return symbol(boolsym.EMPTYSET); } 
  "("                             { return symbol(boolsym.LPAREN); }
  ")"                             { return symbol(boolsym.RPAREN); }
  "|"                             { return symbol(boolsym.PIPE); }
  "*"                             { return symbol(boolsym.MULTIPLY); }
  "+"                             { return symbol(boolsym.PLUS); }
  "-"                             { return symbol(boolsym.MINUS); }
  "%"                             { return symbol(boolsym.MODULUS); }
  "=="                            { return symbol(boolsym.DOUBLEEQUAL); }
  "<"                             { return symbol(boolsym.LESSTHAN); }
  "<="                            { return symbol(boolsym.LESSTHANEQUAL); }
  ">"                             { return symbol(boolsym.GREATERTHAN); }
  ">="                            { return symbol(boolsym.GREATERTHANEQUAL); }
  "!="                 		  { return symbol(boolsym.NOTEQUAL); }
  "&&"                 		  { return symbol(boolsym.AND); }
  "||"                 		  { return symbol(boolsym.OR); }
  "->"                		  { return symbol(boolsym.IMPLIES); }
  "\\neg"                          { return symbol(boolsym.NOT); }
  "\\in"                           { return symbol(boolsym.SETELEMENT); }
  "\\notin"                        { return symbol(boolsym.NOTSETELEMENT); }
  "\\subset"                       { return symbol(boolsym.SUBSET); }
  "\\subseteq"                     { return symbol(boolsym.PROPERSUBSET); }
  "\\cup"                          { return symbol(boolsym.UNION); }
  "\\cap"                          { return symbol(boolsym.INTERSECTION); }
  "\\complement"                   { return symbol(boolsym.COMPLEMENT); }
  /* identifiers */ 
 {Identifier}                     { return symbol(boolsym.AGENT_IDENTIFIER, yytext()); }

}


/* error fallback */
[^]|\n                             { throw new Error("Illegal character <"+
                                                    yytext()+ " at line " + yyline + " at column " + yycolumn + ">"); }
