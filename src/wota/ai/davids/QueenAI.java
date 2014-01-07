/**
 * 
 */
package wota.ai.davids;

import wota.gamemaster.AIInformation;
import wota.gameobjects.*;
import wota.utility.SeededRandomizer;
import wota.utility.Vector;

/**
 *
 */
@AIInformation( creator = "David S.", name = "DieBuddies" )
public class QueenAI
    extends wota.gameobjects.QueenAI
{
    // Konstanten:
    public static final int numberScouts = 3;

    public static final int SecretaryID = -1;

    public static final int GathererID = -2;

    public static final int QueenAIID = -3;

    public static final int ScoutID = -4;

    // Attribute:
    boolean scoutsSent = false;

    int ticknumber = 0;

    Hill enemy = null;

    double availableFood = 0;

    int numberOfDefenders = 0;

    // SafeCreator
    int numberCreatedLastTick = 0;

    int creatorCounter = 0;

    double food = 0;

    /*
     * your Queen is not able to move but can communicate and create new ants. You can create new ants with
     * createAnt(caste, antAIClass) e.g. if you want a gatherer and the AI you want use is called SuperGathererAI
     * createAnt(Caste.Gatherer, SuperGathererAI.class)
     */
    @Override
    public void tick()
        throws Exception
    {
        numberOfDefenders = 0;
        for ( Ant ant : this.visibleFriends() )
        {
            if ( ant.antAIClassName.equals( "Defender" ) )
                numberOfDefenders++;
        }
        initSafeCreator();
        ticknumber++;
        availableFood = this.visibleHills.get( 0 ).food;
        if ( enemy == null )
            for ( Message message : audibleMessages )
            {
                if ( message.contentHill != null )
                {
                    enemy = message.contentHill;
                }
            }
        /*
         * try to create an Ant using the TemplateAI in every tick if you don't have enough food to create the ant your
         * call will be ignored
         */
        boolean secretaryActive = false;
        for ( Message message : this.audibleMessages )
            if ( message.content == SecretaryID )
                secretaryActive = true;
        if ( !secretaryActive && ticknumber != 2 )
            createSafeAnt( Caste.Scout, Secretary.class );
        if ( ticknumber == 1 )
        {
            for ( int i = 0; i < 3; i++ )
                createSafeAnt( Caste.Scout, Scout.class );
        }

        double random = SeededRandomizer.getDouble();
        if ( this.visibleEnemies().size() > numberOfDefenders )
        {
            createSafeAnt( Caste.Soldier, Defender.class );
        }
        // else if ( enemy != null && numberCreatedLastTick >= 1 )
        else if ( random < 0.3 )
        {
            createSafeAnt( Caste.Soldier, Warrior.class );
            talk( QueenAIID, enemy );
        }else if(random <0.6)
            createSafeAnt(Caste.Soldier,SugarDefender.class);
        else if ( numberCreatedLastTick == 0 )
            createSafeAnt( Caste.Gatherer, Gatherer.class );
        else
            createSafeAnt( Caste.Gatherer, Gatherer.class );
    }

    void initSafeCreator()
    {
        numberCreatedLastTick = creatorCounter;
        creatorCounter = 0;
        food = this.visibleHills.get( 0 ).food;
    }

    void createSafeAnt( Caste caste, Class<? extends AntAI> haha )
    {
        if ( food >= this.parameters.ANT_COST )
        {
            creatorCounter++;
            food -= this.parameters.ANT_COST;
            this.createAnt( caste, haha );
        }
    }

}
