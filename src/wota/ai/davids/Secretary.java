package wota.ai.davids;

import wota.gameobjects.*;
import wota.utility.Vector;

import java.util.List;
import java.util.LinkedList;

public class Secretary
    extends AntAI
{
    boolean closestSugarDetermined = false;

    boolean QueenWantToKnowEnemy = true;

    Sugar closest = null;

    List<Sugar> sugarList = new LinkedList<Sugar>();

    Hill enemy = null;

    @Override
    public void tick()
        throws Exception
    {
        // evaluate Messages
        for ( Message message : this.audibleMessages )
        {
            if ( message.contentSugar != null )
            {
                if ( message.content == QueenAI.ScoutID )
                {
                    boolean notcontained = true;
                    for ( int i = 0; i < sugarList.size(); i++ )
                    {
                        if ( message.contentSugar.hasSameOriginal( sugarList.get( i ) ) )
                        {
                            notcontained = false;
                            sugarList.set( i, message.contentSugar );
                        }
                    }
                    if ( notcontained )
                    {
                        sugarList.add( message.contentSugar );
                        closestSugarDetermined = false;
                    }
                }
                else if ( message.content == QueenAI.GathererID )
                {
                    forLoop: for ( int i = 0; i < sugarList.size(); i++ )
                    {
                        if ( message.contentSugar.hasSameOriginal( sugarList.get( i ) ) )
                        {
                            sugarList.remove( i );
                            closestSugarDetermined=false;
                            break forLoop;
                        }
                    }
                }
            }
            if ( message.contentHill != null && QueenWantToKnowEnemy )
            {
                System.out.println( "    Secretary knows Enemy" );
                enemy = message.contentHill;
            }
        }
        // END evaluate Messages
        // evaluate closest SugarDetermined
        if ( !closestSugarDetermined && sugarList.size() != 0 )
        {
            closest = sugarList.get( 0 );
            for ( Sugar sugar : sugarList )
            {
                if ( this.vectorTo( sugar ).length() < this.vectorTo( closest ).length() )
                {
                    closest = sugar;
                }
            }
            closestSugarDetermined = true;
        }
        // END evaluate closest
        if ( !QueenWantToKnowEnemy || enemy == null )
            this.talk( QueenAI.SecretaryID, closest );
        else if ( QueenWantToKnowEnemy && enemy != null )
        {
            this.talk( QueenAI.SecretaryID, enemy );
            QueenWantToKnowEnemy = false;
        }
        else
            talk( QueenAI.SecretaryID );
    }
}
