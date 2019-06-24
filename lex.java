import java.util.Scanner;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Arrays;
import java.util.Queue;
import java.util.LinkedList;
import java.lang.Enum;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class lex {
  public static int row = 0; // lexer's horizontal location when scanning through the doc
  public static int col = 1; // lexer's vertical location when scanning through the doc
  public static Boolean global_inside_quotations = false; //used to determine if we are inside quotations. This means most things should be read as chars
  public static Boolean lexErrorsExist = false; // true if any errors are detected during lexical analysis
  public static char[][] arrayOfCharArrays = new char[255][255]; //255 is the max length of programs for our machine
  public static int elementsInArray =0; // counter for arrayOfcharArrays
  public static Queue<Token> TokenQueue = new LinkedList<>();
  public static Queue<Token> saTokenQueueClone = new LinkedList<>();
  public static Queue<String> cstQueue = new LinkedList<>();
  public static Queue<String> astQueue = new LinkedList<>();
  public static Queue<Integer> astDepthQueue = new LinkedList<>();
  public static Queue<Integer> astDepthQueueClone = new LinkedList<>();
  public enum Type {
    CHAR,
    L_BRACE,
    R_BRACE,
    EQUALS,
    EQUALITY,
    INEQUALITY,
    L_PAREN,
    R_PAREN,
    ADD,
    COMMENT,
    BOOLEAN,
    FALSE,
    IF,
    INT,
    INTEGER,
    PRINT,
    STRING,
    TRUE,
    WHILE,
    EOP,
    QUOTATION,
    ID,
    SPACE
  }

  public static class TokenType {
  Type type;
    public TokenType(Type type) {
      this.type = type;
    }
  }

  public static class Token {
    Type type;
    int row;
    int col;
    char character;

    Token() {
      type = null;
      row = 0;
      col = 0;
      character = ' ';
    }
  }

  public static final String ANSI_RESET = "\u001B[0m";
  public static final String ANSI_BLACK = "\u001B[30m";
  public static final String ANSI_RED = "\u001B[31m";
  public static final String ANSI_GREEN = "\u001B[32m";
  public static final String ANSI_YELLOW = "\u001B[33m";
  public static final String ANSI_BLUE = "\u001B[34m";
  public static final String ANSI_PURPLE = "\u001B[35m";
  public static final String ANSI_CYAN = "\u001B[36m";
  public static final String ANSI_WHITE = "\u001B[37m";

  public static int programCounter = 0;
  public static Scanner input = new Scanner(System.in);

  public static void lex(String[] args) {
    String line;
    // english method of cooresponidng array columns to their symbols. Hopefully will help preserve sanity.
    int start      = 1;
    int a          = 2;
    int b          = 3;
    int c          = 4;
    int d          = 5;
    int e          = 6;
    int f          = 7;
    int g          = 8;
    int h          = 9;
    int i          = 10;
    int j          = 11;
    int k          = 12;
    int l          = 13;
    int m          = 14;
    int n          = 15;
    int o          = 16;
    int p          = 17;
    int q          = 18;
    int r          = 19;
    int s          = 20;
    int t          = 21;
    int u          = 22;
    int v          = 23;
    int w          = 24;
    int x          = 25;
    int y          = 26;
    int z          = 27;
    int d0         = 28;   // d for digit
    int d1         = 29;
    int d2         = 30;
    int d3         = 31;
    int d4         = 32;
    int d5         = 33;
    int d6         = 34;
    int d7         = 35;
    int d8         = 36;
    int d9         = 37;
    int sBackslash = 38;  // s for symbol
    int sAsterisk  = 39;
    int sEquals    = 40;
    int sPlus      = 41;
    int sLParen    = 42;
    int sRParen    = 43;
    int sLBracket  = 44;
    int sRBracket  = 45;
    int sPeriod    = 46;
    int sExclam    = 47;
    int sEOP       = 48;
    int sSpace     = 49;
    int sNewline   = 50;
    int sQuotation = 51;

   int[][] transitionTable = new int[100][100];
   transitionTable[1][b] = 22;
   transitionTable[1][f] = 29;
   transitionTable[1][i] = 34;
   transitionTable[1][p] = 38;
   transitionTable[1][s] = 43;
   transitionTable[1][t] = 49;
   transitionTable[1][w] = 53;
   transitionTable[1][d0] = 5;
   transitionTable[1][d1] = 4;
   transitionTable[1][d2] = 4;
   transitionTable[1][d3] = 4;
   transitionTable[1][d4] = 4;
   transitionTable[1][d5] = 4;
   transitionTable[1][d6] = 4;
   transitionTable[1][d7] = 4;
   transitionTable[1][d8] = 4;
   transitionTable[1][d9] = 4;
   transitionTable[1][sBackslash] = 18;
   transitionTable[1][sEquals]    = 9;
   transitionTable[1][sPlus]      = 15;
   transitionTable[1][sLParen]    = 13;
   transitionTable[1][sRParen]    = 14;
   transitionTable[1][sLBracket]  = 7;
   transitionTable[1][sRBracket]  = 8;
   transitionTable[1][sExclam]    = 11;

   transitionTable[1][a] = 0;
   transitionTable[1][c] = 0;
   transitionTable[1][d] = 0;
   transitionTable[1][e] = 0;
   transitionTable[1][g] = 0;
   transitionTable[1][h] = 0;
   transitionTable[1][j] = 0;
   transitionTable[1][k] = 0;
   transitionTable[1][l] = 0;
   transitionTable[1][m] = 0;
   transitionTable[1][n] = 0;
   transitionTable[1][o] = 0;
   transitionTable[1][q] = 0;
   transitionTable[1][r] = 0;
   transitionTable[1][u] = 0;
   transitionTable[1][v] = 0;
   transitionTable[1][x] = 0;
   transitionTable[1][y] = 0;
   transitionTable[1][z] = 0;

   //boolean
   transitionTable[22][o] = 23;
   transitionTable[23][o] = 24;
   transitionTable[24][l] = 25;
   transitionTable[25][e] = 26;
   transitionTable[26][a] = 27;
   transitionTable[27][n] = 28;
   //false
   transitionTable[29][a] = 30;
   transitionTable[30][l] = 31;
   transitionTable[31][s] = 32;
   transitionTable[32][e] = 33;
   //if
   transitionTable[34][f] = 35;
   //int
   transitionTable[34][n] = 36;
   transitionTable[36][t] = 37;
   //print
   transitionTable[38][r] = 39;
   transitionTable[39][i] = 40;
   transitionTable[40][n] = 41;
   transitionTable[41][t] = 42;
   //string
   transitionTable[43][t] = 44;
   transitionTable[44][r] = 45;
   transitionTable[45][i] = 46;
   transitionTable[46][n] = 47;
   transitionTable[47][g] = 48;
   //true
   transitionTable[49][r] = 50;
   transitionTable[50][u] = 51;
   transitionTable[51][e] = 52;
   //while
   transitionTable[53][h] = 54;
   transitionTable[54][i] = 55;
   transitionTable[55][l] = 56;
   transitionTable[56][e] = 57;
   //int values
   transitionTable[1][d1] = 4;
   transitionTable[1][d2] = 4;
   transitionTable[1][d3] = 4;
   transitionTable[1][d4] = 4;
   transitionTable[1][d5] = 4;
   transitionTable[1][d6] = 4;
   transitionTable[1][d7] = 4;
   transitionTable[1][d8] = 4;
   transitionTable[1][d9] = 4;
   transitionTable[1][d0] = 4;
   transitionTable[4][d1] = 4;
   transitionTable[4][d2] = 4;
   transitionTable[4][d3] = 4;
   transitionTable[4][d4] = 4;
   transitionTable[4][d5] = 4;
   transitionTable[4][d6] = 4;
   transitionTable[4][d7] = 4;
   transitionTable[4][d8] = 4;
   transitionTable[4][d9] = 4;
   //decimal values (not needed?)
   //transitionTable[4][sPeriod] = 17;
   // SYMBOLS
   //equality op
   transitionTable[1][sEquals] = 9;
   //bool equality op
   transitionTable[9][sEquals] = 10;
   // need more!!!
   // add op
   transitionTable[1][sPlus] = 15;
   // parenthesis
   transitionTable[1][sLParen] = 13;
   transitionTable[1][sRParen] = 14;
   // brackey
   transitionTable[1][sLBracket] = 7;
   transitionTable[1][sRBracket] = 8;
   //inequal op
   transitionTable[1][sExclam] = 11;
   transitionTable[11][sEquals] = 12;
   // EOP
   transitionTable[1][sEOP] = 58;
   //comments
   transitionTable[1][sBackslash] = 18;
   transitionTable[18][sAsterisk] = 19;
   transitionTable[19][sAsterisk] = 20;
   transitionTable[20][sBackslash] = 21;
    // quotation marks
   transitionTable[1][sQuotation] = 59;

   if (input != null) {
     System.out.println("===================================================");
     System.out.println("TOKEN TYPE      TOKEN NAME                LOCATION");
     System.out.println("===================================================");
     programCounter++;
   }

   while(input.hasNext()) {
     line = input.nextLine().toLowerCase();
     line = line.trim(); // ignore leading & trailing spaces
     char lineCharArr[] = line.toCharArray();
     row++;
     col = 0;
     findKeywords(lineCharArr, col, transitionTable);
   }
   if (currentChar != '$') {
     System.out.println(ANSI_RED + "Warning, program was not ended with an EOP [$] symbol. Automatically adding EOP to token queue..." + ANSI_RESET);
     programCounter++;
     Token eopToken = new Token();
     eopToken.type = eopToken.type.EOP;
     eopToken.row = row;
     eopToken.col = col;
     global_inside_quotations = false;
     TokenQueue.add(eopToken);
     // now send program to see if itll parse
     checkForAcceptState(58, '$', '$');
  }
}

public static char currentChar;
public static void findKeywords(char[] lineCharArr, int col, int[][] transitionTable) {
//    System.out.println("Row               = " + row);
//    System.out.println("Line length       = " + lineCharArr.length);
    Stack<Integer> stateHistory = new Stack<Integer>();
    int startState = 1;
    int currentState = 1;

    char nextChar = '~'; // just needs to be initialised to something
    while(col <= lineCharArr.length) {
      if (col == lineCharArr.length) {
        break;
      }
      else {
        col++;
        if (currentState == 1) {
          stateHistory.push(transitionTable[currentState][char2IntConverter(lineCharArr[col - 1], stateHistory)]);
          currentState = stateHistory.peek();
          currentChar = lineCharArr[col-1];
          if (lineCharArr.length > col) {
            nextChar = lineCharArr[col];
          }
//          System.out.println("Current Character = " + lineCharArr[col - 1]);
//          System.out.println("Current State     = " + currentState);
          if (checkForAcceptState(currentState, currentChar, nextChar) == true) {
            stateHistory.push(1);
            if (currentChar != '$') {
              System.out.println(" at      " + "[" + row + ":" + col + "]");
            }
            currentState = 1;     // re-initalize state
          }
        }
        else {
          stateHistory.push(transitionTable[currentState][char2IntConverter(lineCharArr[col - 1], stateHistory)]);
          currentState = stateHistory.peek();
          currentChar = lineCharArr[col-1];
          if (lineCharArr.length > col) {
            nextChar = lineCharArr[col];
          }
          if (checkForAcceptState(currentState, currentChar, nextChar) == true) {
            stateHistory.push(1);
            System.out.println(" at      " + "[" + row + ":" + col + "]");
            currentState = 1;     // re-initalize state
          }
        }
      }
    }
  }

public static boolean checkForAcceptState(int currentState, char currentChar, char nextChar) {
  // check if current character is inside quotations.
  if (global_inside_quotations == true && currentChar == '$') {
    //figure out exactly what has to happen under these circumstances. for now, print this message
    errorMessager(2,'$');
  }
  if (global_inside_quotations == true && currentChar != '"') {
    currentState = 0;
  }
  switch (currentState) {
    // usually for chars. some other single symbol tokens make it to this state however. Added backup cases for now
    case 0 :
      currentState = 1; //reset current state
      if (currentChar == ' ') {
      return false;
      }
      else if (currentChar == '=') {
        if (nextChar != '=') {
          System.out.print("Symbol              EQUALS    found");
          //System.out.print("Next Char: " + nextChar);
          Token equalsToken = new Token();
          equalsToken.type = equalsToken.type.EQUALS;
          equalsToken.row = row;
          equalsToken.col = col;
          TokenQueue.add(equalsToken);
          return true;
        }
        else {
          System.out.print("Operator          EQUALITY    found");
          Token equalityToken = new Token();
          equalityToken.type = equalityToken.type.EQUALITY;
          equalityToken.row = row;
          equalityToken.col = col;
          TokenQueue.add(equalityToken);
          return true;
        }
      }
      else if (currentChar == '!') {
        if (nextChar == '=') {
          System.out.print("Operator        INEQUALITY    found");
          Token inequalityToken = new Token();
          inequalityToken.type = inequalityToken.type.INEQUALITY;
          inequalityToken.row = row;
          inequalityToken.col = col;
          TokenQueue.add(inequalityToken);
          return true;
        }
        else {
          System.out.println(ANSI_RED + "ERROR: [!] found outside of a string. Did you mean to use [!=] operator?");
          lexErrorsExist = true;
        }
      }
      else if (currentChar == '{') {
        System.out.print("Symbol             L_BRACE    found");
        Token l_braceToken = new Token();
        l_braceToken.type = l_braceToken.type.L_BRACE;
        l_braceToken.row = row;
        l_braceToken.col = col;
        TokenQueue.add(l_braceToken);
        return true;
      }
      else if (currentChar == '}') {
        System.out.print("Symbol             R_BRACE    found");
        Token r_braceToken = new Token();
        r_braceToken.type = r_braceToken.type.R_BRACE;
        r_braceToken.row = row;
        r_braceToken.col = col;
        TokenQueue.add(r_braceToken);
        return true;
      }
      else if (currentChar == '(') {
        System.out.print("Symbol             L_PAREN    found");
        Token l_parenToken = new Token();
        l_parenToken.type = l_parenToken.type.L_PAREN;
        l_parenToken.row = row;
        l_parenToken.col = col;
        TokenQueue.add(l_parenToken);
        return true;
      }
      else if (currentChar == ')') {
        System.out.print("Symbol             R_PAREN    found");

        Token r_parenToken = new Token();
        r_parenToken.type = r_parenToken.type.R_PAREN;
        r_parenToken.row = row;
        r_parenToken.col = col;
        TokenQueue.add(r_parenToken);
        return true;
      }
      else if (currentChar == '+') {
        System.out.print("Operator                 " + currentChar + "    found");

        Token addToken = new Token();
        addToken.type = addToken.type.ADD;
        addToken.row = row;
        addToken.col = col;
        TokenQueue.add(addToken);
        return true;
      }
      else if (currentChar == '0' || currentChar == '1' || currentChar == '2' || currentChar == '3' || currentChar == '4' || currentChar == '5' || currentChar == '6'|| currentChar == '7' || currentChar == '8' || currentChar == '9') {
        System.out.print("Integer                  " + currentChar + "    found");
        Token integerToken = new Token();
        integerToken.type = integerToken.type.INTEGER;
        integerToken.row = row;
        integerToken.col = col;
        integerToken.character = currentChar;
        TokenQueue.add(integerToken);
        return true;
      }
      else if(currentChar == '"') {
        System.out.print("Symbol           QUOTATION    found");
        Token quotationToken = new Token();
        quotationToken.type = quotationToken.type.QUOTATION;
        quotationToken.row = row;
        quotationToken.col = col;
        TokenQueue.add(quotationToken);
        if (global_inside_quotations == false) {   //used to determine if we are inside quotations. This means most things should be read as chars
          global_inside_quotations = true;
        }
        else {
          global_inside_quotations = false;
        }
        return true;
      }
      else {
        System.out.print("Char                     " + currentChar + "    found");
        Token charToken = new Token();
        charToken.type = charToken.type.CHAR;
        charToken.row = row;
        charToken.col = col;
        charToken.character = currentChar;
        TokenQueue.add(charToken);
        return true;
      }
    case 4 :
      System.out.print("Integer                  " + currentChar + "    found");
      Token integerToken = new Token();
      integerToken.type = integerToken.type.INTEGER;
      integerToken.row = row;
      integerToken.col = col;
      integerToken.character = currentChar;
      TokenQueue.add(integerToken);
      return true;
    case 7 :
      System.out.print("Symbol             L_BRACE    found");
      Token l_braceToken = new Token();
      l_braceToken.type = l_braceToken.type.L_BRACE;
      l_braceToken.row = row;
      l_braceToken.col = col;
      TokenQueue.add(l_braceToken);
      return true;
    case 8 :
      System.out.print("Symbol             R_BRACE    found");
      Token r_braceToken = new Token();
      r_braceToken.type = r_braceToken.type.R_BRACE;
      r_braceToken.row = row;
      r_braceToken.col = col;
      TokenQueue.add(r_braceToken);
      return true;
    case 9 :
      if (nextChar == '=') {
        System.out.print("Symbol              EQUALS    found");
        //System.out.print("NextChar! "+ nextChar);
        Token equalsToken = new Token();
        equalsToken.type = equalsToken.type.EQUALS;
        equalsToken.row = row;
        equalsToken.col = col;
        TokenQueue.add(equalsToken);
        return true;
      }
      else {
        return false;
      }
    case 10:
      System.out.print("Operator          EQUALITY    found");
      Token equalityToken = new Token();
      equalityToken.type = equalityToken.type.EQUALITY;
      equalityToken.row = row;
      equalityToken.col = col;
      TokenQueue.add(equalityToken);
      return true;
    case 12 :
      System.out.print("Operator        INEQUALITY    found");
      Token inequalityToken = new Token();
      inequalityToken.type = inequalityToken.type.INEQUALITY;
      inequalityToken.row = row;
      inequalityToken.col = col;
      TokenQueue.add(inequalityToken);
      return true;
    case 13 :
      System.out.print("Symbol             L_PAREN    found");
      Token l_parenToken = new Token();
      l_parenToken.type = l_parenToken.type.L_PAREN;
      l_parenToken.row = row;
      l_parenToken.col = col;
      TokenQueue.add(l_parenToken);
      return true;
    case 14 :
      System.out.print("Symbol             R_PAREN    found");
      Token r_parenToken = new Token();
      r_parenToken.type = r_parenToken.type.R_PAREN;
      r_parenToken.row = row;
      r_parenToken.col = col;
      TokenQueue.add(r_parenToken);
      return true;
    case 15 :
      System.out.print("Operator               ADD    found");
      Token addToken = new Token();
      addToken.type = addToken.type.ADD;
      addToken.row = row;
      addToken.col = col;
      TokenQueue.add(addToken);
      return true;
    case 21 :
      System.out.print("Symbol             COMMENT    found");
      Token commentToken = new Token();
      commentToken.type = commentToken.type.COMMENT;
      commentToken.row = row;
      commentToken.col = col;
      TokenQueue.add(commentToken);
      return true;
    case 28 :
      System.out.print("Keyword            BOOLEAN    found");
      Token booleanToken = new Token();
      booleanToken.type = booleanToken.type.BOOLEAN;
      booleanToken.row = row;
      booleanToken.col = col;
      TokenQueue.add(booleanToken);
      return true;
    case 33 :
      System.out.print("Keyword              FALSE    found");
      Token falseToken = new Token();
      falseToken.type = falseToken.type.FALSE;
      falseToken.row = row;
      falseToken.col = col;
      TokenQueue.add(falseToken);
      return true;
    case 35 :
      System.out.print("Keyword                 IF    found");
      Token ifToken = new Token();
      ifToken.type = ifToken.type.IF;
      ifToken.row = row;
      ifToken.col = col;
      TokenQueue.add(ifToken);
      return true;
    case 37 :
      System.out.print("Keyword                INT    found");
      Token intToken = new Token();
      intToken.type = intToken.type.INT;
      intToken.row = row;
      intToken.col = col;
      TokenQueue.add(intToken);
      return true;
    case 42 :
      System.out.print("Keyword              PRINT    found");
      Token printToken = new Token();
      printToken.type = printToken.type.PRINT;
      printToken.row = row;
      printToken.col = col;
      TokenQueue.add(printToken);
      return true;
    case 48 :
      System.out.print("Identifier          STRING    found");
      Token stringToken = new Token();
      stringToken.type = stringToken.type.STRING;
      stringToken.row = row;
      stringToken.col = col;
      TokenQueue.add(stringToken);
      return true;
    case 49 :
      //dont print for spaces
      Token spaceToken = new Token();
      spaceToken.type = spaceToken.type.SPACE;
      spaceToken.row = row;
      spaceToken.col = col;
      TokenQueue.add(spaceToken);
      return true;
    case 52 :
      System.out.print("Keyword               TRUE    found");
      Token trueToken = new Token();
      trueToken.type = trueToken.type.TRUE;
      trueToken.row = row;
      trueToken.col = col;
      TokenQueue.add(trueToken);
      return true;
    case 57 :
      System.out.print("Keyword              WHILE    found");
      Token whileToken = new Token();
      whileToken.type = whileToken.type.WHILE;
      whileToken.row = row;
      whileToken.col = col;
      TokenQueue.add(whileToken);
      return true;
    case 58 :
      System.out.println("Symbol     [EOP]         $    found\n");
      programCounter++;
      Token eopToken = new Token();
      eopToken.type = eopToken.type.EOP;
      eopToken.row = row;
      eopToken.col = col;
      global_inside_quotations = false;
      TokenQueue.add(eopToken);
      if (lexErrorsExist == false) {
        System.out.println(ANSI_GREEN + "Lexical Analysis Successful" + ANSI_RESET);
        System.out.println("\n===================================================");
        System.out.println("                      PARSE                        ");
        System.out.println("===================================================\n");
        parse.parse();
      }
      else {
        System.out.println(ANSI_RED + "Lexical Analysis Unsuccessful" + ANSI_RESET);
      }
        if (parse.parseErrorsFound == false) {
          System.out.println(lex.ANSI_GREEN + "Parse Successful" + lex.ANSI_RESET);
          System.out.println("\n===================================================");
          System.out.println("               CONCRETE SYNTAX TREE               ");
          System.out.println("===================================================\n");
          parse.createCSTree();
          System.out.println("\n===================================================");
          System.out.println("               ABSTRACT SYNTAX TREE                ");
          System.out.println("===================================================\n");
          parse.createASTree();
      }
      else {
        System.out.println(lex.ANSI_RED + "Parse Unsuccessful" + lex.ANSI_RESET);
      }
      if (input.hasNext() == true) {
        System.out.println("\nMore input detected...\nLexing Program: " + programCounter);
        System.out.println("===================================================");
        System.out.println("TOKEN TYPE      TOKEN NAME                LOCATION");
        System.out.println("===================================================");
      }
      return true;
    case 59 :
      System.out.print("Symbol           QUOTATION    found");
      Token quotationToken = new Token();
      quotationToken.type = quotationToken.type.QUOTATION;
      quotationToken.row = row;
      quotationToken.col = col;
      TokenQueue.add(quotationToken);
      if (global_inside_quotations == false) {   //used to determine if we are inside quotations. This means most things should be read as chars
        global_inside_quotations = true;
      }
      else {
        global_inside_quotations = false;
      }
      return true;
    default :
  }
  return false;
}

public static int char2IntConverter(char x, Stack<Integer> lastState) {
  switch(x) {
    case 'a' :
      return 2;
    case 'b' :
      return 3;
    case 'c' :
      return 4;
    case 'd' :
      return 5;
    case 'e' :
      return 6;
    case 'f' :
      return 7;
    case 'g' :
      return 8;
    case 'h' :
      return 9;
    case 'i' :
      return 10;
    case 'j' :
      return 11;
    case 'k' :
      return 12;
    case 'l' :
      return 13;
    case 'm' :
      return 14;
    case 'n' :
      return 15;
    case 'o' :
      return 16;
    case 'p' :
      return 17;
    case 'q' :
      return 18;
    case 'r' :
      return 19;
    case 's' :
      return 20;
    case 't' :
      return 21;
    case 'u' :
      return 22;
    case 'v' :
      return 23;
    case 'w' :
      return 24;
    case 'x' :
      return 25;
    case 'y' :
      return 26;
    case 'z' :
      return 27;
    case '0' :
      return 28;
    case '1' :
      return 29;
    case '2' :
      return 30;
    case '3' :
      return 31;
    case '4' :
      return 32;
    case '5' :
      return 33;
    case '6' :
      return 34;
    case '7' :
      return 35;
    case '8' :
      return 36;
    case '9' :
      return 37;
    case '/' :
      return 38;
    case '*' :
      return 39;
    case '=' :
      return 40;
    case '+' :
      return 41;
    case '(' :
      return 42;
    case ')' :
      return 43;
    case '{' :
      return 44;
    case '}' :
      return 45;
//      case '.' :
//        return 46;
    case '!' :
      return 47;
    case '$' :
      return 48;
    case ' ' :
      return 49;
    case '\n' :
      if (lastState.size() == 0) {   // avoid peeking at empty stack
        return 50;
      }
      else {
        return lastState.peek();
      }
    case '"' :
      return 51;
  }
   errorMessager(1, x);
   return 1;
}


public static void errorMessager(int code, char unrecognizedSymbol) {
  switch(code) {
    case (1) : // unrecognized symbol
      String message = ANSI_RED + "ERROR: Unrecognized Character " + "[" + unrecognizedSymbol + "]" + " at " + row + ":" + col + ANSI_RESET;
      System.out.println(message);
      lexErrorsExist = true;
      break;
    case (2) : // premature eop inside quotations
      System.out.println(ANSI_RED + "ERROR: EOP [$] DETECTED WITHIN QUOTATIONS!" + ANSI_RESET);
      lexErrorsExist = true;
      break;
    case (3) :
      System.out.println(ANSI_RED + "PARSE ERROR inside parseProgram()" + ANSI_RESET);
      break;
    case (4) :
      System.out.println(ANSI_RED + "PARSE ERROR inside parseBlock()" + ANSI_RESET);
      break;
    case (5) :
      System.out.println(ANSI_RED + "PARSE ERROR inside parseStatementList()" + ANSI_RESET);
      break;
    case (6) :
      System.out.println(ANSI_RED + "PARSE ERROR inside parseStatement()" + ANSI_RESET);
      break;
    case (7) :
      System.out.println(ANSI_RED + "PARSE ERROR inside parseAssignmentStatement()" + ANSI_RESET);
      break;
    case (8) :
      System.out.println(ANSI_RED + "PARSE ERROR inside parseVarDecl()" + ANSI_RESET);
      break;
    case (9) :
      System.out.println(ANSI_RED + "PARSE ERROR inside parseWhileStatement()" + ANSI_RESET);
      break;
    case (10) :
      System.out.println(ANSI_RED + "PARSE ERROR inside parseIfStatement()" + ANSI_RESET);
      break;
    case (11) :
      System.out.println(ANSI_RED + "PARSE ERROR inside parseExpr()" + ANSI_RESET);
      break;
    case (12) :
      System.out.println(ANSI_RED + "PARSE ERROR inside parseIntExpr()" + ANSI_RESET);
      break;
    case (13) :
      System.out.println(ANSI_RED + "PARSE ERROR inside parseStringExpr()" + ANSI_RESET);
      break;
    case (14) :
      System.out.println(ANSI_RED + "PARSE ERROR inside parseBooleanExpr()" + ANSI_RESET);
      break;
    case (15) :
      System.out.println(ANSI_RED + "PARSE ERROR inside parseId()" + ANSI_RESET);
      break;
    case (16) :
      System.out.println(ANSI_RED + "PARSE ERROR inside parseCharList()" + ANSI_RESET);
      break;
    case (17) :
      System.out.println(ANSI_RED + "PARSE ERROR inside parseType()" + ANSI_RESET);
      break;
    case (18) :
      System.out.println(ANSI_RED + "PARSE ERROR inside parseChar()" + ANSI_RESET);
      break;
    case (19) :
      System.out.println(ANSI_RED + "PARSE ERROR inside parseSpace()" + ANSI_RESET);
      break;
    case (20) :
      System.out.println(ANSI_RED + "PARSE ERROR inside parseDigit()" + ANSI_RESET);
      break;
    case (21) :
      System.out.println(ANSI_RED + "PARSE ERROR inside parseBoolop()" + ANSI_RESET);
      break;
    case (22) :
      System.out.println(ANSI_RED + "PARSE ERROR inside parseBoolval()" + ANSI_RESET);
      break;
    case (23) :
      System.out.println(ANSI_RED + "PARSE ERROR inside parseIntop()" + ANSI_RESET);
      break;
    case (24) :
      System.out.println(ANSI_RED + "PARSE ERROR inside parsePrintStatement()" + ANSI_RESET);
    }
 }

public static void printLexDebugInfo(int currentState) {
  System.out.println("  Current Char: " + currentChar + "Current State: " + currentState);
  }
}
