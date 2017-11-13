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

%class ExpandedCallConditionScanner
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


Identifier = 0 | [1-9][0-9]*
//DecIntegerLiteral = 0 | [1-9][0-9]*
//DecDoubleLiteral = 0 | 0 \. 0 | [1-9][0-9]* (\.[0-9]+)?

%%

/* keywords */


<YYINITIAL> {
 /* comments */
  {Comment}                      { /* ignore */ }
 
  /* whitespace */
  {WhiteSpace}                   { /* ignore */ }
  "knows"                         { System.out.println("found knows "); return symbol(expccsym.KNOWS); }
  "true"                          { System.out.println("found \"true\""); return symbol(expccsym.TRUE); }  
  "false"                         { System.out.println("found \"false\""); return symbol(expccsym.FALSE); }  
  "secret"                        { System.out.println("found secret "); return symbol(expccsym.SECRET); } 
  "init"                          { System.out.println("found init "); return symbol(expccsym.INIT); } 
  "fin"                           { System.out.println("found fin "); return symbol(expccsym.FIN); } 
  "empty"                         { System.out.println("found fin "); return symbol(expccsym.EMPTYSET); } 
  "("                             { System.out.println("found ("); return symbol(expccsym.LPAREN); }
  ")"                             { System.out.println("found )"); return symbol(expccsym.RPAREN); }
  "|"                             { System.out.println("found |"); return symbol(expccsym.PIPE); }
  "*"                             { System.out.println("found *"); return symbol(expccsym.MULTIPLY); }
  "+"                             { System.out.println("found +"); return symbol(expccsym.PLUS); }
  "-"                             { System.out.println("found -"); return symbol(expccsym.MINUS); }
  "%"                             { System.out.println("found %"); return symbol(expccsym.MODULUS); }
  "=="                            { System.out.println("found =="); return symbol(expccsym.DOUBLEEQUAL); }
  "<"                             { System.out.println("found <"); return symbol(expccsym.LESSTHAN); }
  "<="                            { System.out.println("found <="); return symbol(expccsym.LESSTHANEQUAL); }
  ">"                             { System.out.println("found >"); return symbol(expccsym.GREATERTHAN); }
  ">="                            { System.out.println("found >="); return symbol(expccsym.GREATERTHANEQUAL); }
  "!="                 		  { System.out.println("found !="); return symbol(expccsym.NOTEQUAL); }
  "&&"                 		  { System.out.println("found &&"); return symbol(expccsym.AND); }
  "||"                 		  { System.out.println("found ||"); return symbol(expccsym.OR); }
  "->"                		  { System.out.println("found ->"); return symbol(expccsym.IMPLIES); }
  "\\neg"                          { System.out.println("found not"); return symbol(expccsym.NOT); }
  "\\in"                           { System.out.println("found elementOf"); return symbol(expccsym.SETELEMENT); }
  "\\notin"                        { System.out.println("found notElementOf"); return symbol(expccsym.NOTSETELEMENT); }
  "\\subset"                       { System.out.println("found subset"); return symbol(expccsym.SUBSET); }
  "\\subseteq"                     { System.out.println("found subseteq"); return symbol(expccsym.PROPERSUBSET); }
  "\\cup"                          { System.out.println("found union"); return symbol(expccsym.UNION); }
  "\\cap"                          { System.out.println("found intersection"); return symbol(expccsym.INTERSECTION); }
  "\\complement"                   { System.out.println("found complement"); return symbol(expccsym.COMPLEMENT); }
  /* identifiers */ 
 {Identifier}                     { System.out.println("found identifier " + yytext());  return symbol(expccsym.AGENT_IDENTIFIER, yytext()); }

}

/* error fallback */
[^]|\n                             { throw new Error("Illegal character <"+
                                                    yytext()+ " at line " + yyline + " at column " + yycolumn + ">"); }
