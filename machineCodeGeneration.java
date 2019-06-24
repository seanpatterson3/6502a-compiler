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
import java.io.IOException;

public class machineCodeGeneration {
  public static String[] machineCodeTable = new String[256];
  public static int globalTempAddressIterator = 0;
  public static int globalJumpAddressIterator = 0;
  public static int globalOffsetIterator = 0;
  public static Queue<String> outputQueue = new LinkedList<>();
  public static int programSize = 0;
  public static int heapSize = 0; // give 1 index buffer at the end of the program
  public static Boolean sizeExceeded = false;
  public static int scopePointer;
  public static Boolean insideIfStatement = false;
  public static int ifNestDepth = 0; //used to track depth of nested if stmts. 0 is outside of if statement, 1 is one if, 2 is two ifs, etc...
  public static int whileNestDepth = 0;
  public static int ifBlockDistInitalSize = 0;
  public static Boolean semanticAnalysisErrors = false;

  public static class StaticTableEntry {
    String[] address = new String[2];
    char id = ' ';
    String type = null;
    int offset = globalOffsetIterator;
    Boolean validEntry = false;
    int scope = 0;

    StaticTableEntry() {
      address[0] = "T" + String.valueOf(globalTempAddressIterator);
      address[1] = "xx";
      id = ' '; // chars need to be initalized to something
    }
  }

  public static class JumpTableEntry {
    String tempAddress;
    int distance = 0;
    int nestDepth;

    JumpTableEntry() {
      //TODO limit jump address iterator to 10
      tempAddress = "J" + String.valueOf(globalJumpAddressIterator);
      distance = 0;
    }
  }

  public static class ifNest {
    int nestDepth;
    int size;
  }

