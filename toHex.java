import java.math.BigInteger;
import java.io.IOException;
public class toHex {
  public static String toHex(String arg) throws java.io.IOException {
    String value;
    // size should be 2x the length of string
    int size = arg.length() * 2;
    String sizeFormatted = "%" + Integer.toString(size) + "x";
    value = String.format(sizeFormatted, new BigInteger(1, arg.getBytes("UTF-8")));
    return value;

    }

public static String hexToString(String hex) {
  return Integer.toHexString(Integer.parseInt(hex));
  }

public static void  undeclaredVariableError(String arg) {
  System.out.println("Variable " + arg + "was not found in static table. Please make sure it was declared");
}

}
