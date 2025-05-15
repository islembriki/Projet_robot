import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
public class RobotDeliveryGUI extends JFrame {
    private JPanel cardPanel;// Panel principal pour les cartes
    private CardLayout cardLayout;//gestionnaire de cartes 
    private RobotLivraison robot;// Instance de RobotLivraison qui va executer les actions 
    private static final int GRID_SIZE = 20;//taille de grille ou le robot vas se deplacer 
    private static final int CELL_SIZE = 30;//taille de chaque cellule en pixels 
    private static final String CARD_SETUP = "setup";//ecran de verification 
    private static final String CARD_DELIVERY = "delivery";//ecran de livraison 
    private static final String CARD_COMPLETE = "complete";//ecran de fin de livraison 
    private RobotFacePanel robotFacePanel;// Panneau pour afficher le visage du robot
    private JTextField colisField;// Champ pour entrer le nom du colis
    private JTextField destinationField;// Champ pour entrer la destination
    private JTextField reseauField; // Champ pour entrer le réseau
    private JCheckBox ecoNumeriqueCheckBox;// Case à cocher pour le mode éco-numérique
    private EnergyBar energyBar; // Barre d'énergie principale
    private EnergyBar kersBar; // Barre d'énergie KERS (système de récupération d'énergie cinétique)
    private JButton connectButton;// Bouton pour se connecter
    private JButton startButton;// Bouton pour démarrer la livraison
    private GridPanel gridPanel;//panneau pour afficher la grille de déplacement
    private LogPanel logPanel;// Panneau pour afficher l'historique des actions
    private EnergyBar deliveryEnergyBar;// Barre d'énergie dans l'écran de livraison
    private EnergyBar deliveryKersBar;// Barre d'énergie KERS dans l'écran de livraison
    private JButton ecoModeButton; // Bouton pour activer/désactiver le mode économiseur
    private JLabel idleStateLabel;// Étiquette pour afficher l'état du robot (actif/veille
    private JLabel kersStateLabel;// Étiquette pour afficher l'état du KERS
    private JLabel ecoNumeriqueStateLabel;// Étiquette pour afficher l'état du mode éco-numériqu
    private JButton newDeliveryButton; // Bouton pour démarrer une nouvelle livraison
    private JButton stopButton;// Bouton pour arrêter l'application
    public RobotDeliveryGUI() {
        // Initialisation du robot avec un ID, une position initiale (0,0) et 100% d'énergie
        robot = new RobotLivraison("DEL-001", 0, 0, 100);
        setTitle("Gestion de robot");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        //creation des differents cartes d' affichages et l'ajout au cardpanel 
        createSetupCard();
        createDeliveryCard();
        createCompletionCard();
        add(cardPanel);
        cardLayout.show(cardPanel, CARD_SETUP);
        //  // Démarre le timer pour mettre à jour l'interface régulièrement qui est indispensable pour le mode idle 
        startUpdateTimer();
    }
    //l'ecran de configuration 
    private void createSetupCard() {
        //panneau avec borderlayout 
        JPanel setupCard = new JPanel(new BorderLayout(20, 20));
        setupCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));//marge autour de panneau
        // Panneau gauche pour les barres d'énergie 
        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 0, 20));
        leftPanel.setPreferredSize(new Dimension(100, 300));
        // Création du panneau pour la barre d'énergie principale
        JPanel energyPanel = new JPanel(new BorderLayout());
        JLabel energyLabel = new JLabel("Énergie", JLabel.CENTER);
        energyBar = new EnergyBar(Color.GREEN);
        energyPanel.add(energyLabel, BorderLayout.NORTH);
        energyPanel.add(energyBar, BorderLayout.CENTER);
        // Création du panneau pour la barre d'énergie KERS 
        JPanel kersPanel = new JPanel(new BorderLayout());
        JLabel kersLabel = new JLabel("KERS", JLabel.CENTER);
        kersBar = new EnergyBar(Color.ORANGE);
        kersPanel.add(kersLabel, BorderLayout.NORTH);
        kersPanel.add(kersBar, BorderLayout.CENTER);
        // Ajout des panneaux d'énergie au panneau gauche
        leftPanel.add(energyPanel);
        leftPanel.add(kersPanel);
        // Panneau central pour le visage du robot et les champs de saisie
        JPanel centerPanel = new JPanel(new BorderLayout(20, 20));
        // Création du panneau pour le visage du robot
        robotFacePanel = new RobotFacePanel();
        robotFacePanel.setPreferredSize(new Dimension(200, 200));
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        inputPanel.add(new JLabel("Colis à transporter:"));
        colisField = new JTextField();
        inputPanel.add(colisField);
        inputPanel.add(new JLabel("Destination:"));
        destinationField = new JTextField();
        inputPanel.add(destinationField);
        inputPanel.add(new JLabel("Réseau:"));
        reseauField = new JTextField();
        inputPanel.add(reseauField);
        inputPanel.add(new JLabel("Mode Éco-Numérique:"));
        ecoNumeriqueCheckBox = new JCheckBox();
        inputPanel.add(ecoNumeriqueCheckBox);
        centerPanel.add(robotFacePanel, BorderLayout.CENTER);
        centerPanel.add(inputPanel, BorderLayout.SOUTH);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        connectButton = new JButton("Se connecter");
        connectButton.addActionListener(e -> connectRobot());
        startButton = new JButton("Démarrer");
        startButton.setEnabled(false);
        startButton.addActionListener(e -> startDelivery());
        buttonPanel.add(connectButton);
        buttonPanel.add(startButton);
        setupCard.add(leftPanel, BorderLayout.WEST);
        setupCard.add(centerPanel, BorderLayout.CENTER);
        setupCard.add(buttonPanel, BorderLayout.SOUTH);
        cardPanel.add(setupCard, CARD_SETUP);
    }
    private void createDeliveryCard() {
        JPanel deliveryCard = new JPanel(new BorderLayout(10, 10));
        deliveryCard.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        gridPanel = new GridPanel();
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 0));
        logPanel = new LogPanel();
        logPanel.setPreferredSize(new Dimension(400, 200));
        JPanel statusPanel = new JPanel(new GridLayout(5, 1, 5, 5));
        JPanel deliveryEnergyPanel = new JPanel(new BorderLayout(5, 5));
        deliveryEnergyPanel.add(new JLabel("Énergie:"), BorderLayout.WEST);
        deliveryEnergyBar = new EnergyBar(Color.GREEN);
        deliveryEnergyPanel.add(deliveryEnergyBar, BorderLayout.CENTER);
        JPanel deliveryKersPanel = new JPanel(new BorderLayout(5, 5));
        deliveryKersPanel.add(new JLabel("KERS:"), BorderLayout.WEST);
        deliveryKersBar = new EnergyBar(Color.ORANGE);
        deliveryKersPanel.add(deliveryKersBar, BorderLayout.CENTER);
        ecoModeButton = new JButton("Activer mode économiseur d'énergie");
        ecoModeButton.addActionListener(e -> toggleEcoMode());
        idleStateLabel = new JLabel("État: Actif");
        kersStateLabel = new JLabel("KERS: Non utilisé");
        ecoNumeriqueStateLabel = new JLabel("Éco-Numérique: Désactivé");
        statusPanel.add(deliveryEnergyPanel);
        statusPanel.add(deliveryKersPanel);
        statusPanel.add(ecoModeButton);
        statusPanel.add(idleStateLabel);
        statusPanel.add(ecoNumeriqueStateLabel);
        bottomPanel.add(logPanel, BorderLayout.CENTER);
        bottomPanel.add(statusPanel, BorderLayout.EAST);
        deliveryCard.add(gridPanel, BorderLayout.CENTER);
        deliveryCard.add(bottomPanel, BorderLayout.SOUTH);
        cardPanel.add(deliveryCard, CARD_DELIVERY);
    }
    private void createCompletionCard() {
        JPanel completionCard = new JPanel(new BorderLayout());
        completionCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel completionLabel = new JLabel("Livraison effectuée!", JLabel.CENTER);
        completionLabel.setFont(new Font("Arial", Font.BOLD, 24));
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        newDeliveryButton = new JButton("Nouvelle livraison");
        newDeliveryButton.addActionListener(e -> cardLayout.show(cardPanel, CARD_SETUP));
        stopButton = new JButton("Arrêter");
        stopButton.addActionListener(e -> {
            robot.arreter();
            System.exit(0);
        });
        buttonPanel.add(newDeliveryButton);
        buttonPanel.add(stopButton);
        completionCard.add(completionLabel, BorderLayout.CENTER);
        completionCard.add(buttonPanel, BorderLayout.SOUTH);
        cardPanel.add(completionCard, CARD_COMPLETE);
    }
    private void connectRobot() {
        String reseau = reseauField.getText().trim();
        if (reseau.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez entrer un nom de réseau", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            if (!robot.isEnMarche()) {
                robot.demarrer();
            }
            robot.connecter(reseau);
            if (ecoNumeriqueCheckBox.isSelected()) {
                try {
                    robot.activerModeEcoNumerique();
                } catch (RobotException e) {
                    logPanel.addLog("Erreur: " + e.getMessage());
                }
            }
            robotFacePanel.setSmiling(true);
            startButton.setEnabled(true);
            connectButton.setEnabled(false);
        } catch (RobotException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors de la connexion: " + e.getMessage(),  "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void startDelivery() {
        String colis = colisField.getText().trim();
        String destination = destinationField.getText().trim();
        if (colis.isEmpty() || destination.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez entrer un colis et une destination",   "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            robot.chargerColis(destination, colis);
            gridPanel.setupDelivery();
            cardLayout.show(cardPanel, CARD_DELIVERY);
            updateLog();
        } catch (RobotException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement du colis: " + e.getMessage(), 
                                        "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void toggleEcoMode() {
        if (robot.isEnMarche()) {
            if (ecoModeButton.getText().contains("Activer")) {
                robot.activerModeEconomiseur();
                ecoModeButton.setText("Désactiver mode économiseur d'énergie");
            } else {
                robot.desactiverModeEconomiseur();
                ecoModeButton.setText("Activer mode économiseur d'énergie");
            }
            updateLog();
        }
    }
    private void updateLog() {
        logPanel.clear();
        for (String line : robot.getHistorique().split("\n")) {
            if (!line.isEmpty() && !line.startsWith("Historique des actions")) {
                logPanel.addLog(line.replace(" - ", ""));
            }
        }
    }
    private void startUpdateTimer() {//la fonction fondamentale
    final boolean[] kersInUse = {false};
    Timer timer = new Timer(500, e -> {
        energyBar.setValue(robot.getEnergie());
        kersBar.setValue(robot.getEnergieRecuperee());
        deliveryEnergyBar.setValue(robot.getEnergie());
        deliveryKersBar.setValue(robot.getEnergieRecuperee());
        idleStateLabel.setText("État: " + (robot.isEnModeVeille() ? "En veille" : "Actif"));
        kersStateLabel.setText("KERS: " + (robot.getEnergieRecuperee() > 0 ? "Disponible (" + robot.getEnergieRecuperee() + "%)" : "Non disponible"));
        ecoNumeriqueStateLabel.setText("Éco-Numérique: " + (robot.isEnModeEcoNumerique() ? "Activé" : "Désactivé"));
        if (robot.getEnergie() < 20 && !robot.isEnModeEconomiseur()) {
            robot.activerModeEconomiseur();
            ecoModeButton.setText("Désactiver mode économiseur d'énergie");
            updateLog();
        }
        if (robot.getEnergie() == 0 && robot.getEnergieRecuperee() > 0 && !kersInUse[0]) {
            kersInUse[0] = true;
            try {
                int kersToUse = Math.min(5, robot.getEnergieRecuperee());
                robot.utiliserEnergieRecuperee(kersToUse);
                robot.recharger(kersToUse);
                JOptionPane.showMessageDialog(RobotDeliveryGUI.this,
                    "Énergie épuisée! Utilisation de " + kersToUse + "% d'énergie KERS de secours.", "KERS activé", JOptionPane.INFORMATION_MESSAGE);
                updateLog();
            } catch (RobotException ex) {
                JOptionPane.showMessageDialog(RobotDeliveryGUI.this,
                    "Erreur lors de l'utilisation de l'énergie KERS: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            } finally {
                Timer resetTimer = new Timer(2000, resetEvent -> {
                    kersInUse[0] = false;
                });
                resetTimer.setRepeats(false);
                resetTimer.start();
            }
        }
    });
    timer.start();
}
    class RobotFacePanel extends JPanel {
        private boolean smiling = false;
        private Point eyeTarget = new Point(0, 0);
        public RobotFacePanel() {
            setBackground(new Color(230, 230, 230));
            setBorder(BorderFactory.createLineBorder(new Color(139, 69, 19), 3));
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {// Met à jour la position de la cible des yeux
                    eyeTarget = e.getPoint();
                    repaint();
                }
            });
        }
        public void setSmiling(boolean smiling) {
            this.smiling = smiling;
            repaint();
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int width = getWidth();
            int height = getHeight();
            g2d.setColor(new Color(139, 69, 19));
            g2d.fillRoundRect(width/4, height/8, width/2, height*6/8, 20, 20);
            g2d.setColor(Color.WHITE);
            int eyeSize = width/10;
            int leftEyeX = width*3/8;
            int rightEyeX = width*5/8;
            int eyeY = height*3/8;
            g2d.fillOval(leftEyeX - eyeSize/2, eyeY - eyeSize/2, eyeSize, eyeSize);
            g2d.fillOval(rightEyeX - eyeSize/2, eyeY - eyeSize/2, eyeSize, eyeSize);
            g2d.setColor(Color.BLACK);
            int pupilSize = eyeSize/2;
            float maxEyeMove = eyeSize/4;
            float leftDx = Math.min(maxEyeMove, Math.max(-maxEyeMove, eyeTarget.x - leftEyeX));
            float leftDy = Math.min(maxEyeMove, Math.max(-maxEyeMove, eyeTarget.y - eyeY));
            g2d.fillOval(leftEyeX - pupilSize/2 + (int)leftDx, eyeY - pupilSize/2 + (int)leftDy, pupilSize, pupilSize);
            float rightDx = Math.min(maxEyeMove, Math.max(-maxEyeMove, eyeTarget.x - rightEyeX));
            float rightDy = Math.min(maxEyeMove, Math.max(-maxEyeMove, eyeTarget.y - eyeY));
            g2d.fillOval(rightEyeX - pupilSize/2 + (int)rightDx, eyeY - pupilSize/2 + (int)rightDy, pupilSize, pupilSize);
            g2d.setColor(Color.BLACK);
            if (smiling) {
                g2d.setStroke(new BasicStroke(2f));
                g2d.drawArc(width/3, eyeY + eyeSize, width/3, height/6, 0, -180);
            } else {
                g2d.drawLine(width*3/8, eyeY + eyeSize*2, width*5/8, eyeY + eyeSize*2);
            }
        }
    }
    class EnergyBar extends JPanel {
        private int value = 0;
        private Color barColor;
        public EnergyBar(Color barColor) {
            this.barColor = barColor;
            setBorder(BorderFactory.createLineBorder(Color.BLACK));
        }
        public void setValue(int value) {
            this.value = Math.max(0, Math.min(100, value));
            repaint();
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            int width = getWidth();
            int height = getHeight();
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillRect(0, 0, width, height);
            g2d.setColor(barColor);
            int fillHeight = (int)(height * (value / 100.0));
            g2d.fillRect(0, height - fillHeight, width, fillHeight);
            g2d.setColor(Color.BLACK);
            String valueText = value + "%";
            FontMetrics fm = g2d.getFontMetrics();// Obtenir les métriques de la police
            int textWidth = fm.stringWidth(valueText);
            int textHeight = fm.getHeight();
            g2d.drawString(valueText, width/2 - textWidth/2, height/2 + textHeight/4);
        }
    }
    class GridPanel extends JPanel {
        private int robotX = 0;
        private int robotY = 0;
        private int destX = 10;
        private int destY = 10;
        private boolean deliveryActive = false;
        public GridPanel() {
            setBackground(Color.WHITE);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (deliveryActive) {
                        moveRobot(e.getX() / CELL_SIZE, e.getY() / CELL_SIZE);
                    }
                }
            });
        }
        public void setupDelivery() {
            robotX = 0;
            robotY = 0;
            destX = 5 + (int)(Math.random() * (GRID_SIZE - 5));
            destY = 5 + (int)(Math.random() * (GRID_SIZE - 5));
            deliveryActive = true;
            repaint();
        }
        private void moveRobot(int newX, int newY) {
            if (newX < 0 || newX >= GRID_SIZE || newY < 0 || newY >= GRID_SIZE) {
                return;
            }
            int dx = newX - robotX;
            int dy = newY - robotY;
            double distance = Math.sqrt(dx*dx + dy*dy);
            try {
                robot.deplacer(newX, newY);
                robotX = newX;
                robotY = newY;
                updateLog();
                if (robotX == destX && robotY == destY) {
                    try {
                        robot.faireLivraison(destX, destY);
                        deliveryActive = false;
                        updateLog();
                        cardLayout.show(cardPanel, CARD_COMPLETE);
                    } catch (RobotException e) {
                        JOptionPane.showMessageDialog(RobotDeliveryGUI.this, 
                                                  "Erreur lors de la livraison: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                    }
                } 
                repaint();
            } catch (RobotException e) {
                JOptionPane.showMessageDialog(RobotDeliveryGUI.this, 
                                          "Erreur lors du déplacement: " + e.getMessage(), 
                                          "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.LIGHT_GRAY);
            for (int i = 0; i <= GRID_SIZE; i++) {
                g2d.drawLine(i * CELL_SIZE, 0, i * CELL_SIZE, GRID_SIZE * CELL_SIZE);
                g2d.drawLine(0, i * CELL_SIZE, GRID_SIZE * CELL_SIZE, i * CELL_SIZE);
            }
            if (deliveryActive) {
                g2d.setColor(Color.RED);
                int destinationSize = CELL_SIZE;
                long currentTime = System.currentTimeMillis();
                int pulseSize = (int)(Math.sin(currentTime / 300.0) * 5) + destinationSize;
                g2d.setColor(new Color(255, 0, 0, 100));
                g2d.fillOval(destX * CELL_SIZE + CELL_SIZE/2 - pulseSize/2,  destY * CELL_SIZE + CELL_SIZE/2 - pulseSize/2,pulseSize, pulseSize);
                g2d.setColor(Color.RED);
                g2d.fillOval(destX * CELL_SIZE + CELL_SIZE/2 - destinationSize/2, destY * CELL_SIZE + CELL_SIZE/2 - destinationSize/2, destinationSize, destinationSize);
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                g2d.drawString("DEST", destX * CELL_SIZE + 3, destY * CELL_SIZE - 5);
                int arrowX = destX * CELL_SIZE + CELL_SIZE/2;
                int arrowY = destY * CELL_SIZE - CELL_SIZE/2;
                drawArrow(g2d, arrowX, arrowY, arrowX, arrowY + CELL_SIZE/2, Color.YELLOW);
                g2d.setColor(new Color(139, 69, 19));
                g2d.fillRect(robotX * CELL_SIZE + 2, robotY * CELL_SIZE + 2,  CELL_SIZE - 4, CELL_SIZE - 4);
                g2d.setColor(Color.WHITE);
                g2d.fillOval(robotX * CELL_SIZE + CELL_SIZE/4, robotY * CELL_SIZE + CELL_SIZE/4, CELL_SIZE/4, CELL_SIZE/4);
                g2d.fillOval(robotX * CELL_SIZE + CELL_SIZE/2, robotY * CELL_SIZE + CELL_SIZE/4, CELL_SIZE/4, CELL_SIZE/4);
                g2d.setColor(Color.BLACK);
                g2d.drawLine(robotX * CELL_SIZE + CELL_SIZE/3, robotY * CELL_SIZE + CELL_SIZE*2/3,   robotX * CELL_SIZE + CELL_SIZE*2/3, robotY * CELL_SIZE + CELL_SIZE*2/3);
                String destCoords = "(" + destX + "," + destY + ")";
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                g2d.drawString(destCoords, 10, 20);
            }
        }
        private void drawArrow(Graphics2D g2d, int x1, int y1, int x2, int y2, Color color) {
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(2f));
            g2d.drawLine(x1, y1, x2, y2);
            double angle = Math.atan2(y2 - y1, x2 - x1);
            int arrowSize = 8;
            int dx1 = (int)(arrowSize * Math.cos(angle - Math.PI/6));
            int dy1 = (int)(arrowSize * Math.sin(angle - Math.PI/6));
            int dx2 = (int)(arrowSize * Math.cos(angle + Math.PI/6));
            int dy2 = (int)(arrowSize * Math.sin(angle + Math.PI/6));
            g2d.drawLine(x2, y2, x2 - dx1, y2 - dy1);
            g2d.drawLine(x2, y2, x2 - dx2, y2 - dy2);
        }
public int getRobotX() {
    return robotX;
}
public int getRobotY() {
    return robotY;
}
public int getDestX() {
    return destX;
}
public int getDestY() {
    return destY;
}
    }
    class LogPanel extends JPanel {
        private JTextArea logArea;
        private List<String> logEntries;
        public LogPanel() {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createTitledBorder("Historique des actions"));
            logEntries = new ArrayList<>();
            logArea = new JTextArea();
            logArea.setEditable(false);
            logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            JScrollPane scrollPane = new JScrollPane(logArea);
            add(scrollPane, BorderLayout.CENTER);
        }
        public void addLog(String log) {
            logEntries.add(log);
            updateLogArea();
        }
        public void clear() {
            logEntries.clear();
            updateLogArea();
        }
        private void updateLogArea() {
            StringBuilder sb = new StringBuilder();
            for (String entry : logEntries) {
                sb.append(entry).append("\n");
            }
            logArea.setText(sb.toString());
            logArea.setCaretPosition(logArea.getDocument().getLength());
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());//le look and feel du systeme qui est plus joli
            } catch (Exception e) {
                e.printStackTrace();// Afficher l'exception dans la console
            }
            RobotDeliveryGUI gui = new RobotDeliveryGUI();
            gui.setVisible(true);
        });
    }
}