  public static StaticTableEntry[] staticTable = new StaticTableEntry[256];
  public static StaticTableEntry[] updatedStaticTable = new StaticTableEntry[256];
  public static JumpTableEntry[] jumpTable = new JumpTableEntry[256];
  public static JumpTableEntry[] updatedJumpTable = new JumpTableEntry[256];
  public static void computeOPcode(TreeMap<Integer, String> tree, Queue<Integer> depth) {
    semanticAnalysisErrors = false;
    scopePointer = -1;
    int indexer = 1;
    String type;
    String value;
    while (indexer < tree.size()) {
      switch (tree.get(indexer).toString()) {
        case "PROGRAM" :
          //ignore
          indexer++;
          break;
        case "BLOCK" :
          scopePointer++;
          indexer++;
          break;
        case "ASSIGNMENT STATEMENT" :
          type = tree.get(indexer + 1).toString();
          // get value through table
          if (getTypeBasedOnId(type, scopePointer) == "INT") {
            value = tree.get(indexer + 2).toString();
          }
          else {
            value = getValueBasedOnId(type);
          }
          if (checkType(type, value, scopePointer) == true) {
            assignmentStatement(value,type);
          }
          indexer = indexer + 2;
          break;
        case "VAR DECL" :
          type = tree.get(indexer + 1).toString();
          value = tree.get(indexer + 2).toString();
          variableDeclaration(value, type);
          indexer = indexer + 2;
          break;
        case "PRINT" :
          value = tree.get(indexer + 1);
          printStatement(value);
          indexer = indexer + 1;
          break;
        case "ENDBLOCK" :
          //endblock for if
          if (ifNestDepth == 1) {
            //get number of opcodes passed, and set that as the distance
            int dist = outputQueue.size() - ifBlockDistInitalSize + 1;
            //gets last value in the jump table and assigns the correct address length
            System.out.println(globalJumpAddressIterator);
            jumpTable[ifNestDepth - 1].distance = dist;
            ifBlockDistInitalSize = 0;
            dist = outputQueue.size() - ifBlockDistInitalSize + 1;
            //gets last value in the jump table and assigns the correct address length
            jumpTable[ifNestDepth - 1].distance = dist;
            ifNestDepth--;
          }
          else if (ifNestDepth > 1) {
            int depthCopy = ifNestDepth;
            while (depthCopy != 1) {
              ifBlockDistInitalSize = ifBlockDistInitalSize - jumpTable[depthCopy - 1].distance;
              depthCopy--;
              int dist = outputQueue.size() - ifBlockDistInitalSize + 1;
              //gets last value in the jump table and assigns the correct address length
              jumpTable[ifNestDepth - 1].distance = dist;
              ifNestDepth--;
            }
          }
          // endblock for while
          if (whileNestDepth == 1) {
            outputQueue.add("A9");
            outputQueue.add("00");

            outputQueue.add("8D");
            String[] printValue = getAddressBasedOnId("!", scopePointer); // ! is the id of all T's, @ is the id of all T+1's
            for (int z = 0; z < getAddressBasedOnId("!", scopePointer).length; z++) {
              outputQueue.add(printValue[z]);
            }
            outputQueue.add("A2");
            outputQueue.add("01");

            outputQueue.add("EC");
            for (int z = 0; z < getAddressBasedOnId("!", scopePointer).length; z++) {
              outputQueue.add(printValue[z]);
            }
            outputQueue.add("D0");
            int jumpLength = machineCodeTable.length - (outputQueue.size() - jumpTable[globalJumpAddressIterator - 1].distance + 1);
            System.out.println("Jumplength " + jumpLength);
            // convert distance to hex and add to queue
            int hexNumber = 0x0, p = 1;
            while (jumpLength != 0) {
              hexNumber = hexNumber + p * (jumpLength % 16);
              jumpLength = jumpLength / 16;
              p = p * 16;
            }
            String jumpValueAsString = Integer.toHexString(hexNumber).toUpperCase();
            if (jumpValueAsString.length() == 1) {
              jumpValueAsString = "0" + jumpValueAsString;
            }
            outputQueue.add(jumpValueAsString);
          }
          else if (whileNestDepth > 1) {
            //
          }
          scopePointer--;
          indexer++;
          break;
        case "IF" :
          // indexer + 1 = var, indexer + 2 = boolop, indexer + 3 = compare-to
          String param1 = tree.get(indexer + 1).toString();
          String param2 = tree.get(indexer + 3).toString();
          //check if == or !=
          if (tree.get(indexer + 2) == "EQUALITY") {
            ifStatement(param1, param2, scopePointer, true);
          }
          else if (tree.get(indexer + 2) == "INEQUALITY") {
            ifStatement(param1, param2, scopePointer, false);
          }
          else {
            System.out.println(tree.get(indexer + 3));
          }
          indexer = indexer + 3;
          scopePointer--;//need to reduce scope because an if statement creates a new block
          break;
        case "WHILE" :
          // indexer + 1 = var, indexer + 2 = boolop, indexer + 3 = compare-to
          String whileParam1 = tree.get(indexer + 1).toString();
          String whileParam2 = tree.get(indexer + 3).toString();
          //check if == or !=
          if (tree.get(indexer + 2) == "EQUALITY") {
            whileStatement(whileParam1, whileParam2, scopePointer, true);
          }
          else if (tree.get(indexer + 2) == "INEQUALITY") {
            whileStatement(whileParam1, whileParam2, scopePointer, false);
          }
          else {
            System.out.println(tree.get(indexer + 3));
          }
          indexer = indexer + 3;
          scopePointer--;//need to reduce scope because an if statement creates a new block
          break;
        default :
          indexer++;
          break;
      }
    }
    // we wrote the distances of the jump table backwards so we need to flip them
    Stack<Integer> flipJumpDistance = new Stack<>();
    for (int x = 0; x < globalJumpAddressIterator; x++) {
      flipJumpDistance.push(jumpTable[x].distance);
    }
    for (int y = 0; flipJumpDistance.size() > 0; y++) {
        jumpTable[y].distance = flipJumpDistance.peek();
        flipJumpDistance.pop();
    }

    pushBreak();
    programSize = getProgramSize();
    backPatch(outputQueue);
    if (sizeExceeded == false && semanticAnalysisErrors == false) {
      printMachineCodeTable();
    }
  }

  public static void printStaticTable(StaticTableEntry[] table) {
    // if the entry in the staticTable has been adjusted, print its information
    for (int y = 0; y != globalTempAddressIterator; y++) {
      if (table[y].id != ' ') {
        System.out.println("ID: " + table[y].id);
        System.out.println("Scope " + table[y].scope);
        System.out.println("Type " + table[y].type);
      //System.out.println("Value " + table[y].value);
        for (int x = 0; x < table[y].address.length; x++) {
          System.out.print(table[y].address[x] + " ");
        }
      }
    }
  }

