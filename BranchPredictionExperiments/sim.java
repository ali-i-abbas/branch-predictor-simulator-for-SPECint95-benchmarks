import java.io.IOException;

class sim {
	public static void main(String[] args) {
		try {
			new BranchPredictionSimulator();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
