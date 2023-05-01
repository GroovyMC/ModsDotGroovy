import groovy.transform.CompileStatic

@CompileStatic
enum DependencyOrdering {
    BEFORE, AFTER, NONE

    DependencyOrdering() {}
}
