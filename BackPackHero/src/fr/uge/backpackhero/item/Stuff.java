package fr.uge.backpackhero.item;

public enum Stuff {
    WoodSword(Rarity.COMMON),
    MouseBow(Rarity.UNCOMMON),
    ShortArrow(Rarity.COMMON),
    LeatherCap(Rarity.RARE),
    RoughBuckler(Rarity.COMMON),
    ElectricWand(Rarity.UNCOMMON),
    CloudSword(Rarity.LEGENDARY),
    Curse(Rarity.CURSE),
    ManaStone(Rarity.COMMON);

    private final Rarity rarity;

    Stuff(Rarity rarity) {
        this.rarity = rarity;
    }

    public Rarity rarity() {
        return rarity;
    }
}
