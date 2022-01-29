package Assignment1;

import java.util.Random;

import general.Counter;
import general.SystemState;
import general.annotations.AutoCounter;
import general.annotations.AutoMeasure;
import general.annotations.Initialize;
import general.annotations.StopCriterium;

public class LitterCollectionState extends SystemState<LitterCollectionState>
{
	// Parameters
	// State variables
	int sensor;
	int maxCapacity;
	double lambda;
	int timeDelay;
	int costContainer;
	double costOutsideBag;

	boolean runOver;

	private final Random random;

	// Counter variables
	@AutoCounter("Outside Bags")
	private Counter outsideBags;

	@AutoCounter("Inside Bags")
	private Counter insideBags;

	@AutoCounter("Cost")
	private Counter cost;

	@AutoCounter("Time")
	private Counter time;

	public LitterCollectionState(double timeHorizon, long seed,
								 int sensor,
								 int maxCapacity,
								 double lambda,
								 int timeDelay,
								 int costContainer,
								 double costOutsideBag) {
		super(timeHorizon, seed);
		this.random = new Random(seed);
		this.sensor = sensor;
		this.maxCapacity = maxCapacity;
		this.lambda = lambda;
		this.timeDelay = timeDelay;
		this.costContainer = costContainer;
		this.costOutsideBag = costOutsideBag;

		reset();
	}

	@Initialize
	public void initReplication() {
		double nextArrivalTime = UtilsLitterCollection.nextInterArrivalTime(this.random, this.lambda);
		addEvent(nextArrivalTime, this::doArrival);
	}

	@StopCriterium
	public boolean veryDangerousCheck() {
		return this.runOver;
	}

	public void doArrival(double eventTime) {

		cost.incrementBy(outsideBags.getValue() * (eventTime - getCurrentTime()) * this.costOutsideBag);

		if (this.insideBags.getValue() == this.maxCapacity) {
			outsideBags.increment();
		} else {
			insideBags.increment();
		}

		if (insideBags.getValue() == this.sensor) {
			addEvent(eventTime + this.timeDelay, this::doClean);
		}

		double nextInterArrivalTime = UtilsLitterCollection.nextInterArrivalTime(random, lambda);
		double nextArrivalTime = eventTime + nextInterArrivalTime;
		addEvent(nextArrivalTime, this::doArrival);
	}

	public void doClean(double eventTime) {
		cost.incrementBy(outsideBags.getValue() * (eventTime - getCurrentTime()) * this.costOutsideBag);
		cost.incrementBy(this.costContainer);
		time.incrementBy(eventTime);
		runOver = true;
	}

	@AutoMeasure("Time till clean up (days)")
	public Double getTime() {
		return this.time.getValue() / 24;
	}

	@AutoMeasure("Cost")
	public Double getCost() {
		return this.cost.getValue();
	}

	@AutoMeasure("Yearly Cost")
	public Double getYearlyCost() {
		return this.getCost() / this.time.getValue() * 24 * 365;
	}

	@Override
	public void reset() {
		runOver = false;
	}
}