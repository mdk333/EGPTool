/**
 *
 * @author Maduka Attamah
 *
 * Copyright 2011-2015 Maduka Attamah
 *
 */
package egp.concurrent.util;

import egp.FrameworkEntry;
import egp.concurrent.modeller.Situation;
import egp.concurrent.modeller.SituationBHive;
import egp.concurrent.parsers.BoolExprParser;
import egp.concurrent.scanners.BoolConditionScanner;
import egp.concurrent.scanners.GenCombinationGenerator;
import java.io.StringReader;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Utilities {

    public HashSet<SituationBHive> TT;// = new HashSet<>();
    public SituationBHive currentSituation;  //just making this a static variable makes the whole program erratic, yielding different result for each run! (temperamental) - think about this, the reason.

    public Utilities(SituationBHive situation) {
        TT = new HashSet<>();
        currentSituation = situation;
        TT.add(currentSituation);
    }

    public Utilities() {
    }

    public static String expandDisjunct(String agentList, String disjunctExpression) {

        String output = "";
        //we want to prepare the data set for combination generation
        StringBuffer partitions = new StringBuffer();
        String elementsString = "";
        int numAgents = FrameworkEntry.numberOfAgents;

        for (int i = 0; i < agentList.split(",").length; i++) {  // that is the number agent-variables over the disjunction
            for (int j = 0; j < numAgents; j++) {
                elementsString = elementsString + j + ",";
            }
            partitions.append(i * numAgents).append(",").append(i * numAgents + (numAgents - 1)).append(";"); //increamentally constructs the partitions for the elementString
        }

        GenCombinationGenerator gen = new GenCombinationGenerator(elementsString.split(",").length, agentList.split(",").length);
        String[] combinations = gen.generateAll(elementsString.split(","), partitions.toString().split(";"));
        String[] agents = agentList.split(",");
        String temp = "";
        //now we use the variable in agentList as place holders to substitute the combinations we got from generateAll method above
        for (int i = 0; i < combinations.length; i++) {
            temp = disjunctExpression;
            for (int j = 0; j < agents.length; j++) {
                //Hey, there is need to check somewhere that the agent ids used in the disjunct expression are same as those
                //specified in the agent list, so as to ensure that the substitution here is correct!!!
                //The reason for the square brackets around agents[j] is because in the cup specification I deliberately
                //surrounded the agentId with the square brackets to be able to distinguish them from other naturally occuring
                //character sequences during replacement.
                temp = temp.replaceAll("\\[" + agents[j] + "\\]", combinations[i].split(",")[j]);
            }
            output = output + (output.isEmpty() ? ("(" + temp + ")") : ("||" + "(" + temp + ")"));
        }
        return output;
    }

    public static String expandConjunct(String agentList, String disjunctExpression) {
        String output = "";
        //we want to prepare the data set for combination generation
        StringBuffer partitions = new StringBuffer();
        String elementsString = "";
        int numAgents = FrameworkEntry.numberOfAgents;

        for (int i = 0; i < agentList.split(",").length; i++) {  // that is the number agent-variables over the disjunction
            for (int j = 0; j < numAgents; j++) {
                elementsString = elementsString + j + ",";
            }
            partitions.append(i * numAgents).append(",").append(i * numAgents + (numAgents - 1)).append(";"); //increamentally constructs the partitions for the elementString
        }

        GenCombinationGenerator gen = new GenCombinationGenerator(elementsString.split(",").length, agentList.split(",").length);
        String[] combinations = gen.generateAll(elementsString.split(","), partitions.toString().split(";"));
        String[] agents = agentList.split(",");
        String temp = "";
        //now we use the variable in agentList as place holders to substitute the combinations we got from generateAll method above
        for (int i = 0; i < combinations.length; i++) {
            temp = disjunctExpression;
            for (int j = 0; j < agents.length; j++) {
                //Hey, there is need to check somewhere that the agent ids used in the disjunct expression are same as those
                //specified in the agent list, so as to ensure that the substitution here is correct!!!
                //The reason for the square brackets around agents[j] is because in the cup specification I deliberately
                //surrounded the agentId with the square brackets to be able to distinguish them from other naturally occuring
                //character sequences during replacement.
                temp = temp.replaceAll("\\[" + agents[j] + "\\]", combinations[i].split(",")[j]);
            }
            output = output + (output.isEmpty() ? ("(" + temp + ")") : ("||" + "(" + temp + ")")); //THIS METHOD IS EXACTLY THE SAME AS THE ONE ABOVE EXCEPT FOR THE && SIGN HERE,
            //AND OF COURSE THE NAME OF THE METHOD. SO WE COULD MERGE THE TWO METHODS BY ADDING ONE EXTRA ARGUMENT TO SPECIFY THE 
            //STRINGING FUNCTION...IN THAT WAY WE MAKE IT MORE GENERIC.
        }
        return output;
    }

    /* This method takes the expanded condition expression, that is the condition expression with
     * all the disjunct and conjunct operators expanded fully, from the above methods "expandConjunct"
     * and "expandDisjunct".
     * 
     * The method then passes the conditionExpression to another parser to obtain the result of this expression
     * we will fix agentIdA and agentIdB from the calling forLoop in the TaskComputeNode successor for 
     * the current agents i,j of the nested loops. That means the 
     */
    public static String evaluateCallCondition(String agentIdA, String agentIdB, String conditionExpression, String callerOne, String callerTwo) {
        //first we substitute the actual i,j callers for the place holders in "let a call b..." of the specification
        //it's now easier to do with the expanded call condition

        conditionExpression = conditionExpression.replaceAll("\\[" + agentIdA + "\\]", callerOne);
        conditionExpression = conditionExpression.replaceAll("\\[" + agentIdB + "\\]", callerTwo);

        //now we can call or second parser for the full expression is now ready for evaluation
        return conditionExpression;
    }

    public static Integer getSetMagnitude(Integer sec) {
        int count = 0;
        for (int i = FrameworkEntry.numberOfAgents - 1; i >= 0; i--) {
            if ((sec.intValue() - Math.pow(2, i)) > 0) {
                sec = (int) (sec.intValue() - Math.pow(2, i));
                count++;
            }
        }
        return new Integer(count);
    }

    public static Integer getModulus(Integer num1, Integer num2) {
        return num1.intValue() % num2.intValue();
    }

    public static Integer getProduct(Integer num1, Integer num2) {
        return num1.intValue() * num2.intValue();
    }

    public static Integer getAddition(Integer num1, Integer num2) {
        return num1.intValue() + num2.intValue();
    }

    public static Integer getSubtraction(Integer num1, Integer num2) {
        return num1.intValue() - num2.intValue();
    }

    public static Integer getEmptySet() {
        return new Integer(0);  // we assume that the integer zero is the empty set
    }

    public static Integer getAgentSecret(String agentId, Situation currentSituation) {
        return new Integer(currentSituation.getConfiguration()[Integer.parseInt(agentId)]);
    }

    public static Integer getAgentInitSecret(String agentId) {
        return new Integer((int) Math.pow(2, Integer.parseInt(agentId)));
    }

    public static Integer getFinalState() {
        return new Integer((int) Math.pow(2, FrameworkEntry.numberOfAgents));
    }

    public static Integer getSetComplement(Integer num) {
        return new Integer((int) (Math.pow(2, FrameworkEntry.numberOfAgents) - num.intValue()));  //that is, final set minus current set (integer-wise)
    }

    public static Integer getSetUnion(Integer num1, Integer num2) {
        return new Integer(num1.intValue() | num2.intValue()); //just like the call itself
    }

    public static Integer getSetIntersection(Integer num1, Integer num2) {
        return new Integer(num1.intValue() & num2.intValue());  //that is the bits that are common among the two sets
    }

    public static Integer getSetMinus(Integer num1, Integer num2) {
        return new Integer(num1.intValue() & (~num2.intValue()));  //use professor osuagwu's digital design techniques
    }

    public static Boolean isIntExprNotEqual(Integer ie1, Integer ie2) {
        return (ie1.intValue() != ie2.intValue());
    }

    public static Boolean isIntExprEqual(Integer ie1, Integer ie2) {
        return (ie1.intValue() == ie2.intValue());
    }

    public static Boolean isIntExprLessThanEqual(Integer ie1, Integer ie2) {
        return (ie1.intValue() <= ie2.intValue());
    }

    public static Boolean isIntExprLessThan(Integer ie1, Integer ie2) {
        return (ie1.intValue() < ie2.intValue());
    }

    public static Boolean isIntExprGreaterThanEqual(Integer ie1, Integer ie2) {
        return (ie1.intValue() >= ie2.intValue());
    }

    public static Boolean isIntExprGreaterThan(Integer ie1, Integer ie2) {
        return (ie1.intValue() > ie2.intValue());
    }

    public static Boolean isSetExprNotEqual(Integer se1, Integer se2) {
//        if(se1.intValue()!=se2.intValue()){
//            return true;
//        } else {
//            return false;
//        }
        //   return (se1 != se2); //This !=, and == leads to confusion, it compares the object references rather than their values!!! It caused serious bugs and confusion
        return (se1.intValue() != se2.intValue());
    }

    public static Boolean isSetExprEqual(Integer se1, Integer se2) {
        return (se1.intValue() == se2.intValue());
    }

    public static Boolean isProperSubset(Integer se1, Integer se2) {
        return ((se1.intValue() != se2.intValue()) && ((se1.intValue() | se2.intValue()) == se2.intValue()));  //that is, set1 does not contribute to set 2, but set1 is not equal to set2
    }

    public static Boolean isSubset(Integer se1, Integer se2) {
        return ((se1.intValue() | se2.intValue()) == se2.intValue());  //here set1 and set1 could be equal while se1 not contributing to se2
    }

    public static Boolean isNotElementOf(Integer sing, Integer se) {
        return ((sing.intValue() | se.intValue()) != se.intValue());  //sing would contribute sth to set by OR if it were not already in set
    }

    public static Boolean isElementOf(Integer sing, Integer se) {
        return ((sing.intValue() | se.intValue()) == se.intValue());   //on the contrary, there is nothing for sing to contribute to set if it is already an element of set
    }

    public HashSet<SituationBHive> getCondExprImplication(HashSet<SituationBHive> ce1, HashSet<SituationBHive> ce2) {
        return getCondExprOROperator(getCondExprNOT(ce1), ce2);
    }

    public HashSet<SituationBHive> getCondExprOROperator(HashSet<SituationBHive> ce1, HashSet<SituationBHive> ce2) {
        ce1.addAll(ce2);
        return ce1;
    }

    public HashSet<SituationBHive> getCondExprANDOperator(HashSet<SituationBHive> ce1, HashSet<SituationBHive> ce2) {
        ce1.retainAll(ce2);
        return ce1;
    }

    public HashSet<SituationBHive> getCondExprNOT(HashSet<SituationBHive> ce) {
        HashSet<SituationBHive> TTPrime = new HashSet<>();

        //we clone everything in current TT
        for (SituationBHive situationBHive : TT) {
            TTPrime.add(situationBHive);
        }
        //for to obtain the complement of getKnowSetBooleanExpr, with respect to current TT (TTPrime)
        TTPrime.removeAll(ce);

        return TTPrime;  //we try to leave TT intact always
    }

    public static Boolean getSetBoolANDOperator(Boolean sbe1, Boolean sbe2) {
        return (sbe1 && sbe2);
    }

    public static Boolean getSetBoolOROperator(Boolean sbe1, Boolean sbe2) {
        return (sbe1 || sbe2);
    }

    public static Boolean getSetBoolNOT(Boolean sbe) {
        return !sbe;
    }

    public static Boolean getSetBoolImplication(Boolean sbe1, Boolean sbe2) {
        return getSetBoolOROperator(getSetBoolNOT(sbe1), sbe2);
    }

    public HashSet<SituationBHive> getKnowsSetBooleanExpr(Integer agentId, String boolExpr) {
        HashSet<SituationBHive> TTPrime = new HashSet<>();
        HashSet<SituationBHive> TTPrimePrime = new HashSet<>();
        boolean truthFlag = true;
        outer:
        for (SituationBHive situationBHive : TT) {
            TTPrime.addAll(situationBHive.getColony(agentId).getColonyMembers());
            inner:
            for (SituationBHive sitBHive : TTPrime) {
                BoolExprParser p = new BoolExprParser(new BoolConditionScanner(new StringReader(boolExpr)), sitBHive);
                try {
                    Boolean result = (Boolean) p.parse().value;
                    if (result.booleanValue() == false) {
                        truthFlag = false;
                        break inner;
                    }
                } catch (Exception ex) {
                    Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if(truthFlag == true){
                TTPrimePrime.add(situationBHive);  //knows bool expr is true in situationBHive bcos all its equivalents satisfy the bool expre part
            }
            truthFlag = true;
            TTPrime.clear();
        }
        
        return TTPrimePrime;
        //Update TT first, because we do not do it in the parser at "Knows" of  "set_booleanf_expr": this was to resolve the grammar ambiguity 
        //that arises when an action immediately follows this "knows" and when an action immediately follows the "Knows" at "condition_expr"
        //That is, after those two "Knows" an action is to be performed, but the two productions, up to the "Knows" are the same, so the parser
        //does not know the actual production.
        //For this reason we opt to do the updateTT inside here for the Knows at "set_boolean_expr", since this is the ground production (we 
        //do not expect further "Knows" after that. We do the same for the "Not Knows" "set_boolean_expr". So with that we break the ambiguity:
        //there is no more action following the "Knows" in Knows set_boolean_expr and Not Knows set_boolean_expr, and so the parser is now clear
        //about the only action point after Knows, namely Knows (*) condition_expr (and Not Knows condition_expr)

        //We need to UpdateTT first, to account for the Knows operator     
//        //get colony of each situation in currentTT
//        updateTT(agentId.intValue() + "");
//
//        ////now I have to call a new parser and scanner to check the boolean expression on all the situations now in TTPrime
//
//        for (SituationBHive situationBHive : TT) {
//            BoolExprParser p = new BoolExprParser(new BoolConditionScanner(new StringReader(boolExpr)), situationBHive);
//            try {
//                Boolean result = (Boolean) p.parse().value;
//                if (result.booleanValue() == true) {
//                    TTPrime.add(situationBHive);  //We are now getting(restricting) the ground truth set, making it potentially narrower than the truth set computed from the top down approach
//                }
//            } catch (Exception ex) {
//                Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//
//        return TTPrime;  //we try to leave TT intact always
    }

    public HashSet<SituationBHive> getNotKnowsSetBooleanExpr(Integer agentId, String boolExpr) {
        HashSet<SituationBHive> TTPrime = new HashSet<>();

        //We need to UpdateTT first, to account for the Knows operator
       // updateTT(agentId.intValue() + "");

        //we clone everything in current TT
        for (SituationBHive situationBHive : TT) {
            TTPrime.add(situationBHive);
        }
        
        TTPrime.removeAll(getKnowsSetBooleanExpr(agentId, boolExpr));
//        //for to obtain the complement of getKnowSetBooleanExpr, with respect to current TT (TTPrime)
//        HashSet<SituationBHive> TTPrimePrime = new HashSet<>();
//
//        for (SituationBHive situationBHive : TT) {
//            BoolExprParser p = new BoolExprParser(new BoolConditionScanner(new StringReader(boolExpr)), situationBHive);
//            try {
//                Boolean result = (Boolean) p.parse().value;
//                if (result.booleanValue() == true) {
//                    TTPrimePrime.add(situationBHive);  //We are now getting(restricting) the ground truth set, making it potentially narrower than the truth set computed from the top down approach
//                }
//            } catch (Exception ex) {
//                Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        TTPrime.removeAll(TTPrimePrime);
        return TTPrime;  //we try to leave TT intact always
    }

    public HashSet<SituationBHive> getKnowsCondExpr(Integer agentId, HashSet<SituationBHive> cee) {
        //we evaluate the knows predecessors of cee, that is all those situations in TT for which their agentId accessibility relations terminate in some situation in cee
        HashSet<SituationBHive> TTPrime = new HashSet<>();
        for (SituationBHive situationBHive : TT) {
            if (cee.containsAll(situationBHive.getColony(agentId).getColonyMembers())) {
                TTPrime.add(situationBHive);
            }
        }
        return TTPrime;
    }

    public HashSet<SituationBHive> getNotKnowsCondExpr(Integer agentId, HashSet<SituationBHive> cee) {
        //just the complement of getKnowsCondExpr
        HashSet<SituationBHive> TTPrime = new HashSet<>();

        //we clone everything in current TT
        for (SituationBHive situationBHive : TT) {
            TTPrime.add(situationBHive);
        }
        //for to obtain the complement of getKnowSetBooleanExpr, with respect to current TT (TTPrime)
        TTPrime.removeAll(getKnowsCondExpr(agentId, cee));

        return TTPrime;  //we try to leave TT intact always
    }

    /*
     * this method is not used!
     * 
     */
    
//    public static ArrayList<String> getDeterminantFormulas() {
//        ArrayList<String> detFormulas = new ArrayList<>();
//        //call the rewrite parser for all the pairs ij
//
//        for (int i = 0; i < 4/*GossipProtocolMultiThread.numberOfAgents*/; i++) {
//            for (int j = 0; j < 4/*GossipProtocolMultiThread.numberOfAgents*/; j++) {
//                if (j == i) {
//                    continue;
//                }
//                try {
//                    String fileName = "D:/DropboxDrive/Dropbox/codes/GossipProtocolsFramework/testcodes/kig_dedicto_spec2.gos";
//                    ProtocolParser p = new ProtocolParser(new ProtocolSpecScanner(new FileReader(fileName)), String.valueOf(i), String.valueOf(j));
//                    // p.action_obj.setCallers(String.valueOf(i), String.valueOf(j));
//                    Object callCondition = p.parse().value;
//                    //System.out.println(callCondition.toString());
//                    detFormulas.add(callCondition.toString());
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//            }
//
//        }
//        for (String string : detFormulas) {
//            System.out.println(string);
//        }
//
//        return detFormulas;
//    }

    public void updateTT(String agentId) {
        HashSet<SituationBHive> TTPrime = new HashSet<>();
        for (SituationBHive situationBHive : TT) {
            TTPrime.addAll(situationBHive.getColony(Integer.parseInt(agentId)).getColonyMembers());
        }

        //tidy things up a bit
        TT.clear();  //no need for this, the previous contents would still feature since they are equivalent to themselves for any agent
        TT.addAll(TTPrime);
        TTPrime.clear();
    }
}
