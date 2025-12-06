package phase_1;

import java.util.List;

public sealed interface Item permits RangeWeapon, Arrow, MeleeWeapon, Armor, Shield, ManaStone, MagicItem, Gold{
	public abstract int price();
	public abstract String toString();
	public abstract List<Position> pos();
	public abstract String name();
	public abstract String details();
}
