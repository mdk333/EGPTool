/**
 *
 * @author Maduka Attamah
 *
 * Copyright 2011-2015 Maduka Attamah
 *
 */

package egp.concurrent.modeller;

import egp.FrameworkEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.Callable;

public class TaskComputeNodeSuccessorsbkupb4genframework implements Callable {

    private final SituationBHive situation;
    //private final ArrayList<SituationBHive> currentLayerList;

    /*
     * The following method will later be modified to also take the expression which defines the prevailing protocol
     * Here we assume the prevailing protocol is KIG so we hard code it
     */
    public TaskComputeNodeSuccessorsbkupb4genframework(SituationBHive aSituation, HashMap<String, String> expandedCallConditions) {
        situation = aSituation;
       // currentLayerList = currLayerList;
    }

    @Override
    public ArrayList<SituationBHive> call() {
        ArrayList<SituationBHive> iEquivList = new ArrayList<>();
        ArrayList<String> iCallList;
        String refLocalState;
        int numCallsInHistory;

        // here the task is to generate the successors for "situation", given the currentLayerList in which
        // the given "situation" is situated.
        ArrayList<SituationBHive> nodeNextLayer = new ArrayList<>();

        if (isConfigurationComplete(situation.getConfiguration())) {
            //...and take some stats
           // numCallsInHistory = situation.getHistory().split(";").length;
            FrameworkEntry.takeStats(situation);  //we will use this method to collect other stats as well

            //other stats later include 
            //   -length of current history, we keep separate count for each possible length
            //   -print out the sequence

            //return the empty list
            return nodeNextLayer;
        }

        for (int i = 0; i < FrameworkEntry.numberOfAgents; i++) {
            //now let us find other elements of currentList that are equivalent to the currentHistory for agent i

//            //now given the current situation, get the local state of the current agent
//            refLocalState = getLocalState(i, situation.getHistory());
//
//            for (SituationBHive sibSituation : currentLayerList) {
//                //two states(histories) are equivalent for i if i's local state is the same in both AND
//                //and i learns the same secret in both
//                if (refLocalState.equalsIgnoreCase(getLocalState(i, sibSituation.getHistory()))) {
//                    if (situation.getConfiguration()[i] == sibSituation.getConfiguration()[i]) {
//                        //that means the two histories belong to the equivalence class for the current agent i
//                        //so include sibSituation in iEquivList. Keep in mind that "situation" itself will eventually 
//                        //feature in the iEquivList as well...
//                        iEquivList.add(sibSituation);
//                    }
//                }
//            }


            /*
             * The following chunck of code enables us to get the equivalence 
             * class for a given agent. Using the knowledge of the Bees World
             * for that agent and the colony of the situation being considered,
             * In the Bees World for that agent
             */

            iEquivList = situation.getColony(i).copyColonyMembers();
            /*
             * ENDS CHUNK FOR EQUIVALENCE CLASS
             */

            /*
             * The following chunck of code enables us get the calls an agent 
             * can make in a situation, based on the prevailing protocol. This
             * is where we evaluate the epistemic condition for selection 
             * of callers --- a key part of the general framework
             */
            //now that we have the iEquivList (that is, all histories (situations) that are equivalent to currentHistory for i)
            //So let's now see which calls i can make at current situation
            /// First of all, let's clear the previous iCallList
            iCallList = getFullCallListFor(i);
            for (int j = 0; j < FrameworkEntry.numberOfAgents; j++) {
                if (j == i) {
                    //we don't want to compare i's secrets with i's secrets! but they are equal though ;)
                    iCallList.remove(String.valueOf(j)); //it's important that the object itself is targetted for removal, and not the index which changes as the list shrinks (source of bug)!
                    continue;
                }
                for (SituationBHive equivSituation : iEquivList) {
                    if (situation.getConfiguration()[i] == equivSituation.getConfiguration()[j]) {  //THIS CONDITION IS EXCLUSIVE TO KIG  -- notice that this is the point we now have to evaluate the epistemic condition that determines whether i can call j
                        //agent j does not know more or less than i, so no rationale under this protocol for i to call j
                        // so we exclude j from the iCallList
                        iCallList.remove(String.valueOf(j));
                        break;
                    }
                }
            }
            //So now that we have everyone i can call at "callHistory", let's actually extend callHistory,
            //by those calls; add these to the nodeNextLayer
            for (String iCall : iCallList) {
                if (iCall.compareToIgnoreCase(String.valueOf(i)) > 0) {
                    //that means i is lexicographically less than iCall
                    //so we place i first, since we want an ascending lexicographic ordering
                    //BUT we want to add only if no such history had already been added
                    //that is, we don't want symmetric calls (ij==ji)...

                    //Create a new situation
                    SituationBHive newSituation = new SituationBHive(situation.getHistory() + (situation.getHistory().isEmpty() ? "" : ";") + i + iCall, makeCall(i + iCall, situation.copyConfiguration()), situation);
                    //so we copy the replication factor of the parent situation
                    newSituation.setReplicationFactor(situation.getReplicationFactor());
                    
                    int index = nodeNextLayer.indexOf(newSituation);
//                    if (!nodeNextLayer.contains(newSituation)) {
                    if(index == -1){ // the collection does not contain newSituation, so we treat it as new
                        //set the colony of this situation, and add it to the colony (that is, two way!)
                        setAndUpdateColony(newSituation, situation);
                        nodeNextLayer.add(newSituation); // no need to touch the replication factor (leave it as 1 i.e the default value)
                    } else { // so the collection already contains new situation so we update its replication factor
                        // get the object, which is about to be replicated
                        // the new replication factor of the replicated node is the replication factor of the would-have-been replicate (newSituation) PLUS the 
                        // current replication factor of the situation that is replicated (already in the collection)
                        nodeNextLayer.get(index).setReplicationFactor(newSituation.getReplicationFactor() + nodeNextLayer.get(index).getReplicationFactor());
                    }
                } else if (iCall.compareToIgnoreCase(String.valueOf(i)) < 0) {
                    SituationBHive newSituation = new SituationBHive(situation.getHistory() + (situation.getHistory().isEmpty() ? "" : ";") + iCall + i, makeCall(iCall + i, situation.copyConfiguration()), situation);
                    
                    //so we copy the replication factor of the parent situation
                    newSituation.setReplicationFactor(situation.getReplicationFactor());
                    int index = nodeNextLayer.indexOf(newSituation);
//                    if (!nodeNextLayer.contains(newSituation)) {
                    if(index == -1){ // the collection does not contain newSituation, so we treat it as new
                        //set the colony of this situation, and add it to the colony (that is, two way!)
                        setAndUpdateColony(newSituation, situation);  //we are currently
                        nodeNextLayer.add(newSituation);
                    }  else { // so the collection already contains new situation so we update its replication factor
                        // get the object, which is about to be replicated
                        // the new replication factor of the replicated node is the replication factor of the would-have-been replicate (newSituation) PLUS the 
                        // current replication factor of the situation that is replicated (already in the collection)
                        nodeNextLayer.get(index).setReplicationFactor(newSituation.getReplicationFactor() + nodeNextLayer.get(index).getReplicationFactor());
                    }
                }  //I don't expect to get "==0" since I don't have a self caller...
            }
            
            iEquivList.clear();
            iCallList.clear();

        }
        return nodeNextLayer;
    }

