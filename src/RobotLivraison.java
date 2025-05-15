import java.util.Scanner;
public class RobotLivraison extends RobotConnecte {
    private String colisActuel;          // Nom ou description du colis actuellement transporté
    private String destination;          // Lieu où le colis doit être livré
    private boolean enLivraison;         // Indique si le robot est en cours de livraison
    private static final int ENERGIE_LIVRAISON = 15;    // Énergie nécessaire pour effectuer une livraison
    private static final int ENERGIE_CHARGEMENT = 5;    // Énergie nécessaire pour charger un colis
    private int energieRecuperee;                       // Énergie stockée dans le super condensateur
    private static final int CAPACITE_MAX_KERS = 30;    // Capacité maximale du super condensateur (30%)
    public RobotLivraison(String id, int x, int y) {
        // Appel du constructeur parent avec un niveau d'énergie par défaut de 100%
        super(id, x, y, 100);
        // Initialisation des attributs spécifiques
        this.colisActuel = null;
        this.destination = null;
        this.enLivraison = false;
        this.energieRecuperee = 0;       // Initialisation de l'énergie KERS à 0
    }
    // Constructeur avec niveau d'énergie spécifié
    public RobotLivraison(String id, int x, int y, int energie) {
        super(id, x, y, energie);
        this.colisActuel = null;
        this.destination = null;
        this.enLivraison = false;
        this.energieRecuperee = 0;       // Initialisation de l'énergie KERS à 0
    }
    @Override
    public void effectuerTache() throws RobotException {
        // Vérifier si le robot est démarré
        if (!isEnMarche()) {
            throw new RobotException("Le robot doit être démarré pour effectuer une tâche");
        }
        mettreAJourActivite();
        if (enLivraison) {
            Scanner scanner = new Scanner(System.in);
            ajouterHistorique("En livraison vers " + destination);
            System.out.println("Veuillez entrer les coordonnées X de la destination:");
            int destX = scanner.nextInt();
            System.out.println("Veuillez entrer les coordonnées Y de la destination:");
            int destY = scanner.nextInt();
            faireLivraison(destX, destY);
        } else {
            // Robot non en livraison - demander s'il faut charger un nouveau colis
            Scanner scanner = new Scanner(System.in);
            System.out.println("Voulez-vous charger un nouveau colis? (oui/non)");
            String reponse = scanner.nextLine().toLowerCase();
            if (reponse.equals("oui")) {
                // Vérifier l'énergie nécessaire pour le chargement
                try {
                    verifierEnergie(ENERGIE_CHARGEMENT);
                    System.out.println("Entrez la destination du colis:");
                    String nouvelleDestination = scanner.nextLine();
                    chargerColis(nouvelleDestination, "Colis de test");
                    
                } catch (EnergieInsuffisanteException e) {
                    ajouterHistorique("Impossible de charger un colis: " + e.getMessage());
                    throw new RobotException("Énergie insuffisante pour charger un colis", e);
                }
            } else {
                ajouterHistorique("En attente de colis");
            }
        }
        gestionIntelligenteDEnergie();
    }
    public void faireLivraison(int destX, int destY) throws RobotException {
        if (!enLivraison || colisActuel == null) {
            throw new RobotException("Aucun colis à livrer");
        }
        try {
            deplacer(destX, destY);
            String destinationLivrée = destination;
            colisActuel = null;
            destination = null;
            enLivraison = false;
            ajouterHistorique("Livraison terminée à " + destinationLivrée);
            
        } catch (RobotException e) {
            ajouterHistorique("Échec de la livraison: " + e.getMessage());
            throw e;
        }
    }
    @Override
    public void consommerEnergie(int quantite) {
    if (this.isEnModeEconomiseur() && this.isEnMarche()) {
        quantite = (quantite * 90) / 100; // Réduit la consommation d'énergie de 10% en mode économie
    }
    int nouvelleEnergie = Math.max(0, this.getEnergie() - quantite);
    this.setEnergie(nouvelleEnergie);
    ajouterHistorique("Consommation d'énergie : -" + quantite + "%, niveau actuel : " + this.getEnergie()  + "%");
    this.incrementerHeuresUtilisation(quantite / 10);
    // Vérifier si le robot doit passer en mode économie d'énergie
    if (this.getHeuresUtilisation() >= 100) {
        ajouterHistorique("Maintenance requise après " + this.getHeuresUtilisation() + " heures d'utilisation");
    }
    if (this.getEnergie()  < 20 && !this.isEnModeEconomiseur()) {
        activerModeEconomiseur();
        ajouterHistorique("Mode économie d'énergie activé : niveau d'énergie faible (" + this.getEnergie()  + "%)");
    }
    if (this.getEnergie()  == 0 && this.isEnMarche()) {
        if (this instanceof RobotLivraison) {
            RobotLivraison robotLivraison = (RobotLivraison) this;
            if (robotLivraison.getEnergieRecuperee() > 0) {
                try {
                    ajouterHistorique("Tentative d'utilisation du KERS au niveau d'énergie critique (0%)");
                    robotLivraison.utiliserEnergieRecuperee(Math.min(10, robotLivraison.getEnergieRecuperee()));
                    return;
                } catch (RobotException e) {
                    ajouterHistorique("Échec de l'utilisation du KERS: " + e.getMessage());
                }
            }
        }
        arreter();
        ajouterHistorique("Arrêt automatique : niveau d'énergie à 0%");
    }
}
    @Override
    public void deplacer(int nouveauX, int nouveauY) throws RobotException {
        if (!isEnMarche()) {
            throw new RobotException("Le robot doit être démarré pour se déplacer");
        }
        int x1 = getX();
        int y1 = getY();
        int x2 = nouveauX;
        int y2 = nouveauY;
        double distance = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        if (distance > 100) {
            throw new RobotException("Distance trop grande: " + distance + " unités (maximum: 100 unités)");
        }
            int energieRequise = (int) Math.ceil(distance * 0.3);
        try {
            // Vérifier l'énergie et la maintenance
            verifierEnergie(energieRequise);
            verifierMaintenance();
            // Effectuer le déplacement
            setX(nouveauX);
            setY(nouveauY);
            // Consommer l'énergie
            consommerEnergie(energieRequise);
            // Incrémenter les heures d'utilisation (1 heure pour chaque 10 unités de distance)
            int heuresSupplementaires = (int) Math.ceil(distance / 10);
            incrementerHeuresUtilisation(heuresSupplementaires);
            // Journaliser le déplacement
            ajouterHistorique("Déplacement de (" + x1 + "," + y1 + ") à (" +  nouveauX + "," + nouveauY + ") - Distance: " +  String.format("%.2f", distance) + " unités");
            // Récupération d'énergie cinétique via KERS
            recupererEnergieCinetique(distance);    
        } catch (EnergieInsuffisanteException | MaintenanceRequiseException e) {
            // Si l'énergie est insuffisante, essayer d'utiliser l'énergie KERS
            if (e instanceof EnergieInsuffisanteException && energieRecuperee > 0) {
                try {
                    // Calculer l'énergie manquante
                    int energieManquante = energieRequise - getEnergie();
                    // Vérifier si on peut couvrir le manque avec l'énergie KERS
                    if (energieManquante <= energieRecuperee) {
                        // Utiliser l'énergie KERS pour combler le manque
                        utiliserEnergieRecuperee(energieManquante);
                        // Continuer avec le déplacement
                        setX(nouveauX);
                        setY(nouveauY);
                        // Consommer le reste d'énergie normale
                        if (getEnergie() > 0) {
                            consommerEnergie(getEnergie());
                        }
                        // Incrémenter les heures d'utilisation
                        int heuresSupplementaires = (int) Math.ceil(distance / 10);
                        incrementerHeuresUtilisation(heuresSupplementaires);
                        ajouterHistorique("Déplacement d'urgence avec KERS de (" + x1 + "," + y1 + ") à (" + nouveauX + "," + nouveauY + ") - Distance: " +  String.format("%.2f", distance) + " unités");
                        // Récupération d'énergie cinétique via KERS pour ce déplacement aussi
                        recupererEnergieCinetique(distance);
                        // Sortie anticipée car le déplacement a réussi avec l'énergie KERS
                        return;
                    }
                } catch (RobotException ex) {
                    // Si l'utilisation du KERS échoue, continuer avec l'exception originale
                    ajouterHistorique("Échec de l'utilisation du KERS: " + ex.getMessage());
                }
            }
            // Si on arrive ici, c'est que le KERS n'a pas pu sauver la situation
            ajouterHistorique("Échec du déplacement: " + e.getMessage());
            throw new RobotException("Impossible de se déplacer: " + e.getMessage(), e);
        }
    }
    public void chargerColis(String nouvelleDestination,String colis) throws RobotException {
        // Vérifier que le robot n'est pas déjà en livraison
        if (enLivraison) {
            throw new RobotException("Le robot est déjà en livraison");
        }
        // Vérifier que le robot n'a pas déjà un colis
        if (colisActuel != null) {
            throw new RobotException("Le robot transporte déjà un colis: " + colisActuel);
        }
        try {
            // Vérifier l'énergie
            verifierEnergie(ENERGIE_CHARGEMENT);
           this.colisActuel = colis;
            this.destination = nouvelleDestination;
            this.enLivraison = true;
            // Consommer l'énergie de chargement
            consommerEnergie(ENERGIE_CHARGEMENT);
            // Journaliser le chargement
            ajouterHistorique("Chargement du colis '" + colisActuel + "' pour la destination: " + destination);
            // Mettre à jour l'activité
            mettreAJourActivite();
            
        } catch (EnergieInsuffisanteException e) {
            // Si l'énergie est insuffisante, essayer d'utiliser l'énergie KERS
            if (energieRecuperee >= ENERGIE_CHARGEMENT) {
                try {
                    utiliserEnergieRecuperee(ENERGIE_CHARGEMENT);
                    // Demander à l'utilisateur le nom du colis
                    Scanner scanner = new Scanner(System.in);
                    System.out.println("Entrez le nom ou la description du colis:");
                    String nomColis = scanner.nextLine();
                    // Mettre à jour les attributs
                    this.colisActuel = nomColis;
                    this.destination = nouvelleDestination;
                    this.enLivraison = true;
                    // Journaliser le chargement avec KERS
                    ajouterHistorique("Chargement du colis avec énergie KERS '" + colisActuel + "' pour la destination: " + destination);
                    // Mettre à jour l'activité
                    mettreAJourActivite();
                    return;
                } catch (RobotException ex) {
                    // Si l'utilisation du KERS échoue, continuer avec l'exception originale
                }
            }
            ajouterHistorique("Échec du chargement: " + e.getMessage());
            throw new RobotException("Impossible de charger le colis: " + e.getMessage(), e);
        }
    }
    @Override
    protected void verifierEnergie(int energieRequise) throws EnergieInsuffisanteException {
        if (getEnergie() < energieRequise) {
            // Vérifier si on peut utiliser l'énergie KERS
            if (energieRecuperee >= energieRequise - getEnergie()) {
                // On a assez d'énergie KERS pour compenser
                return;
            }
            throw new EnergieInsuffisanteException("Énergie insuffisante: " + getEnergie() + "%, requis: " + energieRequise + "%");
        }
    }
    private void recupererEnergieCinetique(double distance) {
        // Récupérer 20% de l'énergie dépensée pour le déplacement
        int energieRecupereeActuelle = (int)(distance * 0.2); // 0.2 unité récupérée par unité de distance
        // Ajouter l'énergie récupérée au super condensateur (avec limite max)
        energieRecuperee = Math.min(CAPACITE_MAX_KERS, energieRecuperee + energieRecupereeActuelle);
        
        ajouterHistorique("KERS: " + energieRecupereeActuelle + "% d'énergie récupérée (Total: " + energieRecuperee + "%)");
    }
    public void utiliserEnergieRecuperee(int quantite) throws RobotException {
        if (energieRecuperee <= 0) {
            throw new RobotException("Aucune énergie récupérée disponible dans le condensateur KERS");
        }
        if (quantite <= 0) {
            throw new RobotException("La quantité d'énergie à utiliser doit être positive");
        }
        // Limiter la quantité à utiliser par l'énergie disponible
        int energieAUtiliser = Math.min(energieRecuperee, quantite);
        // Réduire l'énergie KERS stockée
        energieRecuperee -= energieAUtiliser;
        /*  Si nécessaire, recharger la batterie principale
        if (getEnergie() < 100) {
            int niveauEnergieFinal = Math.min(100, getEnergie() + energieAUtiliser);
            recharger(niveauEnergieFinal - getEnergie());
        }*/
        ajouterHistorique("Utilisation de " + energieAUtiliser + "% d'énergie KERS (Restant: " + energieRecuperee + "%)");
    }
    public void utiliserTouteEnergieRecuperee() throws RobotException {
        if (energieRecuperee <= 0) {
            throw new RobotException("Aucune énergie récupérée disponible dans le condensateur KERS");
        } 
        utiliserEnergieRecuperee(energieRecuperee);
    }
    public void gestionIntelligenteDEnergie() {
        // Si l'énergie est très faible (moins de 10%) et qu'il y a de l'énergie KERS disponible
        if (getEnergie() < 10 && energieRecuperee > 0) {
            try {
                utiliserEnergieRecuperee(Math.min(energieRecuperee, 10));
                ajouterHistorique("Gestion intelligente: utilisation automatique de l'énergie KERS");
            } catch (RobotException e) {
                ajouterHistorique("Erreur lors de la gestion intelligente d'énergie: " + e.getMessage());
            }
        }
    }
    @Override
    public String toString() {
        // Récupérer les informations de base du robot connecté et de livraison
        String baseInfo = super.toString();
        // Enlever le dernier crochet pour ajouter les informations de livraison et KERS
        baseInfo = baseInfo.substring(0, baseInfo.length() - 1);
        // Ajouter les informations de livraison et KERS
        return baseInfo + 
               ", Colis: " + (colisActuel != null ? "'" + colisActuel + "'" : "Aucun") + 
               ", Destination: " + (destination != null ? destination : "Aucune") + 
               ", En livraison: " + (enLivraison ? "Oui" : "Non") +
               ", Énergie KERS: " + energieRecuperee + "%" + "]";
    }
    // Getters et setters supplémentaires
    public String getColisActuel() {
        return colisActuel;
    }
    public String getDestination() {
        return destination;
    }
    public boolean isEnLivraison() {
        return enLivraison;
    }
    public int getEnergieRecuperee() {
        return energieRecuperee;
    }
}