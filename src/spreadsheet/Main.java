package spreadsheet;

import common.gui.SpreadsheetGUI;

public class Main {

  private static final int DEFAULT_NUM_ROWS = 5000;
  private static final int DEFAULT_NUM_COLUMNS = 5000;

  public static void main(String[] args) {
    Spreadsheet s = new Spreadsheet();
    SpreadsheetGUI sGUI;
    if (args.length == 0) {
      sGUI = new SpreadsheetGUI(s, DEFAULT_NUM_ROWS,
          DEFAULT_NUM_COLUMNS);
    } else {
      int numRows = Integer.parseInt(args[0]);
      int numCols = Integer.parseInt(args[1]);
      sGUI = new SpreadsheetGUI(s, numRows, numCols);
    }
    sGUI.start();
  }

}
