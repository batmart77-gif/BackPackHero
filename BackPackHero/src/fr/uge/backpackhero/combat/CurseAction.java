package fr.uge.backpackhero.combat;

import java.util.Objects;

import fr.uge.backpackhero.item.Curse;

public record CurseAction(Curse curse) implements EnemyAction {
  
  public CurseAction{  
    Objects.requireNonNull(curse);
  }
  
  @Override
  public String description() {
    return "Malediction ";
  }
}
