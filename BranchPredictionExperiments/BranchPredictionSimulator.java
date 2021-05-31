import java.io.FileWriter;

class BranchPredictionSimulator {
	public BranchPredictionSimulator()  throws java.io.IOException {
		
		FileWriter statsOutput = new FileWriter("stats.csv");

		String[] traceFiles = new String[] {"gcc_trace.txt", "jpeg_trace.txt", "perl_trace.txt"};
		
		// part 1
		// String predictor = "smith";
		// statsOutput.write("trace,b,MR\n");

		// part 2
		// String predictor = "bimodal";
		// statsOutput.write("trace,m,MR\n");

		// part 3
		String predictor = "gshare";
		statsOutput.write("trace,m,n,MR\n");

		for (String traceFile : traceFiles) {
			switch (predictor) {
				case "smith":
					for (int b = 1; b <= 6; b++) {
						new SmithBranchPredictor(statsOutput, b, traceFile);
					}					
					break;
	
				case "bimodal":
					for (int m2 = 7; m2 <= 12; m2++) {
						new BimodalBranchPredictor(statsOutput, m2, traceFile);
					}
					break;
	
				case "gshare":
					for (int m1 = 7; m1 <= 12; m1++) {
						for (int n = 2; n <= m1; n += 2) {
							new GShareBranchPredictor(statsOutput, m1, n, traceFile);
						}
					}
					break;	
				default:
					System.out.println("Input argument <" + predictor + "> is not valid.");
					System.exit(1);
			}
		}

		
		statsOutput.close();

		
	}

	

	
}