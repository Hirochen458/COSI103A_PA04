package edu.brandeis.cs.cs131.pa4;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Timeout;

import edu.brandeis.cs.cs131.pa4.logging.Event;
import edu.brandeis.cs.cs131.pa4.logging.EventType;
import edu.brandeis.cs.cs131.pa4.logging.Log;
import edu.brandeis.cs.cs131.pa4.scheduler.Scheduler;
import edu.brandeis.cs.cs131.pa4.submission.Vehicle;
import edu.brandeis.cs.cs131.pa4.tunnel.Ambulance;
import edu.brandeis.cs.cs131.pa4.tunnel.Car;
import edu.brandeis.cs.cs131.pa4.tunnel.Direction;
import edu.brandeis.cs.cs131.pa4.tunnel.Sled;
import edu.brandeis.cs.cs131.pa4.tunnel.Tunnel;

public class PrioritySchedulerTest {

	private final String preemptivePrioritySchedulerName = "PREEMPTIVE_SCHEDULER";

	private static final int NUM_RUNS = 10;

	@BeforeEach
	public void setUp() {
		Tunnel.DEFAULT_LOG.clearLog();
	}

	@BeforeAll
	public static void broadcast() {
		System.out.printf("Running Priority Scheduler Tests using %s \n",
				TestUtilities.factory.getClass().getCanonicalName());
	}

	private Scheduler setupPreemptivePriorityScheduler(String name) {
		Collection<Tunnel> tunnels = new ArrayList<Tunnel>();
		tunnels.add(TestUtilities.factory.createNewPreemptiveTunnel(name));
		return TestUtilities.factory.createNewPreemptivePriorityScheduler(preemptivePrioritySchedulerName, tunnels);
	}

	private Scheduler setupPreemptivePrioritySchedulerTwoTunnels(String name1, String name2) {
		Collection<Tunnel> tunnels = new ArrayList<Tunnel>();
		tunnels.add(TestUtilities.factory.createNewPreemptiveTunnel(name1));
		tunnels.add(TestUtilities.factory.createNewPreemptiveTunnel(name2));
		return TestUtilities.factory.createNewPreemptivePriorityScheduler(preemptivePrioritySchedulerName, tunnels);
	}

	@RepeatedTest(NUM_RUNS)
	@Timeout(30)
	public void Car_Enter() {
		Vehicle car = TestUtilities.factory.createNewCar(TestUtilities.gbNames[0], Direction.random());
		Scheduler scheduler = setupPreemptivePriorityScheduler(TestUtilities.mrNames[0]);
		TestUtilities.VehicleEnters(car, scheduler);
	}

	@RepeatedTest(NUM_RUNS)
	@Timeout(30)
	public void Sled_Enter() {
		Vehicle sled = TestUtilities.factory.createNewSled(TestUtilities.gbNames[0], Direction.random());
		Scheduler scheduler = setupPreemptivePriorityScheduler(TestUtilities.mrNames[0]);
		TestUtilities.VehicleEnters(sled, scheduler);
	}

	@RepeatedTest(NUM_RUNS)
	@Timeout(30)
	public void Ambulance_Enter() {
		Vehicle ambulance = TestUtilities.factory.createNewAmbulance(TestUtilities.gbNames[0], Direction.random());
		Scheduler scheduler = setupPreemptivePriorityScheduler(TestUtilities.mrNames[0]);
		TestUtilities.VehicleEnters(ambulance, scheduler);
	}

