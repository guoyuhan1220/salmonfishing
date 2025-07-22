import SwiftUI

// MARK: - Pull to Refresh Component
struct PullToRefreshView<Content: View>: View {
    var content: Content
    var onRefresh: () -> Void
    
    @State private var isRefreshing = false
    
    init(onRefresh: @escaping () -> Void, @ViewBuilder content: () -> Content) {
        self.onRefresh = onRefresh
        self.content = content()
    }
    
    var body: some View {
        if #available(iOS 15.0, *) {
            ScrollView {
                content
                    .refreshable {
                        isRefreshing = true
                        onRefresh()
                        // In a real app, we would wait for the refresh to complete
                        DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
                            isRefreshing = false
                        }
                    }
            }
        } else {
            // For iOS 14 and earlier, we would implement a custom pull to refresh
            // This is a simplified version
            ZStack(alignment: .top) {
                ScrollView {
                    VStack {
                        if isRefreshing {
                            ProgressView()
                                .padding()
                        }
                        
                        content
                    }
                }
                
                // Custom pull to refresh indicator would go here
            }
        }
    }
}

// MARK: - Swipe Navigation
struct SwipeNavigationView<Content: View>: View {
    var content: Content
    var onSwipeLeft: () -> Void
    var onSwipeRight: () -> Void
    
    init(onSwipeLeft: @escaping () -> Void, onSwipeRight: @escaping () -> Void, @ViewBuilder content: () -> Content) {
        self.onSwipeLeft = onSwipeLeft
        self.onSwipeRight = onSwipeRight
        self.content = content()
    }
    
    var body: some View {
        content
            .gesture(
                DragGesture(minimumDistance: 50, coordinateSpace: .local)
                    .onEnded { value in
                        if value.translation.width < 0 {
                            // Swiped left
                            onSwipeLeft()
                        } else if value.translation.width > 0 {
                            // Swiped right
                            onSwipeRight()
                        }
                    }
            )
    }
}

// MARK: - Pinch to Zoom
struct PinchToZoomView<Content: View>: View {
    var content: Content
    @State private var scale: CGFloat = 1.0
    @State private var lastScale: CGFloat = 1.0
    @State private var offset: CGSize = .zero
    @State private var lastOffset: CGSize = .zero
    
    init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }
    
    var body: some View {
        GeometryReader { geometry in
            content
                .scaleEffect(scale)
                .offset(x: offset.width, y: offset.height)
                .gesture(
                    MagnificationGesture()
                        .onChanged { value in
                            let delta = value / lastScale
                            lastScale = value
                            
                            // Limit the scale
                            let newScale = scale * delta
                            scale = min(max(newScale, 1.0), 3.0)
                        }
                        .onEnded { _ in
                            lastScale = 1.0
                            
                            // Reset to original size if scale is close to 1
                            if scale < 1.1 {
                                withAnimation {
                                    scale = 1.0
                                    offset = .zero
                                }
                            }
                        }
                )
                .simultaneousGesture(
                    DragGesture()
                        .onChanged { value in
                            // Only allow dragging when zoomed in
                            if scale > 1.0 {
                                let newOffset = CGSize(
                                    width: lastOffset.width + value.translation.width,
                                    height: lastOffset.height + value.translation.height
                                )
                                
                                // Limit the offset based on the zoom level
                                let maxOffsetX = (geometry.size.width * (scale - 1)) / 2
                                let maxOffsetY = (geometry.size.height * (scale - 1)) / 2
                                
                                offset = CGSize(
                                    width: min(maxOffsetX, max(-maxOffsetX, newOffset.width)),
                                    height: min(maxOffsetY, max(-maxOffsetY, newOffset.height))
                                )
                            }
                        }
                        .onEnded { _ in
                            lastOffset = offset
                            
                            // Reset offset if scale is reset
                            if scale <= 1.0 {
                                withAnimation {
                                    offset = .zero
                                }
                            }
                        }
                )
                .animation(.spring(), value: scale <= 1.0)
        }
    }
}

// MARK: - Double Tap to Zoom
struct DoubleTapToZoomView<Content: View>: View {
    var content: Content
    @State private var scale: CGFloat = 1.0
    
    init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }
    
    var body: some View {
        content
            .scaleEffect(scale)
            .onTapGesture(count: 2) {
                withAnimation {
                    scale = scale == 1.0 ? 2.0 : 1.0
                }
            }
    }
}

// MARK: - Extensions
extension View {
    func pullToRefresh(onRefresh: @escaping () -> Void) -> some View {
        PullToRefreshView(onRefresh: onRefresh) {
            self
        }
    }
    
    func swipeNavigation(onSwipeLeft: @escaping () -> Void, onSwipeRight: @escaping () -> Void) -> some View {
        SwipeNavigationView(onSwipeLeft: onSwipeLeft, onSwipeRight: onSwipeRight) {
            self
        }
    }
    
    func pinchToZoom() -> some View {
        PinchToZoomView {
            self
        }
    }
    
    func doubleTapToZoom() -> some View {
        DoubleTapToZoomView {
            self
        }
    }
}