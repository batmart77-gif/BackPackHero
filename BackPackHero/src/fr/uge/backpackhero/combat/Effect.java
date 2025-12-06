package fr.uge.backpackhero.combat;

public enum Effect {
  HASTE("Hâte"),          // +X Blocage
  RAGE("Rage"),           // +X Dégâts
  SLOW("Lenteur"),        // -X Blocage
  WEAK("Faiblesse"),      // -X Dégâts
  POISON("Poison"),       // X dégâts fin de tour (ignore armure)
  BURN("Brûlure"),        // X dégâts début de tour
  DODGE("Esquive"),       // Ignore X attaques
  REGEN("Régénération");  // +X PV début de tour

  private final String nom;

  Effect(String nom) {
    this.nom = nom;
  }

  public String getNom() {
    return nom;
  }
}