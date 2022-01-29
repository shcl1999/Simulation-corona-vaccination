package Assignment1;

import general.Replication;
import general.Simulation;
import general.automagic.AutoReplication;

public class MainLitterCollection {

	public static void main(String[] args) {
		// parameters
		// time unit is hour
		int maxCapacity = 1000;
		double lambda = 2d;
		int timeDelay = 2 * 24;
		int costContainer = 100;
		double costOutsideBag = 10.0/24.0;

		double timeHorizon = Double.POSITIVE_INFINITY;
		long n = 1000;
		long seed = 0;
		
		for (int i = 850; i <= 950; i= i + 5) {
			int sensorLevel = i;
			LitterCollectionState state = new LitterCollectionState(timeHorizon, seed, sensorLevel, maxCapacity, lambda, timeDelay, costContainer, costOutsideBag);
			Replication<LitterCollectionState> replication = new AutoReplication<LitterCollectionState>(state);

			Simulation<LitterCollectionState> simulation = new Simulation<>(replication);
			simulation.run(n);
			System.out.println("SensorLevel: " + sensorLevel);
			simulation.printEstimates();
			System.out.println("--------------------------------------------------------------------------------------------------------");
		}
	}
}
