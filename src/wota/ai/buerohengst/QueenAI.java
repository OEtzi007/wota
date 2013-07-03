/**
 * 
 */
package wota.ai.buerohengst;

import java.util.*;

import wota.gamemaster.AIInformation;
import wota.gameobjects.*;
import wota.utility.SeededRandomizer;

/**
 *  Collects all information and organizes all Ants
 */
@AIInformation(creator = "Pascal", name = "Bürohengst")
public class QueenAI extends wota.gameobjects.QueenAI {

	private Queue<Sugar> knownSugar = new LinkedList<Sugar>();
	//private Map<Sugar, List<Ant> > antDivision;
	private Queue<Ant> awaitsOrders = new LinkedList<Ant>();
	
	@Override
	public void tick() throws Exception {
		createAnt(Caste.Gatherer, StupidWorker.class);
		
		for (Message message : audibleMessages) {
			if (message.sender.id == self.id) {
				continue;
			}
			switch (message.content) {
			case StupidWorker.AWAITING_ORDERS: 
				awaitsOrders.add(message.sender);
				break;
			case StupidWorker.SUGAR_IS_THERE:
				if ( !containsSameOriginal(knownSugar, message.contentSugar)) {
					knownSugar.add(message.contentSugar);
				}
				break;
			case StupidWorker.SUGAR_IS_NOT_THERE:
				if ( containsSameOriginal(knownSugar, message.contentSugar)) {
				knownSugar.remove(message.contentSugar);
				}
				break;
			default:
				System.out.println("unexpected message" + message);
			}
		}
		
		if ( !awaitsOrders.isEmpty() ) {
			Ant worker = awaitsOrders.poll();
			
			if ( !knownSugar.isEmpty() ) {
				Sugar sugar = knownSugar.peek();
				talk(worker.id, sugar);
			}
			else {
				talk(worker.id, null);
			}
		}
		
	}

	/** 
	 * returns true if list contains an element with the same original
	 * as snapshot
	 * @param list
	 * @param snapshot
	 * @return
	 */
	private static boolean containsSameOriginal(Queue<? extends Snapshot> list, Snapshot snapshot) {
		for (Snapshot snap : list) {
			if (snap.hasSameOriginal(snapshot)) {
				return true;
			}
		}
		return false;
	}
}