//
//  ContentView.swift
//  meow
//
//  Created by Jacky on 2020/07/04.
//

import SwiftUI
import GridStack
struct ContentView: View {
    var body: some View {
        NavigationView {
            GridStack(minCellWidth: 110, spacing: 2, numItems: 27) { index, cellWidth in
                Cat(dataIndex: index).frame(width: cellWidth, height: cellWidth, alignment: /*@START_MENU_TOKEN@*/.center/*@END_MENU_TOKEN@*/)
            }
            .navigationTitle(Text("title"))
            .navigationBarItems(trailing:
                HStack {
                    Button("🐱") {
                        print("About tapped!")
                    }
                }
            )
            .background(NavigationConfigurator { nc in
                nc.navigationBar.barTintColor = Color(hex: 0xE76A66).uiColor()
                nc.navigationBar.titleTextAttributes = [.foregroundColor : UIColor.white]
            })
        }
        .edgesIgnoringSafeArea(.bottom)
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
            .previewDevice("iPhone 11")
            
    }
}

struct NavigationConfigurator: UIViewControllerRepresentable {
    var configure: (UINavigationController) -> Void = { _ in }

    func makeUIViewController(context: UIViewControllerRepresentableContext<NavigationConfigurator>) -> UIViewController {
        UIViewController()
    }
    func updateUIViewController(_ uiViewController: UIViewController, context: UIViewControllerRepresentableContext<NavigationConfigurator>) {
        if let nc = uiViewController.navigationController {
            self.configure(nc)
        }
    }

}
