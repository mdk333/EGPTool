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
  
  "true"                          { System.out.println("found \"true\""); return symbol(sym.TRUE); }  
  "false"                         { System.out.println("found \"false\""); return symbol(sym.FALSE); }  
  "agent"                         { System.out.println("found agent "); return symbol(sym.AGENT); }  // likewise, when we declare an agent 'type', we expect it to be followed by an identifier which is an agent identifier...
  "knows"                         { System.out.println("found knows "); return symbol(sym.KNOWS); }
  "disjunct"                      { System.out.println("found disjunct"); return symbol(sym.DISJUNCT); }
  "conjunct"                      { System.out.println("found conjunct"); return symbol(sym.CONJUNCT); }
  "call"                          { System.out.println("found call"); return symbol(sym.CALL); }
  "let"                           { System.out.println("found let"); return symbol(sym.LET); }
  "if"                            { System.out.println("found if "); return symbol(sym.IF); } 
  "secret"                        { System.out.println("found secret "); return symbol(sym.SECRET); } 
  "init"                          { System.out.println("found init "); return symbol(sym.INIT); } 
  "fin"                           { System.out.println("found fin "); return symbol(sym.FIN); } 
  "empty"                         { System.out.println("found fin "); return symbol(sym.EMPTYSET); } 
  "begin"                         { System.out.println("found begin "); return symbol(sym.BEGIN); }
  "end"                           { System.out.println("found end "); return symbol(sym.END); }
  "("                             { System.out.println("found ("); return symbol(sym.LPAREN); }
  ")"                             { System.out.println("found )"); return symbol(sym.RPAREN); }
  "{"                             { System.out.println("{ "); return symbol(sym.LBRACE); } 
  "}"                             { System.out.println("} "); return symbol(sym.RBRACE); } 
  ","                             { System.out.println("found , [comma]"); return symbol(sym.COMMA); }
  ";"                             { System.out.println("found ;"); return symbol(sym.SEMI); }  
  ":"                             { System.out.println("found :"); return symbol(sym.COLON); }  
  "|"                             { System.out.println("found |"); return symbol(sym.PIPE); }
  "*"                             { System.out.println("found *"); return symbol(sym.MULTIPLY); }
  "+"                             { System.out.println("found +"); return symbol(sym.PLUS); }
  "-"                             { System.out.println("found -"); return symbol(sym.MINUS); }
  "%"                             { System.out.println("found %"); return symbol(sym.MODULUS); }
  "=="                            { System.out.println("found =="); return symbol(sym.DOUBLEEQUAL); }
  "<"                             { System.out.println("found <"); return symbol(sym.LESSTHAN); }
  "<="                            { System.out.println("found <="); return symbol(sym.LESSTHANEQUAL); }
  ">"                             { System.out.println("found >"); return symbol(sym.GREATERTHAN); }
  ">="                            { System.out.println("found >="); return symbol(sym.GREATERTHANEQUAL); }
  "!="                 		  { System.out.println("found !="); return symbol(sym.NOTEQUAL); }
  "&&"                 		  { System.out.println("found &&"); return symbol(sym.AND); }
  "||"                 		  { System.out.println("found ||"); return symbol(sym.OR); }
  "->"                		  { System.out.println("found ->"); return symbol(sym.IMPLIES); }
  "\\neg"                          { System.out.println("found not"); return symbol(sym.NOT); }
  "\\in"                           { System.out.println("found elementOf"); return symbol(sym.SETELEMENT); }
  "\\notin"                        { System.out.println("found notElementOf"); return symbol(sym.NOTSETELEMENT); }
  "\\subset"                       { System.out.println("found subset"); return symbol(sym.SUBSET); }
  "\\subseteq"                     { System.out.println("found subseteq"); return symbol(sym.PROPERSUBSET); }
  "\\cup"                          { System.out.println("found union"); return symbol(sym.UNION); }
  "\\cap"                          { System.out.println("found intersection"); return symbol(sym.INTERSECTION); }
  "\\complement"                   { System.out.println("found complement"); return symbol(sym.COMPLEMENT); }
  /* identifiers */ 
 {Identifier}                     { System.out.println("found identifier " + yytext()); return symbol(sym.AGENT_IDENTIFIER, yytext()); }
 {DecIntegerLiteral}              { System.out.println("found Integer " + yytext()); return symbol(sym.INTEGER, new Integer(yytext())); }

}

/* error fallback */
[^]|\n                             { throw new Error("Illegal character <"+
                                                    yytext()+ " at line " + yyline + " at column " + yycolumn + ">"); }
