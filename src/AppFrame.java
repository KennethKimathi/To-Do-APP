import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class AppFrame extends JFrame{
    private JTable table;
    DefaultTableModel tableModel;

    public JTable getTable() {
        return table;
    }
    public void AppGui(){
    setTitle("My To-Do App");
    setSize(400,600);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setBackground(Color.black);
    setVisible(true);
    addGuiComponents();
    addTable();
    loadExistingProjects();

    }
    private void addGuiComponents(){
        JButton addProjectButton = new JButton("ADD");
        //addProjectButton.setBounds(0,600, getWidth(), getHeight() );
        addProjectButton.setBorderPainted(true);
        addProjectButton.setBackground(Color.GRAY);
        //add(addProjectButton, BorderLayout.SOUTH);

        JButton markAsComplete = new JButton("COMPLETE");
        markAsComplete.setBorderPainted(true);
        markAsComplete.setBackground(Color.GRAY);

        JButton clearProject = new JButton("CLEAR");
        clearProject.setBorderPainted(true);
        clearProject.setBackground(Color.GRAY);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.CYAN);
        buttonPanel.add(addProjectButton);
        buttonPanel.add(markAsComplete);
        buttonPanel.add(clearProject);

        add(buttonPanel, BorderLayout.SOUTH);
        addProjectButton.addActionListener(e -> {
            String projectName = JOptionPane.showInputDialog("Enter Project Name:");
            if (projectName != null && !projectName.isEmpty()) {
                tableModel.addRow(new Object[]{tableModel.getRowCount() + 1, projectName, ""});

                try {
                    File directory = new File("C:/Users/ADMIN/OneDrive/Desktop/Personal Projects/MyToDoApp/MyToDoApp/src/Projects/");
                    if (!directory.exists()) {
                        directory.mkdirs(); // Create directory and parent directories if necessary
                    }
                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document doc = dBuilder.newDocument();

                    // Create the root element
                    Element rootElement = doc.createElement("tasks");
                    doc.appendChild(rootElement);

                    // Write the document to a file
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    DOMSource source = new DOMSource(doc);
                    StreamResult result = new StreamResult(new File("C:/Users/ADMIN/OneDrive/Desktop/Personal Projects/MyToDoApp/MyToDoApp/src/Projects/" +projectName + ".xml"));
                    transformer.transform(source, result);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
    private void addTable(){
        tableModel = new DefaultTableModel();
        tableModel.addColumn("No.");
        tableModel.addColumn("Project");
        //tableModel.addColumn("State");

        table = new JTable(tableModel){
        @Override
        public boolean isCellEditable(int row, int column){
            return column ==2;
        }
    };
        JScrollPane scrollPane = new JScrollPane(table);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        // Set column widths
        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50);
        columnModel.getColumn(1).setPreferredWidth(250);
        //columnModel.getColumn(2).setPreferredWidth(200);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = table.getSelectedRow();
                    if (index >=0){
                        String projectName = (String) table.getModel().getValueAt(index, 1);
                        MainMenu mainMenu = new MainMenu(projectName, AppFrame.this);
                        mainMenu.setVisible(true);
                        dispose();
                    }

                }
            }
        });
        table.setShowGrid(false);
        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.DELETE) {
                    // Update the row numbers
                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        tableModel.setValueAt(i + 1, i, 0); // Update the "No." column
                    }
                    // Refresh the table view
                    table.repaint();
                }
            }
        });
    }
    private void loadExistingProjects(){
        File directory = new File("C:/Users/ADMIN/OneDrive/Desktop/Personal Projects/MyToDoApp/MyToDoApp/src/Projects/");
        File[] xmlFiles = directory.listFiles((dir, name) -> name.endsWith(".xml"));

        if (xmlFiles != null) {
            for (File file : xmlFiles) {
                try {
                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document doc = dBuilder.parse(file);

                    // Extract project details from XML
                    String projectName = file.getName();
                    projectName = projectName.substring(0, projectName.lastIndexOf('.'));// Extract project name from XML
                            //String state = ""; // Extract project state from XML

                            // Add project to table
                            tableModel.addRow(new Object[]{tableModel.getRowCount() + 1, projectName});
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

}
