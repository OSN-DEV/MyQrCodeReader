package jp.gr.javaconf.tenpokei.myqrcodereader.event

/**
 * fired this event when side menu item is selected
 */
class SideMenuSelectedEvent(itemType:MenuItemType) {

    //==============================================================================================
    // Declaration
    //==============================================================================================
    enum class MenuItemType(val rawValue: Int) {
        Recent(1),
        License(2)
    }
    private val _item = itemType;

    //==============================================================================================
    // Public Method
    //==============================================================================================
    /**
     * getter
     */
    public fun getItemType() : MenuItemType {
        return this._item
    }

}