	@RepeatedTest(NUM_RUNS)
	@Timeout(30)
	public void PreemptivePriority() {
		List<Thread> vehicleThreads = new ArrayList<Thread>();
		Scheduler preemptivePriorityScheduler = setupPreemptivePriorityScheduler(TestUtilities.mrNames[0]);
		// start 3 fast cars
		for (int i = 0; i < 3; i++) {
			Vehicle car = TestUtilities.factory.createNewCar(Integer.toString(i), Direction.NORTH);
			car.setSpeed(8);
			car.setScheduler(preemptivePriorityScheduler);
			car.start();
			vehicleThreads.add(car);
		}
		try {
			Thread.sleep(50);
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
		// start one slow ambulance
		for (int i = 0; i < 1; i++) {
			Vehicle ambulance = TestUtilities.factory.createNewAmbulance("AMB" + i,
					Direction.values()[i % Direction.values().length]);
			ambulance.setSpeed(0);
			ambulance.setScheduler(preemptivePriorityScheduler);
			ambulance.start();
			vehicleThreads.add(ambulance);
		}
		for (Thread t : vehicleThreads) {
			try {
				t.join();
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
		// make sure that nobody exits the tunnel until ambulances exit
		Tunnel.DEFAULT_LOG.addToLog(EventType.END_TEST);
		Log log = Tunnel.DEFAULT_LOG;
		Event currentEvent;
		Vehicle ambulance = null;
		Vehicle[] cars = new Vehicle[3];
		for (int i = 0; i < 3; i++)
			cars[i] = null;
		boolean ambulanceLeft = false;
		do {
			currentEvent = log.get();
			if (currentEvent.getEvent() == EventType.ENTER_SUCCESS && currentEvent.getVehicle() instanceof Ambulance) {
				ambulance = currentEvent.getVehicle();
			}
			if (currentEvent.getEvent() == EventType.ENTER_SUCCESS && currentEvent.getVehicle() instanceof Car) {
				switch (currentEvent.getVehicle().getVehicleName()) {
				case "0":
					cars[0] = currentEvent.getVehicle();
					break;
				case "1":
					cars[1] = currentEvent.getVehicle();
					break;
				case "2":
					cars[2] = currentEvent.getVehicle();
					break;
				default:
					assertTrue(false, "Wrong vehicle entered tunnel!");
					break;
				}
			}
			if (currentEvent.getEvent() == EventType.LEAVE_START) {
				if (currentEvent.getVehicle() instanceof Car && !ambulanceLeft) {
					assertTrue(false,
							"Vehicle " + currentEvent.getVehicle() + " left tunnel while ambulance was still running!");
				}
				if (currentEvent.getVehicle() instanceof Ambulance)
					ambulanceLeft = true;
			}
			System.out.println(currentEvent.toString());
		} while (!currentEvent.getEvent().equals(EventType.END_TEST));
		if (ambulance == null | cars[0] == null || cars[1] == null || cars[2] == null) {
			assertTrue(false, "Vehicles did not enter tunnel successfully!");
		}
	}

	@RepeatedTest(NUM_RUNS)
	@Timeout(30)
	public void PreemptivePriorityManyAmb() {
		List<Thread> vehicleThreads = new ArrayList<Thread>();
		Scheduler preemptivePriorityScheduler = setupPreemptivePriorityScheduler(TestUtilities.mrNames[0]);
		// start 3 slow cars
		for (int i = 0; i < 3; i++) {
			Vehicle car = TestUtilities.factory.createNewCar(Integer.toString(i), Direction.NORTH);
			car.setSpeed(0);
			car.setScheduler(preemptivePriorityScheduler);
			car.start();
			vehicleThreads.add(car);
		}
		try {
			Thread.sleep(50);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		// start 4 fast ambulances
		for (int i = 0; i < 4; i++) {
			Vehicle ambulance = TestUtilities.factory.createNewAmbulance("AMB" + i,
					Direction.values()[i % Direction.values().length]);
			ambulance.setSpeed(9);
			ambulance.setScheduler(preemptivePriorityScheduler);
			ambulance.start();
			vehicleThreads.add(ambulance);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		for (Thread t : vehicleThreads) {
			try {
				t.join();
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
		// make sure that nobody exits the tunnel until ambulances exit
		Tunnel.DEFAULT_LOG.addToLog(EventType.END_TEST);
		Log log = Tunnel.DEFAULT_LOG;
		Event currentEvent;
		Vehicle ambulances[] = new Vehicle[4];
		Vehicle cars[] = new Vehicle[3];
		for (int i = 0; i < 4; i++)
			ambulances[i] = null;
		for (int i = 0; i < 3; i++)
			cars[i] = null;
		int ambulancesLeft = 0;
		do {
			currentEvent = log.get();
			if (currentEvent.getEvent() == EventType.ENTER_SUCCESS && currentEvent.getVehicle() instanceof Ambulance) {
				switch (currentEvent.getVehicle().getVehicleName()) {
				case "AMB0":
					ambulances[0] = currentEvent.getVehicle();
					break;
				case "AMB1":
					ambulances[1] = currentEvent.getVehicle();
					break;
				case "AMB2":
					ambulances[2] = currentEvent.getVehicle();
					break;
				case "AMB3":
					ambulances[3] = currentEvent.getVehicle();
					break;
				default:
					assertTrue(false, "Wrong vehicle entered tunnel!");
					break;
				}
			}
			if (currentEvent.getEvent() == EventType.ENTER_SUCCESS && currentEvent.getVehicle() instanceof Car) {
				switch (currentEvent.getVehicle().getVehicleName()) {
				case "0":
					cars[0] = currentEvent.getVehicle();
					break;
				case "1":
					cars[1] = currentEvent.getVehicle();
					break;
				case "2":
					cars[2] = currentEvent.getVehicle();
					break;
				default:
					assertTrue(false, "Wrong vehicle entered tunnel!");
					break;
				}
			}
			if (currentEvent.getEvent() == EventType.LEAVE_START) {
				if (currentEvent.getVehicle() instanceof Car && ambulancesLeft < 4) {
					assertTrue(false,
							"Vehicle " + currentEvent.getVehicle() + " left tunnel while "
									+ ((4 - ambulancesLeft) == 1 ? " 1 ambulance was"
											: (4 - ambulancesLeft) + " ambulances were")
									+ " still running!");
				}
				if (currentEvent.getVehicle() instanceof Ambulance)
					ambulancesLeft++;
			}
			System.out.println(currentEvent.toString());
		} while (!currentEvent.getEvent().equals(EventType.END_TEST));
		for (int i = 0; i < 4; i++)
			if (ambulances[i] == null)
				assertTrue(false, "Ambulances did not enter tunnel successfully!");
		for (int i = 0; i < 3; i++)
			if (cars[i] == null)
				assertTrue(false, "Cars did not enter tunnel successfully!");
	}

	@RepeatedTest(NUM_RUNS)
	@Timeout(30)
	public void PreemptivePriorityManyTunnels() {
		List<Thread> vehicleThreads = new ArrayList<Thread>();
		Scheduler preemptivePriorityScheduler = setupPreemptivePrioritySchedulerTwoTunnels(TestUtilities.mrNames[0],
				TestUtilities.mrNames[1]);
		// start a fast car in first tunnel
		Vehicle car1 = TestUtilities.factory.createNewCar("0", Direction.NORTH);
		car1.setSpeed(9);
		car1.setScheduler(preemptivePriorityScheduler);
		car1.start();
		vehicleThreads.add(car1);
		// start a fast car in second tunnel
		Vehicle car2 = TestUtilities.factory.createNewCar("1", Direction.SOUTH);
		car2.setSpeed(9);
		car2.setScheduler(preemptivePriorityScheduler);
		car2.start();
		vehicleThreads.add(car2);
		try {
			Thread.sleep(50);
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
		// start a slow ambulance
		Vehicle ambulance = TestUtilities.factory.createNewAmbulance("AMB0", Direction.NORTH);
		ambulance.setSpeed(0);
		ambulance.setScheduler(preemptivePriorityScheduler);
		Thread ambulanceThread = new Thread(ambulance);
		ambulanceThread.start();
		vehicleThreads.add(ambulanceThread);
		for (Thread t : vehicleThreads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Tunnel.DEFAULT_LOG.addToLog(EventType.END_TEST);
		Log log = Tunnel.DEFAULT_LOG;
		Event currentEvent;
		ambulance = null;
		car1 = null;
		car2 = null;
		Tunnel ambulanceTunnel = null;
		Tunnel car1Tunnel = null;
		Vehicle lonelyCar = null;
		boolean ambulanceLeft = false, carLonelyTunnelLeft = false;
		do {
			currentEvent = log.get();
			if (currentEvent.getEvent() == EventType.ENTER_SUCCESS && currentEvent.getVehicle() instanceof Ambulance) {
				ambulance = currentEvent.getVehicle();
				ambulanceTunnel = currentEvent.getTunnel();
				lonelyCar = (car1Tunnel == ambulanceTunnel ? car2 : car1);
			}
			if (currentEvent.getEvent() == EventType.ENTER_SUCCESS && currentEvent.getVehicle() instanceof Car) {
				switch (currentEvent.getVehicle().getVehicleName()) {
				case "0":
					car1 = currentEvent.getVehicle();
					car1Tunnel = currentEvent.getTunnel();
					break;
				case "1":
					car2 = currentEvent.getVehicle();
					break;
				default:
					assertTrue(false, "Wrong vehicle entered tunnel!");
					break;
				}
			}
			if (currentEvent.getEvent() == EventType.LEAVE_START) {
				if (currentEvent.getVehicle() instanceof Car
						&& currentEvent.getTunnel().getName() == ambulanceTunnel.getName() && !ambulanceLeft) {
					assertTrue(false,
							"Vehicle " + currentEvent.getVehicle() + " left tunnel while ambulance was still running!");
				}
				if (currentEvent.getVehicle() instanceof Car
						&& currentEvent.getTunnel().getName() != ambulanceTunnel.getName()) {
					assertTrue(currentEvent.getVehicle() == lonelyCar,
							"Car " + currentEvent.getVehicle().getVehicleName() + " should be in the other Tunnel");
					carLonelyTunnelLeft = true;
				}
				if (currentEvent.getVehicle() instanceof Ambulance) {
					ambulanceLeft = true;
					// at this point, car in the other tunnel must have left!
					if (!carLonelyTunnelLeft)
						assertTrue(false, "Car " + lonelyCar.getVehicleName()
								+ " should not wait for ambulance to exit, since they are in different tunnels");
				}
			}
			System.out.println(currentEvent.toString());
		} while (!currentEvent.getEvent().equals(EventType.END_TEST));
		if (ambulance == null | car1 == null || car2 == null) {
			assertTrue(false, "Vehicles did not enter tunnel successfully!");
		}
	}

	@RepeatedTest(NUM_RUNS)
	@Timeout(30)
	public void testCarAmbulanceCar() {
		Scheduler preemptivePriorityScheduler = setupPreemptivePriorityScheduler(TestUtilities.mrNames[0]);
		Vehicle[] cars = new Vehicle[2];
		for (int i = 0; i < cars.length; i++) {
			cars[i] = TestUtilities.factory.createNewCar(Integer.toString(i), Direction.NORTH);
			cars[i].setScheduler(preemptivePriorityScheduler);
			cars[i].setSpeed(5);
		}
		Vehicle ambulance = TestUtilities.factory.createNewAmbulance("0", Direction.NORTH);
		ambulance.setScheduler(preemptivePriorityScheduler);
		ambulance.setSpeed(0);

		cars[0].start();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		ambulance.start();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		cars[1].start();
		try {
			ambulance.join();
			for (Thread carThread : cars) {
				carThread.join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Tunnel.DEFAULT_LOG.addToLog(EventType.END_TEST);
		Log log = Tunnel.DEFAULT_LOG;
		Event currentEvent = null;

		boolean ambulanceLeft = false;
		Set<Vehicle> completed = new HashSet<Vehicle>();

		do {
			currentEvent = log.get();
			System.out.println(currentEvent);

			if (currentEvent.getEvent().equals(EventType.LEAVE_START)) {
				if (currentEvent.getVehicle() instanceof Ambulance) {
					ambulanceLeft = true;
				} else {
					assertTrue(ambulanceLeft, currentEvent.getVehicle() + " left before the ambulance");
				}
			} else if (currentEvent.getEvent().equals(EventType.ENTER_SUCCESS)) {
				assertFalse(ambulanceLeft,
						currentEvent.getVehicle() + " should have entered successfully before the ambulance left");
			} else if (currentEvent.getEvent().equals(EventType.COMPLETE)) {
				completed.add(currentEvent.getVehicle());
			}

		} while (!currentEvent.getEvent().equals(EventType.END_TEST));

		assertTrue(completed.contains(ambulance), ambulance + " did not complete");
		for (Vehicle car : cars) {
			assertTrue(completed.contains(car), car + " did not complete");
		}
	}

	private void testVehicleAfterAmbulance(Vehicle vehicle) {
		Scheduler preemptivePriorityScheduler = setupPreemptivePriorityScheduler(TestUtilities.mrNames[0]);
		vehicle.setScheduler(preemptivePriorityScheduler);
		vehicle.setSpeed(5);

		Vehicle[] ambulances = new Vehicle[2];
		Thread[] ambulanceThreads = new Thread[ambulances.length];
		for (int i = 0; i < ambulances.length; i++) {
			ambulances[i] = TestUtilities.factory.createNewAmbulance(Integer.toString(i), Direction.NORTH);
			ambulances[i].setScheduler(preemptivePriorityScheduler);
			ambulances[i].setSpeed(0);
			ambulanceThreads[i] = new Thread(ambulances[i]);
		}

		ambulanceThreads[0].start();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		vehicle.start();
		try {
			ambulanceThreads[0].join();
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		ambulanceThreads[1].start();
		try {
			ambulanceThreads[1].join();
			vehicle.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Tunnel.DEFAULT_LOG.addToLog(EventType.END_TEST);
		Log log = Tunnel.DEFAULT_LOG;
		Event currentEvent = null;

		boolean[] ambulanceLeft = new boolean[ambulances.length];
		Set<Vehicle> completed = new HashSet<Vehicle>();

		do {
			currentEvent = log.get();
			System.out.println(currentEvent);

			if (currentEvent.getEvent().equals(EventType.ENTER_SUCCESS) && currentEvent.getVehicle().equals(vehicle)) {
				for (int i = 0; i < ambulances.length; i++) {
					assertFalse(ambulanceLeft[i],
							currentEvent.getVehicle() + " entered successfully after " + ambulances[i] + " left");
				}
			} else if (currentEvent.getEvent().equals(EventType.LEAVE_START)) {
				if (currentEvent.getVehicle() instanceof Ambulance) {
					int name = Integer.parseInt(currentEvent.getVehicle().getVehicleName());
					ambulanceLeft[name] = true;
				} else {
					for (int i = 0; i < ambulances.length; i++) {
						assertTrue(ambulanceLeft[i],
								currentEvent.getVehicle() + " left before " + ambulances[i] + " left");
					}
				}
			} else if (currentEvent.getEvent().equals(EventType.COMPLETE)) {
				completed.add(currentEvent.getVehicle());
			}

		} while (!currentEvent.getEvent().equals(EventType.END_TEST));

		assertTrue(completed.contains(vehicle), vehicle + " did not complete");
		for (Vehicle ambulance : ambulances) {
			assertTrue(completed.contains(ambulance), ambulance + " did not complete");
		}

	}

	@RepeatedTest(NUM_RUNS)
	@Timeout(30)
	public void testCarAfterAmbulance() {
		testVehicleAfterAmbulance(TestUtilities.factory.createNewCar("0", Direction.NORTH));
	}

	@RepeatedTest(NUM_RUNS)
	@Timeout(30)
	public void testSledAfterAmbulance() {
		testVehicleAfterAmbulance(TestUtilities.factory.createNewSled("0", Direction.NORTH));
	}

	/**
	 * Ensures that the presence of an Ambulance does not impact container
	 * constraints imposed by BasicTunnel in PA3. That is, if there is a Sled and
	 * Ambulance in a Tunnel, a Car will be rejected. It should not enter and pull
	 * over. Opposite of testCarAmbulanceSled().
	 * 
	 * @author Chami Lamelas
	 */
	@RepeatedTest(NUM_RUNS)
	@Timeout(30)
	public void testSledAmbulanceCar() {
		Scheduler preemptivePriorityScheduler = setupPreemptivePriorityScheduler(TestUtilities.mrNames[0]);

		// Create a Car and a Sled both speed 5 and corresponding Threads.
		Vehicle car = TestUtilities.factory.createNewCar("0", Direction.NORTH);
		car.setScheduler(preemptivePriorityScheduler);
		car.setSpeed(5);
		Vehicle sled = TestUtilities.factory.createNewSled("0", Direction.NORTH);
		sled.setScheduler(preemptivePriorityScheduler);
		sled.setSpeed(5);

		// Create Ambulance 1/2 the speed of the above Car and Sled. Speed = 5 =>
		// Vehicle spends 500 ms in tunnel. Speed = 0 => Vehicle spends 1000 ms in
		// tunnel.
		Vehicle ambulance = TestUtilities.factory.createNewAmbulance("0", Direction.NORTH);
		ambulance.setScheduler(preemptivePriorityScheduler);
		ambulance.setSpeed(0);

		// Start the sled and pause, make sure it gets in the tunnel and starts driving
		sled.start();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Start the ambulance and pause (Sled should now be pulled over). If the Sled
		// wasn't pulled over, it's driving 2x the speed of the Ambulance and would
		// finish before the Ambulance leaves.
		ambulance.start();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Start the car (Car should not enter and pull over, it should be rejected and
		// now should be waiting for the Sled to leave).
		car.start();

		// Block until all vehicle threads have finished travel
		try {
			car.join();
			ambulance.join();
			sled.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Tunnel.DEFAULT_LOG.addToLog(EventType.END_TEST);
		Log log = Tunnel.DEFAULT_LOG;
		Event currentEvent = null;

		boolean ambulanceLeft = false;
		boolean sledLeft = false;
		Set<Vehicle> completed = new HashSet<Vehicle>();

		do {
			currentEvent = log.get();
//			System.out.println(currentEvent);

			if (currentEvent.getEvent().equals(EventType.LEAVE_START)) {

				// If Ambulance is leaving, set flag
				if (currentEvent.getVehicle() instanceof Ambulance) {
					ambulanceLeft = true;
				}
				// If the Sled is leaving, make sure it was preempted (i.e. that it pulled over)
				// by making sure that the ambulance has already left. As mentioned previously,
				// since Sled is 2x fast, if it hadn't pulled over it would have left before the
				// Ambulance. Also, set flag.
				else if (currentEvent.getVehicle() instanceof Sled) {
					assertTrue(ambulanceLeft, sled + " left before Ambulance");
					sledLeft = true;
				}
			}
			// If Car entered successfully, this should only happen after the sled has left.
			else if (currentEvent.getEvent().equals(EventType.ENTER_SUCCESS)
					&& currentEvent.getVehicle() instanceof Car) {
				assertTrue(sledLeft, car + " entered successfully before " + sled + " left");
			} else if (currentEvent.getEvent().equals(EventType.COMPLETE)) {
				completed.add(currentEvent.getVehicle());
			}

		} while (!currentEvent.getEvent().equals(EventType.END_TEST));

		// Make sure all vehicles completed
		Vehicle[] allVehicles = { sled, car, ambulance };
		for (Vehicle vehicle : allVehicles) {
			assertTrue(completed.contains(vehicle), vehicle + " did not complete");
		}
	}

	/**
	 * Ensures that the presence of an Ambulance does not impact container
	 * constraints imposed by BasicTunnel in PA3. That is, if there is a Car and
	 * Ambulance in a Tunnel, a Sled will be rejected. It should not enter and pull
	 * over. Opposite of testSledAmbulanceCar().
	 * 
	 * @author Chami Lamelas
	 */
	@RepeatedTest(NUM_RUNS)
	@Timeout(30)
	public void testCarAmbulanceSled() {
		Scheduler preemptivePriorityScheduler = setupPreemptivePriorityScheduler(TestUtilities.mrNames[0]);

		// Create a Car and a Sled both speed 5 and corresponding Threads.
		Vehicle car = TestUtilities.factory.createNewCar("0", Direction.NORTH);
		car.setScheduler(preemptivePriorityScheduler);
		car.setSpeed(5);
		Vehicle sled = TestUtilities.factory.createNewSled("0", Direction.NORTH);
		sled.setScheduler(preemptivePriorityScheduler);
		sled.setSpeed(5);

		// Create Ambulance 1/2 the speed of the above Car and Sled. Speed = 5 =>
		// Vehicle spends 500 ms in tunnel. Speed = 0 => Vehicle spends 1000 ms in
		// tunnel.
		Vehicle ambulance = TestUtilities.factory.createNewAmbulance("0", Direction.NORTH);
		ambulance.setScheduler(preemptivePriorityScheduler);
		ambulance.setSpeed(0);

		// Start the car and pause, make sure it gets in the tunnel and starts driving
		car.start();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Start the ambulance and pause (Car should now be pulled over). If the Car
		// wasn't pulled over, it's driving 2x the speed of the Ambulance and would
		// finish before the Ambulance leaves.
		ambulance.start();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Start the sled (Sled should not enter and pull over, it should be rejected
		// and now should be waiting for the Car to leave).
		sled.start();

		// Block until all vehicle threads have finished travel
		try {
			car.join();
			ambulance.join();
			sled.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Tunnel.DEFAULT_LOG.addToLog(EventType.END_TEST);
		Log log = Tunnel.DEFAULT_LOG;
		Event currentEvent = null;

		boolean ambulanceLeft = false;
		boolean carLeft = false;
		Set<Vehicle> completed = new HashSet<Vehicle>();

		do {
			currentEvent = log.get();
//			System.out.println(currentEvent);

			if (currentEvent.getEvent().equals(EventType.LEAVE_START)) {

				// If Ambulance is leaving, set flag
				if (currentEvent.getVehicle() instanceof Ambulance) {
					ambulanceLeft = true;
				}
				// If the Car is leaving, make sure it was preempted (i.e. that it pulled over)
				// by making sure that the ambulance has already left. As mentioned previously,
				// since Car is 2x fast, if it hadn't pulled over it would have left before the
				// Ambulance. Also, set flag.
				else if (currentEvent.getVehicle() instanceof Car) {
					assertTrue(ambulanceLeft, car + " left before Ambulance");
					carLeft = true;
				}
			}
			// If Sled entered successfully, this should only happen after the car has left.
			else if (currentEvent.getEvent().equals(EventType.ENTER_SUCCESS)
					&& currentEvent.getVehicle() instanceof Sled) {
				assertTrue(carLeft, sled + " entered successfully before " + car + " left");
			} else if (currentEvent.getEvent().equals(EventType.COMPLETE)) {
				completed.add(currentEvent.getVehicle());
			}

		} while (!currentEvent.getEvent().equals(EventType.END_TEST));

		// Make sure all vehicles completed
		Vehicle[] allVehicles = { sled, car, ambulance };
		for (Vehicle vehicle : allVehicles) {
			assertTrue(completed.contains(vehicle), vehicle + " did not complete");
		}
	}

	@RepeatedTest(NUM_RUNS)
	@Timeout(30)
	public void testMultipleAmbulanceMultipleTunnels() {
		Scheduler preemptivePriorityScheduler = setupPreemptivePrioritySchedulerTwoTunnels(TestUtilities.mrNames[0],
				TestUtilities.mrNames[1]);
		int[] carSpeeds = { 7, 7 };
		int[] ambulanceSpeeds = { 7, 0 };
		Direction[] directions = { Direction.NORTH, Direction.SOUTH };
		Vehicle[] cars = new Vehicle[2];
		Thread[] carThreads = new Thread[2];
		Vehicle[] ambulances = new Vehicle[2];
		Thread[] ambulanceThreads = new Thread[2];
		for (int i = 0; i < 2; i++) {
			cars[i] = TestUtilities.factory.createNewCar("0", directions[i]);
			cars[i].setScheduler(preemptivePriorityScheduler);
			cars[i].setSpeed(carSpeeds[i]);
			carThreads[i] = cars[i];
			ambulances[i] = TestUtilities.factory.createNewAmbulance(Integer.toString(i), Direction.NORTH);
			ambulances[i].setScheduler(preemptivePriorityScheduler);
			ambulances[i].setSpeed(ambulanceSpeeds[i]);
			ambulanceThreads[i] = new Thread(ambulances[i]);
		}
		for (Thread carThread : carThreads) {
			carThread.start();
		}
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		for (Thread ambulanceThread : ambulanceThreads) {
			ambulanceThread.start();
		}
		try {
			for (int i = 0; i < 2; i++) {
				carThreads[i].join();
				ambulanceThreads[i].join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Tunnel.DEFAULT_LOG.addToLog(EventType.END_TEST);
		Log log = Tunnel.DEFAULT_LOG;
		Event currentEvent = null;
		Set<Vehicle> completed = new HashSet<Vehicle>();
		Map<Tunnel, Vehicle> mapTunnelToAmbulance = new HashMap<Tunnel, Vehicle>();
		Set<Vehicle> ambulancesLeft = new HashSet<Vehicle>();
		do {
			currentEvent = log.get();
			System.out.println(currentEvent);
			if (currentEvent.getEvent().equals(EventType.ENTER_SUCCESS)
					&& currentEvent.getVehicle() instanceof Ambulance) {
				mapTunnelToAmbulance.put(currentEvent.getTunnel(), currentEvent.getVehicle());
			} else if (currentEvent.getEvent().equals(EventType.LEAVE_START)) {
				if (currentEvent.getVehicle() instanceof Ambulance) {
					ambulancesLeft.add(currentEvent.getVehicle());
				} else {
					assertTrue(ambulancesLeft.contains(mapTunnelToAmbulance.get(currentEvent.getTunnel())));
				}
			} else if (currentEvent.getEvent().equals(EventType.COMPLETE)) {
				completed.add(currentEvent.getVehicle());
			}
		} while (!currentEvent.getEvent().equals(EventType.END_TEST));
		// Make sure all vehicles completed
		for (int i = 0; i < 2; i++) {
			assertTrue(completed.contains(cars[i]), cars[i] + " did not complete");
			assertTrue(completed.contains(ambulances[i]), ambulances[i] + " did not complete");
		}
	}

	/**
	 * Tests to make sure that placing an Ambulance into a Tunnel has no effect on
	 * the direction of travel of the Tunnel.
	 * 
	 * @author Chami Lamelas
	 */
	@RepeatedTest(NUM_RUNS)
	@Timeout(30)
	public void testAmbulanceDirectionEffects() {
		// Create northbound ambulance, southbound car and a Tunnel
		Vehicle ambulanceNorth = TestUtilities.factory.createNewAmbulance("0", Direction.NORTH);
		Vehicle carSouth = TestUtilities.factory.createNewCar("0", Direction.SOUTH);
		Tunnel tunnel = TestUtilities.factory.createNewPreemptiveTunnel(TestUtilities.mrNames[0]);

		// Both ambulance and car should be allowed to enter
		assertTrue(tunnel.tryToEnter(ambulanceNorth), ambulanceNorth + " did not enter");
		assertTrue(tunnel.tryToEnter(carSouth), carSouth + " did not enter");
	}
}
