import SwiftUI

struct RecommendationsView: View {
    var body: some View {
        VStack {
            Text("Recommendations View")
                .font(.title)
                .padding()
            
            Text("This view would show fishing equipment recommendations")
                .padding()
        }
    }
}

struct RecommendationsView_Previews: PreviewProvider {
    static var previews: some View {
        RecommendationsView()
    }
}