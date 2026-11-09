import Foundation

enum AppGroup {
    static let identifier = "group.com.jianwang.portfolio.focustimer"
    static var defaults: UserDefaults {
        UserDefaults(suiteName: identifier) ?? .standard
    }
}
