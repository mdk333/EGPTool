package egp;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import egp.concurrent.modeller.Colony;
import egp.concurrent.modeller.SituationBHive;
import egp.concurrent.modeller.TaskComputeNodeSuccessors;
import egp.concurrent.parsers.ProtocolParser;
import egp.concurrent.scanners.ProtocolSpecScanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author byear
 */
public class FrameworkEntry {

    public static int numberOfAgents;
    public static Double numberOfLeaves;
    public static HashMap lengthCats;
    public static HashMap<Integer, ArrayList<Colony>> bWorlds;  //these are the Bee Worlds
    public static HashMap<Integer, ArrayList<Colony>> nextBWorlds;  //these are the Bee Worlds
    private static final int NUMTHREADS = Runtime.getRuntime().availableProcessors();
    private static int determinant;
    private static int layerCount = 0;
    static Formatter formatterEquiv;
    private final String[] specFileNames;
    public static ArrayList<SituationBHive> terminatingSequences = new ArrayList<>();
    public static ArrayList<SituationBHive> deadLockSequences = new ArrayList<>();
    public static ArrayList<SituationBHive> loopingSequences = new ArrayList<>();
    public static String topology = "";
    public static HashSet<String> topologyAgents = new HashSet<>();
    public static ArrayList<String> topologyDBase = new ArrayList<>();
    public static boolean topologyFlag = false;
    public static Integer equivalenceNotion = 2;  //by default it is 2 i.e same history AND same configuration
    static String outputFolderPath = "ProgOutputs/";
    /*
     * We use the following variable to control whether symmetric calls are reduced to one call (only for purpose of printing sample sequences)
     * Or whether they are printed as separate calls (this prints much more calls and makes the programme take longer to finish)
     * The more efficient option is however the default (we identfiy symmetric calls)...Note that the count always distinguishes
     * between symmetric calls.
     */
    public static boolean allowSymmetricCallsInOutput = false;

    public FrameworkEntry(int numAgents, String[] specFiles) {
        numberOfAgents = numAgents;
        numberOfLeaves = 0.0;
        lengthCats = new HashMap<>();
        //initialise the map
        for (int i = 0; i < /*numAgents*(numAgents-1)/2*/ numAgents * (numAgents - 1) / 2 - (2 * numAgents - 4) + 1; i++) {  ////we won't get any length less than (2*numAgents-4), but let us leave it like this for now
            lengthCats.put(new Integer(2 * numAgents - 4 + i), 0.0); //must be 0.0, not just 0
        }

        bWorlds = new HashMap<>();
        nextBWorlds = new HashMap<>();

        determinant = numAgents * (numAgents - 1) / 2;

        // we require that the size of specFiles tally exactly with the number of agents
        //since each entry tells us where to source the specification file for the each given agent
        specFileNames = specFiles;
    }

