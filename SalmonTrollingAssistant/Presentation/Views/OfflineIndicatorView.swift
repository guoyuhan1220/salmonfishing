import SwiftUI

/**
 * A banner that displays when the app is in offline mode
 */
struct OfflineBanner: View {
    let isOffline: Bool
    let onRefreshClick: () -> Void
    
    @State private var isVisible = false
    
    var body: some View {
        if isOffline {
            ZStack {
                RoundedRectangle(cornerRadius: 8)
                    .fill(Color.red.opacity(0.2))
                
                HStack {
                    Image(systemName: "cloud.slash")
                        .foregroundColor(.red)
                    
                    Text("You're offline. Using cached data.")
                        .foregroundColor(.red)
                    
                    Spacer()
                    
                    Button(action: onRefreshClick) {
                        Image(systemName: "arrow.clockwise")
                            .foregroundColor(.blue)
                            .padding(8)
                            .background(Color.blue.opacity(0.1))
                            .clipShape(Circle())
                    }
                }
                .padding()
            }
            .frame(maxWidth: .infinity)
            .frame(height: 60)
            .padding(.horizontal)
            .opacity(isVisible ? 1 : 0)
            .animation(.easeInOut(duration: 0.3), value: isVisible)
            .onAppear {
                isVisible = true
            }
        } else {
            EmptyView()
        }
    }
}

/**
 * A small indicator that shows the current connection status
 */
struct ConnectionStatusIndicator: View {
    let isOffline: Bool
    let syncStatus: OfflineDataManager.SyncStatus
    
    @State private var rotation: Double = 0
    
    var body: some View {
        HStack(spacing: 4) {
            // Status indicator dot
            Circle()
                .fill(statusColor)
                .frame(width: 8, height: 8)
            
            // Status text
            Text(statusText)
                .font(.caption)
                .foregroundColor(.secondary)
            
            // Show sync animation if syncing
            if syncStatus == .syncing {
                Image(systemName: "arrow.triangle.2.circlepath")
                    .font(.caption)
                    .foregroundColor(.yellow)
                    .rotationEffect(.degrees(rotation))
                    .onAppear {
                        withAnimation(Animation.linear(duration: 1).repeatForever(autoreverses: false)) {
                            rotation = 360
                        }
                    }
            }
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 4)
        .background(Color.secondary.opacity(0.1))
        .cornerRadius(12)
    }
    
    private var statusColor: Color {
        switch (isOffline, syncStatus) {
        case (true, _):
            return .red
        case (false, .syncing):
            return .yellow
        case (false, .synced):
            return .green
        default:
            return .red
        }
    }
    
    private var statusText: String {
        switch (isOffline, syncStatus) {
        case (true, _):
            return "Offline"
        case (false, .syncing):
            return "Syncing..."
        case (false, .synced):
            return "Online"
        default:
            return "Connection Error"
        }
    }
}

/**
 * A component that displays data freshness information
 */
struct DataFreshnessIndicator: View {
    let freshnessPercentage: Int
    let expirationTime: Date?
    
    private var freshnessColor: Color {
        switch freshnessPercentage {
        case 71...100:
            return .green
        case 31...70:
            return .yellow
        default:
            return .red
        }
    }
    
    private var backgroundColor: Color {
        switch freshnessPercentage {
        case 71...100:
            return .green.opacity(0.2)
        case 31...70:
            return .yellow.opacity(0.2)
        default:
            return .red.opacity(0.2)
        }
    }
    
