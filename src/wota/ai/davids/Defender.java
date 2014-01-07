package wota.ai.davids;

import wota.gameobjects.Ant;
import wota.gameobjects.AntAI;
import wota.utility.Vector;

public class Defender
    extends AntAI
{
    int DefenderID = 0;

    @Override
    public void tick()
        throws Exception
    {
        // ID-distribution
        int[] defenderids = new int[this.visibleFriends().size()];
        int numberOfIDsFound = 0;
        int counter = 0;
        while ( counter < this.visibleFriends().size() )
        {
            if ( this.visibleFriends().get( counter ).antAIClassName.equals( "Defender" ) )
            {
                defenderids[numberOfIDsFound] = visibleAnts.get( counter ).id;
                numberOfIDsFound++;
            }
            counter++;
        }
        int smallerIDs = 0;
        for ( int i = 0; i < numberOfIDsFound; i++ )
            if ( self.id > defenderids[i] )
                smallerIDs++;
        this.DefenderID = smallerIDs;
        // end ID-distribution

        if ( this.vectorToHome().length() > this.self.caste.SIGHT_RANGE )
            this.moveHome();
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
            if ( strength( weakest ) > this.DefenderID * this.self.caste.ATTACK )
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
            this.moveHome();

    }
    
    double strength( Ant ant )
    {
        double strength = ant.health;
        if ( ant.sugarCarry > 0 )
            strength /= parameters.VULNERABILITY_WHILE_CARRYING;
        return strength/(Vector.subtract( vectorToHome(), vectorTo(ant) ).length()+1);
    }
}