  public static void assignmentStatement(String value, String type) {
    Boolean numeric = false;
    numeric = value.matches("-?\\d+(\\.\\d+)?");
    if (numeric == true) {
      if (Integer.parseInt(value) < 100) {
        outputQueue.add("A9");
        if (value.length() < 2) {
          outputQueue.add(0 + value); //add to accumulator with 0 in front since it is less than 10
        }
        else {
          outputQueue.add(value); // add to accumulator
        }
        outputQueue.add("8D");
      }
      // search for variable in statictable, then grab its temp address location, finally push it to the output queue
      for (int x = 0; x < globalTempAddressIterator; x++) {// globalTempAddressIterator should be an accurate representation of how large the table is
        if (String.valueOf(staticTable[x].id).equals(type) && staticTable[x].scope == scopePointer) {
          for (int y = 0; y < staticTable[x].address.length; y++) {
            outputQueue.add(staticTable[x].address[y]);
          }
        }
      }

    }
    //check if one variable is being assigned to the value of another
    else if (isVariableInsideStaticTable(type, scopePointer) == true && isVariableInsideStaticTable(value, scopePointer) == true) {
      //System.out.println("You're attempting to assign one variable to another!");
      outputQueue.add("AD");
      String[] returnedStringsForType = getAddressBasedOnId(type, scopePointer);
      String[] returnedStringsForValue = getAddressBasedOnId(value, scopePointer);
      for (int z = 0; z < getAddressBasedOnId(type, scopePointer).length; z++) {
        outputQueue.add(returnedStringsForValue[z]);
      }
      outputQueue.add("8D");
      for (int y = 0; y < getAddressBasedOnId(type, scopePointer).length; y++) {
        outputQueue.add(returnedStringsForType[y]);
      }
    }
    // check if string
    else if (isVariableInsideStaticTable(type, scopePointer) == true) {
      if (getTypeBasedOnId(type, scopePointer) == "STRING"){
        try {
          value = toHex.toHex(value).toString().toUpperCase();
          value = value + "00";
          heapSize = heapSize + (value.length() / 2);
          if (heapSize + programSize >= 256) {
            System.out.println(lex.ANSI_RED + "Code Gen Error: Program is larger than 255" + lex.ANSI_RESET);
            sizeExceeded = true;
          }
          else {
          // divide value into string[2] segments in order to write properly
          // find index value that is open
          int writeStartingPoint = machineCodeTable.length - heapSize;
          String hexReferencePointer = Integer.toHexString(writeStartingPoint).toUpperCase();
          // load accumulator with constant
          outputQueue.add("A9");
          // insert reference pointer showing where we are writing the string to
          outputQueue.add(hexReferencePointer);
          // store value in accumulator in mem
          outputQueue.add("8D");
          Stack<String> valueStack = new Stack<>();
          Queue<String> valueQueue = new LinkedList<>();
          for (int x = 0; x < value.length(); x++) {
            valueQueue.add(String.valueOf(value.charAt(x)));
          }

          int z = 0;
          while (valueQueue.size() != 0) {
            String temp = valueQueue.peek();
            valueQueue.remove();
            machineCodeTable[writeStartingPoint + z] = valueQueue.peek();
            machineCodeTable[writeStartingPoint + z] = machineCodeTable[writeStartingPoint + z] + temp;
            valueQueue.remove();
            machineCodeTable[writeStartingPoint + z] = ReverseString(machineCodeTable[writeStartingPoint + z]);
            z++;
          }
          // put our temporary variable name into runtime environment
          for (int x = 0; x < globalTempAddressIterator; x++) {// globalTempAddressIterator should be an accurate representation of how large the table is
            if (String.valueOf(staticTable[x].id).equals(type)) {

              for (int y = 0; y < staticTable[x].address.length; y++) {

                outputQueue.add(staticTable[x].address[y]);
              }
            }
          }
        }
      }
      catch (IOException e ) {
        e.printStackTrace();
      }
      int lengthOfValue = value.length();
    }
  }
  else {
    System.out.println(lex.ANSI_RED + "SEMANTIC ANALYSIS ERROR: Variable " + type + " was not found in static table." + lex.ANSI_RESET);
    semanticAnalysisErrors = true;
  }
  }

  public static Boolean typeCheckPassed(String id, String type, int scope, String value) {
    for (int x = 0; x < globalTempAddressIterator; x++) {
      if (String.valueOf(staticTable[x].id).equals(id) && staticTable[x].scope == scope) {
        switch (staticTable[x].type) {
          case "INT" :
            Boolean numeric = false;
            numeric = value.matches("-?\\d+(\\.\\d+)?");
            if (numeric == true) {
              return true;
            }
            return false;
          case "STRING" :
            // should accept anything that is inside quotes that isnt an illegal char. Parse checks for this
            return true;
          //case "BOOLEAN" :
          default :
            return false;
        }
      }
    }
  //var wasn't found in static table
  return false;
  }

