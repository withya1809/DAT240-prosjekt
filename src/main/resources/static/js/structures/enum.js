/**
 * Enumerator class that maps the property to the string name
 */
class Enum {
    /**
     * @param enums {$Values} - Varargs of strings with enum names
     */
    constructor(...enums) {
        for (let i = 0; i < enums.length; i++) {
            this[enums[i]] = enums[i];
        }
    }
}