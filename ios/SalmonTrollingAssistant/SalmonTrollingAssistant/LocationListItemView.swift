import SwiftUI

struct LocationListItemView: View {
    let pin: SimpleLocation
    let isSelected: Bool
    
    var body: some View {
        HStack {
            // Salmon icon with water background
            ZStack {
                Circle()
                    .fill(
                        LinearGradient(
                            gradient: Gradient(colors: [
                                Color(red: 0.1, green: 0.4, blue: 0.8),
                                Color(red: 0.0, green: 0.2, blue: 0.5)
                            ]),
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .frame(width: 50, height: 50)
                
                Image(systemName: "fish.fill")
                    .font(.system(size: 24))
                    .foregroundColor(.white)
                    .rotationEffect(.degrees(isSelected ? 15 : 0))
                    .scaleEffect(isSelected ? 1.2 : 1.0)
                    .animation(.spring(), value: isSelected)
            }
            .padding(.trailing, 8)
            
            VStack(alignment: .leading, spacing: 4) {
                Text(pin.name)
                    .font(.headline)
                    .fontWeight(.semibold)
                
                HStack {
                    Image(systemName: "mappin")
                        .foregroundColor(.orange)
                        .font(.caption)
                    
                    Text("\(pin.latitude.formatted(.number.precision(.fractionLength(2)))), \(pin.longitude.formatted(.number.precision(.fractionLength(2))))")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                
                HStack {
                    Image(systemName: "cloud.sun.fill")
                        .foregroundColor(.blue)
                        .font(.caption)
                    
                    Text("Tap for forecast & recommendations")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }
        }
    }
}

struct LocationListItemView_Previews: PreviewProvider {
    static var previews: some View {
        LocationListItemView(
            pin: SimpleLocation(id: "1", name: "Puget Sound", latitude: 47.6062, longitude: -122.3321),
            isSelected: false
        )
        .previewLayout(.sizeThatFits)
        .padding()
    }
}