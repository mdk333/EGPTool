/**
 *
 * @author Maduka Attamah
 *
 * Copyright 2011-2015 Maduka Attamah
 *
 */

package egp.concurrent.modeller;

import egp.FrameworkEntry;
import egp.concurrent.parsers.ExpCCParser;
import egp.concurrent.util.Utilities;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TaskComputeNodeSuccessors implements Callable {

    private final SituationBHive situation;
    private final HashMap<String, String> expandedCallConditions;
    //private final ArrayList<SituationBHive> currentLayerList;

    /*
     * The following method will later be modified to also take the expression which defines the prevailing protocol
     * Here we assume the prevailing protocol is KIG so we hard code it
     */
    public TaskComputeNodeSuccessors(SituationBHive aSituation, HashMap<String, String> expandedCCs) {
        situation = aSituation;
        expandedCallConditions = expandedCCs;
        // currentLayerList = currLayerList;

    }

    @Override
    public ArrayList<SituationBHive> call() {
        //  ArrayList<SituationBHive> iEquivList = new ArrayList<>();
        ArrayList<String> iCallList = new ArrayList<>();
        //  String refLocalState;
        //  int numCallsInHistory;

        // here the task is to generate the successors for "situation", given the currentLayerList in which
        // the given "situation" is situated.
        ArrayList<SituationBHive> nodeNextLayer = new ArrayList<>();

        if (isConfigurationComplete(situation.getConfiguration())) {
            //...and take some stats
//            numCallsInHistory = situation.getHistory().split(";").length;
//            FrameworkEntry.takeStats(numCallsInHistory, situation.getReplicationFactor());  //we will use this method to collect other stats as well

            FrameworkEntry.takeStats(situation);
            //other stats later include 
            //   -length of current history, we keep separate count for each possible length
            //   -print out the sequence

            //return the empty list
            return nodeNextLayer;
        }

        String formula = "";
        for (int i = 0; i < FrameworkEntry.numberOfAgents; i++) {
            for (int j = 0; j < FrameworkEntry.numberOfAgents; j++) {
                if (j == i) {
                    //we don't want to compare i's secrets with i's secrets! but they are equal though ;)
                    continue;
                }
                //If topology is in use, then check neighbourhood
                /*
                 If i and j are NOT neighbours, the rest of the program which handles the checking of the epistemic
                 calling condition between i and j, will never be executed due to the "continue" keyword. But if i and j
                 j are neigbours then then the execution will run down across the "if" statements and get to the epistemic 
                 calling conditions.
                 */
                if (FrameworkEntry.topologyFlag == true) {
                    if (!FrameworkEntry.topologyDBase.contains("(" + i + "," + j + ")")) {
                        continue;
                    }
                }
                formula = expandedCallConditions.get(i + "" + j);
                ExpCCParser p = new ExpCCParser(new ExpandedCallConditionScanner(new StringReader(formula)), situation);
                try {
                    HashSet<SituationBHive> result = (HashSet<SituationBHive>) p.parse().value;
                    if (result.contains(situation)) {
                        //the expanded formula is satisfied in the current "situation"
                        //so we add this ij to the list of calls that will be made on this situation
                        //we say i can call j, so we update iCallList accordingly

                        //If topology is in use, then also check neighbourhood
//                        if (FrameworkEntry.topologyFlag == true) {
//                            if (FrameworkEntry.topologyDBase.contains("(" + i + "," + j + ")")) {
//                                iCallList.add(j + "");
//                            }
//                        } else {
                        iCallList.add(j + "");
//                        }
                    }
                } catch (Exception ex) {
                    Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            /*
             * Now let's check for deadlocks. Deadlock occurs when there is no call that can be made
             * eventhough the situation is not yet complete (everyone does not yet know every secret.
             * So if iCallList is empty at this point, then this situation is at deadlock
             */
//            if(iCallList.isEmpty()){
//                //Deadlock! add it to the deadlocked pile, and quit this method
//                FrameworkEntry.deadLockSequences.add(situation);
//                return nodeNextLayer;
//            }
            //           //So now that we have everyone i can call at "callHistory", let's actually extend callHistory,
            //           //by those calls; add these to the nodeNextLayer
            /*
             * I modified this block on 30/06/2014, removing the need to sort the calling parties lexicographically
             * because we need to know when, for example 02 and when 20.
             * I don't think there is need also to check if a situation "already exists in next layer" - the algorithm
             * doesn't seem capable of producing that, so I removed this check...
             * We check whether the results stay the same by comparing with produced results in previous versions (version 3 down)
             * We wish to also observe the time difference this has produced
             */

            if (FrameworkEntry.allowSymmetricCallsInOutput == true) {
                for (String iCall : iCallList) {

                    //Create a new situation
                    SituationBHive newSituation = new SituationBHive(situation.getHistory() + (situation.getHistory().isEmpty() ? "" : ";") + i + iCall, makeCall(i + iCall, situation.copyConfiguration()), situation);
                    //so we copy the replication factor of the parent situation
                    newSituation.setReplicationFactor(situation.getReplicationFactor());

                    setAndUpdateColony(newSituation, situation);
                    nodeNextLayer.add(newSituation); // no need to touch the replication factor (leave it as 1 i.e the default value)

                }
            } else {
                for (String iCall : iCallList) {
                    if (iCall.compareToIgnoreCase(String.valueOf(i)) > 0) {
                        //                   //that means i is lexicographically less than iCall
                        //                   //so we place i first, since we want an ascending lexicographic ordering
                        //                   //BUT we want to add only if no such history had already been added
                        //                   //that is, we don't want symmetric calls (ij==ji)...

                        //                   //Create a new situation
                        SituationBHive newSituation = new SituationBHive(situation.getHistory() + (situation.getHistory().isEmpty() ? "" : ";") + i + iCall, makeCall(i + iCall, situation.copyConfiguration()), situation);

                        //                    //so we copy the replication factor of the parent situation
                        newSituation.setReplicationFactor(situation.getReplicationFactor());

                        int index = nodeNextLayer.indexOf(newSituation);
////                    if (!nodeNextLayer.contains(newSituation)) {
                        if (index == -1) { // the collection does not contain newSituation, so we treat it as new
                            //                       //set the colony of this situation, and add it to the colony (that is, two way!)
                            setAndUpdateColony(newSituation, situation);
                            nodeNextLayer.add(newSituation); // no need to touch the replication factor (leave it as 1 i.e the default value)
                        } else { // so the collection already contains new situation so we update its replication factor
//                        // get the object, which is about to be replicated
//                        // the new replication factor of the replicated node is the replication factor of the would-have-been replicate (newSituation) PLUS the 
//                        // current replication factor of the situation that is replicated (already in the collection)
                            nodeNextLayer.get(index).setReplicationFactor(newSituation.getReplicationFactor() + nodeNextLayer.get(index).getReplicationFactor());
                        }
                    } else if (iCall.compareToIgnoreCase(String.valueOf(i)) < 0) {
                        SituationBHive newSituation = new SituationBHive(situation.getHistory() + (situation.getHistory().isEmpty() ? "" : ";") + iCall + i, makeCall(iCall + i, situation.copyConfiguration()), situation);

                        //so we copy the replication factor of the parent situation
                        newSituation.setReplicationFactor(situation.getReplicationFactor());
                        int index = nodeNextLayer.indexOf(newSituation);
//                    if (!nodeNextLayer.contains(newSituation)) {
                        if (index == -1) { // the collection does not contain newSituation, so we treat it as new
                            //set the colony of this situation, and add it to the colony (that is, two way!)
                            setAndUpdateColony(newSituation, situation);  //we are currently
                            nodeNextLayer.add(newSituation);
                        } else { // so the collection already contains new situation so we update its replication factor
                            // get the object, which is about to be replicated
                            // the new replication factor of the replicated node is the replication factor of the would-have-been replicate (newSituation) PLUS the 
                            // current replication factor of the situation that is replicated (already in the collection)
                            nodeNextLayer.get(index).setReplicationFactor(newSituation.getReplicationFactor() + nodeNextLayer.get(index).getReplicationFactor());
                        }
                    }  //I don't expect to get "==0" since I don't have a self caller...
                }
            }
            // iEquivList.clear();
            iCallList.clear();

        }
        //if nodeNextLayer is empty, that means we have a deadlocked situation here
        if (nodeNextLayer.isEmpty()) {
            FrameworkEntry.updateDeadLockedList(situation);
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
                        sit = (SituationBHive) childColony.getColonyMembers().get(0); //I think I made sure once a colony is added, then it must not be an empty colony, it must have at least one member
                        if (isEquivalent(newSituation, sit, i) == true) {
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

    private boolean isEquivalent(SituationBHive sitA, SituationBHive sitB, int agent) {
        switch (FrameworkEntry.equivalenceNotion) {
            case 0: {
                //same configuration only
                return sitA.getConfiguration()[agent] == sitB.getConfiguration()[agent];
            }
            case 1: {
                //same history only
                return sitA.getLocalState(agent).equalsIgnoreCase(sitB.getLocalState(agent));
            }
            case 2: {
            //same configuration AND same history
            /* The following is then the difference between version 6 and version 7,
                 namely, we ensure that the agent under consideration learns the same secret
                 from calling the same other agent. The following are the steps we take to
                 add this additional constraint:
                 (1) get the local state of both sequences
                 (2) if the local states are equul and the configurations are equal then proceed with step 3
                 (3) if the last character of both local states is not '*' then proceed to step 4 (this means the agent under
                 consideration called the same other agent in the last call, and the last character of the local state is
                 actually that other agent that called with the agent under consideration)
                 (4) extract the last character of the local states,
                 (5) get the parent node of both nodes
                 (6) check that the agent extracted in step 4 knows the same secrets at the two parent nodes extracted in step 5
                 */
                String lsA = sitA.getLocalState(agent);
                String lsB = sitB.getLocalState(agent);

                if (lsA.equalsIgnoreCase(lsB) && (!lsA.endsWith("*"))
                        && (sitA.getParentSituation().getConfiguration()[Integer.parseInt(lsA.substring(lsA.length() - 1))] == sitB.getParentSituation().getConfiguration()[Integer.parseInt(lsB.substring(lsB.length() - 1))])
                        && (sitA.getConfiguration()[agent] == sitB.getConfiguration()[agent])) {
                    return true;
                } else return lsA.equalsIgnoreCase(lsB) && (lsA.endsWith("*")) && (sitA.getConfiguration()[agent] == sitB.getConfiguration()[agent]);
            }
            case 3: {
                //same configuration OR same history
                return sitA.getLocalState(agent).equalsIgnoreCase(sitB.getLocalState(agent))
                        || (sitA.getConfiguration()[agent] == sitB.getConfiguration()[agent]);
            }
            default: {
                return sitA.getLocalState(agent).equalsIgnoreCase(sitB.getLocalState(agent))
                        && (sitA.getConfiguration()[agent] == sitB.getConfiguration()[agent]);
            }
        }
    }
}