    public boolean isConfigurationComplete(int[] configuration) {
        for (int i : configuration) {
            if (i != Math.pow(2, FrameworkEntry.numberOfAgents) - 1) {
                return false;
            }
        }
        return true;
    }

    /*
     * the method that will dynamically give you the local state of a given agent
     * for any given global state. The global state is the sequence
     */
    public String getLocalState(int agent, String globalState) {
        String[] theCalls = globalState.split(";");
        String localState = "";
        if (globalState.isEmpty()) {
            return localState; //so local state is defined as empty at the initial state
        }
        for (String aCall : theCalls) {
            if (aCall.substring(0, 1).equalsIgnoreCase(String.valueOf(agent))) {
                //agent is in the call, it is the caller
                localState = localState + aCall.substring(1, 2);  //that means get the calling patner
            } else if (aCall.substring(1, 2).equalsIgnoreCase(String.valueOf(agent))) {
                //agent is in the call, it is the called
                localState = localState + aCall.substring(0, 1); //return its calling partner
            } else {
                //agent is not in the call
                localState = localState + "*";
            }
        }
        return localState;
    }

    /*
     * Returns an array list of all the agents in the scenario
     */
    public ArrayList<String> getFullCallListFor(int agent) {
        ArrayList<String> callers = new ArrayList<>((int) FrameworkEntry.numberOfAgents);
        for (int i = 0; i < FrameworkEntry.numberOfAgents; i++) {
            // agent cannot be expected to call itself, but we'll take care of that later at use (that is by first removing
            //the reference agent from the list first) -- this approach is clearer and more straightforward although the computational
            //advantage is not quite sure
            //it's easier that way since we have to later "exclude" these elements either by their
            //reference (which we won't have as we will be iterating over them with int indexes)
            //or by their index, which is not straightforward mapping to the agent id, as with the 
            //previous method the list is no longer equal to the number of agents, and current agent
            //is dynamic so we would have to do some overhead computation to determine the correct index
            //of the element we intend to exclude (if index to be excluded is more that agent, deduct 1 from it; 
            //if index to be removed (target index) is less than agent, leave it as it is).
            callers.add(String.valueOf(i));
        }
        return callers;
    }

