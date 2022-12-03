package modsdotgroovy

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

import static groovy.lang.Closure.DELEGATE_FIRST

@CompileStatic
class MixinConfigBuilder extends HashMap {
    MixinConfigBuilder() {
        setMinVersion('0.8')
        injectors {
            setDefaultRequire(1)
        }
        setRequired(true)
    }

    void setRefMap(String refMap) {
        put('refmap', refMap)
    }

    void setRequired(boolean required) {
        put('required', required)
    }

    void setPackageName(String packageName) {
        put('package', packageName)
    }

    void setCompatibilityLevel(int javaVersion) {
        put('compatibilityLevel', 'JAVA_' + javaVersion)
    }

    void setMinVersion(String minVersion) {
        put('minVersion', minVersion)
    }

    void injectors(@DelegatesTo(value = Injectors, strategy = DELEGATE_FIRST)
                    @ClosureParams(value = SimpleType, options = 'modsdotgroovy.MixinConfigBuilder$Injectors') final Closure closure) {
        final builder = new Injectors()
        closure.delegate = builder
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(builder)
        this['injectors'] = builder
    }

    void mixins(@DelegatesTo(value = Mixins, strategy = DELEGATE_FIRST)
                    @ClosureParams(value = SimpleType, options = 'modsdotgroovy.MixinConfigBuilder$Mixins') final Closure closure) {
        final builder = new Mixins()
        closure.delegate = builder
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call(builder)
        this['mixins'] = builder.common
        this['client'] = builder.client
        this['server'] = builder.server
    }

    static final class Injectors extends HashMap {
        void setDefaultRequire(int defaultRequire) {
            put('defaultRequire', defaultRequire)
        }
    }

    static final class Mixins {
        final List<String> common = []
        final List<String> client = []
        final List<String> server = []

        void common(String... commonMixins) {
            this.common.addAll(commonMixins)
        }

        void client(String... clientMixins) {
            this.client.addAll(clientMixins)
        }

        void server(String... serverMixins) {
            this.server.addAll(serverMixins)
        }
    }
}
