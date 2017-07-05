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
  "knows"                         { return symbol(expccsym.KNOWS); }
  "true"                          { return symbol(expccsym.TRUE); }  
  "false"                         { return symbol(expccsym.FALSE); }  
  "secret"                        { return symbol(expccsym.SECRET); } 
  "init"                          { return symbol(expccsym.INIT); } 
  "fin"                           { return symbol(expccsym.FIN); } 
  "empty"                         { return symbol(expccsym.EMPTYSET); } 
  "("                             { return symbol(expccsym.LPAREN); }
  ")"                             { return symbol(expccsym.RPAREN); }
  "|"                             { return symbol(expccsym.PIPE); }
  "*"                             { return symbol(expccsym.MULTIPLY); }
  "+"                             { return symbol(expccsym.PLUS); }
  "-"                             { return symbol(expccsym.MINUS); }
  "%"                             { return symbol(expccsym.MODULUS); }
  "=="                            { return symbol(expccsym.DOUBLEEQUAL); }
  "<"                             { return symbol(expccsym.LESSTHAN); }
  "<="                            { return symbol(expccsym.LESSTHANEQUAL); }
  ">"                             { return symbol(expccsym.GREATERTHAN); }
  ">="                            { return symbol(expccsym.GREATERTHANEQUAL); }
  "!="                 		  { return symbol(expccsym.NOTEQUAL); }
  "&&"                 		  { return symbol(expccsym.AND); }
  "||"                 		  { return symbol(expccsym.OR); }
  "->"                		  { return symbol(expccsym.IMPLIES); }
  "\\neg"                          { return symbol(expccsym.NOT); }
  "\\in"                           { return symbol(expccsym.SETELEMENT); }
  "\\notin"                        { return symbol(expccsym.NOTSETELEMENT); }
  "\\subset"                       { return symbol(expccsym.SUBSET); }
  "\\subseteq"                     { return symbol(expccsym.PROPERSUBSET); }
  "\\cup"                          { return symbol(expccsym.UNION); }
  "\\cap"                          { return symbol(expccsym.INTERSECTION); }
  "\\complement"                   { return symbol(expccsym.COMPLEMENT); }
  /* identifiers */ 
 {Identifier}                     {  return symbol(expccsym.AGENT_IDENTIFIER, yytext()); }
}

/* error fallback */
[^]|\n                             { throw new Error("Illegal character <"+
                                                    yytext()+ " at line " + yyline + " at column " + yycolumn + ">"); }
