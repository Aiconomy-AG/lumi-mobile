import UIKit
import SwiftUI
import Shared

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Self.Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Self.Context) {}
}

struct ContentView: View {
    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            ComposeView().ignoresSafeArea()
#if DEBUG
            Button("Debug CallKit") {
                LumiCallManager.shared.debugIncomingCall()
            }
            .buttonStyle(.borderedProminent)
            .padding()
#endif
        }
    }
}
