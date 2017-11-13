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
  "true"                          { System.out.println("found \"true\""); return symbol(boolsym.TRUE); }  
  "false"                         { System.out.println("found \"false\""); return symbol(boolsym.FALSE); }  
  "secret"                        { System.out.println("found secret "); return symbol(boolsym.SECRET); } 
  "init"                          { System.out.println("found init "); return symbol(boolsym.INIT); } 
  "fin"                           { System.out.println("found fin "); return symbol(boolsym.FIN); } 
  "empty"                         { System.out.println("found fin "); return symbol(boolsym.EMPTYSET); } 
  "("                             { System.out.println("found ("); return symbol(boolsym.LPAREN); }
  ")"                             { System.out.println("found )"); return symbol(boolsym.RPAREN); }
  "|"                             { System.out.println("found |"); return symbol(boolsym.PIPE); }
  "*"                             { System.out.println("found *"); return symbol(boolsym.MULTIPLY); }
  "+"                             { System.out.println("found +"); return symbol(boolsym.PLUS); }
  "-"                             { System.out.println("found -"); return symbol(boolsym.MINUS); }
  "%"                             { System.out.println("found %"); return symbol(boolsym.MODULUS); }
  "=="                            { System.out.println("found =="); return symbol(boolsym.DOUBLEEQUAL); }
  "<"                             { System.out.println("found <"); return symbol(boolsym.LESSTHAN); }
  "<="                            { System.out.println("found <="); return symbol(boolsym.LESSTHANEQUAL); }
  ">"                             { System.out.println("found >"); return symbol(boolsym.GREATERTHAN); }
  ">="                            { System.out.println("found >="); return symbol(boolsym.GREATERTHANEQUAL); }
  "!="                 		  { System.out.println("found !="); return symbol(boolsym.NOTEQUAL); }
  "&&"                 		  { System.out.println("found &&"); return symbol(boolsym.AND); }
  "||"                 		  { System.out.println("found ||"); return symbol(boolsym.OR); }
  "->"                		  { System.out.println("found ->"); return symbol(boolsym.IMPLIES); }
  "\\neg"                          { System.out.println("found not"); return symbol(boolsym.NOT); }
  "\\in"                           { System.out.println("found elementOf"); return symbol(boolsym.SETELEMENT); }
  "\\notin"                        { System.out.println("found notElementOf"); return symbol(boolsym.NOTSETELEMENT); }
  "\\subset"                       { System.out.println("found subset"); return symbol(boolsym.SUBSET); }
  "\\subseteq"                     { System.out.println("found subseteq"); return symbol(boolsym.PROPERSUBSET); }
  "\\cup"                          { System.out.println("found union"); return symbol(boolsym.UNION); }
  "\\cap"                          { System.out.println("found intersection"); return symbol(boolsym.INTERSECTION); }
  "\\complement"                   { System.out.println("found complement"); return symbol(boolsym.COMPLEMENT); }
  /* identifiers */ 
 {Identifier}                     { System.out.println("found identifier " + yytext()); return symbol(boolsym.AGENT_IDENTIFIER, yytext()); }

}


/* error fallback */
[^]|\n                             { throw new Error("Illegal character <"+
                                                    yytext()+ " at line " + yyline + " at column " + yycolumn + ">"); }
