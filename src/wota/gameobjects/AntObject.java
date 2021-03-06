package wota.gameobjects;

import java.util.List;

import wota.gameobjects.Parameters;
import wota.utility.SeededRandomizer;
import wota.utility.Vector;


/**
 * Interne Darstellung von Ants. Enthält alle Informationen.
 * Im Gegensatz dazu enthält Ant nur die Informationen, welche die KI sehen darf.
 * @author pascal
 */
public class AntObject extends GameObject{
	
	private static int idCounter = 0;
	private Ant ant;
	protected final AntAI ai;
	public final int id;
	protected double health;
	private double speed;
	private double lastMovementDirection = 0;
	
	/** amount of sugar carried now */
	private int sugarCarry = 0;
	
	/** SugarObject AntObject is waiting for */ 
	private SugarObject sugarTarget = null;
	
	/** Angriffspunkte */
	private Action action;
	private final Caste caste;
	public final GameWorld.Player player;
	private boolean isAttacking = false;
	private AntObject attackTarget = null;
	// Only has a meaning while the ant is waiting at a sugar source. Has to be saved because the sugar
	// source has to increase its amount of sugar by this much when this ant dies while waiting.
	private int amountPickedUpLastTime; 
	
	public AntObject(Vector position, Caste caste, Class<? extends AntAI> antAIClass, GameWorld.Player player, Parameters parameters) {
		super(position, parameters);
		
		this.player = player;
		this.id = getNewID();
		AntAI antAI = null;
		try {
			antAI = antAIClass.newInstance(); 
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Could not create AntAI -> exit");
			System.exit(1);
		}
		antAI.setParameters(parameters);
		this.caste = caste;
		// set parameters
		health = caste.INITIAL_HEALTH;
		speed = caste.SPEED;
		
		this.ai = antAI;
		this.ai.setAntObject(this);
	}

	public AntAI getAI() {
		return ai;
	}
	
	public Ant getAnt() {
		return ant;
	}
	
	public Caste getCaste() {
		return caste;
	}

	public double getHealth() {
		return health;
	}
	
	public AntObject getAttackTarget() {
		if(isAttacking) {
			return attackTarget;
		}
		else
			return null;
	}
	
	void setAttackTarget(AntObject target) {
		if (target == null) {
			isAttacking = false;
			attackTarget = null;
		}
		else {
			isAttacking = true;
			attackTarget = target;
		}
	}

	public double getSpeed() {
		return speed;
	}
	
	public Action getAction() {
		return action;
	}
	
	public int getSugarCarry() {
		return sugarCarry;
	}
	
	public double getLastMovementDirection() {
		return lastMovementDirection;
	}
	
	public void createAnt() {
		ant = new Ant(this);
		this.ai.setAnt(ant);
	}
	
	public void takesDamage(double attack) {
		double takenDamage;
		if (isCarrying()) {
			takenDamage = parameters.VULNERABILITY_WHILE_CARRYING * attack;
		}
		else {
			takenDamage = attack;
		}
			
		health = health - takenDamage;
	}
	
	public void pickUpSugar(SugarObject sugarObject) {
		int oldAmountOfSugarCarried = sugarCarry;
		sugarCarry = Math.min(caste.MAX_SUGAR_CARRY, sugarCarry + sugarObject.getAmount());
		amountPickedUpLastTime = sugarCarry - oldAmountOfSugarCarried;
		// amountPickedUpLastTime is really about this time and may be zero, because other 
		// ants may already have picked up all the remaining sugar *during this tick*. 
		// In this case, do not wait at the sugar source. 
		if (amountPickedUpLastTime > 0) {
			sugarObject.requestSugarPickup(this, amountPickedUpLastTime);		
			sugarTarget = sugarObject;
		} 
	}
	
	/** sets amount of carried sugar to 0 */
	public void dropSugar() {
		sugarCarry = 0;
	}
	
	/** Checks if AntObject has positive health and has been at its hill less than 
	 *  than TICKS_TO_LIVE ticks ago. */
	public boolean isDead() {
		return health <= 0;
	}
	
	/** calls ai.tick(), handles exceptions and saves the action */
	public void tick(List<Ant> visibleAnts, List<Sugar> visibleSugar, 
			List<Hill> visibleHills, List<Message> incomingMessages) {
		ai.visibleAnts = visibleAnts;
		ai.visibleSugar = visibleSugar;
		ai.visibleHills = visibleHills;
		ai.audibleMessages = incomingMessages;
		
		if ( !isWaitingForSugar() ) {
			try {
				ai.tick();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		action = ai.popAction();
	}
	
	/**
	 * @return true if sugarTarget is not null
	 */
	private boolean isWaitingForSugar() {
		return (sugarTarget != null) ;
	}

	public boolean isCarrying() {
		return sugarCarry > 0;
	}

	@Override
	public void move(Vector moveVector) {
		Vector realMovement = moveVector;
		if (moveVector.length() != 0) {
			double angleError = parameters.ANGLE_ERROR_PER_DISTANCE * moveVector.length() 
					* 2 * (SeededRandomizer.getDouble() - 0.5); 
			realMovement = Vector.fromPolar(moveVector.length(), moveVector.angle() + angleError);
		}
		lastMovementDirection = realMovement.angle();
		super.move(realMovement);
	}
	
	/** get new id for antObject */
	private static int getNewID() {
		idCounter++;
		return idCounter - 1;
	}

	/**
	 * gets called when AntObject is dying
	 */
	public void die() {
		if (sugarTarget != null) {
			sugarTarget.removeFromQueueEarly(this, amountPickedUpLastTime);
			sugarCarry -= amountPickedUpLastTime; // doesn't matter now, would if one were able to resurrect ants
		}
	}

	/**
	 * Call whenever AntObject should not wait for sugarTarget anymore.
	 */
	public void unsetSugarTarget() {
		sugarTarget = null;
	}

	public int getAmountPickedUpLastTime() {
		return amountPickedUpLastTime;
	}

}
