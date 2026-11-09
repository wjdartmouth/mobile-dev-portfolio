import Foundation

enum MoneyFormat {
    static let currency: NumberFormatter = {
        let f = NumberFormatter()
        f.numberStyle = .currency
        f.currencyCode = Locale.current.currency?.identifier ?? "USD"
        return f
    }()

    static func string(from value: Double) -> String {
        currency.string(from: NSNumber(value: value)) ?? String(format: "%.2f", value)
    }
}
