import SwiftUI

struct GearScanView: View {
    @Binding var userGear: [FishingGear]
    
    var body: some View {
        MyGearView(userGear: $userGear)
    }
}

struct GearScanView_Previews: PreviewProvider {
    @State static var previewGear: [FishingGear] = FishingGear.mockGear()
    
    static var previews: some View {
        GearScanView(userGear: $previewGear)
    }
}