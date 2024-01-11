import groovy.transform.CompileStatic

@CompileStatic
enum DependencySide {
    CLIENT, SERVER, BOTH

    DependencySide() {}
}
