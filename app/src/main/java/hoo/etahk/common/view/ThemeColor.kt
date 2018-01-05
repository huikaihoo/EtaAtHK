package hoo.etahk.common.view

import hoo.etahk.view.App

class ThemeColor{

    constructor(_colorPrimaryResId: Int, _colorPrimaryDarkResId: Int, _colorPrimaryAccentResId: Int) {
        this.colorPrimaryResId = _colorPrimaryResId
        this.colorPrimaryDarkResId = _colorPrimaryDarkResId
        this.colorPrimaryAccentResId = _colorPrimaryAccentResId
    }

    var colorPrimaryResId: Int
        set(value) {
            colorPrimary = App.instance.getColor(value)
        }

    var colorPrimaryDarkResId: Int
        set(value) {
            colorPrimaryDark = App.instance.getColor(value)
        }

    var colorPrimaryAccentResId: Int
        set(value) {
            colorPrimaryAccent = App.instance.getColor(value)
        }

    var colorPrimary: Int = 0
    var colorPrimaryDark: Int = 0
    var colorPrimaryAccent: Int = 0
}