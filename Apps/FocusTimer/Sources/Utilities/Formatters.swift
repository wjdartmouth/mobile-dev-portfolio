import Foundation

enum Formatters {
    static let shortDate: DateFormatter = {
        let f = DateFormatter()
        f.dateStyle = .medium
        f.timeStyle = .none
        return f
    }()

    static let dayMonth: DateFormatter = {
        let f = DateFormatter()
        f.setLocalizedDateFormatFromTemplate("MMMd")
        return f
    }()

    static let time: DateFormatter = {
        let f = DateFormatter()
        f.dateStyle = .none
        f.timeStyle = .short
        return f
    }()

    static func duration(seconds: Int) -> String {
        let m = seconds / 60
        let s = seconds % 60
        if m >= 60 {
            let h = m / 60
            let rm = m % 60
            return String(format: "%dh %02dm", h, rm)
        }
        return String(format: "%d:%02d", m, s)
    }
}
