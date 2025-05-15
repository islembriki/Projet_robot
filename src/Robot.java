import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
public abstract class Robot {
    private String id;               // Identifiant unique du robot
    private int x;                   // Position X sur une grille 2D
    private int y;                   // Position Y sur une grille 2D
    private int energie;             // Niveau d'énergie (entre 0 et 100)
    private int heuresUtilisation;   // Nombre d'heures d'utilisation avant maintenance
    private boolean enMarche;        // État du robot (allumé ou éteint)
    private List<String> historiqueActions; // Historique des actions effectuées
    private boolean economiserEnergie; // Indicateur d'économie d'énergie
    private boolean modeVeille;      // État du mode veille
    private LocalDateTime derniereMiseAJour; // Dernière mise à jour d'activité
    private LocalDateTime debutVeille; // Moment où le mode veille a commencé
    private int secondesAvantVeille; // Secondes d'inactivité avant d'entrer en mode veille
    private Timer timerVeille;       // Timer pour gérer le mode veille
    private boolean verificationVeilleActive; // Indique si la vérification du mode veille est active
    public void activerModeEconomiseur() {
        if (enMarche && !economiserEnergie) {
            economiserEnergie = true;
            ajouterHistorique("Mode economie d'energie activé - attention les fonctionnalités sont limitées");
        }
    }
    public void desactiverModeEconomiseur() {
        if (enMarche && economiserEnergie) {
            economiserEnergie = false;
            ajouterHistorique("Mode economie d'energie désactivé- fonctionnalités normales");
        }
    }
    public Robot(String id, int x, int y, int energie) {
        this(id, x, y, energie, 30); // Par défaut, mode veille après 30 secondes d'inactivité
    }
    public Robot(String id, int x, int y, int energie, int secondesAvantVeille) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.energie = Math.max(0, Math.min(100, energie)); // Limite l'énergie entre 0 et 100
        this.heuresUtilisation = 0;
        this.enMarche = false;
        this.historiqueActions = new ArrayList<>();
        this.economiserEnergie = false;
        this.modeVeille = false;
        this.derniereMiseAJour = LocalDateTime.now();
        this.secondesAvantVeille = secondesAvantVeille;
        this.verificationVeilleActive = false;
        this.timerVeille = new Timer(true); // Timer en tant que daemon
        ajouterHistorique("Robot créé");
    }
    protected void mettreAJourActivite() {
        this.derniereMiseAJour = LocalDateTime.now();
        if (modeVeille) {
            desactiverModeVeille();
        }
    }
    public void activerModeVeille() {
        if (enMarche && !modeVeille) {
            modeVeille = true;
            debutVeille = LocalDateTime.now();
            ajouterHistorique("Mode veille activé manuellement");
            demarrerConsommationVeille();
        }
    }
    public void desactiverModeVeille() {
        if (enMarche && modeVeille) {
            modeVeille = false;
            // Calculer la consommation d'énergie pendant la veille
            Duration dureeVeille = Duration.between(debutVeille, LocalDateTime.now());
            long minutesEnVeille = dureeVeille.toMinutes();
            if (minutesEnVeille > 0) {
                consommerEnergie((int) minutesEnVeille);
            }
            ajouterHistorique("Mode veille désactivé - durée: " + dureeVeille.toMinutes() + " minutes");
        }
    }
    private void demarrerVerificationVeille() {
        if (!verificationVeilleActive && enMarche) {
            verificationVeilleActive = true;
            timerVeille.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    verifierModeVeille();
                }
            }, 1000, 1000); // Vérifier toutes les secondes
            ajouterHistorique("Système de vérification du mode veille activé");
        }
    }
    private void arreterVerificationVeille() {
        if (verificationVeilleActive) {
            timerVeille.cancel();
            timerVeille = new Timer(true);
            verificationVeilleActive = false;
            ajouterHistorique("Système de vérification du mode veille désactivé");
        }
    }
    private void verifierModeVeille() {
        if (enMarche && !modeVeille) {
            Duration dureeInactivite = Duration.between(derniereMiseAJour, LocalDateTime.now());
            if (dureeInactivite.getSeconds() >= secondesAvantVeille) {
                modeVeille = true;
                debutVeille = LocalDateTime.now();
                ajouterHistorique("Mode veille activé automatiquement après " + 
                                 dureeInactivite.getSeconds() + " secondes d'inactivité");
                demarrerConsommationVeille();
            }
        }
    }
    private void demarrerConsommationVeille() {
        timerVeille.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (enMarche && modeVeille) {
                    consommerEnergie(1); // Consomme 1% d'énergie par minute
                    ajouterHistorique("Consommation en mode veille: -1% d'énergie");
                } else {
                    cancel(); // Arrêter cette tâche si le robot n'est plus en veille
                }
            }
        }, 60000, 60000); // Exécuter toutes les minutes (60000 ms)
    }
    protected void ajouterHistorique(String action) {
        LocalDateTime maintenant = LocalDateTime.now();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm:ss");
        String dateFormatee = maintenant.format(format);
        historiqueActions.add(dateFormatee + " " + action);
    }
    protected void verifierEnergie(int energieRequise) throws EnergieInsuffisanteException {
        if (energie < energieRequise) {
            throw new EnergieInsuffisanteException("Énergie insuffisante : " + energie + "% disponible, " + energieRequise + "% requis");
        }
    }
    protected void verifierMaintenance() throws MaintenanceRequiseException {
        if (heuresUtilisation >= 1000) {
            throw new MaintenanceRequiseException("Maintenance requise après " + heuresUtilisation + " heures d'utilisation");
        }
    }
    public void demarrer() throws RobotException {
        try {
            verifierEnergie(10); // Nécessite au moins 10% d'énergie
            verifierMaintenance();
            enMarche = true;
            mettreAJourActivite();
            demarrerVerificationVeille();
            ajouterHistorique("Démarrage du robot");
        } catch (EnergieInsuffisanteException | MaintenanceRequiseException e) {
            ajouterHistorique("Échec du démarrage : " + e.getMessage());
            throw new RobotException("Impossible de démarrer le robot : " + e.getMessage(), e);
        }
    }
    public void arreter() {
        if (modeVeille) {
            desactiverModeVeille();
        }
        enMarche = false;
        arreterVerificationVeille();
        ajouterHistorique("Arrêt du robot");
    }
    protected void consommerEnergie(int quantite) {
        if (economiserEnergie && enMarche) {
            quantite = (quantite * 90) / 100; // Réduit la consommation d'énergie de 10% en mode économie
        }
        int nouvelleEnergie = Math.max(0, energie - quantite);
        energie = nouvelleEnergie;
        ajouterHistorique("Consommation d'énergie : -" + quantite + "%, niveau actuel : " + energie + "%");
        heuresUtilisation += quantite / 10; // Consomme 1 heure d'utilisation pour chaque 10% d'énergie consommée
        if (heuresUtilisation >= 1000) {
            ajouterHistorique("Maintenance requise après " + heuresUtilisation + " heures d'utilisation");
        }
        if (energie < 20 && !economiserEnergie) {
            activerModeEconomiseur();
            ajouterHistorique("Mode économie d'énergie activé : niveau d'énergie faible (" + energie + "%)");
        }
        if (energie == 0 && enMarche) {
            arreter();
            ajouterHistorique("Arrêt automatique : niveau d'énergie à 0%");
        }
    }
    public void recharger(int quantite) {
        int ancienneEnergie = energie;
        energie = Math.min(100, energie + quantite);
        int energieAjoutee = energie - ancienneEnergie;
        ajouterHistorique("Recharge d'énergie : +" + energieAjoutee + "%, niveau actuel : " + energie + "%");
        mettreAJourActivite();
    }
    public abstract void deplacer(int nouveauX, int nouveauY) throws RobotException;
    public abstract void effectuerTache() throws RobotException;
    public String getHistorique() {
        StringBuilder sb = new StringBuilder("Historique des actions du robot " + this.id + ":\n");
        for (String action : historiqueActions) {
            sb.append(" - ").append(action).append("\n");
        }
        return sb.toString();
    }
    @Override
    public String toString() {
        String etat = enMarche ? (modeVeille ? "En veille" : "Actif") : "Éteint";
        String nomClasse = this.getClass().getSimpleName();
        return nomClasse + " [ID : " + id + ", Position : (" + x + "," + y + "), " +
               "Énergie : " + energie + "%, Heures : " + heuresUtilisation + ", État : " + etat + "]";
    }
    //------------------------------
    // Getters et Setters
    public String getId() {
        return id;
    }

    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    protected void setX(int x) {
        this.x = x;
        mettreAJourActivite(); // Mise à jour de l'activité lors d'un changement de position
    }
    protected void setY(int y) {
        this.y = y;
        mettreAJourActivite(); // Mise à jour de l'activité lors d'un changement de position
    }
    public int getEnergie() {
        return energie;
    }
    public void setEnergie(int energie) {
        this.energie = Math.max(0, Math.min(100, energie)); // Limite l'énergie entre 0 et 100
        ajouterHistorique("Énergie modifiée à " + energie + "%");
    }
    public int getHeuresUtilisation() {
        return heuresUtilisation;
    }
    public void setHeuresUtilisation(int heures) {
        this.heuresUtilisation = heures;
        ajouterHistorique("Heures d'utilisation modifiées à " + heures);
    }
    protected void incrementerHeuresUtilisation(int heures) {
        this.heuresUtilisation += heures;
    }
    public boolean isEnMarche() {
        return enMarche;
    }
    
    public boolean isEnModeVeille() {
        return modeVeille;
    }

    public void setSecondesAvantVeille(int secondes) {
        this.secondesAvantVeille = secondes;
        ajouterHistorique("Délai d'inactivité avant veille modifié à " + secondes + " secondes");
    }
    public boolean isEnModeEconomiseur() {
        return economiserEnergie;
    }

}