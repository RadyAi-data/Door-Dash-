package game.engine.cells;

import game.engine.monsters.Monster;
import game.engine.Board;
import game.engine.cards.*;
import java.util.*;
public class CardCell extends Cell {
	
	public CardCell(String name) {
        super(name);
    }
 
	@Override
	public void onLand(Monster landingMonster, Monster opponenMonster) {
		// Draws a card from the deck and performs its action
		ArrayList<Card> deck = Board.getCards();
		int randomIndex = (int)(Math.random() * deck.size());
		Card randomCard = deck.get(randomIndex);
		// first check which instance randomCard is from to cast it
		if (randomCard instanceof SwapperCard)
		{
			((SwapperCard) randomCard).performAction(landingMonster, opponenMonster);
		}
		else if (randomCard instanceof StartOverCard)
		{
			((StartOverCard) randomCard).performAction(landingMonster, opponenMonster);
		}
		else if (randomCard instanceof ShieldCard)
		{
			((ShieldCard) randomCard).performAction(landingMonster, opponenMonster);
		}
		else if (randomCard instanceof EnergyStealCard)
		{
			((EnergyStealCard) randomCard).performAction(landingMonster, opponenMonster);

		}
		else if (randomCard instanceof ConfusionCard)
		{
			((ConfusionCard) randomCard).performAction(landingMonster, opponenMonster);
		}
		 // idk
	}
	
   
}
