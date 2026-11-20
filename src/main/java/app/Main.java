package app;

import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Standalone launcher for the menu module until the shared login flow supplies truck identity. */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        Path dataDirectory = Paths.get("data");
        String truckId = "truck-1";
        for (int index = 0; index < args.length; index++) {
            final String option = args[index];
            if ("--help".equals(option)) {
                System.out.println("TruckTrack menu module: --data-dir PATH --truck-id ID");
                return;
            }
            if (!("--data-dir".equals(option) || "--truck-id".equals(option)) || index + 1 >= args.length) {
                throw new IllegalArgumentException("Expected --data-dir PATH or --truck-id ID. Use --help.");
            }
            final String value = args[++index];
            if (value.trim().isEmpty()) {
                throw new IllegalArgumentException("Option values cannot be blank.");
            }
            if ("--data-dir".equals(option)) {
                dataDirectory = Paths.get(value);
            }
            else {
                truckId = value;
            }
        }
        final Path selectedDirectory = dataDirectory;
        final String selectedTruck = truckId;
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                new AppBuilder(selectedDirectory, selectedTruck).build().setVisible(true);
            }
            catch (Exception exception) {
                JOptionPane.showMessageDialog(null,
                        "TruckTrack could not start: " + exception.getMessage(),
                        "Unable to start", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
