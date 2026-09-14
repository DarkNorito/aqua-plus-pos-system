package aquapluspos;

import aquapluspos.ui.Inventory;

public class AquaPlusPOS {

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            new Inventory().setVisible(true);
        });
    }
}