  public static Boolean variableWithWrongScopeError(String id, int scope) { // checks to see if the variable called was found, but the user is using it outside of the correct scope
    for (int x = 0; x < globalTempAddressIterator; x++) {
      if (String.valueOf(staticTable[x].id).equals(id) && staticTable[x].scope != scope) {
        return true;
      }
    }
    return false;
  }

  public static Boolean isVariableInsideStaticTable(String id, int scope) {
    for (int x = 0; x < globalTempAddressIterator; x++) { // globalTempAddressIterator should be an accurate representation of how large the table is
      if (String.valueOf(staticTable[x].id).equals(id) && staticTable[x].scope == scope) {
        return true;
      }
    }
    return false;
  }

  public static String[] getAddressBasedOnId(String id, int scope) {
    String[] outputArray = new String[2];
    for (int x = 0; x < globalTempAddressIterator; x++) { // globalTempAddressIterator should be an accurate representation of how large the table is
      if (String.valueOf(staticTable[x].id).equals(id) && staticTable[x].scope == scope) {
        for (int y = 0; y < staticTable[x].address.length; y++) {
          outputArray[y] = staticTable[x].address[y];
        }
      }
    }
    return outputArray;
  }

  public static String getTypeBasedOnId(String id, int scope) {
    String outputString = "";
    for (int x = 0; x < globalOffsetIterator; x++) {
      if (String.valueOf(staticTable[x].id).equals(id) && staticTable[x].scope == scope) {
        if (staticTable[x].type != null) {
          outputString = staticTable[x].type;
          return outputString;
        }
      }
    }
  return outputString;
  }

  public static String getValueBasedOnId(String id) {
  String outputString = "";
  for (int x = 0; x < parse.typeTableEntryCounter; x++) {// globalTempAddressIterator should be an accurate representation of how large the table is
    if (String.valueOf(parse.TypeTable[x].id).equals(id) && parse.TypeTable[x].value != null) {
      outputString = parse.TypeTable[x].value;
      return outputString;
    }
  }
  return outputString;
  }


  public static void variableDeclaration(String value, String type) {
    Boolean isInt = false;
    Boolean isString = false;

    if (type == "INT") {
      outputQueue.add("A9");
      outputQueue.add("00"); // initalize integers to 0
      outputQueue.add("8D");
      isInt = true;
      //add value to static table, then load it into our output queue temporarily
      StaticTableEntry varDecl = new StaticTableEntry();
      varDecl.id = value.charAt(0);
      varDecl.type = "INT";
      varDecl.scope = scopePointer;
      varDecl.validEntry = true;
      staticTable[globalTempAddressIterator] = varDecl;

      for (int x = 0; x < staticTable[globalTempAddressIterator].address.length; x++) {
        outputQueue.add(staticTable[globalTempAddressIterator].address[x]);
      }
      globalTempAddressIterator++;
      globalOffsetIterator++;
    }
    else if (type == "STRING") {
      isString = true;
      StaticTableEntry varDecl = new StaticTableEntry();
      varDecl.id = value.charAt(0);
      varDecl.type = "STRING";
      varDecl.scope = scopePointer;
      varDecl.validEntry = true;
      staticTable[globalTempAddressIterator] = varDecl;
      globalTempAddressIterator++;
      globalOffsetIterator++;
    }
  }

  public static void printStatement(String value) {
    outputQueue.add("AC"); // load y register with value of the variable
    String[] returnedValue = getAddressBasedOnId(value, scopePointer);
    for (int y = 0; y < getAddressBasedOnId(value, scopePointer).length; y++) {
      if (returnedValue[y] == null) {
        System.out.println("VALUE: " + value);
        System.out.println("SCOPE: " + scopePointer);
        System.out.println("RETURNED NULL VAL");
      }
      outputQueue.add(returnedValue[y]);
    }
    outputQueue.add("A2"); // load x register
    if (getTypeBasedOnId(value, scopePointer) == "STRING") {
      outputQueue.add("02");
    }
    else if(getTypeBasedOnId(value, scopePointer) == "INT") {
      outputQueue.add("01");
    }
    else {
      //System.out.println("DEBUG: type for print " + value + " statement is " + getTypeBasedOnId(value, scopePointer));
    }
    outputQueue.add("FF"); // system call
  }

  public static void pushBreak() {
    outputQueue.add("00");
  }

