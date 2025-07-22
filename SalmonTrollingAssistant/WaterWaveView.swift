import SwiftUI

struct WaterWaveView: View {
    @Binding var waveOffset: Double
    @State private var phase = 0.0
    
    var body: some View {
        VStack {
            Spacer()
            
            ZStack {
                // First wave
                Wave(offset: waveOffset, percent: 0.05, strength: 30)
                    .fill(Color.blue.opacity(0.3))
                    .frame(height: 100)
                
                // Second wave
                Wave(offset: waveOffset + 0.5, percent: 0.05, strength: 40)
                    .fill(Color.blue.opacity(0.2))
                    .frame(height: 100)
            }
            .onAppear {
                withAnimation(Animation.linear(duration: 5).repeatForever(autoreverses: false)) {
                    waveOffset += 1.0
                }
            }
        }
    }
}

struct Wave: Shape {
    var offset: Double
    var percent: Double
    var strength: Double
    
    func path(in rect: CGRect) -> Path {
        var path = Path()
        
        // Move to the starting point (bottom left)
        path.move(to: CGPoint(x: 0, y: rect.height))
        
        // Calculate the wave
        let width = rect.width
        let height = rect.height
        let midHeight = height * (1 - CGFloat(percent))
        
        // Draw the wave
        for x in stride(from: 0, to: width, by: 1) {
            let relativeX = x / width
            let sine = sin(relativeX * .pi * 4 + CGFloat(offset) * .pi * 2)
            let y = midHeight + CGFloat(strength) * sine
            path.addLine(to: CGPoint(x: x, y: y))
        }
        
        // Complete the path
        path.addLine(to: CGPoint(x: width, y: height))
        path.addLine(to: CGPoint(x: 0, y: height))
        path.closeSubpath()
        
        return path
    }
    
    var animatableData: Double {
        get { offset }
        set { offset = newValue }
    }
}

struct WaterWaveView_Previews: PreviewProvider {
    static var previews: some View {
        WaterWaveView(waveOffset: .constant(0.0))
    }
}