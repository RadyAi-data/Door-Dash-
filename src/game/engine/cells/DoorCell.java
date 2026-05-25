package game.engine.cells;

import game.engine.Role;
import game.engine.interfaces.CanisterModifier;
import game.engine.monsters.Monster;

public class DoorCell extends Cell implements CanisterModifier {
	private Role role;
	private int energy;
	private boolean activated;
	
	public DoorCell(String name, Role role, int energy) {
		super(name);
		this.role = role;
		this.energy = energy;
		this.activated = false;
	}
	
	public Role getRole() {
		return role;
	}
	
	public int getEnergy() {
		return energy;
	}
	
	public boolean isActivated() {
		return activated;
	}

	public void setActivated(boolean isActivated) {
		this.activated = isActivated;
	}

	@Override
	public void modifyCanisterEnergy(Monster monster, int canisterValue) {
		if (canisterValue >=0) // just add the energy to the existing energy of the monster
		{
			monster.setEnergy(monster.getEnergy() + canisterValue);
		}
		else // must first check if the total energy may be < 0, as the energy must be >=0
		{
			if( monster.getEnergy() - Math.abs(canisterValue) <= 0)
			{
				monster.setEnergy(0);
			}
			else
			{
				monster.setEnergy(monster.getEnergy() - Math.abs(canisterValue));
			}
			
		}
	}

	@Override
	public void onLand(Monster landingMonster, Monster opponenMonster) {
		
		
	}

}
