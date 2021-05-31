import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

class SmithBranchPredictor {

    public SmithBranchPredictor(FileWriter statsOutput, int b, String traceFile) throws IOException {

        int numberOfPredictions = 0;
        int numberOfMispredictions = 0;

        // initialize the counter to be 2 ^ (b - 1). ex. if b = 4, initialize to 8
        // this is also the threshold that determines the prediction
        int threshold = 1 << (b - 1);
        int counter = threshold;

        // counter max value is 2 ^ b - 1
        int maxCounterValue = (1 << b) - 1;

        
        try (Scanner scanner = new Scanner(new File(traceFile))) {
            String addressStr;
            String branchActualOutcome;
            String branchPredictedOutcome;
			while (scanner.hasNext()) {
                addressStr = scanner.next();

                branchActualOutcome = scanner.next().toLowerCase().replaceAll("[^t,^n]", "");;

                numberOfPredictions++;
                
                // predict the branch outcome using the counter and threshold
                if (counter < threshold) {
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
                        if (counter < maxCounterValue) {
                            counter++;
                        }
                        break;
                
                    case "n":                        
                        if (counter > 0) {
                            counter--;
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
        
        statsOutput.write(traceFile + "," + b + "," + String.format("%.2f", (Math.round((float)numberOfMispredictions / numberOfPredictions * 10000) / 100.0)) + "\n");
        
    }
    
}