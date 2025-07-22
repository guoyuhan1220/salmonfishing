import SwiftUI

// MARK: - Large Touch Target Button
struct LargeTouchButton<Label: View>: View {
    var action: () -> Void
    var label: () -> Label
    
    init(action: @escaping () -> Void, @ViewBuilder label: @escaping () -> Label) {
        self.action = action
        self.label = label
    }
    
    var body: some View {
        Button(action: action) {
            label()
                .frame(minWidth: 44, minHeight: 44) // Apple's recommended minimum touch target size
                .contentShape(Rectangle()) // Makes the entire area tappable
        }
    }
}

// MARK: - One-Handed Operation Mode
struct OneHandedModeModifier: ViewModifier {
    @Binding var isOneHandedModeEnabled: Bool
    @State private var isRightHanded: Bool = true
    
    func body(content: Content) -> some View {
        GeometryReader { geometry in
            if isOneHandedModeEnabled {
                VStack {
                    content
                        .frame(maxWidth: geometry.size.width * 0.85)
                        .frame(maxWidth: .infinity, alignment: isRightHanded ? .trailing : .leading)
                        .padding(.horizontal)
                    
                    // Hand preference toggle
                    HStack {
                        Text("Left")
                            .foregroundColor(isRightHanded ? .gray : .blue)
                        
                        Toggle("", isOn: $isRightHanded)
                            .labelsHidden()
                        
                        Text("Right")
                            .foregroundColor(isRightHanded ? .blue : .gray)
                    }
                    .padding(.horizontal, 40)
                    .padding(.vertical, 8)
                    .background(Color(.systemGray6))
                    .cornerRadius(20)
                    .padding(.bottom, 8)
                }
            } else {
                content
            }
        }
    }
}

// MARK: - Responsive Layout
struct ResponsiveLayoutModifier: ViewModifier {
    @Environment(\.horizontalSizeClass) var horizontalSizeClass
    
    func body(content: Content) -> some View {
        Group {
            if horizontalSizeClass == .regular {
                // iPad or landscape iPhone
                content
                    .padding(.horizontal, 20)
                    .frame(maxWidth: 800)
                    .frame(maxWidth: .infinity)
            } else {
                // Portrait iPhone
                content
                    .padding(.horizontal, 16)
            }
        }
    }
}

// MARK: - Progressive Disclosure
struct ProgressiveDisclosureView<Content: View>: View {
    var title: String
    var summary: String
    var content: () -> Content
    @State private var isExpanded: Bool = false
    
    init(title: String, summary: String, @ViewBuilder content: @escaping () -> Content) {
        self.title = title
        self.summary = summary
        self.content = content
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Button(action: {
                withAnimation {
                    isExpanded.toggle()
                }
            }) {
                HStack {
                    VStack(alignment: .leading, spacing: 4) {
                        Text(title)
                            .font(.headline)
                        
                        if !isExpanded {
                            Text(summary)
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }
                    
                    Spacer()
                    
                    Image(systemName: isExpanded ? "chevron.up" : "chevron.down")
                        .foregroundColor(.blue)
                }
                .padding()
                .background(Color(.systemBackground))
                .cornerRadius(10)
                .shadow(radius: 2)
            }
            .buttonStyle(PlainButtonStyle())
            
            if isExpanded {
                content()
                    .padding(.horizontal)
                    .transition(.opacity.combined(with: .move(edge: .top)))
            }
        }
    }
}

// MARK: - Extensions
extension View {
    func withLargeTouchTarget() -> some View {
        self.frame(minWidth: 44, minHeight: 44)
            .contentShape(Rectangle())
    }
    
    func oneHandedMode(enabled: Binding<Bool>) -> some View {
        self.modifier(OneHandedModeModifier(isOneHandedModeEnabled: enabled))
    }
    
    func responsiveLayout() -> some View {
        self.modifier(ResponsiveLayoutModifier())
    }
}

// MARK: - Preview
struct MobileOptimizedComponents_Previews: PreviewProvider {
    static var previews: some View {
        VStack(spacing: 20) {
            LargeTouchButton(action: {
                print("Button tapped")
            }) {
                Text("Large Touch Button")
                    .padding()
                    .background(Color.blue)
                    .foregroundColor(.white)
                    .cornerRadius(10)
            }
            
            ProgressiveDisclosureView(
                title: "Weather Details",
                summary: "Tap to see more information"
            ) {
                VStack(alignment: .leading, spacing: 10) {
                    Text("Temperature: 72°F")
                    Text("Wind: 5 mph NW")
                    Text("Humidity: 65%")
                    Text("Pressure: 1013 hPa")
                }
                .padding()
                .background(Color(.systemGray6))
                .cornerRadius(10)
            }
        }
        .padding()
    }
}