// Copyright (c) 2023 FRC 6328
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by an MIT-style
// license that can be found in the LICENSE file at
// the root directory of this project.

package frc.robot;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.Notifier;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.util.Color;

import java.util.List;

public class LEDManager {
	public enum Mode {
		NONE,
		AUTO_ALIGN_CORAL,
		INTAKE_ALGAE,
		STALL_ALGAE,
		ESTOP,
		INTAKE_CORAL,
		EJECT_CORAL,
	};

	private static AddressableLED leds;
	private static AddressableLEDBuffer buffer;
	private static Notifier loadingNotifier;

	private static Mode mode;
	private static int loopCycleCount = 0;
	private static double autoFinishedTime = 0.0;
	private static Alliance alliance = Alliance.Blue;
	private static boolean lastEnabledAuto = false;
	private static double lastEnabledTime = 0.0;
	private static boolean auto;

	private static final int minLoopCycleCount = 10;
	private static final int length = 37;
	private static final double strobeFastDuration = 0.1;
	private static final double strobeSlowDuration = 0.2;
	private static final double breathDuration = 0.5;
	private static final double rainbowCycleLength = 25.0;
	private static final double rainbowDuration = 0.25;
	private static final double waveExponent = 0.4;
	private static final double waveFastCycleLength = 25.0;
	private static final double waveFastDuration = 0.25;
	private static final double waveSlowCycleLength = 25.0;
	private static final double waveSlowDuration = 3.0;
	private static final double waveAllianceCycleLength = 15.0;
	private static final double waveAllianceDuration = 2.0;
	private static final double autoFadeTime = 2.5; // 3s nominal
	private static final double autoFadeMaxTime = 5.0; // Return to normal

	public static void initialize() {
		LEDManager.leds = new AddressableLED(0);
		buffer = new AddressableLEDBuffer(length);
		leds.setLength(length);
		leds.setData(buffer);
		leds.start();
		loadingNotifier = new Notifier(
				() -> {
					LEDManager.breath(
							Section.FULL,
							Color.kWhite,
							Color.kBlack,
							0.25,
							System.currentTimeMillis() / 1000.0);
					leds.setData(buffer);
				});

		loadingNotifier.startPeriodic(Constants.LOOP_TIME);
	}

	public static void setMode(Mode mode) {
		LEDManager.mode = mode;
	}

	public static void stopMode(Mode mode) {
		if (LEDManager.mode == mode)
			LEDManager.mode = Mode.NONE;
	}

	public static void periodic() {
		solid(0, Color.kBlack);

		if (DriverStation.getAlliance().isPresent())
			LEDManager.alliance = DriverStation.getAlliance().get();

		// Update auto state
		if (DriverStation.isDisabled()) {
			LEDManager.auto = true;
		} else {
			lastEnabledAuto = DriverStation.isAutonomous();
			lastEnabledTime = Timer.getFPGATimestamp();
		}

		// Update estop state
		if (DriverStation.isEStopped())
			LEDManager.mode = Mode.ESTOP;

		// Exit during initial cycles
		loopCycleCount += 1;
		if (loopCycleCount < minLoopCycleCount)
			return;

		// Stop loading notifier if running
		loadingNotifier.stop();

		// Select LED mode
		solid(Section.FULL, Color.kBlack); // Default to off
		if (LEDManager.mode == Mode.ESTOP) {
			solid(Section.FULL, Color.kDarkRed);
		} else if (DriverStation.isDisabled()) {
			if (lastEnabledAuto && Timer.getFPGATimestamp() - lastEnabledTime < autoFadeMaxTime) {
				// Auto fade
				solid(1.0 - ((Timer.getFPGATimestamp() - lastEnabledTime) / autoFadeTime), Color.kGreen);
			} else {
				// Default pattern
				switch (alliance) {
					case Red:
						wave(
								Section.FULL,
								Color.kRed,
								Color.kBlack,
								waveAllianceCycleLength,
								waveAllianceDuration);
						break;
					case Blue:
						wave(
								Section.FULL,
								Color.kFirstBlue,
								Color.kBlack,
								waveAllianceCycleLength,
								waveAllianceDuration);
						break;
					default:
						wave(Section.FULL, Color.kWhite, Color.kBlack, 1, waveSlowDuration);
						break;
				}
			}
		} else if (DriverStation.isAutonomous()) {
			wave(Section.FULL, Color.kWhite, LEDManager.alliance == Alliance.Red ? Color.kRed : Color.kFirstBlue,
					waveFastCycleLength, waveFastDuration);

			if (!LEDManager.auto) {
				double fullTime = (double) length / waveFastCycleLength * waveFastDuration;
				solid((Timer.getFPGATimestamp() - autoFinishedTime) / fullTime, Color.kGreen);
			}
		}

		switch (LEDManager.mode) {
			case AUTO_ALIGN_CORAL -> rainbow(Section.FULL, rainbowCycleLength, rainbowDuration);
			case INTAKE_ALGAE -> breath(Section.FULL, Color.kGreen, Color.kBlack, 0.5);
			case STALL_ALGAE -> breath(Section.FULL, Color.kGreen, Color.kYellow, 0.8);
			case ESTOP -> strobe(Section.FULL, Color.kRed, strobeFastDuration);
			case INTAKE_CORAL ->
				breath(Section.FULL, Color.kBlack, Color.kWhite, 0.1, System.currentTimeMillis() / 1000.0);
			case EJECT_CORAL ->
				breath(Section.FULL, Color.kWhite, Color.kBlack, 0.1, System.currentTimeMillis() / 1000.0);
			default -> {
			}
		}

		// Update LEDs
		leds.setData(buffer);
	}

