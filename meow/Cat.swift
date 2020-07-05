//
//  Cat.swift
//  meow
//
//  Created by Jacky on 2020/07/05.
//

import SwiftUI

struct Cat: View {
    var dataIndex: Int
    
    var body: some View {

        Button(action: {
            Sounds.playSounds(soundfile: String(format: "m_%03d", dataIndex+1))
        }, label: {
            VStack {
                Image(String(format: "c%02d", dataIndex+1))
                    .renderingMode(.original)
                Text(LocalizedStringKey(String(format: "t%02d", dataIndex+1)))
                    .padding(.horizontal, 8)
                    .font(.caption)
                    .foregroundColor(Color(hex: 0xe76a66))
                    .background(RoundedRectangle(cornerRadius: 10, style: .continuous).fill(Color(hex: 0xFFF6F7)))
            }
            .background(RoundedRectangle(cornerRadius: 30, style: .continuous).fill(Color(hex: 0xffffe0)))   //selected: Color.clear
        })
        .padding(.all, 2)
            
        
    }
}

struct Cat_Previews: PreviewProvider {
    static var previews: some View {
        Group {
            Cat(dataIndex: 0)
                .previewLayout(.sizeThatFits)
            Cat(dataIndex: 1)
                .preferredColorScheme(.dark)
                .previewLayout(.sizeThatFits)
        }
    }
}