    public static void main(String[] args) {
        /*
         * take arguments from command line
         * args[0]: (optional) -printsymcalls --- allows outputing of symmetric calls (takes longer) nb: symmetric calls are always counted but this flag has to be set for them to be differentiated in the outputs
         * args[1]: number of agents
         * args[2]: file where spec file sources are listed, corresponding to number of agents
         * args[3]: (optional) output filename ...default is output.* (.sum - summary; .term - terminating seqs; .loop - looping sequences)
         *
         */
        long startTime = System.currentTimeMillis();
        ArrayList<SituationBHive> currentLayerList = new ArrayList<>();
        ArrayList<SituationBHive> nextLayerList = new ArrayList<>();
        boolean loopingFlag = false;

        int argPrintSymCalls = 0, nAgents = 1, specSources = 2, outputFileName = 3;

        if (args.length == 0 || args.length < 2) {
            System.out.println("Error: Program agruments needed!");
            System.out.println("Command args are as follows: \n");

            System.out.println("args[0]: (Optional) -printsymcalls --- Allows outputing of symmetric calls (takes longer) \n\t NB: Symmetric calls are always counted but this flag has to be set for them to be \n\t differentiated in the outputs");
            System.out.println("args[1]: Number of agents");
            System.out.println("args[2]: File where spec file sources are listed, corresponding to number of agents");
            System.out.println("args[3]: (Optional) Output filename ...Default is \"output.*\" \n\t (.sum - summary; .term - terminating seqs; .loop - looping sequences)");
            return;
        }

        if (args[argPrintSymCalls].equalsIgnoreCase("-printsymcalls")) {
            allowSymmetricCallsInOutput = true;
        } else {
            //the -printsymcalls switch wasn't supplied, so we skip it
            nAgents = 0;
            specSources = 1;
            outputFileName = 2;
        }

//        String[] specs = {
//            "testcodes/kig_dedicto_spec1.gos",
//            "testcodes/kig_dedicto_spec1.gos",
//            "testcodes/kig_dedicto_spec1.gos",
//            "testcodes/kig_dedicto_spec1.gos" //"D:/DropboxDrive/Dropbox/codes/GossipProtocolsFramework/testcodes/kig_dedicto_spec1.gos"
//        //    "testcodes/withtopology/kig_dedicto_spec1.gos" //"D:/DropboxDrive/Dropbox/codes/GossipProtocolsFramework/testcodes/kig_dedicto_spec1.gos"
//        }; // number of files have to tally with number of agents
        int numAgents = 0;
        if (!args[nAgents].isEmpty()) {
            numAgents = Integer.parseInt(args[nAgents]);
        } else {
            System.out.println("Error: Specify the number of agents!");

            System.out.println("Command args are as follows: \n");

            System.out.println("args[0]: (Optional) -printsymcalls --- Allows outputing of symmetric calls (takes longer) \n\t NB: Symmetric calls are always counted but this flag has to be set for them to be \n\t differentiated in the outputs");
            System.out.println("args[1]: Number of agents");
            System.out.println("args[2]: File where spec file sources are listed, corresponding to number of agents");
            System.out.println("args[3]: (Optional) Output filename ...Default is \"output.*\" \n\t (.sum - summary; .term - terminating seqs; .loop - looping sequences)");
            return;
        }
        String[] specs = new String[numAgents];

        if (!args[specSources].isEmpty()) {
            try {
                Scanner specFileSources = new Scanner(new File(args[specSources]));
                int count = 0;
                while (specFileSources.hasNextLine() && count < numAgents) {
                    specs[count] = specFileSources.nextLine();
                    count++;
                }
                //check if the number of lines are enough to go round each agent (a line indicates a path for the specs of an agent)
                //do nothing if the number of lines exceed number of agents, just take the much that is needed and ignore the rest
                if (count < numAgents - 1) {
                    //lines did not go round
                    System.out.println("Error: Given file for spec file paths seems to contain insufficient paths for the number of agents!");
                    return;
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(FrameworkEntry.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Error: Please provide a valid  file for the paths to specs sources!");
                System.out.println("Command args are as follows: \n");

                System.out.println("args[0]: (Optional) -printsymcalls --- Allows outputing of symmetric calls (takes longer) \n\t NB: Symmetric calls are always counted but this flag has to be set for them to be \n\t differentiated in the outputs");
                System.out.println("args[1]: Number of agents");
                System.out.println("args[2]: File where spec file sources are listed, corresponding to number of agents");
                System.out.println("args[3]: (Optional) Output filename ...Default is \"output.*\" \n\t (.sum - summary; .term - terminating seqs; .loop - looping sequences); .dlock - deadlocked sequences");
                return;
            }
        } else {
            System.out.println("Error: Provide a file listing the protocol specifications for each of the " + args[1] + "agents");
            System.out.println("Command args are as follows: \n");

            System.out.println("args[0]: (Optional) -printsymcalls --- Allows outputing of symmetric calls (takes longer) \n\t NB: Symmetric calls are always counted but this flag has to be set for them to be \n\t differentiated in the outputs");
            System.out.println("args[1]: Number of agents");
            System.out.println("args[2]: File where spec file sources are listed, corresponding to number of agents");
            System.out.println("args[3]: (Optional) Output filename ...Default is \"output.*\" \n\t (.sum - summary; .term - terminating seqs; .loop - looping sequences)");
            return;
        }

        FrameworkEntry framework = new FrameworkEntry(numAgents, specs); //we start with 3 agents ;)

        String fileNameTerm = "";
        String fileNameTemp = "";
        String fileNameLoop = "";
        String fileNameSum = "";
        String fileNameDDLock = "";
        String fileNameEqClasses = "";

        if (args.length <= 2) { // that is, no third argument specified
            //We then use the defaults
            File file = new File(specs[0]);
            fileNameTemp = file.getName().split("\\.")[0];
            fileNameSum = outputFolderPath + fileNameTemp.concat(".sum");
            fileNameTerm = outputFolderPath + fileNameTemp.concat(".term");
            fileNameLoop = outputFolderPath + fileNameTemp.concat(".loop");
            fileNameEqClasses = outputFolderPath + fileNameTemp.concat(".clas");
            fileNameDDLock = outputFolderPath + fileNameTemp.concat(".dlock");
        } else {
            try {
                File file = new File(args[outputFileName]);
                fileNameTemp = file.getName().split("\\.")[0];
                fileNameSum = outputFolderPath + fileNameTemp.concat(".sum");
                fileNameTerm = outputFolderPath + fileNameTemp.concat(".term");
                fileNameLoop = outputFolderPath + fileNameTemp.concat(".loop");
                fileNameEqClasses = outputFolderPath + fileNameTemp.concat(".clas");
                fileNameDDLock = outputFolderPath + fileNameTemp.concat(".dlock");
            } catch (ArrayIndexOutOfBoundsException ex) {
                //no output file name specified, so we use the defaults
                File file = new File(specs[0]);
                fileNameTemp = file.getName().split("\\.")[0];
                fileNameSum = outputFolderPath + fileNameTemp.concat(".sum");
                fileNameTerm = outputFolderPath + fileNameTemp.concat(".term");
                fileNameLoop = outputFolderPath + fileNameTemp.concat(".loop");
                fileNameEqClasses = outputFolderPath + fileNameTemp.concat(".clas");
                fileNameDDLock = outputFolderPath + fileNameTemp.concat(".dlock");
            }
        }
        //make sure the folder for outputs is created
        (new File(outputFolderPath)).mkdir();
//        FileAttribute<Set<String>> attr;
//        Files.createDirectory("ProgOutputs", attr);
        Formatter formatterSum = null;

        try {
            formatterEquiv = new Formatter(fileNameEqClasses);
            formatterSum = new Formatter(fileNameSum);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FrameworkEntry.class.getName()).log(Level.SEVERE, null, ex);
        }

        //Now we want to preprocess the specs, expand them and store them for each pair of agents
        //Obtain the "REWRITE"/expanded form of i->j call condition
        HashMap<String, String> expandedCallConditions;
        expandedCallConditions = getDeterminantFormulas(specs);

        System.out.println(expandedCallConditions.toString());

        //by now we should have topology information as well, if included in the specification
        System.out.println(topologyDBase.toString());

        if (!topologyDBase.isEmpty()) {
            //indicate for all that topology is in use
            topologyFlag = true;
        }

        //add the root situation to the nextLayerList, to begin with
        SituationBHive rootSituation = new SituationBHive(numberOfAgents);
        //set the colony of the root situation
        //That is we add the root situation to the bworld of all the agents
        for (int i = 0; i < numberOfAgents; i++) {
            //initialise each (agent's) bee world with a new empty list of colonies
            bWorlds.put(i, new ArrayList<Colony>());
            //then say that actually the list of colonies comprises, for starters, of only one colony
            Colony colony = new Colony();
            bWorlds.get(i).add(colony);
            rootSituation.setColony(i, colony);
            //so I know there is only one colony in bworld of this agent, that is why I reference it with index 0
            bWorlds.get(i).get(0).addOneMember(rootSituation);
        }

        //now initialise the nextBWorlds for capturing migrations to the next round
        for (int i = 0; i < numberOfAgents; i++) {
            //initialise each (agent's) bee world with a new empty list of colonies
            nextBWorlds.put(i, new ArrayList<Colony>());
        }

        nextLayerList.add(rootSituation);

        //Each future -(that is the result of executing a task (i.e. the list of all the successor of a given node))-
        //is of type ArrayList of situation. But then since we expect a list from each of the many threads, then we will
        //keep an arraylist of these futures.
        ArrayList<Future<ArrayList<SituationBHive>>> nodeNextLayerLists = new ArrayList<>();

        while (!nextLayerList.isEmpty()) {
            currentLayerList.clear();
            currentLayerList.addAll(nextLayerList);
            nextLayerList.clear();
            //create a new executor because you shut down the previous one
//            ExecutorService executor = Executors.newFixedThreadPool(NUMTHREADS);
            ExecutorService executor = Executors.newFixedThreadPool(NUMTHREADS);
//            ExecutorService executor = Executors.newSingleThreadExecutor();
//            ExecutorService executor = Executors.newFixedThreadPool(currentLayerList.size());

            for (SituationBHive situation : currentLayerList) {
                Callable<ArrayList<SituationBHive>> nodeSuccessorGetter = new TaskComputeNodeSuccessors(situation, expandedCallConditions);
                Future<ArrayList<SituationBHive>> nodeNextLayer = executor.submit(nodeSuccessorGetter);
                nodeNextLayerLists.add(nodeNextLayer);

                /*
                 * Essentially this for loop does not finish execution until all the spawned threads have returned with their
                 * 'Future' values.
                 */
            }

            //now let's aggregate the lists obtained from each of the thread to get the global nextLayerList
            for (Future<ArrayList<SituationBHive>> aNodeNextLayer : nodeNextLayerLists) {
                try {
                    nextLayerList.addAll(aNodeNextLayer.get(21L, TimeUnit.DAYS));
                } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                    Logger.getLogger(FrameworkEntry.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            //now clear nodeNextLayerLists --- This led to errors!!!!
            nodeNextLayerLists.clear();

            executor.shutdown();
            try {
                executor.awaitTermination(21L, TimeUnit.DAYS);
            } catch (InterruptedException ex) {
                Logger.getLogger(FrameworkEntry.class.getName()).log(Level.SEVERE, null, ex);
            }

            ArrayList<Colony> colonyList;
            // try (Formatter formatterEquiv = new Formatter("equivclasses.dat")) {

            FrameworkEntry.formatterEquiv.format("Layer %d: size=%d%n", layerCount, framework.getLayerSize(currentLayerList));
            FrameworkEntry.formatterEquiv.format("-----------------------%n");
            int rowChecker = 1;
            long size = 0;
            for (int i = 0; i < numberOfAgents; i++) {
                FrameworkEntry.formatterEquiv.format("%d:\t", i);
                colonyList = bWorlds.get(i);
                for (Colony colony : colonyList) {

                    FrameworkEntry.formatterEquiv.format("%d,\t", framework.getColonySize(colony.getColonyMembers()));
                    if (rowChecker > 700) {  // there are to be 700 columns in a row
                        FrameworkEntry.formatterEquiv.format("%n");
                        rowChecker = 1;
                    } else {
                        rowChecker++;
                    }
                }
                FrameworkEntry.formatterEquiv.format("%n");
            }
            FrameworkEntry.formatterEquiv.format("-----------------------");
            FrameworkEntry.formatterEquiv.format("%n%n");
//            } catch (FileNotFoundException ex) {
//                Logger.getLogger(FrameworkEntry.class.getName()).log(Level.SEVERE, null, ex);
//            }
            //Let us to tend to the Bees Worlds
            bWorlds.clear(); // I will later try clearing bWorlds first to see if it still remains correct (so I can free up the associated memory)
            bWorlds.putAll(nextBWorlds);
            nextBWorlds.clear();
            //re-initialize nextBWorlds
            for (int i = 0; i < numberOfAgents; i++) {
                //initialise each (agent's) bee world with a new empty list of colonies
                nextBWorlds.put(i, new ArrayList<Colony>());
            }
//            if (layerCount == determinant - 1) {
//                //means all the nodes in the last layer have just been returned
//                //so we do go round again checking them, we simply take the stats
//                //accordingly and quit
//                numberOfLeaves += nextLayerList.size();
//                lengthCats.put(determinant, nextLayerList.size());
//                break;
//            }
            layerCount++;

            if (layerCount > determinant && !nextLayerList.isEmpty()) {
                //then we are in for a non-terminating possibly looping protocol, so 
                //pick one of the sequences that have not yet terminated as a possibly looping sequence,
                // and finish!

                loopingFlag = true;
                {
                    formatterSum.format("The given protocol is NON-TERMINATING, with LOOPING. %n");
                    formatterSum.format("Number of TERMINATING sequences for %d Agent is %f Sequences.%n", numberOfAgents, numberOfLeaves);
                    Iterator it = lengthCats.entrySet().iterator();//TaskComputeNodeSuccessors.lengthCats.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry lenCat = (Map.Entry) it.next();
                        formatterSum.format("Length %s = %s Sequences%n", lenCat.getKey().toString(), Double.parseDouble(lenCat.getValue().toString()));
                    }
                    //System.out.println("The maximum integer is " + Integer.MAX_VALUE);
                    formatterSum.format("Number of LOOPING sequences for %d Agent is %d Sequences.%n", numberOfAgents, nextLayerList.size());
                    formatterSum.format("The following is one of the looping sequences.%n");
                    formatterSum.format("%s%n", nextLayerList.get(0).getHistory());
                    formatterSum.format("%s%n", printEquivNotion());

                    long duration = System.currentTimeMillis() - startTime;
                    formatterSum.format("Duration of the program on my desktop is %d:%02d:%02d.%03d", duration / (3600 * 1000), ((duration / 1000) % 3600) / 60, (duration / 1000) % 60, duration % 1000);
                    formatterSum.format("%n%n--------------------------%n%n");
                    //formatter.close(); // do not close yet, it might be deadlocked as well
                }
                /*
                 * IF NEEDED WE CAN CREATE A FILE WITH ALL THE LOOPING SEQUENCES! 
                 * WE JUST NEED TO DUMP THE CONTENTS OF FrameworkEntry.loopingSequences.
                 */

                String hist;
                try (Formatter formatter = new Formatter(fileNameLoop)) { //
                    for (SituationBHive lpSituation : nextLayerList) {  //the nextLAyer list will contain situations with looping sequences
                        hist = lpSituation.getHistory();
                        formatter.format("%s%n", hist);
                    }
                    formatter.close();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(FrameworkEntry.class.getName()).log(Level.SEVERE, null, ex);
                }

                /*
                 * But the protocol might have deadlocked sequences as well,
                 * so we print these....
                 */
                String hist2;
                try (Formatter formatter = new Formatter(fileNameDDLock)) { //
                    for (SituationBHive ddSituation : deadLockSequences) {
                        hist2 = ddSituation.getHistory();
                        formatter.format("%s%n", hist2);
                    }
                    formatter.close();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(FrameworkEntry.class.getName()).log(Level.SEVERE, null, ex);
                }

                /*
                 * And finally there may also be some terminating sequences,
                 * So we print them as well...
                 */
                String hist3;
                try (Formatter formatter = new Formatter(fileNameTerm)) { //
                    for (SituationBHive termSituation : terminatingSequences) {
                        hist3 = termSituation.getHistory();
                        formatter.format("%s%n", hist3);
                    }
                    formatter.close();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(FrameworkEntry.class.getName()).log(Level.SEVERE, null, ex);
                }

                System.out.printf("The given protocol is NON-TERMINATING with LOOPING. %n");
                System.out.printf("Number of TERMINATING sequences for %d Agent is %f Sequences.%n", numberOfAgents, numberOfLeaves);
                Iterator it = lengthCats.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry lenCat = (Map.Entry) it.next();
                    System.out.println("Length " + lenCat.getKey() + " = " + lenCat.getValue() + " Sequences.");
                }

                System.out.printf("Number of LOOPING sequences for %d Agent is %d Sequences.%n", numberOfAgents, nextLayerList.size());
                System.out.printf("The following is one of the looping sequences.%n");
                System.out.printf("%s%n", nextLayerList.get(0).getHistory());
                System.out.printf("%s%n", printEquivNotion());
                // System.out.println("maximum double is"  + Double.MAX_VALUE);
                //framework.formatterEquiv.close();
                break;
            }
        }

        if (!FrameworkEntry.deadLockSequences.isEmpty()) {
            //Our protocol terminates with deadlocks!
            {
                formatterSum.format("The given protocol is NON-TERMINATING, with DEADLOCK. %n");
                formatterSum.format("Number of TERMINATING sequences for %d Agent is %f Sequences.%n", numberOfAgents, numberOfLeaves);
                Iterator it = lengthCats.entrySet().iterator();//TaskComputeNodeSuccessors.lengthCats.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry lenCat = (Map.Entry) it.next();
                    formatterSum.format("Length %s = %s Sequences%n", lenCat.getKey().toString(), Double.parseDouble(lenCat.getValue().toString()));
                }
                //System.out.println("The maximum integer is " + Integer.MAX_VALUE);
                formatterSum.format("Number of DEADLOCKED sequences for %d Agent is %d Sequences.%n", numberOfAgents, FrameworkEntry.deadLockSequences.size());
                formatterSum.format("The following is one of the deadlocked sequences.%n");
                formatterSum.format("%s%n", FrameworkEntry.deadLockSequences.get(0).getHistory());
                formatterSum.format("%s%n", printEquivNotion());

                long duration = System.currentTimeMillis() - startTime;
                formatterSum.format("Duration of the program on my desktop is %d:%02d:%02d.%03d", duration / (3600 * 1000), ((duration / 1000) % 3600) / 60, (duration / 1000) % 60, duration % 1000);
                formatterSum.format("%n%n--------------------------%n%n");

                //NOW WE CAN CLOSE THE FILE!
                //The idea is that if the protocol is both looping and deadlocking, we'll capture all the summary in one file
                //If the protocol is either looping or deadlocking we will capture any of the scenarios in one file as well
                //formatterSum.close();
            }
            /*
             * IF NEEDED WE CAN CREATE A FILE WITH ALL THE DEADLOCK SEQUENCES! 
             * WE JUST NEED TO DUMP THE CONTENTS OF FrameworkEntry.loopingSequences.
             */
            String hist = "";
            try (Formatter formatter = new Formatter(fileNameDDLock)) { //
                for (SituationBHive ddSituation : deadLockSequences) {
                    hist = ddSituation.getHistory();
                    formatter.format("%s%n", hist);
                }
                formatter.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(FrameworkEntry.class.getName()).log(Level.SEVERE, null, ex);
            }

            /*
             * But the protocol might have looping sequences as well,
             * so we print these....
             */
            String hist2;
            try (Formatter formatter = new Formatter(fileNameLoop)) { //
                for (SituationBHive lpSituation : nextLayerList) {  //the nextLAyer list will contain situations with looping sequences
                    hist2 = lpSituation.getHistory();
                    formatter.format("%s%n", hist2);
                }
                formatter.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(FrameworkEntry.class.getName()).log(Level.SEVERE, null, ex);
            }

            /*
             * And finally there may also be some terminating sequences,
             * So we print them as well...
             */
            String hist3;
            try (Formatter formatter = new Formatter(fileNameTerm)) { //
                for (SituationBHive termSituation : terminatingSequences) {
                    hist3 = termSituation.getHistory();
                    formatter.format("%s%n", hist3);
                }
                formatter.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(FrameworkEntry.class.getName()).log(Level.SEVERE, null, ex);
            }

            System.out.printf("The given protocol is NON-TERMINATING with DEADLOCK. %n");
            System.out.printf("Number of TERMINATING sequences for %d Agent is %f Sequences.%n", numberOfAgents, numberOfLeaves);
            Iterator it = lengthCats.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry lenCat = (Map.Entry) it.next();
                System.out.println("Length " + lenCat.getKey() + " = " + lenCat.getValue() + " Sequences.");
            }

            System.out.printf("Number of DEADLOCK sequences for %d Agent is %d Sequences.%n", numberOfAgents, FrameworkEntry.deadLockSequences.size());
            System.out.printf("The following is one of the deadlocking sequences.%n");
            SituationBHive exampleDeadlockedSequence = FrameworkEntry.deadLockSequences.get(0);
            System.out.printf("%s%n", exampleDeadlockedSequence.getHistory());
            System.out.printf("%s%n", printEquivNotion());
            /*
             Now we print all the histories that are equivalent to this example sequence, for each agent
             */
//            System.out.println("********************************************");
//            for (int i = 0; i < FrameworkEntry.numberOfAgents; i++) {
//                ArrayList<SituationBHive> colonyMembers = exampleDeadlockedSequence.getColony(i).getColonyMembers();
//                System.out.println("**************************************************");
//                System.out.println("The equivalence class for agent " + i + " is as follows:");
//                System.out.println("**************************************************");
//                for (SituationBHive ddSituation : colonyMembers) {
//                    System.out.printf("%s%n", ddSituation.getHistory());
//                }
//            }

        } else {
            if (loopingFlag == true) {
                //The protocol was found to be looping, so just end here
                formatterSum.close();
                return;
            } else {
                //The protocol is terminating
                double totalCalls = 0.0;
                double totalSequences = 0.0;
                double expectExecLength = 0.0;
                try (Formatter formatter = new Formatter(fileNameSum)) { //
                    formatter.format("The given protocol is TERMINATING. %n");
                    formatter.format("Number of sequences for %d Agent is %f Sequences.%n", numberOfAgents, numberOfLeaves);

                    totalSequences = numberOfLeaves;

                    Iterator it = lengthCats.entrySet().iterator();//TaskComputeNodeSuccessors.lengthCats.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry lenCat = (Map.Entry) it.next();
                        formatter.format("Length %s = %s Sequences%n", lenCat.getKey().toString(), Double.parseDouble(lenCat.getValue().toString()));

                        totalCalls += Double.parseDouble(lenCat.getKey().toString()) * Double.parseDouble(lenCat.getValue().toString());

                    }
                    expectExecLength = totalCalls / totalSequences;

                    formatter.format("Expected execution length of protocol is: %.5f%n", expectExecLength);

                    formatter.format("%s%n", printEquivNotion());
                    long duration = System.currentTimeMillis() - startTime;
                    formatter.format("Duration of the program on my desktop is %d:%02d:%02d.%03d", duration / (3600 * 1000), ((duration / 1000) % 3600) / 60, (duration / 1000) % 60, duration % 1000);
                    formatter.close();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(FrameworkEntry.class.getName()).log(Level.SEVERE, null, ex);
                }
                String hist;
                try (Formatter formatter = new Formatter(fileNameTerm)) { //
                    for (SituationBHive termSituation : terminatingSequences) {
                        hist = termSituation.getHistory();
                        formatter.format("%s%n", hist);
                    }
                    formatter.close();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(FrameworkEntry.class.getName()).log(Level.SEVERE, null, ex);
                }

                /*
                 * Print (empty) file for deadlocked sequences
                 */
                String hist2 = "";
                try (Formatter formatter = new Formatter(fileNameDDLock)) { //
                    for (SituationBHive ddSituation : deadLockSequences) {
                        hist2 = ddSituation.getHistory();
                        formatter.format("%s%n", hist2);
                    }
                    formatter.close();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(FrameworkEntry.class.getName()).log(Level.SEVERE, null, ex);
                }

                /*
                 * print (empty) for looping sequences
                 */
                String hist3;
                try (Formatter formatter = new Formatter(fileNameLoop)) { //
                    for (SituationBHive lpSituation : nextLayerList) {  //the nextLAyer list will contain situations with looping sequences
                        hist3 = lpSituation.getHistory();
                        formatter.format("%s%n", hist3);
                    }
                    formatter.close();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(FrameworkEntry.class.getName()).log(Level.SEVERE, null, ex);
                }

                System.out.printf("The given protocol is TERMINATING. %n");
                System.out.println("Number of sequences for " + numberOfAgents + " Agents is " + numberOfLeaves + " Sequences.");
                Iterator it = lengthCats.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry lenCat = (Map.Entry) it.next();
                    System.out.println("Length " + lenCat.getKey() + " = " + lenCat.getValue() + " Sequences.");
                }