	private static void solid(Section section, Color color) {
		if (color != null) {
			for (int i = section.start(); i < section.end(); i++) {
				buffer.setLED(i, color);
			}
		}
	}

	private static void solid(double percent, Color color) {
		for (int i = 0; i < MathUtil.clamp(length * percent, 0, length); i++) {
			buffer.setLED(i, color);
		}
	}

	private static void strobe(Section section, Color color, double duration) {
		boolean on = ((Timer.getFPGATimestamp() % duration) / duration) > 0.5;
		solid(section, on ? color : Color.kBlack);
	}

	private static void breath(Section section, Color c1, Color c2, double duration) {
		breath(section, c1, c2, duration, Timer.getFPGATimestamp());
	}

	private static void breath(Section section, Color c1, Color c2, double duration, double timestamp) {
		double x = ((timestamp % breathDuration) / breathDuration) * 2.0 * Math.PI;
		double ratio = (Math.sin(x) + 1.0) / 2.0;
		double red = (c1.red * (1 - ratio)) + (c2.red * ratio);
		double green = (c1.green * (1 - ratio)) + (c2.green * ratio);
		double blue = (c1.blue * (1 - ratio)) + (c2.blue * ratio);
		solid(section, new Color(red, green, blue));
	}

	private static void rainbow(Section section, double cycleLength, double duration) {
		double x = (1 - ((Timer.getFPGATimestamp() / duration) % 1.0)) * 180.0;
		double xDiffPerLed = 180.0 / cycleLength;
		for (int i = 0; i < section.end(); i++) {
			x += xDiffPerLed;
			x %= 180.0;
			if (i >= section.start()) {
				buffer.setHSV(i, (int) x, 255, 255);
			}
		}
	}

	private static void wave(Section section, Color c1, Color c2, double cycleLength, double duration) {
		double x = (1 - ((Timer.getFPGATimestamp() % duration) / duration)) * 2.0 * Math.PI;
		double xDiffPerLed = (2.0 * Math.PI) / cycleLength;
		for (int i = 0; i < section.end(); i++) {
			x += xDiffPerLed;
			if (i >= section.start()) {
				double ratio = (Math.pow(Math.sin(x), waveExponent) + 1.0) / 2.0;
				if (Double.isNaN(ratio)) {
					ratio = (-Math.pow(Math.sin(x + Math.PI), waveExponent) + 1.0) / 2.0;
				}
				if (Double.isNaN(ratio)) {
					ratio = 0.5;
				}
				double red = (c1.red * (1 - ratio)) + (c2.red * ratio);
				double green = (c1.green * (1 - ratio)) + (c2.green * ratio);
				double blue = (c1.blue * (1 - ratio)) + (c2.blue * ratio);
				buffer.setLED(i, new Color(red, green, blue));
			}
		}
	}

	private static void stripes(Section section, List<Color> colors, int length, double duration) {
		int offset = (int) (Timer.getFPGATimestamp() % duration / duration * length * colors.size());
		for (int i = section.start(); i < section.end(); i++) {
			int colorIndex = (int) (Math.floor((double) (i - offset) / length) + colors.size()) % colors.size();
			colorIndex = colors.size() - 1 - colorIndex;
			buffer.setLED(i, colors.get(colorIndex));
		}
	}

	private static enum Section {
		FULL;

		private int start() {
			switch (this) {
				default:
					return 0;
			}
		}

		private int end() {
			return length;
		}
	}
}