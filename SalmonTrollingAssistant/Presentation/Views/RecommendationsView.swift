import SwiftUI

struct RecommendationsView: View {
    let recommendations: [EquipmentRecommendation]
    
    var body: some View {
        NavigationView {
            List {
                ForEach(recommendations) { recommendation in
                    Section(header: Text(recommendation.type.rawValue)) {
                        ForEach(recommendation.items) { item in
                            VStack(alignment: .leading, spacing: 5) {
                                Text(item.name)
                                    .font(.headline)
                                
                                Text(item.description)
                                    .font(.subheadline)
                                    .foregroundColor(.secondary)
                                
                                HStack {
                                    ForEach(Array(item.specifications.keys.sorted()), id: \.self) { key in
                                        if let value = item.specifications[key] {
                                            Text("\(key): \(value)")
                                                .font(.caption)
                                                .padding(4)
                                                .background(Color.blue.opacity(0.1))
                                                .cornerRadius(4)
                                        }
                                    }
                                }
                                
                                Text("Confidence: \(Int(recommendation.confidenceScore * 100))%")
                                    .font(.caption)
                                    .foregroundColor(confidenceColor(for: recommendation.confidenceScore))
                            }
                            .padding(.vertical, 5)
                        }
                        
                        Text(recommendation.reasonForRecommendation)
                            .font(.caption)
                            .italic()
                            .padding(.top, 5)
                    }
                }
            }
            .navigationTitle("Recommendations")
        }
    }
    
    private func confidenceColor(for score: Float) -> Color {
        if score >= 0.8 {
            return .green
        } else if score >= 0.6 {
            return .blue
        } else if score >= 0.4 {
            return .yellow
        } else {
            return .orange
        }
    }
}

struct RecommendationsView_Previews: PreviewProvider {
    static var previews: some View {
        RecommendationsView(recommendations: EquipmentRecommendation.mockRecommendations())
    }
}