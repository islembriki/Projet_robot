public abstract class RobotConnecte extends Robot implements Connectable {
    private boolean connecte;        
    private String reseauConnecte;
    private boolean modeEcoNumerique= false; // Indique si le mode Éco-Numérique est activé
    private int freqCommunication; // Fréquence des communications en secondes
    private int dataSaved=0; // Données économisées en Mo
     /** Active le mode Éco-Numérique qui réduit l'empreinte carbone des communications Ce mode réduit la fréquence des communications et compresse les données*/
    public void activerModeEcoNumerique() throws RobotException {
        if (!connecte) {
            throw new RobotException("Impossible d'activer le mode Éco-Numérique: robot non connecté");
        }
        modeEcoNumerique = true;
        freqCommunication = 60; // Communication toutes les 60 secondes au lieu de 5 secondes
        ajouterHistorique("Mode Éco-Numérique activé - Réduction de l'empreinte numérique");
    }
     public void setFrequenceCommunication(int secondes) throws RobotException {// Définit la fréquence de communication en secondes 
        if (!modeEcoNumerique) {
            throw new RobotException("Le mode Éco-Numérique doit être activé");
        }
        // Minimum 30 secondes pour rester écologique
        if (secondes < 30) {
            secondes = 30;
            ajouterHistorique("Fréquence minimale forcée à 30 secondes pour raisons écologiques");
        }
        freqCommunication = secondes;
        ajouterHistorique("Fréquence de communication mise à jour: " + secondes + " secondes");
    }
    public RobotConnecte(String id, int x, int y, int energie) {    /// Constructeur de la classe RobotConnecte
        super(id, x, y, energie);
        this.connecte = false;
        this.reseauConnecte = null;
    }
    @Override
    public void connecter(String reseau) throws RobotException {// Méthode pour connecter le robot à un réseau
        // Vérifier si le robot est déjà connecté
        try {
            // Vérifier si le robot dispose de suffisamment d'énergie (5%)
            verifierEnergie(5);
            this.reseauConnecte = reseau;
            this.connecte = true;
            // Consommer 5% d'énergie car la connexion au réseau est établie
            consommerEnergie(5);
            ajouterHistorique("Connexion au réseau : " + reseau);
            // Mettre à jour l'activité pour éviter le mode veille
            mettreAJourActivite();
        } catch (EnergieInsuffisanteException e) {
            ajouterHistorique("Échec de connexion au réseau : " + e.getMessage());
            throw new RobotException("Impossible de se connecter au réseau : " + e.getMessage(), e);
        }
    }
    @Override
    public void deconnecter() {
        if (connecte) {
            // Enregistrer l'action dans l'historique
            ajouterHistorique("Déconnexion du réseau : " + reseauConnecte);
            // Mettre à jour les attributs
            this.connecte = false;
            this.reseauConnecte = null;
            // Mettre à jour l'activité pour éviter le mode veille
            mettreAJourActivite();//comme on a dit dans la classe robot cette methode est fondamentale pour eviter le mode veille
        }
    }
    @Override
    public void envoyerDonnees(String donnees, int tailleDonnees) throws RobotException {
    // Vérifier si le robot est connecté à un réseau
    if (!connecte) {
        throw new RobotException("Le robot n'est pas connecté à un réseau");
    }
    // Variables pour l'optimisation écologique 
    int energieRequise;
    int tailleFinale = tailleDonnees;
    String messageCompression = ""; 
    // Appliquer la compression si le mode éco-numérique est activé
    if (modeEcoNumerique) {
        // Compression des données (réduction de 60%)
        tailleFinale = (int)(tailleDonnees * 0.4);
        int dataEconomise = tailleDonnees - tailleFinale;
        dataSaved += dataEconomise;
        // Moins d'énergie requise pour les données compressées
        energieRequise = tailleFinale / 5;
        messageCompression = "Éco-Numérique: Compression des données " + tailleDonnees + " Mo → " + tailleFinale + " Mo (Économie: " + dataEconomise + " Mo)";
    } else {
        // Sans compression, consommation normale 
        // (utiliser 3% d'énergie comme dans votre méthode originale si tailleDonnees est faible)
        energieRequise = Math.max(3, tailleDonnees / 3);
    }
    try {
        // Vérifier si le robot dispose de suffisamment d'énergie
        verifierEnergie(energieRequise);
        // Consommer l'énergie nécessaire
        consommerEnergie(energieRequise);
        // Enregistrer l'action de compression dans l'historique si applicable
        if (!messageCompression.isEmpty()) {
            ajouterHistorique(messageCompression);
        }
        // Enregistrer l'action d'envoi dans l'historique
        ajouterHistorique("Envoi de " + tailleFinale + " Mo de données sur le réseau " + reseauConnecte + " : " + donnees + " (Énergie: -" + energieRequise + "%)");
        // Mettre à jour l'activité pour éviter le mode veille (comme dans votre méthode originale)
        mettreAJourActivite();
    } catch (EnergieInsuffisanteException e) {
        ajouterHistorique("Échec d'envoi de données : " + e.getMessage());
        throw new RobotException("Impossible d'envoyer les données : " + e.getMessage(), e);
    }
}
@Override
    public String toString() {
        String baseInfo = super.toString();
        return baseInfo.substring(0, baseInfo.length() - 1) + ", Connecté: " + 
               (connecte ? "Oui (" + reseauConnecte + ")" : "Non") + "]";
    }
//getter et setter et verfications
    public boolean isConnecte() {
        return connecte;
    }
    public void setReseau(String reseau) {
        this.reseauConnecte = reseau;
    }
   
    public String getReseauConnecte() {
        return reseauConnecte;
    }
    
    public boolean isEnModeEcoNumerique() {
        return modeEcoNumerique;
    }
 
}