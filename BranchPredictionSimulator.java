import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

class BranchPredictionSimulator {
	public BranchPredictionSimulator(String[] args) {
		
		int m1, m2, k, n;
		String traceFile;
		
		String predictor = args[0].trim().toLowerCase();

		switch (predictor) {
			case "smith":			
				int b = Utility.tryParseInt(args[1]);
				traceFile = args[2];
				new SmithBranchPredictor(b, traceFile);
				break;

			case "bimodal":
				m2 = Utility.tryParseInt(args[1]);
				traceFile = args[2];
				new BimodalBranchPredictor(m2, traceFile);
				break;

			case "gshare":
				m1 = Utility.tryParseInt(args[1]);
				n = Utility.tryParseInt(args[2]);
				traceFile = args[3];
				new GShareBranchPredictor(m1, n, traceFile);
				break;

			case "hybrid":
				k = Utility.tryParseInt(args[1]);
				m1 = Utility.tryParseInt(args[2]);
				n = Utility.tryParseInt(args[3]);
				m2 = Utility.tryParseInt(args[4]);
				traceFile = args[5];
				new HybridBranchPredictor(k, m1, n, m2, traceFile);
				break;

			default:
				System.out.println("Input argument <" + predictor + "> is not valid.");
				System.exit(1);
		}

		
	}

	

	
}