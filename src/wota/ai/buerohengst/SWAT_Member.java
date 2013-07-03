package wota.ai.buerohengst;

import java.util.LinkedList;
import java.util.List;

import wota.gameobjects.*;
import wota.utility.SeededRandomizer;
import wota.utility.Vector;

/** acts in small groups which are seperated by a small distance */
public class SWAT_Member extends AntAI {

	int tick = 0;
	Ant squadLeader = null;
	Ant target = null;
	
	@Override
	public void tick() throws Exception {
		tick++;
		if (squadLeader == null) {
			lookForLeader();
			return;
		}
		squadLeader = getRecentAnt(visibleAnts, squadLeader);
		
		listenForAttackTarget();
		target = getRecentAnt(visibleEnemies(), target);
		
		if (target == null || target.health <= 0) {
			System.out.println("follow leader");
			followLeader();
		}
		else {
			System.out.println("attack target");
			tacticallyAttackTarget();
		}
		
	}

	private void listenForAttackTarget() {
		for (Message message : audibleMessages) {
			if (message.sender.hasSameOriginal(squadLeader)) {
				target = message.contentAnt;
			}
		}
	}

	private void tacticallyAttackTarget() {
		if (vectorTo(target).length() > parameters.ATTACK_RANGE) {
			moveToward(target);
		}
		else {
			attack(target);
			if (visibleFriends().size() > 0) {
				List<Ant> friends = antsWithinRadius(visibleFriends(), parameters.ATTACK_RANGE);
				if ( !friends.isEmpty() ) {
					List<Vector> positions = getPositions(friends);
					Vector cmsFriends = centorOfMass(positions);
					Vector smallRandomVector = new Vector(SeededRandomizer.getDouble()*2-1,
														  SeededRandomizer.getDouble()*2-1);
					cmsFriends = Vector.add(cmsFriends, smallRandomVector);
					if ( !cmsFriends.isSameVectorAs(self.getPosition())) {
						Vector movementTarget = parameters.shortestDifferenceOnTorus(
															self.getPosition(), cmsFriends);
						moveInDirection(movementTarget.angle());
					}
				}
			}
		}
	}

	private void followLeader() {
		moveToward(squadLeader);
	}

	private void lookForLeader() {
		for (Ant ant : visibleAnts) {
			if (ant.antAIClassName.contains("SWAT_Leader")) {
				squadLeader = ant;
				talk(QueenAI.FOUND_LEADER, squadLeader);
				break;
			}
		}
	}
	
	public static Vector centorOfMass(List<Vector> vectors) {
		if (vectors.size() == 0) {
			return null;
		}
		Vector output = new Vector(0,0);
		for (Vector vector : vectors) {
			output = Vector.add(output, vector);
		}
		output.scale(1./vectors.size());
		return output;
	}
	
	public static List<Vector> getPositions(List<Ant> ants) {
		LinkedList<Vector> list = new LinkedList<Vector>();
		for (Ant ant : ants) {
			list.add(ant.getPosition());
		}
		return list;
	}
	
	public List<Ant> antsWithinRadius(List<Ant> ants, double radius) {
		LinkedList<Ant> list = new LinkedList<Ant>();
		for (Ant ant : ants) {
			if (vectorTo(ant).length() <= radius) {
				list.add(ant);
			}
		}
		return list;
	}
	
	public static Ant getRecentAnt(List<Ant> candidates, Ant oldAnt) {
		for (Ant ant : candidates) {
			if (ant.hasSameOriginal(oldAnt)) {
				return ant;
			}
		}
		return null;
	}
	
}
