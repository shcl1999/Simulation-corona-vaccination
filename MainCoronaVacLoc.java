package Assignment1;

import general.Replication;
import general.Simulation;
import general.automagic.AutoReplication;

public class MainCoronaVacLoc {

	public static void main(String[] args) {
		// parameters

		int mu = 12;
		int nChairs = 25;
		double rejectionHour = 9;

		double chairSittingTime = 0.25;

		double timeHorizon = 100;
		long n = 1000;
		long seed = 0;

		for (int i = 1; i <= 10; i++) {
			int nBooths = i;
			int maxArrivals = nBooths * 10 * 9;
			int lambda = nBooths * 10;
			CoronaVacLocState state = new CoronaVacLocState(timeHorizon, seed, nBooths, nChairs, lambda, mu, chairSittingTime, maxArrivals, rejectionHour);
			Replication<CoronaVacLocState> replication = new AutoReplication<CoronaVacLocState>(state);
			Simulation<CoronaVacLocState> simulation = new Simulation<>(replication);
			simulation.run(n);
			System.out.println("nBooths: " + nBooths);
			simulation.printEstimates();
			System.out.println("--------------------------------------------------------------------------------------------------------");
		}
	}
}
