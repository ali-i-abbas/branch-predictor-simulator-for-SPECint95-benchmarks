import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

class GShareBranchPredictor {

    public GShareBranchPredictor(int m1, int n, String traceFile) {
            
        int numberOfPredictions = 0;
        int numberOfMispredictions = 0;

        long globalBranchHistory = 0;

        // set the threshold that determines the prediction
        int threshold = 4;
        // max counter value used for saturated updates
        int maxCounterValue = 7;

        // size of predictors array is 2 ^ m1
        int predictorsSize = 1 << m1;

        // create and initialize array of predictors to threshold value
        int[] predictors = new int[predictorsSize];
        Arrays.fill(predictors, threshold);

        // mask used to get n bits of the address which is 2 ^ n - 1
        long maskN = (1 << n) - 1;

        // mask used to get m1 - n bits of the address after n bits
        long maskMN = ((1 << (m1 - n)) - 1) << n;

        
        try (Scanner scanner = new Scanner(new File(traceFile))) {
            String addressStr;
            String branchActualOutcome;
            String branchPredictedOutcome;
            long branchActualOutcomeHistoryBit;
			while (scanner.hasNext()) {
                addressStr = scanner.next();
                long address = Utility.tryParseHexToLong(addressStr);                
                branchActualOutcome = scanner.next().toLowerCase().replaceAll("[^t,^n]", "");;

                numberOfPredictions++;

                // get the index in predictors array by xoring n bits of address with globalBranchHistory and adding the m-n bits of the address after n bits 
                int index = (int)((((address >> 2) & maskN) ^ globalBranchHistory) | ((address >> 2) & maskMN));
                
                // predict the branch outcome using the counter and threshold
                if (predictors[index] < threshold) {
                    branchPredictedOutcome = "n";
                } else {
                    branchPredictedOutcome = "t";
                }

                // count number of mispredictions
                if (branchPredictedOutcome.compareTo(branchActualOutcome) != 0) {
                    numberOfMispredictions++;
                }
                

                // update branch predictor based on branchActualOutcome
				switch (branchActualOutcome) {
                    case "t":
                        branchActualOutcomeHistoryBit = 1;
                        if (predictors[index] < maxCounterValue) {
                            predictors[index]++;
                        }
                        break;
                
                    case "n":   
                        branchActualOutcomeHistoryBit = 0;                     
                        if (predictors[index] > 0) {
                            predictors[index]--;
                        }
                        break;

                    default:
                        System.out.println("Trace file contains wrong branch outcome token.");
                        System.exit(1); 
                        return;
                }

                // update globalBranchHistory
                globalBranchHistory = (globalBranchHistory >> 1) | (branchActualOutcomeHistoryBit << (n - 1));

			}
		} catch (FileNotFoundException e) {
			System.out.println("Trace File <" + traceFile + "> not found.");
			System.exit(1); 
        }
        
        System.out.println("COMMAND");
        System.out.println("./sim gshare " + m1 + " " + n + " " + traceFile);
        System.out.println("OUTPUT");
        System.out.println("number of predictions:		" + numberOfPredictions);
        System.out.println("number of mispredictions:	" + numberOfMispredictions);
        System.out.println("misprediction rate:		" + String.format("%.2f", (Math.round((float)numberOfMispredictions / numberOfPredictions * 10000) / 100.0)) + "%");
        System.out.println("FINAL GSHARE CONTENTS");
        for (int i = 0; i < predictors.length; i++) {
            System.out.println(i + "\t" + predictors[i]);
        }

    }
    
}