    /*
     * This simply makes the given call sequence in the given configuration and
     * returns the resulting configuration
     */
    public int[] makeCall(String callSequence, int[] aConfiguration) {
        int i;
        int j;
        if (callSequence.isEmpty()) {
            return aConfiguration;
        }
        for (String aCall : callSequence.split(";")) {
            i = Integer.parseInt(aCall.substring(0, 1));
            j = Integer.parseInt(aCall.substring(1, 2));
            aConfiguration[i] = aConfiguration[j] = aConfiguration[i] | aConfiguration[j];
        }
        return aConfiguration;
        //the returned rootConfiguration is terminal is all it's elements are equal to (2^numagents)-1
    }

    public boolean iKnowsJsSecret(int i, int j, SituationBHive situation) {
        return ((situation.getConfiguration()[i] | (int) Math.pow(2, j)) == situation.getConfiguration()[i]);

//        /*
//         * This is true if I am able to take away j's secret from i's current sum of secrets
//         * starting from the agent with the highest base secret, without getting a negative
//         * number.
//         */
//        int iKnowledge = situation.getConfiguration()[i];
//        for (int k = FrameworkEntry.numberOfAgents - 1; k > j; k--) {
//            if (iKnowledge - Math.pow(2, k) < 0) {
//                continue;
//            } else {
//                iKnowledge -= Math.pow(2, k);
//            }
//        }
//        //now try removing j's unique secret (k=j), having removed every base 
//        //secret greater than j that can be removed
//        if (iKnowledge - Math.pow(2, j) >= 0) {
//            return true;  // that means i knows j's secret in the given situation
//        } else {
//            return false;  // that means i does not know j's secret
//        }
    }

    private void setAndUpdateColony(SituationBHive newSituation, SituationBHive parentSituation) {
        for (int i = 0; i < FrameworkEntry.numberOfAgents; i++) {
            Colony parentColony = parentSituation.getColony(i);
             synchronized (parentColony) {
            //obtain the colony of the parent situation, that is the parent colony, and check if it has any child colonies already
            if (parentColony.getChildColonies().isEmpty()) {
                //then create a new child colony in the migration colony nextBWorlds
                Colony childColony = new Colony();
                childColony.addOneMember(newSituation);
                //get the nextBWorld of the given agent caller
                //begin for next two lines, the two-way update
//                FrameworkEntry.nextBWorlds.get(i).add(childColony);
                FrameworkEntry.updateNextBWorld(i, childColony);
                newSituation.setColony(i, childColony);
                //update the child colonies of parent
                parentColony.addOneChildColony(childColony);
            } else {
                //iterate over the child colonies of the parent situation to see if the new situation fits in one of them
                Iterator it = parentColony.getChildColonies().iterator(); //note that the references to childColonies are resolved in nextBWorlds! Is this possible, this inter-object communications??
                    boolean colonyMatch = false;
                
                    SituationBHive sit;
                    Colony childColony;
                    while (it.hasNext()) {
                        childColony = (Colony) it.next();
                        sit = childColony.getColonyMembers().get(0); //I think I made sure once a colony is added, then it must not be an empty colony, it must have at least one member
                        if (newSituation.getLocalState(i).equalsIgnoreCase(sit.getLocalState(i))
                                && (newSituation.getConfiguration()[i] == sit.getConfiguration()[i])) {
                            //in that case newSituation belongs to the colony of sit, for they are in the same equivalence class for this agent
                            childColony.addOneMember(newSituation);
                            //there is no need to add a new colony to nextBWorlds since we needed only to update an existing colony
                            //But, let us now set the colony of this situation
                            newSituation.setColony(i, childColony);
                            //no need to add a new child colony under parent colony
                            //Now we are done with the colony as well as with this agent, no more to be done so we break
                            colonyMatch = true;
                            break;
                        }
                    }
                
                if (!colonyMatch) {
                    //no existing child colony could accommodate newSituation
                    //so we break-off and start a new colony for it
                    //same as with empty list of child colonies addressed above
                    //then create a new child colony in the migration colony nextBWorlds
                    Colony migChildColony = new Colony();
                    migChildColony.addOneMember(newSituation);
                    //get the nextBWorld of the given agent caller
                    //begin for next two lines, the two-way update
//                    FrameworkEntry.nextBWorlds.get(i).add(migChildColony);
                    FrameworkEntry.updateNextBWorld(i, migChildColony);
                    newSituation.setColony(i, migChildColony);
                    //update the child colonies of parent
                    parentColony.addOneChildColony(migChildColony);
                }
            }
             }
        }
    }
}
