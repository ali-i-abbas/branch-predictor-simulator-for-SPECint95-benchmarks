import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Scanner;

class HybridBranchPredictor {

    public HybridBranchPredictor(FileWriter statsOutput, int k, int m1, int n, int m2, String traceFile) {
            
        int numberOfPredictions = 0;
        int numberOfMispredictions = 0;

        // set the threshold that determines the prediction
        int threshold = 4;
        // max counter value used for saturated updates
        int maxCounterValue = 7;

        // size of bimodal predictors array is 2 ^ m2
        int bimodalPredictorsSize = 1 << m2;

        // create and initialize array of bimodal predictors to threshold value
        int[] bimodalPredictors = new int[bimodalPredictorsSize];
        Arrays.fill(bimodalPredictors, threshold);

        // mask used to get m2 bits of the address which is 2 ^ m2 - 1
        long maskM2 = (1 << m2) - 1;

        
        long globalBranchHistory = 0;

        // size of gshare predictors array is 2 ^ m1
        int gsharePredictorsSize = 1 << m1;

        // create and initialize array of gsharePredictors to threshold value
        int[] gsharePredictors = new int[gsharePredictorsSize];
        Arrays.fill(gsharePredictors, threshold);

        // mask used to get n bits of the address which is 2 ^ n - 1
        long maskN = (1 << n) - 1;

        // mask used to get m1 - n bits of the address after n bits
        long maskMN = ((1 << (m1 - n)) - 1) << n;

        
        // size of chooser array is 2 ^ k
        int chooserSize = 1 << k;

        // create and initialize chooser array
        int[] chooser = new int[chooserSize];
        Arrays.fill(chooser, 1);

        // mask used to get k bits of the address which is 2 ^ k - 1
        long maskK = (1 << k) - 1;
        
        try (Scanner scanner = new Scanner(new File(traceFile))) {
            String addressStr;
            long address;
            String branchActualOutcome;
            int bimodalIndex;
            String bimodalBranchPredictedOutcome;
            int gshareIndex;
            String gshareBranchPredictedOutcome;
            long branchActualOutcomeHistoryBit;
            int chooserIndex;
            String branchPredictedOutcome;
            boolean isBimodalPredictionCorrect;
            boolean isGsharePredictionCorrect;
			while (scanner.hasNext()) {
                addressStr = scanner.next();
                address = Utility.tryParseHexToLong(addressStr);                
                branchActualOutcome = scanner.next().toLowerCase().replaceAll("[^t,^n]", "");;

                numberOfPredictions++;

                bimodalIndex = (int)((address >> 2) & maskM2);
                
                // predict the branch outcome using the bimodal predictors and threshold
                if (bimodalPredictors[bimodalIndex] < threshold) {
                    bimodalBranchPredictedOutcome = "n";
                } else {
                    bimodalBranchPredictedOutcome = "t";
                }

                // get the index in gshare predictors array by xoring n bits of address with globalBranchHistory and adding the m-n bits of the address after n bits 
                gshareIndex = (int)((((address >> 2) & maskN) ^ globalBranchHistory) | ((address >> 2) & maskMN));
                
                // predict the branch outcome using the gshare predictors and threshold
                if (gsharePredictors[gshareIndex] < threshold) {
                    gshareBranchPredictedOutcome = "n";
                } else {
                    gshareBranchPredictedOutcome = "t";
                }


                chooserIndex = (int)((address >> 2) & maskK);
                
                // use either bimodal or gshare prediction based on chooser value at chooserIndex location in chooser array
                if (chooser[chooserIndex] < 2) {
                    // bimodal predictor is selected

                    branchPredictedOutcome = bimodalBranchPredictedOutcome;

                    // update bimodal branch predictor based on branchActualOutcome
                    switch (branchActualOutcome) {
                        case "t":
                            branchActualOutcomeHistoryBit = 1;
                            if (bimodalPredictors[bimodalIndex] < maxCounterValue) {
                                bimodalPredictors[bimodalIndex]++;
                            }
                            break;
                    
                        case "n":
                            branchActualOutcomeHistoryBit = 0;                        
                            if (bimodalPredictors[bimodalIndex] > 0) {
                                bimodalPredictors[bimodalIndex]--;
                            }
                            break;

                        default:
                            System.out.println("Trace file contains wrong branch outcome token.");
                            System.exit(1);
                            return; 
                    }
                } else {
                    // gshare predictor is selected

                    branchPredictedOutcome = gshareBranchPredictedOutcome;

                    // update branch predictor based on branchActualOutcome
                    switch (branchActualOutcome) {
                        case "t":
                            branchActualOutcomeHistoryBit = 1;
                            if (gsharePredictors[gshareIndex] < maxCounterValue) {
                                gsharePredictors[gshareIndex]++;
                            }
                            break;
                    
                        case "n":   
                            branchActualOutcomeHistoryBit = 0;                     
                            if (gsharePredictors[gshareIndex] > 0) {
                                gsharePredictors[gshareIndex]--;
                            }
                            break;

                        default:
                            System.out.println("Trace file contains wrong branch outcome token.");
                            System.exit(1); 
                            return;
                    }
                }

                // update globalBranchHistory
                globalBranchHistory = (globalBranchHistory >> 1) | (branchActualOutcomeHistoryBit << (n - 1));

                // count number of mispredictions
                if (branchPredictedOutcome.compareTo(branchActualOutcome) != 0) {
                    numberOfMispredictions++;
                }
                
                isBimodalPredictionCorrect = bimodalBranchPredictedOutcome.compareTo(branchActualOutcome) == 0;
                isGsharePredictionCorrect = gshareBranchPredictedOutcome.compareTo(branchActualOutcome) == 0;

                // update the chooser based on gshare and bimodal predictions
                if (isGsharePredictionCorrect && !isBimodalPredictionCorrect) {
                    if (chooser[chooserIndex] < 3) {
                        chooser[chooserIndex]++;
                    }
                } else if (!isGsharePredictionCorrect && isBimodalPredictionCorrect) {
                    if (chooser[chooserIndex] > 0) {
                        chooser[chooserIndex]--;
                    }
                }
                

			}
		} catch (FileNotFoundException e) {
			System.out.println("Trace File <" + traceFile + "> not found.");
			System.exit(1); 
        }
        
        System.out.println("COMMAND");
        System.out.println("./sim hybrid " + k + " " + m1 + " " + n + " " + m2 + " " + traceFile);
        System.out.println("OUTPUT");
        System.out.println("number of predictions:		" + numberOfPredictions);
        System.out.println("number of mispredictions:	" + numberOfMispredictions);
        System.out.println("misprediction rate:		" + String.format("%.2f", (Math.round((float)numberOfMispredictions / numberOfPredictions * 10000) / 100.0)) + "%");
        System.out.println("FINAL CHOOSER CONTENTS");
        for (int i = 0; i < chooser.length; i++) {
            System.out.println(i + "\t" + chooser[i]);
        }
        System.out.println("FINAL GSHARE CONTENTS");
        for (int i = 0; i < gsharePredictors.length; i++) {
            System.out.println(i + "\t" + gsharePredictors[i]);
        }
        System.out.println("FINAL BIMODAL CONTENTS");
        for (int i = 0; i < bimodalPredictors.length; i++) {
            System.out.println(i + "\t" + bimodalPredictors[i]);
        }

    }
    
}