(ns cljfun.editor.grammar.hiccup)

;; Hiccup's grammar is a subset of edn
(def grammar
   "mixed-hiccup = [mixed-string] hiccup [mixed-string]
   hiccup = l-sq-paren [space] keyword [space] [attributes] [space] children [space] r-sq-paren [space]
   <number> = #'[0-9]+'
   <alphabet> = #'[a-zA-Z]+'
   <alphanumeric> = alphabet ([number] | [alphabet])+
   <space>  = <#'[, \t\r\n]*'>
   <string-space> = #'[, \t\r\n]'
   <l-sq-paren> = <'['>
   <r-sq-paren> = <']'>
   keyword = <':'> alphanumeric
   <attributes> = map
   <key-value> = [space] keyword space string [space]
   <nested-map> = [space] keyword space map
   map = <'{'> (nested-map | key-value)* <'}'>
   <children> = (hiccup* | string)
   <special-characters> = ('!' | '#' | '$' | '.' | ',' | ':' | '/' | '<' | '>' | '{' | '}')
   <character> = ( alphabet | number | string-space | special-characters )
   mixed-string = character-sequence
   <character-sequence> = character*
   string = <'\"'> character-sequence <'\"'>
 ")

;; For reference, see the grammar from
;; https://github.com/bjeanes/gojure/blob/master/edn/edn.ebnf
(def g
  "EDN ::= Whitespace (Comment | [Whitespace] [Tag Whitespace] (List | Vector | Map | Set | String | Number | Keyword | Symbol | Nil | Boolean | Char) Whitespace [Comment])

   Nil ::= 'nil'
   True ::= 'true'
   False ::= 'false'
   Boolean ::= True | False
   Symbol ::= (Namespace '/')? ('/' | (Alphabetic Alphanumeric*)? (('-' | '.' | '+')? Alphabetic | ('*' | '!' | '_' | '?' | '$' | '%' | '&' | '=')) (Alphanumeric | '#' | ':')*)
   Keyword ::= ':' Symbol

   Whitespace ::= (Space | Tab | Comma)*
   Space ::= ' '
   Tab ::= '\t'
   Comma ::= ','

   List ::= '(' EDN* ')'
   Vector ::= '[' EDN* ']'
   Map ::= '{' (EDN EDN)* '}'
   Set ::= '#{' EDN* '}'
   String ::= '\"' Character* '\"'
   Character ::= '\\' (Alphabetic | 'newline' | 'tab' | 'return' | 'space')

   NonZeroDigit ::= '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9'
   ZeroDigit ::= '0'
   Digit ::= ZeroDigit | NonZeroDigit
   Digits ::= ZeroDigit | (NonZeroDigit Digit*)
   Integer ::= Digits 'N'?
   Float ::= Digits ('M' | (Digits (Fraction | (Fraction? Exponent)) 'M'?))
   Fraction ::= Digit+
   Exponent ::= ('e' | 'E') Sign? Digits
   Sign ::= '+' | '-'
   SemiColon ::= '#'
   Number ::= Sign? (Integer | Float)

   Comment ::= SemiColon Character* NewLine
   NewLine ::= '\r\n' | '\n' | '\r'
   UpperAlphabetic ::= 'A' | 'B' | 'C' | 'D' | 'E' | 'F' | 'G' | 'H' | 'I' | 'J' | 'K' | 'L' | 'M' | 'N' | 'O' | 'P' | 'Q' | 'R' | 'S' | 'T' | 'U' | 'V' | 'W' | 'X' | 'Y' | 'Z'
   LowerAlphabetic ::= 'a' | 'b' | 'c' | 'd' | 'e' | 'f' | 'g' | 'h' | 'i' | 'j' | 'k' | 'l' | 'm' | 'n' | 'o' | 'p' | 'q' | 'r' | 's' | 't' | 'u' | 'v' | 'w' | 'x' | 'y' | 'z'
   Alphabetic ::= UpperAlphabetic | LowerAlphabetic
   Namespace ::= LowerAlphabetic+ (LowerAlphabetic+)*
   Tag ::= '#' (Namespace ('/') Alphabetic+")
