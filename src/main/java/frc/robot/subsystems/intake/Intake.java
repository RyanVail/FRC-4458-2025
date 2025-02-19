package frc.robot.subsystems.intake;

import org.littletonrobotics.junction.Logger;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class Intake extends SubsystemBase {
    IntakeIO io;

    PIDController pid;
    SimpleMotorFeedforward feedforward;

    private static final String LPREFIX = "/Subsystems/Intake/";

    public Intake(IntakeIO io) {
        this.io = io;
    }

    @Override
    public void periodic() {
        Logger.recordOutput(LPREFIX + "HasCoral", hasCoral());
        Logger.recordOutput(LPREFIX + "IsCoralLoaded", isCoralLoaded());
        Logger.recordOutput(LPREFIX + "Velocity", getVelocity());
        Logger.recordOutput(LPREFIX + "Voltage", getVoltage());

        io.simulationPeriodic();
    }

    public void setVoltage(double voltage) {
        io.setVoltage(voltage);
    }

    public double getVelocity() {
        return io.getVelocity();
    }

    public double getVoltage() {
        return io.getVoltage();
    }

    public boolean hasCoral() {
        return io.hasCoral();
    }

    public boolean isCoralLoaded() {
        return io.isCoralLoaded();
    }
}
