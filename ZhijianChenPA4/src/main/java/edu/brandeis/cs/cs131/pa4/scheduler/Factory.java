package edu.brandeis.cs.cs131.pa4.scheduler;

import java.util.Collection;

import edu.brandeis.cs.cs131.pa4.submission.PreemptivePriorityScheduler;
import edu.brandeis.cs.cs131.pa4.submission.PreemptiveTunnel;
import edu.brandeis.cs.cs131.pa4.submission.Vehicle;
import edu.brandeis.cs.cs131.pa4.tunnel.Ambulance;
import edu.brandeis.cs.cs131.pa4.tunnel.Car;
import edu.brandeis.cs.cs131.pa4.tunnel.Direction;
import edu.brandeis.cs.cs131.pa4.tunnel.Sled;
import edu.brandeis.cs.cs131.pa4.tunnel.Tunnel;

/**
 * The interface that creates instances of specific classes
 * @author cs131a
 *
 */
public class Factory {

	/**
	 * Creates a new instance of class BasicTunnel
	 * @param name the name of the tunnel to create
	 * @return the newly created instance of the BasicTunnel class
	 */
    public Tunnel createNewPreemptiveTunnel(String name) {
    	return new PreemptiveTunnel(name);
    }

    
    /**
     * Creates a new instance of class PreemptivePriorityScheduler
     * @param name the name of the preemptive priority scheduler to create
     * @param tunnels the collection of tunnels that the scheduler should manage
     * @return the newly created instance of the PreemptivePriorityScheduler class
     */
    public Scheduler createNewPreemptivePriorityScheduler(String name, Collection<Tunnel> tunnels) {
    	return new PreemptivePriorityScheduler(name, tunnels);
    }
    
    /**
     * Creates a new instance of class Car
     * @param name the name of the car to create
     * @param direction the direction of the car
     * @return the newly created instance of the Car class
     */
    public Vehicle createNewCar(String name, Direction direction) {
    	return new Car(name, direction); 
    }

    /**
     * Creates a new instance of class Sled
     * @param name the name of the sled to create
     * @param direction the direction of the sled
     * @return the newly created instance of the Sled class
     */
    public Vehicle createNewSled(String name, Direction direction) {
    	return new Sled(name, direction);
    }
    
    /**
     * Creates a new instance of class Ambulance
     * @param name the name of the ambulance to create
     * @param direction the direction of the ambulance
     * @return the newly created instance of the Ambulance class
     */
    public Vehicle createNewAmbulance(String name, Direction direction) {
    	return new Ambulance(name, direction);
    }

}
