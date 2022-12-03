/*
 * Copyright (c) 2022 GroovyMC
 * SPDX-License-Identifier: MIT
 */

package modsdotgroovy

import groovy.transform.CompileStatic

@CompileStatic
class PackMcMetaBuilder extends HashMap {
    void setDescription(String description) {
        packMap['description'] = description
    }

    void setPackFormat(int packFormat) {
        packMap['pack_format'] = packFormat
    }

    void setForgeResourcePackFormat(int packFormat) {
        packMap['forge:resource_pack_format'] = packFormat
    }

    void setForgeDataPackFormat(int packFormat) {
        packMap['forge:data_pack_format'] = packFormat
    }

    private Map getPackMap() {
        (Map)computeIfAbsent('pack') { new HashMap<>() }
    }
}
