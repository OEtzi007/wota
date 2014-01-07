package wota.ai.davids;

import wota.gameobjects.Ant;
import wota.gameobjects.AntAI;
import wota.gameobjects.Message;
import wota.gameobjects.Sugar;
import wota.utility.Vector;

public class SugarDefender
    extends AntAI
{
    int SugarDefenderID=0;
    
    Sugar sugarTarget = null;

    boolean sugarTargetavailable = true;

    @Override
    public void tick()
        throws Exception
    {
        // ID-distribution
        int[] sugardefenderids = new int[this.visibleFriends().size()];
        int numberOfIDsFound = 0;
        int counter = 0;
        while ( counter < this.visibleFriends().size() )
        {
            if ( this.visibleFriends().get( counter ).antAIClassName.equals( "SugarDefender" ) )
            {
                sugardefenderids[numberOfIDsFound] = visibleAnts.get( counter ).id;
                numberOfIDsFound++;
            }
            counter++;
        }
        int smallerIDs = 0;
        for ( int i = 0; i < numberOfIDsFound; i++ )
            if ( self.id > sugardefenderids[i] )
                smallerIDs++;
        this.SugarDefenderID = smallerIDs;
        // end ID-distribution
        
        for ( Message message : audibleMessages )
        {
            if ( message.contentSugar != null )
            {
                if ( message.content == QueenAI.SecretaryID )
                {
                    sugarTarget = message.contentSugar;
                    sugarTargetavailable = true;
                }
                else if ( message.content == QueenAI.GathererID )
                {
                    if ( message.contentSugar.hasSameOriginal( sugarTarget ) )
                        sugarTargetavailable = false;
                }
            }
        }
        if ( sugarTarget != null )
        {
            if ( sugarTargetavailable && this.vectorTo( sugarTarget ).length() < this.self.caste.SIGHT_RANGE )
            {
                sugarTargetavailable = false;
                for ( Sugar sugar : this.visibleSugar )
                {
                    if ( sugar.hasSameOriginal( sugarTarget ) )
                    {
                        sugarTargetavailable = true;
                        sugarTarget = sugar;
                    }
                }
            }
            if ( sugarTargetavailable )
            {
                if ( sugarTarget.radius < this.vectorTo( sugarTarget ).length() )
                    this.moveToward( sugarTarget );
                else if ( this.visibleEnemies().size() >= 2 )
                {
                    Ant weakest;
                    Ant secondWeakest;
                    if ( strength( visibleEnemies().get( 0 ) ) < strength( visibleEnemies().get( 1 ) ) )
                    {
                        weakest = visibleEnemies().get( 0 );
                        secondWeakest = visibleEnemies().get( 1 );
                    }
                    else
                    {
                        weakest = visibleEnemies().get( 1 );
                        secondWeakest = visibleEnemies().get( 0 );
                    }
                    for ( int i = 2; i < this.visibleEnemies().size(); i++ )
                    {
                        if ( strength( weakest ) > strength( visibleEnemies().get( i ) ) )
                        {
                            secondWeakest = weakest;
                            weakest = visibleEnemies().get( i );
                        }
                        else if ( strength( secondWeakest ) > strength( visibleEnemies().get( i ) ) )
                            secondWeakest = visibleEnemies().get( i );
                    }
                    if ( strength( weakest ) > this.SugarDefenderID * this.self.caste.ATTACK )
                    {
                        this.moveToward( weakest );
                        this.attack( weakest );
                    }
                    else
                    {
                        this.moveToward( secondWeakest );
                        this.attack( secondWeakest );
                    }
                }
                else if ( this.visibleEnemies().size() == 1 )
                {
                    this.moveToward( visibleEnemies().get( 0 ) );
                    this.attack( visibleEnemies().get( 0 ) );
                }
                else
                    this.moveToward( sugarTarget );
            }
            else
            {
                this.moveHome();
                this.talk( QueenAI.GathererID, sugarTarget );
            }
        }
    }

    double strength( Ant ant )
    {
        double strength = ant.health;
        if ( ant.sugarCarry > 0 )
            strength /= parameters.VULNERABILITY_WHILE_CARRYING;
        return strength / ( Vector.subtract( this.vectorTo( sugarTarget ), vectorTo( ant ) ).length() + 1 );
    }

}
