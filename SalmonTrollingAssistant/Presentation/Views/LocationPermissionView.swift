import SwiftUI
import CoreLocation
import Combine

struct LocationPermissionView: View {
    @ObservedObject private var viewModel: LocationPermissionViewModel
    
    init(locationService: LocationService) {
        self.viewModel = LocationPermissionViewModel(locationService: locationService)
    }
    
    var body: some View {
        VStack(spacing: 20) {
            Image(systemName: "location.circle.fill")
                .resizable()
                .scaledToFit()
                .frame(width: 100, height: 100)
                .foregroundColor(.blue)
            
            Text("Location Access Required")
                .font(.title)
                .fontWeight(.bold)
            
            Text("Salmon Trolling Assistant needs your location to provide accurate weather and tide information for your fishing spot.")
                .multilineTextAlignment(.center)
                .padding(.horizontal)
            
            if viewModel.permissionStatus == .denied {
                Text("You have denied location access. Please enable it in Settings to use all features.")
                    .foregroundColor(.red)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal)
                
                Button("Open Settings") {
                    if let url = URL(string: UIApplication.openSettingsURLString) {
                        UIApplication.shared.open(url)
                    }
                }
                .padding()
                .background(Color.blue)
                .foregroundColor(.white)
                .cornerRadius(10)
                
                Button("Continue Without Location") {
                    viewModel.continueWithoutLocation()
                }
                .padding()
                .foregroundColor(.blue)
            } else {
                Button("Allow Location Access") {
                    viewModel.requestLocationPermission()
                }
                .padding()
                .background(Color.blue)
                .foregroundColor(.white)
                .cornerRadius(10)
                
                Button("Continue Without Location") {
                    viewModel.continueWithoutLocation()
                }
                .padding()
                .foregroundColor(.blue)
            }
        }
        .padding()
    }
}

class LocationPermissionViewModel: ObservableObject {
    private let locationService: LocationService
    private var cancellables = Set<AnyCancellable>()
    
    @Published var permissionStatus: LocationPermissionStatus = .notDetermined
    @Published var shouldProceed: Bool = false
    
    init(locationService: LocationService) {
        self.locationService = locationService
        
        locationService.permissionStatus
            .receive(on: DispatchQueue.main)
            .sink { [weak self] status in
                self?.permissionStatus = status
                if status.isAuthorized {
                    self?.shouldProceed = true
                }
            }
            .store(in: &cancellables)
    }
    
    func requestLocationPermission() {
        locationService.requestLocationPermission()
    }
    
    func continueWithoutLocation() {
        shouldProceed = true
    }
}

struct LocationPermissionView_Previews: PreviewProvider {
    static var previews: some View {
        LocationPermissionView(locationService: LocationServiceImpl())
    }
}