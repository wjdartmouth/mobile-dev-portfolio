import WidgetKit
import SwiftUI

@main
struct FocusTimerWidgetsBundle: WidgetBundle {
    var body: some Widget {
        FocusHomeWidget()
        FocusLockScreenWidget()
        FocusLiveActivity()
    }
}
