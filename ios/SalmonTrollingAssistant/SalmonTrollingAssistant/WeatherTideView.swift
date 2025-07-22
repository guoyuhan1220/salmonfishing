import SwiftUI

struct WeatherTideView: View {
    var body: some View {
        NavigationView {
            VStack {
                Text("Weather & Tide Information")
                    .font(.title)
                    .padding()
                
                Spacer()
                
                Text("This is a placeholder for weather and tide information.")
                    .multilineTextAlignment(.center)
                    .padding()
                
                Spacer()
            }
            .navigationTitle("Weather & Tide")
        }
    }
}

struct WeatherTideView_Previews: PreviewProvider {
    static var previews: some View {
        WeatherTideView()
    }
}