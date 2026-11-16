import XCTest

final class TimerMathTests: XCTestCase {
    func testSecondsScaleMinutesBySixty() {
        XCTAssertEqual(
            TimerMath.seconds(for: .focus, focusMinutes: 25, shortBreakMinutes: 5, longBreakMinutes: 15),
            25 * 60
        )
        XCTAssertEqual(
            TimerMath.seconds(for: .shortBreak, focusMinutes: 25, shortBreakMinutes: 5, longBreakMinutes: 15),
            5 * 60
        )
        XCTAssertEqual(
            TimerMath.seconds(for: .longBreak, focusMinutes: 25, shortBreakMinutes: 5, longBreakMinutes: 15),
            15 * 60
        )
    }

    func testZeroMinutePhaseIsZeroSeconds() {
        XCTAssertEqual(
            TimerMath.seconds(for: .focus, focusMinutes: 0, shortBreakMinutes: 5, longBreakMinutes: 15),
            0
        )
    }

    func testDurationFormatting() {
        XCTAssertEqual(TimerMath.duration(seconds: 0), "0:00")
        XCTAssertEqual(TimerMath.duration(seconds: 65), "1:05")
        XCTAssertEqual(TimerMath.duration(seconds: 59), "0:59")
        XCTAssertEqual(TimerMath.duration(seconds: 3600), "1h 00m")
        XCTAssertEqual(TimerMath.duration(seconds: 3660), "1h 01m")
    }
}
