//
// Created by Sultan on 25/04/2026.
//

import Foundation
import UserNotifications
import UIKit

@objc class NotificationHelper: NSObject {
    @objc static func requestPermission() {
        UNUserNotificationCenter.current().requestAuthorization(
            options: [.alert, .sound, .badge]
        ) { granted, _ in
            if granted {
                DispatchQueue.main.async {
                    UIApplication.shared.registerForRemoteNotifications()
                }
            }
        }
    }
}