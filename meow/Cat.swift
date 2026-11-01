//
//  Cat.swift
//  meow
//
//  Created by Jacky on 2020/07/05.
//

import SwiftUI

struct CatSound: Identifiable, Equatable {
    static let all = WidgetSelection.soundIDs.indices.map { CatSound(id: $0) }

    let id: Int
    var soundID: String { WidgetSelection.soundIDs[id] }
    var imageName: String { String(format: "c%02d", id + 1) }
    var titleKey: LocalizedStringKey { LocalizedStringKey(String(format: "t%02d", id + 1)) }
}

struct Cat: View {
    let sound: CatSound
    var selected = false

    var body: some View {
        VStack(spacing: 10) {
            Image(sound.imageName)
                .renderingMode(.original)
                .resizable()
                .scaledToFit()
                .frame(height: 64)
                .accessibilityHidden(true)
            Text(sound.titleKey)
                .font(.system(.caption, design: .rounded).weight(.semibold))
                .foregroundColor(.primary)
                .multilineTextAlignment(.center)
                .fixedSize(horizontal: false, vertical: true)
                .frame(maxWidth: .infinity, minHeight: 32, alignment: .top)
        }
        .padding(12)
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
        .background(RoundedRectangle(cornerRadius: 24, style: .continuous)
            .fill(selected ? Color("lightYellow") : Color("lightPink")))
        .overlay(RoundedRectangle(cornerRadius: 24, style: .continuous)
            .strokeBorder(selected ? Color(hex: 0xE76A66) : .clear, lineWidth: 2))
        .overlay(alignment: .topTrailing) {
            if selected {
                Image(systemName: "checkmark.circle.fill")
                    .foregroundColor(Color(hex: 0xE76A66))
                    .padding(8)
                    .accessibilityHidden(true)
            }
        }
    }
}

struct Cat_Previews: PreviewProvider {
    static var previews: some View {
        Group {
            Cat(sound: CatSound.all[0])
                .previewLayout(.sizeThatFits)
            Cat(sound: CatSound.all[1])
                .preferredColorScheme(.dark)
                .previewLayout(.sizeThatFits)
        }
    }
}
