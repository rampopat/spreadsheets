package spreadsheet;

import common.api.CellLocation;
import common.api.ExpressionUtils;
import common.api.Tabular;
import common.api.value.LoopValue;
import common.api.value.Value;
import common.api.value.ValueEvaluator;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class Spreadsheet implements Tabular {
  private final Set<Cell> toRecompute = new HashSet<>();
  private final Map<CellLocation, Cell> cellMap = new HashMap<>();
  private final Deque<Cell> cellsToRecompute = new ArrayDeque<>(); // The queue

  public Set<Cell> getToRecompute() {
    return toRecompute;
  }

  @Override
  public String getExpression(CellLocation location) {
    if (cellMap.containsKey(location)) {
      return cellMap.get(location).getExpr();
    }
    return "";
  }
  @Override
  public Value getValue(CellLocation location) {
    if (cellMap.containsKey(location)) {
      return cellMap.get(location).getVal();
    }
    return null;
  }
  public Cell getCell(CellLocation location) {
    // Post: Given a location, returns the corresponding cell, or returns a new
    // corresponding cell if none already exists in the map
    if (cellMap.containsKey(location)) {
      return cellMap.get(location);
    } else {
      Cell c = new Cell(this, location);
      cellMap.put(location, c);
      return c;
    }
  }


  @Override
  public void setExpression(CellLocation location, String expression) {
    if (cellMap.containsKey(location)) {
      cellMap.get(location).setExpr(expression);
    } else {
      Cell c = new Cell(this, location);
      c.setExpr(expression);
      cellMap.put(location, c);
    }
  }

  @Override
  public void recompute() {
    Iterator<Cell> iterator = toRecompute.iterator();
    while (iterator.hasNext()) {
      recomputeCell(iterator.next());
      iterator = toRecompute.iterator();
    }
  }

  private void recomputeCell(Cell c) {
    checkLoops(c, new LinkedHashSet<>());
    Cell current;
    Set<Cell> refsToRecompute;
    if (toRecompute.contains(c)) {
      cellsToRecompute.add(c);
      while (!cellsToRecompute.isEmpty()) {
        current = cellsToRecompute.pop();
        refsToRecompute = new HashSet<>(toRecompute);
        refsToRecompute.retainAll(current.getReferences());
        if (!refsToRecompute.isEmpty()) {
          for (Cell cell : refsToRecompute) {
            cellsToRecompute.addFirst(cell);
          }
          cellsToRecompute.add(current);
        } else {
          calculateCellValue(current);
          toRecompute.remove(current);
        }
      }
    }
  }
  private void checkLoops(Cell c, LinkedHashSet<Cell> cellsSeen) {
    if (cellsSeen.contains(c)) {
      markAsValidatedLoop(c, cellsSeen);
    } else {
      cellsSeen.add(c);
      for (Cell ref : c.getReferences()) {
        checkLoops(ref, cellsSeen);
      }
      cellsSeen.remove(c);
    }
  }
  private void markAsValidatedLoop(Cell startCell, LinkedHashSet<Cell> cells) {
    toRecompute.removeAll(cells);
    boolean foundLoop = false;
    for (Cell c : cells) {
      // following logic is to ensure that all the cells from the one that is
      // the same as start cell have a loop value
      if (c == startCell) {
        foundLoop = true; // will stay true once found cell equal to startCell
      }
      if (foundLoop) {
        c.setVal(LoopValue.INSTANCE);
      }
    }
  }

  private void calculateCellValue(Cell cell) {
    Map<CellLocation, Double> valueOverlay = new HashMap<>();
    for (Cell c : cell.getReferences()) {
      c.getVal().evaluate(new ValueEvaluator() {
        @Override
        public void evaluateDouble(double value) {
          valueOverlay.put(c.getLoc(), value);
        }

        @Override
        public void evaluateLoop() {

        }

        @Override
        public void evaluateString(String expression) {

        }

        @Override
        public void evaluateInvalid(String expression) {

        }
      });
    }
    cell.setVal(ExpressionUtils.computeValue(cell.getExpr(), valueOverlay));
  }
}

