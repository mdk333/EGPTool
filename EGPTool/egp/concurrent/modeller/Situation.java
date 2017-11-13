
/**
 *
 * @author Maduka Attamah
 *
 * Copyright 2011-2015 Maduka Attamah
 */

package egp.concurrent.modeller;

import java.io.Serializable;

public class Situation implements Serializable {
    private static final long serialVersionUID = 2L;
    private String history;
    private int[] configuration;
    private boolean isTerminal;
    private String[] localState;
    private int replicationFactor;
    private Situation parentSituation;

    public int getReplicationFactor() {
        return replicationFactor;
    }

    public void setReplicationFactor(int replicationFactor) {
        this.replicationFactor = replicationFactor;
    }
    
    public void increamentReplicationFactor(){
        this.replicationFactor++;
    }
    
    public Situation(String aHistory, int[] aConfiguration, Situation parentSit){
        history = aHistory;
        configuration = aConfiguration;
        isTerminal = false; // by default
        localState = new String[aConfiguration.length];
        for (int i = 0; i < aConfiguration.length; i++) {
            localState[i] = this.computeLocalState(i);
        }
        
        this.replicationFactor = 1;
        this.parentSituation = parentSit;
        
    }
       
    /* gives us the root situation, history should be empty here */
    public Situation(int numAgents){
        history = "";
        configuration = this.getInitConfiguration(numAgents);
        localState = new String[numAgents];
        for (int i = 0; i < numAgents; i++) {
            localState[i] = this.computeLocalState(i);
        }
        this.replicationFactor = 1;
        this.parentSituation = null;
    }
    
    public Situation(String aHistory, int numAgents, Situation parentSit){
        history = aHistory;
        configuration = this.makeCall(aHistory, this.getInitConfiguration(numAgents));
        localState = new String[numAgents];
        for (int i = 0; i < numAgents; i++) {
            localState[i] = this.computeLocalState(i);
        }
        this.replicationFactor = 1;
        this.parentSituation = parentSit;
    }
    
    public int[] getConfiguration(){
        return this.configuration;
    }
    
    public int[] copyConfiguration(){
        int[] config = new int[this.configuration.length];
        System.arraycopy(this.configuration, 0, config, 0, this.configuration.length);
        return config;
    }
    
    public String getHistory(){
        return this.history;
    }
    
    public Situation getParentSituation(){
        return this.parentSituation;
    }
 
    public boolean isTerminal(){
        return this.isTerminal;
    }
    
    public synchronized void setIsTerminal(boolean val){
        this.isTerminal = val;
    }

    /*
     * This method is needed by arrayList.contains() method to check whether a list 
     * already contains a situation
     */
    @Override
    public boolean equals(Object situation){
        Situation sit = (Situation)situation;
        if(this.history.equalsIgnoreCase(sit.history)){
            return true;
        }
        return false;
        
    }
    
     private int[] makeCall(String callSequence, int[] aConfiguration) {
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
    
    private int[] getInitConfiguration(int numberOfAgents) {
        int[] config = new int[(int) numberOfAgents];
        int baseSecret = 1;
        for (int i = 0; i < numberOfAgents; i++) {
            config[i] = baseSecret;
            baseSecret *= 2;
        }
        return config;
    }
    
    
    public String getLocalState(int agent){
        return this.localState[agent];
    }
    
    /*
     * the method that will dynamically give you the local state of a given agent
     * for any given global state. The global state is the sequence
     */
    private String computeLocalState(int agent) {
        String globalState = this.history;
        String[] theCalls = globalState.split(";");
        String locState = "";
        if (globalState.isEmpty()) {
            return locState; //so local state is defined as empty at the initial state
        }
        for (String aCall : theCalls) {
            if (aCall.substring(0, 1).equalsIgnoreCase(String.valueOf(agent))) {
                //agent is in the call, it is the caller
                locState = locState + aCall.substring(1, 2);  //that means get the calling patner
            } else if (aCall.substring(1, 2).equalsIgnoreCase(String.valueOf(agent))) {
                //agent is in the call, it is the called
                locState = locState + aCall.substring(0, 1); //return its calling partner
            } else {
                //agent is not in the call
                locState = locState + "*";
            }
        }
        return locState;
    }
    
    public void printHistory(){
        System.out.printf("%s%n", this.history);
    }
  
}
