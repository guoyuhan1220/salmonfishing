import SwiftUI
import MapKit
import CoreLocation

struct SimpleMapView: View {
    @State private var mapRegion = MKCoordinateRegion(
        center: CLLocationCoordinate2D(latitude: 47.6062, longitude: -122.3321),
        span: MKCoordinateSpan(latitudeDelta: 0.1, longitudeDelta: 0.1)
    )
    @State private var showingAddressConfirmation = false
    @State private var pinLocation: CLLocationCoordinate2D?
    @State private var addressDetails: String = ""
    @State private var pinTitle: String = ""
    @State private var showingTextFieldAlert = false
    @State private var pins: [MapPin] = []
    
    var body: some View {
        ZStack {
            // Full screen map
            Map(coordinateRegion: $mapRegion, showsUserLocation: true, annotationItems: pins) { pin in
                MapAnnotation(coordinate: pin.coordinate) {
                    VStack {
                        Image(systemName: "mappin.circle.fill")
                            .font(.title)
                            .foregroundColor(.red)
                        
                        if !pin.title.isEmpty {
                            Text(pin.title)
                                .font(.caption)
                                .padding(5)
                                .background(Color.white.opacity(0.8))
                                .cornerRadius(5)
                        }
                    }
                }
            }
            .ignoresSafeArea()
            .gesture(
                LongPressGesture(minimumDuration: 0.5)
                    .sequenced(before: DragGesture(minimumDistance: 0))
                    .onEnded { value in
                        switch value {
                        case .second(true, let drag):
                            if let location = drag?.location {
                                let coordinate = convertToCoordinate(location)
                                pinLocation = coordinate
                                lookupAddress(for: coordinate) { address in
                                    addressDetails = address
                                    showingAddressConfirmation = true
                                }
                            }
                        default:
                            break
                        }
                    }
            )
            
            // Controls overlay
            VStack {
                HStack {
                    Spacer()
                    
                    Button(action: {
                        // Get current location
                    }) {
                        Image(systemName: "location")
                            .font(.title2)
                            .padding(12)
                            .background(Color.white)
                            .clipShape(Circle())
                            .shadow(radius: 3)
                    }
                }
                .padding()
                
                Spacer()
                
                // Instructions
                Text("Long press to drop a pin")
                    .font(.caption)
                    .padding(8)
                    .background(Color.white.opacity(0.8))
                    .cornerRadius(8)
                    .padding(.bottom)
            }
        }
        .alert(isPresented: $showingAddressConfirmation) {
            Alert(
                title: Text("Confirm Location"),
                message: Text(addressDetails),
                primaryButton: .default(Text("Use This Location")) {
                    showingTextFieldAlert = true
                },
                secondaryButton: .cancel()
            )
        }
        .overlay(
            Group {
                if showingTextFieldAlert {
                    TextFieldAlert(
                        title: "Name this location",
                        message: "Enter a name for this fishing spot",
                        text: $pinTitle,
                        isShowing: $showingTextFieldAlert,
                        onSave: {
                            if let coordinate = pinLocation {
                                addPin(
                                    title: pinTitle,
                                    coordinate: coordinate,
                                    address: addressDetails
                                )
                                pinTitle = ""
                            }
                        }
                    )
                }
            }
        )
    }
    
    private func convertToCoordinate(_ point: CGPoint) -> CLLocationCoordinate2D {
        let mapSize = UIScreen.main.bounds.size
        
        let normalizedPoint = CGPoint(
            x: point.x / mapSize.width,
            y: point.y / mapSize.height
        )
        
        let span = mapRegion.span
        let center = mapRegion.center
        
        let xOffset = span.longitudeDelta * (normalizedPoint.x - 0.5)
        let yOffset = span.latitudeDelta * (0.5 - normalizedPoint.y)
        
        return CLLocationCoordinate2D(
            latitude: center.latitude + yOffset,
            longitude: center.longitude + xOffset
        )
    }
    
    private func lookupAddress(for coordinate: CLLocationCoordinate2D, completion: @escaping (String) -> Void) {
        let geocoder = CLGeocoder()
        let location = CLLocation(latitude: coordinate.latitude, longitude: coordinate.longitude)
        
        geocoder.reverseGeocodeLocation(location) { placemarks, error in
            if let error = error {
                print("Reverse geocoding error: \(error.localizedDescription)")
                completion("Unknown location")
                return
            }
            
            guard let placemark = placemarks?.first else {
                completion("Unknown location")
                return
            }
            
            var addressComponents: [String] = []
            
            if let name = placemark.name {
                addressComponents.append(name)
            }
            
            if let thoroughfare = placemark.thoroughfare {
                addressComponents.append(thoroughfare)
            }
            
            if let locality = placemark.locality {
                addressComponents.append(locality)
            }
            
            if let administrativeArea = placemark.administrativeArea {
                addressComponents.append(administrativeArea)
            }
            
            if let postalCode = placemark.postalCode {
                addressComponents.append(postalCode)
            }
            
            let addressString = addressComponents.joined(separator: ", ")
            completion(addressString.isEmpty ? "Unknown location" : addressString)
        }
    }
    
    private func addPin(title: String, coordinate: CLLocationCoordinate2D, address: String) {
        let newPin = MapPin(
            id: UUID().uuidString,
            title: title,
            coordinate: coordinate,
            address: address
        )
        
        pins.append(newPin)
    }
}

// Custom TextFieldAlert since SwiftUI doesn't natively support text fields in alerts
struct TextFieldAlert: View {
    let title: String
    let message: String
    @Binding var text: String
    @Binding var isShowing: Bool
    let onSave: () -> Void
    
    var body: some View {
        ZStack {
            Color.black.opacity(0.4)
                .edgesIgnoringSafeArea(.all)
                .onTapGesture {
                    isShowing = false
                }
            
            VStack(spacing: 20) {
                Text(title)
                    .font(.headline)
                
                Text(message)
                    .font(.subheadline)
                
                TextField("Location name", text: $text)
                    .textFieldStyle(RoundedBorderTextFieldStyle())
                    .padding(.horizontal)
                
                HStack {
                    Button("Cancel") {
                        isShowing = false
                    }
                    .foregroundColor(.red)
                    
                    Spacer()
                    
                    Button("Save") {
                        onSave()
                        isShowing = false
                    }
                    .foregroundColor(.blue)
                }
                .padding(.horizontal)
            }
            .padding()
            .background(Color.white)
            .cornerRadius(15)
            .shadow(radius: 10)
            .padding(30)
        }
    }
}

struct MapPin: Identifiable {
    let id: String
    let title: String
    let coordinate: CLLocationCoordinate2D
    let address: String
}

struct SimpleMapView_Previews: PreviewProvider {
    static var previews: some View {
        SimpleMapView()
    }
}