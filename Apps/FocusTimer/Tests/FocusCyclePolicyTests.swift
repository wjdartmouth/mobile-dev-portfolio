import XCTest

final class FocusCyclePolicyTests: XCTestCase {
    func testShortBreakUntilLongBreakCadence() {
        XCTAssertEqual(
            FocusCyclePolicy.nextPhase(after: .focus, completedFocusCount: 1, sessionsUntilLongBreak: 4),
            .shortBreak
        )
        XCTAssertEqual(
            FocusCyclePolicy.nextPhase(after: .focus, completedFocusCount: 2, sessionsUntilLongBreak: 4),
            .shortBreak
        )
        XCTAssertEqual(
            FocusCyclePolicy.nextPhase(after: .focus, completedFocusCount: 3, sessionsUntilLongBreak: 4),
            .shortBreak
        )
    }

    func testLongBreakWhenCompletedFocusCountHitsCadence() {
        XCTAssertEqual(
            FocusCyclePolicy.nextPhase(after: .focus, completedFocusCount: 4, sessionsUntilLongBreak: 4),
            .longBreak
        )
        XCTAssertEqual(
            FocusCyclePolicy.nextPhase(after: .focus, completedFocusCount: 8, sessionsUntilLongBreak: 4),
            .longBreak
        )
    }

    func testZeroCompletedFocusCountStaysOnShortBreak() {
        XCTAssertEqual(
            FocusCyclePolicy.nextPhase(after: .focus, completedFocusCount: 0, sessionsUntilLongBreak: 4),
            .shortBreak
        )
    }

    func testEveryFocusIsALongBreakWhenCadenceIsOne() {
        XCTAssertEqual(
            FocusCyclePolicy.nextPhase(after: .focus, completedFocusCount: 1, sessionsUntilLongBreak: 1),
            .longBreak
        )
    }

    func testAnyBreakReturnsToFocus() {
        XCTAssertEqual(
            FocusCyclePolicy.nextPhase(after: .shortBreak, completedFocusCount: 4, sessionsUntilLongBreak: 4),
            .focus
        )
        XCTAssertEqual(
            FocusCyclePolicy.nextPhase(after: .longBreak, completedFocusCount: 4, sessionsUntilLongBreak: 4),
            .focus
        )
    }
}