  public static void printOutputQueue(Queue<String> queue) {
    System.out.println("\n==========================================");
    System.out.println("          MACHINE CODE GENERATION          ");
    System.out.println("==========================================\n");
    int newlineCounter = 0;
    Boolean afterFirst = false;
    while(queue.size() != 0) {
      if (newlineCounter == 0 && afterFirst == false) {
        newlineCounter++;
        System.out.print(queue.peek());
        queue.remove();
      }
      else if (newlineCounter < 8) {
        newlineCounter++;
        System.out.print(" ");
        System.out.print(queue.peek());
        queue.remove();
      }
      else {
        System.out.print("\n");
        System.out.print(queue.peek());
        queue.remove();
        newlineCounter = 1;
        afterFirst = true;
      }
    }
    System.out.println("\n");
  }

  public static void ifStatement(String param1, String param2, int scope, boolean equality) {
    insideIfStatement = true;
    ifNestDepth++;
    if (equality == true) {
      outputQueue.add("A2"); //load value of if statement into x reg
      //TODO add handling for if statements with strings
      // if number, convert it to hex
      Boolean numeric;
      numeric = param2.matches("-?\\d+(\\.\\d+)?");
      if (numeric == true) {
        int param2Value = Integer.valueOf(param2);
        int hexNumber = 0x0, p = 1;
        while (param2Value != 0) {
          hexNumber = hexNumber + p * (param2Value % 16);
          param2Value = param2Value / 16;
          p = p * 16;
        }
        param2 = Integer.toHexString(hexNumber).toUpperCase();
        if (param2.length() < 2) {
          param2 = "0" + param2;
        }
        outputQueue.add(param2);
      }
      outputQueue.add("EC"); //compare
      //get address of param1
      String[] addressValue = getAddressBasedOnId(param1, scope);
      for (int x = 0; x < getAddressBasedOnId(param1, scope).length; x++) {
        outputQueue.add(addressValue[x]);
      }
      outputQueue.add("D0"); // branch
      // add jumpaddress
      outputQueue.add("J" + String.valueOf(globalJumpAddressIterator));
      JumpTableEntry newEntry = new JumpTableEntry();
      jumpTable[globalJumpAddressIterator] = newEntry;
      globalJumpAddressIterator++;
      //get current outputqueue size so we can subtract the size of the queue after the if statement.
      //this will give us the distance which is calculated in computeOPcode switch case for endblock if inside an if statement
      ifBlockDistInitalSize = outputQueue.size();
    }
    else {
      //IF WITH INEQUALITY SIGN
        outputQueue.add("AD");
        //get address of var
        String[] addressValue = getAddressBasedOnId(param1, scope);
        for (int x = 0; x < getAddressBasedOnId(param1, scope).length; x++) {
          outputQueue.add(addressValue[x]);
        }
        //make new entry in static table, for the compare-to value. this value is going to be the next Tx xx
        StaticTableEntry copyOfVar = new StaticTableEntry();
        copyOfVar.id = '!'; //needs value to not disrupt all other searches based off id. ! will never be used as an id, so it shouldnt cause issues
        copyOfVar.type = "INT";
        copyOfVar.scope = scopePointer;
        copyOfVar.validEntry = false; //not real entry, we just need a placeholder in the table
        staticTable[globalTempAddressIterator] = copyOfVar;
        globalTempAddressIterator++;
        globalOffsetIterator++;
        //copy var to our new entry
        outputQueue.add("8D");
        for (int x = 0; x < copyOfVar.address.length; x++) {
          outputQueue.add(copyOfVar.address[x]);
        }
        //copy compare-to value to our old variable's location
        outputQueue.add("A9");
        //convert value to hex, then add to queue
        int param2Value = Integer.valueOf(param2);
        int hexNumber = 0x0, p = 1;
        while (param2Value != 0) {
          hexNumber = hexNumber + p * (param2Value % 16);
          param2Value = param2Value / 16;
          p = p * 16;
        }
        param2 = Integer.toHexString(hexNumber).toUpperCase();
        if (param2.length() < 2) {
          param2 = "0" + param2;
        }
        outputQueue.add(param2);

        outputQueue.add("8D");
        //add compare-to value
        StaticTableEntry compareTo = new StaticTableEntry();
        compareTo.id = '@';
        compareTo.type = "INT";
        compareTo.scope = scopePointer;
        compareTo.validEntry = false; //not real entry, we just need a placeholder in the table
        staticTable[globalTempAddressIterator] = compareTo;
        // re-orient our iterators so they are caught up
        globalTempAddressIterator++;
        globalOffsetIterator++;

        for (int x = 0; x < compareTo.address.length; x++) {
          outputQueue.add(compareTo.address[x]);
        }

        //compare Tx+1 & Tx & assign z flag
        outputQueue.add("AE");
        for (int x = 0; x < copyOfVar.address.length; x++) {
          outputQueue.add(copyOfVar.address[x]);
        }
        outputQueue.add("EC");
        for (int x = 0; x < compareTo.address.length; x++) {
          outputQueue.add(compareTo.address[x]);
        }
        //load acc with 0
        outputQueue.add("A9");
        outputQueue.add("00");
        // if Tx+1 != Tx branch 2 ahead so we are in front of where we set acc to 1
        outputQueue.add("D0");
        outputQueue.add("02");
        // if Tx+1 == Tx, acc = 1
        outputQueue.add("A9");
        outputQueue.add("01");
        // x reg = 0
        outputQueue.add("A2");
        outputQueue.add("00");
        // store Acc in Tx
        outputQueue.add("8D");
        for (int x = 0; x < compareTo.address.length; x++) {
          outputQueue.add(compareTo.address[x]);
        }
        // compare t1 and x reg, branch if unequal
        outputQueue.add("EC");
        for (int x = 0; x < compareTo.address.length; x++) {
          outputQueue.add(compareTo.address[x]);
        }
        outputQueue.add("D0");
        outputQueue.add("J" + String.valueOf(globalJumpAddressIterator));
        JumpTableEntry newEntry = new JumpTableEntry();
        jumpTable[globalJumpAddressIterator] = newEntry;
        globalJumpAddressIterator++;
        //get current outputqueue size so we can subtract the size of the queue after the if statement.
        //this will give us the distance which is calculated in computeOPcode switch case for endblock if inside an if statement
        ifBlockDistInitalSize = outputQueue.size();
      }
    }

