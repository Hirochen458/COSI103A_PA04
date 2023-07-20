package edu.brandeis.cs.cs131.pa4;

import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.brandeis.cs.cs131.pa4.scheduler.Factory;
import edu.brandeis.cs.cs131.pa4.scheduler.Scheduler;
import edu.brandeis.cs.cs131.pa4.submission.Vehicle;
import edu.brandeis.cs.cs131.pa4.tunnel.Tunnel;


public class TestUtilities {
    //Change the import to use your concreteFactory and nothing else
    
    public static final String carName = "CAR";
    public static final String sledName = "SLED";
    //Names used in testing
    public static final String[] gbNames = {"VENKMAN", "STANTZ", "SPENGLER", "ZEDDEMORE", "BARRETT", "TULLY", "MELNITZ", "PECK", "LENNY", "GOZER", "SLIMER", "STAY PUFT", "GATEKEEPER", "KEYMASTER"};
    public static final String[] mrNames = {"CATSKILL", "ROCKY", "APPALACHIAN", "OLYMPIC", "HIMALAYA", "GREAT DIVIDING", "TRANSANTRIC", "URAL", "ATLAS", "ALTAI", "CARPATHIAN", "KJOLEN", "BARISAN", "COAST", "QIN", "WESTERN GHATS"};
    
    public static final Factory factory = new Factory();
    
    public static void VehicleEnters(Vehicle vehicle, Tunnel tunnel) {
        boolean canEnter = tunnel.tryToEnter(vehicle);
        assertTrue(canEnter, String.format("%s cannot use", vehicle));
        vehicle.doWhileInTunnel();
        tunnel.exitTunnel(vehicle);
    }
    
    public static void VehicleEnters(Vehicle vehicle, Scheduler scheduler) {
        Tunnel tunnel = scheduler.admit(vehicle);
        assertTrue((tunnel != null), String.format("%s cannot use", vehicle));
        vehicle.doWhileInTunnel();
        scheduler.exit(vehicle);
    }
   
}
