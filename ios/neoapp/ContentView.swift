import SwiftUI

struct ContentView: View {
    var body: some View {
        VStack(spacing: 24) {
            // Logo
            ZStack {
                RoundedRectangle(cornerRadius: 20)
                    .fill(
                        LinearGradient(
                            colors: [Color(hex: "0052D9"), Color(hex: "618DFF")],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .frame(width: 80, height: 80)

                Text("N")
                    .font(.system(size: 36, weight: .bold))
                    .foregroundColor(.white)
            }
            .padding(.top, 60)

            Text("NeoApp")
                .font(.largeTitle)
                .fontWeight(.bold)
                .foregroundColor(Color(hex: "1D2129"))

            Text("让业务管理更简单高效")
                .font(.body)
                .foregroundColor(Color(hex: "86909C"))

            Spacer()

            Text("v1.0.0")
                .font(.caption)
                .foregroundColor(Color(hex: "86909C"))
                .padding(.bottom, 40)
        }
        .frame(maxWidth: .infinity)
        .background(Color(hex: "F3F3F3"))
    }
}

extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 6:
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8:
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (255, 0, 0, 0)
        }
        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue: Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}

#Preview {
    ContentView()
}
