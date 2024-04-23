import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AppFrame appFrame = new AppFrame();
            appFrame.AppGui();
        });
    }
}