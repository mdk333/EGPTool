/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package egp.concurrent.modeller;

import java.util.ArrayList;

/**
 *
 * @author byear
 */
public class Colony {
    private final ArrayList<SituationBHive> colonyMembers;
    private final ArrayList<Colony> childColonies;
    
    public Colony(){
        colonyMembers = new ArrayList<>();
        childColonies = new ArrayList<>();
    }
    
    public synchronized ArrayList<SituationBHive> getColonyMembers(){
        return this.colonyMembers;
    }
    
    public synchronized ArrayList<SituationBHive> copyColonyMembers(){
        return new ArrayList<>(this.colonyMembers);
    }
    
    public synchronized ArrayList<Colony> getChildColonies(){
        return this.childColonies;
    }
    
    public synchronized void addMembers(ArrayList<SituationBHive> members){
        this.colonyMembers.addAll(members);
    }
    
    public synchronized void addOneMember(SituationBHive member){
        this.colonyMembers.add(member);
    }
    
    public synchronized void addChildColonies(ArrayList<Colony> children){
        this.childColonies.addAll(children);
    }
    
    public synchronized void addOneChildColony(Colony child){
        this.childColonies.add(child);
    }
    
}