                System.out.printf("Expected execution length of protocol is: %.5f%n", expectExecLength);
                System.out.printf("%s%n", printEquivNotion());
            }
            // System.out.println("maximum double is"  + Double.MAX_VALUE);
            FrameworkEntry.formatterEquiv.close();
        }
        formatterSum.close();

    }
    /*
     * Because the threads would access this method, to maintain consistency of the 
     * leaves counting, I synchronise this method.
     */

    public static synchronized void takeStats(SituationBHive situation) {
        Integer historyLength = situation.getHistory().split(";").length;
        numberOfLeaves += situation.getReplicationFactor();  // we take into account the replicates of this situation node
        lengthCats.put(historyLength, (Double) lengthCats.get(historyLength) + situation.getReplicationFactor());
        FrameworkEntry.terminatingSequences.add(situation);
    }

    public static synchronized void updateDeadLockedList(SituationBHive situation) {
        FrameworkEntry.deadLockSequences.add(situation);
    }

    public int[] getInitConfiguration() {
        int[] configuration = new int[(int) numberOfAgents];
        int baseSecret = 1;
        for (int i = 0; i < numberOfAgents; i++) {
            configuration[i] = baseSecret;
            baseSecret *= 2;
        }
        return configuration;
    }

    public int getNumberOfAgents() {
        return FrameworkEntry.numberOfAgents;
    }

    public String[] getSpecFileNames() {
        return this.specFileNames;
    }

    public static synchronized void updateNextBWorld(int agent, Colony colony) {
        nextBWorlds.get(agent).add(colony);
    }

    public long getLayerSize(ArrayList<SituationBHive> layer) {
        long size = 0;
        Iterator itLayer = layer.iterator();
        while (itLayer.hasNext()) {
            size += ((SituationBHive) itLayer.next()).getReplicationFactor();
        }
        return size;
    }

    public long getColonySize(ArrayList<SituationBHive> colony) {
        long size = 0;
        Iterator itColony = colony.iterator();
        while (itColony.hasNext()) {
            size += ((SituationBHive) itColony.next()).getReplicationFactor();
        }
        return size;
    }

    public static HashMap<String, String> getDeterminantFormulas(String[] specs) {
        //ArrayList<String> detFormulas = new ArrayList<>();
        //call the rewrite parser for all the pairs ij
        HashMap<String, String> ijCallCondition = new HashMap<>();

        for (int i = 0; i < numberOfAgents; i++) {
            for (int j = 0; j < numberOfAgents; j++) {
                if (j == i) {
                    continue;
                }
                try {
                    //String fileName = "D:/DropboxDrive/Dropbox/codes/GossipProtocolsFramework/testcodes/kig_dedicto_spec2.gos";
                    ProtocolParser p = new ProtocolParser(new ProtocolSpecScanner(new FileReader(specs[i])), String.valueOf(i), String.valueOf(j));
                    // p.action_obj.setCallers(String.valueOf(i), String.valueOf(j));
                    Object callCondition = p.parse().value;
                    //System.out.println(callCondition.toString());
                    //detFormulas.add(callCondition.toString());
                    ijCallCondition.put(i + "" + j, callCondition.toString());

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }
        //If we have any topology declarations, then process it...
        if (!topology.isEmpty()) {
            String[] topAgents = topologyAgents.toArray(new String[0]);
            if (topAgents.length == numberOfAgents) {
                Arrays.sort(topAgents);
                for (int i = 0; i < topAgents.length && i < numberOfAgents; i++) {
                    topology = topology.replaceAll(topAgents[i], i + "");
                }
                String[] topologyStates = topology.split(":");
                topologyDBase.addAll(Arrays.asList(topologyStates));
            } else {
                System.err.println("Your topology specification is incomplete or unsound :-(");
                System.err.println("Note that all agents must have at least one neighbour!");
                System.err.println("Program is now (prematurely) ended...");
                System.exit(0);
            }
        }
        // return detFormulas;
        return ijCallCondition;
    }

    public static String printEquivNotion() {
        switch (equivalenceNotion) {
            case 0: {
                return "Equivalence Notion is 0: Same Configuration.";
            }
            case 1: {
                return "Equivalence Notion is 1: Same History";
            }
            case 2: {
                return "Equivalence Notion is 2: Same Configuration AND Same History";
            }
            case 3: {
                return "Equivalence Notion is 3: Same Configuration OR Same History";
            }
            default: {
                return "By default, Equivalence Notion is 2: Same Configuration AND Same History";
            }
        }
    }
}
