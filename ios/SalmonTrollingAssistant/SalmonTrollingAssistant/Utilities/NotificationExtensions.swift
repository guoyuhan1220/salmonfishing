import Foundation

extension Notification.Name {
    static let switchToTab = Notification.Name("switchToTab")
}

struct TabNotification {
    static func postSwitchToTab(index: Int) {
        NotificationCenter.default.post(
            name: .switchToTab,
            object: nil,
            userInfo: ["tabIndex": index]
        )
    }
}