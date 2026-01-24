package com.searly.taxcontrol.sii.swingtool;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class SiiSwingToolMain {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            EmpresaEmisoraFrame frame = new EmpresaEmisoraFrame();
            frame.setVisible(true);
        });
    }
}
