import XCTest

final class LedgerAnalyticsTests: XCTestCase {
    private var calendar: Calendar!
    private var now: Date!

    override func setUp() {
        super.setUp()
        var gregorian = Calendar(identifier: .gregorian)
        gregorian.timeZone = TimeZone(secondsFromGMT: 0)!
        calendar = gregorian
        now = date(2026, 11, 16, 12)
    }

    func testEmptySearchKeepsEveryEntryInTheSelectedRange() {
        let summary = LedgerAnalytics.summarize(
            entries: [entry("a", title: "Coffee", amount: 3, day: 16), entry("b", title: "Rent", amount: 10, day: 2)],
            filter: .all,
            selectedCategoryID: nil,
            searchText: "   ",
            now: now,
            calendar: calendar
        )
        XCTAssertEqual(summary.entries.map(\.id), ["a", "b"])
        XCTAssertEqual(summary.totalAmount, 13, accuracy: 0.001)
    }

    func testThisMonthDropsOtherMonthsAndLastSevenDaysUsesARollingWindow() {
        let olderThanWindow = entry("old", title: "Old", amount: 1, day: 8, hour: 11)
        let onBoundary = entry("edge", title: "Edge", amount: 2, day: 9, hour: 12)
        let thisMonth = entry("now", title: "Now", amount: 4, day: 16)
        let lastMonth = entry("oct", title: "October", amount: 8, month: 10, day: 20)
        let entries = [olderThanWindow, onBoundary, thisMonth, lastMonth]

        let month = LedgerAnalytics.summarize(
            entries: entries,
            filter: .thisMonth,
            selectedCategoryID: nil,
            searchText: "",
            now: now,
            calendar: calendar
        )
        XCTAssertEqual(month.entries.map(\.id), ["old", "edge", "now"])

        let week = LedgerAnalytics.summarize(
            entries: entries,
            filter: .last7Days,
            selectedCategoryID: nil,
            searchText: "",
            now: now,
            calendar: calendar
        )
        XCTAssertEqual(week.entries.map(\.id), ["edge", "now"])
    }

    func testCategoryFilterThenCaseInsensitiveSearch() {
        let food = entry("food", title: "Coffee", amount: 3, categoryID: "food-id", categoryName: "Food")
        let note = entry("note", title: "Taxi", amount: 9, note: "Airport", categoryID: "travel-id", categoryName: "Transport")
        let other = entry("other", title: "Rent", amount: 100, categoryID: "bills-id", categoryName: "Bills")

        let foodOnly = LedgerAnalytics.applyFilters(
            entries: [food, note, other],
            filter: .all,
            selectedCategoryID: "food-id",
            searchText: "COFF",
            now: now,
            calendar: calendar
        )
        XCTAssertEqual(foodOnly.map(\.id), ["food"])

        let byNote = LedgerAnalytics.applyFilters(
            entries: [food, note, other],
            filter: .all,
            selectedCategoryID: nil,
            searchText: "air",
            now: now,
            calendar: calendar
        )
        XCTAssertEqual(byNote.map(\.id), ["note"])

        let byCategoryName = LedgerAnalytics.applyFilters(
            entries: [food, note, other],
            filter: .all,
            selectedCategoryID: nil,
            searchText: "bills",
            now: now,
            calendar: calendar
        )
        XCTAssertEqual(byCategoryName.map(\.id), ["other"])
    }

    func testCategoryTotalsSortDescendingAndNameMissingCategories() {
        let totals = LedgerAnalytics.categoryTotals(for: [
            entry("1", title: "A", amount: 5, categoryName: "Food", symbol: "fork.knife", hex: "#FF6B6B"),
            entry("2", title: "B", amount: 1, categoryName: "Food", symbol: "fork.knife", hex: "#FF6B6B"),
            entry("3", title: "C", amount: 20, categoryName: "Rent", symbol: "house", hex: "#111111"),
            entry("4", title: "D", amount: 2, categoryName: nil, symbol: nil, hex: nil)
        ])

        XCTAssertEqual(totals.map(\.name), ["Rent", "Food", "Uncategorized"])
        XCTAssertEqual(totals.map(\.total), [20.0, 6.0, 2.0])
        XCTAssertEqual(totals[2].symbol, "questionmark.circle")
        XCTAssertEqual(totals[2].colorHex, "#95A5A6")
    }

    func testCSVSortsNewestFirstAndQuotesCommasQuotesAndNewlines() {
        let older = entry("old", title: "Plain", amount: 1.5, day: 1, categoryName: "Food")
        let newer = entry(
            "new",
            title: "Cafe, \"Main\"\nSt",
            amount: 12,
            day: 16,
            note: "line\n2",
            categoryName: nil
        )
        let csv = LedgerCSV.render([older, newer]) { date in
            isoDate(date)
        }
        let small = String(format: "%.2f", 1.5)
        let large = String(format: "%.2f", 12.0)
        let expected = """
        id,date,title,amount,category,note
        new,\(isoDate(newer.date)),"Cafe, ""Main""\nSt",\(large),Uncategorized,"line\n2"
        old,\(isoDate(older.date)),Plain,\(small),Food,
        """
        XCTAssertEqual(csv, expected)
    }

    func testEmptyCSVIsHeaderOnly() {
        XCTAssertEqual(
            LedgerCSV.render([], formatDate: { _ in "" }),
            "id,date,title,amount,category,note"
        )
    }

    private func entry(
        _ id: String,
        title: String,
        amount: Double,
        month: Int = 11,
        day: Int,
        hour: Int = 12,
        note: String = "",
        categoryID: String? = "cat",
        categoryName: String? = "Food",
        symbol: String? = "fork.knife",
        hex: String? = "#FF6B6B"
    ) -> LedgerEntry {
        LedgerEntry(
            id: id,
            title: title,
            amount: amount,
            date: date(2026, month, day, hour),
            note: note,
            categoryID: categoryID,
            categoryName: categoryName,
            categorySymbol: symbol,
            categoryColorHex: hex
        )
    }

    private func date(_ year: Int, _ month: Int, _ day: Int, _ hour: Int) -> Date {
        calendar.date(from: DateComponents(year: year, month: month, day: day, hour: hour, minute: 0, second: 0))!
    }

    private func isoDate(_ date: Date) -> String {
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withFullDate]
        formatter.timeZone = TimeZone(secondsFromGMT: 0)
        return formatter.string(from: date)
    }
}
