import SwiftUI

struct SmartRecsButton: View {
    @Binding var selectedTabIndex: Int
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        Button(action: {
            // Navigate to Smart Recommendations tab (index 2)
            print("Smart Recommendations button tapped, navigating to tab 2")
            selectedTabIndex = 2
            presentationMode.wrappedValue.dismiss()
        }) {
            HStack {
                Image(systemName: "wand.and.stars")
                    .font(.title2)
                Text("Get Smart Recommendations")
                    .fontWeight(.semibold)
            }
            .frame(maxWidth: .infinity)
            .padding()
            .background(Color.blue)
            .foregroundColor(.white)
            .cornerRadius(10)
        }
        .padding(.horizontal)
        .padding(.top, 10)
    }
}

struct SmartRecsButton_Previews: PreviewProvider {
    static var previews: some View {
        SmartRecsButton(selectedTabIndex: .constant(0))
    }
}