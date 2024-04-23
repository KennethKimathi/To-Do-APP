import javax.swing.*;
import java.awt.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class MainMenu extends JFrame {
    //AppFrame appFrame = new AppFrame(); // Create an instance of AppFrame
    //MainMenu mainMenu = new MainMenu("ProjectName", appFrame); // Pass the AppFrame instance to MainMenu

    private final String projectName;
    private JTable table;
    DefaultTableModel tableModel;
    private AppFrame appFrame;

    public JTable getTable() {
        return table;
    }
    public MainMenu(String projectName, AppFrame appFrame) {
        this.projectName = projectName;
        this.appFrame = appFrame;
        setTitle("My To-Do App");
        setSize(500, 600);
        setBackground(Color.pink);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        addGuiComponents();
        readFile();
    }
    private void addGuiComponents(){
        addToolbar();
        addTable();
        addButtons();
    }
    private void addToolbar(){
        JToolBar toolBar = new JToolBar();
        toolBar.setBounds(0,600,getWidth(),getHeight());
        toolBar.setBackground(Color.BLUE);
        toolBar.setFloatable(false);
        JLabel projectNameLabel = new JLabel("PROJECT NAME: " +projectName);
        System.out.println(projectName);
        toolBar.add(projectNameLabel);

        add(toolBar, BorderLayout.NORTH);
    }
    private void addTable() {
        tableModel = new DefaultTableModel();
        tableModel.addColumn("No.");
        tableModel.addColumn("Task");
        tableModel.addColumn("State");
        tableModel.addColumn("Schedule");

        table = new JTable(tableModel);
        String[] items = {"Pending", "In Progress", "Complete", "Past Due", "Remove"};
        JComboBox<String> stateComboBox = new JComboBox<>(items);
        TableColumn stateColumn = table.getColumnModel().getColumn(2); // get the "State" column
        stateColumn.setCellEditor(new DefaultCellEditor(stateComboBox));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (c == null) {
                    // Handle the case where the component is null
                    return null;
                }
                if (column == 2) {
                    String state = (String) table.getValueAt(row, 2);
                    switch (state) {
                        case "Pending":
                            c.setBackground(Color.GRAY);
                            break;
                        case "In Progress":
                            c.setBackground(Color.ORANGE);
                            break;
                        case "Complete":
                            c.setBackground(Color.GREEN);
                            break;
                        case "Past Due":
                            c.setBackground(Color.RED);
                            break;
                        case "Remove":

                            break;

                        default:
                            c.setBackground(table.getBackground());
                            break;
                    }
                } else {
                    c.setBackground(table.getBackground());
                }
                return c;
            }
        });


        // Create a spinner for the schedule column
        SpinnerDateModel spinnerModel = new SpinnerDateModel();
        JSpinner dateSpinner = new JSpinner(spinnerModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy/MM/dd HH:mm:ss");
        dateSpinner.setEditor(dateEditor);
        TableColumn scheduleColumn = table.getColumnModel().getColumn(3); // get the "Schedule" column
        scheduleColumn.setCellEditor(new SpinnerCellEditor(dateSpinner));

        JScrollPane scrollPane = new JScrollPane(table);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        // Set column widths
        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50);
        columnModel.getColumn(1).setPreferredWidth(200);
        columnModel.getColumn(2).setPreferredWidth(150);
        columnModel.getColumn(3).setPreferredWidth(100);

        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 2) {
                    // The state was updated
                    int row = e.getFirstRow();
                    String newState = (String) tableModel.getValueAt(row, 2);

                    // Update the state in the XML file
                    try {
                        File file = new File(projectName + ".xml");
                        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                        Document doc = dBuilder.parse(file);

                        // Get the task element that was updated
                        Element taskElement = (Element) doc.getElementsByTagName("task").item(row);

                        // Update the state attribute
                        taskElement.setAttribute("state", newState);

                        // Write the document back to the file
                        TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        Transformer transformer = transformerFactory.newTransformer();
                        DOMSource source = new DOMSource(doc);
                        StreamResult result = new StreamResult(file);
                        transformer.transform(source, result);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        // Create a popup menu
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem copyItem = new JMenuItem("Copy");
        JMenuItem deleteItem = new JMenuItem("Delete");
        popupMenu.add(copyItem);
        popupMenu.add(deleteItem);

        // Add mouse listener to table
        table.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent me) {
                int row = table.rowAtPoint(me.getPoint());
                int col = table.columnAtPoint(me.getPoint());

                // Check if right mouse button was clicked and if it was on the second column
                if (SwingUtilities.isRightMouseButton(me) && col == 1) {
                    // Show popup menu
                    popupMenu.show(me.getComponent(), me.getX(), me.getY());

                    // Add action listener to delete item
                    deleteItem.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent ae) {
                            deleteRow(row);
                        }
                    });
                }
            }
        });
    }

    private void addButtons(){
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.CYAN);
        //TextField
        JTextField task = new JTextField();

        task.setBackground(Color.gray);

        JButton addTask = new JButton("ADD");
        addTask.setBorderPainted(false);
        addTask.setBackground(null);

        JButton viewProjects = new JButton("BACK");
        viewProjects.setBorderPainted(false);
        viewProjects.setBackground(null);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.CYAN);
        buttonPanel.add(addTask);
        buttonPanel.add(viewProjects);
        add(buttonPanel, BorderLayout.SOUTH);

        panel.add(task);
        panel.setBackground(Color.CYAN);
        panel.add(buttonPanel);
        add(panel, BorderLayout.SOUTH);

        addTask.addActionListener(e -> {
            String taskName = task.getText(); // Get the text from the JTextField
            if (taskName != null && !taskName.isEmpty()) {
                tableModel.addRow(new Object[]{tableModel.getRowCount() + 1, taskName, "Pending", ""});
                task.setText("");

                // Create or update the XML file for the project
                try {
                    File file = new File(projectName + ".xml");
                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document doc;

                    if (file.exists()) {
                        // If the file already exists, parse it
                        doc = dBuilder.parse(file);
                    } else {
                        // If the file doesn't exist, create a new document
                        doc = dBuilder.newDocument();

                        // Create the root element
                        Element rootElement = doc.createElement("tasks");
                        doc.appendChild(rootElement);
                    }

                    // Get the root element
                    Element rootElement = doc.getDocumentElement();

                    // Create a new task element
                    Element taskElement = doc.createElement("task");

                    // Set the task's attributes
                    taskElement.setAttribute("number", Integer.toString(tableModel.getRowCount()));
                    taskElement.setAttribute("name", taskName);
                    taskElement.setAttribute("state", "Pending");
                    taskElement.setAttribute("schedule", "");

                    // Add the task element to the root element
                    rootElement.appendChild(taskElement);

                    // Write the document back to the file
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    DOMSource source = new DOMSource(doc);
                    StreamResult result = new StreamResult(file);
                    transformer.transform(source, result);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });


        viewProjects.addActionListener(e -> {
            returnToAppFrame();

        });
    }
    private void returnToAppFrame() {
        appFrame.setVisible(true); // Make the original AppFrame instance visible
        dispose(); // Close the MainMenu window
    }
    private void readFile(){
        File xmlFile = new File(("C:/Users/ADMIN/OneDrive/Desktop/Personal Projects/MyToDoApp/" + projectName + ".xml"));
        if (xmlFile.exists()) {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(xmlFile);
                doc.getDocumentElement().normalize();

                NodeList nodeList = doc.getElementsByTagName("task");

                for (int i = 0; i < ((NodeList) nodeList).getLength(); i++) {
                    Node node = nodeList.item(i);
                    if (((Node) node).getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) node;
                        String number = element.getAttribute("number");
                        String taskName = element.getAttribute("name");
                        String state = element.getAttribute("state");
                        String schedule = element.getAttribute("schedule");
                        tableModel.addRow(new Object[]{number, taskName, state, schedule});
                        System.out.println(taskName);
                        if (element.hasAttribute("state")) {
                            state = element.getAttribute("state");
                            System.out.println(state);
                        } else {
                            System.out.println("State attribute does not exist");
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void deleteRow(int row) {
        if (row >= 0 && row < tableModel.getRowCount()) {
            // Remove the row from the table model
            tableModel.removeRow(row);

            // Update the XML file
            try {
                File file = new File(projectName + ".xml");
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(file);

                // Adjust row index to match row numbers in XML document (1-based)
                int xmlRowIndex = row + 1;

                // Get the task element that was deleted
                Element taskElement = (Element) doc.getElementsByTagName("task").item(xmlRowIndex - 1); // Adjusted index

                // Remove the task element from the document
                taskElement.getParentNode().removeChild(taskElement);

                // Write the document back to the file
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(file);
                transformer.transform(source, result);

                System.out.println("Row " + xmlRowIndex + " deleted successfully");
                table.repaint();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

//    private void deleteRow(int row) {
//        // Get the task ID associated with the selected row
//        int taskNo = (int) tableModel.getValueAt(row, 0);
//        //taskNo = (int) taskNo;
//
//        // Remove the row from the table model
//        tableModel.removeRow(row);
//
//        // Update the XML file
//        try {
//            File file = new File(projectName + ".xml");
//            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//            Document doc = dBuilder.parse(file);
//
//            // Find the task element with the matching ID
//            NodeList tasks = doc.getElementsByTagName("task");
//            for (int i = 0; i < tasks.getLength(); i++) {
//                Element taskElement = (Element) tasks.item(i);
//                if (taskElement.getAttribute("number").equals(taskNo)) {
//                    // Remove the task element from the document
//                    taskElement.getParentNode().removeChild(taskElement);
//                    break; // Exit the loop once the task is found and removed
//                }
//            }
//
//            // Write the document back to the file
//            TransformerFactory transformerFactory = TransformerFactory.newInstance();
//            Transformer transformer = transformerFactory.newTransformer();
//            DOMSource source = new DOMSource(doc);
//            StreamResult result = new StreamResult(file);
//            transformer.transform(source, result);
//            table.repaint();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }

}
