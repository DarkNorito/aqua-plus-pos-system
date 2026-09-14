
package aquapluspos.ui;


public class Home extends javax.swing.JFrame {

    JpanelLoader jpload = new JpanelLoader();
    
    public Home() {
        initComponents();
        getContentPane().setBackground(java.awt.Color.BLACK);
        this.setExtendedState(Home.MAXIMIZED_BOTH);
        
        
        
        
        
        
        // --- AQUA PARTICLE FOUNTAIN (IDLE SCREEN) ---
        class Particle {
            double x, y, vx, vy;
            int life, maxLife;
            java.awt.Color color;
            
            Particle(int w, int h) {
                x = w / 2.0; // Start at the exact horizontal center
                y = h;       // Start at the bottom of the panel
                vx = (Math.random() - 0.5) * 6; // Spread outwards left and right
                vy = -Math.random() * 12 - 5;   // Initial upward burst
                maxLife = (int)(Math.random() * 40 + 30);
                life = maxLife;
                // Generate random Aqua/Cyan/Blue water colors
                color = new java.awt.Color(0, (int)(Math.random() * 100 + 155), 255); 
            }
        }

        javax.swing.JPanel fountainPanel = new javax.swing.JPanel() {
            java.util.ArrayList<Particle> particles = new java.util.ArrayList<>();
            javax.swing.Timer pTimer;
            {
                setOpaque(false); // Make the background transparent
                pTimer = new javax.swing.Timer(16, e -> { // Run at ~60 FPS
                    int w = getWidth(), h = getHeight();
                    if (w > 0 && h > 0) {
                        // Spawn 5 new water droplets every frame
                        for (int i = 0; i < 5; i++) particles.add(new Particle(w, h)); 
                    }
                    // Apply gravity and update droplet positions
                    for (int i = particles.size() - 1; i >= 0; i--) {
                        Particle p = particles.get(i);
                        p.x += p.vx;
                        p.vy += 0.5; // Gravity pulling the water down
                        p.y += p.vy;
                        p.life--;
                        if (p.life <= 0) particles.remove(i); // Remove dead droplets
                    }
                    repaint();
                });
                pTimer.start();
            }
            
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                java.awt.Graphics2D g2d = (java.awt.Graphics2D) g;
                // Turn on anti-aliasing for smooth, high-quality circles
                g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                for (Particle p : particles) {
                    // Fade out as they fall
                    int alpha = Math.max(0, (int) (255 * ((double) p.life / p.maxLife)));
                    g2d.setColor(new java.awt.Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), alpha));
                    g2d.fillOval((int) p.x, (int) p.y, 10, 10);
                }
            }
        };
        
        // Inject the fountain into the empty panel_load
        panel_load.setLayout(new java.awt.BorderLayout());
        panel_load.add(fountainPanel, java.awt.BorderLayout.CENTER);
        // ---------------------------------------------
        
        
        
        
        
        
        
        
        // Custom Animated Glowing Border Timer
        javax.swing.Timer glowingBorderTimer = new javax.swing.Timer(50, new java.awt.event.ActionListener() {
            float hue = 0.45f; // Start at Aqua Blue
            boolean shiftingUp = true;
            
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                // Smoothly shift the color hue back and forth
                if (shiftingUp) {
                    hue += 0.005f;
                    if (hue > 0.85f) shiftingUp = false; // Reverse at Purple/Pink
                } else {
                    hue -= 0.005f;
                    if (hue < 0.45f) shiftingUp = true; // Reverse at Aqua Blue
                }
                
                // Generate the new color and apply a 3-pixel thick rounded border
                java.awt.Color animatedColor = java.awt.Color.getHSBColor(hue, 0.8f, 0.9f);
                javax.swing.border.Border smoothBorder = javax.swing.BorderFactory.createLineBorder(animatedColor, 3, true);
                
                // Apply the animated border to your main panels
                jPanel1.setBorder(smoothBorder); // Sidebar panel
                panel_load.setBorder(smoothBorder); // Main loading panel
                jPanel3.setBorder(smoothBorder); // Top panel
            }
        });
        glowingBorderTimer.start();
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
    }

   
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        home_bnt_grp = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jToggleButton1 = new javax.swing.JToggleButton();
        jToggleButton2 = new javax.swing.JToggleButton();
        jToggleButton3 = new javax.swing.JToggleButton();
        jToggleButton4 = new javax.swing.JToggleButton();
        jToggleButton5 = new javax.swing.JToggleButton();
        jToggleButton6 = new javax.swing.JToggleButton();
        jToggleButton7 = new javax.swing.JToggleButton();
        panel_load = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(0, 0, 0));
        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jToggleButton1.setBackground(new java.awt.Color(102, 102, 255));
        home_bnt_grp.add(jToggleButton1);
        jToggleButton1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jToggleButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/aquapluspos/images/search x30.png"))); // NOI18N
        jToggleButton1.setText("DASHBOARD");
        jToggleButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jToggleButton1MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jToggleButton1MouseExited(evt);
            }
        });
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });

        home_bnt_grp.add(jToggleButton2);
        jToggleButton2.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jToggleButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/aquapluspos/images/invo.png"))); // NOI18N
        jToggleButton2.setText("INVENTORY");
        jToggleButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton2ActionPerformed(evt);
            }
        });

        home_bnt_grp.add(jToggleButton3);
        jToggleButton3.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jToggleButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/aquapluspos/images/sales_menu.png"))); // NOI18N
        jToggleButton3.setText("EMPLOYEE");
        jToggleButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton3ActionPerformed(evt);
            }
        });

        home_bnt_grp.add(jToggleButton4);
        jToggleButton4.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jToggleButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/aquapluspos/images/product.png"))); // NOI18N
        jToggleButton4.setText("PRODUCT");
        jToggleButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton4ActionPerformed(evt);
            }
        });

        home_bnt_grp.add(jToggleButton5);
        jToggleButton5.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jToggleButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/aquapluspos/images/customer.png"))); // NOI18N
        jToggleButton5.setText("CUSTOMER");
        jToggleButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton5ActionPerformed(evt);
            }
        });

        home_bnt_grp.add(jToggleButton6);
        jToggleButton6.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jToggleButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/aquapluspos/images/supplier.png"))); // NOI18N
        jToggleButton6.setText("SUPPLIER");
        jToggleButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton6ActionPerformed(evt);
            }
        });

        home_bnt_grp.add(jToggleButton7);
        jToggleButton7.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jToggleButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/aquapluspos/images/emp.png"))); // NOI18N
        jToggleButton7.setText("REVIEWS");
        jToggleButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton7ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jToggleButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(46, 46, 46)
                .addComponent(jToggleButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButton4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButton5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButton6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButton7)
                .addContainerGap(206, Short.MAX_VALUE))
        );

        panel_load.setBackground(new java.awt.Color(0, 0, 0));
        panel_load.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout panel_loadLayout = new javax.swing.GroupLayout(panel_load);
        panel_load.setLayout(panel_loadLayout);
        panel_loadLayout.setHorizontalGroup(
            panel_loadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1056, Short.MAX_VALUE)
        );
        panel_loadLayout.setVerticalGroup(
            panel_loadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanel3.setBackground(new java.awt.Color(0, 0, 0));
        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 60, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panel_load, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panel_load, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        // customer load
        
       // customer cus = new customer();
        //jpload.jPanelLoader(panel_load, cus);
       
        
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    private void jToggleButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton2ActionPerformed
       
      //supplier sp = new supplier();
      //jpload.jPanelLoader(panel_load, sp);
      
        
        
    }//GEN-LAST:event_jToggleButton2ActionPerformed

    private void jToggleButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton4ActionPerformed
        // emp load
        
        //employee emp =new employee();
        //jpload.jPanelLoader(panel_load, emp);
        
        
    }//GEN-LAST:event_jToggleButton4ActionPerformed

    private void jToggleButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton3ActionPerformed
        
        
        
           // emp load
        
        employee emp =new employee();
        jpload.jPanelLoader(panel_load, emp);
        
        
        
    }//GEN-LAST:event_jToggleButton3ActionPerformed

    private void jToggleButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton5ActionPerformed
        
       //sale sl = new sale();
       //jpload.jPanelLoader(panel_load, sl);
       
        
        
    }//GEN-LAST:event_jToggleButton5ActionPerformed

    private void jToggleButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton6ActionPerformed
       
        //Invoice inv = new Invoice();
        //jpload.jPanelLoader(panel_load, inv);
        
        
        
    }//GEN-LAST:event_jToggleButton6ActionPerformed

    private void jToggleButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton7ActionPerformed
       
        //reports rp = new reports();
        //jpload.jPanelLoader(panel_load, rp);
        
    }//GEN-LAST:event_jToggleButton7ActionPerformed

    private void jToggleButton1MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jToggleButton1MouseEntered


        // Slides the text away from the icon and makes the font slightly larger
jToggleButton1.setIconTextGap(15); 
jToggleButton1.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 15));



        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButton1MouseEntered

    private void jToggleButton1MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jToggleButton1MouseExited


            // Returns the spacing and font to their original default sizes
jToggleButton1.setIconTextGap(4); 
jToggleButton1.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 14));



        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButton1MouseExited

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Home.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Home.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Home.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Home.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Home().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup home_bnt_grp;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JToggleButton jToggleButton2;
    private javax.swing.JToggleButton jToggleButton3;
    private javax.swing.JToggleButton jToggleButton4;
    private javax.swing.JToggleButton jToggleButton5;
    private javax.swing.JToggleButton jToggleButton6;
    private javax.swing.JToggleButton jToggleButton7;
    private javax.swing.JPanel panel_load;
    // End of variables declaration//GEN-END:variables
}
