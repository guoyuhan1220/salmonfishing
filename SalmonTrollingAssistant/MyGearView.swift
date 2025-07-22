import SwiftUI

struct MyGearView: View {
    @Binding var userGear: [FishingGear]
    
    var body: some View {
        NavigationView {
            VStack {
                if userGear.isEmpty {
                    // Empty state
                    VStack(spacing: 20) {
                        Spacer()
                        
                        Image(systemName: "camera")
                            .font(.system(size: 60))
                            .foregroundColor(.blue)
                        
                        Text("No Gear Added Yet")
                            .font(.title2)
                            .fontWeight(.bold)
                        
                        Text("Add your fishing gear to get personalized recommendations")
                            .multilineTextAlignment(.center)
                            .foregroundColor(.secondary)
                            .padding(.horizontal, 40)
                        
                        Button(action: {
                            // Add sample gear for demo
                            userGear = FishingGear.mockGear()
                        }) {
                            Text("Add Sample Gear")
                                .padding()
                                .background(Color.blue)
                                .foregroundColor(.white)
                                .cornerRadius(10)
                        }
                        .padding(.top)
                        
                        Spacer()
                    }
                } else {
                    // Gear list
                    List {
                        ForEach(GearType.allCases, id: \.self) { gearType in
                            let gearOfType = userGear.filter { $0.type == gearType }
                            
                            if !gearOfType.isEmpty {
                                Section(header: Text(gearType.rawValue + "s")) {
                                    ForEach(gearOfType) { gear in
                                        HStack {
                                            Image(systemName: gearType.icon)
                                                .font(.title2)
                                                .foregroundColor(.blue)
                                                .frame(width: 40, height: 40)
                                                .background(Color.blue.opacity(0.1))
                                                .clipShape(Circle())
                                            
                                            VStack(alignment: .leading, spacing: 4) {
                                                Text(gear.name)
                                                    .font(.headline)
                                                
                                                Text("\(gear.brand) • \(gear.color) • \(gear.size)")
                                                    .font(.subheadline)
                                                    .foregroundColor(.secondary)
                                            }
                                            
                                            Spacer()
                                        }
                                    }
                                }
                            }
                        }
                    }
                    .listStyle(InsetGroupedListStyle())
                }
            }
            .navigationTitle("My Gear")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: {
                        // Add sample gear for demo
                        userGear = FishingGear.mockGear()
                    }) {
                        Image(systemName: "plus")
                    }
                }
            }
        }
    }
}