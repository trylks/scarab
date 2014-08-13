package com.github.trylks.scarab

import java.io.Closeable

object ScarabCommons {
    def using[T <% Closeable, R](resource: T)(block: (T => R)): R = {
        try {
            block(resource)
        } finally {
            resource.close()
        }
    }
}