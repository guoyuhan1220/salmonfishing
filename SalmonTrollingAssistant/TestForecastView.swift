import SwiftUI

struct TestForecastView: View {
    let location: SimpleLocation
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        NavigationView {
            VStack(spacing: 20) {
                Text("Test Forecast View")
                    .font(.largeTitle)
                    .padding()
                
                Text("Location: \(location.name)")
                    .font(.title)
                
                Text("Latitude: \(location.latitude)")
                Text("Longitude: \(location.longitude)")
                
                Spacer()
                
                Button("Close") {
                    presentationMode.wrappedValue.dismiss()
                }
                .padding()
                .background(Color.blue)
                .foregroundColor(.white)
                .cornerRadius(10)
            }
            .padding()
            .navigationBarTitle("Test Forecast", displayMode: .inline)
            .navigationBarItems(trailing: Button("Done") {
                presentationMode.wrappedValue.dismiss()
            })
        }
    }
}