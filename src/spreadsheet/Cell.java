package spreadsheet;

import common.api.CellLocation;
import common.api.monitor.Tracker;
import common.api.value.InvalidValue;
import common.api.value.StringValue;
import common.api.value.Value;
import common.api.ExpressionUtils;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Cell implements Tracker<Cell>{
  private final Spreadsheet belongsTo;
  private final CellLocation loc;
  private String expr;
  private Value val;
  private Set<Cell> references = new HashSet<>();
  private final Set<Tracker<Cell>> trackers = new HashSet<>();
  public Cell(Spreadsheet belongsTo, CellLocation loc, String expr, Value val) {
    this.belongsTo = belongsTo;
    this.loc = loc;
    this.expr = expr;
    this.val = val;
  }
  public Cell(Spreadsheet belongsTo, CellLocation loc) {
    this(belongsTo, loc, "", new StringValue(""));
  }

  public CellLocation getLoc() {
    return loc;
  }

  public String getExpr() {
    return expr;
  }

  public Value getVal() {
    return val;
  }

  public Set<Cell> getReferences() {
    return references;
  }

  public Set<Tracker<Cell>> getTrackers() {
    return trackers;
  }

  public void setExpr(String expr) {
    this.expr = expr;
    val = new InvalidValue(expr);
    belongsTo.getToRecompute().add(this);

    // sort out cells that this cell depends on
    // remove trackers of cells that depend on this cell
    for (Cell c : references) {
      c.removeTracker(this);
    }
    updateReferences(expr);
    // update the trackers of those cells that now depend on this cell
    for (Cell c : references) {
      c.addTracker(this);
    }

    // Now inform cells that depend on this cell that this cell has changed
    informTrackers();
  }

  public void informTrackers() {
    // This informs all of the trackers subscribed to this cell that this cell
    // has changed - added to avoid duplication
    for (Tracker<Cell> t: trackers) {
      t.update(this);
    }
  }

  public void updateReferences(String expr) {
    Set<CellLocation> locations = ExpressionUtils.getReferencedLocations(expr);
    references = locations.stream().map(belongsTo::getCell).collect(Collectors
        .toSet());
  }

  public void setVal(Value val) {
    this.val = val;
  }

  @Override
  public void update(Cell changed) {
    if (!belongsTo.getToRecompute().contains(this)) {
      belongsTo.getToRecompute().add(this);
      setVal(new InvalidValue(expr));
      informTrackers();
    }

  }

  private void removeTracker(Tracker<Cell> tracker) {
    trackers.remove(tracker);
  }
  private void addTracker(Tracker<Cell> tracker) {
    trackers.add(tracker);
  }

}
