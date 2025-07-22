import SwiftUI
import Combine

/**
 * A view modifier that implements progressive loading of content
 * to improve perceived performance and user experience
 */
struct ProgressiveLoadingModifier<LoadingView: View, ErrorView: View>: ViewModifier {
    var isLoading: Bool
    var hasError: Bool
    var loadingView: () -> LoadingView
    var errorView: () -> ErrorView
    
    @State private var showContent: Bool = false
    @State private var showLoading: Bool = true
    
    func body(content: Content) -> some View {
        ZStack {
            // Main content
            content
                .opacity(showContent ? 1 : 0)
                .animation(.easeInOut(duration: 0.3), value: showContent)
            
            // Loading view
            if showLoading {
                loadingView()
                    .transition(.opacity)
                    .animation(.easeInOut(duration: 0.3), value: showLoading)
            }
            
            // Error view
            if hasError && !showLoading {
                errorView()
                    .transition(.opacity)
                    .animation(.easeInOut(duration: 0.3), value: hasError)
            }
        }
        .onAppear {
            updateState()
        }
        .onChange(of: isLoading) { _ in
            updateState()
        }
        .onChange(of: hasError) { _ in
            updateState()
        }
    }
    
    private func updateState() {
        if isLoading {
            showLoading = true
            showContent = false
        } else if hasError {
            showLoading = false
            showContent = false
        } else {
            // Short delay before showing content for smoother transition
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
                showContent = true
                
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
                    showLoading = false
                }
            }
        }
    }
}

/**
 * A view modifier that implements progressive loading of images
 * with proper caching and optimization
 */
struct ProgressiveImageLoadingModifier: ViewModifier {
    var url: URL?
    var placeholder: Image
    
    @State private var loadedImage: UIImage? = nil
    @State private var isLoading: Bool = true
    
    // Image cache
    private static let imageCache = NSCache<NSURL, UIImage>()
    
    func body(content: Content) -> some View {
        Group {
            if let image = loadedImage {
                Image(uiImage: image)
                    .resizable()
                    .transition(.opacity)
            } else {
                placeholder
                    .resizable()
                    .opacity(isLoading ? 0.3 : 1.0)
            }
        }
        .onAppear {
            loadImage()
        }
        .onChange(of: url) { _ in
            loadImage()
        }
    }
    
    private func loadImage() {
        guard let url = url else {
            isLoading = false
            return
        }
        
        isLoading = true
        
        // Check cache first
        if let cachedImage = Self.imageCache.object(forKey: url as NSURL) {
            loadedImage = cachedImage
            isLoading = false
            return
        }
        
        // Load from network
        URLSession.shared.dataTask(with: url) { data, response, error in
            guard let data = data, error == nil else {
                DispatchQueue.main.async {
                    isLoading = false
                }
                return
            }
            
            // Process image in background
            DispatchQueue.global(qos: .userInitiated).async {
                if let uiImage = UIImage(data: data) {
                    // Resize image if needed to save memory
                    let resizedImage = resizeImageIfNeeded(uiImage)
                    
                    // Cache the image
                    Self.imageCache.setObject(resizedImage, forKey: url as NSURL)
                    
                    DispatchQueue.main.async {
                        withAnimation(.easeInOut(duration: 0.3)) {
                            loadedImage = resizedImage
                            isLoading = false
                        }
                    }
                } else {
                    DispatchQueue.main.async {
                        isLoading = false
                    }
                }
            }
        }.resume()
    }
    
    private func resizeImageIfNeeded(_ image: UIImage) -> UIImage {
        let maxDimension: CGFloat = 1200
        
        // Check if image needs resizing
        if image.size.width <= maxDimension && image.size.height <= maxDimension {
            return image
        }
        
        // Calculate new size
        var newSize: CGSize
        if image.size.width > image.size.height {
            let ratio = maxDimension / image.size.width
            newSize = CGSize(width: maxDimension, height: image.size.height * ratio)
        } else {
            let ratio = maxDimension / image.size.height
            newSize = CGSize(width: image.size.width * ratio, height: maxDimension)
        }
        
        // Resize image
        UIGraphicsBeginImageContextWithOptions(newSize, false, 0.0)
        image.draw(in: CGRect(origin: .zero, size: newSize))
        let resizedImage = UIGraphicsGetImageFromCurrentImageContext() ?? image
        UIGraphicsEndImageContext()
        
        return resizedImage
    }
}

/**
 * A view model for progressive list loading
 */
class ProgressiveListViewModel<T>: ObservableObject {
    @Published var visibleItems: [T] = []
    @Published var isLoading: Bool = true
    
    private var allItems: [T] = []
    private var loadingTask: Task<Void, Never>? = nil
    
    func loadItems(_ items: [T]?, isLoading: Bool) {
        // Cancel any existing loading task
        loadingTask?.cancel()
        
        if isLoading {
            self.isLoading = true
            self.visibleItems = []
        } else if let items = items {
            self.isLoading = false
            
            if items.isEmpty {
                self.visibleItems = []
                return
            }
            
            self.allItems = items
            
            // Start progressive loading
            loadingTask = Task { [weak self] in
                guard let self = self else { return }
                
                // For small lists, load all at once
                if items.count <= 5 {
                    await MainActor.run {
                        self.visibleItems = items
                    }
                    return
                }
                
                // For larger lists, load in batches
                let initialBatch = Array(items.prefix(5))
                
                await MainActor.run {
                    self.visibleItems = initialBatch
                }
                
                // Load remaining items in batches
                let remainingItems = Array(items.dropFirst(5))
                let batchSize = 5
                
                for i in stride(from: 0, to: remainingItems.count, by: batchSize) {
                    if Task.isCancelled { return }
                    
                    let endIndex = min(i + batchSize, remainingItems.count)
                    let batch = Array(remainingItems[i..<endIndex])
                    
                    // Small delay between batches
                    try? await Task.sleep(nanoseconds: 100_000_000) // 100ms
                    
                    if Task.isCancelled { return }
                    
                    await MainActor.run {
                        self.visibleItems.append(contentsOf: batch)
                    }
                }
            }
        } else {
            self.isLoading = false
            self.visibleItems = []
        }
    }
    
    deinit {
        loadingTask?.cancel()
    }
}

// MARK: - View Extensions

extension View {
    /**
     * Apply progressive loading to a view
     */
    func progressiveLoading<LoadingView: View, ErrorView: View>(
        isLoading: Bool,
        hasError: Bool = false,
        @ViewBuilder loadingView: @escaping () -> LoadingView,
        @ViewBuilder errorView: @escaping () -> ErrorView
    ) -> some View {
        self.modifier(ProgressiveLoadingModifier(
            isLoading: isLoading,
            hasError: hasError,
            loadingView: loadingView,
            errorView: errorView
        ))
    }
    
    /**
     * Apply progressive loading to a view with default loading and error views
     */
    func progressiveLoading(
        isLoading: Bool,
        hasError: Bool = false
    ) -> some View {
        self.progressiveLoading(
            isLoading: isLoading,
            hasError: hasError,
            loadingView: {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            },
            errorView: {
                Text("Unable to load content. Please try again.")
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
        )
    }
    
    /**
     * Apply progressive image loading to an image
     */
    func progressiveImageLoading(
        url: URL?,
        placeholder: Image = Image(systemName: "photo")
    ) -> some View {
        self.modifier(ProgressiveImageLoadingModifier(
            url: url,
            placeholder: placeholder
        ))
    }
}