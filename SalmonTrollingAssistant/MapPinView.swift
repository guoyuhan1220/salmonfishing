import SwiftUI

struct MapPinView: View {
    let pin: SimpleLocation
    let isSelected: Bool
    let isPulsing: Bool
    
    var body: some View {
        VStack {
            ZStack {
                // Pulsing circle for selected pin
                if isSelected {
                    Circle()
                        .fill(Color.blue.opacity(0.3))
                        .frame(width: isPulsing ? 60 : 40, height: isPulsing ? 60 : 40)
                        .animation(Animation.easeInOut(duration: 1.0).repeatForever(autoreverses: true), value: isPulsing)
                }
                
                // Salmon icon instead of standard pin
                Image(systemName: "fish.fill")
                    .font(.title)
                    .foregroundColor(.orange)
                    .rotationEffect(.degrees(isSelected ? 15 : 0))
                    .background(
                        Circle()
                            .fill(Color.white)
                            .frame(width: 36, height: 36)
                    )
                    .shadow(radius: 2)
                    .scaleEffect(isSelected ? 1.2 : 1.0)
                    .animation(.spring(), value: isSelected)
            }
            
            Text(pin.name)
                .font(.caption)
                .fontWeight(.medium)
                .padding(5)
                .background(
                    RoundedRectangle(cornerRadius: 8)
                        .fill(Color.white.opacity(0.9))
                        .shadow(radius: 1)
                )
        }
    }
}

struct MapPinView_Previews: PreviewProvider {
    static var previews: some View {
        MapPinView(
            pin: SimpleLocation(id: "1", name: "Puget Sound", latitude: 47.6062, longitude: -122.3321),
            isSelected: true,
            isPulsing: true
        )
        .previewLayout(.sizeThatFits)
        .padding()
    }
}