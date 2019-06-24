public class semanticAnalysis {

  public static void printTypeTable(parse.TypeTableEntry[] table) {
    // if the entry in the staticTable has been adjusted, print its information
    for (int y = 0; y != parse.typeTableEntryCounter; y++) {
      System.out.println("Entry: " + y);
      System.out.println("ID: " + table[y].id + " Type: " + table[y].type + " Scope " + table[y].scope);
    }
  }
}
