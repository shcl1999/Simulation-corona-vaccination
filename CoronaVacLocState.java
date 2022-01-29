package Assignment1;

import general.Counter;
import general.SystemState;
import general.annotations.AutoCounter;
import general.annotations.AutoMeasure;
import general.annotations.Initialize;
import general.annotations.StopCriterium;

import java.util.Random;

public class CoronaVacLocState extends SystemState<CoronaVacLocState>
{
	// Parameters
	// State variables

	private final int maxArrivals;
	private final int lambda;
	private final double rejectionHour;
	private boolean lastPerson;

	private final int nServers;
	private int nServersBusy;
	private int nServerQueue;

	private final int mu;

	private final int nChairs;
	private int nChairsBusy;
	private int nChairQueue;

	private final double chairSittingTime;

	private double lastPersonLeftTime;
	private double lastPersonArrivedTime;

	private final Random random;

	// Counter variables
	@AutoCounter("Cumulated time spend in chair queue")
	private Counter cumQueueChair;

	@AutoCounter("Total Standing People")
	private Counter cumNoAvailableChair;

	@AutoCounter("Total arrivals")
	private Counter arrivals;

	@AutoCounter("Total departures")
	private Counter departures;

	public CoronaVacLocState(double timeHorizon, long seed,
							 int nServers,
							 int nChairs,
							 int lambda,
							 int mu,
							 double chairSittingTime,
							 int maxArrivals,
							 double rejectionHour) {
		super(timeHorizon, seed);
		this.random = new Random(seed);
		this.nServers = nServers;
		this.nChairs = nChairs;
		this.lambda = lambda;
		this.mu = mu;
		this.chairSittingTime = chairSittingTime;
		this.maxArrivals = maxArrivals;
		this.rejectionHour = rejectionHour;

		reset();
	}

	@Initialize
	public void initReplication() {
		double nextArrivalTime = UtilsCoronaVacLoc.nextInterArrivalTime(this.random, this.lambda);
		addEvent(nextArrivalTime, this::doArrival);
	}

	@StopCriterium
	public boolean shouldTerminate() {
		return this.departures.getValue() == this.arrivals.getValue() && this.lastPerson;
	}

	public void doArrival(double eventTime) {

		// update counter for chair queue time
		this.cumQueueChair.incrementBy((eventTime - getCurrentTime()) * this.nChairQueue);

		this.arrivals.increment();

		if (this.nServersBusy == this.nServers) {
			this.nServerQueue++;
		} else {
			this.nServersBusy++;
			// generate next departure
			double serviceDuration = UtilsCoronaVacLoc.nextServiceTime(random, mu);
			double departureTime = eventTime + serviceDuration;
			addEvent(departureTime, this::doServerDeparture);
		}

		// generate next arrival
		if (this.maxArrivals == this.arrivals.getValue()) {
			addEvent(Double.POSITIVE_INFINITY, this::doArrival);
			this.lastPerson = true;
		} else {
			double nextInterArrivalTime = UtilsCoronaVacLoc.nextInterArrivalTime(random, lambda);
			double nextArrivalTime = eventTime + nextInterArrivalTime;
			if (nextArrivalTime <= this.rejectionHour) {
				addEvent(nextArrivalTime, this::doArrival);
			} else {
				addEvent(Double.POSITIVE_INFINITY, this::doArrival);
				this.lastPerson = true;
			}
		}

		this.lastPersonArrivedTime = eventTime;

	}

	public void doServerDeparture(double eventTime) {

		// update counter for chair queue time
		this.cumQueueChair.incrementBy((eventTime - getCurrentTime()) * this.nChairQueue);

		this.nServersBusy--;
		addEvent(eventTime + this.chairSittingTime, this::doChairDeparture);
		if (this.nChairsBusy == this.nChairs) {
			this.nChairQueue++;
			this.cumNoAvailableChair.increment();
		} else {
			this.nChairsBusy++;
		}

		if (this.nServerQueue > 0) {
			this.nServerQueue--;
			this.nServersBusy++;
			// generate next departure
			double serviceDuration = UtilsCoronaVacLoc.nextServiceTime(random, mu);
			double departureTime = eventTime + serviceDuration;
			addEvent(departureTime, this::doServerDeparture);
		}

	}

	public void doChairDeparture(double eventTime) {

		// update counter for chair queue time
		this.cumQueueChair.incrementBy((eventTime - getCurrentTime()) * this.nChairQueue);

		this.departures.increment();
		this.nChairsBusy--;
		if (this.nChairQueue > 0) {
			this.nChairsBusy++;
			this.nChairQueue--;
		}

		this.lastPersonLeftTime = eventTime;
	}

	@AutoMeasure("p")
	public Double getP() {
		return this.cumNoAvailableChair.getValue()/this.arrivals.getValue();
	}

	@AutoMeasure("if no chair time is")
	public Double getChairQueueTime() {
		if (this.cumNoAvailableChair.getValue() == 0) {
			return 0.0;
		} else {
			return this.cumQueueChair.getValue() / this.cumNoAvailableChair.getValue();
		}
	}

	@AutoMeasure("last person arrived at")
	public Double getLastPersonArrivalTime() {
		return this.lastPersonArrivedTime;
	}

	@AutoMeasure("last person left at")
	public Double getLastPersonTime() {
		return this.lastPersonLeftTime;
	}

	@AutoMeasure("Arrivals")
	public Double getArrivals() {
		return this.arrivals.getValue();
	}

	@AutoMeasure("cumQueueTime")
	public Double getCumQueueTime() {
		return this.cumQueueChair.getValue();
	}

	@AutoMeasure("cumNoAvailableChair")
	public Double getCumNoAvailableChair() {
		return this.cumNoAvailableChair.getValue();
	}

	@Override
	public void reset() {
		this.nServersBusy = 0;
		this.nServerQueue = 0;
		this.nChairsBusy = 0;
		this.nChairQueue = 0;

		this.lastPerson = false;
	}
}