  public static void whileStatement(String param1, String param2, int scope, boolean equality) {
    whileNestDepth++;
    JumpTableEntry whileJump = new JumpTableEntry();
    whileJump.distance = outputQueue.size();
    jumpTable[globalJumpAddressIterator] = whileJump;
    globalJumpAddressIterator++;

    if (equality == true) {

    }
    else {
      outputQueue.add("AD");
      //get address of var
      String[] addressValue = getAddressBasedOnId(param1, scope);
      for (int x = 0; x < getAddressBasedOnId(param1, scope).length; x++) {
        outputQueue.add(addressValue[x]);
      }
      //make new entry in static table, for the compare-to value. this value is going to be the next Tx xx
      StaticTableEntry copyOfVar = new StaticTableEntry();
      copyOfVar.id = '!'; //needs value to not disrupt all other searches based off id. ! will never be used as an id, so it shouldnt cause issues
      copyOfVar.type = "INT";
      copyOfVar.scope = scopePointer;
      copyOfVar.validEntry = false; //not real entry, we just need a placeholder in the table
      staticTable[globalTempAddressIterator] = copyOfVar;
      globalTempAddressIterator++;
      globalOffsetIterator++;
      //copy var to our new entry
      outputQueue.add("8D");
      for (int x = 0; x < copyOfVar.address.length; x++) {
        outputQueue.add(copyOfVar.address[x]);
      }
      //copy compare-to value to our old variable's location
      outputQueue.add("A9");
      //convert value to hex, then add to queue
      int param2Value = Integer.valueOf(param2);
      int hexNumber = 0x0, p = 1;
      while (param2Value != 0) {
        hexNumber = hexNumber + p * (param2Value % 16);
        param2Value = param2Value / 16;
        p = p * 16;
      }
      param2 = Integer.toHexString(hexNumber).toUpperCase();
      if (param2.length() < 2) {
        param2 = "0" + param2;
      }
      outputQueue.add(param2);

      outputQueue.add("8D");
      //add compare-to value
      StaticTableEntry compareTo = new StaticTableEntry();
      compareTo.id = '@';
      compareTo.type = "INT";
      compareTo.scope = scopePointer;
      compareTo.validEntry = false; //not real entry, we just need a placeholder in the table
      staticTable[globalTempAddressIterator] = compareTo;
      // re-orient our iterators so they are caught up
      globalTempAddressIterator++;
      globalOffsetIterator++;

      for (int x = 0; x < compareTo.address.length; x++) {
        outputQueue.add(compareTo.address[x]);
      }

      //compare Tx+1 & Tx & assign z flag
      outputQueue.add("AE");
      for (int x = 0; x < copyOfVar.address.length; x++) {
        outputQueue.add(copyOfVar.address[x]);
      }
      outputQueue.add("EC");
      for (int x = 0; x < compareTo.address.length; x++) {
        outputQueue.add(compareTo.address[x]);
      }
      //load acc with 0
      outputQueue.add("A9");
      outputQueue.add("00");
      // if Tx+1 != Tx branch 2 ahead si we are in front of where we set acc to 1
      outputQueue.add("D0");
      outputQueue.add("02");
      // if Tx+1 == Tx, acc = 1
      outputQueue.add("A9");
      outputQueue.add("01");
      // x reg = 0
      outputQueue.add("A2");
      outputQueue.add("00");
      // store Acc in Tx
      outputQueue.add("8D");
      for (int x = 0; x < compareTo.address.length; x++) {
        outputQueue.add(compareTo.address[x]);
      }
      // compare t1 and x reg, branch if unequal
      outputQueue.add("EC");
      for (int x = 0; x < compareTo.address.length; x++) {
        outputQueue.add(compareTo.address[x]);
      }
      outputQueue.add("D0");
      //TODO figure out how to determine where to branch
      outputQueue.add("20");
    }
  }


