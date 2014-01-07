/**
 * 
 */
package wota.ai.davids; /* <-- change this to wota.ai.YOUR_AI_NAME
 * make sure your file is in the folder /de/wota/ai/YOUR_AI_NAME
 * and has the same name as the class (change TemplateAI to
 * the name of your choice) 
 */

import wota.gamemaster.AIInformation;
import wota.gameobjects.*;
import wota.utility.SeededRandomizer;

import java.util.List;
import java.util.LinkedList;
import java.math.*;

/**
 * Put a describtion of you AI here.
 */
// Here, you may use spaces, etc., unlike in the package path wota.ai.YOUR_AI_NAME:
@AIInformation( creator = "David S.", name = "DieBuddies" )
public class Scout
    extends AntAI
{

    int scoutid = 0;

    boolean firsttick = true;

    int level = 0;

    boolean curve = false;

    boolean goback = false;

    boolean talking = false;

    boolean talkingEmpty = false;

    int talkingNumber = 0;

    Hill enemy;

    boolean enemyTalked = false;

    List<Sugar> sugarList = new LinkedList<Sugar>();

    List<Sugar> emptySugarList = new LinkedList<Sugar>();

    @Override
    public void tick()
        throws Exception
    {
        deleteLightSugar();
        if ( firsttick )
        {
            int[] scoutids = new int[QueenAI.numberScouts - 1];
            int numberOfIDsFound = 0;
            int counter = 0;
            while ( numberOfIDsFound < QueenAI.numberScouts - 1 )
            {
                if ( visibleAnts.get( counter ).antAIClassName.equals( "Scout" ) )
                {
                    scoutids[numberOfIDsFound] = visibleAnts.get( counter ).id;
                    numberOfIDsFound++;
                }
                counter++;
            }
            int smallerIDs = 0;
            for ( int i = 0; i < QueenAI.numberScouts - 1; i++ )
                if ( self.id > scoutids[i] )
                    smallerIDs++;
            this.scoutid = smallerIDs;
            firsttick = false;
        }
        for ( Sugar sugar : this.visibleSugar )
        {
            boolean notcontained = true;
            for ( int i = 0; i < sugarList.size(); i++ )
            {
                if ( sugar.hasSameOriginal( sugarList.get( i ) ) )
                {
                    notcontained = false;
                    sugarList.set( i, sugar );
                }
            }
            if ( notcontained )
            {
                sugarList.add( sugar );
                goback = true;
                curve = false;
            }
        }
        for ( Hill hill : this.visibleHills )
        {
            if ( hill.playerID != this.self.playerID )
                enemy = hill;
            if ( !enemyTalked && enemy != null && !talking )
            {
                curve = false;
                goback = true;
            }
        }
        if ( curve )
        {
            if ( mod( this.vectorToHome().angle() + 180, 360 ) - 120 * ( ( -level % 2 + scoutid + 1 ) % 3 ) > 0
                && mod( this.vectorToHome().angle() + 180, 360 ) - 120 * ( ( -level % 2 + scoutid + 1 ) % 3 ) < 5 )
            {
                curve = false;
                level = ( level + 1 ) % 3;
                if ( level == 0 )
                {
                    goback = true;
                }
            }
            else
            {
                if ( this.vectorToHome().length() > 10 + ( self.caste.SIGHT_RANGE ) * ( 2 * level + 2 ) )
                    this.moveInDirection( this.vectorToHome().angle() - 80 + 160 * ( level % 2 ) ); // TODO
                else
                    this.moveInDirection( this.vectorToHome().angle() - 90 + 180 * ( level % 2 ) );
            }
        }
        else if ( goback )
        {
            moveInDirection( vectorToHome().angle() );
            if ( vectorToHome().length() < this.self.caste.SIGHT_RANGE )
            {
                curve = false;
                goback = false;
                talking = true;
            }
        }
        else if ( talking )
        {
            if ( !talkingEmpty )
            {
                if ( talkingNumber == sugarList.size() )
                {
                    talkingNumber = 0;
                    talkingEmpty = true;
                    curve = false;
                    goback = false;
                }
                else if ( enemyTalked || enemy == null )
                {
                    talk( QueenAI.ScoutID, sugarList.get( talkingNumber ) );
                    talkingNumber++;
                }
                if ( !enemyTalked && enemy != null )
                {
                    talk( QueenAI.ScoutID, enemy );
                    enemyTalked = true;
                }
            }
            else
            {
                if ( talkingNumber == emptySugarList.size() )
                {
                    emptySugarList=new LinkedList<Sugar>();
                    talkingNumber = 0;
                    talkingEmpty=false;
                    talking = false;
                    curve = false;
                    goback = false;
                }
                else if ( enemyTalked || enemy == null )
                {
                    // imitate a Gatherer
                    talk( QueenAI.GathererID, emptySugarList.get( talkingNumber ) );
                    talkingNumber++;
                }
                if ( !enemyTalked && enemy != null )
                {
                    talk( QueenAI.ScoutID, enemy );
                    enemyTalked = true;
                }
            }
        }
        else
        {
            if ( this.vectorToHome().length() > ( this.self.caste.SIGHT_RANGE ) * ( 2 * level + 2 )
                || this.vectorToHome().length() >= Math.min( parameters.SIZE_X, parameters.SIZE_Y ) / 2 - 2
                    * this.self.speed )
            {// TODO
                curve = true;
                this.moveInDirection( this.vectorToHome().angle() - 90 + 180 * ( level % 2 ) );
            }
            else
            {

                switch ( ( ( level % 2 + scoutid ) % 3 ) )
                {
                    case 0:
                        this.moveInDirection( 0 );
                        break;
                    case 1:
                        this.moveInDirection( 120 );
                        break;
                    case 2:
                        this.moveInDirection( 240 );
                        break;
                }
            }
        }
    }

    void deleteLightSugar()
    {
        int sugarSize = sugarList.size();
        for ( int i = 0; i < sugarSize; i++ )
            if ( this.vectorTo( sugarList.get( i ) ).length() < this.self.caste.SIGHT_RANGE )
            {
                boolean sugarDoesntExistAnymore = true;
                for ( Sugar sugar : this.visibleSugar )
                {
                    if ( sugar.hasSameOriginal( sugarList.get( i ) ) )
                        sugarDoesntExistAnymore = false;
                }
                if ( sugarDoesntExistAnymore )
                {
                    emptySugarList.add( sugarList.get(i) );
                    sugarList.remove( i );
                    i--;
                    sugarSize--;
                }
            }
    }

}
