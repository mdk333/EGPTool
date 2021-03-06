/**
 *
 * @author Maduka Attamah
 *
 * Copyright 2011-2015 Maduka Attamah
 *
 */

package egp.concurrent.modeller;

import java.util.HashMap;

public class SituationBHive extends Situation{

    private HashMap<Integer, Colony> colony;
    
    public SituationBHive(String aHistory, int[] aConfiguration, Situation parentSit){
        super(aHistory, aConfiguration, parentSit);
        colony = new HashMap<>();
    }
       
    /* gives us the root situation, history should be empty here */
    public SituationBHive(int numAgents){
        super(numAgents);
        colony = new HashMap<>();
    }
    
    public SituationBHive(String aHistory, int numAgents, Situation parentSit){
        super(aHistory, numAgents, parentSit);
        colony = new HashMap<>();
    }
 
    public synchronized void setColony(Integer agent, Colony aColony){
        this.colony.put(agent, aColony);
    }
    
    public synchronized Colony getColony(Integer agent){
        return this.colony.get(agent);
    }
    
}