  public static void backPatch(Queue<String> queue) {
    // load values into machine code table, while removing them from printOutputQueue
    // write over addresses which are inside the staticTable
    // little endian
    // updatedStaticTable = staticTable;
    // initalize values in updatedStaticTable to be equivalent to statictable
    // backpatch jump table
    for (int i = 0; i < globalOffsetIterator; i++) {
      StaticTableEntry temp = new StaticTableEntry();
      for (int a = 0; a < temp.address.length; a++) {
        temp.address[a] = staticTable[i].address[a];
      }
      temp.id = staticTable[i].id;
      temp.offset = staticTable[i].offset;
      temp.scope = staticTable[i].scope;
      temp.type = staticTable[i].type;
      temp.validEntry = true;
      updatedStaticTable[i] = temp;
    }

    int length = programSize;
    int offset = 0;
    //get offset
    for (int x = 0; x < globalTempAddressIterator; x++) {
      offset = length + staticTable[x].offset;
      String[] currentVarLocation = new String[2];
      //converts to hex
      int hexNumber = 0x0, p = 1;
      String value = "";
      while (offset != 0) {
        hexNumber = hexNumber + p * (offset % 16);
        offset = offset / 16;
        p = p * 16;
      }
      value = Integer.toHexString(hexNumber).toUpperCase();
      if (value.length() < 2) {
        value = "0" + value;
      }
      currentVarLocation = convertToLittleEndian(value);
      updatedStaticTable[x].address[0] = currentVarLocation[0];
      updatedStaticTable[x].address[1] = currentVarLocation[1];
    }
    // write to machineCodeTable
    for (int y = 0; y < length; y++) {
      if (queue.size() != 0) {
        //move through the queue, if any element matches with the temp address of something in the staticTable, then replace it with its new address
        for (int z = 0; z < globalTempAddressIterator; z++) {
          if (queue.peek() == staticTable[z].address[0]) { //matches only the first part of a temp address. (T0,T1,T2,etc...)
            //give updated position
            machineCodeTable[y] = updatedStaticTable[z].address[0];
            queue.remove();
            y++;
            machineCodeTable[y] = updatedStaticTable[z].address[1];
            //queue.remove();
          }
          else {
            if (queue.size() != 0) {
              machineCodeTable[y] = queue.peek();
              //queue.remove();
            }
          }
        }
        // for each entry in the jump table, if we find the tempAddress Identifier for an entry, replace it with the distance
        for (int k = 0; k < globalJumpAddressIterator; k++) {
          if (queue.peek() != null) {
            if (queue.peek().equals(jumpTable[k].tempAddress)) {
              // convert distance to hex
              int distance = jumpTable[k].distance;
              int hexNumber = 0x0, p = 1;
              String value = "";
              while (distance != 0) {
                hexNumber = hexNumber + p * (distance % 16);
                distance = distance / 16;
                p = p * 16;
              }
              value = Integer.toHexString(hexNumber).toUpperCase();
              if (value.length() < 2) {
                value = "0" + value;
              }
              machineCodeTable[y] = value;
              //queue.remove();
              //y++;
            }
          }
        }
        queue.remove();
      }
    }
    for (int xx = 0; xx < programSize; xx++) {
      if (machineCodeTable[xx] == "xx") {
        machineCodeTable[xx] = "00";
      }
    }
  }

