import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

class BimodalBranchPredictor {
    public BimodalBranchPredictor(int m2, String traceFile) {
        
        int numberOfPredictions = 0;
        int numberOfMispredictions = 0;

        // set the threshold that determines the prediction
        int threshold = 4;
        // max counter value used for saturated updates
        int maxCounterValue = 7;

        // size of predictors array is 2 ^ m2
        int predictorsSize = 1 << m2;

        // create and initialize array of predictors to threshold value
        int[] predictors = new int[predictorsSize];
        Arrays.fill(predictors, threshold);

        // mask used to get m2 bits of the address which is 2 ^ m2 - 1
        long mask = (1 << m2) - 1;

        
        try (Scanner scanner = new Scanner(new File(traceFile))) {
            String addressStr;
            String branchActualOutcome;
            String branchPredictedOutcome;
			while (scanner.hasNext()) {
                addressStr = scanner.next();
                long address = Utility.tryParseHexToLong(addressStr);                
                branchActualOutcome = scanner.next().toLowerCase().replaceAll("[^t,^n]", "");;

                numberOfPredictions++;

                int index = (int)((address >> 2) & mask);
                
                // predict the branch outcome using the bimodal predictors and threshold
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
                        if (predictors[index] < maxCounterValue) {
                            predictors[index]++;
                        }
                        break;
                
                    case "n":                        
                        if (predictors[index] > 0) {
                            predictors[index]--;
                        }
                        break;

                    default:
                        System.out.println("Trace file contains wrong branch outcome token.");
			            System.exit(1); 
                }

			}
		} catch (FileNotFoundException e) {
			System.out.println("Trace File <" + traceFile + "> not found.");
			System.exit(1); 
        }
        
        System.out.println("COMMAND");
        System.out.println("./sim bimodal " + m2 + " " + traceFile);
        System.out.println("OUTPUT");
        System.out.println("number of predictions:		" + numberOfPredictions);
        System.out.println("number of mispredictions:	" + numberOfMispredictions);
        System.out.println("misprediction rate:		" + String.format("%.2f", (Math.round((float)numberOfMispredictions / numberOfPredictions * 10000) / 100.0)) + "%");
        System.out.println("FINAL BIMODAL CONTENTS");
        for (int i = 0; i < predictors.length; i++) {
            System.out.println(i + "\t" + predictors[i]);
        }

    }
}