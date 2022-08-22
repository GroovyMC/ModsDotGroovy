

import groovy.transform.CompileStatic

@CompileStatic
class QuiltDotGroovy extends ModsDotGroovy {
    void put(String name, Object value) {
        quiltData[name] = value
    }
}
