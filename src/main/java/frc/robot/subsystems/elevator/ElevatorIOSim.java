package frc.robot.subsystems.elevator;

import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.wpilibj.simulation.ElevatorSim;
import frc.robot.Constants;

public class ElevatorIOSim implements ElevatorIO {
    ElevatorSim sim;

    public ElevatorIOSim() {
        sim = new ElevatorSim(
                DCMotor.getNEO(2),
                Constants.ElevatorConstants.GEAR_RATIO,
                Constants.ElevatorConstants.MASS,
                Constants.ElevatorConstants.RADIUS,
                Constants.ElevatorConstants.MIN_HEIGHT,
                Constants.ElevatorConstants.MAX_HEIGHT,
                true,
                0.0);
    }

    @Override
    public void simulationPeriodic() {
        sim.update(Constants.LOOP_TIME);
    }

    public double getHeight() {
        return sim.getPositionMeters();
    }

    public void setVoltage(double voltage) {
        sim.setInputVoltage(voltage);
    }

    public void zeroEncoders() {
    }
}
