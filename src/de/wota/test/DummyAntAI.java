package de.wota.test;

import de.wota.Action;
import de.wota.ai.AntAI;

public class DummyAntAI extends AntAI {

	public DummyAntAI() {
	}

	@Override
	public void tick() {
	}

	@Override
	public Action getAction() {
		return null;
	}

}
