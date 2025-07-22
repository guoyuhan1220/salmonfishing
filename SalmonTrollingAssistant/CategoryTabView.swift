import SwiftUI

struct CategoryTabView: View {
    let category: String
    let isSelected: Bool
    
    var body: some View {
        Text(category)
            .font(.subheadline)
            .fontWeight(.medium)
            .padding(.vertical, 8)
            .padding(.horizontal, 16)
            .background(
                Capsule()
                    .fill(backgroundFill)
            )
            .foregroundColor(isSelected ? .white : .primary)
    }
    
    private var backgroundFill: some ShapeStyle {
        if isSelected {
            return LinearGradient(
                gradient: Gradient(colors: [Color.blue, Color(red: 0.0, green: 0.5, blue: 0.8)]),
                startPoint: .leading,
                endPoint: .trailing
            )
        } else {
            return Color.gray.opacity(0.2)
        }
    }
}

struct CategoryTabView_Previews: PreviewProvider {
    static var previews: some View {
        HStack {
            CategoryTabView(category: "All Spots", isSelected: true)
            CategoryTabView(category: "Favorites", isSelected: false)
        }
        .padding()
        .previewLayout(.sizeThatFits)
    }
}