import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.Date;

public class SpinnerCellEditor extends AbstractCellEditor implements TableCellEditor {
    private final JSpinner spinner;

    public SpinnerCellEditor(JSpinner spinner) {
        this.spinner = spinner;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (value instanceof Date) {
            spinner.setValue(value);
        } else {
            spinner.setValue(new Date()); // Set a default value if the value is not a Date
        }
        return spinner;
    }

    @Override
    public Object getCellEditorValue() {
        return spinner.getValue();
    }
}
