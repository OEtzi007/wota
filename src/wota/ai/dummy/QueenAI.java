package wota.ai.dummy;

import wota.gamemaster.AIInformation;
import wota.gameobjects.Caste;
import wota.utility.SeededRandomizer;

@AIInformation(creator = "Wota Team", name = "Dummy")
public class QueenAI extends wota.gameobjects.QueenAI {
	
	@Override
	public void tick() {
		createAnt(Caste.Gatherer, MoveAI.class);
	}
}
