import XCTest

final class NoteConflictPolicyTests: XCTestCase {
    private let earlier = Date(timeIntervalSince1970: 1_700_000_000)
    private let later = Date(timeIntervalSince1970: 1_700_000_100)

    func testHigherRemoteRevisionWinsEvenIfOlder() {
        let local = NoteVersion(revision: 2, updatedAt: later, isDeleted: false)
        let remote = NoteVersion(revision: 3, updatedAt: earlier, isDeleted: false)
        XCTAssertTrue(NoteConflictPolicy.shouldPreferRemote(remote, over: local))
    }

    func testLowerRemoteRevisionLosesEvenIfNewer() {
        let local = NoteVersion(revision: 4, updatedAt: earlier, isDeleted: false)
        let remote = NoteVersion(revision: 3, updatedAt: later, isDeleted: false)
        XCTAssertFalse(NoteConflictPolicy.shouldPreferRemote(remote, over: local))
    }

    func testEqualRevisionNewerRemoteWins() {
        let local = NoteVersion(revision: 2, updatedAt: earlier, isDeleted: false)
        let remote = NoteVersion(revision: 2, updatedAt: later, isDeleted: false)
        XCTAssertTrue(NoteConflictPolicy.shouldPreferRemote(remote, over: local))
    }

    func testEqualRevisionOlderRemoteLoses() {
        let local = NoteVersion(revision: 2, updatedAt: later, isDeleted: false)
        let remote = NoteVersion(revision: 2, updatedAt: earlier, isDeleted: false)
        XCTAssertFalse(NoteConflictPolicy.shouldPreferRemote(remote, over: local))
    }

    func testEqualRevisionAndTimestampKeepsLocal() {
        let local = NoteVersion(revision: 2, updatedAt: earlier, isDeleted: false)
        let remote = NoteVersion(revision: 2, updatedAt: earlier, isDeleted: true)
        XCTAssertFalse(NoteConflictPolicy.shouldPreferRemote(remote, over: local))
    }

    func testDeletedRemoteIsPreferredWhenRevisionWins() {
        let local = NoteVersion(revision: 2, updatedAt: later, isDeleted: false)
        let remote = NoteVersion(revision: 5, updatedAt: earlier, isDeleted: true)
        XCTAssertTrue(NoteConflictPolicy.shouldPreferRemote(remote, over: local))
    }
}
