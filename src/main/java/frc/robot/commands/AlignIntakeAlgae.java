package frc.robot.commands;

import com.pathplanner.lib.util.FlippingUtil;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.Constants.FieldConstants;
import frc.robot.subsystems.drive.Drive;

public class AlignIntakeAlgae extends AlignPose {
    public AlignIntakeAlgae(Drive drive) {
        super(drive, null, AlignCamera.Front);
        super.setName("AlignIntakeAlgae");
    }

    @Override
    public void initialize() {
        Pose2d pose = (DriverStation.getAlliance().orElse(Alliance.Red) == Alliance.Red)
                ? FlippingUtil.flipFieldPose(drive.getPose())
                : drive.getPose();

        int index = getClosestPoseIndex(pose);
        Pose2d preintake_pose = FieldConstants.ALGAE_PREINTAKE_POSES[index];
        Pose2d intake_pose = FieldConstants.ALGAE_INTAKE_POSES[index];
        Commands.print("preintake_pose " + preintake_pose).schedule();
        Commands.print("intake_pose  " + intake_pose).schedule();

        // TODO: Prescore too.
        if (DriverStation.getAlliance().orElse(Alliance.Red) == Alliance.Red) {
            intake_pose = FlippingUtil.flipFieldPose(intake_pose);
        }

        setTarget(intake_pose);
        super.initialize();
    }

    public int getClosestPoseIndex(Pose2d pose) {
        Translation2d robot_pos = pose.getTranslation();
        int closest_pose_index = 0;
        double closest_dist = Double.MAX_VALUE;

        for (int i = 0; i < FieldConstants.ALGAE_INTAKE_POSES.length; i++) {
            Pose2d p = FieldConstants.ALGAE_INTAKE_POSES[i];
            double dist = p.getTranslation().getDistance(robot_pos);
            if (closest_dist >= dist) {
                closest_pose_index = i;
                closest_dist = dist;
            }
        }
 
        return closest_pose_index;
    }
}
