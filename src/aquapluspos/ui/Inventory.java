package aquapluspos.ui;

import aquapluspos.database.DBConnection;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class Inventory extends javax.swing.JFrame {

    private int selectedProductID = 1;
    private String selectedProductName = "RO Unit 5-stage";

    public Inventory() {
        initComponents();
        setupItemSelection();
        loadStockData();
    }

    private void setupItemSelection() {
        addSelectListener(jLabel7, 1, "RO Unit 5-stage");
        addSelectListener(jLabel8, 2, "RO Unit 7-stage");
        addSelectListener(jLabel9, 3, "UV Filter Unit");
        addSelectListener(jLabel2, 1, "Filter Units Category");
        addSelectListener(jLabel3, 2, "Cartridges Category");
        addSelectListener(jLabel4, 3, "Spare Parts Category");
        addSelectListener(jLabel5, 4, "Accessories Category");
        addSelectListener(jLabel18, 4, "Cartridge Type A");
        addSelectListener(jLabel19, 5, "Cartridge Type B");

        selectItem(1, "RO Unit 5-stage", jLabel7);
    }

    private void addSelectListener(JLabel label, int productId, String productName) {
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                selectItem(productId, productName, label);
            }
        });
    }

    private void selectItem(int id, String name, JLabel label) {
        selectedProductID = id;
        selectedProductName = name;

        resetLabelStyles();

        if (label != null) {
            label.setForeground(new Color(255, 255, 255));
            label.setFont(label.getFont().deriveFont(Font.BOLD));
        }

        loadStockData();
    }

    private void resetLabelStyles() {
        JLabel[] blueLabels = {jLabel2, jLabel3, jLabel4, jLabel5, jLabel7, jLabel8, jLabel9};
        for (JLabel l : blueLabels) {
            l.setForeground(new Color(0, 153, 255));
            l.setFont(l.getFont().deriveFont(Font.PLAIN));
        }
        jLabel18.setForeground(new Color(204, 204, 204));
        jLabel18.setFont(jLabel18.getFont().deriveFont(Font.PLAIN));
        jLabel19.setForeground(new Color(255, 51, 51));
        jLabel19.setFont(jLabel19.getFont().deriveFont(Font.PLAIN));
    }

    private void loadStockData() {
        jLabel10.setText(selectedProductName);

        try (Connection conn = DBConnection.getConnection()) {
            // 1. Fetch Product and Inventory metrics using schema JOIN (product & inventory)
            String sql = "SELECT p.productName, i.currentStock, i.minimumThreshold " +
                         "FROM product p " +
                         "LEFT JOIN inventory i ON p.productID = i.productID " +
                         "WHERE p.productID = ?";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, selectedProductID);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String dbName = rs.getString("productName");
                int currentStock = rs.getInt("currentStock");
                int minimumThreshold = rs.getInt("minimumThreshold");

                if (dbName != null && !dbName.isBlank()) {
                    selectedProductName = dbName;
                    jLabel10.setText(dbName);
                }

                jLabel14.setText(String.valueOf(currentStock));
                jLabel15.setText(String.valueOf(minimumThreshold));
                updateStatusDisplay(currentStock, minimumThreshold);
            } else {
                // If inventory row doesn't exist yet for this productID, insert default inventory record
                PreparedStatement insertPs = conn.prepareStatement(
                    "INSERT INTO inventory (productID, currentStock, minimumThreshold) VALUES (?, 0, 10)"
                );
                insertPs.setInt(1, selectedProductID);
                insertPs.executeUpdate();
                insertPs.close();

                jLabel14.setText("0");
                jLabel15.setText("10");
                updateStatusDisplay(0, 10);
            }

            rs.close();
            ps.close();

            // 2. Fetch Compatible Cartridges from cartridge_compatibility table
            loadCompatibleCartridges(conn);

            // 3. Check for any Low-Stock Alerts across the entire inventory database
            checkGlobalLowStockAlerts(conn);

        } catch (Exception e) {
            System.err.println("Database load error: " + e.getMessage());
        }
    }

    private void loadCompatibleCartridges(Connection conn) {
        try {
            String sql = "SELECT c.productName " +
                         "FROM cartridge_compatibility cc " +
                         "JOIN product c ON cc.cartridgeID = c.productID " +
                         "WHERE cc.filterUnitID = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, selectedProductID);
            ResultSet rs = ps.executeQuery();

            List<String> list = new ArrayList<>();
            while (rs.next()) {
                list.add(rs.getString("productName"));
            }
            rs.close();
            ps.close();

            if (!list.isEmpty()) {
                jLabel18.setText(list.get(0));
                if (list.size() > 1) {
                    jLabel19.setText(list.get(1));
                } else {
                    jLabel19.setText("Cartridge Type B");
                }
            } else {
                jLabel18.setText("Cartridge Type A");
                jLabel19.setText("Cartridge Type B");
            }
        } catch (Exception ignored) {}
    }

    private void updateStatusDisplay(int currentStock, int minimumThreshold) {
        if (currentStock <= 0) {
            jLabel16.setText("Out of stock");
            jPanel6.setBackground(new Color(255, 51, 51));
        } else if (currentStock <= minimumThreshold) {
            jLabel16.setText("Low stock");
            jPanel6.setBackground(new Color(255, 153, 0));
        } else {
            jLabel16.setText("In stock");
            jPanel6.setBackground(new Color(0, 204, 102));
        }
    }

    private void checkGlobalLowStockAlerts(Connection conn) {
        try {
            String sql = "SELECT p.productName, i.currentStock, i.minimumThreshold " +
                         "FROM inventory i " +
                         "JOIN product p ON i.productID = p.productID " +
                         "WHERE i.currentStock <= i.minimumThreshold " +
                         "ORDER BY i.currentStock ASC LIMIT 1";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String lowItemName = rs.getString("productName");
                int stock = rs.getInt("currentStock");
                int threshold = rs.getInt("minimumThreshold");
                jPanel7.setVisible(true);
                if (stock <= 0) {
                    jLabel20.setText("Alert: " + lowItemName + " is out of stock!");
                } else {
                    jLabel20.setText("Low-stock alert: " + lowItemName + " below reorder level (" + stock + " / " + threshold + ")");
                }
            } else {
                jPanel7.setVisible(false);
            }
            rs.close();
            ps.close();
        } catch (Exception ignored) {}
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {

        jButton2 = new javax.swing.JButton();
        jScrollBar1 = new javax.swing.JScrollBar();
        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jLabel20 = new javax.swing.JLabel();

        jButton2.setText("jButton2");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Aqua Plus POS - Inventory Management System");

        jPanel2.setBackground(new java.awt.Color(0, 0, 0));
        jPanel2.setForeground(new java.awt.Color(0, 153, 255));

        jPanel1.setBackground(new java.awt.Color(51, 51, 51));
        jPanel1.setForeground(new java.awt.Color(204, 204, 204));

        jLabel1.setForeground(new java.awt.Color(153, 153, 153));
        jLabel1.setText("Items");

        jLabel2.setForeground(new java.awt.Color(0, 153, 255));
        jLabel2.setText("Filter units");

        jLabel3.setForeground(new java.awt.Color(0, 153, 255));
        jLabel3.setText("Cartridges");

        jLabel4.setForeground(new java.awt.Color(0, 153, 255));
        jLabel4.setText("Spare parts");

        jLabel5.setForeground(new java.awt.Color(0, 153, 255));
        jLabel5.setText("Accessories");

        jLabel6.setForeground(new java.awt.Color(153, 153, 153));
        jLabel6.setText("Categories");

        jLabel7.setForeground(new java.awt.Color(0, 153, 255));
        jLabel7.setText("RO Unit 5-stage");

        jLabel8.setForeground(new java.awt.Color(0, 153, 255));
        jLabel8.setText("RO Unit 7-stage");

        jLabel9.setForeground(new java.awt.Color(0, 153, 255));
        jLabel9.setText("UV Filter Unit");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(53, 53, 53)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel2)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(33, 33, 33)
                        .addComponent(jLabel7))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(33, 33, 33)
                        .addComponent(jLabel8))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(33, 33, 33)
                        .addComponent(jLabel9))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel6)))
                .addContainerGap(115, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel6)
                .addGap(10, 10, 10)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(jLabel7)
                .addGap(18, 18, 18)
                .addComponent(jLabel8)
                .addGap(18, 18, 18)
                .addComponent(jLabel9)
                .addGap(191, 191, 191))
        );

        jPanel3.setBackground(new java.awt.Color(51, 51, 51));

        jButton1.setBackground(new java.awt.Color(102, 102, 102));
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("Edit stock");
        jButton1.setToolTipText("Set exact stock quantity");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton3.setBackground(new java.awt.Color(0, 153, 255));
        jButton3.setForeground(new java.awt.Color(255, 255, 255));
        jButton3.setText("+ Add stock");
        jButton3.setToolTipText("Add to existing stock");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setBackground(new java.awt.Color(102, 102, 102));
        jButton4.setForeground(new java.awt.Color(255, 255, 255));
        jButton4.setText("Reorder Level");
        jButton4.setToolTipText("Update minimum threshold level");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setBackground(new java.awt.Color(0, 204, 102));
        jButton5.setForeground(new java.awt.Color(255, 255, 255));
        jButton5.setText("+ Add Item");
        jButton5.setToolTipText("Add new product to inventory");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jButton6.setBackground(new java.awt.Color(255, 51, 51));
        jButton6.setForeground(new java.awt.Color(255, 255, 255));
        jButton6.setText("Delete Item");
        jButton6.setToolTipText("Delete selected item from database");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 16));
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));
        jLabel10.setText("RO Unit 5-stage");

        jLabel11.setText("Current stock");

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 14));
        jLabel14.setText("0");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11)
                    .addComponent(jLabel14))
                .addContainerGap(145, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel14)
                .addGap(0, 10, Short.MAX_VALUE))
        );

        jLabel12.setText("Reorder level");

        jLabel15.setFont(new java.awt.Font("Segoe UI", 1, 14));
        jLabel15.setText("10");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel12)
                    .addComponent(jLabel15))
                .addContainerGap(145, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel15)
                .addGap(0, 10, Short.MAX_VALUE))
        );

        jPanel6.setBackground(new java.awt.Color(0, 204, 102));

        jLabel13.setText(" Status");

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 14));
        jLabel16.setText("In stock");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel13)
                    .addComponent(jLabel16))
                .addContainerGap(137, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel16)
                .addGap(0, 10, Short.MAX_VALUE))
        );

        jLabel17.setForeground(new java.awt.Color(204, 204, 204));
        jLabel17.setText("Compatible cartridges");

        jLabel18.setForeground(new java.awt.Color(204, 204, 204));
        jLabel18.setText(" Cartridge Type A");

        jLabel19.setForeground(new java.awt.Color(255, 51, 51));
        jLabel19.setText("Cartridge Type B");

        jPanel7.setBackground(new java.awt.Color(255, 0, 51));

        jLabel20.setForeground(new java.awt.Color(255, 255, 255));
        jLabel20.setText("Low-stock alert: cartridge type B below reorder level");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel20)
                .addContainerGap(304, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(jLabel20)
                .addGap(0, 7, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGap(60, 60, 60)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton6)
                .addGap(9, 9, 9))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel17)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(64, 64, 64)
                                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(61, 61, 61)
                                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel19)
                                .addComponent(jLabel18))))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(114, 114, 114)
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(252, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton5)
                            .addComponent(jButton4)
                            .addComponent(jButton1)
                            .addComponent(jButton3)
                            .addComponent(jButton6)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(jLabel10)))
                .addGap(28, 28, 28)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(37, 37, 37)
                .addComponent(jLabel17)
                .addGap(37, 37, 37)
                .addComponent(jLabel18)
                .addGap(18, 18, 18)
                .addComponent(jLabel19)
                .addGap(18, 18, 18)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(67, 67, 67)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 331, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(81, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }

    // CRUD 1: UPDATE Stock Quantity (Edit Stock)
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            String input = JOptionPane.showInputDialog(
                this, 
                "Enter new stock quantity for " + selectedProductName + ":"
            );
            if (input == null || input.isBlank()) {
                return;
            }

            int qty = Integer.parseInt(input.trim());

            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(
                    "UPDATE inventory SET currentStock = ? WHERE productID = ?"
                );
                ps.setInt(1, qty);
                ps.setInt(2, selectedProductID);
                int updated = ps.executeUpdate();
                ps.close();

                if (updated == 0) {
                    PreparedStatement insertPs = conn.prepareStatement(
                        "INSERT INTO inventory (productID, currentStock, minimumThreshold) VALUES (?, ?, 10)"
                    );
                    insertPs.setInt(1, selectedProductID);
                    insertPs.setInt(2, qty);
                    insertPs.executeUpdate();
                    insertPs.close();
                }
            }

            JOptionPane.showMessageDialog(this, "Stock set successfully!");
            loadStockData();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    // CRUD 1: UPDATE Incremental Stock (+ Add Stock)
    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            String input = JOptionPane.showInputDialog(
                this, 
                "Enter quantity to add for " + selectedProductName + ":"
            );
            if (input == null || input.isBlank()) {
                return;
            }

            int qty = Integer.parseInt(input.trim());

            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(
                    "UPDATE inventory SET currentStock = currentStock + ? WHERE productID = ?"
                );
                ps.setInt(1, qty);
                ps.setInt(2, selectedProductID);
                int updated = ps.executeUpdate();
                ps.close();

                if (updated == 0) {
                    PreparedStatement insertPs = conn.prepareStatement(
                        "INSERT INTO inventory (productID, currentStock, minimumThreshold) VALUES (?, ?, 10)"
                    );
                    insertPs.setInt(1, selectedProductID);
                    insertPs.setInt(2, qty);
                    insertPs.executeUpdate();
                    insertPs.close();
                }
            }

            JOptionPane.showMessageDialog(this, "Stock updated successfully!");
            loadStockData();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    // CRUD 1: UPDATE Reorder Level (minimumThreshold)
    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            String input = JOptionPane.showInputDialog(
                this, 
                "Enter minimum threshold for " + selectedProductName + ":"
            );
            if (input == null || input.isBlank()) {
                return;
            }

            int level = Integer.parseInt(input.trim());

            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(
                    "UPDATE inventory SET minimumThreshold = ? WHERE productID = ?"
                );
                ps.setInt(1, level);
                ps.setInt(2, selectedProductID);
                ps.executeUpdate();
                ps.close();
            }

            JOptionPane.showMessageDialog(this, "Minimum threshold updated successfully!");
            loadStockData();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    // CRUD 2: CREATE New Product & Inventory Record
    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            String name = JOptionPane.showInputDialog(this, "Enter New Product Name:");
            if (name == null || name.isBlank()) return;

            String catStr = JOptionPane.showInputDialog(this, "Enter Category ID (1 = Filter Units, 2 = Cartridges, 3 = Spare Parts, 4 = Accessories):");
            int catId = (catStr != null && !catStr.isBlank()) ? Integer.parseInt(catStr.trim()) : 1;

            String priceStr = JOptionPane.showInputDialog(this, "Enter Unit Price (e.g. 15000.00):");
            double price = (priceStr != null && !priceStr.isBlank()) ? Double.parseDouble(priceStr.trim()) : 0.0;

            String stockStr = JOptionPane.showInputDialog(this, "Enter Initial Stock:");
            int stock = (stockStr != null && !stockStr.isBlank()) ? Integer.parseInt(stockStr.trim()) : 0;

            String thresholdStr = JOptionPane.showInputDialog(this, "Enter Minimum Threshold:");
            int threshold = (thresholdStr != null && !thresholdStr.isBlank()) ? Integer.parseInt(thresholdStr.trim()) : 10;

            try (Connection conn = DBConnection.getConnection()) {
                // 1. Insert into product table
                PreparedStatement ps1 = conn.prepareStatement(
                    "INSERT INTO product (productName, categoryID, unitPrice) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
                );
                ps1.setString(1, name.trim());
                ps1.setInt(2, catId);
                ps1.setDouble(3, price);
                ps1.executeUpdate();

                ResultSet rs = ps1.getGeneratedKeys();
                int newProdId = -1;
                if (rs.next()) {
                    newProdId = rs.getInt(1);
                }
                rs.close();
                ps1.close();

                if (newProdId > 0) {
                    // 2. Insert into inventory table
                    PreparedStatement ps2 = conn.prepareStatement(
                        "INSERT INTO inventory (productID, currentStock, minimumThreshold) VALUES (?, ?, ?)"
                    );
                    ps2.setInt(1, newProdId);
                    ps2.setInt(2, stock);
                    ps2.setInt(3, threshold);
                    ps2.executeUpdate();
                    ps2.close();

                    selectedProductID = newProdId;
                    selectedProductName = name.trim();
                }
            }

            JOptionPane.showMessageDialog(this, "Product '" + name + "' added successfully!");
            loadStockData();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric values.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding product: " + e.getMessage());
        }
    }

    // CRUD 2: DELETE Product & Related Inventory Records
    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete '" + selectedProductName + "' (ID: " + selectedProductID + ")?",
            "Confirm Delete Product",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            // 1. Remove references from cartridge_compatibility
            PreparedStatement ps1 = conn.prepareStatement(
                "DELETE FROM cartridge_compatibility WHERE filterUnitID = ? OR cartridgeID = ?"
            );
            ps1.setInt(1, selectedProductID);
            ps1.setInt(2, selectedProductID);
            ps1.executeUpdate();
            ps1.close();

            // 2. Remove row from inventory table
            PreparedStatement ps2 = conn.prepareStatement("DELETE FROM inventory WHERE productID = ?");
            ps2.setInt(1, selectedProductID);
            ps2.executeUpdate();
            ps2.close();

            // 3. Remove row from product table
            PreparedStatement ps3 = conn.prepareStatement("DELETE FROM product WHERE productID = ?");
            ps3.setInt(1, selectedProductID);
            int deleted = ps3.executeUpdate();
            ps3.close();

            if (deleted > 0) {
                JOptionPane.showMessageDialog(this, "Product deleted successfully!");
                selectItem(1, "RO Unit 5-stage", jLabel7);
            } else {
                JOptionPane.showMessageDialog(this, "Product not found in database.");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error deleting product: " + e.getMessage());
        }
    }

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Inventory.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Inventory.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Inventory.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Inventory.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Inventory().setVisible(true);
            }
        });
    }

    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollBar jScrollBar1;
}
