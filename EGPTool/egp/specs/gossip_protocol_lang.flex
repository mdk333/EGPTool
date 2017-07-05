package kig.multithread;
import java_cup.runtime.*;


%%

%class ProtocolSpecScanner
%unicode
%cup
%line
%column

%{
  StringBuffer string = new StringBuffer();

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


Identifier = [:jletter:] [:jletterdigit:]*
DecIntegerLiteral = 0 | [1-9][0-9]*
//DecDoubleLiteral = 0 | 0 \. 0 | [1-9][0-9]* (\.[0-9]+)?

%%

/* keywords */


<YYINITIAL> {
 /* comments */
  {Comment}                      { /* ignore */ }
 
  /* whitespace */
  {WhiteSpace}                   { /* ignore */ }
  
  "true"                          { return symbol(sym.TRUE); }  
  "false"                         { return symbol(sym.FALSE); }  
  //"agent"                         { return symbol(sym.AGENT); }  // likewise, when we declare an agent 'type', we expect it to be followed by an identifier which is an agent identifier...
  "knows"                         { return symbol(sym.KNOWS); }
  "disjunct"                      { return symbol(sym.DISJUNCT); }
  "conjunct"                      { return symbol(sym.CONJUNCT); }
  "call"                          { return symbol(sym.CALL); }
  "let"                           { return symbol(sym.LET); }
  "if"                            { return symbol(sym.IF); } 
  "secret"                        { return symbol(sym.SECRET); } 
  "init"                          { return symbol(sym.INIT); } 
  "fin"                           { return symbol(sym.FIN); } 
  "empty"                         { return symbol(sym.EMPTYSET); } 
  "begin"                         { return symbol(sym.BEGIN); }
  "end"                           { return symbol(sym.END); }
  "topology"                      { return symbol(sym.TOPOLOGY); }
  "neighbour"                     { return symbol(sym.NEIGHBOUR); }
  "equivalence_notion"            { return symbol(sym.EQUIV_NOTION); }
  "="                             { return symbol(sym.ASSIGNMENT); }  
  "("                             { return symbol(sym.LPAREN); }
  ")"                             { return symbol(sym.RPAREN); }
  "{"                             { return symbol(sym.LBRACE); } 
  "}"                             { return symbol(sym.RBRACE); } 
  ","                             { return symbol(sym.COMMA); }
  ";"                             { return symbol(sym.SEMI); }  
  ":"                             { return symbol(sym.COLON); }  
  "|"                             { return symbol(sym.PIPE); }
  "*"                             { return symbol(sym.MULTIPLY); }
  "+"                             { return symbol(sym.PLUS); }
  "-"                             { return symbol(sym.MINUS); }
  "%"                             { return symbol(sym.MODULUS); }
  "=="                            { return symbol(sym.DOUBLEEQUAL); }
  "<"                             { return symbol(sym.LESSTHAN); }
  "<="                            { return symbol(sym.LESSTHANEQUAL); }
  ">"                             { return symbol(sym.GREATERTHAN); }
  ">="                            { return symbol(sym.GREATERTHANEQUAL); }
  "!="                 		  { return symbol(sym.NOTEQUAL); }
  "&&"                 		  { return symbol(sym.AND); }
  "||"                 		  { return symbol(sym.OR); }
  "->"                		  { return symbol(sym.IMPLIES); }
  "\\neg"                          { return symbol(sym.NOT); }
  "\\in"                           { return symbol(sym.SETELEMENT); }
  "\\notin"                        { return symbol(sym.NOTSETELEMENT); }
  "\\subset"                       { return symbol(sym.SUBSET); }
  "\\subseteq"                     { return symbol(sym.PROPERSUBSET); }
  "\\cup"                          { return symbol(sym.UNION); }
  "\\cap"                          { return symbol(sym.INTERSECTION); }
  "\\complement"                   { return symbol(sym.COMPLEMENT); }
  /* identifiers */ 
 {Identifier}                     { return symbol(sym.AGENT_IDENTIFIER, yytext()); }
 {DecIntegerLiteral}              { return symbol(sym.INTEGER, new Integer(yytext())); }

}

/* error fallback */
[^]|\n                             { throw new Error("Illegal character <"+
                                                    yytext()+ " at line " + yyline + " at column " + yycolumn + ">"); }