    var body: some View {
        VStack(alignment: .center, spacing: 8) {
            Text("Data Freshness")
                .font(.headline)
                .foregroundColor(freshnessColor)
            
            GeometryReader { geometry in
                ZStack(alignment: .leading) {
                    Rectangle()
                        .fill(Color.secondary.opacity(0.2))
                        .frame(width: geometry.size.width, height: 8)
                        .cornerRadius(4)
                    
                    Rectangle()
                        .fill(freshnessColor)
                        .frame(width: geometry.size.width * CGFloat(freshnessPercentage) / 100, height: 8)
                        .cornerRadius(4)
                }
            }
            .frame(height: 8)
            
            HStack {
                if freshnessPercentage < 30 {
                    Image(systemName: "exclamationmark.triangle")
                        .foregroundColor(.red)
                        .font(.caption)
                }
                
                Text("\(freshnessPercentage)% Fresh")
                    .font(.subheadline)
                    .foregroundColor(freshnessColor)
            }
            
            if let expirationTime = expirationTime {
                let dateFormatter = DateFormatter()
                dateFormatter.dateFormat = "MMM d, h:mm a"
                
                Text("Data expires: \(dateFormatter.string(from: expirationTime))")
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
            }
        }
        .padding()
        .background(backgroundColor)
        .cornerRadius(8)
        .padding(.horizontal)
    }
}

/**
 * A dialog that appears when the user is offline and tries to perform an action that requires connectivity
 */
struct OfflineActionDialog: View {
    let isVisible: Bool
    let onDismiss: () -> Void
    let onGoOnline: () -> Void
    let actionDescription: String
    
    @State private var offset: CGFloat = 1000
    
    var body: some View {
        if isVisible {
            ZStack {
                Color.black.opacity(0.4)
                    .edgesIgnoringSafeArea(.all)
                    .onTapGesture {
                        withAnimation {
                            offset = 1000
                            DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
                                onDismiss()
                            }
                        }
                    }
                
                VStack(spacing: 16) {
                    Image(systemName: "wifi.slash")
                        .font(.system(size: 48))
                        .foregroundColor(.red)
                    
                    Text("You're Offline")
                        .font(.title2)
                        .fontWeight(.bold)
                    
                    Text("Cannot \(actionDescription) while offline. Please connect to the internet and try again.")
                        .multilineTextAlignment(.center)
                        .font(.body)
                    
                    HStack(spacing: 16) {
                        Button(action: {
                            withAnimation {
                                offset = 1000
                                DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
                                    onDismiss()
                                }
                            }
                        }) {
                            Text("Stay Offline")
                                .frame(maxWidth: .infinity)
                                .padding()
                                .background(Color.secondary.opacity(0.2))
                                .foregroundColor(.primary)
                                .cornerRadius(8)
                        }
                        
                        Button(action: {
                            withAnimation {
                                offset = 1000
                                DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
                                    onGoOnline()
                                }
                            }
                        }) {
                            Text("Go Online")
                                .frame(maxWidth: .infinity)
                                .padding()
                                .background(Color.blue)
                                .foregroundColor(.white)
                                .cornerRadius(8)
                        }
                    }
                }
                .padding()
                .background(Color(UIColor.systemBackground))
                .cornerRadius(16)
                .shadow(radius: 10)
                .padding(.horizontal, 24)
                .offset(y: offset)
                .onAppear {
                    withAnimation(.spring()) {
                        offset = 0
                    }
                }
            }
        } else {
            EmptyView()
        }
    }
}

struct OfflineIndicators_Previews: PreviewProvider {
    static var previews: some View {
        VStack(spacing: 20) {
            OfflineBanner(isOffline: true) {
                print("Refresh clicked")
            }
            
            ConnectionStatusIndicator(isOffline: false, syncStatus: .syncing)
            
            DataFreshnessIndicator(
                freshnessPercentage: 75,
                expirationTime: Date().addingTimeInterval(3600)
            )
            
            Spacer()
        }
        .padding()
        .previewDisplayName("Offline Indicators")
        
        OfflineActionDialog(
            isVisible: true,
            onDismiss: { print("Dismissed") },
            onGoOnline: { print("Go online") },
            actionDescription: "update weather data"
        )
        .previewDisplayName("Offline Dialog")
    }
}