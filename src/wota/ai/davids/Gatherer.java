package wota.ai.davids;

import wota.gameobjects.*;

public class Gatherer
    extends AntAI
{
    Sugar sugarTarget = null;

    boolean sugarTargetavailable = true;

    @Override
    public void tick()
        throws Exception
    {
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
                if ( this.self.sugarCarry == 0 && sugarTarget.radius < this.vectorTo( sugarTarget ).length() )
                    this.moveToward( sugarTarget );
                else if ( this.self.sugarCarry == 0 && sugarTarget.radius >= this.vectorTo( sugarTarget ).length() )
                {
                    this.pickUpSugar( sugarTarget );
                    this.moveHome();
                }
                else if ( this.self.sugarCarry > 0 )
                    this.moveHome();
            }
            else
            {
                this.moveHome();
                this.talk( QueenAI.GathererID, sugarTarget );
            }
        }
    }

}
