package models

case class Table(id: Option[Int], name: String, participants: Byte)

/**
  * This singelton holds the List of Tables
  * as well as methods for Tables' management.
  */
object Tables {
  /** Tables are stored in an immutable Scala list.
    * The list itself is stored in a variable that gets updated
    * with a new List of Tables by successful requests from Client.
    */
  private var _tables: List[Table] = List()

  /** Table IDs are actual List indexes of a corresponding Table entry.
    * They change with addition of new tables and removal of existing ones.
    * Therefore we need this accessor method that
    * traverses through the List of Tables and updates the IDs.
    *
    * @return List of Tables with up-to-date IDs.
    */
  def tables: List[Table] = for (table <- _tables) yield
    Table(Option(_tables.indexOf(table)), table.name, table.participants)

  def add(after_id: Int, newTable: Table): Either[String, String] = {
    val table = Table(None, newTable.name, newTable.participants)
    val tableID = after_id + 1
    /** Do not allow insertion at List positions with negative indexes
      * and after non-existent IDs.
      * If the Table List is empty, the only possible option is
      * to insert after ID -1 = at index 0.
      */
    val validID = tableID > -1 && after_id < _tables.length
    if (validID) {
      _tables = _insert(tables, tableID, table)
      Right("table_added")
    } else
      Left("addition_failed")
  }

  def remove(tableID: Int): Either[String, String] = {
    // Do not allow removal of entries at non-existent IDs.
    val validID = tableID > -1 && tableID < _tables.length
    if (validID) {
      _tables = tables.filterNot(_.id.getOrElse(-1) == tableID)
      Right("table_removed")
    } else
      Left("removal_failed")
  }

  def update(table: Table): Either[String, String] = {
    // Do not allow updates at non-existent IDs.
    val tableID = table.id.getOrElse(-1)
    val validID = tableID > -1 && tableID < _tables.length
    if (validID) {
      _tables = tables.updated(table.id.get, table)
      Right("table_updated")
    } else
      Left("update_failed")
  }

  /**
    * Helper method that inserts entries at a given position in a List of Tables.
    *
    * @param list the List to be modified.
    * @param pos the position to insert the new element at.
    * @param value the Table to be inserted at the specified position.
    * @return List of Tables with new Table inserted.
    */
  private def _insert(list: List[Table], pos: Int, value: Table): List[Table] = {
    val (head, tail) = list.splitAt(pos)
    head ++ List(value) ++ tail
  }

}