  public static void printMachineCodeTable() {
    System.out.println("\n===================================================");
    System.out.println("               MACHINE CODE GENERATION               ");
    System.out.println("===================================================\n");
    System.out.println("");
    int newlineCounter = 0;
    Boolean afterFirst = false;
    int length = programSize;
    for (int x = 0; x < machineCodeTable.length; x++)  {
      if (newlineCounter == 0 && afterFirst == false) {
        newlineCounter++;
        if (machineCodeTable[x] != null) {
          System.out.print(lex.ANSI_GREEN + machineCodeTable[x] + lex.ANSI_RESET);
        }
        else {
          System.out.print(lex.ANSI_GREEN + "00" + lex.ANSI_RESET);
        }
      }
      else if (newlineCounter < 8) {
        newlineCounter++;
        System.out.print(" ");
          if (machineCodeTable[x] != null) {
            System.out.print(lex.ANSI_GREEN + machineCodeTable[x] + lex.ANSI_RESET);
          }
          else {
            System.out.print(lex.ANSI_GREEN + "00" + lex.ANSI_RESET);
          }
        }
        else {
        System.out.print("\n");
          if (machineCodeTable[x] != null) {
            System.out.print(lex.ANSI_GREEN + machineCodeTable[x] + lex.ANSI_RESET);
          }
          else {
            System.out.print(lex.ANSI_GREEN + "00" + lex.ANSI_RESET);
          }
        newlineCounter = 1;
        afterFirst = true;
      }
    }
    System.out.println("\n");
    }

  public static String[] convertToLittleEndian(String hexLocation) {
    // dont completely understand this yet. for now im gonna take the hexvalue of the location, and put 00 on it, then flip it
    String[] returnValue = new String[2];
    returnValue[0] = hexLocation;
    returnValue[1] = "00";
    return returnValue;
  }

  public static void decToHex(int number) {
    int hexNumber = 0x0, p = 1;
    String value = "";
    while (number != 0) {
      hexNumber = hexNumber + p * (number % 16);
      number = number / 16;
      p = p * 16;
    }
    value = Integer.toHexString(hexNumber).toUpperCase();
  }

  public static int getProgramSize() {
    int returnValue = outputQueue.size();
    System.out.println("Program Size: " + returnValue);
    return returnValue;
  }

  public static Boolean isInsideArray(int[] array, int c) {
    for(int x = 0; x < array.length; x++) {
      if (array[x] == c) {
        return true;
      }
    }
    return false;
  }

  public static String ReverseString(String str) {
    String reverse = "";
    for(int i = str.length() - 1; i >= 0; i--) {
        reverse = reverse + str.charAt(i);
    }
    return reverse;
  }

  public static Boolean checkType(String id, String value, int currentScope) {
    //look up value in static table with id
    for (int x = 0; x < globalTempAddressIterator; x++) {
      if (String.valueOf(staticTable[x].id).equals(id) && staticTable[x].scope == currentScope) {
        if (staticTable[x].type != null) {
          switch (staticTable[x].type.toString().toUpperCase()) {
            case "INT" :
              //test if numeric
              Boolean numeric = false;
              numeric = value.matches("-?\\d+(\\.\\d+)?");
              if (numeric == true) {
                return true;
              }
              else if (value.length() == 1) { // could be an id
                if (getTypeBasedOnId(value, currentScope) == "INT") {
                  return true;
                }
              }
              //System.out.println(lex.ANSI_RED + "TYPE CHECK ERROR: Expected var [" + id + "] to be assigned INTEGER value, but found [ " + value + " ]" + lex.ANSI_RESET);
              System.out.println(lex.ANSI_RED + "TYPE CHECK ERROR: Expected var [" + id + "] to be assigned INTEGER value" + lex.ANSI_RESET);
              semanticAnalysisErrors = true;
              return false;
            case "STRING" :
              //test it isnt empty
              if (value.length() > 0) {
                return true;
              }
              else {
                System.out.println(lex.ANSI_RED + "TYPE CHECK ERROR: Expected var [" + id + "] to be assigned STRING value, but found [ EMPTY STRING, or INTEGER ]" + lex.ANSI_RESET);
              }
              semanticAnalysisErrors = true;
              return false;
            //case BOOLEAN :
          }
        }
      }
    }
    return true;
  }
}
