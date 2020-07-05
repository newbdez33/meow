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
